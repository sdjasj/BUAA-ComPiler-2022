package SyntaxTree;

import ErrorTool.FuncRealParamsType;
import IntermediateCode.AllCode.CalculateCode;
import IntermediateCode.AllCode.MemoryCode;
import IntermediateCode.IntermediateVisitor;
import IntermediateCode.Operand;
import IntermediateCode.Operator;
import IntermediateCode.TCode;
import Lexer.Token;
import MySymbolTable.SymbolTable;
import MySymbolTable.SymbolTableItem;
import MySymbolTable.SymbolType;
import Tool.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


//LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组
public class LValNode extends ParserNode {
    private Token ident;
    private ArrayList<ExpNode> dimensionOfExp;
    //dimensionAndExp size为0 -> 非数组; size为1 -> 一维数组; size为2 -> 二维数组

    public LValNode(Token ident, ArrayList<ExpNode> dimensionOfExp) {
        this.ident = ident;
        this.dimensionOfExp = dimensionOfExp;
    }

    public int getDimension() {
        return dimensionOfExp.size();
    }

    public int getConstVal(SymbolTable symbolTable) {
        SymbolTable mySymbolTable = symbolTable;
        SymbolTableItem item = null;
        while (mySymbolTable != null) {
            HashMap<String, SymbolTableItem> items = mySymbolTable.getItems();
            if (items.containsKey(ident.getValue())) {
                SymbolTableItem item2 = items.get(ident.getValue());
                if (item2.getLine() <= ident.getLine()) {
                    //找到之前声明的常量
                    item = item2;
                    break;
                }
            }
            mySymbolTable = mySymbolTable.getParentTable();
        }

        if (item == null) {
//            System.err.println(ident.getValue());
            System.err.println("error in getConstVal of LValNode");
            return 0;
        }
        int dimension = getDimension();
        if (dimension == 0) {
            return item.getConstVarInitVal();
        } else if (dimension == 1) {
            //左值是一维常量数组
            int dimensionVal = dimensionOfExp.get(0).getConstVal(symbolTable);
            ArrayList<Integer> constArray = item.getConstArrayInitVal();
            return constArray.get(dimensionVal);
        } else {
            int oneDimension = dimensionOfExp.get(0).getConstVal(symbolTable);
            int twoDimension = dimensionOfExp.get(1).getConstVal(symbolTable);
//            System.err.println(item.getName());
            ArrayList<Integer> constArray = item.getConstArrayInitVal();
            ArrayList<Integer> dimensionLength = item.getConstDimensionLength();
            return constArray.get(oneDimension * dimensionLength.get(0) + twoDimension);
        }
    }

    public FuncRealParamsType getFuncRealParamsType(SymbolTable symbolTable) {
        int dimension = getDimension();
        SymbolTableItem item =
            SymbolTable.findItemFromAllTable(ident, symbolTable);
        if (item == null) {
//            System.out.println("error in getFuncRealParamsType of LvalNode of no ident");
            return null;
        }
        if (item.getDimension() == 0) {
            if (getDimension() != 0) {
                System.out.println("error in getFuncRealParamsType of dimension 0");
            }
            return new FuncRealParamsType(SymbolType.VAR, 0);
        } else if (item.getDimension() == 1) { //左值声明为一维数组
            if (getDimension() == 0) {
                return new FuncRealParamsType(SymbolType.ARRAY, 1);
            } else {
                return new FuncRealParamsType(SymbolType.VAR, 0);
            }
        } else { //左值声明为二维数组
            if (getDimension() == 0) { //左值使用为二维数组
                return new FuncRealParamsType(SymbolType.ARRAY, 2, item.getConstDimensionLength());
            } else if (getDimension() == 1) { //左值使用为一维数组
                return new FuncRealParamsType(SymbolType.ARRAY, 1, item.getConstDimensionLength());
            } else { //左值使用为常数
                return new FuncRealParamsType(SymbolType.VAR, 0);
            }
        }
    }

    public Token getIdent() {
        return ident;
    }

