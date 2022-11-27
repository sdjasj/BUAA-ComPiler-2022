package SyntaxTree;


import IntermediateCode.AllCode.AssignCode;
import IntermediateCode.AllCode.InputCode;
import IntermediateCode.AllCode.JumpCode;
import IntermediateCode.AllCode.LabelCode;
import IntermediateCode.AllCode.MemoryCode;
import IntermediateCode.AllCode.OutputCode;
import IntermediateCode.FunctionCode.ExitCode;
import IntermediateCode.FunctionCode.FunctionReturnCode;
import IntermediateCode.GlobalDecl;
import IntermediateCode.GlobalStrDecl;
import IntermediateCode.IntermediateVisitor;
import IntermediateCode.Operand;
import IntermediateCode.Operator;
import IntermediateCode.TCode;
import Lexer.Token;
import Tool.Optimizer;
import Tool.Pair;

import java.util.ArrayList;

//语句 Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
// | [Exp] ';' //有无Exp两种情况
// | Block
// | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
// | 'while' '(' Cond ')' Stmt
// | 'break' ';'
// | 'continue' ';'
// | 'return' [Exp] ';' // 1.有Exp 2.无Exp
// | LVal '=' 'getint''('')'';'
// | 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
public class StmtNode extends ParserNode {
    public static final int LVAL_EXP = 1, EXP_SEMICN = 2, BLOCK = 3, IF = 4, WHILE = 5, BREAK = 6,
        CONTINUE = 7, RETRUN = 8, LVAL_GETINT = 9, PRINTF = 10;
    private int stmtType;
    private LValNode lValNode;
    private ExpNode expNode;
    private BlockNode blockNode;
    private CondNode ifCondNode;
    private StmtNode ifStmtNode;
    private StmtNode elseStmtNode;
    private CondNode whileCondNode;
    private StmtNode whileStmtNode;
    //break and continue is already in stmtType
    //getint is already in stmtType
    private Token formatString;
    private ArrayList<ExpNode> formatStringExp;

    public StmtNode(int stmtType, LValNode lValNode, ExpNode expNode, BlockNode blockNode,
                    CondNode ifCondNode, StmtNode ifStmtNode, StmtNode elseStmtNode,
                    CondNode whileCondNode, StmtNode whileStmtNode, Token formatString,
                    ArrayList<ExpNode> formatStringExp) {
        this.stmtType = stmtType;
        this.lValNode = lValNode;
        this.expNode = expNode;
        this.blockNode = blockNode;
        this.ifCondNode = ifCondNode;
        this.ifStmtNode = ifStmtNode;
        this.elseStmtNode = elseStmtNode;
        this.whileCondNode = whileCondNode;
        this.whileStmtNode = whileStmtNode;
        this.formatString = formatString;
        this.formatStringExp = formatStringExp;
    }

    public static StmtNode generateLvalExp(LValNode lValNode, ExpNode expNode) {
        return new StmtNode(StmtNode.LVAL_EXP, lValNode, expNode, null, null, null, null, null,
            null, null, null);
    }

    public static StmtNode generateExpAndSemicn(ExpNode expNode) {
        //expnode为null说明stmt只有;
        return new StmtNode(StmtNode.EXP_SEMICN, null, expNode, null, null, null, null, null, null,
            null, null);
    }

    public static StmtNode generateBlock(BlockNode blockNode) {
        return new StmtNode(StmtNode.BLOCK, null, null, blockNode, null, null, null, null, null,
            null, null);
    }

    public static StmtNode generateIf(CondNode condNode, StmtNode stmtNode, StmtNode elseStmtNode) {
        return new StmtNode(StmtNode.IF, null, null, null, condNode, stmtNode, elseStmtNode, null, null,
            null, null);
    }

    public static StmtNode generateWhile(CondNode condNode, StmtNode stmtNode) {
        return new StmtNode(StmtNode.WHILE, null, null, null, null, null, null, condNode, stmtNode,
            null, null);
    }

    public static StmtNode generateBreak() {
        return new StmtNode(StmtNode.BREAK, null, null, null, null, null, null, null, null, null,
            null);
    }

