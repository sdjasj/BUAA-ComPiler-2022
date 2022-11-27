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
import Tool.Optimizer;
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
                return Operand.getNewOperand(TCode.reName(name, item.getBlockDepth()),
                    Operand.OperandType.ADDRESS);
            } else {
                //非数组
                //常量
                if (item.isConst()) {
                    return Operand.getNewOperand(String.valueOf(item.getConstVarInitVal()),
                        Operand.OperandType.NUMBER);
                }
                //变量
                return Operand.getNewOperand(TCode.reName(name, item.getBlockDepth()), Operand.OperandType.VAR);
            }
        }


        //数组取一维值直接返回
        if (dimension == 1) {
            //数组取一维值，形如a[x], src1 = x,但a可能是多维数组
            //得到数组偏移
            if (item.getDimension() > dimension) {
                Operand src2 =
                    dimensionOfExp.get(0).generateMidCodeAndReturnTempVar(intermediateVisitor);
                if (src2.isNUMBER() && Optimizer.ConstOptimizer) {
                    src2 = Operand.getNewOperand(String.valueOf(Integer.parseInt(src2.getName()) * item.getDimensionLength(1) * 4),
                        Operand.OperandType.NUMBER);
                } else {
                    Operand temp = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);

                    intermediateVisitor.addIntermediateCode(new CalculateCode(temp, src2,
                        Operand.getNewOperand(String.valueOf(item.getDimensionLength(1) * 4),
                            Operand.OperandType.NUMBER), Operator.MUL));
                    src2 = temp;
                }
                Operand target = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.ADDRESS);
                Operand src1 =
                    Operand.getNewOperand(TCode.reName(name, item.getBlockDepth()), Operand.OperandType.ADDRESS);
                if (item.isConst()) {
                    src1.setGlobal(true);
                }
                CalculateCode calculateCode = new CalculateCode(target, src1, src2, Operator.ADD);
                intermediateVisitor.addIntermediateCode(calculateCode);

                return target;
            } else {
                //变量数组
                Operand src2 =
                    dimensionOfExp.get(0).generateMidCodeAndReturnTempVar(intermediateVisitor);
                if (src2.isNUMBER() && Optimizer.ConstOptimizer) {
                    src2 = Operand.getNewOperand(String.valueOf(Integer.parseInt(src2.getName()) * 4),
                        Operand.OperandType.NUMBER);
                } else {
                    Operand temp = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
                    intermediateVisitor.addIntermediateCode(
                        new CalculateCode(temp, src2, Operand.getNewOperand("4",
                            Operand.OperandType.NUMBER), Operator.MUL));
                    src2 = temp;
                }

                Operand target = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
                Operand src1 =
                    Operand.getNewOperand(TCode.reName(name, item.getBlockDepth()), Operand.OperandType.ADDRESS);
                if (item.isConst()) {
                    src1.setGlobal(true);
                }
                MemoryCode memoryCode = new MemoryCode(target, src1, src2, Operator.LOAD);
                intermediateVisitor.addIntermediateCode(memoryCode);

                return target;
            }
        }

        //array name
        String arrayName = TCode.reName(ident.getValue(), item.getBlockDepth());
        Operand src1 = dimensionOfExp.get(0).generateMidCodeAndReturnTempVar(intermediateVisitor);
        Operand target = null;
        CalculateCode calculateCode = null;
        int dimensionLength = item.getDimensionLength(1);
        if (src1.isNUMBER() && Optimizer.ConstOptimizer) {
            target = Operand.getNewOperand(String.valueOf(Integer.parseInt(src1.getName()) * dimensionLength),
                Operand.OperandType.NUMBER);
            src1 = target;
        } else {
            target = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
            calculateCode =
                new CalculateCode(target, src1,
                    Operand.getNewOperand(String.valueOf(dimensionLength), Operand.OperandType.NUMBER),
                    Operator.MUL
                );
            intermediateVisitor.addIntermediateCode(calculateCode);
            src1 = target;
        }

        for (int i = 1; i < dimensionOfExp.size(); i++) {
            Operand src2 =
                dimensionOfExp.get(i).generateMidCodeAndReturnTempVar(intermediateVisitor);
            dimensionLength = item.getDimensionLength(i + 1);

            if (dimensionLength == 0) {
                if (item.getDimension() > dimension) {
                    if (src1.isNUMBER() && src2.isNUMBER() && Optimizer.ConstOptimizer) {
                        target = Operand.getNewOperand(String.valueOf(
                                Integer.parseInt(src1.getName()) + Integer.parseInt(src2.getName())),
                            Operand.OperandType.NUMBER);
                    } else {
                        target = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
                        calculateCode = new CalculateCode(target, src1, src2, Operator.ADD);
                        intermediateVisitor.addIntermediateCode(calculateCode);
                    }


                    Operand temp = null;
                    if (target.isNUMBER() && Optimizer.ConstOptimizer) {
                        temp = Operand.getNewOperand(String.valueOf(Integer.parseInt(
                            target.getName()) * 4), Operand.OperandType.NUMBER);
                    } else {
                        temp = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
                        intermediateVisitor.addIntermediateCode(
                            new CalculateCode(temp, target, Operand.getNewOperand("4",
                                Operand.OperandType.NUMBER), Operator.MUL));
                    }



                    target = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.ADDRESS);
                    Operand addr = Operand.getNewOperand(arrayName, Operand.OperandType.ADDRESS);
                    if (item.isConst()) {
                        addr.setGlobal(true);
                    }
                    CalculateCode calculateCode1 =
                        new CalculateCode(target, addr, temp, Operator.ADD);
                    intermediateVisitor.addIntermediateCode(calculateCode1);

                    return target;
                } else {
                    if (src1.isNUMBER() && src2.isNUMBER() && Optimizer.ConstOptimizer) {
                        target = Operand.getNewOperand(String.valueOf(
                                Integer.parseInt(src1.getName()) + Integer.parseInt(src2.getName())),
                            Operand.OperandType.NUMBER);
                    } else {
                        target = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
                        calculateCode = new CalculateCode(target, src1, src2, Operator.ADD);
                        intermediateVisitor.addIntermediateCode(calculateCode);
                    }
                    Operand temp = null;
                    if (target.isNUMBER() && Optimizer.ConstOptimizer) {
                        temp = Operand.getNewOperand(String.valueOf(Integer.parseInt(
                            target.getName()) * 4), Operand.OperandType.NUMBER);
                    } else {
                        temp = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
                        intermediateVisitor.addIntermediateCode(
                            new CalculateCode(temp, target, Operand.getNewOperand("4",
                                Operand.OperandType.NUMBER), Operator.MUL));
                    }


                    target = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
                    Operand addr = Operand.getNewOperand(arrayName, Operand.OperandType.ADDRESS);
                    if (item.isConst()) {
                        addr.setGlobal(true);
                    }
                    MemoryCode memoryCode =
                        new MemoryCode(target, addr, temp, Operator.LOAD);
                    intermediateVisitor.addIntermediateCode(memoryCode);

                    return target;
                }
            } else {
                if (src2.isNUMBER() && Optimizer.ConstOptimizer) {
                    target = Operand.getNewOperand(String.valueOf(Integer.parseInt(src2.getName()) * dimensionLength),
                        Operand.OperandType.NUMBER);
                    src2 = target;
                } else {
                    target = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
                    calculateCode =
                        new CalculateCode(target, src2,
                            Operand.getNewOperand(String.valueOf(dimensionLength), Operand.OperandType.NUMBER),
                            Operator.MUL);
                    intermediateVisitor.addIntermediateCode(calculateCode);
                    src2 = target;
                }

                if (src1.isNUMBER() && src2.isNUMBER() && Optimizer.ConstOptimizer) {
                    target = Operand.getNewOperand(String.valueOf(
                            Integer.parseInt(src1.getName()) + Integer.parseInt(src2.getName())),
                        Operand.OperandType.NUMBER);
                    src1 = target;
                } else {
                    target = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
                    calculateCode = new CalculateCode(target, src1, src2, Operator.ADD);
                    intermediateVisitor.addIntermediateCode(calculateCode);
                    src1 = target;
                }
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
                Operand.getNewOperand(TCode.reName(name, item.getBlockDepth()), Operand.OperandType.VAR),
                null);
        }


        //一维数组直接返回
        if (item.getDimension() == 1) {
            //一维数组，形如a[x], src1 = x
            //得到数组偏移
            Operand src2 =
                dimensionOfExp.get(0).generateMidCodeAndReturnTempVar(intermediateVisitor);
            if (src2.isNUMBER() && Optimizer.ConstOptimizer) {
                src2 = Operand.getNewOperand(String.valueOf(Integer.parseInt(src2.getName()) * 4),
                    Operand.OperandType.NUMBER);
            } else {
                Operand temp = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
                intermediateVisitor.addIntermediateCode(
                    new CalculateCode(temp, src2, Operand.getNewOperand("4",
                        Operand.OperandType.NUMBER), Operator.MUL));
                src2 = temp;
            }

            Operand src1 = Operand.getNewOperand(TCode.reName(name, item.getBlockDepth()),
                Operand.OperandType.ADDRESS);

            if (item.isConst()) {
                src1.setGlobal(true);
            }

            return new Pair<>(src1, src2);
        }
        Operand src1 = dimensionOfExp.get(0).generateMidCodeAndReturnTempVar(intermediateVisitor);
        Operand target = null;
        CalculateCode calculateCode = null;
        int dimensionLength = item.getDimensionLength(1);
        if (src1.isNUMBER() && Optimizer.ConstOptimizer) {
            target = Operand.getNewOperand(String.valueOf(Integer.parseInt(src1.getName()) * dimensionLength),
                Operand.OperandType.NUMBER);
            src1 = target;
        } else {
            target = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
            calculateCode =
                new CalculateCode(target, src1,
                    Operand.getNewOperand(String.valueOf(dimensionLength), Operand.OperandType.NUMBER),
                    Operator.MUL
                );
            intermediateVisitor.addIntermediateCode(calculateCode);
            src1 = target;
        }

        for (int i = 1; i < dimensionOfExp.size(); i++) {
            Operand src2 =
                dimensionOfExp.get(i).generateMidCodeAndReturnTempVar(intermediateVisitor);
            dimensionLength = item.getDimensionLength(i + 1);
            if (dimensionLength == 0) {
                if (src1.isNUMBER() && src2.isNUMBER() && Optimizer.ConstOptimizer) {
                    target = Operand.getNewOperand(String.valueOf(
                            Integer.parseInt(src1.getName()) + Integer.parseInt(src2.getName())),
                        Operand.OperandType.NUMBER);
                } else {
                    target = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
                    calculateCode = new CalculateCode(target, src1, src2, Operator.ADD);
                    intermediateVisitor.addIntermediateCode(calculateCode);
                }

                if (target.isNUMBER() && Optimizer.ConstOptimizer) {
                    target = Operand.getNewOperand(
                        String.valueOf(Integer.parseInt(target.getName()) * 4),
                        Operand.OperandType.NUMBER);
                } else {
                    Operand temp = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
                    intermediateVisitor.addIntermediateCode(
                        new CalculateCode(temp, target, Operand.getNewOperand("4",
                            Operand.OperandType.NUMBER), Operator.MUL));
                    target = temp;
                }
                Operand addr = Operand.getNewOperand(TCode.reName(name, item.getBlockDepth()),
                    Operand.OperandType.ADDRESS);
                if (item.isConst()) {
                    addr.setGlobal(true);
                }
                return new Pair<>(addr, target);

            } else {
                if (src2.isNUMBER() && Optimizer.ConstOptimizer) {
                    target = Operand.getNewOperand(String.valueOf(Integer.parseInt(src2.getName()) * dimensionLength),
                        Operand.OperandType.NUMBER);
                    src2 = target;
                } else {
                    target = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
                    calculateCode =
                        new CalculateCode(target, src2,
                            Operand.getNewOperand(String.valueOf(dimensionLength), Operand.OperandType.NUMBER),
                            Operator.MUL);
                    intermediateVisitor.addIntermediateCode(calculateCode);
                    src2 = target;
                }

                if (src1.isNUMBER() && src2.isNUMBER() && Optimizer.ConstOptimizer) {
                    target = Operand.getNewOperand(String.valueOf(
                            Integer.parseInt(src1.getName()) + Integer.parseInt(src2.getName())),
                        Operand.OperandType.NUMBER);
                    src1 = target;
                } else {
                    target = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
                    calculateCode = new CalculateCode(target, src1, src2, Operator.ADD);
                    intermediateVisitor.addIntermediateCode(calculateCode);
                    src1 = target;
                }
            }
        }
        return null;
    }
}
