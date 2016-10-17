package ast;

import lombok.Value;

import java.util.*;

/**
 * Created by ooee on 9/26/16.
 */
@Value
public class LogicalOrExpressionImpl implements LogicalOrExpression {
    public static final String OR = "||";

    private Collection<LogicalAndExpression> logicalAndExpressions;

    @Override
    public String toCode() {
        List<String> codes = collectionToCode(logicalAndExpressions);
        return String.join(OR, codes);
    }

    @Override
    public Set<String> getChangedVariables() {
        return multiGetChangedVariables(logicalAndExpressions.toArray(new Expression[0]));
    }

    @Override
    public Set<String> getVariables() {
        Set<String> variables = new HashSet<>();
        for (LogicalAndExpression logicalAndExpression : logicalAndExpressions) {
            variables.addAll(logicalAndExpression.getVariables());
        }
        return variables;
    }

    @Override
    public Set<String> getDependentVariables() {
        return multiGetDependentVariables(logicalAndExpressions.toArray(new Expression[0]));
    }

    @Override
    public Set<PostfixExpressionInvocationImpl> getInvocations() {
        return multiGetInvocations(logicalAndExpressions.toArray(new Expression[0]));
    }
}
