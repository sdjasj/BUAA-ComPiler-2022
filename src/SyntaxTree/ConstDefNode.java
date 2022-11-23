package SyntaxTree;

import IntermediateCode.AllCode.ConstAssignCode;
import IntermediateCode.AllCode.DeclCode;
import IntermediateCode.GlobalArrayDecl;
import IntermediateCode.GlobalDecl;
import IntermediateCode.GlobalVarDecl;
import IntermediateCode.IntermediateVisitor;
import IntermediateCode.Operand;
import IntermediateCode.TCode;
import Lexer.Token;

import java.util.ArrayList;

//ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
public class ConstDefNode extends ParserNode {
    private Token ident;
    private ArrayList<Integer> dimensionLength;
    private ConstInitValNode constInitValNode;
    private BTypeNode bTypeNode;

    public ConstDefNode(Token ident, ArrayList<Integer> dimensionLength,
                        ConstInitValNode constInitValNode) {
        this.ident = ident;
        this.dimensionLength = dimensionLength;
        this.constInitValNode = constInitValNode;
    }

    public void setbTypeNode(BTypeNode bTypeNode) {
        this.bTypeNode = bTypeNode;
    }

    public void generateIntermediate(IntermediateVisitor intermediateVisitor) {
//        if (blockDepth == 0) {
            //global
        if (dimensionLength.size() == 0) {
            //非数组常量
            int initval = constInitValNode.getConstVal(RecordSymbolTable);
            String name = TCode.reName(ident.getValue(), blockDepth);
            GlobalVarDecl constDecl =
                new GlobalVarDecl(GlobalDecl.StoreType.int_, name, true, initval);
            intermediateVisitor.addGlobalDecl(constDecl);
        } else {
            //数组
            ArrayList<Integer> initval = constInitValNode.getConstArrayVal(RecordSymbolTable);
            String name = TCode.reName(ident.getValue(), blockDepth);
            GlobalArrayDecl arrayDecl =
                new GlobalArrayDecl(GlobalDecl.StoreType.int_, name, true, dimensionLength,
                    initval);
            intermediateVisitor.addGlobalDecl(arrayDecl);
        }
//        } else {
//            if (dimensionLength.size() == 0) {
//                int initVal = constInitValNode.getConstVal(RecordSymbolTable);
//                String name = TCode.reName(ident.getValue(), blockDepth);
//
//                Operand target = Operand.getNewOperand(name, Operand.OperandType.VAR);
//                DeclCode declCode =
//                    new DeclCode(target, new ArrayList<>());
//                intermediateVisitor.addIntermediateCode(declCode);
//
//                ConstAssignCode constAssignCode =
//                    new ConstAssignCode(Operand.getNewOperand(name, Operand.OperandType.VAR),
//                        new ArrayList<Integer>() {
//                            {
//                                add(initVal);
//                            }
//                        }, new ArrayList<>());
//                intermediateVisitor.addIntermediateCode(constAssignCode);
//            } else {
//
//
//                ArrayList<Integer> initVal = constInitValNode.getConstArrayVal(RecordSymbolTable);
//                String name = TCode.reName(ident.getValue(), blockDepth);
//
//                Operand target = Operand.getNewOperand(name, Operand.OperandType.ADDRESS);
//                DeclCode declCode = new DeclCode(target, dimensionLength);
//                intermediateVisitor.addIntermediateCode(declCode);
//
//                ConstAssignCode constAssignCode =
//                    new ConstAssignCode(Operand.getNewOperand(name, Operand.OperandType.ADDRESS), initVal,
//                        dimensionLength);
//                intermediateVisitor.addIntermediateCode(constAssignCode);
//            }
//        }
    }
}
