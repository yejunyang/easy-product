package ai.yunxi.backend.controller;

import ai.yunxi.common.Constants;
import ai.yunxi.common.bean.Result;
import ai.yunxi.common.utils.DateUtils;
import ai.yunxi.common.utils.ResultUtil;
import ai.yunxi.core.domain.dto.ChartData;
import ai.yunxi.core.domain.dto.OrderChartData;
import ai.yunxi.core.service.CountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@Api(description = "统计")
public class CountController {

    @Autowired
    private CountService countService;

    @RequestMapping(value = "/count/order", method = RequestMethod.GET)
    @ApiOperation(value = "通过panelId获得板块内容列表")
    public Result<Object> countOrder(@RequestParam int type,
                                     @RequestParam(required = false) String startTime,
                                     @RequestParam(required = false) String endTime,
                                     @RequestParam(required = false) int year) {
        ChartData data = new ChartData();
        Date startDate = null;
        Date endDate = null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (type == Constants.CUSTOM_DATE) {
            try {
                startDate = DateUtils.beginOfDay(df.parse(startTime));
                endDate = DateUtils.endOfDay(df.parse(endTime));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            long betweenDay = DateUtils.betweenDays(startDate, endDate);
            if (betweenDay > 31) {
                return new ResultUtil<Object>().setErrorMsg("所选日期范围过长，最多不能超过31天");
            }
        }


        List<OrderChartData> list = countService.getOrderCountData(type, startDate, endDate, year);
        List<Object> xDatas = new ArrayList<>();
        List<Object> yDatas = new ArrayList<>();
        BigDecimal countAll = new BigDecimal("0");
        for (OrderChartData orderData : list) {
            if (type == Constants.CUSTOM_YEAR) {
                df = new SimpleDateFormat("yyyy-MM");
                xDatas.add(df.format(orderData));
            } else {
                df = new SimpleDateFormat("yyyy-MM-dd");
                xDatas.add(df.format(orderData));
            }
            yDatas.add(orderData.getMoney());
            countAll = countAll.add(orderData.getMoney());
        }
        data.setxDatas(xDatas);
        data.setyDatas(yDatas);
        data.setCountAll(countAll);
        return new ResultUtil<Object>().setData(data);
    }
}
