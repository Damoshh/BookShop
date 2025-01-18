package com.book.model;

public class Book {
    private String _id;
    private String title;  // Changed from name to match CSV
    private String author;
    private double price;
    private String category;
    private String description;
    private String coverImg;  // Changed from image to match CSV

    // Constructor
    public Book(String _id, String title, String author, double price, 
                String category, String description, String coverImg) {
        this._id = _id;
        this.title = title;
        this.author = author;
        this.price = price;
        this.category = category;
        this.description = description;
        this.coverImg = coverImg;
    }

    // Getters and Setters
    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverImg() { return coverImg; }
    public void setCoverImg(String coverImg) { this.coverImg = coverImg; }

    // For compatibility with frontend expecting 'name' and 'image'
    public String getName() { return title; }
    public String getImage() { return coverImg; }
}