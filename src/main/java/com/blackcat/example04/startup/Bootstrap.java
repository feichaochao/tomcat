/* explains Tomcat's default container */
package com.blackcat.example04.startup;


import com.blackcat.example04.core.SimpleContainer;
import org.apache.catalina.connector.http.HttpConnector;


public final class Bootstrap {

  /**                          传递Connector
   * HttpConnector (绑定容器) --------------> HttpProcess  --> connector.getContainer().invoke(request, response) --> Servlet.service( request,response);
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    HttpConnector connector = new HttpConnector();
    SimpleContainer container = new SimpleContainer();
    connector.setContainer(container);
    try {
      //主要阅读下面代码
      //初始化  serverSocket 赋值
      connector.initialize();
      /**
       * 启动HttpConnector的线程(连接器线程) 和 HttpProcess的线程(处理器线程)
       */
      connector.start();

      // make the application wait until we press any key.
      System.in.read();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}