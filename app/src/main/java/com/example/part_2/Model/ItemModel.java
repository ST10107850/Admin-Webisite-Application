package com.example.part_2.Model;

public class ItemModel {

    private String name;
    private String description;
    private String image_url;
    private String rating;
    private String type;
    private int price;

    public ItemModel() {
    }

    public ItemModel(String name, String description, String image_url, String rating, String type, int price) {
        this.name = name;
        this.description = description;
        this.image_url = image_url;
        this.rating = rating;
        this.type = type;
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

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
