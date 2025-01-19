package com.book.model;

import java.util.List;

public class CartResponse {
    private final List<CartItem> items;
    private final int totalItems;
    private final double totalAmount;
    private final double deliveryFee;
    private final double grandTotal;

    public CartResponse(List<CartItem> items, int totalItems, double totalAmount, 
                        double deliveryFee, double grandTotal) {
        this.items = items;
        this.totalItems = totalItems;
        this.totalAmount = totalAmount;
        this.deliveryFee = deliveryFee;
        this.grandTotal = grandTotal;
    }

    public List<CartItem> getItems() { return items; }
    public int getTotalItems() { return totalItems; }
    public double getTotalAmount() { return totalAmount; }
    public double getDeliveryFee() { return deliveryFee; }
    public double getGrandTotal() { return grandTotal; }
}