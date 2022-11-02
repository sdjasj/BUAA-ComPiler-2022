package SyntaxTree;

import IntermediateCode.FunctionCode.FunctionCode;
import IntermediateCode.FunctionCode.FunctionEndCode;
import IntermediateCode.FunctionCode.FunctionReturnCode;
import IntermediateCode.IntermediateVisitor;
import IntermediateCode.Operand;
import Lexer.Token;
import Lexer.TokenType;

public class FuncDefNode extends ParserNode {
    private FuncTypeNode funcType;
    private Token ident;
    private FuncFParamsNode funcFParams;
    private BlockNode block;

    public FuncDefNode(FuncTypeNode funcType, Token ident, FuncFParamsNode funcFParams,
                       BlockNode block) {
        this.funcType = funcType;
        this.ident = ident;
        this.funcFParams = funcFParams;
        this.block = block;
    }

    public void generateIntermediate(IntermediateVisitor intermediateVisitor) {
        intermediateVisitor.changeNewFunction(false, ident.getValue());
        FunctionCode functionCode =
            new FunctionCode(new Operand(ident.getValue(), Operand.OperandType.ADDRESS));
        intermediateVisitor.addIntermediateCode(functionCode);
        if (funcFParams != null) {
            funcFParams.generateIntermediate(intermediateVisitor);
        }
        block.generateIntermediate(intermediateVisitor, null);
        if (funcType.getFuncType() == TokenType.VOIDTK) {
            intermediateVisitor.addIntermediateCode(new FunctionReturnCode());
        }
        FunctionEndCode functionEndCode =
            new FunctionEndCode(new Operand(ident.getValue(), Operand.OperandType.ADDRESS));
        intermediateVisitor.addIntermediateCode(functionEndCode);
    }
}
