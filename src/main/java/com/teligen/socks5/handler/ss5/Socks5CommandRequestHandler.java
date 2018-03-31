package com.teligen.socks5.handler.ss5;

import com.teligen.socks5.handler.transform.ResponseHandler;
import com.teligen.socks5.tools.FileOperation;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;

import java.util.concurrent.atomic.AtomicInteger;

public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest>{
	
	private static final Logger logger = LoggerFactory.getLogger(Socks5CommandRequestHandler.class);
	private ResponseHandler responseHandler;

	public Socks5CommandRequestHandler(ResponseHandler responseHandler) {
		this.responseHandler = responseHandler;
	}

	@Override
	protected void channelRead0(final ChannelHandlerContext clientChannelContext, DefaultSocks5CommandRequest msg) throws Exception {
		logger.debug("目标服务器  : " + msg.type() + "," + msg.dstAddr() + "," + msg.dstPort());
		if(msg.type().equals(Socks5CommandType.CONNECT)) {
			StringBuffer stringBuffer=new StringBuffer();
			stringBuffer.append("Host=").append(msg.dstAddr()).append("\r\n")
					.append("Port=").append(msg.dstPort()).append("\r\n");
			Channel channel = clientChannelContext.channel();
			String key = channel.id().asShortText();
			String fileName= key +"_0";
			FileOperation fileOperation = FileOperation.getInstance();
			fileOperation.createRequestFile(fileName,stringBuffer.toString().getBytes());

			ResponseHandler.Response response = responseHandler.new Response(key, clientChannelContext);
			responseHandler.register(response);

			clientChannelContext.pipeline().addLast(new ChannelInboundHandlerAdapter(){
				AtomicInteger requestCount = new AtomicInteger(1);
				@Override
				public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
					ByteBuf byteBuf=(ByteBuf)msg;
					String fileName = ctx.channel().id().asShortText() + "_" + requestCount.getAndIncrement();
					logger.info("Read data in byteBuf and write to "+fileName);
					FileOperation fileOperation = FileOperation.getInstance();
					byte[] bytes = fileOperation.readBytesInByteBuf(byteBuf);
					fileOperation.createRequestFile(fileName,bytes);
				}
			});

			Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
			clientChannelContext.writeAndFlush(commandResponse);
//            logger.trace("准备连接目标服务器");
//			EventLoopGroup bossGroup = new NioEventLoopGroup();
//			Bootstrap bootstrap = new Bootstrap();
//			bootstrap.group(bossGroup)
//			.channel(NioSocketChannel.class)
//			.option(ChannelOption.TCP_NODELAY, true)
//			.handler(new ChannelInitializer<SocketChannel>() {
//				@Override
//				protected void initChannel(SocketChannel ch) throws Exception {
//					//ch.pipeline().addLast(new LoggingHandler());//in out
//					//将目标服务器信息转发给客户端
//					ch.pipeline().addLast(new Dest2ClientHandler(clientChannelContext));
//				}
//			});
//			logger.trace("连接目标服务器");
//			ChannelFuture future = bootstrap.connect(msg.dstAddr(), msg.dstPort());
//			future.addListener(new ChannelFutureListener() {
//
//				public void operationComplete(final ChannelFuture future) throws Exception {
//					if(future.isSuccess()) {
//						logger.trace("成功连接目标服务器");
//						clientChannelContext.pipeline().addLast(new Client2DestHandler(future));
//						Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4);
//						clientChannelContext.writeAndFlush(commandResponse);
//					} else {
//						Socks5CommandResponse commandResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4);
//						clientChannelContext.writeAndFlush(commandResponse);
//					}
//				}
//
//			});
		} else {
			clientChannelContext.fireChannelRead(msg);
		}
	}

	/**
	 * 将目标服务器信息转发给客户端
	 * 
	 * @author huchengyi
	 *
	 */
	private static class Dest2ClientHandler extends ChannelInboundHandlerAdapter {
		
		private ChannelHandlerContext clientChannelContext;
		
		public Dest2ClientHandler(ChannelHandlerContext clientChannelContext) {
			this.clientChannelContext = clientChannelContext;
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx2, Object destMsg) throws Exception {
			logger.trace("将目标服务器信息转发给客户端");
			clientChannelContext.writeAndFlush(destMsg);
		}

		@Override
		public void channelInactive(ChannelHandlerContext ctx2) throws Exception {
			logger.trace("目标服务器断开连接");
			clientChannelContext.channel().close();
		}
	}
	
	/**
	 * 将客户端的消息转发给目标服务器端
	 * 
	 * @author huchengyi
	 *
	 */
	private static class Client2DestHandler extends ChannelInboundHandlerAdapter {
		
		private ChannelFuture destChannelFuture;
		
		public Client2DestHandler(ChannelFuture destChannelFuture) {
			this.destChannelFuture = destChannelFuture;
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			logger.trace("将客户端的消息转发给目标服务器端");
			destChannelFuture.channel().writeAndFlush(msg);
		}
		
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			logger.trace("客户端断开连接");
			destChannelFuture.channel().close();
		}
	}
}
