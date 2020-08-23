package ai.yunxi.core.service.impl;

import ai.yunxi.common.bean.DataTablesResult;
import ai.yunxi.common.exception.CommonException;
import ai.yunxi.common.redis.JedisClient;
import ai.yunxi.core.domain.dto.OrderDetail;
import ai.yunxi.core.domain.entity.*;
import ai.yunxi.core.domain.mapper.TbOrderItemMapper;
import ai.yunxi.core.domain.mapper.TbOrderMapper;
import ai.yunxi.core.domain.mapper.TbOrderShippingMapper;
import ai.yunxi.core.service.OrderService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TbOrderMapper tbOrderMapper;

    @Autowired
    private TbOrderItemMapper tbOrderItemMapper;

    @Autowired
    private TbOrderShippingMapper tbOrderShippingMapper;

    @Autowired
    private JedisClient jedisClient;

    @Autowired
    private EmailUtil emailUtil;

    @Override
    public DataTablesResult getOrderList(int draw, int start, int length, String search, String orderCol, String orderDir) {
        DataTablesResult result = new DataTablesResult();
        //分页
        PageHelper.startPage(start / length + 1, length);
        List<TbOrder> list = tbOrderMapper.selectByMulti("%" + search + "%", orderCol, orderDir);
        PageInfo<TbOrder> pageInfo = new PageInfo<>(list);
        result.setRecordsFiltered((int) pageInfo.getTotal());
        result.setRecordsTotal(Math.toIntExact(cancelOrder()));
        result.setDraw(draw);
        result.setData(list);
        return result;
    }

    @Override
    public Long countOrder() {
        TbOrderExample example = new TbOrderExample();
        Long result = tbOrderMapper.countByExample(example);
        if (result == null) {
            throw new CommonException("统计订单数目失败");
        }
        return result;
    }

    @Override
    public OrderDetail getOrderDetail(String orderId) {
        OrderDetail orderDetail = new OrderDetail();
        TbOrder tbOrder = tbOrderMapper.selectByPrimaryKey(orderId);
        TbOrderItemExample example = new TbOrderItemExample();
        TbOrderItemExample.Criteria criteria = example.createCriteria();
        criteria.andOrderIdEqualTo(orderId);
        List<TbOrderItem> orderItemList = tbOrderItemMapper.selectByExample(example);
        TbOrderShipping tbOrderShipping = tbOrderShippingMapper.selectByPrimaryKey(orderId);
        orderDetail.setTbOrder(tbOrder);
        orderDetail.setTbOrderItem(orderItemList);
        orderDetail.setTbOrderShipping(tbOrderShipping);
        return orderDetail;
    }

    @Override
    public int deliver(String orderId, String shippingName, String shippingCode, BigDecimal postFee) {
        TbOrder o = tbOrderMapper.selectByPrimaryKey(orderId);
        o.setShippingName(shippingName);
        o.setShippingCode(shippingCode);
        o.setPostFee(postFee);
        o.setConsignTime(new Date());
        o.setUpdateTime(new Date());
        //之前忘记设置常量了 将就这样看吧 0、未付款，1、已付款，2、未发货，3、已发货，4、交易成功，5、交易关闭
        o.setStatus(3);
        tbOrderMapper.updateByPrimaryKey(o);
        return 1;
    }

    @Override
    public int remark(String orderId, String message) {
        TbOrder o = tbOrderMapper.selectByPrimaryKey(orderId);
        o.setBuyerMessage(message);
        o.setUpdateTime(new Date());
        tbOrderMapper.updateByPrimaryKey(o);
        return 1;
    }

    @Override
    public int cancelOrderByAdmin(String orderId) {
        TbOrder o = tbOrderMapper.selectByPrimaryKey(orderId);
        o.setCloseTime(new Date());
        o.setUpdateTime(new Date());
        //之前忘记设置常量了 将就这样看吧 0、未付款，1、已付款，2、未发货，3、已发货，4、交易成功，5、交易关闭
        o.setStatus(5);
        tbOrderMapper.updateByPrimaryKey(o);
        return 1;
    }

    @Override
    public int deleteOrder(String id) {
        if (tbOrderMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException("删除订单数失败");
        }

        TbOrderItemExample example = new TbOrderItemExample();
        TbOrderItemExample.Criteria criteria = example.createCriteria();
        criteria.andOrderIdEqualTo(id);
        List<TbOrderItem> list = tbOrderItemMapper.selectByExample(example);
        for (TbOrderItem tbOrderItem : list) {
            if (tbOrderItemMapper.deleteByPrimaryKey(tbOrderItem.getId()) != 1) {
                throw new CommonException("删除订单商品失败");
            }
        }

        if (tbOrderShippingMapper.deleteByPrimaryKey(id) != 1) {
            throw new CommonException("删除物流失败");
        }
        return 1;
    }

    @Override
    public int cancelOrder() {
        TbOrderExample example = new TbOrderExample();
        List<TbOrder> list = tbOrderMapper.selectByExample(example);
        for (TbOrder tbOrder : list) {
            judgeOrder(tbOrder);
        }
        return 1;
    }

    /**
     * 判断订单是否超时未支付
     */
    public String judgeOrder(TbOrder tbOrder) {
        String result = null;
        if (tbOrder.getStatus() == 0) {
            //判断是否已超1天
            long diff = System.currentTimeMillis() - tbOrder.getCreateTime().getTime();
            long days = diff / (1000 * 60 * 60 * 24);
            if (days >= 1) {
                //设置失效
                tbOrder.setStatus(5);
                tbOrder.setCloseTime(new Date());
                if (tbOrderMapper.updateByPrimaryKey(tbOrder) != 1) {
                    throw new CommonException("设置订单关闭失败");
                }
            } else {
                //返回到期时间
                long time = tbOrder.getCreateTime().getTime() + 1000 * 60 * 60 * 24;
                result = String.valueOf(time);
            }
        }
        return result;
    }

    @Override
    public int passPay(String tokenName, String token, String id) {
        //验证token
        if (StringUtils.isBlank(tokenName) || StringUtils.isBlank(tokenName) || StringUtils.isBlank(id)) {
            return -1;
        }
        String value = jedisClient.get(tokenName);
        if (!value.equals(token)) {
            return -1;
        }
        //修改订单状态
        TbOrder tbOrder = tbOrderMapper.selectByPrimaryKey(id);
        if (tbOrder != null) {
            tbOrder.setStatus(4);
            tbOrder.setEndTime(new Date());
            tbOrder.setUpdateTime(new Date());
            if (tbOrderMapper.updateByPrimaryKey(tbOrder) != 1) {
                return 0;
            }
        }
        // 可增加同步发送短信通知
        return 1;
    }

    @Override
    public int backPay(String tokenName, String token, String id) {
        //验证token
        if (StringUtils.isBlank(tokenName) || StringUtils.isBlank(tokenName) || StringUtils.isBlank(id)) {
            return -1;
        }
        String value = jedisClient.get(tokenName);
        if (!value.equals(token)) {
            return -1;
        }
        //修改订单状态
        TbOrder tbOrder = tbOrderMapper.selectByPrimaryKey(id);
        if (tbOrder != null) {
            tbOrder.setStatus(6);
            tbOrder.setCloseTime(new Date());
            tbOrder.setUpdateTime(new Date());
            if (tbOrderMapper.updateByPrimaryKey(tbOrder) != 1) {
                return 0;
            }
        }
        // 可增加同步发送短信通知
        return 1;
    }

    @Override
    public int notShowPay(String tokenName, String token, String id) {
        //验证token
        if (StringUtils.isBlank(tokenName) || StringUtils.isBlank(tokenName) || StringUtils.isBlank(id)) {
            return -1;
        }
        String value = jedisClient.get(tokenName);
        if (!value.equals(token)) {
            return -1;
        }

        //修改订单状态
        TbOrder tbOrder = tbOrderMapper.selectByPrimaryKey(id);
        if (tbOrder != null) {
            tbOrder.setStatus(4);
            tbOrder.setEndTime(new Date());
            tbOrder.setUpdateTime(new Date());
            if (tbOrderMapper.updateByPrimaryKey(tbOrder) != 1) {
                return 0;
            }
        }
        // 可增加同步发送短信通知
        return 1;
    }
}
