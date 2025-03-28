package chGameUtil;

public class Incrementer {
    private static Incrementer instance;
    private int i = 0; // Counter

    // Private constructor prevents direct instantiation
    private Incrementer() {}

    // Public method to get the singleton instance
    public static Incrementer getInstance() {
        if (instance == null) {
            instance = new Incrementer();
        }
        return instance;
    }

    // Method to increment and return the value
    public int increment() {
        return ++i;
    }

    // Method to get the current value
    public int getValue() {
        return i;
    }
}
