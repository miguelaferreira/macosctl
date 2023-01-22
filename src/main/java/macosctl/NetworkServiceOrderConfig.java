package macosctl;


import io.vavr.collection.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NetworkServiceOrderConfig {

    String primaryService;
    String secondaryService;
    List<String> users;
    boolean exclusiveUserMatch;
    boolean druRun;
}
