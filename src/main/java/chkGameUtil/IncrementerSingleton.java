package chkGameUtil;

public class IncrementerSingleton {
    private static IncrementerSingleton instance;
    private int i = 0; // Counter

    // Private constructor prevents direct instantiation
    private IncrementerSingleton() {}

    // Public method to get the singleton instance
    public static IncrementerSingleton getInstance() {
        if (instance == null) {
            instance = new IncrementerSingleton();
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
