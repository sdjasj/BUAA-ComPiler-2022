package SyntaxTree;

import IntermediateCode.AllCode.AssignCode;
import IntermediateCode.AllCode.DeclCode;
import IntermediateCode.AllCode.MemoryCode;
import IntermediateCode.GlobalArrayDecl;
import IntermediateCode.GlobalDecl;
import IntermediateCode.GlobalVarDecl;
import IntermediateCode.IntermediateVisitor;
import IntermediateCode.Operand;
import IntermediateCode.Operator;
import IntermediateCode.TCode;
import Lexer.Token;
import MySymbolTable.SymbolTable;

import java.io.IOException;
import java.util.ArrayList;

public class VarDefNode extends ParserNode {
    //VarDef → Ident { '[' ConstExp ']' } // 包含普通变量、一维数组、二维数组定义
    //VarDef → Ident { '[' ConstExp ']' } '=' InitVal
    private Token ident;
    private ArrayList<Integer> dimensionLength;
    private InitValNode initValNode;
    private BTypeNode bTypeNode;

    //initValNode为null说明没有初值
    public VarDefNode(Token ident,
                      ArrayList<Integer> dimensionLength,
                      InitValNode initValNode) {
        this.ident = ident;
        this.dimensionLength = dimensionLength;
        this.initValNode = initValNode;
    }

    public void setbTypeNode(BTypeNode bTypeNode) {
        this.bTypeNode = bTypeNode;
    }

    public boolean hasInitval() {
        return initValNode != null;
    }

    public void generateIntermediate(IntermediateVisitor intermediateVisitor) {
        if (blockDepth == 0) {
            //global
            if (dimensionLength.size() == 0) {
                //非数组变量
                GlobalDecl.StoreType storeType = null;
                if (bTypeNode.isInt()) {
                    storeType = GlobalDecl.StoreType.int_;
                }
                String name = TCode.reName(ident.getValue(), blockDepth);
                int initval = 0;
                if (hasInitval()) {
                    initval = initValNode.getConstVal(RecordSymbolTable);
                }
                GlobalVarDecl globalVarDecl = new GlobalVarDecl(storeType, name, false, initval);
                intermediateVisitor.addGlobalDecl(globalVarDecl);
            } else {
                GlobalDecl.StoreType storeType = null;
                if (bTypeNode.isInt()) {
                    storeType = GlobalDecl.StoreType.int_;
                }
                String name = TCode.reName(ident.getValue(), blockDepth);
                ArrayList<Integer> initval = null;
                if (hasInitval()) {
                    initval = initValNode.getConstArrayVal(RecordSymbolTable);
                }
                GlobalArrayDecl globalArrayDecl =
                    new GlobalArrayDecl(storeType, name, false, dimensionLength, initval);
                intermediateVisitor.addGlobalDecl(globalArrayDecl);
            }
        } else {
            if (dimensionLength.size() == 0) {
                //非数组变量
                String name =  TCode.reName(ident.getValue(), blockDepth);
                Operand target = Operand.getNewOperand(name, Operand.OperandType.VAR);
                DeclCode declCode =
                    new DeclCode(target, new ArrayList<>());
                intermediateVisitor.addIntermediateCode(declCode);
                if (hasInitval()) {
                    Operand src1 = initValNode.generateMidCodeAndReturnTempVarAsVar(intermediateVisitor);
                    AssignCode assignCode = new AssignCode(target, src1);
                    intermediateVisitor.addIntermediateCode(assignCode);
                }
            } else {
                String name =  TCode.reName(ident.getValue(), blockDepth);
                Operand target = Operand.getNewOperand(name, Operand.OperandType.ADDRESS);
                DeclCode declCode = new DeclCode(target, dimensionLength);
                intermediateVisitor.addIntermediateCode(declCode);
                if (hasInitval()) {
                    ArrayList<Operand> operands =
                        initValNode.generateMidCodeAndReturnTempVarAsArray(intermediateVisitor);
                    for (int i = 0; i < operands.size(); i++) {
                        Operand t = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
                        intermediateVisitor.addIntermediateCode(new AssignCode(t, operands.get(i)));
                        MemoryCode memoryCode = new MemoryCode(t, target,
                            Operand.getNewOperand(String.valueOf(i * 4), Operand.OperandType.NUMBER),
                            Operator.STORE);
                        intermediateVisitor.addIntermediateCode(memoryCode);
                    }
                }
            }
        }
    }
}
