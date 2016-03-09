package com.mls.demo.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class BioServer {

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = new ServerSocket(9001);
		Socket socket = null;
		try {
			while (true) {
				socket = serverSocket.accept();
				new Thread(new SocketHandler(socket)).start();
				System.out.println("server start...");
			}
		} catch (Exception e) {
		} finally {
			if (serverSocket !=null){
				System.out.println("server close...");
				serverSocket.close();
			}
		}
	}

	static class SocketHandler implements Runnable{
		Socket socket;

		public SocketHandler(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			BufferedReader in = null;
			PrintWriter out = null;
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(this.socket.getOutputStream(), true);
				String currentTime = null;
				while (true) {
					String body = in.readLine();
					if (body ==null)
						break;
					System.out.println("this time server receive order : " + body);
					currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date().toString(): "BAD ORDER";
					out.println(currentTime);
				}
			} catch (Exception e) {
				if (in !=null){
					try {
						in.close();
					} catch (Exception e2) {
					}
				}
				if (out !=null){
					try {
						out.close();
					} catch (Exception e2) {
					}
				}
				if (socket !=null){
					try {
						socket.close();
					} catch (Exception e2) {
					}
				}
			}
		}
	}
}
