package ast;

import ast.type.Type;
import lombok.Value;

/**
 * Created by ooee on 9/26/16.
 */
@Value
public class UnaryExpressionUnaryOperatorImpl implements UnaryExpression {
    public enum UnaryOperator {
        ADDRESS_OF("&"),
        DEREFERENCE("*"),
        POSITIVE("+"),
        NEGATIVE("-"),
        BITWISE_NOT("~"),
        NOT("!");

        public final String token;

        UnaryOperator(String token) {
            this.token = token;
        }

        public static UnaryOperator toUnaryOperator(String token) {
            for (UnaryOperator unaryOperator : UnaryOperator.values()) {
                if (unaryOperator.token.equals(token)) {
                    return unaryOperator;
                }
            }
            return null;
        }
    }

    private UnaryOperator unaryOperator;
    private CastExpression castExpression;

    @Override
    public String toCode() {
        return unaryOperator.token + castExpression.toCode();
    }
}
