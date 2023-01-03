package shut.nure.agents.lab34.jade;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import shut.nure.agents.lab34.wumpusworld.WumpusAction;

import static java.util.stream.Collectors.*;

/**
 * Environment agent action types.
 */
public enum EnvActionType {
    RENT_CAVE("rent-cave", null),
    FREE_CAVE("free-cave", null),
    TURN_LEFT("turn-left", WumpusAction.TURN_LEFT),
    TURN_RIGHT("turn-right", WumpusAction.TURN_RIGHT),
    FORWARD("forward", WumpusAction.FORWARD),
    SHOOT("shoot", WumpusAction.SHOOT),
    GRAB("grab", WumpusAction.GRAB),
    CLIMB("climb", WumpusAction.CLIMB);
    
    private static final Map<String, EnvActionType> NAME_TO_ENUM_IDX = Arrays.stream(EnvActionType.values())
        .collect(toMap(
                EnvActionType::getName,
                Function.identity()
        ));
    private static final Map<WumpusAction, EnvActionType> WUMPUS_TO_ENUM_IDX = Arrays.stream(EnvActionType.values())
            .filter(t -> t.getWumpusAction() != null)
            .collect(toMap(
                    EnvActionType::getWumpusAction,
                    Function.identity()
            ));

    private final String name;

    private final WumpusAction wumpusAction;

    EnvActionType(String name, WumpusAction wumpusAction) {
        this.name = name;
        this.wumpusAction = wumpusAction;
    }

    public String getName() {
        return name;
    }

    public WumpusAction getWumpusAction() {
        return wumpusAction;
    }

    public static EnvActionType findByName(String name) {
        EnvActionType result = NAME_TO_ENUM_IDX.get(name);
        if (result != null) {
            return result;
        }
        throw new IllegalArgumentException(
                "No matching environment action found for name of [" + name + "]!"
        );
    }

    public static EnvActionType findByWumpusAction(WumpusAction wumpusAction) {
        EnvActionType result = WUMPUS_TO_ENUM_IDX.get(wumpusAction);
        if (result != null) {
            return result;
        }
        throw new IllegalArgumentException(
                "No matching environment action found for Wumpus action of [" + wumpusAction + "]!"
        );
    }
}
