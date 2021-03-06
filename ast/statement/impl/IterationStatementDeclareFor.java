package ast.statement.impl;

import ast.declaration.VariableDeclaration;
import ast.expression.Expression;
import ast.statement.CanContainStatements;
import ast.statement.IterationStatement;
import ast.statement.Statement;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import visitor.Visitor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ooee on 9/29/16.
 */
@Data
@AllArgsConstructor
public class IterationStatementDeclareFor implements IterationStatement, CanContainStatements {
    @NonNull private VariableDeclaration variableDeclaration;
    private Expression condition;
    private Expression iteration;
    @NonNull private Statement statement;
    @NonNull private String commentTip;

    public IterationStatementDeclareFor(VariableDeclaration variableDeclaration, Expression condition, Expression iteration, Statement statement) {
        this(variableDeclaration, condition, iteration, statement, toCommentTip(variableDeclaration, condition, iteration));
    }

    private static String toCommentTip(VariableDeclaration variableDeclaration, Expression condition, Expression iteration) {
        String conditionString = "", iterationString = "";
        if (condition != null) {
            conditionString = condition.toCode();
        }
        if (iteration != null) {
            iterationString = iteration.toCode();
        }
        return String.format("for (%s %s; %s)", variableDeclaration.toCode(false), conditionString, iterationString);
    }

    @Override
    public String toCode(boolean showOriginalLine) {
        String conditionString = "", iterationString = "";
        if (condition != null) {
            conditionString = condition.toCode();
        }
        if (iteration != null) {
            iterationString = iteration.toCode();
        }
        return String.format("for (%s %s; %s) %s", variableDeclaration.toCode(false), conditionString, iterationString, statement.toCode(showOriginalLine));
    }

    @Override
    public boolean hasJump() {
        return statement.hasJump();
    }

    @Override
    public Set<String> getDependantVariables() {
        Set<String> dependantVariables = new HashSet<>();
        for (VariableDeclaration.DeclaredVariable declaredVariable : variableDeclaration.getDeclaredVariables()) {
            if (declaredVariable.getInitializer() != null) {
                dependantVariables.addAll(declaredVariable.getInitializer().getVariables());
            }
        }
        if (condition != null) {
            dependantVariables.addAll(condition.getVariables());
        }
        if (iteration != null) {
            dependantVariables.addAll(iteration.getVariables());
        }
        dependantVariables.addAll(statement.getDependantVariables());
        return dependantVariables;
    }

    @Override
    public Collection<Statement> getStatementNodes() {
        return Lists.newArrayList(statement);
    }

    @Override
    public Set<String> getGuaranteedChangedVariables() {
        Set<String> guaranteedChangedVariables = new HashSet<>();
        for (VariableDeclaration.DeclaredVariable declaredVariable : variableDeclaration.getDeclaredVariables()) {
            if (declaredVariable.getInitializer() != null) {
                guaranteedChangedVariables.addAll(declaredVariable.getInitializer().getGuaranteedChangedVariables());
            }
        }
        if (condition != null) {
            guaranteedChangedVariables.addAll(condition.getGuaranteedChangedVariables());
        }
        return guaranteedChangedVariables;
    }

    @Override
    public Set<String> getPotentiallyChangedVariables() {
        Set<String> potentiallyChangedVariables = new HashSet<>();
        for (VariableDeclaration.DeclaredVariable declaredVariable : variableDeclaration.getDeclaredVariables()) {
            if (declaredVariable.getInitializer() != null) {
                potentiallyChangedVariables.addAll(declaredVariable.getInitializer().getPotentiallyChangedVariables());
            }
        }
        if (condition != null) {
            potentiallyChangedVariables.addAll(condition.getPotentiallyChangedVariables());
        }
        potentiallyChangedVariables.addAll(statement.getPotentiallyChangedVariables());
        if (iteration != null) {
            potentiallyChangedVariables.addAll(iteration.getPotentiallyChangedVariables());
        }
        return potentiallyChangedVariables;
    }

    @Override
    public boolean isCritical() {
        return true;
    }

    @Override
    public void visitEachStatement(Visitor<Statement> visitor) {
        visitor.visit(statement);
    }

    @Override
    public void visitOwnedExpressions(Visitor<Expression> visitor) {
        for (VariableDeclaration.DeclaredVariable declaredVariable : variableDeclaration.getDeclaredVariables()) {
            if (declaredVariable.getInitializer() != null) {
                visitor.visit(declaredVariable.getInitializer());
            }
        }
        if (condition != null) {
            visitor.visit(condition);
        }
        if (iteration != null) {
            visitor.visit(iteration);
        }
    }

    @Override
    public void visitAllExpressions(Visitor<Expression> visitor) {
        visitOwnedExpressions(visitor);
        statement.visitOwnedExpressions(visitor);
    }
}
