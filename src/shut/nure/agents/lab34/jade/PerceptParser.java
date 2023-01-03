package shut.nure.agents.lab34.jade;

import java.util.Set;

/**
 * Parses speleologist percepts expressed in natural language into constants.
 */
public interface PerceptParser {

    Set<EnvPerceptType> parse(String percepts);

}
