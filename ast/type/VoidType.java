package ast.type;

/**
 * Created by ooee on 9/24/16.
 */
public class VoidType implements Type {
    @Override
    public String toCode() {
        return "void";
    }

    @Override
    public String toExpandedCode() {
        return toCode();
    }
}
