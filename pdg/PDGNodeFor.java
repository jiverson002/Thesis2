package pdg;

import ast.Expression;
import ast.IterationStatementFor;
import ast.IterationStatementWhile;
import ast.Statement;
import lombok.Value;

/**
 * Created by ooee on 10/9/16.
 */
@Value
public class PDGNodeFor extends PDGNode<IterationStatementFor> {
    private PDGNode<? extends Statement> body;

    public PDGNodeFor(IterationStatementFor statement, PDGNode<? extends Statement> body) {
        this(statement, body, false);
    }

    public PDGNodeFor(IterationStatementFor statement, PDGNode<? extends Statement> body, boolean required) {
        super(statement, required);
        this.body = body;
    }

    @Override
    public IterationStatementFor sort(PDGSorter sorter) {
        Expression condition = blockItem.getCondition();
        Statement statement = body.sort(sorter);
        return new IterationStatementFor(blockItem.getInitial(), blockItem.getCondition(), blockItem.getIteration(), statement);
    }

    public String toCode(PDGSorterDefault sorter) {
        return "";
    }
}