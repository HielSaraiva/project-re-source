package edu.br.resource.resourcesystem.quality.unit.presentation;

import static org.assertj.core.api.Assertions.*;

import edu.br.resource.resourcesystem.dto.response.*;
import edu.br.resource.resourcesystem.model.enums.*;
import edu.br.resource.resourcesystem.presentation.CategoryIconResolver;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.*;
import java.util.stream.*;

@DisplayName("RF-003/RF-017 — categorias, códigos e contratos de apresentação")
class PresentationContractsTest {
    @ParameterizedTest
    @EnumSource(ItemCategory.class)
    void icon_WithSupportedCategory_PointsToPackagedAsset(ItemCategory category) {
        String path = new CategoryIconResolver().resolve(category);
        assertThat(getClass().getResource("/static" + path)).isNotNull();
    }

    @Test
    void icon_WithMissingCategory_UsesPackageFallback() {
        assertThat(new CategoryIconResolver().resolve(null)).endsWith("package.svg");
    }

    static Stream<Arguments> enums() {
        return Stream.of(
                        AccountRole.class,
                        AccountStatus.class,
                        InstitutionStatus.class,
                        InstitutionSector.class,
                        MatchStatus.class,
                        DonationStatus.class,
                        DeliveryMethod.class,
                        DeliveryStatus.class,
                        ItemCategory.class,
                        ItemCondition.class,
                        NecessityStatus.class,
                        NecessityPriority.class,
                        DonationStage.class)
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("enums")
    void enum_WithPersistedCode_RoundTripsWithoutLosingIdentity(Class<?> type) throws Exception {
        var parser = type.getMethod("fromValue", String.class);
        for (Object value : type.getEnumConstants()) {
            String code = (String) type.getMethod("getValue").invoke(value);
            assertThat(parser.invoke(null, code)).isSameAs(value);
        }
        assertThatThrownBy(() -> parser.invoke(null, "not-a-code"))
                .hasCauseInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> parser.invoke(null, (Object) null))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void dateLabel_WithNoCreationDate_DoesNotInventTimestamp() {
        var d =
                new DonationHistoryResponse(
                        1,
                        "Item",
                        "/icon",
                        ItemCategory.FOOD,
                        "Comida",
                        ItemCondition.GOOD,
                        null,
                        1,
                        1,
                        0,
                        0,
                        null,
                        DonationStatus.REGISTERED,
                        "Registrada",
                        0,
                        0,
                        true,
                        true,
                        null,
                        null,
                        0,
                        false);
        assertThat(d.dateLabel()).isEqualTo("—");
    }
}
