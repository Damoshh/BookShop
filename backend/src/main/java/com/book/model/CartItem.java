package com.book.model;

import java.time.LocalDateTime;

public class CartItem {
    private Long id;
    private String userId;
    private String bookId;
    private int quantity;
    private double price;
    private LocalDateTime dateAdded;

    // Default constructor
    public CartItem() {}

    // Constructor with fields
    public CartItem(Long id, String userId, String bookId, int quantity, double price, LocalDateTime dateAdded) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.price = price;
        this.dateAdded = dateAdded;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public LocalDateTime getDateAdded() { return dateAdded; }
    public void setDateAdded(LocalDateTime dateAdded) { this.dateAdded = dateAdded; }
}