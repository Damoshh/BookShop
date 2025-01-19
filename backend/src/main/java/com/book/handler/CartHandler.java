package com.book.handler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.book.model.Book;
import com.book.model.CartItem;
import com.book.model.CartResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class CartHandler implements HttpHandler {
    private static final String CART_CSV_PATH = "cartItems.csv";
    private static final String BOOKS_CSV_PATH = "books.csv";

    @Override
    @SuppressWarnings({"UseSpecificCatch", "CallToPrintStackTrace"})
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        int statusCode = 200;

        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, Book> books = readBooks();
            String userId;
            CartResponse cart = null;

            if ("GET".equals(method)) {
                String[] pathParts = path.split("/");
                userId = pathParts[pathParts.length - 1];
                cart = getCartItems(userId, books);
                
            } else if ("POST".equals(method)) {
                BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    requestBody.append(line);
                }
                
                JSONObject jsonRequest = new JSONObject(requestBody.toString());
                userId = jsonRequest.getString("userId");
                String bookId = jsonRequest.getString("bookId");
                
                Book book = books.get(bookId);
                if (book == null) {
                    throw new IllegalArgumentException("Book not found");
                }

                if (path.endsWith("/add")) {
                    cart = addToCart(userId, bookId, book.getPrice(), books);
                } else if (path.endsWith("/remove")) {
                    cart = removeFromCart(userId, bookId, books);
                } else {
                    throw new IllegalArgumentException("Invalid cart operation");
                }
            }

            if (cart != null) {
                JSONObject jsonResponse = createCartResponse(cart, books);
                response = jsonResponse.toString();
            }

        } catch (Exception e) {
            statusCode = 500;
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", e.getMessage());
            response = errorResponse.toString();
            e.printStackTrace();
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private JSONObject createCartResponse(CartResponse cart, Map<String, Book> books) {
        JSONObject jsonResponse = new JSONObject();
        JSONArray itemsArray = new JSONArray();
        
        for (CartItem item : cart.getItems()) {
            JSONObject itemJson = new JSONObject();
            Book book = books.get(item.getBookId());
            
            if (book != null) {
                itemJson.put("id", item.getId());
                itemJson.put("bookId", item.getBookId());
                itemJson.put("title", book.getTitle());
                itemJson.put("author", book.getAuthor());
                itemJson.put("image", book.getImage());
                itemJson.put("category", book.getCategory());
                itemJson.put("description", book.getDescription());
                itemJson.put("quantity", item.getQuantity());
                itemJson.put("price", item.getPrice());
            }
            itemsArray.put(itemJson);
        }
        
        jsonResponse.put("items", itemsArray);
        jsonResponse.put("subtotal", cart.getTotalAmount());
        jsonResponse.put("deliveryFee", cart.getDeliveryFee());
        jsonResponse.put("total", cart.getGrandTotal());
        jsonResponse.put("totalItems", cart.getTotalItems());
        
        return jsonResponse;
    }

    private Map<String, Book> readBooks() throws IOException {
        Map<String, Book> books = new HashMap<>();
        File file = new File(BOOKS_CSV_PATH);
        
        if (!file.exists()) {
            return books;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine(); // Skip header
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 7) {
                    String image = values[6].trim();
                    if (!image.startsWith("/images/")) {
                        image = "/images/" + image;
                    }
                    
                    Book book = new Book(
                        values[0].trim(),  // _id
                        values[1].trim(),  // title
                        values[2].trim(),  // author
                        Double.parseDouble(values[5].trim()),  // price
                        values[3].trim(),  // category
                        values[4].trim(),  // description
                        image  // coverImg
                    );
                    books.put(book.get_id(), book);
                }
            }
        }
        return books;
    }

    public CartResponse getCartItems(String userId, Map<String, Book> books) throws IOException {
        List<CartItem> items = readCartItems().stream()
            .filter(item -> item.getUserId().equals(userId))
            .collect(Collectors.toList());
    
        return calculateCartTotals(items);
    }

    public CartResponse addToCart(String userId, String bookId, double price, Map<String, Book> books) throws IOException {
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
        return calculateCartTotals(items.stream()
            .filter(item -> item.getUserId().equals(userId))
            .collect(Collectors.toList()));
    }

    public CartResponse removeFromCart(String userId, String bookId, Map<String, Book> books) throws IOException {
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
    
        List<CartItem> userItems = items.stream()
            .filter(item -> item.getUserId().equals(userId))
            .collect(Collectors.toList());
            
        return calculateCartTotals(userItems);
    }

    private CartResponse calculateCartTotals(List<CartItem> items) {
        if (items.isEmpty()) {
            return new CartResponse(items, 0, 0, 0, 0);
        }
    
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

    private double calculateDeliveryFee(double totalAmount) {
        if (totalAmount == 0) return 0.0;
        if (totalAmount >= 100) return 0.0;
        if (totalAmount >= 50) return 5.0;
        return 10.0;
    }

    @SuppressWarnings("UnnecessaryTemporaryOnConversionFromString")
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