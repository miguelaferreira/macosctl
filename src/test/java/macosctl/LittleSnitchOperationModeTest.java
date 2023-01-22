package macosctl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LittleSnitchOperationModeTest {

    @Test
    void readFromCode() {
        assertThat(LittleSnitchOperationMode.fromCode(0))
                .isNotEmpty()
                .contains(LittleSnitchOperationMode.ALERT);

        assertThat(LittleSnitchOperationMode.fromCode(1))
                .isNotEmpty()
                .contains(LittleSnitchOperationMode.SILENT_ALLOW);

        assertThat(LittleSnitchOperationMode.fromCode(2))
                .isNotEmpty()
                .contains(LittleSnitchOperationMode.SILENT_DENNY);

        assertThat(LittleSnitchOperationMode.fromCode(3))
                .isEmpty();

        assertThat(LittleSnitchOperationMode.fromCode(4))
                .isEmpty();

        assertThat(LittleSnitchOperationMode.fromCode(-1))
                .isEmpty();
    }
}
