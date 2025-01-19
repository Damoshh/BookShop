package com.book.handler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class BookHandler implements HttpHandler {
    private static final String BOOKS_CSV_PATH = "books.csv";

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
                // Debug each field
                System.out.println("Processing book id: " + book.get("id"));
                System.out.println("Title: " + book.get("title"));
                
                jsonBook.put("_id", book.get("id"));
                jsonBook.put("title", book.get("title")); // Put title first
                jsonBook.put("name", book.get("title")); // Then name for backwards compatibility
                jsonBook.put("author", book.get("author"));
                jsonBook.put("category", book.get("category"));
                jsonBook.put("description", book.get("description"));
                jsonBook.put("price", Double.parseDouble(book.get("price")));
                jsonBook.put("image", book.get("image"));
                jsonBook.put("coverImg", book.get("image"));
                
                // Debug final JSON
                System.out.println("Created JSON: " + jsonBook.toString());
                
                jsonArray.put(jsonBook);
            }
            response = jsonArray.toString();

        } catch (Exception e) {
            statusCode = 500;
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", e.getMessage());
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
        
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_CSV_PATH))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("CSV file is empty!");
                return books;
            }
            
            // Debug the headers
            System.out.println("CSV Headers: " + headerLine);
            
            String[] headers = headerLine.split(",");
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i].trim();
                System.out.println("Header " + i + ": '" + headers[i] + "'");
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= headers.length) {
                    Map<String, String> book = new HashMap<>();
                    for (int i = 0; i < headers.length; i++) {
                        String value = values[i].trim();
                        // Remove any quotes
                        value = value.replaceAll("^\"|\"$", "");
                        book.put(headers[i], value);
                    }
                    
                    // Debug the book object
                    System.out.println("Created book: " + book);
                    
                    books.add(book);
                } else {
                    System.out.println("Warning: Malformed line: " + line);
                }
            }
        }
        
        // Debug final list
        System.out.println("Total books loaded: " + books.size());
        return books;
    }

    private List<Map<String, String>> getBooksByCategory(String category) throws IOException {
        return getAllBooks().stream()
            .filter(book -> category.equalsIgnoreCase(book.get("category")))
            .collect(Collectors.toList()); // Fixed Collectors usage
    }
}