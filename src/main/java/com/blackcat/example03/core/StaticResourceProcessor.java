package com.blackcat.example03.core;


import com.blackcat.example03.connector.http.HttpRequest;
import com.blackcat.example03.connector.http.HttpResponse;

import java.io.IOException;

public class StaticResourceProcessor {

  public void process(HttpRequest request, HttpResponse response) {
    try {
      response.sendStaticResource();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

}
