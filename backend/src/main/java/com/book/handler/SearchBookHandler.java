package com.book.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class SearchBookHandler implements HttpHandler {
    private static final String BOOKS_CSV_PATH = "books.csv";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String response = "";
        int statusCode = 200;

        try {
            // Get the query parameter
            String query = exchange.getRequestURI().getQuery();
            List<Map<String, String>> books;

            if (query != null && query.startsWith("q=")) {
                String searchQuery = URLDecoder.decode(query.substring(2), StandardCharsets.UTF_8);
                System.out.println("Processing search query: " + searchQuery); // Debug log
                
                if (searchQuery.trim().isEmpty()) {
                    books = getAllBooks(); // Return all books for empty search
                } else {
                    books = searchBooks(searchQuery.trim());
                }
            } else {
                books = getAllBooks(); // Return all books if no query
            }

            // Convert to format expected by frontend
            JSONArray jsonArray = new JSONArray();
            for (Map<String, String> book : books) {
                JSONObject jsonBook = new JSONObject();
                jsonBook.put("_id", book.get("id"));
                jsonBook.put("title", book.get("name")); // Map 'name' to 'title'
                jsonBook.put("author", book.get("author"));
                jsonBook.put("category", book.get("category"));
                jsonBook.put("description", book.get("description"));
                jsonBook.put("price", Double.parseDouble(book.get("price")));
                jsonBook.put("coverImg", book.get("image")); // Map 'image' to 'coverImg'
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

        // Set response headers
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private List<Map<String, String>> searchBooks(String query) throws IOException {
        if (query == null || query.trim().isEmpty()) {
            return getAllBooks();
        }
        
        final String searchQuery = query.toLowerCase().trim();
        System.out.println("Processing search for: " + searchQuery); // Debug log
        
        return getAllBooks().stream()
            .filter(book -> {
                String name = book.get("name") != null ? book.get("name").toLowerCase() : "";
                String author = book.get("author") != null ? book.get("author").toLowerCase() : "";
                String category = book.get("category") != null ? book.get("category").toLowerCase() : "";
                String description = book.get("description") != null ? book.get("description").toLowerCase() : "";
                
                // Debug logs
                System.out.println("Checking book: " + name);
                
                return name.contains(searchQuery) ||
                       author.contains(searchQuery) ||
                       category.contains(searchQuery) ||
                       description.contains(searchQuery);
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, String>> getAllBooks() throws IOException {
        List<Map<String, String>> books = new ArrayList<>();
        
        File file = new File(BOOKS_CSV_PATH);
        if (!file.exists()) {
            System.out.println("Books CSV file not found at: " + file.getAbsolutePath());
            return books;
        }
    
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return books;
            }
            
            String[] headers = headerLine.split(",");
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                Map<String, String> book = new HashMap<>();
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    book.put(headers[i].trim(), values[i].trim());
                }
                books.add(book);
            }
        }
        return books;
    }
}