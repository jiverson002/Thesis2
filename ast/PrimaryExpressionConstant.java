package ast;

import lombok.Value;

import java.util.Collections;
import java.util.Set;

/**
 * Created by kly04 on 9/26/16.
 */
@Value
public class PrimaryExpressionConstant implements PrimaryExpression {
    private Object value;

    public PrimaryExpressionConstant(long l) {
        value = l;
    }

    public PrimaryExpressionConstant(double d) {
        value = d;
    }

    public PrimaryExpressionConstant(char c) {
        value = c;
    }

    public PrimaryExpressionConstant(String s) {
        value = s;
    }

    @Override
    public Set<String> getLValues() {
        return Collections.emptySet();
    }

    public String toCode() {
        return value.toString();
    }

    @Override
    public Set<String> getVariables() {
        return Collections.emptySet();
    }
}