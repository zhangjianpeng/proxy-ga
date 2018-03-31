package com.teligen.socks5.task;

import com.teligen.socks5.handler.transform.ResponseHandler;
import org.apache.log4j.Logger;

public class ResponseTask implements Runnable {
    private static final Logger LOGGER=Logger.getLogger(ResponseTask.class);
    private ResponseHandler responseHandler;

    public ResponseTask(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    @Override
    public void run() {
        while (true){
            try {
                responseHandler.response();
            }catch (Exception e){
                LOGGER.error(e);
            }
        }
    }
}
