package macosctl;


import io.vavr.collection.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LittleSnitchModeConfig {

    LittleSnitchOperationMode primaryMode;
    LittleSnitchOperationMode secondaryMode;
    List<String> users;
    boolean exclusiveUserMatch;
    boolean druRun;
}
