package com.book.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.book.utils.BookCsvUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class BookManagementHandler implements HttpHandler {
    
    @Override
    @SuppressWarnings({"UseSpecificCatch", "CallToPrintStackTrace"})
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String response;
        int statusCode = 200;

        try {
            String path = exchange.getRequestURI().getPath();
            String bookId = path.substring(path.lastIndexOf('/') + 1);
            
            switch (exchange.getRequestMethod()) {
                case "DELETE" -> {
                    if (BookCsvUtils.deleteBook(bookId)) {
                        response = new JSONObject()
                            .put("message", "Book deleted successfully")
                            .put("status", "success")
                            .toString();
                    } else {
                        statusCode = 404;
                        response = new JSONObject()
                            .put("error", "Book not found")
                            .put("status", "error")
                            .toString();
                    }
                }
                case "PUT" -> {
                    // Read request body
                    BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                    String requestBody = reader.lines().collect(Collectors.joining());
                    JSONObject bookData = new JSONObject(requestBody);

                    String updatedBookEntry = String.format("%s,%s,%s,%s,%s,%.2f,%s",
                        bookId,
                        bookData.getString("title"),
                        bookData.getString("author"),
                        bookData.getString("category"),
                        bookData.getString("description").replace(",", ";"),
                        bookData.getDouble("price"),
                        bookData.getString("image")
                    );

                    if (BookCsvUtils.updateBook(bookId, updatedBookEntry)) {
                        response = new JSONObject()
                            .put("message", "Book updated successfully")
                            .put("status", "success")
                            .toString();
                    } else {
                        statusCode = 404;
                        response = new JSONObject()
                            .put("error", "Book not found")
                            .put("status", "error")
                            .toString();
                    }
                }
                default -> {
                    statusCode = 405;
                    response = new JSONObject()
                        .put("error", "Method not allowed")
                        .put("status", "error")
                        .toString();
                }
            }
        } catch (Exception e) {
            statusCode = 500;
            response = new JSONObject()
                .put("error", e.getMessage())
                .put("status", "error")
                .toString();
            e.printStackTrace();
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}