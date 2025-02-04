package it.unive.lisa.interprocedural.context.recursion;

import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.StatementStore;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.lattices.ExpressionSet;
import it.unive.lisa.analysis.value.TypeDomain;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.conf.FixpointConfiguration;
import it.unive.lisa.interprocedural.InterproceduralAnalysisException;
import it.unive.lisa.interprocedural.OpenCallPolicy;
import it.unive.lisa.interprocedural.callgraph.CallGraph;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.program.Application;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.fixpoints.CFGFixpoint.CompoundState;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.call.CFGCall;
import it.unive.lisa.program.cfg.statement.call.Call;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.util.collections.workset.WorkingSet;
import it.unive.lisa.util.datastructures.graph.algorithms.FixpointException;

/**
 * A recursion solver that applies a single iteration of the recursion starting
 * from bottom and using top as entry state for the recursion. This is useful
 * for understanding what is the return value in the base cases of the
 * recursion: as the call that closes the recursion loop returns bottom, only
 * the returns from the base cases will affect the result.
 * 
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 * 
 * @param <A> the type of {@link AbstractState} contained into the analysis
 *                state
 * @param <H> the type of {@link HeapDomain} contained into the computed
 *                abstract state
 * @param <V> the type of {@link ValueDomain} contained into the computed
 *                abstract state
 * @param <T> the type of {@link TypeDomain} contained into the computed
 *                abstract state
 */
public class BaseCasesFinder<A extends AbstractState<A, H, V, T>,
		H extends HeapDomain<H>,
		V extends ValueDomain<V>,
		T extends TypeDomain<T>>
		extends ContextBasedAnalysis<A, H, V, T> {

	private final Recursion<A, H, V, T> recursion;

	private final boolean returnsVoid;

	/**
	 * Builds the solver.
	 * 
	 * @param backing     the analysis that backs this solver, and that can be
	 *                        used to query call results
	 * @param recursion   the recursion to solve
	 * @param returnsVoid whether or not the recursion returns void
	 */
	public BaseCasesFinder(
			ContextBasedAnalysis<A, H, V, T> backing,
			Recursion<A, H, V, T> recursion,
			boolean returnsVoid) {
		super(backing);
		this.recursion = recursion;
		this.returnsVoid = returnsVoid;
	}

	@Override
	public void init(
			Application app,
			CallGraph callgraph,
			OpenCallPolicy policy)
			throws InterproceduralAnalysisException {
		// we mark this as unsupported to make sure it never gets used as a root
		// analysis
		throw new UnsupportedOperationException();
	}

	@Override
	public void fixpoint(
			AnalysisState<A, H, V, T> entryState,
			Class<? extends WorkingSet<Statement>> fixpointWorkingSet,
			FixpointConfiguration conf)
			throws FixpointException {
		// we mark this as unsupported to make sure it never gets used as a root
		// analysis
		throw new UnsupportedOperationException();
	}

	@Override
	public AnalysisState<A, H, V, T> getAbstractResultOf(
			CFGCall call,
			AnalysisState<A, H, V, T> entryState,
			ExpressionSet<SymbolicExpression>[] parameters,
			StatementStore<A, H, V, T> expressions)
			throws SemanticException {
		boolean inRecursion = recursion.getMembers().contains(call.getCFG());
		if (inRecursion && call.getTargetedCFGs().contains(recursion.getRecursionHead())) {
			// this is a back call
			if (returnsVoid)
				return entryState.bottom();
			else
				return new AnalysisState<>(
						entryState.getState().bottom(),
						call.getMetaVariable(),
						entryState.getAliasing().bottom());
		}

		return super.getAbstractResultOf(call, entryState, parameters, expressions);
	}

	@Override
	protected boolean canShortcut(CFG cfg) {
		// we want to compute the recursive chain with no shortcuts
		return !recursion.getMembers().contains(cfg);
	}

	@Override
	protected boolean shouldCheckForRecursions() {
		return false;
	}

	@Override
	protected boolean shouldStoreFixpointResults() {
		return false;
	}

	/**
	 * Solves the recursion by iterating once starting from bottom.
	 * 
	 * @return the poststate for the call that starts the recursion
	 * 
	 * @throws SemanticException if an exception happens during the computation
	 */
	public AnalysisState<A, H, V, T> find() throws SemanticException {
		Call start = recursion.getInvocation();
		CompoundState<A, H, V, T> entryState = recursion.getEntryState();

		// we reset the analysis at the point where the starting call can be
		// evaluated
		token = recursion.getInvocationToken();
		Expression[] actuals = start.getParameters();
		@SuppressWarnings("unchecked")
		ExpressionSet<SymbolicExpression>[] params = new ExpressionSet[actuals.length];
		for (int i = 0; i < params.length; i++)
			params[i] = entryState.intermediateStates.getState(actuals[i]).getComputedExpressions();
		return start.expressionSemantics(this, entryState.postState.top(), params, entryState.intermediateStates);
	}
}
