package com.book.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class SearchBookHandler implements HttpHandler {
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
                System.out.println("Processing book id: " + book.get("id")); // Debug log
                
                jsonBook.put("_id", book.get("id"));
                jsonBook.put("title", book.get("title")); // Get title directly
                jsonBook.put("name", book.get("title")); // Add name for compatibility
                jsonBook.put("author", book.get("author"));
                jsonBook.put("category", book.get("category"));
                jsonBook.put("description", book.get("description"));
                jsonBook.put("price", Double.parseDouble(book.get("price")));
                jsonBook.put("image", book.get("image"));
                jsonBook.put("coverImg", book.get("image")); 
                
                System.out.println("Created JSON: " + jsonBook.toString()); // Debug log
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
        if (query == null || query.trim().isEmpty())
         {
            return getAllBooks();
        }
        
        final String searchQuery = query.toLowerCase().trim();
        System.out.println("Processing search for: " + searchQuery);
        
        return getAllBooks().stream()
            .filter(book -> {
                // Get all searchable fields
                String title = book.get("title") != null ? book.get("title").toLowerCase() : "";
                String author = book.get("author") != null ? book.get("author").toLowerCase() : "";
                String category = book.get("category") != null ? book.get("category").toLowerCase() : "";
                String description = book.get("description") != null ? book.get("description").toLowerCase() : "";
                
                // Split search query into words to match partial words
                String[] searchWords = searchQuery.split("\\s+");
                
                // Check if any search word is contained in any field
                for (String word : searchWords) {
                    if (title.contains(word) || 
                        author.contains(word) || 
                        category.contains(word) || 
                        description.contains(word)) {
                        return true;
                    }
                }
                
                return false;
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