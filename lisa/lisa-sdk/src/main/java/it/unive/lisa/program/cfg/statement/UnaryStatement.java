package it.unive.lisa.program.cfg.statement;

import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.StatementStore;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.lattices.ExpressionSet;
import it.unive.lisa.analysis.value.TypeDomain;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.interprocedural.InterproceduralAnalysis;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.statement.evaluation.EvaluationOrder;
import it.unive.lisa.program.cfg.statement.evaluation.LeftToRightEvaluation;
import it.unive.lisa.symbolic.SymbolicExpression;

/**
 * An {@link NaryStatement} with a single sub-expression.
 * 
 * @author <a href="mailto:luca.negrini@unive.it">Luca Negrini</a>
 */
public abstract class UnaryStatement extends NaryStatement {

	/**
	 * Builds the statement, happening at the given location in the program. The
	 * {@link EvaluationOrder} is {@link LeftToRightEvaluation}.
	 * 
	 * @param cfg           the cfg that this statement belongs to
	 * @param location      the location where the statement is defined within
	 *                          the program
	 * @param constructName the name of the construct represented by this
	 *                          statement
	 * @param subExpression the sub-expression of this statement
	 */
	protected UnaryStatement(CFG cfg, CodeLocation location, String constructName, Expression subExpression) {
		super(cfg, location, constructName, subExpression);
	}

	/**
	 * Builds the statement, happening at the given location in the program.
	 * 
	 * @param cfg           the cfg that this statement belongs to
	 * @param location      the location where the statement is defined within
	 *                          the program
	 * @param constructName the name of the construct represented by this
	 *                          statement
	 * @param order         the evaluation order of the sub-expressions
	 * @param subExpression the sub-expression of this statement
	 */
	protected UnaryStatement(CFG cfg, CodeLocation location, String constructName, EvaluationOrder order,
			Expression subExpression) {
		super(cfg, location, constructName, order, subExpression);
	}

	/**
	 * Yields the only sub-expression of this unary statement.
	 * 
	 * @return the only sub-expression
	 */
	public Expression getSubExpression() {
		return getSubExpressions()[0];
	}

	@Override
	public <A extends AbstractState<A, H, V, T>,
			H extends HeapDomain<H>,
			V extends ValueDomain<V>,
			T extends TypeDomain<T>> AnalysisState<A, H, V, T> statementSemantics(
					InterproceduralAnalysis<A, H, V, T> interprocedural,
					AnalysisState<A, H, V, T> state,
					ExpressionSet<SymbolicExpression>[] params,
					StatementStore<A, H, V, T> expressions)
					throws SemanticException {
		AnalysisState<A, H, V, T> result = state.bottom();
		for (SymbolicExpression expr : params[0])
			result = result.lub(unarySemantics(interprocedural, state, expr, expressions));
		return result;
	}

	/**
	 * Computes the semantics of the statement, after the semantics of the
	 * sub-expression has been computed. Meta variables from the sub-expression
	 * will be forgotten after this statement returns.
	 * 
	 * @param <A>             the type of {@link AbstractState}
	 * @param <H>             the type of the {@link HeapDomain}
	 * @param <V>             the type of the {@link ValueDomain}
	 * @param <T>             the type of {@link TypeDomain}
	 * @param interprocedural the interprocedural analysis of the program to
	 *                            analyze
	 * @param state           the state where the statement is to be evaluated
	 * @param expr            the symbolic expressions representing the computed
	 *                            value of the sub-expression of this expression
	 * @param expressions     the cache where analysis states of intermediate
	 *                            expressions are stored and that can be
	 *                            accessed to query for post-states of
	 *                            parameters expressions
	 * 
	 * @return the {@link AnalysisState} representing the abstract result of the
	 *             execution of this statement
	 * 
	 * @throws SemanticException if something goes wrong during the computation
	 */
	public abstract <A extends AbstractState<A, H, V, T>,
			H extends HeapDomain<H>,
			V extends ValueDomain<V>,
			T extends TypeDomain<T>> AnalysisState<A, H, V, T> unarySemantics(
					InterproceduralAnalysis<A, H, V, T> interprocedural,
					AnalysisState<A, H, V, T> state,
					SymbolicExpression expr,
					StatementStore<A, H, V, T> expressions)
					throws SemanticException;
}
