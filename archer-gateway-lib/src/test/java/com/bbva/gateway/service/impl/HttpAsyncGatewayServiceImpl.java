package com.bbva.gateway.service.impl;

import com.bbva.common.consumers.record.CRecord;
import com.bbva.gateway.service.http.HttpAsyncGatewayService;


public class HttpAsyncGatewayServiceImpl extends HttpAsyncGatewayService {


    @Override
    public Object call(final CRecord record) {
        return super.call(record);
    }

    @Override
    protected boolean isSuccess(final Object response) {
        return true;
    }

    @Override
    public String transactionId(final Object response) {
        return null;
    }

    @Override
    public void createListener(final CRecord output, final Object response) {

    }
}
