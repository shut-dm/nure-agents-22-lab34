package shut.nure.agents.lab34.jade;

import java.util.Set;

/**
 * Express percepts in natural language.
 */
public interface PerceptFormatter {

    String format(Set<EnvPerceptType> perceptTypes);

}
