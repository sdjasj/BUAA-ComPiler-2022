package SyntaxTree;

import IntermediateCode.FunctionCode.ExitCode;
import IntermediateCode.FunctionCode.FunctionCode;
import IntermediateCode.FunctionCode.FunctionEndCode;
import IntermediateCode.IntermediateVisitor;
import IntermediateCode.Operand;

//MainFuncDef → 'int' 'main' '(' ')' Block
public class MainFuncDefNode extends ParserNode {
    private FuncTypeNode funcType;
    private BlockNode blockNode;

    public MainFuncDefNode(FuncTypeNode tokenType, BlockNode blockNode) {
        this.funcType = tokenType;
        this.blockNode = blockNode;
    }

    @Override
    public void generateIntermediate(IntermediateVisitor intermediateVisitor) {
        intermediateVisitor.changeNewFunction(true, "main");
        FunctionCode functionCode = new FunctionCode(new Operand("main", Operand.OperandType.ADDRESS));
        intermediateVisitor.addIntermediateCode(functionCode);
        blockNode.generateIntermediate(intermediateVisitor);
        FunctionEndCode functionEndCode =
            new FunctionEndCode(new Operand("main", Operand.OperandType.ADDRESS));
        intermediateVisitor.addIntermediateCode(functionEndCode);
    }
}
