package com.payline.payment.cvconnect.bean.request;

import com.google.gson.Gson;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class CVCoRequest {
    private String requestDate;

    CVCoRequest() {
        this.requestDate = ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT );
    }

    public abstract List<String> getANCVSecurity();


    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
