package com.blackcat.example02;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

@Slf4j
public class ServletProcessor1 {


    public void process(Request request, Response response) {

        String uri = request.getUri();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);
        URLClassLoader loader = null;

        /**
         *  URLClassLoader 学习
         */
        try {
            // create a URLClassLoader
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            File classPath = new File(Constants.WEB_ROOT);
            // the forming of repository is taken from the createClassLoader method in
            // org.apache.catalina.startup.ClassLoaderFactory
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString() ;
            // the code for forming the URL is taken from the addRepository method in
            // org.apache.catalina.loader.StandardClassLoader class.
            urls[0] = new URL(null, repository, streamHandler);
            loader = new URLClassLoader(urls);
        }
        catch (IOException e) {
            log.error(e.toString() );
        }
        Class myClass = null;
        try {
            myClass = loader.loadClass(servletName);
        }
        catch (ClassNotFoundException e) {
            log.error(e.toString());
        }

        Servlet servlet = null;
        /**
         * 对request、response进行封装,防止强转成request,response,调用里面的方法
         */
        RequestFacade requestFacade = new RequestFacade(request);
        ResponseFacade responseFacade = new ResponseFacade(response);

        try {
            servlet = (Servlet) myClass.newInstance();
            servlet.service((ServletRequest) requestFacade, (ServletResponse) responseFacade);
        }
        catch (Exception e) {
            log.error(e.toString());
        }
        catch (Throwable e) {
            log.error(e.toString());
        }

    }
}
