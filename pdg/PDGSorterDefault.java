package pdg;

import ast.*;
import com.google.common.collect.Sets;
import lombok.Value;

import java.util.*;

/**
 * Created by ooee on 10/14/16.
 */
@Value
public class PDGSorterDefault implements PDGSorter {

    @Override
    public CompoundStatement sort(Collection<PDGNode<? extends BlockItem>> nodes) {
        List<BlockItem> blockItems = new ArrayList<>();
        List<Declaration> emptyDeclarations = new ArrayList<>();
        List<PDGNode> emptyDeclarationNodes = new ArrayList<>();
        for (PDGNode node : nodes) {
            if (node.blockItem instanceof Declaration) {
                if (node.blockItem.getDependantVariables().isEmpty()) {
                    emptyDeclarations.add(((Declaration) node.blockItem));
                    emptyDeclarationNodes.add(node);
                }
            }
        }
        blockItems.addAll(emptyDeclarations);
        for (PDGNode emptyDeclarationNode : emptyDeclarationNodes) {
            removeNode(emptyDeclarationNode, nodes);
        }
        markRequiredNodes(nodes);
        removeAllNonRequiredNodes(nodes);
        while (nodes.size() > 0) {
            Collection<PDGNode<? extends BlockItem>> candidateNextNodes = getReadyNodes(nodes);
            PDGNode chosenNode = pickNextNode(candidateNextNodes);
            removeNode(chosenNode, nodes);
            blockItems.add(processNode(chosenNode));
        }
        return new CompoundStatement(blockItems);
    }

    private PDGNode<? extends BlockItem> pickNextNode(Collection<PDGNode<? extends BlockItem>> candidates) {
        int mostDependsOn = -1;
        PDGNode best;
        best = candidates.stream().findFirst().get();
        for (PDGNode candidate : candidates) {
            if (!(candidate.blockItem instanceof JumpStatementStrict) && candidate.getIsADependencyFor().size() > mostDependsOn) {
                best = candidate;
                mostDependsOn = candidate.getIsADependencyFor().size();
            }
        }
        return best;
    }

    private void removeNode(PDGNode<? extends BlockItem> node, Collection<PDGNode<? extends BlockItem>> nodes) {
        nodes.remove(node);
        Collection<PDGNode<? extends BlockItem>> dependents = node.getIsADependencyFor();
        for (PDGNode dependent : dependents) {
            dependent.getDependsOn().remove(node);
        }
    }

    private BlockItem processNode(PDGNode node) {
        return node.sort(this);
    }

    private Collection<PDGNode<? extends BlockItem>> getReadyNodes(Collection<PDGNode<? extends BlockItem>> nodes) {
        Collection<PDGNode<? extends BlockItem>> readyNodes = new ArrayList<>();
        for (PDGNode<? extends BlockItem> node : nodes) {
            if (node.getDependsOn().isEmpty()) {
                readyNodes.add(node);
            }
        }
        return readyNodes;
    }

    private void propagateRequired(PDGNode<? extends BlockItem> node, Set<PDGNode<? extends BlockItem>> required) {
        for (PDGNode<? extends BlockItem> pdgNode : node.getDependsOn()) {
            if (!required.contains(pdgNode)) {
                required.add(pdgNode);
                pdgNode.required = true;
                propagateRequired(pdgNode, required);
            }
            if (pdgNode instanceof PDGNodeContainsStatementNode) {
                propagateThroughStatementNodes(((PDGNodeContainsStatementNode<? extends Statement>) pdgNode), node.getBlockItem().getDependantVariables());
            }
        }
    }

    private void propagateThroughStatementNodes(PDGNodeContainsStatementNode<? extends Statement> node, Set<String> variablesToBeRequired) {
        for (PDGNode<? extends Statement> pdgNode : node.getStatementNodes()) {
            if (pdgNode instanceof PDGNodeCompoundStatement) {
                propagateRequiredCompoundStatement(variablesToBeRequired, (PDGNodeCompoundStatement)pdgNode, new HashSet<>());
            } else if (pdgNode instanceof PDGNodeContainsStatementNode) {
                propagateThroughStatementNodes(((PDGNodeContainsStatementNode<? extends Statement>) pdgNode), variablesToBeRequired);
            }
        }
    }

    private void propagateRequiredCompoundStatement(Set<String> dependVariables, PDGNodeCompoundStatement pdgNode, Set<PDGNode<? extends BlockItem>> required) {
        for (String dependVariable : dependVariables) {
            for (PDGNode<? extends BlockItem> node : pdgNode.getLastAssigned().get(dependVariable)) {
                if (!required.contains(node)) {
                    node.required = true;
                    required.add(node);
                    propagateRequired(node, required);
                }
                if (node instanceof PDGNodeContainsStatementNode) {
                    propagateThroughStatementNodes(((PDGNodeContainsStatementNode<? extends Statement>) node), Sets.newHashSet(dependVariable));
                }
            }
        }
    }

    private Collection<PDGNode<? extends BlockItem>> markRequiredNodes(Collection<PDGNode<? extends BlockItem>> nodes) {
        Set<PDGNode<? extends BlockItem>> required = new HashSet<>();
        for (PDGNode<? extends BlockItem> node : nodes) {
            if (node.isRequired() && !required.contains(node)) {
                required.add(node);
                propagateRequired(node, required);
            }
        }
        return nodes;
    }

    private void removeAllNonRequiredNodes(Collection<PDGNode<? extends BlockItem>> nodes) {
        Set<PDGNode<? extends BlockItem>> notRequired = new HashSet<>();
        for (PDGNode<? extends BlockItem> node : nodes) {
            if (node instanceof PDGNodeContainsStatementNode) {
                for (PDGNode<? extends Statement> pdgNode : ((PDGNodeContainsStatementNode<? extends Statement>) node).getStatementNodes()) {
                    if (pdgNode instanceof PDGNodeCompoundStatement) {
                        removeAllNonRequiredNodes(((PDGNodeCompoundStatement) pdgNode).getBody());
                    }
                }
            }
            if (!node.isRequired()) {
                notRequired.add(node);
            }
        }
        nodes.removeAll(notRequired);
    }
}