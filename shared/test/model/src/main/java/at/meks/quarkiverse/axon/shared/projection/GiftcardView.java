package at.meks.quarkiverse.axon.shared.projection;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class GiftcardView {

    @SuppressWarnings("unused")
    private final String id;

    private int currentAmount;
    private String personName;

    @SuppressWarnings("unused")
    GiftcardView() {
        // needed by jackson
        id = null;
    }

    public GiftcardView(String id, int currentAmount) {
        this.id = id;
        this.currentAmount = currentAmount;
    }

    public GiftcardView(String id, int currentAmount, String personName) {
        this.id = id;
        this.currentAmount = currentAmount;
        this.personName = personName;
    }

    void redeem(int amount) {
        currentAmount -= amount;
    }

    public int getCurrentAmount() {
        return currentAmount;
    }

    public void undoLastRedemption(int amount) {
        currentAmount += amount;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

}
