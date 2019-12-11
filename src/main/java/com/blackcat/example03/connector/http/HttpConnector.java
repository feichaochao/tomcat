package com.blackcat.example03.connector.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * httpConnector  职责：等待请求   调用 HttpProcessor 处理soceket
 */
public class HttpConnector implements Runnable {

    boolean stopped;


    private String scheme = "http";

    public String getScheme() {
        return scheme;
    }

    public void run() {
        ServerSocket serverSocket = null;
        int port = 8080;
        try {
            serverSocket =  new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        /**
         * 1、等待HTTP的请求
         * 2、为每个请求创建HttpProcessor对象
         * 3、调用process方法
         */

        while (!stopped) {
            // Accept the next incoming connection from the server socket
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            }
            catch (Exception e) {
                continue;
            }
            // Hand this socket off to an HttpProcessor
            HttpProcessor processor = new HttpProcessor(this);
            processor.process(socket);
        }
    }

    /**
     * 启动当前线程
     */
    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }
}
