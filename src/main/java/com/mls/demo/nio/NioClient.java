package com.mls.demo.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioClient implements Runnable {
	SocketChannel channel = null;
	private volatile boolean isStop = false;
	private Selector selector;

	private NioClient() {
	}

	void init(String serverIp, int port) {
		try {
			channel = SocketChannel.open();
			channel.configureBlocking(false);
			selector = Selector.open();
			channel.connect(new InetSocketAddress(serverIp, port));
			channel.register(selector, SelectionKey.OP_CONNECT);
		} catch (Exception e) {
			System.exit(1);
		}
	}

	@Override
	public void run() {
		try {
			doConnect();
		} catch (Exception e) {
			System.exit(1);
		}

		while (!isStop) {
			Iterator<SelectionKey> itr = selector.selectedKeys().iterator();
			SelectionKey key = null;
			while (itr.hasNext()) {
				key = itr.next();
				itr.remove();
				try {
					handleInput(key);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}

		if (selector != null) {
			try {
				selector.close();
			} catch (Exception e) {

			}
		}
	}

	private void handleInput(SelectionKey key) throws IOException {
		if (key.isValid()) {
			SocketChannel channel = (SocketChannel) key.channel();
			if (key.isConnectable()) {
				if (channel.isConnected()) {
					channel.register(selector, SelectionKey.OP_READ);
					doWrite(channel);
				}
			}

			if (key.isReadable()) {
				ByteBuffer buffer = ByteBuffer.allocate(1000);
				int readSize = channel.read(buffer);
				if (readSize > 0) {
					buffer.flip();
					byte[] bytes = new byte[buffer.remaining()];
					buffer.get(bytes);
					System.out.println("server resp:" + new String(bytes));
					this.isStop = true;
				} else if (readSize < 0) {
					channel.close();
					channel = null;
				}
			}
		}

	}

	private void doWrite(SocketChannel channel) throws IOException {
		byte[] bytes = "QUERY TIME ORDER".getBytes();
		ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
		writeBuffer.put(bytes);
		writeBuffer.flip();
		channel.write(writeBuffer);
		if (!writeBuffer.hasRemaining()) {
			System.out.println("send order 2 succeed!");
		}
	}

	private void doConnect() {

	}

	public void listen() throws IOException {
		System.out.println("客户端程序启动...");
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
				}
			}
		}
	}
}
