package com.example.work_intergrated.Models;

public class RecommendedModel {
    String name;
    String description;
    String rating;
    String image_url;
    int price;

    public RecommendedModel() {
    }

    public RecommendedModel(String name, String description, String rating, String image_url, int price) {
        this.name = name;
        this.description = description;
        this.rating = rating;
        this.image_url = image_url;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
