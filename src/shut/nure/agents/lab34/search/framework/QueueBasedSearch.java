package shut.nure.agents.lab34.search.framework;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;

import shut.nure.agents.lab34.search.framework.problem.Problem;
import shut.nure.agents.lab34.search.framework.qsearch.QueueSearch;

/**
 * Base class for all search algorithms which use a queue to manage not yet
 * explored nodes. Subclasses are responsible for node prioritization. They
 * define the concrete queue to be used as frontier in their constructor.
 * Search space exploration control is always delegated to some
 * <code>QueueSearch</code> implementation.
 *
 * @param <S> The type used to represent states
 * @param <A> The type of the actions to be used to navigate through the state space
 *
 * @author Ruediger Lunde
 */
public abstract class QueueBasedSearch<S, A> implements SearchForActions<S, A>, SearchForStates<S, A> {
	protected final QueueSearch<S, A> impl;
	private final Queue<Node<S, A>> frontier;

	protected QueueBasedSearch(QueueSearch<S, A> impl, Queue<Node<S, A>> queue) {
		this.impl = impl;
		this.frontier = queue;
	}

	@Override
	public Optional<List<A>> findActions(Problem<S, A> p) {
		impl.getNodeFactory().useParentLinks(true);
		frontier.clear();
		Optional<Node<S, A>> node = impl.findNode(p, frontier);
		return SearchUtils.toActions(node);
	}

	@Override
	public Optional<S> findState(Problem<S, A> p) {
		impl.getNodeFactory().useParentLinks(false);
		frontier.clear();
		Optional<Node<S, A>> node = impl.findNode(p, frontier);
		return SearchUtils.toState(node);
	}

	@Override
	public Metrics getMetrics() {
		return impl.getMetrics();
	}

	@Override
	public void addNodeListener(Consumer<Node<S, A>> listener)  {
		impl.getNodeFactory().addNodeListener(listener);
	}

	@Override
	public boolean removeNodeListener(Consumer<Node<S, A>> listener) {
		return impl.getNodeFactory().removeNodeListener(listener);
	}
}
