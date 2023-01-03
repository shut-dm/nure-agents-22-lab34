package shut.nure.agents.lab34.search.informed;

import java.util.function.ToDoubleFunction;

import shut.nure.agents.lab34.search.framework.Node;


/**
 * Search algorithms which make use of heuristics to guide the search
 * are expected to implement this interface.
 *
 * @author Ruediger Lunde
 */
public interface Informed<S, A> {
    void setHeuristicFunction(ToDoubleFunction<Node<S, A>> h);
}
