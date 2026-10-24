// LESSON 1: Java basics.
// Run with:  java Lesson01.java      (Java runs a single file directly, no setup needed)

public class Lesson01 {

    // Every Java program starts at a method named "main". Java calls it for you.
    public static void main(String[] args) {

        // 1. VARIABLES: a named box holding a value. Java needs you to declare the TYPE.
        String productName = "Wireless Mouse";   // String = text
        int quantity = 3;                        // int = whole number
        double price = 29.50;                    // double = decimal number
        boolean inStock = true;                  // boolean = true or false

        // 2. OPERATORS: do maths with them.
        double total = price * quantity;

        // 3. PRINTING: System.out.println writes a line to the console.
        System.out.println("Product: " + productName);   // + joins text together
        System.out.println("Total: " + total);

        // 4. IF / ELSE: make decisions.
        if (inStock && quantity <= 5) {          // && means AND, || means OR
            System.out.println("Order accepted");
        } else {
            System.out.println("Order rejected");
        }

        // 5. LOOPS: repeat things.
        for (int i = 1; i <= quantity; i++) {    // start at 1, continue while i <= 3, add 1 each time
            System.out.println("Packing item " + i);
        }

        // 6. CALLING A METHOD: reusable named block of code (defined below).
        System.out.println("Total with 10% discount: " + applyDiscount(total, 10));
    }

    // 7. METHODS: "double" is the return type, then the name, then the parameters.
    // "static" just means we can call it without creating an object (explained in Stage 2).
    static double applyDiscount(double amount, double percent) {
        return amount - (amount * percent / 100);
    }
}
