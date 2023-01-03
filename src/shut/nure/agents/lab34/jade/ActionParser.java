package shut.nure.agents.lab34.jade;

/**
 * Parses environment actions expressed in natural language into constants.
 */
public interface ActionParser {

    EnvActionType parse(String action);

}
