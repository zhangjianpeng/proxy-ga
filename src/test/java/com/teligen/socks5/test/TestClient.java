package com.teligen.socks5.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;

public class TestClient {
    public static void main(String[] args ) throws InterruptedException, IOException {

        URL url=new URL("https://www.mojidong.com/network/2015/03/07/socket5-1/?spm=a2c4e.11153940.blogcont216135.25.18fd2656kesQTR");
        URLConnection urlConnection = url.openConnection(new Proxy(Proxy.Type.SOCKS,new InetSocketAddress("127.0.0.1",11080)));
        urlConnection.connect();
        InputStream inputStream = urlConnection.getInputStream();
       byte[] bytes=new byte[1000];
        int i;
        while ((i=inputStream.read(bytes))>-1){
            System.out.println(new String(bytes,0,i));
        }
    }
}
