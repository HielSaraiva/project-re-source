package edu.br.resource.resourcesystem.service.donor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DonorInventoryServiceTest {

    private final DonorInventoryService inventoryService = new DonorInventoryService();

    @Test
    void selectsRegisteredStockFromTheRequestedCategory() {
        var donations = inventoryService.registeredDonationsForCategory("Saúde & Mobilidade");

        assertThat(donations).hasSize(2).allSatisfy(donation -> {
            assertThat(donation.category()).isEqualTo("Saúde & Mobilidade");
            assertThat(donation.status()).isEqualTo("registered");
            assertThat(donation.quantity()).isPositive();
        });
        assertThat(inventoryService.registeredDonationsForCategory("Vestuário"))
                .extracting(DonorInventoryService.InventoryDonation::id).containsExactly("children-clothes");
    }

    @Test
    void excludesDonationsAwaitingAcceptanceAndEntriesWithoutStock() {
        assertThat(inventoryService.registeredDonationsForCategory("Eletrônicos"))
                .extracting(DonorInventoryService.InventoryDonation::id).containsExactly("notebooks");
        assertThat(inventoryService.registeredDonationsForCategory("Móveis"))
                .extracting(DonorInventoryService.InventoryDonation::id).containsExactly("school-chairs");
    }

    @Test
    void returnsAnEmptyInventoryWhenNoCompatibleDonationIsRegistered() {
        assertThat(inventoryService.registeredDonationsForCategory("Educação")).isEmpty();
        assertThat(inventoryService.registeredDonationsForCategory("Higiene")).isEmpty();
    }
}
