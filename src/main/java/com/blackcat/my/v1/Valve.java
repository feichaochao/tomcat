package com.blackcat.my.v1;


import javax.servlet.ServletException;
import java.io.IOException;

/**
 * 阀门的接口
 */
public interface Valve {

    public String getInfo();

    public void invoke(Request request, Response response,
                       ValveContext context)
            throws IOException, ServletException;
}
