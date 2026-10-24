// LESSON 2: Classes and objects.
// Run with:  java Lesson02.java

import java.math.BigDecimal;

public class Lesson02 {

    public static void main(String[] args) {

        // CREATING OBJECTS: "new" builds one object from the class blueprint (Product, below).
        Product keyboard = new Product("Mechanical Keyboard", new BigDecimal("89.99"), 25);
        Product mouse = new Product("Wireless Mouse", new BigDecimal("29.50"), 60);

        // Each object has its OWN data. Changing one does not affect the other.
        System.out.println(keyboard.getName() + " costs " + keyboard.getPrice());
        System.out.println(mouse.getName() + " costs " + mouse.getPrice());

        // BEHAVIOUR: objects carry methods that work on their own data.
        keyboard.sell(5);
        System.out.println("Keyboards left: " + keyboard.getStock());   // 20
        System.out.println("Mice left: " + mouse.getStock());           // still 60

        // ENCAPSULATION in action: the class protects its own rules.
        try {
            keyboard.sell(100);   // more than we have
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected: " + e.getMessage());
        }

        // keyboard.stock = -5;   // <- try uncommenting: compile error, because stock is private

        // WHY BigDecimal? Compare:
        System.out.println(0.1 + 0.2);                                              // 0.30000000000000004 (wrong!)
        System.out.println(new BigDecimal("0.1").add(new BigDecimal("0.2")));       // 0.3 (exact)

        // Two objects, same content: are they "equal"?
        Product a = new Product("Pen", new BigDecimal("1.00"), 1);
        Product b = new Product("Pen", new BigDecimal("1.00"), 1);
        System.out.println(a == b);   // false: == asks "same object in memory?"
        Product c = a;
        System.out.println(a == c);   // true: c points to the very same object
    }
}

// A CLASS is a blueprint. It defines what data an object holds (fields)
// and what it can do (methods). This is a simplified version of your real Product.java.
class Product {

    // FIELDS: the object's data. "private" = only code inside this class can touch it.
    private final String name;      // "final" = can be set once, never changed
    private final BigDecimal price;
    private int stock;              // not final: it changes when we sell

    // CONSTRUCTOR: runs when you write "new Product(...)". Same name as the class, no return type.
    // It is the place to set up a valid object.
    public Product(String name, BigDecimal price, int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("stock cannot be negative");
        }
        this.name = name;           // "this.name" = the field; "name" = the parameter
        this.price = price;
        this.stock = stock;
    }

    // GETTERS: controlled read access to private fields.
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }

    // A behaviour method. The rule "never sell more than we have" lives HERE,
    // so no other code can break it.
    public void sell(int quantity) {
        if (quantity > stock) {
            throw new IllegalArgumentException("Only " + stock + " in stock");
        }
        stock = stock - quantity;
    }
}
