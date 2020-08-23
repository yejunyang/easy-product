package ai.yunxi.core.service;

import ai.yunxi.common.bean.DataTablesResult;
import ai.yunxi.core.domain.dto.OrderDetail;

import java.math.BigDecimal;

public interface OrderService {

    /**
     * 获得订单列表
     *
     * @param draw
     * @param start
     * @param length
     * @param search
     * @param orderCol
     * @param orderDir
     * @return
     */
    DataTablesResult getOrderList(int draw, int start, int length, String search, String orderCol, String orderDir);

    /**
     * 统计订单数
     *
     * @return
     */
    Long countOrder();

    /**
     * 获取订单详情
     *
     * @param orderId
     * @return
     */
    OrderDetail getOrderDetail(String orderId);

    /**
     * 发货
     *
     * @param orderId
     * @param shippingName
     * @param shippingCode
     * @param postFee
     * @return
     */
    int deliver(String orderId, String shippingName, String shippingCode, BigDecimal postFee);

    /**
     * 备注
     *
     * @param orderId
     * @param message
     * @return
     */
    int remark(String orderId, String message);

    /**
     * 取消订单
     *
     * @param orderId
     * @return
     */
    int cancelOrderByAdmin(String orderId);

    /**
     * 删除订单
     *
     * @param id
     * @return
     */
    int deleteOrder(String id);

    /**
     * 定时取消订单
     */
    int cancelOrder();

    /**
     * 支付审核通过
     *
     * @param tokenName
     * @param token
     * @param id
     * @return
     */
    int passPay(String tokenName, String token, String id);

    /**
     * 支付审核驳回
     *
     * @param tokenName
     * @param token
     * @param id
     * @return
     */
    int backPay(String tokenName, String token, String id);

    /**
     * 支付审核通过不显示
     *
     * @param tokenName
     * @param token
     * @param id
     * @return
     */
    int notShowPay(String tokenName, String token, String id);
}
