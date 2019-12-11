package com.blackcat.example03.connector.http;
import java.io.File;
import java.util.Locale;

public final class Constants {
  /**
   * 静态资源路径
   */
  public static final String WEB_ROOT =
    System.getProperty("user.dir") + File.separator  + "web";

  /**
   * 包名
   */
  public static final String PACKAGE ="com.blackcat.example03.connector.http";

  public static final int DEFAULT_CONNECTION_TIMEOUT = 60000;

  public static final int PROCESSOR_IDLE = 0;

  public static final int PROCESSOR_ACTIVE = 1;


  public  static  void main(String[] args){
    System.out.println(Locale.getDefault());
    StringManager manager = StringManager.getManager("com.blackcat.example03.connector.http");
    System.out.println(manager.getString("httpConnector.alreadyInitialized"));
  }
}