    public Operand generateMidCodeAndUseAsRight(IntermediateVisitor intermediateVisitor) {
        SymbolTableItem item =
            SymbolTable.findItemFromAllTable(ident, RecordSymbolTable);

        if (item == null) {
            System.err.println("no find ident in lvalNode");
        }

        //查符号表看声明的地方
        String name = item.getName();
        int dimension = getDimension();
        if (dimension == 0) {

            if (item.getDimension() > dimension) {
                return new Operand(TCode.reName(name, item.getBlockDepth()),
                    Operand.OperandType.ADDRESS);
            } else {
                //非数组
                //常量
                if (item.isConst()) {
                    return new Operand(String.valueOf(item.getConstVarInitVal()),
                        Operand.OperandType.NUMBER);
                }
                //变量
                return new Operand(TCode.reName(name, item.getBlockDepth()), Operand.OperandType.VAR);
            }
        }

        //变量数组
        //数组取一维值直接返回
        if (dimension == 1) {
            //数组取一维值，形如a[x], src1 = x,但a可能是多维数组
            //得到数组偏移
            if (item.getDimension() > dimension) {
                Operand src2 =
                    dimensionOfExp.get(0).generateMidCodeAndReturnTempVar(intermediateVisitor);

                Operand temp = new Operand(TCode.genNewT(), Operand.OperandType.VAR);

                intermediateVisitor.addIntermediateCode(new CalculateCode(temp, src2,
                    new Operand(String.valueOf(item.getDimensionLength(1) * 4),
                        Operand.OperandType.NUMBER), Operator.MUL));
                //bug
                src2 = temp;
                Operand target = new Operand(TCode.genNewT(), Operand.OperandType.ADDRESS);
                Operand src1 =
                    new Operand(TCode.reName(name, item.getBlockDepth()), Operand.OperandType.ADDRESS);
                CalculateCode calculateCode = new CalculateCode(target, src1, src2, Operator.ADD);
                intermediateVisitor.addIntermediateCode(calculateCode);



                return target;
            } else {
                Operand src2 =
                    dimensionOfExp.get(0).generateMidCodeAndReturnTempVar(intermediateVisitor);
                Operand temp = new Operand(TCode.genNewT(), Operand.OperandType.VAR);
                intermediateVisitor.addIntermediateCode(
                    new CalculateCode(temp, src2, new Operand("4",
                        Operand.OperandType.NUMBER), Operator.MUL));
                src2 = temp;

                Operand target = new Operand(TCode.genNewT(), Operand.OperandType.VAR);
                Operand src1 =
                    new Operand(TCode.reName(name, item.getBlockDepth()), Operand.OperandType.ADDRESS);
                MemoryCode memoryCode = new MemoryCode(target, src1, src2, Operator.LOAD);
                intermediateVisitor.addIntermediateCode(memoryCode);
                return target;
            }
        }

        //array name
        String arrayName = TCode.reName(ident.getValue(), item.getBlockDepth());
        Operand target = new Operand(TCode.genNewT(), Operand.OperandType.VAR);
        Operand src1 = dimensionOfExp.get(0).generateMidCodeAndReturnTempVar(intermediateVisitor);
        int dimensionLength = item.getDimensionLength(1);
        CalculateCode calculateCode =
            new CalculateCode(target, src1,
                new Operand(String.valueOf(dimensionLength), Operand.OperandType.NUMBER),
                Operator.MUL
            );
        intermediateVisitor.addIntermediateCode(calculateCode);
        src1 = target;
        for (int i = 1; i < dimensionOfExp.size(); i++) {
            Operand src2 =
                dimensionOfExp.get(i).generateMidCodeAndReturnTempVar(intermediateVisitor);
            dimensionLength = item.getDimensionLength(i + 1);

            if (dimensionLength == 0) {
                if (item.getDimension() > dimension) {
                    target = new Operand(TCode.genNewT(), Operand.OperandType.VAR);
                    calculateCode = new CalculateCode(target, src1, src2, Operator.ADD);
                    intermediateVisitor.addIntermediateCode(calculateCode);


                    Operand temp = new Operand(TCode.genNewT(), Operand.OperandType.VAR);
                    intermediateVisitor.addIntermediateCode(
                        new CalculateCode(temp, target, new Operand("4",
                            Operand.OperandType.NUMBER), Operator.MUL));



                    target = new Operand(TCode.genNewT(), Operand.OperandType.ADDRESS);
                    CalculateCode calculateCode1 =
                        new CalculateCode(target, new Operand(arrayName, Operand.OperandType.ADDRESS),
                            temp, Operator.ADD);
                    intermediateVisitor.addIntermediateCode(calculateCode1);
                    return target;
                } else {
                    target = new Operand(TCode.genNewT(), Operand.OperandType.VAR);
                    calculateCode = new CalculateCode(target, src1, src2, Operator.ADD);
                    intermediateVisitor.addIntermediateCode(calculateCode);

                    Operand temp = new Operand(TCode.genNewT(), Operand.OperandType.VAR);
                    intermediateVisitor.addIntermediateCode(
                        new CalculateCode(temp, target, new Operand("4",
                            Operand.OperandType.NUMBER), Operator.MUL));

                    target = new Operand(TCode.genNewT(), Operand.OperandType.VAR);
                    MemoryCode memoryCode =
                        new MemoryCode(target, new Operand(arrayName, Operand.OperandType.ADDRESS),
                            temp, Operator.LOAD);
                    intermediateVisitor.addIntermediateCode(memoryCode);
                    return target;
                }
            } else {
                target = new Operand(TCode.genNewT(), Operand.OperandType.VAR);
                calculateCode =
                    new CalculateCode(target, src2,
                        new Operand(String.valueOf(dimensionLength), Operand.OperandType.NUMBER),
                        Operator.MUL);
                intermediateVisitor.addIntermediateCode(calculateCode);
                src2 = target;
                target = new Operand(TCode.genNewT(), Operand.OperandType.VAR);
                calculateCode = new CalculateCode(target, src1, src2, Operator.ADD);
                intermediateVisitor.addIntermediateCode(calculateCode);
                src1 = target;
            }
        }
        return null;
    }

