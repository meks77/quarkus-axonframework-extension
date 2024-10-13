package io.quarkiverse.axonframework.extension.test.projection;

public class GiftcardView {

    private final String id;

    private int currentAmount;

    @SuppressWarnings("unused")
    GiftcardView() {
        // needed by jackson
        id = null;
    }

    public GiftcardView(String id, int currentAmount) {
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

}
