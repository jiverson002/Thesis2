package ast;

import java.util.Collection;
import java.util.Set;

/**
 * Created by ooee on 9/25/16.
 */
public interface Expression extends BaseElement {
    Set<String> getLValues();

    Set<String> getVariables();
}
