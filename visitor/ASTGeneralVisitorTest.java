package visitor;

import ast.BlockItem;
import ast.Function;
import ast.Program;
import ast.Statement;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;
import parser.CLexer;
import parser.CParser;
import pdg.PDGNode;
import pdg.PDGSorterDefault;

import java.util.Collection;
import java.util.List;

/**
 * Created by ooee on 10/8/16.
 */
public class ASTGeneralVisitorTest {
    public static final String TEST_FILE = "cfiles/lValueTest.c";
    @Test
    public void test() throws Exception {
        CLexer lexer = new CLexer(new ANTLRFileStream(TEST_FILE));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CParser parser = new CParser(tokens);

        CParser.CompilationUnitContext compilationUnit = parser.compilationUnit();
        TreeToASTVisitor visitor = new TreeToASTVisitor();
        Program program = visitor.visit(compilationUnit);
        PDGGenerationVisitor pdgVisitor = new PDGGenerationVisitor();
        pdgVisitor.visit(program);
        for (Function function : program.getFunction()) {
            Collection<PDGNode> functionBody = pdgVisitor.visit(function);
            System.out.println(functionBody.size());
            PDGSorterDefault sorter = new PDGSorterDefault();
            Statement statement = sorter.sort(functionBody);
            System.out.println("Reprinted program");
            System.out.println(statement.toCode());
        }


    }
}