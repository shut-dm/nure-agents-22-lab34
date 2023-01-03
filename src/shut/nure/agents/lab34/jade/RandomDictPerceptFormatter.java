package shut.nure.agents.lab34.jade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Random dictionary implementation of {@link PerceptFormatter}.
 */
public class RandomDictPerceptFormatter implements PerceptFormatter {

    private static final Map<EnvPerceptType, List<String>> DEFAULT_DICT = new HashMap<EnvPerceptType, List<String>>() {{
        put(EnvPerceptType.BREEZE, Arrays.asList(
                "I feel breeze here",
                "There is a breeze",                                    
                "It's a cool breeze here"
        ));
        put(EnvPerceptType.BUMP, Arrays.asList(
                "I bumped into something",
                "I bumped into a wall",
                "I hit the wall"
        ));
        put(EnvPerceptType.GLITTER, Arrays.asList(
                "I see glitter",
                "I see gold",
                "It seems something is glittering there"
        ));
        put(EnvPerceptType.SCREAM, Arrays.asList(
                "I hear terrible scream",
                "I hear someone is screaming",
                "What a horrible scream"
        ));
        put(EnvPerceptType.STENCH, Arrays.asList(
                "I feel stench",
                "Something stinks",
                "I can't feel this stink anymore"
        ));
    }};

    private final Map<EnvPerceptType, List<String>> dict;

    public RandomDictPerceptFormatter() {
        this(DEFAULT_DICT);
    }

    public RandomDictPerceptFormatter(Map<EnvPerceptType, List<String>> dict) {
        this.dict = dict;
    }

    @Override
    public String format(Set<EnvPerceptType> perceptTypes) {
        List<String> result = new ArrayList<>();
        if (perceptTypes != null && !perceptTypes.isEmpty()) {
            for (EnvPerceptType perceptType : perceptTypes) {
                final List<String> options = dict.get(perceptType);
                if (options != null && !options.isEmpty()) {
                    result.add(
                            options.get(
                                    ThreadLocalRandom.current().nextInt(0, options.size())
                            )
                    );
                }
            }
        }
        return String.join(". ", result);
    }
}
