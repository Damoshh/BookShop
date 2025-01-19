package com.book.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class BookCsvUtils {
    private static final String BOOKS_CSV_PATH = "books.csv";
    @SuppressWarnings("unused")
    private static final String CSV_HEADER = "id,title,author,category,description,price,image";

    public static synchronized List<String> readAllLines() throws IOException {
        return Files.readAllLines(Paths.get(BOOKS_CSV_PATH));
    }

    public static synchronized void writeAllLines(List<String> lines) throws IOException {
        Files.write(Paths.get(BOOKS_CSV_PATH), lines);
    }

    public static synchronized boolean deleteBook(String bookId) throws IOException {
        List<String> lines = readAllLines();
        if (lines.isEmpty()) {
            return false;
        }

        String headerLine = lines.get(0);
        List<String> updatedLines = new ArrayList<>();
        updatedLines.add(headerLine);
        
        boolean bookFound = false;
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.startsWith(bookId + ",")) {
                updatedLines.add(line);
            } else {
                bookFound = true;
            }
        }

        if (bookFound) {
            writeAllLines(updatedLines);
        }
        
        return bookFound;
    }

    public static synchronized boolean updateBook(String bookId, String updatedLine) throws IOException {
        List<String> lines = readAllLines();
        if (lines.isEmpty()) {
            return false;
        }

        String headerLine = lines.get(0);
        List<String> updatedLines = new ArrayList<>();
        updatedLines.add(headerLine);
        
        boolean bookFound = false;
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.startsWith(bookId + ",")) {
                updatedLines.add(line);
            } else {
                updatedLines.add(updatedLine);
                bookFound = true;
            }
        }

        if (bookFound) {
            writeAllLines(updatedLines);
        }
        
        return bookFound;
    }
}