    public Pair<Operand, Operand> generateMidCodeAndUseAsLeft(
        IntermediateVisitor intermediateVisitor) {
        //作为左值
        SymbolTableItem item =
            SymbolTable.findItemFromAllTable(ident, RecordSymbolTable);

        if (item == null) {
            System.err.println("no find ident in lvalNode");
        }

        //查符号表看声明的地方
        String name = item.getName();
        int dimension = getDimension();
        if (dimension == 0) {
            //非数组
            return new Pair<>(
                new Operand(TCode.reName(name, item.getBlockDepth()), Operand.OperandType.VAR),
                null);
        }


        //一维数组直接返回
        if (item.getDimension() == 1) {
            //一维数组，形如a[x], src1 = x
            //得到数组偏移
            Operand src2 =
                dimensionOfExp.get(0).generateMidCodeAndReturnTempVar(intermediateVisitor);
            Operand temp = new Operand(TCode.genNewT(), Operand.OperandType.VAR);
            intermediateVisitor.addIntermediateCode(
                new CalculateCode(temp, src2, new Operand("4",
                    Operand.OperandType.NUMBER), Operator.MUL));
            src2 = temp;

            Operand src1 =
                new Operand(TCode.reName(name, item.getBlockDepth()), Operand.OperandType.ADDRESS);
            return new Pair<>(src1, src2);
        }
        Operand target = new Operand(TCode.genNewT(), Operand.OperandType.VAR);
        Operand src1 = dimensionOfExp.get(0).generateMidCodeAndReturnTempVar(intermediateVisitor);
        int dimensionLength = item.getDimensionLength(1);
        CalculateCode calculateCode =
            new CalculateCode(target, src1,
                new Operand(String.valueOf(dimensionLength), Operand.OperandType.NUMBER),
                Operator.MUL
            );
        intermediateVisitor.addIntermediateCode(calculateCode);
        src1 = target;
        for (int i = 1; i < dimensionOfExp.size(); i++) {
            Operand src2 =
                dimensionOfExp.get(i).generateMidCodeAndReturnTempVar(intermediateVisitor);
            dimensionLength = item.getDimensionLength(i + 1);
            if (dimensionLength == 0) {


                target = new Operand(TCode.genNewT(), Operand.OperandType.VAR);
                calculateCode = new CalculateCode(target, src1, src2, Operator.ADD);
                intermediateVisitor.addIntermediateCode(calculateCode);

                intermediateVisitor.addIntermediateCode(
                    new CalculateCode(target, target, new Operand("4",
                        Operand.OperandType.NUMBER), Operator.MUL));

                return new Pair<>(
                    new Operand(TCode.reName(name, item.getBlockDepth()),
                        Operand.OperandType.ADDRESS),
                    target);

            } else {
                target = new Operand(TCode.genNewT(), Operand.OperandType.VAR);
                calculateCode =
                    new CalculateCode(target, src2,
                        new Operand(String.valueOf(dimensionLength), Operand.OperandType.NUMBER),
                        Operator.MUL);
                intermediateVisitor.addIntermediateCode(calculateCode);
                src2 = target;
                target = new Operand(TCode.genNewT(), Operand.OperandType.VAR);
                calculateCode = new CalculateCode(target, src1, src2, Operator.ADD);
                intermediateVisitor.addIntermediateCode(calculateCode);
                src1 = target;
            }
        }
        return null;
    }
}
