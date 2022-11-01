package ErrorTool;

import Lexer.Token;
import MySymbolTable.SymbolTable;
import MySymbolTable.SymbolTableItem;
import MySymbolTable.SymbolType;
import SyntaxTree.FuncRParamsNode;

import java.util.ArrayList;
import java.util.HashMap;

public class ErrorTool {
    public ErrorTool(){}

    public static void printError(String code, Token ident) {
        //System.err.println(ident.getLine() + " " + code + " " + ident.getValue());
        System.err.println(ident.getLine() + " " + code);
    }

    public static void checkA(Token token) {
        String s = token.getValue();
        for (int i = 1; i < s.length() - 1; i++) {
            int idx = s.charAt(i);
            if (idx == 32 || idx == 33 || (40 <= idx && idx <= 126 && idx != 92)) {
                continue;
            }
            if (s.charAt(i) == '%') {
                if (i + 1 < s.length() - 1 && s.charAt(i + 1) == 'd') {
                    continue;
                } else {
                    printError("a", token);
                    return;
                }
            }
            if (s.charAt(i) == '\\') {
                if (i + 1 < s.length() - 1 && s.charAt(i + 1) == 'n') {
                    continue;
                } else {
                    printError("a", token);
                    return;
                }
            }
            printError("a", token);
            return;
        }
    }

    public static boolean checkB(Token ident, SymbolTable symbolTable) {
        if (symbolTable.getItems().containsKey(ident.getValue())) {
            printError("b", ident);
            return false;
        }
        return true;
    }

    public static boolean checkC(Token ident, SymbolTable symbolTable) {
        SymbolTable mySymbolTable = symbolTable;
        while (mySymbolTable != null) {
            HashMap<String, SymbolTableItem> items = mySymbolTable.getItems();
            if (!items.containsKey(ident.getValue()) || items.get(ident.getValue()).getLine() >
                ident.getLine()) {
                mySymbolTable = mySymbolTable.getParentTable();
                continue;
            }
            return true;
        }
        ErrorTool.printError("c", ident);
        return false;
    }

    public static boolean checkD(Token ident, int paramsNum, SymbolTable symbolTable) {
        //一行只有一个错误
        SymbolTableItem symbolTableItem = SymbolTable.findItemFromAllTable(ident, symbolTable);
        if (symbolTableItem == null) {
            return false;
        }
        if (symbolTableItem.getType() != SymbolType.FUNC_INT && symbolTableItem.getType() != SymbolType.FUNC_VOID) {
            System.out.println("one line may has two error in checkD of " + ident.getLine());
            return false;
        }
//        System.err.println(ident.getValue());
//        System.err.println(paramsNum + " " + symbolTableItem.getFuncParamsLength());
//        ArrayList<SymbolTableItem> arrayList = symbolTableItem.getFuncFParams();
//        if (arrayList != null) {
//            for (SymbolTableItem item : arrayList) {
//                System.err.println(item.getName());
//            }
//        }
        if (paramsNum != symbolTableItem.getFuncParamsLength()) {
            ErrorTool.printError("d", ident);
            return false;
        }
        return true;
    }

    public static boolean checkE(Token ident, FuncRParamsNode funcRParamsNode,
                                 SymbolTable symbolTable) {
        //找到符号表中函数定义definedFunc
        SymbolTableItem definedFunc =
            SymbolTable.findItemFromAllTable(ident, symbolTable);
        if (definedFunc == null) {
            return false;
        }
        if (definedFunc.getType() != SymbolType.FUNC_INT &&
            definedFunc.getType() != SymbolType.FUNC_VOID) {

            System.out.println("error in checkE of " + ident);
            return false;
        }

        //检查实参EXP类型
        //实参
        ArrayList<FuncRealParamsType> funcRealParams =
            funcRParamsNode == null ? null : funcRParamsNode.getFuncRealParamsType(symbolTable);
        //形参
        ArrayList<SymbolTableItem> funcFormParams = definedFunc.getFuncFParams();
        if (funcRealParams == null) {
            if (funcFormParams != null) {
                printError("e", ident);
                return false;
            }
            return true;
        }
        //形参和实参数量不等
        if (funcRealParams.size() != funcFormParams.size()) {
            System.out.println("error in checkE of params length " + ident.getLine());
        }
        //逐一比较形参和实参类型
        for (int i = 0; i < funcRealParams.size(); i++) {
            FuncRealParamsType funcRealParam = funcRealParams.get(i);
            SymbolTableItem funcFormParam = funcFormParams.get(i);
            if (funcRealParam == null) {
                return false;
            }
            if (funcRealParam.getFuncType() == SymbolType.NONE) {
                printError("e", ident);
                return false;
            }
            //普通变量
            if (funcRealParam.getFuncType() == SymbolType.VAR) {
                if (funcFormParam.getType() != SymbolType.VAR) {
                    printError("e", ident);
                    return false;
                }
            } else if (funcRealParam.getFuncType() == SymbolType.ARRAY) { //数组
                if (funcFormParam.getType() != SymbolType.ARRAY ||
                    funcFormParam.getDimension() != funcRealParam.getDimension()) {
                    printError("e", ident);
                    return false;
                }
                if (funcFormParam.getDimension() == 2) { //二维数组检查实参第二维长度是否与形参一致
                    if (funcRealParam.getTwoDimensionLength() !=
                        funcFormParam.getTwoDimensionLength()) {
                        printError("e", ident);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static void checkF(Token ident) {
        printError("f", ident);
    }

    public static void checkG(Token ident) {
        printError("g", ident);
    }

    public static void checkH(Token ident, SymbolTable symbolTable) {
        SymbolTableItem item =
            SymbolTable.findItemFromAllTable(ident, symbolTable);
        if (item == null) {
            return;
        }
        if (item.getType() == SymbolType.CONST_VAR || item.getType() == SymbolType.CONST_ARRAY) {
            printError("h", ident);
        }
    }

    public static void checkI(Token ident) {
        printError("i", ident);
    }

    public static void checkJ(Token ident) {
        printError("j", ident);
    }

    public static void checkK(Token ident) {
        printError("k", ident);
    }

    public static void checkL(Token ident) {
        printError("l", ident);
    }

    public static void checkM(Token ident) {
        printError("m", ident);
    }
}
