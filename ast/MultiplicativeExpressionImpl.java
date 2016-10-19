package ast;

import lombok.Value;

import java.util.Set;

/**
 * Created by ooee on 9/26/16.
 */
@Value
public class MultiplicativeExpressionImpl implements MultiplicativeExpression {
    public enum MultiplicativeOperator {
        MULTIPLY("*"),
        DIVIDE("/"),
        MODULO("%");

        public final String token;

        MultiplicativeOperator(String token) {
            this.token = token;
        }

        public static MultiplicativeOperator toMultiplicativeOperator(String token) {
            for (MultiplicativeOperator multiplicativeOperator : MultiplicativeOperator.values()) {
                if (multiplicativeOperator.token.equals(token)) {
                    return multiplicativeOperator;
                }
            }
            return null;
        }
    }

    private MultiplicativeExpression multiplicativeExpression;
    private MultiplicativeOperator multiplicativeOperator;
    private CastExpression castExpression;

    @Override
    public String toCode() {
        return multiplicativeExpression.toCode() + multiplicativeOperator.token + castExpression.toCode();
    }

    @Override
    public Set<String> getGuaranteedChangedVariables() {
        return multiGetGuaranteedChangedVariables(multiplicativeExpression, castExpression);
    }

    @Override
    public Set<String> getDependentVariables() {
        return multiGetDependentVariables(multiplicativeExpression, castExpression);
    }

    @Override
    public Set<PostfixExpressionInvocationImpl> getInvocations() {
        return multiGetInvocations(multiplicativeExpression, castExpression);
    }

    @Override
    public Set<String> getVariables() {
        Set<String> variables = multiplicativeExpression.getVariables();
        variables.addAll(castExpression.getVariables());
        return variables;
    }
}
