package at.meks.quarkiverse.axon.shared.model;

public interface Paymentservice {

    /**
     * prepares the payment of the deposit of the returned giftcard
     *
     * @param id id of the card
     */
    void preparePayment(String id);

    /**
     * initiates the payment of the deposit of the returned giftcard.
     *
     * @param id id of the card
     */
    void payDeposit(String id);
}
