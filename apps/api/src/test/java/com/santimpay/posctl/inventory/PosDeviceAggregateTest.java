package com.santimpay.posctl.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.santimpay.posctl.inventory.domain.DeviceStatus;
import com.santimpay.posctl.inventory.domain.PosDevice;
import com.santimpay.posctl.inventory.events.DeviceMarkedFaulty;
import com.santimpay.posctl.inventory.events.DeviceReceived;
import com.santimpay.posctl.shared.domain.DomainException;
import org.junit.jupiter.api.Test;

/** Device lifecycle state-machine invariants. */
class PosDeviceAggregateTest {

    @Test
    void receive_putsDeviceInStock_andRaisesEvent() {
        PosDevice d = PosDevice.receiveIntoStock("SN-1", "PAX-A920", "PAX", "T-1", "IMEI-1");

        assertThat(d.getStatus()).isEqualTo(DeviceStatus.IN_STOCK);
        assertThat(d.getRaisedEvents()).anyMatch(e -> e instanceof DeviceReceived);
    }

    @Test
    void validLifecycle_inStock_to_deployed_to_faulty() {
        PosDevice d = PosDevice.receiveIntoStock("SN-2", "PAX-A920", "PAX", null, null);
        d.allocate();
        d.markDeployed();
        d.markFaulty("screen broken");

        assertThat(d.getStatus()).isEqualTo(DeviceStatus.FAULTY);
        assertThat(d.getRaisedEvents()).anyMatch(e -> e instanceof DeviceMarkedFaulty);
    }

    @Test
    void illegalTransition_inStock_directly_to_faulty_isRejected() {
        PosDevice d = PosDevice.receiveIntoStock("SN-3", "PAX-A920", "PAX", null, null);

        assertThatThrownBy(() -> d.markFaulty("x"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Illegal device transition");
    }

    @Test
    void retire_fromRepair_isAllowed() {
        PosDevice d = PosDevice.receiveIntoStock("SN-4", "PAX-A920", "PAX", null, null);
        d.allocate();
        d.markDeployed();
        d.markFaulty("dead");
        d.sendToRepair();
        d.retire();

        assertThat(d.getStatus()).isEqualTo(DeviceStatus.RETIRED);
    }
}
