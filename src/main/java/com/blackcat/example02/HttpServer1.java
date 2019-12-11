package com.blackcat.example02;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class HttpServer1 {

    // shutdown command
    private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";

    // the shutdown command received
    private boolean shutdown = false;

    public static void main(String[] args) {
        HttpServer1 server = new HttpServer1();
        server.await();
    }

    public void await() {
        ServerSocket serverSocket = null;
        int port = 8080;
        try {
            serverSocket =  new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Loop waiting for a request
        while (!shutdown) {
            Socket socket = null;
            InputStream input = null;
            OutputStream output = null;
            try {
                socket = serverSocket.accept();
                input = socket.getInputStream();
                output = socket.getOutputStream();

                // create Request object and parse
                Request request = new Request(input);
                request.parse();

                // create Response object
                Response response = new Response(output);
                response.setRequest(request);
                //访问servlet
                // 1、处理请求路径
                // 2、根据请求路径的名称 实例化对应的class类
                // 3、调用service的方法
                if(request.getUri().startsWith("/servlet/")){
                    ServletProcessor1 processor = new ServletProcessor1();
                    //只能看到   Hello. Roses are red.   下面的看不到
                    processor.process(request,response);
                }else{
                    //静态资源访问
                    StaticResourceProcessor processor = new StaticResourceProcessor();
                    processor.process(request,response);
                }

                // Close the socket
                socket.close();

                //check if the previous URI is a shutdown command
                shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
            }
            catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }
}
