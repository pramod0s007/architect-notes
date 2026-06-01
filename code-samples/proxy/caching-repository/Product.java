package proxy.cachingrepository;

public class Product {
    private final String id;
    private String name;
    private String category;
    private double price;

    public Product(String id, String name, String category, double price) {
        this.id = id; this.name = name;
        this.category = category; this.price = price;
    }

    public String getId()       { return id; }
    public String getName()     { return name; }
    public String getCategory() { return category; }
    public double getPrice()    { return price; }
    public void setPrice(double price) { this.price = price; }

    @Override
    public String toString() {
        return String.format("Product{id='%s', name='%s', price=%.2f}", id, name, price);
    }
}
