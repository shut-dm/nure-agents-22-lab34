package shut.nure.agents.lab34.logic.propositional.transformations;

import java.util.Set;

import shut.nure.agents.lab34.logic.propositional.parsing.PLVisitor;
import shut.nure.agents.lab34.logic.propositional.parsing.ast.ComplexSentence;
import shut.nure.agents.lab34.logic.propositional.parsing.ast.PropositionSymbol;
import shut.nure.agents.lab34.util.SetOps;

/**
 * Super class of Visitors that are "read only" and gather information from an
 * existing parse tree .
 * 
 * @author Ravi Mohan
 * 
 * @param <T>
 *            the type of elements to be gathered.
 */
public abstract class BasicGatherer<T> implements PLVisitor<Set<T>, Set<T>> {

	@Override
	public Set<T> visitPropositionSymbol(PropositionSymbol s, Set<T> arg) {
		return arg;
	}

	@Override
	public Set<T> visitUnarySentence(ComplexSentence s, Set<T> arg) {
		return SetOps.union(arg, s.getSimplerSentence(0).accept(this, arg));
	}

	@Override
	public Set<T> visitBinarySentence(ComplexSentence s, Set<T> arg) {
		Set<T> termunion = SetOps.union(
				s.getSimplerSentence(0).accept(this, arg),
				s.getSimplerSentence(1).accept(this, arg));
		return SetOps.union(arg, termunion);
	}
}
