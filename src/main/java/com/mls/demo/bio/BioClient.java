package com.mls.demo.bio;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class BioClient {
	public static void main(String[] args) {
		BufferedReader in = null;
		PrintWriter out = null;
		String resp; 
		Socket socket = null;
		try {
			socket = new Socket("127.0.0.1", 9001);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			out.println("QUERY TIME ORDER");
			System.out.println("client query successed!");
			resp = in.readLine();
			System.out.println("resp :" + resp);
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
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
