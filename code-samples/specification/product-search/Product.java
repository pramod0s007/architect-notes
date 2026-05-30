public final class Product {

    public enum Category {
        ELECTRONICS,
        BOOKS,
        HOME
    }

    private final String name;
    private final double price;
    private final Category category;
    private final double rating;

    public Product(String name, double price, Category category, double rating) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.rating = rating;
    }

    public String name() {
        return name;
    }

    public double price() {
        return price;
    }

    public Category category() {
        return category;
    }

    public double rating() {
        return rating;
    }

    @Override
    public String toString() {
        return name + " $" + price + " " + category + " ★" + rating;
    }
}
