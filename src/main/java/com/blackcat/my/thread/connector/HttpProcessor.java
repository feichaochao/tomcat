package com.blackcat.my.thread.connector;

import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 职责：创建request和response 对象，处理socket
 */
@Slf4j
public class HttpProcessor implements Runnable {


    private boolean avail = false;

    private Socket socket;

    private HttpConnector connector;

    public HttpProcessor(HttpConnector connector) {
        this.connector = connector;
    }


    @Override
    public void run() {
        while (true) {
            Socket socket = await();

            if (socket == null) {
                log.info ("socket is null");
                continue;
            }
            try {
                log.info ("HttpProcessor run handle socket");
                OutputStream os = socket.getOutputStream();
                Thread.sleep(5000);
                PrintWriter writer = new PrintWriter(os, true);
                writer.write("hello world");
                writer.flush();
                writer.close();
                socket.close();
                connector.recycle(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized Socket await() {
        //阻塞，等待唤醒获取socket
        while (!avail) {
            try {
                log.info("[await] 阻塞,等待唤醒");
                wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Socket socket = this.socket;
        avail = false;
        notifyAll();
        return socket;
    }


    public synchronized void assign(Socket socket) {
        while (avail) {
            try {
                log.info("[assign] 阻塞,等待唤醒");
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        avail = true;
        notifyAll();
        this.socket = socket;
    }

    public void start() {
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();
    }
}
