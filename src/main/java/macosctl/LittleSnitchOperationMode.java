package macosctl;

import io.vavr.collection.Stream;
import io.vavr.control.Option;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public enum LittleSnitchOperationMode {
    ALERT(0), SILENT_ALLOW(1), SILENT_DENNY(2);

    @Getter
    private final int code;

    LittleSnitchOperationMode(int code) {
        this.code = code;
    }

    static Option<LittleSnitchOperationMode> fromCode(int code) {
        log.trace("Trying to instantiate LittleSnitchOperationMode from code '{}'", code);
        return Stream.ofAll(Arrays.stream(LittleSnitchOperationMode.values()))
                     .filter(e -> e.getCode() == code)
                     .headOption();
    }
}
