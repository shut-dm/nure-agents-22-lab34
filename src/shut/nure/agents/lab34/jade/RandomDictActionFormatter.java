package shut.nure.agents.lab34.jade;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Random dictionary implementation of {@link ActionFormatter}.
 */
public class RandomDictActionFormatter implements ActionFormatter {

    private static final Map<EnvActionType, List<String>> DEFAULT_DICT = new HashMap<EnvActionType, List<String>>() {{
        put(EnvActionType.CLIMB, Arrays.asList(
                "Go climb!",
                "Climb up",
                "Climb away"
        ));
        put(EnvActionType.FORWARD, Arrays.asList(
                "Go forward",
                "Move forward",
                "Go straight"
        ));
        put(EnvActionType.TURN_LEFT, Arrays.asList(
                "Turn left",
                "go left",
                "Turn counterclockwise"
        ));
        put(EnvActionType.TURN_RIGHT, Arrays.asList(
                "turn right",
                "Go right",
                "Turn clockwise"
        ));
        put(EnvActionType.GRAB, Arrays.asList(
                "Grab it",
                "Go grip!",
                "Take what you see"
        ));
        put(EnvActionType.SHOOT, Arrays.asList(
                "Shoot!",
                "Shoot the arrow",
                "Use your bow!"
        ));
    }};

    private final Map<EnvActionType, List<String>> dict;

    public RandomDictActionFormatter() {
        this(DEFAULT_DICT);
    }

    public RandomDictActionFormatter(Map<EnvActionType, List<String>> dict) {
        this.dict = dict;
    }

    @Override
    public String format(EnvActionType actionType) {
        final List<String> actions = dict.get(actionType);
        if (actions != null && !actions.isEmpty()) {
            return actions.get(
                    ThreadLocalRandom.current().nextInt(0, actions.size())
            );
        }
        return null;
    }
}
