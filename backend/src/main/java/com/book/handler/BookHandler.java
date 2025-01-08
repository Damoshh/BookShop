package com.book.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.book.model.Book;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class BookHandler implements HttpHandler {
    private List<Book> books;

    public BookHandler() {
        books = new ArrayList<>();
        // Add sample data
        initializeBooks();
    }

    private void initializeBooks() {
        books.add(new Book(
            "1",
            "Kau dan Aku",
            "Kisah tentang dua orang sebagai pembunuh upahan",
            12.99,
            "Romance",
            "/assets/imgHeader.jpg",
            "John Doe",
            4.5,
            true
        ));

        books.add(new Book(
            "2",
            "Senja dan Pagi",
            "Perjalanan hidup yang penuh misteri",
            14.99,
            "Fiction",
            "/assets/imgHeader.jpg",
            "Jane Smith",
            4.2,
            true
        ));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Enable CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        String method = exchange.getRequestMethod();

        try {
            switch (method) {
                case "GET":
                    handleGet(exchange, path, query);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                default:
                    sendResponse(exchange, 405, "Method not allowed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error");
        }
    }

    private void handleGet(HttpExchange exchange, String path, String query) throws IOException {
        JSONArray response = new JSONArray();
        
        if (query != null && query.startsWith("category=")) {
            String category = query.split("=")[1];
            for (Book book : books) {
                if (category.equals("All") || book.getCategory().equals(category)) {
                    response.add(bookToJson(book));
                }
            }
        } else {
            for (Book book : books) {
                response.add(bookToJson(book));
            }
        }

        sendResponse(exchange, 200, response.toString());
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        // Read the POST body data
        InputStream inputStream = exchange.getRequestBody();
        InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
        JSONParser parser = new JSONParser();
        
        try {
            // Parse the incoming JSON data
            JSONObject requestBody = (JSONObject) parser.parse(reader);

            // Extract data from the JSON object
            String id = (String) requestBody.get("_id");
            String name = (String) requestBody.get("name");
            String description = (String) requestBody.get("description");
            double price = (double) requestBody.get("price");
            String category = (String) requestBody.get("category");
            String image = (String) requestBody.get("image");
            String author = (String) requestBody.get("author");
            double rating = (double) requestBody.get("rating");
            boolean inStock = (boolean) requestBody.get("inStock");

            // Create a new Book object
            Book newBook = new Book(id, name, description, price, category, image, author, rating, inStock);

            // Add the new book to the list
            books.add(newBook);

            // Send a success response
            JSONObject response = new JSONObject();
            response.put("message", "Book added successfully");
            sendResponse(exchange, 201, response.toString());

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 400, "Invalid JSON input");
        }
    }

    private JSONObject bookToJson(Book book) {
        JSONObject json = new JSONObject();
        json.put("_id", book.get_id());
        json.put("name", book.getName());
        json.put("description", book.getDescription());
        json.put("price", book.getPrice());
        json.put("category", book.getCategory());
        json.put("image", book.getImage());
        json.put("author", book.getAuthor());
        json.put("rating", book.getRating());
        json.put("inStock", book.isInStock());
        return json;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
