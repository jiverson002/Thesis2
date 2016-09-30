package ast;

import lombok.Value;

import java.util.Collection;

/**
 * Created by ooee on 9/26/16.
 */
@Value
public class AdditiveExpressionImplTree implements AdditiveExpression {
    private AdditiveExpression additiveExpression;
    private AdditiveOperator additiveOperator;
    private MultiplicativeExpression multiplicativeExpression;
}