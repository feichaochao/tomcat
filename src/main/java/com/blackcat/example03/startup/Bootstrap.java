package com.blackcat.example03.startup;


import com.blackcat.example03.connector.http.HttpConnector;

public final class Bootstrap {

  /**
   * 启动应用程序
   * 连接器
   * 创建一个HttpRequest对象
   * 创建一个HttpResponse对象
   * 静态资源处理器和servvlet处理器
   * 运行应用程序
   *
   * TODO  http协议解析代码熟悉
   * @param args
   */
  public static void main(String[] args) {
    //实例化连接器并调用start方法
    HttpConnector connector = new HttpConnector();
    connector.start();
  }
}