package com.santimpay.posctl.inventory.domain;

import com.santimpay.posctl.shared.domain.AggregateRoot;
import com.santimpay.posctl.shared.domain.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;

/** Connectivity SIM bound to a device. Simple lifecycle; its own aggregate. */
@Getter
@Entity
@Table(name = "sim_cards", schema = "inventory")
public class SimCard extends AggregateRoot<SimCard> {

    @Column(name = "msisdn", nullable = false, unique = true)
    private String msisdn;

    @Column(name = "iccid", unique = true)
    private String iccid;

    @Column(name = "carrier")
    private String carrier;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SimStatus status;

    @Column(name = "data_plan")
    private String dataPlan;

    protected SimCard() {}

    public static SimCard register(String msisdn, String iccid, String carrier) {
        if (msisdn == null || msisdn.isBlank()) {
            throw DomainException.invalidState("msisdn is required");
        }
        SimCard s = new SimCard();
        s.assignIdentityIfAbsent();
        s.msisdn = msisdn;
        s.iccid = iccid;
        s.carrier = carrier;
        s.status = SimStatus.IN_STOCK;
        return s;
    }

    public void activate() { this.status = SimStatus.ACTIVE; }

    public void deactivate() { this.status = SimStatus.DEACTIVATED; }
}
