package perudo_backend.perudo_backend;

public class Bid {
    private int quantity;
    private int value;

    public Bid(int quantity, int value) {
        this.quantity = quantity;
        this.value = value;
    }

    // Getters and setters
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
