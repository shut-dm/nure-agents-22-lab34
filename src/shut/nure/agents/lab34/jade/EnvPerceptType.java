package shut.nure.agents.lab34.jade;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import shut.nure.agents.lab34.wumpusworld.WumpusPercept;

import static java.util.stream.Collectors.*;

/**
 * Environment request types.
 */
public enum EnvPerceptType {
    STENCH("stench"),
    BREEZE("breeze"),
    GLITTER("glitter"),
    SCREAM("scream"),
    BUMP("bump");
    private static final Map<String, EnvPerceptType> NAME_TO_ENUM_IDX = Arrays.stream(EnvPerceptType.values())
            .collect(toMap(
                    EnvPerceptType::getName,
                    Function.identity()
            ));

    private final String name;

    EnvPerceptType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static EnvPerceptType findByName(String name) {
        EnvPerceptType result = NAME_TO_ENUM_IDX.get(name);
        if (result != null) {
            return result;
        }
        throw new IllegalArgumentException(
                "No matching environment percept type found for name of [" + name + "]!"
        );
    }

    public static EnumSet<EnvPerceptType> fromWumpusPercept(WumpusPercept percept) {
        Set<EnvPerceptType> result = new HashSet<>();
        if (percept.isGlitter()) {
            result.add(GLITTER);
        }
        if (percept.isBreeze()) {
            result.add(BREEZE);
        }
        if (percept.isBump()) {
            result.add(BUMP);
        }
        if (percept.isScream()) {
            result.add(SCREAM);
        }
        if (percept.isStench()) {
            result.add(STENCH);
        }
        return result.isEmpty() ? EnumSet.noneOf(EnvPerceptType.class) : EnumSet.copyOf(result);
    }

    public static WumpusPercept toWumpusPercept(Set<EnvPerceptType> percepts) {
        WumpusPercept result = new WumpusPercept();
        if (percepts.contains(BREEZE)) {
            result.setBreeze();
        }
        if (percepts.contains(GLITTER)) {
            result.setGlitter();
        }
        if (percepts.contains(STENCH)) {
            result.setStench();
        }
        if (percepts.contains(SCREAM)) {
            result.setScream();
        }
        if (percepts.contains(BUMP)) {
            result.setBump();
        }
        return result;
    }
}
