package visitor;

import ast.*;
import ast.declaration.Declaration;
import ast.declaration.TypedefDeclaration;
import ast.declaration.VariableDeclaration;
import ast.expression.Expression;
import ast.statement.Statement;
import ast.statement.impl.*;
import ast.type.PointerType;
import ast.type.Type;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import pdg.*;

import java.util.*;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class PDGGenerationVisitor {

    public static final boolean TREAT_UNKNOWN_FUNCTIONS_AS_ORDER_DEPENDENT = true; // Versus variable dependent

    @Value
    private class Returns<T extends BlockItem> {
        Dependencies dependencies;
        PDGNode<T> pdgNode;
    }

    public void printAdjacencyMatrix(Map<String, PDGNode> allNodes) {
        int i = 0;
        String label;
        System.out.println("Adjacency Matrix");
        Map<PDGNode, String> labels = new IdentityHashMap<>();
        for (PDGNode pdgNode : allNodes.values()) {
            label = String.valueOf(Character.valueOf((char) ('A' + i)));
            labels.put(pdgNode, label);
            System.out.println(label + " := " + pdgNode.getBlockItem().toCode(false));
            i++;
        }

        for (PDGNode pdgNode : allNodes.values()) {
            String source = labels.get(pdgNode);
            System.out.print(source + ": ");
            List<String> destLabels = (List<String>)pdgNode.getIsADependencyFor().stream().map(node -> labels.get(node)).collect(Collectors.toList());
            System.out.println(String.join(", ", destLabels));
        }
        System.out.println("End Matrix");
    }


    @Value
    @AllArgsConstructor
    private class Dependencies {
        private Set<String> dependentVariables;
        private Set<String> guaranteedChangedVariables;
        private Set<String> potentiallyChangedVariables;

        Dependencies() {
            dependentVariables = Sets.newHashSet();
            guaranteedChangedVariables = Sets.newHashSet();
            potentiallyChangedVariables = Sets.newHashSet();
        }

        Dependencies add(Dependencies d) {
            Set<String> otherDependentVariables = new HashSet<>(d.dependentVariables);
            otherDependentVariables.removeAll(this.guaranteedChangedVariables);
            this.dependentVariables.addAll(otherDependentVariables);
            this.potentiallyChangedVariables.removeAll(d.guaranteedChangedVariables);
            this.guaranteedChangedVariables.removeAll(d.potentiallyChangedVariables);
            this.guaranteedChangedVariables.addAll(d.guaranteedChangedVariables);
            this.potentiallyChangedVariables.addAll(d.potentiallyChangedVariables);
            return this;
        }
    }

    private Dependencies nullableExpression(Expression expression, Dependencies dependencies) {
        if (expression != null) {
            Set<String> rightVariables = expression.getDependentVariables();
            rightVariables.removeAll(dependencies.guaranteedChangedVariables);
            dependencies.dependentVariables.addAll(rightVariables);
            dependencies.guaranteedChangedVariables.removeAll(expression.getPotentiallyChangedVariables());
            dependencies.potentiallyChangedVariables.removeAll(expression.getGuaranteedChangedVariables());
            dependencies.guaranteedChangedVariables.addAll(expression.getGuaranteedChangedVariables());
            dependencies.potentiallyChangedVariables.addAll(expression.getPotentiallyChangedVariables());
        }
        return dependencies;
    }

    private Set<String> globalVariables;
    private Program program;
    private ScopeVisitor scopeVisitor;
    private Map<BlockItem, Map<String, Type>> identityTypeMapper;
    public PDGGenerationVisitor(Program p) {
        program = p;
        globalVariables = new HashSet<>();
        scopeVisitor = new ScopeVisitor(p);
        System.err.print("Gathering global variables...");
        for (Declaration declaration : program.getDeclarations()) {
            if (declaration instanceof VariableDeclaration) {
                for (VariableDeclaration.DeclaredVariable declaredVariable : ((VariableDeclaration) declaration).getDeclaredVariables()) {
                    globalVariables.add(declaredVariable.getIdentifier().getIdentifier());
                }
            }
        }
        System.err.println("Done");
        identityTypeMapper = null;
    }

    public PDGNodeCompoundStatement visit(Function function) {
        identityTypeMapper = scopeVisitor.process(function);
        Returns<CompoundStatement> returns = visit(function.getCompoundStatement());
        Set<String> changedVariables = new HashSet<>();
        changedVariables.addAll(returns.getDependencies().getGuaranteedChangedVariables());
        changedVariables.addAll(returns.getDependencies().getPotentiallyChangedVariables());
        for (Parameter parameter : function.getParameterList().getParameters()) {
            // If the type is a pointer, mark it as changed.
            if (parameter.getType() instanceof PointerType && changedVariables.contains(parameter.getFormalParameterName())) {
                // Pointer value changed

            }
        }
        //functionDependencies.getGuaranteedChangedVariables()
        return ((PDGNodeCompoundStatement) returns.pdgNode);
    }

    public Returns<JumpReturnStatement> visit(JumpReturnStatement statement) {
        if (statement.getReturnExpression() != null) {
            return new Returns<>(visit(statement.getReturnExpression()), new PDGNodeReturn(statement, true));
        } else {
            return new Returns<>(new Dependencies(), new PDGNodeReturn(statement, true));
        }
    }

    public Returns<JumpBreakStatement> visit(JumpBreakStatement statement) {
        return new Returns<>(new Dependencies(), new PDGNodeBreak(statement, true));
    }

    public Returns<JumpContinueStatement> visit(JumpContinueStatement statement) {
        return new Returns<>(new Dependencies(), new PDGNodeContinue(statement, true));
    }

    public Returns<? extends Statement> visit(Statement statement) {
        if (statement instanceof CompoundStatement) {
            return visit(((CompoundStatement) statement));
        } else if (statement instanceof ExpressionStatement) {
            return visit(((ExpressionStatement) statement));
        } else if (statement instanceof SelectionStatementIf) {
            return visit(((SelectionStatementIf) statement));
        } else if (statement instanceof IterationStatementFor) {
            return visit(((IterationStatementFor) statement));
        } else if (statement instanceof IterationStatementDeclareFor) {
            return visit(((IterationStatementDeclareFor) statement));
        } else if (statement instanceof IterationStatementWhile) {
            return visit(((IterationStatementWhile) statement));
        } else if (statement instanceof IterationStatementDoWhile) {
            return visit(((IterationStatementDoWhile) statement));
        } else if (statement instanceof JumpReturnStatement) {
            return visit(((JumpReturnStatement) statement));
        } else if (statement instanceof JumpBreakStatement) {
            return visit(((JumpBreakStatement) statement));
        } else if (statement instanceof JumpContinueStatement) {
            return visit(((JumpContinueStatement) statement));
        } else if (statement instanceof SelectionStatementSwitch) {
            return visit(((SelectionStatementSwitch) statement));
        } else if (statement instanceof LabeledCaseStatement) {
            return visit(((LabeledCaseStatement) statement));
        } else if (statement instanceof LabeledDefaultStatement) {
            return visit(((LabeledDefaultStatement) statement));
        } else {
            throw new IllegalArgumentException("What kind of statement is this? " + statement);
        }
    }

    private Returns<LabeledCaseStatement> visit(LabeledCaseStatement statement) {
        Returns<? extends Statement> visit = visit(statement.getStatement());
        return new Returns<>(visit.getDependencies(), new PDGNodeLabeledCase(statement));
    }

    private Returns<LabeledDefaultStatement> visit(LabeledDefaultStatement statement) {
        return new Returns<>(visit(statement.getStatement()).getDependencies(), new PDGNodeDefaultCase(statement));
    }

    private Returns<SelectionStatementSwitch> visit(SelectionStatementSwitch statement) {
        List<List<PDGNode<? extends BlockItem>>> cases = new ArrayList<>();
        for (List<BlockItem> blockItems : statement.getCases()) {
            List<PDGNode<? extends BlockItem>> caseBlock = new ArrayList<>();
            for (BlockItem blockItem : blockItems) {
                caseBlock.add(visit(blockItem).getPdgNode());
            }
            cases.add(caseBlock);
        }
        if (statement.hasDefault()) {
            List<PDGNode<? extends BlockItem>> defaultCase = new ArrayList<>();
            for (BlockItem blockItem : statement.getDefaultCase()) {
                defaultCase.add(visit(blockItem).getPdgNode());
            }
            return new Returns<>(new Dependencies(statement.getDependantVariables(), statement.getGuaranteedChangedVariables(), statement.getPotentiallyChangedVariables()), new PDGNodeSwitch(statement, cases, defaultCase, false));
        } else {
            return new Returns<>(new Dependencies(statement.getDependantVariables(), statement.getGuaranteedChangedVariables(), statement.getPotentiallyChangedVariables()), new PDGNodeSwitch(statement, cases));
        }
    }

    private Returns<? extends BlockItem> visit(BlockItem blockItem) {
        if (blockItem instanceof VariableDeclaration) {
            return visit(((VariableDeclaration) blockItem));
        } else if (blockItem instanceof TypedefDeclaration) {
            TypedefDeclaration typedefDeclaration = (TypedefDeclaration) blockItem;
            return new Returns<>(new Dependencies(), new PDGNodeTypedef(typedefDeclaration));
            //throw new IllegalArgumentException("Typedefs should not be in a function");
        } else {
            if (blockItem instanceof Statement) {
                return visit((Statement) blockItem);
            } else {
                return new Returns<>(new Dependencies(), new PDGNode<BlockItem>(blockItem) {
                    @Override
                    public BlockItem sort(PDGSorter sorter) {
                        return blockItem;
                    }
                });
            }
        }
    }

    private Returns<IterationStatementFor> visit(IterationStatementFor statement) {
        Dependencies dependencies = new Dependencies();
        nullableExpression(statement.getInitial(), dependencies);
        nullableExpression(statement.getCondition(), dependencies);
        Returns<? extends Statement> returns = visit(statement.getStatement());
        Dependencies bodyDependencies = returns.dependencies;
        bodyDependencies.dependentVariables.removeAll(dependencies.getPotentiallyChangedVariables());
        dependencies.potentiallyChangedVariables.addAll(bodyDependencies.getGuaranteedChangedVariables());
        dependencies.potentiallyChangedVariables.addAll(bodyDependencies.getPotentiallyChangedVariables());
        dependencies.dependentVariables.addAll(bodyDependencies.getDependentVariables());

        Dependencies iterationDependencies = nullableExpression(statement.getIteration(), new Dependencies());
        dependencies.dependentVariables.addAll(iterationDependencies.getDependentVariables());
        dependencies.potentiallyChangedVariables.addAll(iterationDependencies.getGuaranteedChangedVariables());
        dependencies.potentiallyChangedVariables.addAll(iterationDependencies.getPotentiallyChangedVariables());

        // Brings dependencies back to the top of the loop
        if (returns.getPdgNode() instanceof PDGNodeCompoundStatement) {
            new PDGUselessCodeRemover().propagateRequiredCompoundStatement(dependencies.dependentVariables, ((PDGNodeCompoundStatement) returns.getPdgNode()));
        }
        return new Returns<>(dependencies, new PDGNodeFor(statement, returns.getPdgNode()));
    }

    private Returns<IterationStatementDeclareFor> visit(IterationStatementDeclareFor statement) {
        Set<String> declaredVariables = new HashSet<>();
        Dependencies dependencies = new Dependencies();
        for (VariableDeclaration.DeclaredVariable declaredVariable : statement.getVariableDeclaration().getDeclaredVariables()) {
            if (declaredVariable.getInitializer() != null) {
                nullableExpression(declaredVariable.getInitializer(), dependencies);
                declaredVariables.add(declaredVariable.getIdentifier().getIdentifier());
            }
        }
        nullableExpression(statement.getCondition(), dependencies);
        Returns<? extends Statement> returns = visit(statement.getStatement());
        Dependencies bodyDependencies = returns.dependencies;

        bodyDependencies.dependentVariables.removeAll(dependencies.getPotentiallyChangedVariables());
        dependencies.potentiallyChangedVariables.addAll(bodyDependencies.getGuaranteedChangedVariables());
        dependencies.potentiallyChangedVariables.addAll(bodyDependencies.getPotentiallyChangedVariables());
        dependencies.dependentVariables.addAll(bodyDependencies.getDependentVariables());

        Dependencies iterationDependencies = nullableExpression(statement.getIteration(), new Dependencies());
        dependencies.dependentVariables.addAll(iterationDependencies.getDependentVariables());
        dependencies.potentiallyChangedVariables.addAll(iterationDependencies.getGuaranteedChangedVariables());
        dependencies.potentiallyChangedVariables.addAll(iterationDependencies.getPotentiallyChangedVariables());

        dependencies.dependentVariables.removeAll(declaredVariables);
        dependencies.potentiallyChangedVariables.removeAll(declaredVariables);
        dependencies.guaranteedChangedVariables.removeAll(declaredVariables);

        // Brings dependencies back to the top of the loop
        if (returns.getPdgNode() instanceof PDGNodeCompoundStatement) {
            new PDGUselessCodeRemover().propagateRequiredCompoundStatement(dependencies.dependentVariables, ((PDGNodeCompoundStatement) returns.getPdgNode()));
        }
        return new Returns<>(dependencies, new PDGNodeDeclareFor(statement, returns.pdgNode));
    }

    private Returns<IterationStatementWhile> visit(IterationStatementWhile statement) {
        Dependencies dependencies = visit(statement.getCondition());
        Returns<? extends Statement> returns = visit(statement.getStatement());
        Dependencies bodyDependencies = returns.dependencies;
        bodyDependencies.dependentVariables.removeAll(dependencies.getGuaranteedChangedVariables());
        dependencies.potentiallyChangedVariables.addAll(bodyDependencies.getGuaranteedChangedVariables());
        dependencies.potentiallyChangedVariables.addAll(bodyDependencies.getPotentiallyChangedVariables());
        dependencies.dependentVariables.addAll(bodyDependencies.getDependentVariables());
        // Brings dependencies back to the top of the loop
        if (returns.getPdgNode() instanceof PDGNodeCompoundStatement) {
            new PDGUselessCodeRemover().propagateRequiredCompoundStatement(dependencies.dependentVariables, ((PDGNodeCompoundStatement) returns.getPdgNode()));
        }
        return new Returns<>(dependencies, new PDGNodeWhile(statement, returns.getPdgNode()));
    }

    private Returns<IterationStatementDoWhile> visit(IterationStatementDoWhile statement) {
        Dependencies dependencies = visit(statement.getCondition());
        Returns<? extends Statement> returns = visit(statement.getStatement());
        dependencies.add(returns.dependencies);
        // Brings dependencies back to the top of the loop
        if (returns.getPdgNode() instanceof PDGNodeCompoundStatement) {
            new PDGUselessCodeRemover().propagateRequiredCompoundStatement(dependencies.dependentVariables, ((PDGNodeCompoundStatement) returns.getPdgNode()));
        }
        return new Returns<>(dependencies, new PDGNodeDoWhile(statement, returns.getPdgNode()));
    }

    private Returns<ExpressionStatement> visit(ExpressionStatement statement) {
        boolean hasInvocations = false;
        if (statement.getExpression() != null) {
            hasInvocations = !statement.getExpression().getInvocations().isEmpty();
            return new Returns<>(visit(statement.getExpression()), new PDGNodeExpressionStatement(statement, hasInvocations));
        } else {
            return new Returns<>(new Dependencies(), new PDGNodeExpressionStatement(statement, false));
        }
    }

    private Dependencies visit(Expression expression) {
        Set<String> dependentVariables = expression.getDependentVariables();
        Set<String> changedVariables = expression.getGuaranteedChangedVariables();
        Set<String> potentialVariables = expression.getPotentiallyChangedVariables();
        return new Dependencies(dependentVariables, changedVariables, potentialVariables);
    }

    private Returns<VariableDeclaration> visit(VariableDeclaration variableDeclaration) {
        Set<String> declaredVariables = new HashSet<>();
        Set<String> dependentVariables = new HashSet<>();
        Set<String> guaranteedChangedVariables = new HashSet<>();
        Set<String> potentiallyChangedVariables = new HashSet<>();
        for (VariableDeclaration.DeclaredVariable declaredVariable : variableDeclaration.getDeclaredVariables()) {
            declaredVariables.add(declaredVariable.getIdentifier().getIdentifier());
            if (declaredVariable.getInitializer() != null) {
                Dependencies dependencies = visit(declaredVariable.getInitializer());
                dependencies.dependentVariables.removeAll(declaredVariables);
                dependentVariables.addAll(dependencies.dependentVariables);
                guaranteedChangedVariables.addAll(dependencies.getGuaranteedChangedVariables());
                potentiallyChangedVariables.addAll(dependencies.getPotentiallyChangedVariables());
                guaranteedChangedVariables.add(declaredVariable.getIdentifier().getIdentifier());
            }
        }
        return new Returns<>(new Dependencies(dependentVariables, guaranteedChangedVariables, potentiallyChangedVariables), new PDGNodeDeclaration(variableDeclaration));
    }

    private Returns<CompoundStatement> visit(CompoundStatement statement) {
        VariableMap usedSinceLastAssignment = new VariableMap();
        Map<BlockItem, PDGNode<? extends BlockItem>> allNodes = new IdentityHashMap<>();
        VariableMap lastAssigned = new VariableMap();
        VariableDeclarationMap variableDeclarations = new VariableDeclarationMap();
        Set<String> dependentVariables = new HashSet<>();
        Set<String> locallyDeclaredVariables = new HashSet<>();
        for (BlockItem blockItem : statement.getBlockItems()) {
            Returns<? extends BlockItem> returns = visit(blockItem);
            boolean required = false;
            PDGNode<? extends BlockItem> pdgNode = returns.getPdgNode();
            allNodes.put(blockItem, pdgNode);
            if (blockItem.readsMemory()) {
                /*
                for (PDGNode<VariableDeclaration> variableDeclarationPDGNode : variableDeclarations.values()) {
                    variableDeclarationPDGNode.linkVariableDependency(pdgNode);
                }
                for (PDGNode<? extends BlockItem> node : lastAssigned.getAllPDGNodes()) {
                    node.linkVariableDependency(pdgNode);
                }
                for (Collection<PDGNode<? extends BlockItem>> pdgNodes : usedSinceLastAssignment.values()) {
                    for (PDGNode<? extends BlockItem> node : pdgNodes) {
                        node.linkOrderDependency(pdgNode);
                    }
                    pdgNodes.add(pdgNode);
                }
                for (String s : returns.getDependencies().getDependentVariables()) {
                    usedSinceLastAssignment.augment(s, pdgNode);
                }
                */
                // For stdlib functions that take no parameters, like printf. If it reads memory, then it requires all
                // previous code to be there

                for (String s : blockItem.getDependantVariables()) {
                    for (Collection<PDGNode<? extends BlockItem>> pdgNodes : lastAssigned.getAllAssociated(s)) {
                        for (PDGNode<? extends BlockItem> node : pdgNodes) {
                            node.linkVariableDependency(pdgNode);
                        }
                    }
                    dependentVariables.add(s);
                }

                for (PDGNode<? extends BlockItem> node : allNodes.values()) {
                    if (TREAT_UNKNOWN_FUNCTIONS_AS_ORDER_DEPENDENT) {
                        node.linkOrderDependency(pdgNode);
                    } else {
                        node.linkVariableDependency(pdgNode);
                    }
                }
                // Set statement as one that depends on things outside of scope?

            } else {

                for (String usedVariable : returns.dependencies.getDependentVariables()) {
                    if (variableDeclarations.containsKey(usedVariable)) {
                        PDGNode<VariableDeclaration> declarationPDGNode = variableDeclarations.get(usedVariable);
                        declarationPDGNode.linkVariableDependency(pdgNode);
                    }
                    if (!lastAssigned.containsKey(usedVariable, identityTypeMapper.get(blockItem))) {
                        dependentVariables.add(usedVariable);
                    } else {
                        for (Collection<PDGNode<? extends BlockItem>> pdgNodes : lastAssigned.getAllAssociated(usedVariable, identityTypeMapper.get(blockItem))) {
                            for (PDGNode<? extends BlockItem> node : pdgNodes) {
                                //node.linkOrderDependency(pdgNode);
                                node.linkVariableDependency(pdgNode);
                            }
                        }
                    }
                    usedSinceLastAssignment.augment(usedVariable, pdgNode);
                }
            }
            if (blockItem.writesMemory()) {
                /*
                for (PDGNode<VariableDeclaration> variableDeclarationPDGNode : variableDeclarations.values()) {
                    variableDeclarationPDGNode.linkVariableDependency(pdgNode);
                }
                */
                required = true;
                /*
                for (Collection<PDGNode<? extends BlockItem>> pdgNodes : lastAssigned.values()) {
                    for (PDGNode<? extends BlockItem> node : pdgNodes) {
                        node.linkVariableDependency(pdgNode);
                    }
                    pdgNodes.add(pdgNode);
                }
                */
                for (String s : returns.getDependencies().getDependentVariables()) {
                    for (Collection<PDGNode<? extends BlockItem>> pdgNodes : lastAssigned.getAllAssociated(s)) {
                        for (PDGNode<? extends BlockItem> node : pdgNodes) {
                            node.linkVariableDependency(pdgNode);
                        }
                    }
                }
                for (String s : returns.getDependencies().getPotentiallyChangedVariables()) {
                    lastAssigned.augment(s, pdgNode);
                }
                for (String s : returns.getDependencies().getGuaranteedChangedVariables()) {
                    lastAssigned.augment(s, pdgNode);
                }
            } else {
                for (String guaranteedChangedVariable : returns.dependencies.getGuaranteedChangedVariables()) {
                    if (globalVariables.contains(guaranteedChangedVariable)) {
                        required = true;
                    }
                    if (variableDeclarations.containsKey(guaranteedChangedVariable)) {
                        PDGNode<VariableDeclaration> declarationPDGNode = variableDeclarations.get(guaranteedChangedVariable);
                        declarationPDGNode.linkVariableDependency(pdgNode);
                    }
                    if (lastAssigned.containsKey(guaranteedChangedVariable, identityTypeMapper.get(blockItem))) {
                        for (Collection<PDGNode<? extends BlockItem>> nodes : lastAssigned.getAllAssociated(guaranteedChangedVariable, identityTypeMapper.get(blockItem))) {
                            for (PDGNode<? extends BlockItem> node : nodes) {
                                node.linkOrderDependency(pdgNode);
                            }
                        }
                    }
                    lastAssigned.replace(guaranteedChangedVariable, pdgNode);
                    //lastAssigned.put(guaranteedChangedVariable, Sets.newHashSet(pdgNode));
                    if (usedSinceLastAssignment.containsKey(guaranteedChangedVariable, identityTypeMapper.get(blockItem))) {
                        for (Collection<PDGNode<? extends BlockItem>> usedSinceLastAssignmentNodes : usedSinceLastAssignment.getAllAssociated(guaranteedChangedVariable, identityTypeMapper.get(blockItem))) {
                            for (PDGNode<? extends BlockItem> usedSinceLastAssignmentNode : usedSinceLastAssignmentNodes) {
                                usedSinceLastAssignmentNode.linkOrderDependency(pdgNode);
                            }
                        }
                    }

                    usedSinceLastAssignment.clearVariable(guaranteedChangedVariable);
                    //usedSinceLastAssignment.put(guaranteedChangedVariable, new HashSet<>());
                }
                for (String potentiallyChangedVariable : returns.dependencies.getPotentiallyChangedVariables()) {
                    if (globalVariables.contains(potentiallyChangedVariable)) {
                        required = true;
                    }
                    if (variableDeclarations.containsKey(potentiallyChangedVariable)) {
                        PDGNode<VariableDeclaration> declarationPDGNode = variableDeclarations.get(potentiallyChangedVariable);
                        declarationPDGNode.linkVariableDependency(pdgNode);
                    }
                    if (lastAssigned.containsKey(potentiallyChangedVariable, identityTypeMapper.get(blockItem))) {
                        for (Collection<PDGNode<? extends BlockItem>> previouslyPotentiallyAssigneds : lastAssigned.getAllAssociated(potentiallyChangedVariable, identityTypeMapper.get(blockItem))) {
                            for (PDGNode<? extends BlockItem> previouslyPotentiallyAssigned : previouslyPotentiallyAssigneds) {
                                //previouslyPotentiallyAssigned.linkVariableDependency(pdgNode);
                                previouslyPotentiallyAssigned.linkOrderDependency(pdgNode);
                            }
                        }
                    } else {
                        lastAssigned.clearVariable(potentiallyChangedVariable);
                        //lastAssigned.put(potentiallyChangedVariable, new ArrayList<>());
                        //lastAssigned.augment(potentiallyChangedVariable, pdgNode);
                    }
                    lastAssigned.augment(potentiallyChangedVariable, pdgNode);
                    //lastAssigned.get(potentiallyChangedVariable).add(pdgNode);

                    for (Collection<PDGNode<? extends BlockItem>> usedSinceLastAssignmentNodes : usedSinceLastAssignment.getAllAssociated(potentiallyChangedVariable, identityTypeMapper.get(blockItem))) {
                        for (PDGNode<? extends BlockItem> usedSinceLastAssignmentNode : usedSinceLastAssignmentNodes) {
                            usedSinceLastAssignmentNode.linkOrderDependency(pdgNode);
                        }
                    }
                }
            }
            /*
            if (blockItem.hasJump()) {
                for (PDGNode<? extends BlockItem> node : allNodes.values()) {
                    node.linkOrderDependency(pdgNode);
                }
            }
            */
            if (blockItem instanceof VariableDeclaration) {
                for (VariableDeclaration.DeclaredVariable declaredVariable : ((VariableDeclaration) blockItem).getDeclaredVariables()) {
                    variableDeclarations.put(declaredVariable.getIdentifier().getIdentifier(), ((PDGNode<VariableDeclaration>) pdgNode));
                    //lastAssigned.put(declaredVariable.getIdentifier(), Sets.newHashSet(pdgNode));
                    locallyDeclaredVariables.add(declaredVariable.getIdentifier().getIdentifier());
                }
            }
            if (required) {
                pdgNode.setRequired(true);
            }
        }
        HashSet<String> changed = new HashSet<>(lastAssigned.keySet());
        changed.removeAll(locallyDeclaredVariables);
        Set<String> guaranteedChangedVariables = changed.stream().filter(str -> lastAssigned.get(str).size() == 1).collect(Collectors.toSet());
        Set<String> potentiallyChangedVariables = changed.stream().filter(str -> lastAssigned.get(str).size() > 1).collect(Collectors.toSet());
        if (guaranteedChangedVariables.size() + potentiallyChangedVariables.size() != changed.size()) {
            System.err.println("Huh, doesn't add up");
        }
        //printAdjacencyMatrix(allNodes);
        return new Returns<>(new Dependencies(dependentVariables, guaranteedChangedVariables, potentiallyChangedVariables), new PDGNodeCompoundStatement(statement, allNodes.values(), lastAssigned));
    }

    private Returns<SelectionStatementIf> visit(SelectionStatementIf statement) {
        Dependencies conditionDependencies = visit(statement.getCondition());
        Set<String> dependentVariables = new HashSet<>(conditionDependencies.getDependentVariables());
        Set<String> guaranteedChangedVariables = new HashSet<>(conditionDependencies.getGuaranteedChangedVariables());
        Set<String> potentiallyChangedVariables = new HashSet<>(conditionDependencies.getPotentiallyChangedVariables());

        Returns<? extends Statement> thenReturns = visit(statement.getThenStatement());
        Dependencies thenDependencies = thenReturns.dependencies;
        thenDependencies.dependentVariables.removeAll(guaranteedChangedVariables);
        dependentVariables.addAll(thenDependencies.dependentVariables);
        potentiallyChangedVariables.addAll(thenDependencies.getPotentiallyChangedVariables());
        PDGNode<? extends Statement> elseNode = null;
        if (statement.getElseStatement() != null) {
            Set<String> branchesChangedVariables = new HashSet<>();
            Returns<? extends Statement> elseReturns = visit(statement.getElseStatement());
            elseNode = elseReturns.pdgNode;
            Dependencies elseDependencies = elseReturns.dependencies;
            potentiallyChangedVariables.addAll(elseDependencies.getPotentiallyChangedVariables());
            elseDependencies.dependentVariables.removeAll(guaranteedChangedVariables);
            dependentVariables.addAll(elseDependencies.dependentVariables);
            branchesChangedVariables.addAll(elseDependencies.getGuaranteedChangedVariables());
            branchesChangedVariables.retainAll(thenDependencies.getGuaranteedChangedVariables());
            guaranteedChangedVariables.addAll(branchesChangedVariables);
            Set<String> nonIntersectingGuarantees = new HashSet<>();
            nonIntersectingGuarantees.addAll(thenDependencies.getGuaranteedChangedVariables());
            nonIntersectingGuarantees.addAll(elseDependencies.getGuaranteedChangedVariables());
            nonIntersectingGuarantees.removeAll(branchesChangedVariables);
            potentiallyChangedVariables.addAll(nonIntersectingGuarantees);
        } else {
            potentiallyChangedVariables.addAll(thenDependencies.getGuaranteedChangedVariables());
            potentiallyChangedVariables.addAll(thenDependencies.getPotentiallyChangedVariables());
        }

        //return new Returns<>(new Dependencies(statement.getDependantVariables(), statement.getGuaranteedChangedVariables(), statement.getPotentiallyChangedVariables()), new PDGNodeIf(statement, thenReturns.pdgNode, elseNode, statement.isCritical()));

        return new Returns<>(new Dependencies(dependentVariables, guaranteedChangedVariables, potentiallyChangedVariables), new PDGNodeIf(statement, thenReturns.pdgNode, elseNode, statement.isCritical()));
    }

    private void printLastAssigned(Map<String, BlockItem> map) {
        for (Map.Entry<String, BlockItem> stringBlockItemEntry : map.entrySet()) {
            System.out.println(stringBlockItemEntry.getKey() + ": " + stringBlockItemEntry.getValue().toCode(false));
        }
    }
}
