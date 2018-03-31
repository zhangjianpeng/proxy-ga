package com.zjp.ch2;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

public class EchoClient {
    private String host;
    private int port;

    public static void main(String[] args) throws InterruptedException {
        EchoClient echoClient=new EchoClient();
        echoClient.host="127.0.0.1";
        echoClient.port=9090;
        echoClient.start();
    }

    public void start() throws InterruptedException {
        EventLoopGroup worker=new NioEventLoopGroup();
        final Bootstrap bootstrap=new Bootstrap();
        bootstrap.group(worker)
                .channel(NioSocketChannel.class)
                .remoteAddress(host,port)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
                                System.out.println(byteBuf.toString(CharsetUtil.UTF_8));
                            }

                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                System.out.println("active client");
                                ctx.channel().write(Unpooled.copiedBuffer("wo am shi hao pengyng".getBytes(CharsetUtil.UTF_8)));
                                ctx.channel().write(Unpooled.copiedBuffer("wo am shi hao pengyng".getBytes(CharsetUtil.UTF_8)));
                                ctx.channel().write(Unpooled.copiedBuffer("wo am shi hao pengyng".getBytes(CharsetUtil.UTF_8)));
                                ctx.channel().write(Unpooled.copiedBuffer("wo am shi hao pengyng".getBytes(CharsetUtil.UTF_8)));
                                ctx.channel().write(Unpooled.copiedBuffer("wo am shi hao pengyng".getBytes(CharsetUtil.UTF_8)));
                                ctx.channel().writeAndFlush(Unpooled.copiedBuffer("wo am shi hao pengyng".getBytes(CharsetUtil.UTF_8)));

                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                System.out.println(" inbound active");

                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                cause.printStackTrace();
                                ctx.close();
                            }
                        });
                    }
                });
        ChannelFuture channelFuture = bootstrap.connect().sync();
        channelFuture.channel().closeFuture().sync();
        worker.shutdownGracefully().sync();

    }
}
