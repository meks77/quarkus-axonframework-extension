package at.meks.quarkiverse.axon.shared.adapter;

import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import at.meks.quarkiverse.axon.shared.model.Paymentservice;

@ApplicationScoped
public class QuarkusPaymentservice implements Paymentservice {

    private final Set<String> preparedPayments = new HashSet<>();
    private final Set<String> paidDeposits = new HashSet<>();

    public void preparePayment(String id) {
        preparedPayments.add(id);
    }

    public void payDeposit(String id) {
        paidDeposits.add(id);
    }

    public boolean isPrepared(String id) {
        return preparedPayments.contains(id);
    }

    public boolean isPaid(String id) {
        return paidDeposits.contains(id);
    }

}
