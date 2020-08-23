package ai.yunxi.core.service.impl;

import ai.yunxi.core.service.CountService;
import ai.yunxi.common.Constants;
import ai.yunxi.common.utils.DateUtils;
import ai.yunxi.core.domain.dto.OrderChartData;
import ai.yunxi.core.domain.mapper.TbOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class CountServiceImpl implements CountService {

    private final static Logger log = LoggerFactory.getLogger(CountServiceImpl.class);

    @Autowired
    private TbOrderMapper tbOrderMapper;

    @Override
    public List<OrderChartData> getOrderCountData(int type, Date startTime, Date endTime, int year) {
        List<OrderChartData> fullData = new ArrayList<>();
        if (type == Constants.THIS_WEEK) {
            //本周
            List<OrderChartData> data = tbOrderMapper.selectOrderChart(DateUtils.getBeginDayOfWeek(), DateUtils.getEndDayOfWeek());
            fullData = getFullData(data, DateUtils.getBeginDayOfWeek(), DateUtils.getEndDayOfWeek());
        } else if (type == Constants.THIS_MONTH) {
            //本月
            List<OrderChartData> data = tbOrderMapper.selectOrderChart(DateUtils.getBeginDayOfMonth(), DateUtils.getEndDayOfMonth());
            fullData = getFullData(data, DateUtils.getBeginDayOfMonth(), DateUtils.getEndDayOfMonth());
        } else if (type == Constants.LAST_MONTH) {
            //上个月
            List<OrderChartData> data = tbOrderMapper.selectOrderChart(DateUtils.getBeginDayOfLastMonth(), DateUtils.getEndDayOfLastMonth());
            fullData = getFullData(data, DateUtils.getBeginDayOfLastMonth(), DateUtils.getEndDayOfLastMonth());
        } else if (type == Constants.CUSTOM_DATE) {
            //自定义
            List<OrderChartData> data = tbOrderMapper.selectOrderChart(startTime, endTime);
            fullData = getFullData(data, startTime, endTime);
        } else if (type == Constants.CUSTOM_YEAR) {
            List<OrderChartData> data = tbOrderMapper.selectOrderChartByYear(year);
            fullData = getFullYearData(data, year);
        }
        return fullData;
    }

    /**
     * 无数据补0
     *
     * @param startTime
     * @param endTime
     */
    public List<OrderChartData> getFullData(List<OrderChartData> data, Date startTime, Date endTime) {
        List<OrderChartData> fullData = new ArrayList<>();
        //相差
        long betweenDay = DateUtils.betweenDays(startTime, endTime);
        //起始时间
        Date everyday = startTime;
        int count = -1;
        for (int i = 0; i <= betweenDay; i++) {
            boolean flag = true;
            for (OrderChartData chartData : data) {
                if (org.apache.commons.lang3.time.DateUtils.isSameDay(chartData.getTime(), everyday)) {
                    //有数据
                    flag = false;
                    count++;
                    break;
                }
            }
            if (!flag) {
                fullData.add(data.get(count));
            } else {
                OrderChartData orderChartData = new OrderChartData();
                orderChartData.setTime(everyday);
                orderChartData.setMoney(new BigDecimal("0"));
                fullData.add(orderChartData);
            }

            //时间+1天
            Calendar cal = Calendar.getInstance();
            cal.setTime(everyday);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            everyday = cal.getTime();
        }
        return fullData;
    }

    /**
     * 无数据补0
     *
     * @param data
     * @param year
     * @return
     */
    public List<OrderChartData> getFullYearData(List<OrderChartData> data, int year) {
        List<OrderChartData> fullData = new ArrayList<>();
        //起始月份
        Date everyMonth = DateUtils.getBeginDayOfYear(year);
        int count = -1;
        for (int i = 0; i < 12; i++) {
            boolean flag = true;
            for (OrderChartData chartData : data) {
                if (DateUtils.month(chartData.getTime()) == DateUtils.month(everyMonth)) {
                    //有数据
                    flag = false;
                    count++;
                    break;
                }
            }
            if (!flag) {
                fullData.add(data.get(count));
            } else {
                OrderChartData orderChartData = new OrderChartData();
                orderChartData.setTime(everyMonth);
                orderChartData.setMoney(new BigDecimal("0"));
                fullData.add(orderChartData);
            }

            //时间+1天
            Calendar cal = Calendar.getInstance();
            cal.setTime(everyMonth);
            cal.add(Calendar.MONTH, 1);
            everyMonth = cal.getTime();
        }
        return fullData;
    }
}
