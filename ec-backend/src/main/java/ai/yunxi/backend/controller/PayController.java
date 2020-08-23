package ai.yunxi.backend.controller;

import ai.yunxi.common.bean.Result;
import ai.yunxi.common.utils.ResultUtil;
import ai.yunxi.core.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(description = "订单")
public class PayController {

    private final static Logger log = LoggerFactory.getLogger(PayController.class);

    @Autowired
    private OrderService orderService;

    @RequestMapping(value = "/pay/pass", method = RequestMethod.GET)
    @ApiOperation(value = "支付审核通过")
    public Result<Object> payPass(String tokenName, String token, String id) {
        int result = orderService.passPay(tokenName, token, id);
        if (result == -1) {
            return new ResultUtil<Object>().setErrorMsg("无效的Token或链接");
        }

        if (result == 0) {
            return new ResultUtil<Object>().setErrorMsg("数据处理出错");
        }

        return new ResultUtil<Object>().setData("处理成功");
    }

    @RequestMapping(value = "/pay/back", method = RequestMethod.GET)
    @ApiOperation(value = "支付审核驳回")
    public Result<Object> backPay(String tokenName, String token, String id) {
        int result = orderService.backPay(tokenName, token, id);
        if (result == -1) {
            return new ResultUtil<Object>().setErrorMsg("无效的Token或链接");
        }

        if (result == 0) {
            return new ResultUtil<Object>().setErrorMsg("数据处理出错");
        }

        return new ResultUtil<Object>().setData("处理成功");
    }

    @RequestMapping(value = "/pay/passNotShow", method = RequestMethod.GET)
    @ApiOperation(value = "支付审核通过但不展示")
    public Result<Object> payNotShow(String tokenName, String token, String id) {
        int result = orderService.notShowPay(tokenName, token, id);
        if (result == -1) {
            return new ResultUtil<Object>().setErrorMsg("无效的Token或链接");
        }
        if (result == 0) {
            return new ResultUtil<Object>().setErrorMsg("数据处理出错");
        }

        return new ResultUtil<Object>().setData("处理成功");
    }
}
