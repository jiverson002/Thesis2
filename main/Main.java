package main;

import ast.*;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import parser.CLexer;
import parser.CParser;
import pdg.PDGNode;
import pdg.PDGSorter;
import pdg.PDGSorterDefault;
import pdg.PDGUselessCodeRemover;
import visitor.FunctionArgumentOrderVisitor;
import visitor.IdentifierNormalizerVisitor;
import visitor.PDGGenerationVisitor;
import visitor.TreeToASTVisitor;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by ooee on 10/20/16.
 */
public class Main {
    public static void main(String args[]) {
        if (args.length < 1) {
            System.err.println("Put C file as first parameter");
            System.exit(-1);
        }
        try {
            CLexer lexer = new CLexer(new ANTLRFileStream(args[0]));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            CParser parser = new CParser(tokens);
            CParser.CompilationUnitContext compilationUnit = parser.compilationUnit();
            TreeToASTVisitor visitor = new TreeToASTVisitor();
            Program program = visitor.visit(compilationUnit);
            PDGGenerationVisitor pdgVisitor = new PDGGenerationVisitor();
            pdgVisitor.visit(program);
            for (Function function : program.getFunction()) {
                Collection<PDGNode<? extends BlockItem>> functionBody = pdgVisitor.visit(function);
                PDGUselessCodeRemover pdgUselessCodeRemover = new PDGUselessCodeRemover();
                pdgUselessCodeRemover.removeUselessCode(functionBody);

                PDGSorter sorter = new PDGSorterDefault();
                CompoundStatement statement = sorter.sort(functionBody);
                function.setCompoundStatement(statement);
                System.out.println(function.toCode());

                IdentifierNormalizerVisitor identifierNormalizerVisitor = new IdentifierNormalizerVisitor();
                //identifierNormalizerVisitor.visit(function);
            }
            FunctionArgumentOrderVisitor functionArgumentOrderVisitor = new FunctionArgumentOrderVisitor();
            //functionArgumentOrderVisitor.visit(program);
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}