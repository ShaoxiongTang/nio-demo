package com.mls.demo.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;

public class NioServer {
	private Selector selector;
	private ServerSocketChannel serverChannel;

	public void init(int port) throws IOException {
		serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		serverChannel.socket().bind(new InetSocketAddress(port));
		selector = Selector.open();
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
	}

	public void listen() throws IOException {
		System.out.println("服务端程序启动...");
		while (true) {
			selector.select(100);
			Iterator<SelectionKey> ite = selector.selectedKeys().iterator();
			while (ite.hasNext()) {
				SelectionKey key = ite.next();
				ite.remove();

				// 客户端连接事件
				if (key.isAcceptable()) {
					ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
					SocketChannel channel = serverSocketChannel.accept();
					channel.configureBlocking(false);
					channel.socket().setReuseAddress(true);
					channel.write(ByteBuffer.wrap(new String("send message to client").getBytes()));
					channel.register(selector, SelectionKey.OP_READ);
					System.out.println("客户端请求连接事件");
					// 可读数据事件
				} else if (key.isReadable()) {
					SocketChannel channel = (SocketChannel) key.channel();
					ByteBuffer buffer = ByteBuffer.allocate(10);
					int readSize = channel.read(buffer);
					if (readSize> 0){ 
						buffer.flip();
						byte[] bytes = new byte[buffer.remaining()];
						String body = new String(bytes);
						System.out.println("the time server receive order :" + body);
						String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date().toString(): "BAD ORDER";
						doWrite(currentTime, channel);
					} else if (readSize <0){ // 对方链路关闭
						key.cancel();
						channel.close();
					} else {
						// 未关闭连接正常情况
					}
				}
			}
		}
	}
	
	private void doWrite (String resp,SocketChannel channel) throws IOException{
		ByteBuffer writeBuffer = ByteBuffer.allocate(resp.getBytes().length);
		writeBuffer.put(resp.getBytes());
		writeBuffer.flip();
		channel.write(writeBuffer);
	}
	
	public static void main(String[] args) throws IOException {
		new Thread(new Runnable() {
			public void run() {
				try {
					NioServer server = new NioServer();
					server.init(9000);
					server.listen();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}).start();
	}
}
