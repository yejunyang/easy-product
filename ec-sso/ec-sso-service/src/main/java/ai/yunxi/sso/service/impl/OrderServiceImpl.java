package ai.yunxi.sso.service.impl;

import ai.yunxi.common.exception.CommonException;
import ai.yunxi.common.redis.JedisClient;
import ai.yunxi.core.domain.dto.DtoUtil;
import ai.yunxi.core.domain.dto.front.CartProduct;
import ai.yunxi.core.domain.dto.front.Order;
import ai.yunxi.core.domain.dto.front.OrderInfo;
import ai.yunxi.core.domain.dto.front.PageOrder;
import ai.yunxi.core.domain.entity.*;
import ai.yunxi.core.domain.mapper.TbMemberMapper;
import ai.yunxi.core.domain.mapper.TbOrderItemMapper;
import ai.yunxi.core.domain.mapper.TbOrderMapper;
import ai.yunxi.core.domain.mapper.TbOrderShippingMapper;
import ai.yunxi.sso.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.shardingsphere.core.keygen.DefaultKeyGenerator;
import io.shardingsphere.core.keygen.KeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final static Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Value("${CART_PRE}")
    private String CART_PRE;

    @Value("${PAY_EXPIRE}")
    private int PAY_EXPIRE;

    @Autowired
    private TbMemberMapper tbMemberMapper;
    @Autowired
    private TbOrderMapper tbOrderMapper;
    @Autowired
    private TbOrderItemMapper tbOrderItemMapper;
    @Autowired
    private TbOrderShippingMapper tbOrderShippingMapper;

    @Autowired
    private JedisClient jedisClient;

    @Override
    public PageOrder getOrderList(Long userId, int page, int size) {
        //分页
        if (page <= 0) {
            page = 1;
        }

        PageHelper.startPage(page, size);
        PageOrder pageOrder = new PageOrder();
        List<Order> list = new ArrayList<>();
        TbOrderExample example = new TbOrderExample();
        TbOrderExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(userId);
        example.setOrderByClause("create_time DESC");
        List<TbOrder> listOrder = tbOrderMapper.selectByExample(example);
        for (TbOrder tbOrder : listOrder) {
            judgeOrder(tbOrder);
            Order order = new Order();
            //orderId
            order.setOrderId(Long.valueOf(tbOrder.getOrderId()));
            //orderStatus
            order.setOrderStatus(String.valueOf(tbOrder.getStatus()));
            //createDate
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String date = formatter.format(tbOrder.getCreateTime());
            order.setCreateDate(date);
            //address
            TbOrderShipping tbOrderShipping = tbOrderShippingMapper.selectByPrimaryKey(tbOrder.getOrderId());
            TbAddress address = new TbAddress();
            address.setUserName(tbOrderShipping.getReceiverName());
            address.setStreetName(tbOrderShipping.getReceiverAddress());
            address.setTel(tbOrderShipping.getReceiverPhone());
            order.setAddressInfo(address);
            //orderTotal
            if (tbOrder.getPayment() == null) {
                order.setOrderTotal(new BigDecimal(0));
            } else {
                order.setOrderTotal(tbOrder.getPayment());
            }
            //goodsList
            TbOrderItemExample exampleItem = new TbOrderItemExample();
            TbOrderItemExample.Criteria criteriaItem = exampleItem.createCriteria();
            criteriaItem.andOrderIdEqualTo(tbOrder.getOrderId());
            List<TbOrderItem> listItem = tbOrderItemMapper.selectByExample(exampleItem);
            List<CartProduct> listProduct = new ArrayList<>();
            for (TbOrderItem tbOrderItem : listItem) {
                CartProduct cartProduct = DtoUtil.TbOrderItem2CartProduct(tbOrderItem);
                listProduct.add(cartProduct);
            }
            order.setGoodsList(listProduct);
            list.add(order);
        }
        PageInfo<Order> pageInfo = new PageInfo<>(list);
        pageOrder.setTotal(getMemberOrderCount(userId));
        pageOrder.setData(list);
        return pageOrder;
    }

    @Override
    public Order getOrder(Long orderId) {
        Order order = new Order();
        TbOrder tbOrder = tbOrderMapper.selectByPrimaryKey(String.valueOf(orderId));
        if (tbOrder == null) {
            throw new CommonException("通过id获取订单失败");
        }

        String validTime = judgeOrder(tbOrder);
        if (validTime != null) {
            order.setFinishDate(validTime);
        }

        //orderId
        order.setOrderId(Long.valueOf(tbOrder.getOrderId()));
        //orderStatus
        order.setOrderStatus(String.valueOf(tbOrder.getStatus()));
        //createDate
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String createDate = formatter.format(tbOrder.getCreateTime());
        order.setCreateDate(createDate);
        //payDate
        if (tbOrder.getPaymentTime() != null) {
            String payDate = formatter.format(tbOrder.getPaymentTime());
            order.setPayDate(payDate);
        }
        //closeDate
        if (tbOrder.getCloseTime() != null) {
            String closeDate = formatter.format(tbOrder.getCloseTime());
            order.setCloseDate(closeDate);
        }
        //finishDate
        if (tbOrder.getEndTime() != null && tbOrder.getStatus() == 4) {
            String finishDate = formatter.format(tbOrder.getEndTime());
            order.setFinishDate(finishDate);
        }
        //address
        TbOrderShipping tbOrderShipping = tbOrderShippingMapper.selectByPrimaryKey(tbOrder.getOrderId());
        TbAddress address = new TbAddress();
        address.setUserName(tbOrderShipping.getReceiverName());
        address.setStreetName(tbOrderShipping.getReceiverAddress());
        address.setTel(tbOrderShipping.getReceiverPhone());
        order.setAddressInfo(address);
        //orderTotal
        if (tbOrder.getPayment() == null) {
            order.setOrderTotal(new BigDecimal(0));
        } else {
            order.setOrderTotal(tbOrder.getPayment());
        }
        //goodsList
        TbOrderItemExample exampleItem = new TbOrderItemExample();
        TbOrderItemExample.Criteria criteriaItem = exampleItem.createCriteria();
        criteriaItem.andOrderIdEqualTo(tbOrder.getOrderId());
        List<TbOrderItem> listItem = tbOrderItemMapper.selectByExample(exampleItem);
        List<CartProduct> listProduct = new ArrayList<>();
        for (TbOrderItem tbOrderItem : listItem) {
            CartProduct cartProduct = DtoUtil.TbOrderItem2CartProduct(tbOrderItem);
            listProduct.add(cartProduct);
        }

        order.setGoodsList(listProduct);
        return order;
    }

    @Override
    public int cancelOrder(Long orderId) {
        TbOrder tbOrder = tbOrderMapper.selectByPrimaryKey(String.valueOf(orderId));
        if (tbOrder == null) {
            throw new CommonException("通过id获取订单失败");
        }
        tbOrder.setStatus(5);
        tbOrder.setCloseTime(new Date());
        if (tbOrderMapper.updateByPrimaryKey(tbOrder) != 1) {
            throw new CommonException("取消订单失败");
        }
        return 1;
    }

    @Override
    public Long createOrder(OrderInfo orderInfo) {
        TbMember member = tbMemberMapper.selectByPrimaryKey(Long.valueOf(orderInfo.getUserId()));
        if (member == null) {
            throw new CommonException("获取下单用户失败");
        }

        TbOrder order = new TbOrder();
        // 分布式订单ID
        // 分库分表 snowflake 分片的热点问题
        // OrderId 分片策略
        // UserId  分片策略
        // Order hashcode
        KeyGenerator keyGenerator = new DefaultKeyGenerator();
        Long orderId = keyGenerator.generateKey().longValue();
        order.setOrderId(String.valueOf(orderId));
        order.setUserId(Long.valueOf(orderInfo.getUserId()));
        order.setBuyerNick(member.getUsername());
        order.setPayment(orderInfo.getOrderTotal());
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        //0、未付款，1、已付款，2、未发货，3、已发货，4、交易成功，5、交易关闭，6、交易失败
        order.setStatus(0);
        if (tbOrderMapper.insert(order) != 1) {
            throw new CommonException("生成订单失败");
        }

        List<CartProduct> list = orderInfo.getGoodsList();
        for (CartProduct cartProduct : list) {
            // TbOrderItem+TbOrder+TbOrderShipping ====》逻辑表
            TbOrderItem orderItem = new TbOrderItem();
            //生成订单商品ID
            Long orderItemId = keyGenerator.generateKey().longValue();
            orderItem.setId(String.valueOf(orderItemId));
            orderItem.setItemId(String.valueOf(cartProduct.getProductId()));
            orderItem.setOrderId(String.valueOf(orderId));
            orderItem.setNum(Math.toIntExact(cartProduct.getProductNum()));
            orderItem.setPrice(cartProduct.getSalePrice());
            orderItem.setTitle(cartProduct.getProductName());
            orderItem.setPicPath(cartProduct.getProductImg());
            orderItem.setTotalFee(cartProduct.getSalePrice().multiply(BigDecimal.valueOf(cartProduct.getProductNum())));
            if (tbOrderItemMapper.insert(orderItem) != 1) {
                throw new CommonException("生成订单商品失败");
            }

            // MQ去异步清空

            //删除购物车中含该订单的商品
            try {
                List<String> jsonList = jedisClient.hvals(CART_PRE + ":" + orderInfo.getUserId());
                for (String json : jsonList) {
                    ObjectMapper om = new ObjectMapper();
                    CartProduct cart = om.readValue(json, CartProduct.class);
                    if (cart.getProductId().equals(cartProduct.getProductId())) {
                        jedisClient.hdel(CART_PRE + ":" + orderInfo.getUserId(), cart.getProductId() + "");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 保存配送信息
        TbOrderShipping orderShipping = new TbOrderShipping();
        orderShipping.setOrderId(String.valueOf(orderId));
        orderShipping.setReceiverName(orderInfo.getUserName());
        orderShipping.setReceiverAddress(orderInfo.getStreetName());
        orderShipping.setReceiverPhone(orderInfo.getTel());
        orderShipping.setCreated(new Date());
        orderShipping.setUpdated(new Date());
        if (tbOrderShippingMapper.insert(orderShipping) != 1) {
            throw new CommonException("保存配送信息失败");
        }

        return orderId;
    }

    @Override
    public int delOrder(Long orderId) {
        if (tbOrderMapper.deleteByPrimaryKey(String.valueOf(orderId)) != 1) {
            throw new CommonException("删除订单失败");
        }

        TbOrderItemExample example = new TbOrderItemExample();
        TbOrderItemExample.Criteria criteria = example.createCriteria();
        criteria.andOrderIdEqualTo(String.valueOf(orderId));
        List<TbOrderItem> list = tbOrderItemMapper.selectByExample(example);
        for (TbOrderItem tbOrderItem : list) {
            if (tbOrderItemMapper.deleteByPrimaryKey(tbOrderItem.getId()) != 1) {
                throw new CommonException("删除订单商品失败");
            }
        }

        if (tbOrderShippingMapper.deleteByPrimaryKey(String.valueOf(orderId)) != 1) {
            throw new CommonException("删除物流失败");
        }
        return 1;
    }

    @Override
    public int payOrder(TbOrder tbOrder) {
        // 设置订单为已付款
        TbOrder order = tbOrderMapper.selectByPrimaryKey(tbOrder.getOrderId());
        // order.setStatus(1); // 待支付
        order.setUpdateTime(new Date());
        order.setPaymentTime(new Date());
        if (tbOrderMapper.updateByPrimaryKey(order) != 1) {
            throw new CommonException("更新订单失败");
        }

        // 插一张表
        // 支付流水表
        // 任务调用支付接口

        // 支付流水表
        // select * from tb_order where pay_status = 0;
        List<TbOrder> list; //= tbMemberMapper.selectByPrimaryKey(tbOrder.getOrderId());
        // MQ 发送消息

        // setnx 10%
        // MQ

        String tokenName = UUID.randomUUID().toString();
        String token = UUID.randomUUID().toString();
        //设置验证token键值对 tokenName:token
        jedisClient.set(tokenName, token);
        jedisClient.expire(tokenName, PAY_EXPIRE);
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
                    throw new CommonException("更新订单失效失败");
                }
            } else {
                //返回到期时间
                long time = tbOrder.getCreateTime().getTime() + 1000 * 60 * 60 * 24;
                result = String.valueOf(time);
            }
        }
        return result;
    }

    public int getMemberOrderCount(Long userId) {
        TbOrderExample example = new TbOrderExample();
        TbOrderExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(userId);
        List<TbOrder> listOrder = tbOrderMapper.selectByExample(example);
        if (listOrder != null) {
            return listOrder.size();
        }
        return 0;
    }
}
