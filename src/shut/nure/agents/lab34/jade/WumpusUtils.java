package shut.nure.agents.lab34.jade;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import shut.nure.agents.lab34.wumpusworld.AgentPosition;

/**
 * Various utility methods
 */
public class WumpusUtils {

    private static final String SEPARATOR_PATTERN = "\\W"; // non-word

    public static List<String> splitToWords(String sentence) {
        List<String> result = new ArrayList<>();
        if (sentence != null && !sentence.isEmpty()) {
            result = Stream.of(sentence.split(SEPARATOR_PATTERN))
                    .map(String::trim)
                    .filter(((Predicate<String>) String::isEmpty).negate())
                    .collect(Collectors.toList());
        }
        return result;
    }

    public static AgentPosition createStartPosition() {
        return new AgentPosition(1, 1, AgentPosition.Orientation.FACING_NORTH);
    }

}
