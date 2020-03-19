package com.payline.payment.cvconnect.bean.common;

import java.util.Date;

public class Authorization {
    String number;
    String type;
    Amount amount;
    Date validationDate;
    String holder;

    public String getNumber() {
        return number;
    }

    public String getType() {
        return type;
    }

    public Amount getAmount() {
        return amount;
    }

    public Date getValidationDate() {
        return validationDate;
    }

    public String getHolder() {
        return holder;
    }
}
