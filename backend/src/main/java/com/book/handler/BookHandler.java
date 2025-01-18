package com.book.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.stream.Collectors;
import java.io.*;
import java.util.*;

public class BookHandler implements HttpHandler {
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
                jsonBook.put("title", book.get("title")); // Using name from CSV
                jsonBook.put("author", book.get("author"));
                jsonBook.put("category", book.get("category"));
                jsonBook.put("description", book.get("description"));
                jsonBook.put("price", Double.parseDouble(book.get("price")));
                jsonBook.put("image", book.get("image")); // Using image from CSV
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
        
        // Get current working directory
        String currentPath = System.getProperty("user.dir");
        System.out.println("Current working directory: " + currentPath);
        
        File file = new File(BOOKS_CSV_PATH);
        System.out.println("Absolute path of books.csv: " + file.getAbsolutePath());
        System.out.println("Does books.csv exist? " + file.exists());
    
        if (!file.exists()) {
            System.out.println("Books CSV file not found at: " + file.getAbsolutePath());
            return books;
        }
    
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                System.out.println("CSV file is empty!");
                return books;
            }
            
            System.out.println("CSV Headers: " + headerLine);
            
            String[] headers = headerLine.split(",");
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                System.out.println("Reading line: " + line);
                String[] values = line.split(",");
                Map<String, String> book = new HashMap<>();
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    book.put(headers[i].trim(), values[i].trim());
                }
                books.add(book);
                lineCount++;
            }
            System.out.println("Total books read: " + lineCount);
        }
        return books;
    }

    private List<Map<String, String>> getBooksByCategory(String category) throws IOException {
        return getAllBooks().stream()
            .filter(book -> category.equalsIgnoreCase(book.get("category")))
            .collect(Collectors.toList()); // Fixed Collectors usage
    }
}