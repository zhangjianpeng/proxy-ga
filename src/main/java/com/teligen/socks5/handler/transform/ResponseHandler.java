package com.teligen.socks5.handler.transform;

import com.teligen.socks5.tools.FileOperation;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import io.netty.util.internal.ConcurrentSet;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ResponseHandler  {
    private static final Logger LOGGER=Logger.getLogger(RequestHandler.class);
    private ConcurrentSet<Response> responses=new ConcurrentSet<>();

    /**
     * 将创建的响应对象注册到类，以便后续的使用这个响应对象
     * @param response
     */
    public void register(Response response){
        responses.add(response);
    }

    public void response() throws IOException {

        List<Response> closedResponses=new ArrayList<>();
        for(Response response:responses){
            if(!response.channelHandlerContext.channel().isActive()){
                closedResponses.add(response);
                continue;
            }
            try {
                response.writeToChannel();
            }catch (Exception e){
                LOGGER.debug(e);
                closedResponses.add(response);
            }
        }
        responses.removeAll(closedResponses);
    }


    public class Response {
        String key;
        AtomicInteger responseCount =new AtomicInteger(1);
        ChannelHandlerContext channelHandlerContext;

        public Response(String key, ChannelHandlerContext channelHandlerContext) {
            this.key = key;
            this.channelHandlerContext = channelHandlerContext;
        }

        void writeToChannel() throws IOException, InterruptedException {
//            channelHandlerContext.channel().isActive();
            FileOperation fileOperation = FileOperation.getInstance();
            File file=new File(fileOperation.getResponseDir(),key+"_"+ responseCount.getAndIncrement());
            byte[] bytes = fileOperation.readDataInFile(file);
            if(bytes==null){
                responseCount.decrementAndGet();
            }else {
                ByteBuf buffer = channelHandlerContext.alloc().buffer(bytes.length);
                //buffer.writeBytes(bytes);
                String s = new String(bytes);
                buffer.writeBytes(bytes);

                channelHandlerContext.channel().writeAndFlush(buffer).sync();
                //file.delete();
            }

        }



    }

}
