package com.book.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.book.utils.BookCsvUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class BookHandler implements HttpHandler {

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
            List<Map<String, String>> books;
            
            if (path.contains("/category/")) {
                String category = path.substring(path.lastIndexOf("/") + 1);
                books = getBooksByCategory(category);
            } else {
                books = getAllBooks();
            }

            JSONArray jsonArray = new JSONArray();
            for (Map<String, String> book : books) {
                JSONObject jsonBook = new JSONObject();
                jsonBook.put("_id", book.get("id"));
                jsonBook.put("title", book.get("title"));
                jsonBook.put("name", book.get("title")); // For backwards compatibility
                jsonBook.put("author", book.get("author"));
                jsonBook.put("category", book.get("category"));
                jsonBook.put("description", book.get("description"));
                jsonBook.put("price", Double.parseDouble(book.get("price")));
                jsonBook.put("image", book.get("image"));
                jsonBook.put("coverImg", book.get("image"));
                
                jsonArray.put(jsonBook);
            }
            response = jsonArray.toString();

        } catch (Exception e) {
            statusCode = 500;
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", e.getMessage());
            errorJson.put("status", "error");
            response = errorJson.toString();
            e.printStackTrace();
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private List<Map<String, String>> getAllBooks() throws IOException {
        List<Map<String, String>> books = new ArrayList<>();
        List<String> lines = BookCsvUtils.readAllLines();
        
        if (lines.isEmpty()) {
            return books;
        }
        
        String[] headers = lines.get(0).split(",");
        for (int i = 0; i < headers.length; i++) {
            headers[i] = headers[i].trim();
        }
        
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] values = line.split(",");
            if (values.length >= headers.length) {
                Map<String, String> book = new HashMap<>();
                for (int j = 0; j < headers.length; j++) {
                    String value = values[j].trim();
                    value = value.replaceAll("^\"|\"$", ""); // Remove any quotes
                    book.put(headers[j], value);
                }
                books.add(book);
            }
        }
        
        return books;
    }

    private List<Map<String, String>> getBooksByCategory(String category) throws IOException {
        return getAllBooks().stream()
            .filter(book -> category.equalsIgnoreCase(book.get("category")))
            .collect(Collectors.toList());
    }
}