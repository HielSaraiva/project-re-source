package edu.br.resource.resourcesystem.quality.unit.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import edu.br.resource.resourcesystem.service.auth.*;

import org.junit.jupiter.api.*;

@DisplayName("RN-019 — fila de recuperação não revela falhas internas ao solicitante")
class RecoveryDeliveryTest {
    @Test
    void submit_WithEligibleRequest_DelegatesAddressAndEmail() {
        var recovery = mock(PasswordRecoveryService.class);
        new PasswordRecoveryDelivery(recovery).submit("fake@example.test", "198.51.100.1");
        verify(recovery).request("fake@example.test", "198.51.100.1");
    }

    @Test
    void submit_WithInternalFailure_DoesNotRevealAccountOrThrow() {
        var recovery = mock(PasswordRecoveryService.class);
        doThrow(new IllegalStateException("fictitious secret"))
                .when(recovery)
                .request(any(), any());
        assertThatCode(
                        () ->
                                new PasswordRecoveryDelivery(recovery)
                                        .submit("fake@example.test", "198.51.100.1"))
                .doesNotThrowAnyException();
    }
}
