package ast;

import lombok.Value;

/**
 * Created by kly04 on 9/26/16.
 */
@Value
public class PrimaryExpressionParentheses implements PrimaryExpression {
    private Expression expression;

    @Override
    public String toCode() {
        return "(" + expression.toCode() + " )";
    }
}
