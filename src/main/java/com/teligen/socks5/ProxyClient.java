package com.teligen.socks5;


import com.teligen.socks5.handler.transform.RequestHandler;
import org.apache.log4j.Logger;

import java.io.IOException;

public class ProxyClient {
    private static final Logger LOGGER=Logger.getLogger(ProxyClient.class);

    public static void main(String[] args) throws IOException {
        LOGGER.info("Client begin...");
        RequestHandler requestHandler=new RequestHandler();
       // new Thread(new DeleteFileTask()).start();
        while (true){
            try {
                requestHandler.request();
                requestHandler.execute();
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error(e);
            }
        }
    }
}
