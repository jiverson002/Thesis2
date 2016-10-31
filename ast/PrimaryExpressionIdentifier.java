package ast;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Data;
import visitor.Visitor;

import java.util.Collections;
import java.util.Set;

/**
 * Created by kly04 on 9/26/16.
 */
@Data
@AllArgsConstructor
public class PrimaryExpressionIdentifier implements PrimaryExpression {
    private String identifier;

    public String toCode() {
        return identifier;
    }

    @Override
    public Set<String> getVariables() {
        return Sets.newHashSet(identifier);
    }

    @Override
    public Set<String> getDependentVariables() {
        return Sets.newHashSet(identifier);
    }

    @Override
    public Set<String> getGuaranteedChangedVariables() {
        return Sets.newHashSet();
    }

    @Override
    public Set<PostfixExpressionInvocationImpl> getInvocations() {
        return Collections.emptySet();
    }

    @Override
    public void visitNestedExpressions(Visitor<Expression> visitor) {
        return;
    }
}
