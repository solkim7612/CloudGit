package com.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Calculator {

    // 1. 메인 함수: 앱이 실행되면 웹 서버를 켭니다.
    public static void main(String[] args) throws IOException {
        // 8080 포트에서 요청 대기
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // "/" 경로로 들어오면 MyHandler가 처리
        server.createContext("/", new MyHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("웹 서버가 8080 포트에서 시작되었습니다! 🚀");
    }

    // 2. 요청 처리기 (Handler)
    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
	    String response = "<h1 style='color:blue'>OLD Version (Blue)</h1><p>Stable Release</p>";

            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    // 3. 기존 테스트용 메서드 (JUnit 테스트 통과용)
    public int add(int a, int b) {
        return a + b;
    }
} 
