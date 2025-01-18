package com.book.handler;

import com.book.model.CartItem;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class CartHandler implements HttpHandler {
    private static final String CART_CSV_PATH = "cartItems.csv";

    public static class CartResponse {
        private List<CartItem> items;
        private int totalItems;
        private double totalAmount;
        private double deliveryFee;
        private double grandTotal;

        public CartResponse(List<CartItem> items, int totalItems, double totalAmount, 
                          double deliveryFee, double grandTotal) {
            this.items = items;
            this.totalItems = totalItems;
            this.totalAmount = totalAmount;
            this.deliveryFee = deliveryFee;
            this.grandTotal = grandTotal;
        }

        // Getters
        public List<CartItem> getItems() { return items; }
        public int getTotalItems() { return totalItems; }
        public double getTotalAmount() { return totalAmount; }
        public double getDeliveryFee() { return deliveryFee; }
        public double getGrandTotal() { return grandTotal; }
    }

    @Override
public void handle(HttpExchange exchange) throws IOException {
    String response = "";
    int statusCode = 200;

    try {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        
        // For CORS preflight requests
        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        // Regular requests
        String userId = null;
        CartResponse cart = null;

        if ("GET".equals(method)) {
            // Extract userId from path for GET requests
            String[] pathParts = path.split("/");
            userId = pathParts[pathParts.length - 1];
            
            cart = getCartItems(userId);
            JSONObject jsonResponse = new JSONObject();
            JSONArray itemsArray = new JSONArray();
            
            for (CartItem item : cart.getItems()) {
                JSONObject itemJson = new JSONObject();
                itemJson.put("id", item.getId());
                itemJson.put("bookId", item.getBookId());
                itemJson.put("quantity", item.getQuantity());
                itemJson.put("price", item.getPrice());
                itemsArray.put(itemJson);
            }
            
            jsonResponse.put("items", itemsArray);
            jsonResponse.put("total", cart.getGrandTotal());
            jsonResponse.put("totalItems", cart.getTotalItems());
            response = jsonResponse.toString();
            
        } else if ("POST".equals(method)) {
            // Read request body
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody());
            BufferedReader br = new BufferedReader(isr);
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                requestBody.append(line);
            }
            
            JSONObject jsonRequest = new JSONObject(requestBody.toString());
            userId = jsonRequest.getString("userId");
            String bookId = jsonRequest.getString("bookId");
            
            if (path.endsWith("/add")) {
                double price = jsonRequest.optDouble("price", 29.99);
                cart = addToCart(userId, bookId, price);
            } else if (path.endsWith("/remove")) {
                cart = removeFromCart(userId, bookId);
            } else {
                throw new IllegalArgumentException("Invalid cart operation");
            }

            JSONObject jsonResponse = new JSONObject();
            JSONArray itemsArray = new JSONArray();
            
            for (CartItem item : cart.getItems()) {
                JSONObject itemJson = new JSONObject();
                itemJson.put("id", item.getId());
                itemJson.put("bookId", item.getBookId());
                itemJson.put("quantity", item.getQuantity());
                itemJson.put("price", item.getPrice());
                itemsArray.put(itemJson);
            }
            
            jsonResponse.put("items", itemsArray);
            jsonResponse.put("total", cart.getGrandTotal());
            jsonResponse.put("totalItems", cart.getTotalItems());
            response = jsonResponse.toString();
        }

    } catch (Exception e) {
        statusCode = 500;
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("error", e.getMessage());
        response = errorResponse.toString();
        e.printStackTrace();
    }

    // Set content type and send response
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    byte[] responseBytes = response.getBytes("UTF-8");
    exchange.sendResponseHeaders(statusCode, responseBytes.length);
    try (OutputStream os = exchange.getResponseBody()) {
        os.write(responseBytes);
    }
}

    // Get cart items with totals for a user
    public CartResponse getCartItems(String userId) throws IOException {
        List<CartItem> items = readCartItems().stream()
            .filter(item -> item.getUserId().equals(userId))
            .collect(Collectors.toList());

        int totalItems = items.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();

        double totalAmount = items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();

        double deliveryFee = calculateDeliveryFee(totalAmount);
        double grandTotal = totalAmount + deliveryFee;

        return new CartResponse(items, totalItems, totalAmount, deliveryFee, grandTotal);
    }

    // Add item to cart
    public CartResponse addToCart(String userId, String bookId, double price) throws IOException {
        List<CartItem> items = readCartItems();
        Optional<CartItem> existingItem = items.stream()
            .filter(item -> item.getUserId().equals(userId) && item.getBookId().equals(bookId))
            .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + 1);
        } else {
            CartItem newItem = new CartItem();
            newItem.setId(generateNewId(items));
            newItem.setUserId(userId);
            newItem.setBookId(bookId);
            newItem.setQuantity(1);
            newItem.setPrice(price);
            newItem.setDateAdded(LocalDateTime.now());
            items.add(newItem);
        }

        writeCartItems(items);
        return getCartItems(userId);
    }

    // Remove item from cart
    public CartResponse removeFromCart(String userId, String bookId) throws IOException {
        List<CartItem> items = readCartItems();
        Optional<CartItem> existingItem = items.stream()
            .filter(item -> item.getUserId().equals(userId) && item.getBookId().equals(bookId))
            .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
            } else {
                items.remove(item);
            }
            writeCartItems(items);
        }

        return getCartItems(userId);
    }

    private double calculateDeliveryFee(double totalAmount) {
        if (totalAmount >= 100) {
            return 0.0; // Free delivery for orders over RM100
        } else if (totalAmount >= 50) {
            return 5.0; // RM5 delivery fee for orders between RM50-100
        } else {
            return 10.0; // RM10 delivery fee for orders under RM50
        }
    }

    private List<CartItem> readCartItems() throws IOException {
        List<CartItem> items = new ArrayList<>();
        File file = new File(CART_CSV_PATH);
        
        if (!file.exists()) {
            return items;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // Skip header
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 6) {
                    CartItem item = new CartItem();
                    item.setId(Long.parseLong(values[0].trim()));
                    item.setUserId(values[1].trim());
                    item.setBookId(values[2].trim());
                    item.setQuantity(Integer.parseInt(values[3].trim()));
                    item.setPrice(Double.parseDouble(values[4].trim()));
                    item.setDateAdded(LocalDateTime.parse(values[5].trim()));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private void writeCartItems(List<CartItem> items) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CART_CSV_PATH))) {
            writer.write("id,userId,bookId,quantity,price,dateAdded\n");
            
            for (CartItem item : items) {
                writer.write(String.format("%d,%s,%s,%d,%.2f,%s\n",
                    item.getId(),
                    item.getUserId(),
                    item.getBookId(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getDateAdded().toString()
                ));
            }
        }
    }

    private Long generateNewId(List<CartItem> items) {
        return items.stream()
            .mapToLong(CartItem::getId)
            .max()
            .orElse(0) + 1;
    }
}