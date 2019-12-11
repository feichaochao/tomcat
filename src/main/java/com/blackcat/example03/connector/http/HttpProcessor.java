package com.blackcat.example03.connector.http;

import com.blackcat.example03.core.ServletProcessor;
import com.blackcat.example03.core.StaticResourceProcessor;
import com.blackcat.example03.utils.RequestUtil;


import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 职责：创建request和response 对象
 */
public class HttpProcessor {

    /**
     * The HttpConnector with which this processor is associated.
     */
    private HttpConnector connector = null;

    private HttpRequestLine requestLine = new HttpRequestLine();

    private HttpRequest request;

    private HttpResponse response;

    /**
     * The string manager for this package.
     */
    protected StringManager sm = StringManager.getManager(Constants.PACKAGE);

    public HttpProcessor(HttpConnector connector) {
        this.connector = connector;
    }

    /**
     *
     * 1、创建一个HttpRequest对象
     * 2、创建一个HttpResponse对象
     * 3、解析HTTP请求的第一行和头部,放入HttpRequest对象
     * 4、解析HttpRequest 和 HttpResponse 对象到StaticResourceProcessor或者ServletProcessor
     *
     * @param socket
     */
    public void process(Socket socket) {
        SocketInputStream input = null;
        OutputStream output = null;
        try {
            //获取嵌套字
            input = new SocketInputStream(socket.getInputStream(), 2048);
            output = socket.getOutputStream();

            // create HttpRequest object and parse
            request = new HttpRequest(input);

            // create HttpResponse object
            response = new HttpResponse(output);
            response.setRequest(request);

            response.setHeader("Server", "Hello Servlet Container");

            //解析请求
            parseRequest(input, output);
            //解析请求头
            parseHeaders(input);

            //check if this is a request for a servlet or a static resource
            //a request for a servlet begins with "/servlet/"
            if (request.getRequestURI().startsWith("/servlet/")) {
                //职责：调用service
                ServletProcessor processor = new ServletProcessor();
                processor.process(request, response);
            }
            else {
                //职责：获取静态资源
                StaticResourceProcessor processor = new StaticResourceProcessor();
                processor.process(request, response);
            }

            // Close the socket
            socket.close();
            // no shutdown for this application
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseHeaders(SocketInputStream input) throws IOException, ServletException {
        while (true) {
            HttpHeader header = new HttpHeader();

            // Read the next header TODO 待研究
            input.readHeader(header);
            if (header.nameEnd == 0) {
                if (header.valueEnd == 0) {
                    return;
                }
                else {
                    throw new ServletException(sm.getString("httpProcessor.parseHeaders.colon"));
                }
            }

            String name = new String(header.name, 0, header.nameEnd);
            String value = new String(header.value, 0, header.valueEnd);
            request.addHeader(name, value);
            // do something for some headers, ignore others.
            if (name.equals("cookie")) {
                Cookie cookies[] = RequestUtil.parseCookieHeader(value);
                for (int i = 0; i < cookies.length; i++) {
                    if (cookies[i].getName().equals("jsessionid")) {
                        // Override anything requested in the URL
                        if (!request.isRequestedSessionIdFromCookie()) {
                            // Accept only the first session id cookie
                            request.setRequestedSessionId(cookies[i].getValue());
                            request.setRequestedSessionCookie(true);
                            request.setRequestedSessionURL(false);
                        }
                    }
                    request.addCookie(cookies[i]);
                }
            }
            else if (name.equals("content-length")) {
                int n = -1;
                try {
                    n = Integer.parseInt(value);
                }
                catch (Exception e) {
                    throw new ServletException(sm.getString("httpProcessor.parseHeaders.contentLength"));
                }
                request.setContentLength(n);
            }
            else if (name.equals("content-type")) {
                request.setContentType(value);
            }
        } //end while

    }

    private void parseRequest(SocketInputStream input, OutputStream output) throws IOException, ServletException {

        // Parse the incoming request line  TODO 本协议解析不是很懂,待重点研究 HTTP协议
        input.readRequestLine(requestLine);

        String method = new String(requestLine.method, 0, requestLine.methodEnd);

        String uri = null;

        String protocol = new String(requestLine.protocol, 0, requestLine.protocolEnd);

        // Validate the incoming request line
        if (method.length() < 1) {
            throw new ServletException("Missing HTTP request method");
        }
        else if (requestLine.uriEnd < 1) {
            throw new ServletException("Missing HTTP request URI");
        }
        // Parse any query parameters out of the request URI
        int question = requestLine.indexOf("?");
        if (question >= 0) {
            //对get请求进行截取
            request.setQueryString(new String(requestLine.uri, question + 1,
                    requestLine.uriEnd - question - 1));
            uri = new String(requestLine.uri, 0, question);
        }
        else {
            request.setQueryString(null);
            uri = new String(requestLine.uri, 0, requestLine.uriEnd);
        }


        // Checking for an absolute URI (with the HTTP protocol)
        //处理http:// 获取域名之后的uri  http://127.0.0.1:8080/users/sign_in 截取成 /users/sign_in
        if (!uri.startsWith("/")) {
            int pos = uri.indexOf("://");
            // Parsing out protocol and host name
            if (pos != -1) {
                pos = uri.indexOf('/', pos + 3);
                if (pos == -1) {
                    uri = "";
                }
                else {
                    uri = uri.substring(pos);
                }
            }
        }

        // Parse any requested session ID out of the request URI
        // http://192.168.1.20:8080/crm/getuserprofile.html;jsessionid=abc123
        String match = ";jsessionid=";
        int semicolon = uri.indexOf(match);
        if (semicolon >= 0) {
            String rest = uri.substring(semicolon + match.length());
            int semicolon2 = rest.indexOf(';');
            if (semicolon2 >= 0) {
                request.setRequestedSessionId(rest.substring(0, semicolon2));
                rest = rest.substring(semicolon2);
            }
            else {
                request.setRequestedSessionId(rest);
                rest = "";
            }
            request.setRequestedSessionURL(true);
            uri = uri.substring(0, semicolon) + rest;
        }
        else {
            request.setRequestedSessionId(null);
            request.setRequestedSessionURL(false);
        }

        // Normalize URI (using String operations at the moment)
        //纠正异常的uri,例如任何\\改成/
        String normalizedUri = normalize(uri);

        // Set the corresponding request properties
        ((HttpRequest) request).setMethod(method);
        request.setProtocol(protocol);
        if (normalizedUri != null) {
            ((HttpRequest) request).setRequestURI(normalizedUri);
        }
        else {
            ((HttpRequest) request).setRequestURI(uri);
        }

        if (normalizedUri == null) {
            throw new ServletException("Invalid URI: " + uri + "'");
        }

    }

    /**
     * Return a context-relative path, beginning with a "/", that represents
     * the canonical version of the specified path after ".." and "." elements
     * are resolved out.  If the specified path attempts to go outside the
     * boundaries of the current context (i.e. too many ".." path elements
     * are present), return <code>null</code> instead.
     * @param path
     * @return
     */
    //TODO  待细看
    private String normalize(String path) {

        if (path == null)
            return null;
        // Create a place for the normalized path
        String normalized = path;

        // Normalize "/%7E" and "/%7e" at the beginning to "/~"
        if (normalized.startsWith("/%7E") || normalized.startsWith("/%7e"))
            normalized = "/~" + normalized.substring(4);

        // Prevent encoding '%', '/', '.' and '\', which are special reserved
        // characters
        if ((normalized.indexOf("%25") >= 0)
                || (normalized.indexOf("%2F") >= 0)
                || (normalized.indexOf("%2E") >= 0)
                || (normalized.indexOf("%5C") >= 0)
                || (normalized.indexOf("%2f") >= 0)
                || (normalized.indexOf("%2e") >= 0)
                || (normalized.indexOf("%5c") >= 0)) {
            return null;
        }

        if (normalized.equals("/."))
            return "/";

        // Normalize the slashes and add leading slash if necessary
        if (normalized.indexOf('\\') >= 0)
            normalized = normalized.replace('\\', '/');
        if (!normalized.startsWith("/"))
            normalized = "/" + normalized;

        // Resolve occurrences of "//" in the normalized path
        while (true) {
            int index = normalized.indexOf("//");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
                    normalized.substring(index + 1);
        }

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            int index = normalized.indexOf("/./");
            if (index < 0)
                break;
            normalized = normalized.substring(0, index) +
                    normalized.substring(index + 2);
        }

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            int index = normalized.indexOf("/../");
            if (index < 0)
                break;
            if (index == 0)
                return (null);  // Trying to go outside our context
            int index2 = normalized.lastIndexOf('/', index - 1);
            normalized = normalized.substring(0, index2) +
                    normalized.substring(index + 3);
        }

        // Declare occurrences of "/..." (three or more dots) to be invalid
        // (on some Windows platforms this walks the directory tree!!!)
        if (normalized.indexOf("/...") >= 0)
            return (null);

        // Return the normalized path that we have completed
        return (normalized);
    }
}
