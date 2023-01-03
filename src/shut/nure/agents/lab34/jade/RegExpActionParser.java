package shut.nure.agents.lab34.jade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * regular expressions based implementation of {@link ActionParser}.
 */
public class RegExpActionParser implements ActionParser {

    private static final Map<EnvActionType, List<Pattern>> DEFAULT_DICT = new HashMap<EnvActionType, List<Pattern>>() {{
        put(EnvActionType.CLIMB, Arrays.asList(
                Pattern.compile("climb")
        ));
        put(EnvActionType.FORWARD, Arrays.asList(
                Pattern.compile("forward|straight")
        ));
        put(EnvActionType.TURN_LEFT, Arrays.asList(
                Pattern.compile("left|counterclockwise")
        ));
        put(EnvActionType.TURN_RIGHT, Arrays.asList(
                Pattern.compile("right|clockwise")
        ));
        put(EnvActionType.SHOOT, Arrays.asList(
                Pattern.compile("shoot|arrow|bow")
        ));
        put(EnvActionType.GRAB, Arrays.asList(
                Pattern.compile("grab|grip|take")
        ));
    }};

    private final Map<EnvActionType, List<Pattern>> dict;

    public RegExpActionParser() {
        this(DEFAULT_DICT);
    }

    public RegExpActionParser(Map<EnvActionType, List<Pattern>> dict) {
        this.dict = dict;
    }

    @Override
    public EnvActionType parse(String action) {
        List<EnvActionType> result = new ArrayList<>();

        if (action != null && !action.isEmpty()) {
            final List<String> words = WumpusUtils.splitToWords(action);
            final List<String> allChunks = Stream.concat(words.stream(), Stream.of(action))
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
            action_loop:
            for (Map.Entry<EnvActionType, List<Pattern>> typePatterns : dict.entrySet()) {
                EnvActionType actionType = typePatterns.getKey();
                List<Pattern> patterns = typePatterns.getValue();
                for (Pattern pattern : patterns) {
                    for (String chunk : allChunks) {
                        if (pattern.matcher(chunk).matches()) {
                            result.add(actionType);
                            continue action_loop;
                        }
                    }
                }
            }
        }
        return result.isEmpty() ? null
                : result.get(ThreadLocalRandom.current().nextInt(0, result.size()));
    }
}
