package at.meks.quarkiverse.axonframework.example.projection;

public class GiftcardDto {

    private final String id;

    private int currentAmount;

    @SuppressWarnings("unused")
    GiftcardDto() {
        // needed by jackson
        id = null;
    }

    public GiftcardDto(String id, int currentAmount) {
        this.id = id;
        this.currentAmount = currentAmount;
    }

    void redeem(int amount) {
        currentAmount -= amount;
    }

    @SuppressWarnings("unused")
    public String getId() {
        // needed by jackson
        return id;
    }

    @SuppressWarnings("unused")
    public int getCurrentAmount() {
        // needed by jackson
        return currentAmount;
    }

    public void undoLatestRedemption(int amount) {
        currentAmount += amount;
    }
}
