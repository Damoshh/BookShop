package com.book;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.book.handler.AdminDashboardHandler;
import com.book.handler.AdminHandler;
import com.book.handler.BookHandler;
import com.book.handler.OrderHandler;
import com.book.handler.UserHandler;
import com.sun.net.httpserver.HttpServer;

public class Main {
    @SuppressWarnings("CallToPrintStackTrace")
public static void main(String[] args) {
    try {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));

        AdminHandler adminHandler = new AdminHandler();
        
        server.createContext("/api/books", new BookHandler());
        server.createContext("/api/users", new UserHandler());  
        server.createContext("/api/admin/login", adminHandler);
        server.createContext("/api/admin/dashboard", new AdminDashboardHandler()); 
        server.createContext("/api/admin/orders", new OrderHandler(adminHandler));
        
        server.start();
        System.out.println("Server started on port 8000");
    } catch (IOException e) {
        e.printStackTrace();
    }
}
}