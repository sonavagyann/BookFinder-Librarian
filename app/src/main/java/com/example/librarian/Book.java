package com.example.librarian;

import java.util.List;
import java.util.Objects;
public class Book {
    private String id, title, author, imageLink, bookedBy;
    private Boolean isBooked;

    public Book() {}

    public Book(String id, String title, String author, String imageLink, String bookedBy, Boolean isBooked) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.imageLink = imageLink;
        this.isBooked = isBooked;
        this.bookedBy = bookedBy;
    }

    @Override
    public String toString() {
        return "Title: " + title + "\n" +
                "Author: " + author + "\n";
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getImageLink() {
        return imageLink;
    }

    public String getBookedBy(){ return bookedBy;}

    public String getId() {return id;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Book)) {
            return false;
        }
        Book other = (Book) obj;
        return Objects.equals(id, other.id)  &&
                Objects.equals(title, other.title) && Objects.equals(author, other.author) &&
                Objects.equals(imageLink, other.imageLink) && isBooked == other.isBooked;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, author,imageLink, isBooked);
    }
}