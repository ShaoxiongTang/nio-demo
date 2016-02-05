package com.mls.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @author shaoxiongtang
 * @date 2016年1月28日
 */
public class SocketCommunicate {
	public static class NioServer {
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
				selector.select();
				Iterator<SelectionKey> ite = selector.selectedKeys().iterator();
				while (ite.hasNext()) {
					SelectionKey key = ite.next();
					ite.remove();

					// 客户端连接事件
					if (key.isAcceptable()) {
						ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
						SocketChannel channel = serverSocketChannel.accept();
						channel.configureBlocking(false);
						channel.write(ByteBuffer.wrap(new String("send message to client").getBytes()));
						channel.register(selector, SelectionKey.OP_READ);
						System.out.println("客户端请求连接事件");
						// 可读数据事件
					} else if (key.isReadable()) {
						SocketChannel channel = (SocketChannel) key.channel();
						ByteBuffer buffer = ByteBuffer.allocate(10);
						int read = channel.read(buffer);
						byte[] data = buffer.array();
						String message = new String(data);
						System.out.println("receive message from client, size:" + buffer.position() + " msg: " + message);
					}
				}
			}
		}
	}

	public static class NioClient {
		private Selector selector;

		public void init(String serverIp, int port) throws IOException {
			SocketChannel channel = SocketChannel.open();
			channel.configureBlocking(false);
			selector = Selector.open();

			channel.connect(new InetSocketAddress(serverIp, port));
			channel.register(selector, SelectionKey.OP_CONNECT);
		}

		public void listen() throws IOException {
			System.out.println("客户端启动");
			// 轮询访问selector
			while (true) {
				// 选择注册过的io操作的事件(第一次为SelectionKey.OP_CONNECT)
				selector.select();
				Iterator<SelectionKey> ite = selector.selectedKeys().iterator();
				while (ite.hasNext()) {
					SelectionKey key = ite.next();
					// 删除已选的key，防止重复处理
					ite.remove();
					if (key.isConnectable()) {
						SocketChannel channel = (SocketChannel) key.channel();

						// 如果正在连接，则完成连接
						if (channel.isConnectionPending()) {
							channel.finishConnect();
						}

						channel.configureBlocking(false);
						// 向服务器发送消息
						channel.write(ByteBuffer.wrap(new String("send message to server.").getBytes()));

						// 连接成功后，注册接收服务器消息的事件
						channel.register(selector, SelectionKey.OP_READ);
						System.out.println("客户端连接成功");
					} else if (key.isReadable()) { // 有可读数据事件。
						SocketChannel channel = (SocketChannel) key.channel();

						ByteBuffer buffer = ByteBuffer.allocate(20);
						channel.read(buffer);
						byte[] data = buffer.array();
						String message = new String(data);

						System.out.println("recevie message from server:, size:" + buffer.position() + " msg: " + message);
						// ByteBuffer outbuffer =
						// ByteBuffer.wrap(("client.".concat(msg)).getBytes());
						// channel.write(outbuffer);
					}
				}
			}
		}
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
		
		new Thread(new Runnable() {
			public void run() {
				NioClient client = new NioClient();
				try {
					client.init("127.0.0.1", 9000);
					client.listen();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}).start();
	}
}