    public static StmtNode generateContinue() {
        return new StmtNode(StmtNode.CONTINUE, null, null, null, null, null, null, null, null, null,
            null);
    }

    public static StmtNode generateReturn(ExpNode expNode) {
        //exp == null ---> void
        return new StmtNode(StmtNode.RETRUN, null, expNode, null, null, null, null, null, null,
            null, null);
    }

    public static StmtNode generateGetInt(LValNode lValNode) {
        return new StmtNode(StmtNode.LVAL_GETINT, lValNode, null, null, null, null, null, null,
            null, null, null);
    }

    public static StmtNode generatePrintf(Token formatString, ArrayList<ExpNode> formatStringExp) {
        return new StmtNode(StmtNode.PRINTF, null, null, null, null, null, null, null, null,
            formatString, formatStringExp);
    }

    public boolean intFuncHasReturnInTheLastStmt() {
        return stmtType == RETRUN;
    }

    public void generateIntermediate(IntermediateVisitor intermediateVisitor,
                                     Pair<String, String> loop) {
        if (stmtType == LVAL_EXP) {
            //语句 Stmt → LVal '=' Exp ';'
            Operand src1 = expNode.generateMidCodeAndReturnTempVar(intermediateVisitor);
            Pair<Operand, Operand> target =
                lValNode.generateMidCodeAndUseAsLeft(intermediateVisitor);
            if (target.getFirst().getOperandType() == Operand.OperandType.ADDRESS) {
                //数组作为左值,需要访存
                MemoryCode memoryCode =
                    new MemoryCode(src1, target.getFirst(), target.getSecond(), Operator.STORE);
                intermediateVisitor.addIntermediateCode(memoryCode);
            } else {
                AssignCode assignCode = new AssignCode(target.getFirst(), src1);
                intermediateVisitor.addIntermediateCode(assignCode);
            }
        } else if (stmtType == EXP_SEMICN) {
            //只有;
            if (expNode == null) {
                return;
            }
            //exp;
            expNode.generateMidCodeAndReturnTempVar(intermediateVisitor);
        } else if (stmtType == BLOCK) {
            blockNode.generateIntermediate(intermediateVisitor, loop);
        } else if (stmtType == RETRUN) {
            if (intermediateVisitor.isMainFuncNow()) {
                ExitCode exitCode = new ExitCode();
                intermediateVisitor.addIntermediateCode(exitCode);
                return;
            }
            if (expNode == null) {
                FunctionReturnCode functionReturnCode = new FunctionReturnCode();
                intermediateVisitor.addIntermediateCode(functionReturnCode);
            } else {
                Operand target = expNode.generateMidCodeAndReturnTempVar(intermediateVisitor);
                FunctionReturnCode functionReturnCode = new FunctionReturnCode(target);
                intermediateVisitor.addIntermediateCode(functionReturnCode);
            }
        } else if (stmtType == LVAL_GETINT) {
            Operand src1 = Operand.getNewOperand(TCode.genNewT(), Operand.OperandType.VAR);
            InputCode inputCode = new InputCode(src1);
            intermediateVisitor.addIntermediateCode(inputCode);
            //判断lval是否为数组 TODO
            Pair<Operand, Operand> target =
                lValNode.generateMidCodeAndUseAsLeft(intermediateVisitor);
            if (target.getFirst().getOperandType() == Operand.OperandType.ADDRESS) {
                //数组作为左值,需要访存
                MemoryCode memoryCode =
                    new MemoryCode(src1, target.getFirst(), target.getSecond(), Operator.STORE);
                intermediateVisitor.addIntermediateCode(memoryCode);
            } else {
                AssignCode assignCode = new AssignCode(target.getFirst(), src1);
                intermediateVisitor.addIntermediateCode(assignCode);
            }
        } else if (stmtType == PRINTF) {
            String[] outputStrings =
                formatString.getValue().substring(1, formatString.getValue().length() - 1)
                    .split("((?<=%d)|(?=%d)|(?<=\\\\n))|(?=\\\\n)");
            int idx = 0;
            ArrayList<Operand> expOutputList = new ArrayList<>();
            for (ExpNode node : formatStringExp) {
                expOutputList.add(node.generateMidCodeAndReturnTempVar(intermediateVisitor));
            }
            for (String outputString : outputStrings) {
                if (outputString.equals("%d")) {
                    Operand target = expOutputList.get(idx);
                    idx++;
                    OutputCode outputCode = new OutputCode(target, true);
                    intermediateVisitor.addIntermediateCode(outputCode);
                } else {
                    String name = TCode.genNewStr();
                    GlobalStrDecl globalStrDecl =
                        new GlobalStrDecl(GlobalDecl.StoreType.str_, name, true,
                            outputString);
                    intermediateVisitor.addGlobalDecl(globalStrDecl);
                    OutputCode outputCode =
                        new OutputCode(Operand.getNewOperand(name, Operand.OperandType.ADDRESS), false);
                    intermediateVisitor.addIntermediateCode(outputCode);
                }
            }
        } else if (stmtType == IF) {
            String falseLable = TCode.genNewLable();
            String elseEndLable = null;
            if (elseStmtNode != null) {
                elseEndLable = TCode.genNewLable();
            }
            //cond
            ifCondNode.generateIntermediate(intermediateVisitor, null, falseLable);
            //stmt
            ifStmtNode.generateIntermediate(intermediateVisitor, loop);
            if (elseStmtNode != null) {
                intermediateVisitor.addIntermediateCode(
                    new JumpCode(Operand.getNewOperand(elseEndLable, Operand.OperandType.ADDRESS),
                        Operator.JUMP));
            }
            //falseLable
            intermediateVisitor.addIntermediateCode(new LabelCode(falseLable));
            //elseStmt
            if (elseStmtNode != null) {
                elseStmtNode.generateIntermediate(intermediateVisitor, loop);
                intermediateVisitor.addIntermediateCode(new LabelCode(elseEndLable));
            }
        } else if (stmtType == WHILE) {
            if (Optimizer.BranchOptimizer) {
                String falseLabel = TCode.genNewLable();
                String beginLabel = TCode.genNewLable();
                String headLabel = TCode.genNewLable();
                whileCondNode.generateIntermediate(intermediateVisitor, null, falseLabel);
                intermediateVisitor.addIntermediateCode(new LabelCode(beginLabel));
                whileStmtNode.generateIntermediate(intermediateVisitor,
                    new Pair<>(headLabel, falseLabel));
                intermediateVisitor.addIntermediateCode(new LabelCode(headLabel));
                whileCondNode.generateIntermediate(intermediateVisitor, beginLabel, null);
//            intermediateVisitor.addIntermediateCode(
//                new JumpCode(Operand.getNewOperand(beginLabel, Operand.OperandType.ADDRESS), Operator.JUMP));
                intermediateVisitor.addIntermediateCode(new LabelCode(falseLabel));
            } else {
                String falseLabel = TCode.genNewLable();
                String beginLabel = TCode.genNewLable();
                intermediateVisitor.addIntermediateCode(new LabelCode(beginLabel));
                whileCondNode.generateIntermediate(intermediateVisitor, null, falseLabel);
                whileStmtNode.generateIntermediate(intermediateVisitor,
                    new Pair<>(beginLabel, falseLabel));
                intermediateVisitor.addIntermediateCode(
                    new JumpCode(Operand.getNewOperand(beginLabel, Operand.OperandType.ADDRESS),
                        Operator.JUMP));
                intermediateVisitor.addIntermediateCode(new LabelCode(falseLabel));
            }
        } else if (stmtType == CONTINUE) {
            String beginLoopLabel = loop.getFirst();
            intermediateVisitor.addIntermediateCode(
                new JumpCode(Operand.getNewOperand(beginLoopLabel, Operand.OperandType.ADDRESS),
                    Operator.JUMP));
        } else if (stmtType == BREAK) {
            String endLoopLabel = loop.getSecond();
            intermediateVisitor.addIntermediateCode(
                new JumpCode(Operand.getNewOperand(endLoopLabel, Operand.OperandType.ADDRESS),
                    Operator.JUMP));
        }

    }
}
