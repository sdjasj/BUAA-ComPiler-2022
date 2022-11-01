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

    @Override
    public void generateIntermediate(IntermediateVisitor intermediateVisitor) {
        String name = TCode.reName(ident.getValue(), blockDepth);
        FunctionParam functionParam =
            new FunctionParam(new Operand(name, Operand.OperandType.VAR), dimensionLength);

        intermediateVisitor.addIntermediateCode(functionParam);
    }
}
