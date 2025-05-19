package perudo_backend.perudo_backend.dto;

import perudo_backend.perudo_backend.Bid;

public class BidDTO {
    private String playerId;
    private int quantity;
    private int value;

    public BidDTO(String playerId, int quantity, int value) {
        this.playerId = playerId;
        this.quantity = quantity;
        this.value = value;
    }
    public BidDTO(Bid currentBid) {
        if (currentBid != null) {
            this.playerId = String.valueOf(currentBid.getPlayerId());
            this.quantity = currentBid.getQuantity();
            this.value = currentBid.getValue();
        }
    }
    public String getPlayerId() {
        return playerId;
    }
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
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
