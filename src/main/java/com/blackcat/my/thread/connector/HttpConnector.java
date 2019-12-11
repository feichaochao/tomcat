package com.blackcat.my.thread.connector;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Stack;

/**
 * 职责：等待请求,获取socket
 */
@Slf4j
public class HttpConnector implements Runnable {

    //process的缓存池 是否存在缓存池的处理策略（min<curProcessors<max && 空闲的时候）
    private Stack<HttpProcessor> stack = new Stack<HttpProcessor>();
    //最大 process的数量
    private static final int MAX_PROCESSORS = 20;
    //最少 process的数量
    private static final int MIN_PROCESSORS = 3;
    //当前 process的标记
    private int curProcessors = 0;


    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(80, 1, InetAddress.getByName("127.0.0.1"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true) {
            Socket socket;
            try {
                //获取嵌套字
                socket = serverSocket.accept();
                log.info("accept socket");
                //获取process
                HttpProcessor processor = getProcessor();
                if (processor == null) {
                    log.info("process is null");
                    socket.close();
                }
                processor.assign(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private HttpProcessor getProcessor() {
        synchronized (stack) {
            //stack 当中为null current 没有达到最大 新建一个process
            if (curProcessors < MAX_PROCESSORS && stack.size() < 1) {
                //主线程的process未放入stack
                log.info("==add==");
                HttpProcessor processor = new HttpProcessor(this);
                curProcessors++;
                //启动process的线程
                processor.start();
                return processor;
            }

             //stack池中存在,取出一个
            if (stack.size() > 0) {
                HttpProcessor processor = stack.pop();
                log.info("pop" + stack.size());
                return processor;
            }
            return null;
        }
    }

    public void start() {
        Thread t = new Thread(this);
        //启动当前线程获取嵌套字
        t.start();
        for (int i = 0; i < MIN_PROCESSORS; i++) {
            //启动处理器线程
            HttpProcessor processor = new HttpProcessor(this);
            processor.start();
            curProcessors++;
            //放入stack
            recycle(processor);
        }
    }

    public void recycle(HttpProcessor httpProcessor) {
        stack.push(httpProcessor);
        log.info("push" + stack.size());
    }
}
