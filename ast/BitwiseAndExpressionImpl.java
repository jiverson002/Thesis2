package ast;

import lombok.Value;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ooee on 9/26/16.
 */
@Value
public class BitwiseAndExpressionImpl implements BitwiseAndExpression {

    public static final String BITWISE_AND = "&";

    private Collection<EqualityExpression> equalityExpressions;

    @Override
    public String toCode() {
        List<String> equalityCodes = equalityExpressions.stream().map(exp -> exp.toCode()).collect(Collectors.toList());
        return String.join(BITWISE_AND, equalityCodes);
    }
/*
    @Override
    public Set<String> getChangedVariables() {
        return Collections.emptySet();
    }
*/
    @Override
    public Set<String> getVariables() {
        Set<String> variables = new HashSet<>();
        for (EqualityExpression equalityExpression : equalityExpressions) {
            variables.addAll(equalityExpression.getVariables());
        }
        return variables;
    }

    @Override
    public Set<String> getDependentVariables() {
        Set<String> dependentVariables = new HashSet<>();
        for (EqualityExpression equalityExpression : equalityExpressions) {
            dependentVariables.addAll(equalityExpression.getDependentVariables());
        }
        return dependentVariables;
    }

    @Override
    public Set<String> getChangedVariables() {
        Set<String> changedVariables = new HashSet<>();
        for (EqualityExpression equalityExpression : equalityExpressions) {
            changedVariables.addAll(equalityExpression.getChangedVariables());
        }
        return changedVariables;
    }

    @Override
    public Set<PostfixExpressionInvocationImpl> getInvocations() {
        Set<PostfixExpressionInvocationImpl> invocations = new HashSet<>();
        for (EqualityExpression equalityExpression : equalityExpressions) {
            invocations.addAll(equalityExpression.getInvocations());
        }
        return invocations;
    }
}
