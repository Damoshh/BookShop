package com.book;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.book.handler.AdminDashboardHandler;
import com.book.handler.AdminHandler;
import com.book.handler.BookHandler;
import com.book.handler.BookManagementHandler;
import com.book.handler.CartHandler;
import com.book.handler.OrderHandler;
import com.book.handler.SearchBookHandler;
import com.book.handler.UserHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class Main {
    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.setExecutor(Executors.newFixedThreadPool(10));

            // Create handlers
            AdminHandler adminHandler = new AdminHandler();
            BookHandler bookHandler = new BookHandler();
            SearchBookHandler searchBookHandler = new SearchBookHandler();  // Add this line
            CartHandler cartHandler = new CartHandler();
            UserHandler userHandler = new UserHandler();
            OrderHandler orderHandler = new OrderHandler();
            BookManagementHandler bookManagementHandler = new BookManagementHandler();



            // Book endpoints
            server.createContext("/api/books", exchange -> {
                enableCors(exchange);
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                bookHandler.handle(exchange);
            });

            // Search endpoint
            server.createContext("/api/books/search", exchange -> {
                enableCors(exchange);
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                searchBookHandler.handle(exchange);
            });

            // User endpoints
            server.createContext("/api/users", exchange -> {
                enableCors(exchange);
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                userHandler.handle(exchange);
            });

            server.createContext("/api/users/profile", exchange -> {
                enableCors(exchange);
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                userHandler.handle(exchange);
            });

            // Cart endpoints
            server.createContext("/api/cart", exchange -> {
                enableCors(exchange);
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                cartHandler.handle(exchange);
            });

            server.createContext("/api/cart/add", exchange -> {
                enableCors(exchange);
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                cartHandler.handle(exchange);
            });

            server.createContext("/api/cart/remove", exchange -> {
                enableCors(exchange);
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                cartHandler.handle(exchange);
            });

            // Admin endpoints
            server.createContext("/api/admin/login", exchange -> {
                enableCors(exchange);
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                adminHandler.handle(exchange);
            });

            server.createContext("/api/admin/profile", exchange -> {
                enableCors(exchange);
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                adminHandler.handle(exchange);
            });

            server.createContext("/api/admin/dashboard", exchange -> {
                enableCors(exchange);
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                new AdminDashboardHandler().handle(exchange);
            });
            
            // Add these endpoints
            server.createContext("/api/orders/create", exchange -> {
                enableCors(exchange);
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                orderHandler.handle(exchange);
            });

            server.createContext("/api/orders/user/", exchange -> {
                enableCors(exchange);
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                orderHandler.handle(exchange);
            });
            server.createContext("/api/books/category/", exchange -> {
                enableCors(exchange);
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                bookHandler.handle(exchange);
            });

            // For admin
            server.createContext("/api/orders/admin/update-status", exchange -> {
                enableCors(exchange);
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                orderHandler.handleAdminOrderUpdate(exchange);
            });

            // For user 
            server.createContext("/api/orders/user/update-status", exchange -> {
                enableCors(exchange);
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                orderHandler.handleUserOrderUpdate(exchange);
            });

            // General orders endpoint for fetching all orders
            server.createContext("/api/orders", exchange -> {
                enableCors(exchange);
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                orderHandler.handle(exchange);
            });

            // In Main.java, add this context
            server.createContext("/api/books/manage/", exchange -> {
                enableCors(exchange);
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }
                bookManagementHandler.handle(exchange);
            });

            // Start the server
            server.start();
            System.out.println("Server started on port 8000");
            System.out.println("API Endpoints:");
            System.out.println("- GET/POST   /api/books");
            System.out.println("- GET/POST   /api/users");
            System.out.println("- GET/PUT    /api/users/profile");
            System.out.println("- GET        /api/cart/{userId}");
            System.out.println("- POST       /api/cart/add");
            System.out.println("- POST       /api/cart/remove");
            System.out.println("- POST       /api/admin/login");
            System.out.println("- GET        /api/admin/dashboard");
            System.out.println("- GET        /api/admin/orders");
            System.out.println("- GET        /api/orders");
            
        } catch (IOException e) {
            System.err.println("Server Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private static void enableCors(HttpExchange exchange) {
        // Remove any existing CORS headers first
        exchange.getResponseHeaders().remove("Access-Control-Allow-Origin");
        exchange.getResponseHeaders().remove("Access-Control-Allow-Methods");
        exchange.getResponseHeaders().remove("Access-Control-Allow-Headers");
        
        // Add CORS headers with consistent configuration
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "http://localhost:5173");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, x-user-email");
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
        
        // For CORS preflight response
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            try {
                exchange.sendResponseHeaders(204, -1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}