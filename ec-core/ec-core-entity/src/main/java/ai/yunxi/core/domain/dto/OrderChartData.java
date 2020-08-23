package ai.yunxi.core.domain.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class OrderChartData implements Serializable {

    Date time;

    BigDecimal money;

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }
}
