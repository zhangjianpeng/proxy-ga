package com.zjp.ch2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

public class EchoServer {
    private int port;

    public static void main(String[] args) throws InterruptedException {
        EchoServer echoServer=new EchoServer();
        echoServer.port=9090;
        echoServer.start();
    }

    public void start() throws InterruptedException {
        final EventLoopGroup boss=new NioEventLoopGroup();
        EventLoopGroup worker=new NioEventLoopGroup();

        final ServerBootstrap serverBootstrap=new ServerBootstrap();
        serverBootstrap.group(boss,worker)
                .channel(NioServerSocketChannel.class)
                .localAddress(port)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                System.out.println("I am active");
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf in=(ByteBuf)msg;
                                System.out.println(in.toString(CharsetUtil.UTF_8));
                                ctx.write(in);
                            }

                            @Override
                            public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                                System.out.println("complete");
                                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                cause.printStackTrace();
                                String gbk = new String(cause.getMessage().getBytes(CharsetUtil.UTF_8), "gbk");
                                System.out.println(gbk);
                                //ctx.close();
                            }
                        });
                    }
                });
        ChannelFuture channelFuture = serverBootstrap.bind().sync();
        channelFuture.channel().closeFuture().sync();
        boss.shutdownGracefully().sync();
        worker.shutdownGracefully().sync();

    }
}
