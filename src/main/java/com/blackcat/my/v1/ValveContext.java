package com.blackcat.my.v1;


import javax.servlet.ServletException;
import java.io.IOException;

/**
 * 阀门上下文接口
 */
public interface ValveContext {

    public String getInfo();

    public void invokeNext(Request request, Response response)
            throws IOException, ServletException;
}
