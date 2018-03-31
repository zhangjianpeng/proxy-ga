package com.zjp.ch2;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TestEchoServer {

    public static void main(String[] args) {
        try{

            SocketChannel socketChannel=SocketChannel.open();
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress("127.0.0.1",9090));
            socketChannel.finishConnect();
            String s="wo shi wu\r\n";
            byte[] bytes = s.getBytes();
            ByteBuffer wrap = ByteBuffer.wrap(bytes);
            socketChannel.write(wrap);
            Thread.sleep(100);
            wrap.flip();
            socketChannel.write(wrap);
            wrap.flip();
            socketChannel.write(wrap);

            ByteBuffer allocate = ByteBuffer.allocate(1000);
            socketChannel.read(allocate);
            allocate.flip();
            while (allocate.hasRemaining()){
                System.out.print((char) allocate.get());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
