package com.payline.payment.cvconnect.bean.request;

import com.google.gson.Gson;
import com.payline.payment.cvconnect.bean.configuration.RequestConfiguration;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public abstract class CVCoRequest {
    private String requestDate;


    CVCoRequest() {
        this.requestDate = ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT );
    }

    public abstract String getANCVSecurity(RequestConfiguration configuration);


    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
