public class Product {
    public final String id;
    public final String name;
    public final String category;
    public final double price;

    public Product(String id, String name, String category, double price) {
        this.id       = id;
        this.name     = name;
        this.category = category;
        this.price    = price;
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + category + " | $" + price;
    }
}
