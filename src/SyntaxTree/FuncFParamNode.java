package SyntaxTree;

import IntermediateCode.FunctionCode.FunctionParam;
import IntermediateCode.IntermediateVisitor;
import IntermediateCode.Operand;
import IntermediateCode.TCode;
import Lexer.Token;

import java.util.ArrayList;

public class FuncFParamNode extends ParserNode {
    private BTypeNode bTypeNode;
    private Token ident;
    private int dimension;
    private ArrayList<Integer> dimensionLength;

    public FuncFParamNode(BTypeNode bTypeNode, Token ident, int dimension,
                          ArrayList<Integer> dimensionLength) {
        this.bTypeNode = bTypeNode;
        this.ident = ident;
        this.dimension = dimension;
        this.dimensionLength = dimensionLength;
    }

    public void generateIntermediate(IntermediateVisitor intermediateVisitor) {
        String name = TCode.reName(ident.getValue(), blockDepth);
        Operand.OperandType operandType;
        if (dimensionLength.size() > 0) {
            operandType = Operand.OperandType.ADDRESS;
        } else {
            operandType = Operand.OperandType.VAR;
        }
        FunctionParam functionParam =
            new FunctionParam(Operand.getNewOperand(name, operandType), dimensionLength);

        intermediateVisitor.addIntermediateCode(functionParam);
    }
}
