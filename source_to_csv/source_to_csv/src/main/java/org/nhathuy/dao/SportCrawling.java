package org.nhathuy.dao;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nhathuy.model.Sport;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SportCrawling {
    private static final String BASE_URL = "https://vnsport.com.vn/danh-muc/phu-kien-the-thao/";

    public static void main(String[] args) {
        System.out.println(getSportDetailCard(BASE_URL));
        System.out.println("---------------------");
        for (Sport sport:getAllSport(BASE_URL)){
            System.out.println(sport);
        }
    }
    public static Sport getSportDetailCard(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();
            String name = doc.selectFirst("h3.mb-5px.title-pro-cat")!=null ? doc.selectFirst("h3.mb-5px.title-pro-cat").text():"";
            String maSp = doc.selectFirst("div.codepro")!=null ? doc.selectFirst("div.codepro").text():"";

            if (maSp.startsWith("Mã SP:")) {
                maSp = maSp.replace("Mã SP:", "").trim();
            }

            Element priceElement = doc.selectFirst("div.gia");
            String original_price = "";
            String discounted_price = "";
            if (priceElement != null) {
                Element oldPriceElement = priceElement.selectFirst("del.price-old span.woocommerce-Price-amount");
                Element newPriceElement = priceElement.selectFirst("ins.price-new span.woocommerce-Price-amount");

                original_price = oldPriceElement != null ? oldPriceElement.text() : "";
                discounted_price = newPriceElement != null ? newPriceElement.text() : "";
            }
            Sport sport = new Sport();
            sport.setName(name);
            sport.setId(maSp);
            sport.setOriginal_price(original_price);
            sport.setDiscounted_price(discounted_price);
            sport.setDate(LocalDate.now().toString());
            return sport;

        } catch (Exception e) {
            System.err.println("Error getting product details from "+ url +": "+e.getMessage());
            return null;
        }
    }

    public static Sport getSportDetails(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            Sport sport = new Sport();

            // Get basic product info
            String name = doc.selectFirst("h1.title-sing-pro") != null ?
                    doc.selectFirst("h1.title-sing-pro").text().trim() : "";
            sport.setName(name);

            // Get price information
            Element priceElement = doc.selectFirst("div.gia");
            if (priceElement != null) {
                Element oldPriceElement = priceElement.selectFirst("del.price-old span.woocommerce-Price-amount");
                Element newPriceElement = priceElement.selectFirst("ins.price-new span.woocommerce-Price-amount");
                Element perElement = priceElement.selectFirst("span.per");

                String original_price = oldPriceElement != null ? oldPriceElement.text() : "";
                String discounted_price = newPriceElement != null ? newPriceElement.text() : "";

                String percent = perElement != null ?
                        perElement.text().replaceAll("[()-]", "").trim() : "";

                sport.setOriginal_price(original_price);
                sport.setDiscounted_price(discounted_price);
                sport.setDiscount_percentage(percent);
            }

            Element ratingElement = doc.selectFirst("input.rating");
            if (ratingElement != null) {
                String ratingValue = ratingElement.attr("value");  // Gets the value of the rating input
                sport.setReviewScore(ratingValue);  // Set the rating to product

                // Get the number of reviews
                Element reviewsElement = ratingElement.siblingElements().select("span.color-var2").first();
                if (reviewsElement != null) {
                    String reviewsText = reviewsElement.text();
                    String ratingCount = reviewsText.replaceAll("[^0-9]", "");  // Remove non-digit characters
                    sport.setRatingCount(ratingCount);  // Set the number of reviews
                }
            }

            //get view cout
            Element viewElement = doc.selectFirst("div.panel-body");
            if(viewElement!=null){
                Element viewerElement = viewElement.selectFirst("p.red");
                String view = viewerElement != null ? viewerElement.text():"";
                if(view.startsWith("Lượt Xem: ")){
                    view = view.replace("Lượt Xem: ","").trim();
                }
                sport.setViewCount(view);
            }

            // Get detailed information from the table
            Elements tableRows = doc.select("table tbody tr");
            for (Element row : tableRows) {
                Element labelCell = row.selectFirst("td");
                Element valueCell = row.select("td").size() > 1 ? row.select("td").get(1) : null;

                if (labelCell != null && valueCell != null) {
                    String label = labelCell.text().trim();
                    String value = valueCell.text().trim();

                    // Remove leading/trailing whitespace and any non-breaking spaces
                    value = value.replaceAll("^\\s+", "").replaceAll("\\s+$", "");

                    switch (label) {
                        case "Màu Sắc":
                            sport.setColors(value);
                            break;
                        case "Kích Thước":
                            sport.setSizes(value);
                            break;
                        case "Chất Liệu":
                            sport.setMaterials(value);
                            break;
                        case "Mã SP":
                            sport.setId(value);
                            break;
                    }
                }
            }
            //transfer day/month/year
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            sport.setDate(LocalDate.now().format(formatter));

            return sport;

        } catch (Exception e) {
            System.err.println("Error getting product details from " + url + ": " + e.getMessage());
            return null;
        }
    }

    public static List<Sport> getAllSport(String url){
        List<Sport> sports = new ArrayList();
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            Elements sportElements = doc.select("div.cot5.dv"); // Các thẻ sản phẩm

            for (Element sportElement : sportElements) {
                // Lấy đường dẫn tới trang chi tiết sản phẩm
                String sportDetailUrl = sportElement.selectFirst("a") != null
                        ? sportElement.selectFirst("a").attr("href")
                        : "";

                // Lấy mã sản phẩm từ card
                String maSp = sportElement.selectFirst("div.codepro") != null
                        ? sportElement.selectFirst("div.codepro").text()
                        : "";
                if (maSp.startsWith("Mã SP:")) {
                    maSp = maSp.replace("Mã SP:", "").trim();
                }

//                System.out.println(productDetailUrl);
                if (!sportDetailUrl.isEmpty()) {
                    // Lấy chi tiết sản phẩm từ trang chi tiết
                    Sport sport = getSportDetails(sportDetailUrl);

                    if (sport != null) {
                        sport.setId(maSp);
                        sports.add(sport);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting product details from " + url + ": " + e.getMessage());
            return null;
        }

        return sports;
    }
}