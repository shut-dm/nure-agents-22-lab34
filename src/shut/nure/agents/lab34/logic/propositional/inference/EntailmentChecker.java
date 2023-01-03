package shut.nure.agents.lab34.logic.propositional.inference;

import shut.nure.agents.lab34.logic.propositional.kb.KnowledgeBase;
import shut.nure.agents.lab34.logic.propositional.parsing.ast.Sentence;

public interface EntailmentChecker {

    /**
     * Determine if KB |= &alpha;, i.e. alpha is entailed by KB.
     *
     * @param kb
     *            a Knowledge Base in propositional logic.
     * @param alpha
     *            a propositional sentence.
     * @return true, if &alpha; is entailed by KB, false otherwise.
     */
    boolean isEntailed(KnowledgeBase kb, Sentence alpha);
}
