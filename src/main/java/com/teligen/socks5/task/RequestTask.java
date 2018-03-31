package com.teligen.socks5.task;

import com.teligen.socks5.handler.transform.RequestHandler;
import org.apache.log4j.Logger;

import java.io.IOException;

public class RequestTask implements Runnable {
    private static final Logger LOGGER=Logger.getLogger(RequestTask.class);
    private RequestHandler requestHandler;

    public RequestTask(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public void run() {
        while (true){
            try {
//                try {
//                    Thread.sleep(20000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                requestHandler.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
