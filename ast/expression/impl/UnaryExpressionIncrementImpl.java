package ast.expression.impl;

import ast.expression.Expression;
import ast.expression.UnaryExpression;
import ast.expression.impl.PostfixExpressionInvocationImpl;
import lombok.Value;
import visitor.Visitor;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ooee on 9/26/16.
 */
@Value
public class UnaryExpressionIncrementImpl implements UnaryExpression {
    @Override
    public Set<String> getDependentVariables() {
        Set<String> dependentVariables = new HashSet<>();
        dependentVariables.addAll(unaryExpression.getDependentVariables());
        dependentVariables.addAll(unaryExpression.getVariables());
        return dependentVariables;
    }

    @Override
    public Set<String> getGuaranteedChangedVariables() {
        Set<String> changedVariables = new HashSet<>();
        changedVariables.addAll(unaryExpression.getGuaranteedChangedVariables());
        changedVariables.addAll(unaryExpression.getVariables());
        return changedVariables;
    }

    public enum IncrementOperator {
        PREFIX_INCREMENT("++"),
        PREFIX_DECREMENT("--");

        private final String token;

        IncrementOperator(String token) {
            this.token = token;
        }
    }

    private IncrementOperator incrementOperator;
    private UnaryExpression unaryExpression;

    @Override
    public String toCode() {
        return incrementOperator.token + unaryExpression.toCode();
    }

    @Override
    public Set<String> getVariables() {
        return unaryExpression.getVariables();
    }

    @Override
    public Set<PostfixExpressionInvocationImpl> getInvocations() {
        return unaryExpression.getInvocations();
    }

    @Override
    public void visitNestedExpressions(Visitor<Expression> visitor) {
        visitor.visit(unaryExpression);
    }
}
