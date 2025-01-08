package com.book;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.book.handler.BookHandler;
import com.sun.net.httpserver.HttpServer;

public class Main {
    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.setExecutor(Executors.newFixedThreadPool(10));

            server.createContext("/api/books", new BookHandler());
            
            server.start();
            System.out.println("Server started on port 8000");
            System.out.println("Try accessing: http://localhost:8000/api/books");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}