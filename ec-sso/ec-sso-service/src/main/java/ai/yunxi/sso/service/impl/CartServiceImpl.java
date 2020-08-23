package ai.yunxi.sso.service.impl;

import ai.yunxi.common.redis.JedisClient;
import ai.yunxi.core.domain.dto.DtoUtil;
import ai.yunxi.core.domain.dto.front.CartProduct;
import ai.yunxi.core.domain.entity.TbItem;
import ai.yunxi.core.domain.mapper.TbItemMapper;
import ai.yunxi.sso.service.CartService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private final static Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    private JedisClient jedisClient;
    @Value("${CART_PRE}")
    private String CART_PRE;
    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public int addCart(long userId, long itemId, int num) {
        ObjectMapper om = new ObjectMapper();
        //hash: "key:用户id" field："商品id" value："商品信息"
        Boolean hexists = jedisClient.hexists(CART_PRE + ":" + userId, itemId + "");
        //如果存在数量相加
        if (hexists) {
            String json = jedisClient.hget(CART_PRE + ":" + userId, itemId + "");
            if (json != null) {
                try {
                    CartProduct cartProduct = om.readValue(json, CartProduct.class);
                    cartProduct.setProductNum(cartProduct.getProductNum() + num);
                    jedisClient.hset(CART_PRE + ":" + userId, itemId + "",
                            om.writeValueAsString(cartProduct));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                return 0;
            }

            return 1;
        }

        //如果不存在，根据商品id取商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            return 0;
        }
        CartProduct cartProduct = DtoUtil.TbItem2CartProduct(item);
        cartProduct.setProductNum((long) num);
        cartProduct.setChecked("1");
        try {
            jedisClient.hset(CART_PRE + ":" + userId, itemId + "",
                    om.writeValueAsString(cartProduct));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    public List<CartProduct> getCartList(long userId) {
        ObjectMapper om = new ObjectMapper();
        List<String> jsonList = jedisClient.hvals(CART_PRE + ":" + userId);
        List<CartProduct> list = new ArrayList<>();
        for (String json : jsonList) {
            try {
                CartProduct cartProduct = om.readValue(json, CartProduct.class);
                list.add(cartProduct);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public int updateCartNum(long userId, long itemId, int num, String checked) {
        ObjectMapper om = new ObjectMapper();
        String json = jedisClient.hget(CART_PRE + ":" + userId, itemId + "");
        if (json == null) {
            return 0;
        }

        CartProduct cartProduct = null;
        try {
            cartProduct = om.readValue(json, CartProduct.class);
            cartProduct.setProductNum((long) num);
            cartProduct.setChecked(checked);
            jedisClient.hset(CART_PRE + ":" + userId, itemId + "",
                    om.writeValueAsString(cartProduct));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    public int checkAll(long userId, String checked) {
        ObjectMapper om = new ObjectMapper();
        List<String> jsonList = jedisClient.hvals(CART_PRE + ":" + userId);
        for (String json : jsonList) {
            CartProduct cartProduct = null;
            try {
                cartProduct = om.readValue(json, CartProduct.class);
                if ("true".equals(checked)) {
                    cartProduct.setChecked("1");
                } else if ("false".equals(checked)) {
                    cartProduct.setChecked("0");
                } else {
                    return 0;
                }
                jedisClient.hset(CART_PRE + ":" + userId, cartProduct.getProductId() + "",
                        om.writeValueAsString(cartProduct));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 1;
    }

    @Override
    public int deleteCartItem(long userId, long itemId) {
        jedisClient.hdel(CART_PRE + ":" + userId, itemId + "");
        return 1;
    }

    @Override
    public int delChecked(long userId) {
        ObjectMapper om = new ObjectMapper();
        List<String> jsonList = jedisClient.hvals(CART_PRE + ":" + userId);
        for (String json : jsonList) {
            CartProduct cartProduct = null;
            try {
                cartProduct = om.readValue(json, CartProduct.class);
                if ("1".equals(cartProduct.getChecked())) {
                    jedisClient.hdel(CART_PRE + ":" + userId, cartProduct.getProductId() + "");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 1;
    }
}
