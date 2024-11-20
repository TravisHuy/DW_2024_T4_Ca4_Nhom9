package org.nhathuy.model;

public class Sport {
    private String id;
    private String name;
    private String original_price;
    private String discounted_price;
    private String discount_percentage;
    private String colors;
    private String sizes;
    private String materials;
    private String reviewScore;
    private String ratingCount;
    private String viewCount;
    private String date;

    public Sport(){

    }
    public Sport(String name, String discounted_price, String original_price, String id) {
        this.name = name;
        this.discounted_price = discounted_price;
        this.original_price = original_price;
        this.id = id;
    }

    public Sport(String id, String name, String original_price, String discounted_price,String discount_percentage, String colors, String sizes, String materials, String reviewScore, String ratingCount, String viewCount, String date) {
        this.id = id;
        this.name = name;
        this.original_price = original_price;
        this.discounted_price = discounted_price;
        this.discount_percentage = discount_percentage;
        this.colors = colors;
        this.sizes = sizes;
        this.materials = materials;
        this.reviewScore = reviewScore;
        this.ratingCount = ratingCount;
        this.viewCount = viewCount;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginal_price() {
        return original_price;
    }

    public void setOriginal_price(String original_price) {
        this.original_price = original_price;
    }

    public String getDiscounted_price() {
        return discounted_price;
    }

    public void setDiscounted_price(String discounted_price) {
        this.discounted_price = discounted_price;
    }

    public String getColors() {
        return colors;
    }

    public String getDiscount_percentage() {
        return discount_percentage;
    }

    public void setDiscount_percentage(String discount_percentage) {
        this.discount_percentage = discount_percentage;
    }

    public void setColors(String colors) {
        this.colors = colors;
    }

    public String getSizes() {
        return sizes;
    }

    public void setSizes(String sizes) {
        this.sizes = sizes;
    }

    public String getMaterials() {
        return materials;
    }

    public void setMaterials(String materials) {
        this.materials = materials;
    }

    public String getReviewScore() {
        return reviewScore;
    }

    public void setReviewScore(String reviewScore) {
        this.reviewScore = reviewScore;
    }

    public String getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(String ratingCount) {
        this.ratingCount = ratingCount;
    }

    public String getViewCount() {
        return viewCount;
    }

    public void setViewCount(String viewCount) {
        this.viewCount = viewCount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Sport{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", original_price='" + original_price + '\'' +
                ", discounted_price='" + discounted_price + '\'' +
                ", discount_percentage='" + discount_percentage + '\'' +
                ", colors='" + colors + '\'' +
                ", sizes='" + sizes + '\'' +
                ", materials='" + materials + '\'' +
                ", reviewScore='" + reviewScore + '\'' +
                ", ratingCount='" + ratingCount + '\'' +
                ", viewCount='" + viewCount + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
