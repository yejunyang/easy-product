package ai.yunxi.sso.service;

import ai.yunxi.core.domain.dto.front.Order;
import ai.yunxi.core.domain.dto.front.OrderInfo;
import ai.yunxi.core.domain.dto.front.PageOrder;
import ai.yunxi.core.domain.entity.TbOrder;

public interface OrderService {

    /**
     * 分页获得用户订单
     *
     * @param userId
     * @param page
     * @param size
     * @return
     */
    PageOrder getOrderList(Long userId, int page, int size);

    /**
     * 获得单个订单
     *
     * @param orderId
     * @return
     */
    Order getOrder(Long orderId);

    /**
     * 取消订单
     *
     * @param orderId
     * @return
     */
    int cancelOrder(Long orderId);

    /**
     * 创建订单
     *
     * @param orderInfo
     * @return
     */
    Long createOrder(OrderInfo orderInfo);

    /**
     * 删除订单
     *
     * @param orderId
     * @return
     */
    int delOrder(Long orderId);

    /**
     * 订单支付
     *
     * @param tbOrder
     * @return
     */
    int payOrder(TbOrder tbOrder);
}
