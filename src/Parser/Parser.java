package Parser;

import ErrorTool.ErrorTool;
import Lexer.Token;
import Lexer.TokenType;
import MySymbolTable.SymbolTable;
import MySymbolTable.SymbolTableItem;
import MySymbolTable.SymbolType;
import SyntaxTree.AddExpNode;
import SyntaxTree.BTypeNode;
import SyntaxTree.BlockItemNode;
import SyntaxTree.BlockNode;
import SyntaxTree.CompUnitNode;
import SyntaxTree.CondNode;
import SyntaxTree.ConstDeclNode;
import SyntaxTree.ConstDefNode;
import SyntaxTree.ConstExpNode;
import SyntaxTree.ConstInitValNode;
import SyntaxTree.DeclNode;
import SyntaxTree.EqExpNode;
import SyntaxTree.ExpNode;
import SyntaxTree.FuncDefNode;
import SyntaxTree.FuncFParamNode;
import SyntaxTree.FuncFParamsNode;
import SyntaxTree.FuncRParamsNode;
import SyntaxTree.FuncTypeNode;
import SyntaxTree.InitValNode;
import SyntaxTree.LAndExpNode;
import SyntaxTree.LOrExpNode;
import SyntaxTree.LValNode;
import SyntaxTree.MainFuncDefNode;
import SyntaxTree.MulExpNode;
import SyntaxTree.NumberNode;
import SyntaxTree.PrimaryExpNode;
import SyntaxTree.RelExpNode;
import SyntaxTree.StmtNode;
import SyntaxTree.UnaryExpNode;
import SyntaxTree.VarDeclNode;
import SyntaxTree.VarDefNode;

import java.io.IOException;
import java.util.ArrayList;

public class Parser {
    private boolean outputParser;
    private ArrayList<Token> tokens;
    private int curPos;
    private int cycleDepth; //循环嵌套层数
    private int blockDepth; //Block深度，为1时在函数内的最外层
    private SymbolTable symbolTable;
    private CurrentStmtDomain currentStmtDomain;
    private boolean funcOfIntHasReturnInTheLast;

    public Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        this.curPos = 0;
        this.cycleDepth = 0;
        this.currentStmtDomain = CurrentStmtDomain.GLOBAL;
        this.blockDepth = 0;
        this.funcOfIntHasReturnInTheLast = false;
    }

    public void nextToken() {
        curPos++;
    }

    public Token getToken() {
        return curPos >= tokens.size() ? null : tokens.get(curPos);
    }

    //获得后面第k个词法单元
    public Token getNextKToken(int k) {
        if (curPos + k < tokens.size()) {
            return tokens.get(curPos + k);
        } else {
            return null;
        }
    }

    public Token getPreToken() {
        if (curPos == 0) {
            return null;
        }
        return tokens.get(curPos - 1);
    }

    public boolean isVaildPos() {
        return 0 <= curPos && curPos <= tokens.size() - 1;
    }

    public boolean isPlusOrMinu() {
        return getToken().getTokenType() == TokenType.PLUS ||
            getToken().getTokenType() == TokenType.MINU;
    }

    public boolean isMultOrDivOrMod() {
        TokenType curTokeType = getToken().getTokenType();
        return curTokeType == TokenType.MULT || curTokeType == TokenType.DIV ||
            curTokeType == TokenType.MOD;
    }

    public int getCurPos() {
        return curPos;
    }

    public void setCurPos(int curPos) {
        this.curPos = curPos;
    }

    public void setOutputParser(boolean outputParser) {
        this.outputParser = outputParser;
    }

    public void setSymbolTable(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    //CompUnit → {Decl} {FuncDef} MainFuncDef
    public CompUnitNode parseCompUnit() throws IOException, ClassNotFoundException {
        ArrayList<DeclNode> declNodes = new ArrayList<>();
        ArrayList<FuncDefNode> funcDefNodes = new ArrayList<>();
        while (getNextKToken(1).getTokenType() != TokenType.MAINTK) {
            if (getNextKToken(2).getTokenType() == TokenType.LPARENT) {
                FuncDefNode funcDefNode = parseFuncDef();
                funcDefNodes.add(funcDefNode);
            } else {
                DeclNode declNode = parseDecl();
                declNodes.add(declNode);
            }
        }
        MainFuncDefNode mainFuncDefNode = parseMainFuncDefNode();
        CompUnitNode compUnitNode = new CompUnitNode(declNodes, funcDefNodes, mainFuncDefNode);

        if (outputParser) {
            System.out.println("<CompUnit>");
        }

        compUnitNode.setBlockDepth(blockDepth);

        return compUnitNode;
    }

    //Decl → ConstDecl | VarDecl
    public DeclNode parseDecl() throws IOException, ClassNotFoundException {
        DeclNode declNode;
        if (getToken().getTokenType() == TokenType.INTTK) {
            declNode = new DeclNode(parseVarDecl());
        } else {
            declNode = new DeclNode(parseConstDeclNode());
        }
        declNode.setBlockDepth(blockDepth);
        return declNode;
    }

    public boolean isFuncFParamsFirst() {
        return getToken().getTokenType() == TokenType.INTTK;
    }

    //FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    public FuncDefNode parseFuncDef() throws IOException, ClassNotFoundException {
        FuncTypeNode funcTypeNode = parseFuncType();
        if (!isIdent()) {
            System.out.println("error in parseFuncDef of Ident");
        }


        Token ident = parseIdent();
        boolean noErrorB = ErrorTool.checkB(ident, symbolTable);

        SymbolTable parentTable = this.symbolTable;
        this.symbolTable = symbolTable.generateChildTable(); //函数参数在子符号表中

        FuncFParamsNode funcFParamsNode;
        if (!isLPARENT()) {
            System.out.println("error in parseFuncDef of (");
        }

        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        }


        nextToken();
        if (!isFuncFParamsFirst()) { // 没有形参
            if (isRPARENT()) {
                funcFParamsNode = null;

                if (outputParser) {
                    System.out.println(getToken().getTokenType() + " " + getToken().getValue());
                } //输出词法成分

                nextToken();
            } else {
                ErrorTool.checkJ(getPreToken());
                funcFParamsNode = null;
            }
        } else { //不是),是函数参数
            funcFParamsNode = parseFuncFParams();
            if (!isRPARENT()) { //匹配完参数没有匹配右括号
                ErrorTool.checkJ(getPreToken());
            } else { //匹配了右括号

                if (outputParser) {
                    System.out.println(getToken().getTokenType() + " " + getToken().getValue());
                } //输出词法成分

                nextToken();
            }
        }

        //将形参加入子符号表后恢复符号表，再将函数名加入当前符号表，再将当前符号表变为子表

        SymbolTable child = this.symbolTable;
        this.symbolTable = parentTable; //换回函数名所在表
        if (noErrorB) {
            ArrayList<SymbolTableItem> funcFParams = new ArrayList<>(child.getOrderedItems());
            SymbolTableItem symbolTableItem = new SymbolTableItem(ident,
                funcTypeNode.getFuncType() == TokenType.VOIDTK ? SymbolType.FUNC_VOID :
                    SymbolType.FUNC_INT, funcTypeNode,
                funcFParams.size() == 0 ? null : funcFParams);
            symbolTableItem.setBlockDepth(blockDepth);
            symbolTable.addItem(symbolTableItem);
        }
        this.symbolTable = child; //换回参数所在表给Block

        //标记当前函数类型
        if (funcTypeNode.getFuncType() == TokenType.VOIDTK) {
            currentStmtDomain = CurrentStmtDomain.VOID_FUNC;
        } else if (funcTypeNode.getFuncType() == TokenType.INTTK) {
            currentStmtDomain = CurrentStmtDomain.INT_FUNC;
            funcOfIntHasReturnInTheLast = false;
        }
        BlockNode blockNode = parseBlock(false);
        if (funcTypeNode.getFuncType() == TokenType.INTTK && !funcOfIntHasReturnInTheLast) {
            ErrorTool.checkG(getPreToken());
        }
        //解除标记
        currentStmtDomain = CurrentStmtDomain.GLOBAL;
        funcOfIntHasReturnInTheLast = false;


        this.symbolTable = parentTable; //解析完Block，换回父表

        if (outputParser) {
            System.out.println("<FuncDef>");
        }

        FuncDefNode funcDefNode = new FuncDefNode(funcTypeNode, ident, funcFParamsNode, blockNode);
        funcDefNode.setBlockDepth(blockDepth);
        return funcDefNode;
    }

    //Block → '{' { BlockItem } '}'
    public BlockNode parseBlock(boolean needNewTable) throws IOException, ClassNotFoundException {
        this.blockDepth++;
        SymbolTable parent = this.symbolTable;
        if (needNewTable) {
            this.symbolTable = symbolTable.generateChildTable();
        }

        if (!isLBRACE()) {
            System.out.println("error in parseBlock of {");
        } else {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
        }

        BlockNode blockNode = new BlockNode();
        while (!isRBRACE()) {
            blockNode.addBlockItemNode(parseBlockItem());
        }

        if (!isRBRACE()) {
            System.out.println("error in parseBlock of }");
        } else {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
        }

        //判断int类型函数最后一个stmt是否是return
        if (currentStmtDomain == CurrentStmtDomain.INT_FUNC && blockDepth == 1) {
            if (blockNode.blockOfIntFuncHasReturnInTheLastStmt()) {
                funcOfIntHasReturnInTheLast = true;
            }
        }

        if (outputParser) {
            System.out.println("<Block>");
        }

        if (needNewTable) {
            this.symbolTable = parent;
        }

        this.blockDepth--;
        return blockNode;
    }

    //BlockItem → Decl | Stmt
    public BlockItemNode parseBlockItem() throws IOException, ClassNotFoundException {
        BlockItemNode blockItemNode;
        if (getToken().getTokenType() == TokenType.CONSTTK ||
            getToken().getTokenType() == TokenType.INTTK) {
            blockItemNode = new BlockItemNode(parseDecl());
        } else {
            blockItemNode = new BlockItemNode(parseStmtNode());
        }
        return blockItemNode;
    }

    //检查是否是Lval '='
    public int checkIsLvalAndAssign() {
        int oldPos = getCurPos();
        if (!isIdent()) {
            return -1;
        }
        int k = 0;
        nextToken();
        k++;
        while (isLBRACK()) {
            while (isVaildPos() && !isRBRACK() && !isSEMICN() && !isASSIGN()) {
                nextToken();
                k++;
            }
            if (!isVaildPos()) {
                setCurPos(oldPos);
                return -1;
            }
            if (isRBRACK()) {
                k++;
                nextToken();
                continue;
            }
            if (isASSIGN()) {
                setCurPos(oldPos);
                return k;
            } else {
                setCurPos(oldPos);
                return -1;
            }
        }
        if (isASSIGN()) {
            setCurPos(oldPos);
            return k;
        }
        setCurPos(oldPos);
        return -1;
    }

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
    public StmtNode parseStmtNode() throws IOException, ClassNotFoundException {

        StmtNode stmtNode;

        if (getToken().getTokenType() == TokenType.IFTK) {
            stmtNode = analyzeIf();
        } else if (isLBRACE()) {
            stmtNode = analyzeBlock();
        } else if (getToken().getTokenType() == TokenType.WHILETK) {
            stmtNode = analyzeWhile();
        } else if (getToken().getTokenType() == TokenType.BREAKTK) {
            stmtNode = analyzeBreak();
        } else if (getToken().getTokenType() == TokenType.CONTINUETK) {
            stmtNode = analyzeContinue();
        } else if (getToken().getTokenType() == TokenType.RETURNTK) {
            stmtNode = analyzeReturn();
        } else if (getToken().getTokenType() == TokenType.PRINTFTK) {
            stmtNode = analyzePrintf();
        } else {
            int pos = getCurPos();
            if (isIdent() && getNextKToken(1).getTokenType() != TokenType.LPARENT) {
                checkLValNode();
                if (isASSIGN()) {
                    if (getNextKToken(1).getTokenType() == TokenType.GETINTTK) {
                        setCurPos(pos);
                        stmtNode = analyzeGetInt();
                    } else {
                        setCurPos(pos);
                        stmtNode = analyzeLvalAndExp();
                    }
                } else {
                    setCurPos(pos);
                    stmtNode = analyzeExpAndSemicn();
                }
            } else {
                stmtNode = analyzeExpAndSemicn();
            }
        }

        if (outputParser) {
            System.out.println("<Stmt>");
        }

        stmtNode.setBlockDepth(blockDepth);
        return stmtNode;
    }

    // | Block
    public StmtNode analyzeBlock() throws IOException, ClassNotFoundException {
        if (!isLBRACE()) {
            System.out.println("error in analyzeBlock");
        }

        return StmtNode.generateBlock(parseBlock(true));
    }

    //语句 Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
    public StmtNode analyzeLvalAndExp() {
        LValNode lValNode = parseLValNode();
        if (getToken().getTokenType() != TokenType.ASSIGN) {
            System.out.println("error in analyzeLvalAndExp of =");
        }

        ErrorTool.checkH(lValNode.getIdent(), symbolTable);

        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        } //输出词法成分

        nextToken();
        ExpNode expNode = parseExp();
        if (!isSEMICN()) {
            ErrorTool.checkI(getPreToken());
        } else {
            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
        }
        return StmtNode.generateLvalExp(lValNode, expNode);
    }

    // | [Exp] ';' //有无Exp两种情况
    public StmtNode analyzeExpAndSemicn() {
        if (isSEMICN()) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
            return StmtNode.generateExpAndSemicn(null);
        }

        ExpNode expNode = parseExp();
        if (!isSEMICN()) {
            ErrorTool.checkI(getPreToken());
        } else {
            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分
            nextToken();
        }


        return StmtNode.generateExpAndSemicn(expNode);
    }

    // | LVal '=' 'getint''('')'';'
    public StmtNode analyzeGetInt() {
        LValNode lValNode = parseLValNode();
        if (getToken().getTokenType() != TokenType.ASSIGN) {
            System.out.println("error in analyzeGetInt of =");
        }

        ErrorTool.checkH(lValNode.getIdent(), symbolTable);

        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        } //输出词法成分

        nextToken();
        if (getToken().getTokenType() != TokenType.GETINTTK) {
            System.out.println("error in analyzeGetInt of getInt");
        }

        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        } //输出词法成分

        nextToken();
        if (!isLPARENT()) {
            System.out.println("error in analyzeGetInt of (");
        }

        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        } //输出词法成分

        nextToken();
        if (!isRPARENT()) {
            ErrorTool.checkJ(getPreToken());
        } else {
            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
        }

        if (!isSEMICN()) {
            ErrorTool.checkI(getPreToken());
        } else {
            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
        }
        return StmtNode.generateGetInt(lValNode);
    }

    //'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    public StmtNode analyzeIf() throws IOException, ClassNotFoundException {
        if (getToken().getTokenType() != TokenType.IFTK) {
            System.out.println("error in analyzeIf of if");
        }

        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        } //输出词法成分

        nextToken(); //(
        if (!isLPARENT()) {
            System.out.println("error in analyzeIf of (");
        }

        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        } //输出词法成分

        nextToken(); //cond
        CondNode condNode = parseCond();
        if (!isRPARENT()) {
            ErrorTool.checkJ(getPreToken());
        } else {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
        }
        StmtNode ifStmt = parseStmtNode();
        if (getToken().getTokenType() == TokenType.ELSETK) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
            StmtNode elseStmt = parseStmtNode();
            return StmtNode.generateIfElse(condNode, ifStmt, elseStmt);
        } else {
            return StmtNode.generateIf(condNode, ifStmt);
        }
    }

    //| 'while' '(' Cond ')' Stmt
    public StmtNode analyzeWhile() throws IOException, ClassNotFoundException {
        if (getToken().getTokenType() != TokenType.WHILETK) {
            System.out.println("error in analyzeWhile of while");
        }

        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        } //输出词法成分

        nextToken();
        if (!isLPARENT()) {
            System.out.println("error in analyzeWhile of (");
        }

        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        } //输出词法成分

        nextToken();
        CondNode condNode = parseCond();
        if (!isRPARENT()) {
            ErrorTool.checkJ(getPreToken());
        } else {
            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
        }
        cycleDepth++;
        StmtNode whileStmt = parseStmtNode();
        cycleDepth--;
        return StmtNode.generateWhile(condNode, whileStmt);
    }

    //| 'break' ';'
    public StmtNode analyzeBreak() {
        if (getToken().getTokenType() != TokenType.BREAKTK) {
            System.out.println("error in analyzeBreak of break");
        }

        if (cycleDepth == 0) {
            ErrorTool.checkM(getToken());
        }

        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        } //输出词法成分

        nextToken();
        if (!isSEMICN()) {
            ErrorTool.checkI(getPreToken());
        } else {
            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
        }
        return StmtNode.generateBreak();
    }

    //| 'continue' ';'
    public StmtNode analyzeContinue() {
        if (getToken().getTokenType() != TokenType.CONTINUETK) {
            System.out.println("error in analyzeContinue of continue");
        }

        if (cycleDepth == 0) {
            ErrorTool.checkM(getToken());
        }

        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        } //输出词法成分

        nextToken();
        if (!isSEMICN()) {
            ErrorTool.checkI(getPreToken());
        } else {
            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
        }
        return StmtNode.generateContinue();
    }

    public boolean isExpFirst() {
        return isUnaryOP() || isLPARENT() || isIdent() || isIntConst();
    }

    //'return' [Exp] ';' // 1.有Exp 2.无Exp
    public StmtNode analyzeReturn() {
        if (getToken().getTokenType() != TokenType.RETURNTK) {
            System.out.println("error in analyzeReturn of return");
        }

        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        } //输出词法成分
        Token returnToken = getToken();
        nextToken();
        if (isExpFirst()) { //return后是exp

            //若是void类型函数，则出错
            if (currentStmtDomain == CurrentStmtDomain.VOID_FUNC) {
                ErrorTool.checkF(returnToken);
            }

            ExpNode expNode = parseExp();
            if (!isSEMICN()) {
                ErrorTool.checkI(getPreToken());
            } else {
                if (outputParser) {
                    System.out.println(getToken().getTokenType() + " " + getToken().getValue());
                } //输出词法成分

                nextToken();
            }

            return StmtNode.generateReturn(expNode);
        }

        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        } //输出词法成分

        if (isSEMICN()) {
            nextToken();
        } else {
            //return后没有分号
            ErrorTool.checkI(getPreToken());
        }
        return StmtNode.generateReturn(null);
    }

    public int getFormatCharCount(Token token) {
        if (token.getTokenType() != TokenType.STRCON) {
            return 0;
        }
        String formatString = token.getValue();
        int cnt = 0;
        for (int i = 0; i < formatString.length() - 1; i++) {
            if (formatString.charAt(i) == '%' && formatString.charAt(i + 1) == 'd') {
                cnt++;
            }
        }
        return cnt;
    }

    // | 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
    public StmtNode analyzePrintf() {
        if (getToken().getTokenType() != TokenType.PRINTFTK) {
            System.out.println("error in analyzePrintf of printf");
        }

        Token printfToken = getToken();

        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        } //输出词法成分

        nextToken();
        if (!isLPARENT()) {
            System.out.println("error in analyzePrintf of (");
        }

        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        } //输出词法成分

        nextToken();
        if (getToken().getTokenType() != TokenType.STRCON) {
            System.out.println("error in analyzePrintf of string");
        }

        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        } //输出词法成分

        Token formatString = getToken();
        ErrorTool.checkA(formatString);

        //格式字符串中%d数量
        int formatCharCount = getFormatCharCount(formatString);
        //表达式个数
        int expCount = 0;
        nextToken();
        ArrayList<ExpNode> expNodes = new ArrayList<>();
        while (isCOMMA()) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
            expNodes.add(parseExp());
            expCount++;
        }

        if (expCount != formatCharCount) {
            ErrorTool.checkL(printfToken);
        }

        if (!isRPARENT()) {
            ErrorTool.checkJ(getPreToken());
        } else {
            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
        }

        if (!isSEMICN()) {
            ErrorTool.checkI(getPreToken());
        } else {
            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
        }

        return StmtNode.generatePrintf(formatString, expNodes);
    }

    //FuncFParams → FuncFParam { ',' FuncFParam }
    public FuncFParamsNode parseFuncFParams() throws IOException, ClassNotFoundException {
        FuncFParamsNode funcFParamsNode = new FuncFParamsNode();
        FuncFParamNode funcFParamNode = parseFuncFParam();
        funcFParamsNode.addParams(funcFParamNode);
        while (isCOMMA()) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
            funcFParamsNode.addParams(parseFuncFParam());
        }

        if (outputParser) {
            System.out.println("<FuncFParams>");
        }
        funcFParamsNode.setBlockDepth(blockDepth);

        return funcFParamsNode;
    }

    //FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    public FuncFParamNode parseFuncFParam() throws IOException, ClassNotFoundException {
        BTypeNode bTypeNode = parseBType();
        Token ident = parseIdent();

        boolean noErrorB = ErrorTool.checkB(ident, symbolTable);

        int dimension = 0;
        ArrayList<Integer> dimensions = new ArrayList<>();
        if (isLBRACK()) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            //有左括号就认为是数组
            dimension++;

            nextToken();
            dimensions.add(0); //一维数组长度不确定
            if (isRBRACK()) {

                if (outputParser) {
                    System.out.println(getToken().getTokenType() + " " + getToken().getValue());
                } //输出词法成分

                nextToken();
            } else {
                ErrorTool.checkK(getPreToken());
            }

            while (isLBRACK()) { //2维

                if (outputParser) {
                    System.out.println(getToken().getTokenType() + " " + getToken().getValue());
                } //输出词法成分

                dimension++;

                nextToken();

                ConstExpNode constExpNode = parseConstExp();
                dimensions.add(constExpNode.getConstVal(symbolTable));
                if (isRBRACK()) {

                    if (outputParser) {
                        System.out.println(
                            getToken().getTokenType() + " " + getToken().getValue());
                    } //输出词法成分

                    nextToken();
                } else {
                    ErrorTool.checkK(getPreToken());
                }
            }
        }

        if (noErrorB) {
            if (dimension == 0) {
                SymbolTableItem symbolTableItem = new SymbolTableItem(ident, SymbolType.VAR);
                symbolTableItem.setBlockDepth(blockDepth + 1);
                symbolTable.addItem(symbolTableItem);
            } else if (dimension == 1) {
                SymbolTableItem symbolTableItem =
                    new SymbolTableItem(ident, SymbolType.ARRAY, 1, dimensions,
                        (InitValNode) null);
                symbolTableItem.setBlockDepth(blockDepth + 1);
                symbolTable.addItem(symbolTableItem);

            } else {
                SymbolTableItem symbolTableItem =
                    new SymbolTableItem(ident, SymbolType.ARRAY, dimensions.size(), dimensions,
                        (InitValNode) null);
                symbolTableItem.setBlockDepth(blockDepth + 1);

                symbolTable.addItem(symbolTableItem);
            }
        }

        if (outputParser) {
            System.out.println("<FuncFParam>");
        }

        FuncFParamNode funcFParamNode =
            new FuncFParamNode(bTypeNode, ident, dimension, dimensions);
        funcFParamNode.setBlockDepth(blockDepth + 1);
        return funcFParamNode;
    }

    public boolean isVaildFuncType() {
        return getToken().getTokenType() == TokenType.VOIDTK ||
            getToken().getTokenType() == TokenType.INTTK;
    }

    //FuncType → 'void' | 'int'
    public FuncTypeNode parseFuncType() {
        if (!isVaildFuncType()) {
            System.out.println("error in parseFuncType");
        }

        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        } //输出词法成分

        if (outputParser) {
            System.out.println("<FuncType>");
        }

        TokenType funcType = getToken().getTokenType();
        nextToken();

        return new FuncTypeNode(funcType);
    }

    //MainFuncDef → 'int' 'main' '(' ')' Block
    public MainFuncDefNode parseMainFuncDefNode() throws IOException, ClassNotFoundException {
        FuncTypeNode funcType = null;
        if (getToken().getTokenType() != TokenType.INTTK) {
            System.out.println("error in parseMainFuncDefNode of int");
        } else {

            funcType = new FuncTypeNode(getToken().getTokenType()); //funcType is int now

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
        }

        if (getToken().getTokenType() != TokenType.MAINTK) {
            System.out.println("error in parseMainFuncDefNode of main");
        } else {

            boolean noErrorB = ErrorTool.checkB(getToken(), symbolTable);

            if (noErrorB) {
                SymbolTableItem symbolTableItem =
                    new SymbolTableItem(getToken(), SymbolType.FUNC_INT, funcType, null);
                symbolTableItem.setBlockDepth(blockDepth);
                symbolTable.addItem(symbolTableItem);
            }

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
        }

        if (!isLPARENT()) {
            System.out.println("error in parseMainFuncDefNode of (");
        } else {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
        }

        if (!isRPARENT()) {
            ErrorTool.checkJ(getToken());
        } else {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
        }

        currentStmtDomain = CurrentStmtDomain.INT_FUNC;
        funcOfIntHasReturnInTheLast = false;
        BlockNode blockNode = parseBlock(true);

        //int类型函数没有返回值
        if (funcOfIntHasReturnInTheLast == false) {
            ErrorTool.checkG(getPreToken());
        }
        funcOfIntHasReturnInTheLast = false;
        currentStmtDomain = CurrentStmtDomain.GLOBAL;

        if (outputParser) {
            System.out.println("<MainFuncDef>");
        }

        return new MainFuncDefNode(funcType, blockNode);
    }

    //ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';
    public ConstDeclNode parseConstDeclNode() throws IOException, ClassNotFoundException {
        parseConst();
        BTypeNode bTypeNode = parseBType();
        ArrayList<ConstDefNode> constDefNodes = new ArrayList<>();
        ConstDefNode constDefNode = parseConstDef();
        constDefNodes.add(constDefNode);
        while (getToken().getTokenType() == TokenType.COMMA) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
            constDefNodes.add(parseConstDef());
        }

        if (isSEMICN()) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
        } else {
            ErrorTool.checkI(getPreToken());
        }

        if (outputParser) {
            System.out.println("<ConstDecl>");
        }
        ConstDeclNode constDeclNode = new ConstDeclNode(bTypeNode, constDefNodes);
        constDeclNode.setBlockDepth(blockDepth);
        return constDeclNode;
    }

    //VarDecl → BType VarDef { ',' VarDef } ';'
    public VarDeclNode parseVarDecl() throws IOException, ClassNotFoundException {
        BTypeNode bTypeNode = parseBType();
        VarDeclNode varDeclNode = new VarDeclNode(bTypeNode);
        VarDefNode varDefNode = parseVarDef();

        varDeclNode.addVarDefNode(varDefNode);

        while (isCOMMA()) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
            varDeclNode.addVarDefNode(parseVarDef());
        }

        if (!isSEMICN()) {
            ErrorTool.checkI(getPreToken());
        } else {
            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();

            if (outputParser) {
                System.out.println("<VarDecl>");
            }
        }
        varDeclNode.setBlockDepth(blockDepth);
        return varDeclNode;
    }

    public boolean isSEMICN() {
        return getToken().getTokenType() == TokenType.SEMICN;
    }

    //VarDef → Ident { '[' ConstExp ']' } // 包含普通变量、一维数组、二维数组定义
    // VarDef → Ident { '[' ConstExp ']' } '=' InitVal
    public VarDefNode parseVarDef() throws IOException, ClassNotFoundException {
        Token ident = parseIdent();
        boolean noErrorB = ErrorTool.checkB(ident, symbolTable);
        ArrayList<Integer> dimensionLength = new ArrayList<>();
        int dimension = 1;
        while (isLBRACK()) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
            ConstExpNode constExpNode = parseConstExp();
            if (!isRBRACK()) {
                ErrorTool.checkK(getPreToken());
            } else {
                if (outputParser) {
                    System.out.println(getToken().getTokenType() + " " + getToken().getValue());
                } //输出词法成分

                nextToken(); //pass ]
            }
            dimensionLength.add(constExpNode.getConstVal(symbolTable));
            dimension++;
        }

        InitValNode initValNode = null;

        if (isASSIGN()) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
            initValNode = parseInitVal();
        }

        if (noErrorB) {
            if (dimension == 1) {
                if (blockDepth == 0) {
                    int initval = 0;
                    if (initValNode != null) {
                        initval = initValNode.getConstVal(symbolTable);
                    }
                    SymbolTableItem symbolTableItem =
                        new SymbolTableItem(ident, SymbolType.VAR, initval, false);
                    symbolTableItem.setBlockDepth(blockDepth);
                    symbolTable.addItem(symbolTableItem);
                } else {
                    SymbolTableItem symbolTableItem =
                        new SymbolTableItem(ident, SymbolType.VAR, initValNode);
                    symbolTableItem.setBlockDepth(blockDepth);
                    symbolTable.addItem(symbolTableItem);
                }
            } else {
                if (blockDepth == 0) {
                    ArrayList<Integer> initArrayVal;
                    if (initValNode != null) {
                        initArrayVal = initValNode.getConstArrayVal(symbolTable);
                    } else {
                        initArrayVal = new ArrayList<>();
                        int size = 1;
                        for (Integer integer : dimensionLength) {
                            size *= integer;
                        }
                        for (int i = 0; i < size; i++) {
                            initArrayVal.add(0);
                        }
                    }
                    SymbolTableItem symbolTableItem =
                        new SymbolTableItem(ident, SymbolType.ARRAY, dimension - 1, dimensionLength,
                            initArrayVal);
                    symbolTableItem.setBlockDepth(blockDepth);
                    symbolTable.addItem(symbolTableItem);
                } else {
                    SymbolTableItem symbolTableItem =
                        new SymbolTableItem(ident, SymbolType.ARRAY, dimension - 1, dimensionLength,
                            initValNode);
                    symbolTableItem.setBlockDepth(blockDepth);
                    symbolTable.addItem(symbolTableItem);
                }
            }
        }

        if (outputParser) {
            System.out.println("<VarDef>");
        }

        VarDefNode varDefNode = new VarDefNode(ident, dimensionLength, initValNode);
        varDefNode.setBlockDepth(blockDepth);
        varDefNode.setSymbolTable(symbolTable);

        return varDefNode;
    }

    //InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    public InitValNode parseInitVal() {
        InitValNode initValNode;
        if (isLBRACE()) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
            initValNode = new InitValNode();

            if (isRBRACE()) {

                if (outputParser) {
                    System.out.println(getToken().getTokenType() + " " + getToken().getValue());
                } //输出词法成分

                nextToken();
            } else {
                initValNode.addInitValNode(parseInitVal());
                while (isCOMMA()) {

                    if (outputParser) {
                        System.out.println(getToken().getTokenType() + " " + getToken().getValue());
                    } //输出词法成分

                    nextToken();
                    initValNode.addInitValNode(parseInitVal());
                }

                if (!isRBRACE()) {
                    System.out.println("error in parseInitVal of array");
                }

                if (outputParser) {
                    System.out.println(getToken().getTokenType() + " " + getToken().getValue());
                } //输出词法成分

                nextToken();
            }

        } else {
            initValNode = new InitValNode(parseExp());
        }

        if (outputParser) {
            System.out.println("<InitVal>");
        }

        initValNode.setBlockDepth(blockDepth);
        initValNode.setSymbolTable(symbolTable);

        return initValNode;
    }

    public boolean isLBRACE() {
        return getToken().getTokenType() == TokenType.LBRACE;
    }

    public boolean isRBRACE() {
        return getToken().getTokenType() == TokenType.RBRACE;
    }

    public boolean isASSIGN() {
        return getToken().getTokenType() == TokenType.ASSIGN;
    }

    //BType → 'int'
    public BTypeNode parseBType() {
        if (getToken().getTokenType() == TokenType.INTTK) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            BTypeNode bTypeNode = new BTypeNode(getToken().getTokenType());
            nextToken();
            return bTypeNode;
        } else {
            System.out.println("error in parseBType");
            return null;
        }
    }


    public void parseConst() {
        if (getToken().getTokenType() == TokenType.CONSTTK) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
        } else {
            System.out.println("error in parseConst");
        }
    }

    //ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    public ConstDefNode parseConstDef() throws IOException, ClassNotFoundException {
        Token ident = parseIdent();
        boolean noErrorB = ErrorTool.checkB(ident, this.symbolTable);
        ArrayList<Integer> dimensionLength = new ArrayList<>();
        int dimension = 1;

        while (isLBRACK()) {
            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
            ConstExpNode constExpNode = parseConstExp();
            if (isRBRACK()) {

                if (outputParser) {
                    System.out.println(getToken().getTokenType() + " " + getToken().getValue());
                } //输出词法成分

                nextToken();
            } else {
                ErrorTool.checkK(getPreToken());
            }
            dimensionLength.add(constExpNode.getConstVal(symbolTable));
            dimension++;
        }
        parseASSIGN();
        ConstInitValNode constInitValNode = parseConstInitVal();
        if (noErrorB) {
            if (dimension == 1) {
                int initVal = constInitValNode.getConstVal(symbolTable);
//                if (ident.getValue().equals("year_1") || ident.getValue().equals("year_2")) {
//                    System.err.println(initVal);
//                }
                SymbolTableItem symbolTableItem =
                    new SymbolTableItem(ident, SymbolType.CONST_VAR, initVal, true);
                symbolTableItem.setBlockDepth(blockDepth);
                symbolTable.addItem(symbolTableItem);
            } else {
                ArrayList<Integer> initArrayVal = constInitValNode.getConstArrayVal(symbolTable);
                SymbolTableItem symbolTableItem =
                    new SymbolTableItem(ident, SymbolType.CONST_ARRAY, dimension - 1,
                        dimensionLength, initArrayVal);
                symbolTableItem.setBlockDepth(blockDepth);
                symbolTable.addItem(symbolTableItem);
            }
        }

        if (outputParser) {
            System.out.println("<ConstDef>");
        }

        ConstDefNode constDefNode = new ConstDefNode(ident, dimensionLength, constInitValNode);
        constDefNode.setBlockDepth(blockDepth);
        constDefNode.setSymbolTable(symbolTable);

        return constDefNode;
    }

    public Token parseIdent() {
        if (getToken().getTokenType() == TokenType.IDENFR) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            Token token = getToken();
            nextToken();
            return token;
        } else {
            System.out.println("error in parseIdent");
            return null;
        }
    }

    //ConstExp → AddExp
    public ConstExpNode parseConstExp() {
        AddExpNode addExpNode = parseAddExp();

        if (outputParser) {
            System.out.println("<ConstExp>");
        }

        return new ConstExpNode(addExpNode);
    }

    //=
    public void parseASSIGN() {
        if (getToken().getTokenType() == TokenType.ASSIGN) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
        } else {
            System.out.println("error in parseAssign");
        }
        return;
    }

    //ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    public ConstInitValNode parseConstInitVal() {
        ConstInitValNode constInitValNode;
        if (isLBRACE()) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();

            constInitValNode = new ConstInitValNode();
            if (isRBRACE()) {
                if (outputParser) {
                    System.out.println(getToken().getTokenType() + " " + getToken().getValue());
                } //输出词法成分

                nextToken();
            } else {
                ConstInitValNode tmp = parseConstInitVal();
                constInitValNode.addConstInitValNode(tmp);
                while (getToken().getTokenType() == TokenType.COMMA) {

                    if (outputParser) {
                        System.out.println(getToken().getTokenType() + " " + getToken().getValue());
                    } //输出词法成分

                    nextToken();
                    constInitValNode.addConstInitValNode(parseConstInitVal());
                }

                if (getToken().getTokenType() != TokenType.RBRACE) {
                    System.out.println("error in parseConstInitVal of array");
                }

                if (outputParser) {
                    System.out.println(getToken().getTokenType() + " " + getToken().getValue());
                } //输出词法成分

                nextToken(); //pass }
            }

        } else {
            ConstExpNode constExpNode = parseConstExp();
            constInitValNode = new ConstInitValNode(constExpNode);
        }

        if (outputParser) {
            System.out.println("<ConstInitVal>");
        }

        constInitValNode.setSymbolTable(symbolTable);
        constInitValNode.setBlockDepth(blockDepth);

        return constInitValNode;
    }

    //AddExp → MulExp | AddExp ('+' | '−') MulExp
    public AddExpNode parseAddExp() {
        MulExpNode mulExpNode = parseMulExp();
        AddExpNode addExpNode = new AddExpNode(mulExpNode);

        if (outputParser) {
            System.out.println("<AddExp>");
        }

        while (isPlusOrMinu()) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            TokenType op = getToken().getTokenType();
            nextToken();
            MulExpNode midMulExpNode = parseMulExp();
            addExpNode = new AddExpNode(addExpNode, op, midMulExpNode);

            if (outputParser) {
                System.out.println("<AddExp>");
            }

        }

        return addExpNode;
    }

    public AddExpNode checkAddExp() {
        MulExpNode mulExpNode = checkMulExp();
        AddExpNode addExpNode = new AddExpNode(mulExpNode);

        while (isPlusOrMinu()) {

            TokenType op = getToken().getTokenType();
            nextToken();
            MulExpNode midMulExpNode = checkMulExp();
            addExpNode = new AddExpNode(addExpNode, op, midMulExpNode);
        }

        return addExpNode;
    }

    //MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    public MulExpNode parseMulExp() {
        UnaryExpNode unaryExpNode = parseUnaryExp();
        MulExpNode mulExpNode = new MulExpNode(unaryExpNode);

        if (outputParser) {
            System.out.println("<MulExp>");
        }

        while (isMultOrDivOrMod()) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            TokenType op = getToken().getTokenType();
            nextToken();
            unaryExpNode = parseUnaryExp();
            mulExpNode = new MulExpNode(mulExpNode, op, unaryExpNode);

            if (outputParser) {
                System.out.println("<MulExp>");
            }

        }

        return mulExpNode;
    }

    public MulExpNode checkMulExp() {
        UnaryExpNode unaryExpNode = checkUnaryExp();
        MulExpNode mulExpNode = new MulExpNode(unaryExpNode);


        while (isMultOrDivOrMod()) {

            TokenType op = getToken().getTokenType();
            nextToken();
            unaryExpNode = checkUnaryExp();
            mulExpNode = new MulExpNode(mulExpNode, op, unaryExpNode);

        }

        return mulExpNode;
    }

    public boolean isFuncCall() {
        return getToken().getTokenType() == TokenType.IDENFR &&
            getNextKToken(1).getTokenType() == TokenType.LPARENT;
    }

    public boolean isUnaryOP() {
        TokenType tokenType = getToken().getTokenType();
        return isPlusOrMinu() || tokenType == TokenType.NOT;
    }

    //UnaryOp → '+' | '−' | '!'
    public TokenType parseUnaryOp() {
        if (!isUnaryOP()) {
            System.out.println("error in parseUnaryOp");
        }

        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        } //输出词法成分

        TokenType op = getToken().getTokenType();
        nextToken();

        if (outputParser) {
            System.out.println("<UnaryOp>");
        }

        return op;
    }

    public TokenType checkUnaryOp() {
        TokenType op = getToken().getTokenType();
        nextToken();
        return op;
    }

    //UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    public UnaryExpNode parseUnaryExp() {
        UnaryExpNode unaryExpNode;
        if (isFuncCall()) { //函数调用
            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            Token ident = getToken();

            //检查错误C
            ErrorTool.checkC(ident, symbolTable);
            //检查错误C

            nextToken();

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分


            //has checked whether is (
            nextToken();
            FuncRParamsNode params;
            if (!isExpFirst()) { //可能没有参数，直接匹配右括号

                if (isRPARENT()) {
                    if (outputParser) {
                        System.out.println(getToken().getTokenType() + " " + getToken().getValue());
                    } //输出词法成分

                    params = null;
                    nextToken();
                } else {
                    params = null;
                    ErrorTool.checkJ(getPreToken());
                }

            } else {
                params = parseFuncRParamsNode();
                if (!isRPARENT()) {
                    ErrorTool.checkJ(getPreToken());
                } else {
                    if (outputParser) {
                        System.out.println(getToken().getTokenType() + " " + getToken().getValue());
                    } //输出词法成分

                    nextToken();
                }
            }
            int paramsNum = params == null ? 0 : params.getRealParamsNum();
            boolean noErrorD = ErrorTool.checkD(ident, paramsNum, symbolTable);
            if (noErrorD) {
                ErrorTool.checkE(ident, params, symbolTable);
            }


            unaryExpNode = new UnaryExpNode(ident, params);

        } else if (isUnaryOP()) {
            TokenType op = parseUnaryOp();
            unaryExpNode = new UnaryExpNode(op, parseUnaryExp());
        } else {
            PrimaryExpNode primaryExpNode = parsePrimaryExp();
            unaryExpNode = new UnaryExpNode(primaryExpNode);
        }

        if (outputParser) {
            System.out.println("<UnaryExp>");
        }

        unaryExpNode.setBlockDepth(blockDepth);
        unaryExpNode.setSymbolTable(symbolTable);

        return unaryExpNode;
    }

    public UnaryExpNode checkUnaryExp() {
        UnaryExpNode unaryExpNode;
        if (isFuncCall()) { //函数调用

            Token ident = getToken();

            nextToken();

            //has checked whether is (
            nextToken();
            FuncRParamsNode params;
            if (!isExpFirst()) { //可能没有参数，直接匹配右括号

                if (isRPARENT()) {

                    params = null;
                    nextToken();
                } else {
                    params = null;
                }

            } else {
                params = parseFuncRParamsNode();
                if (isRPARENT()) {
                    nextToken();
                }
            }


            unaryExpNode = new UnaryExpNode(ident, params);

        } else if (isUnaryOP()) {
            TokenType op = checkUnaryOp();
            unaryExpNode = new UnaryExpNode(op, checkUnaryExp());
        } else {
            PrimaryExpNode primaryExpNode = checkPrimaryExp();
            unaryExpNode = new UnaryExpNode(primaryExpNode);
        }

        return unaryExpNode;
    }

    public boolean isLPARENT() {
        return getToken().getTokenType() == TokenType.LPARENT;
    }

    public boolean isRPARENT() {
        return getToken().getTokenType() == TokenType.RPARENT;
    }

    public boolean isIdent() {
        return getToken().getTokenType() == TokenType.IDENFR;
    }

    public boolean isIntConst() {
        return getToken().getTokenType() == TokenType.INTCON;
    }

    //PrimaryExp → '(' Exp ')' | LVal | Number
    public PrimaryExpNode parsePrimaryExp() {
        PrimaryExpNode primaryExpNode;
        if (isLPARENT()) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
            primaryExpNode = new PrimaryExpNode(parseExp());

            if (!isRPARENT()) {
                System.out.println("error in parsePrimaryExp Of (Exp)");
            }

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
        } else if (isIdent()) {
            primaryExpNode = new PrimaryExpNode(parseLValNode());
        } else if (isIntConst()) {
            primaryExpNode = new PrimaryExpNode(parseNumber());
        } else {
            primaryExpNode = null;
            System.out.println(getToken().getTokenType() + " " + getToken().getValue() + " " +
                getToken().getLine());
            System.out.println(getPreToken().getTokenType() + " " + getPreToken().getValue() + " " +
                getPreToken().getLine());
            System.out.println("error in parsePrimaryExp of Not right expression");
        }

        if (outputParser) {
            System.out.println("<PrimaryExp>");
        }

        if (primaryExpNode != null) {
            primaryExpNode.setBlockDepth(blockDepth);
        }

        return primaryExpNode;
    }

    public PrimaryExpNode checkPrimaryExp() {
        PrimaryExpNode primaryExpNode;
        if (isLPARENT()) {

            nextToken();
            primaryExpNode = new PrimaryExpNode(checkExp());

            nextToken();
        } else if (isIdent()) {
            primaryExpNode = new PrimaryExpNode(checkLValNode());
        } else if (isIntConst()) {
            primaryExpNode = new PrimaryExpNode(checkNumber());
        } else {
            primaryExpNode = null;
            System.out.println(getToken().getTokenType() + " " + getToken().getValue() + " " +
                getToken().getLine());
            System.out.println(getPreToken().getTokenType() + " " + getPreToken().getValue() + " " +
                getPreToken().getLine());
            System.out.println("error in checkPrimaryExp of Not right expression");
        }

        return primaryExpNode;
    }

    public boolean isCOMMA() {
        return getToken().getTokenType() == TokenType.COMMA;
    }

    //FuncRParams → Exp { ',' Exp }
    public FuncRParamsNode parseFuncRParamsNode() {
        FuncRParamsNode funcRParamsNode = new FuncRParamsNode();
        ExpNode expNode = parseExp();
        funcRParamsNode.addExpNode(expNode);
        while (isCOMMA()) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
            expNode = parseExp();
            funcRParamsNode.addExpNode(expNode);
        }

        if (outputParser) {
            System.out.println("<FuncRParams>");
        }

        funcRParamsNode.setBlockDepth(blockDepth);

        return funcRParamsNode;
    }

    public FuncRParamsNode checkFuncRParamsNode() {
        FuncRParamsNode funcRParamsNode = new FuncRParamsNode();
        ExpNode expNode = checkExp();
        funcRParamsNode.addExpNode(expNode);
        while (isCOMMA()) {

            nextToken();
            expNode = checkExp();
            funcRParamsNode.addExpNode(expNode);
        }

        return funcRParamsNode;
    }

    //Exp → AddExp
    public ExpNode parseExp() {
        AddExpNode addExpNode = parseAddExp();
        ExpNode expNode = new ExpNode(addExpNode);

        if (outputParser) {
            System.out.println("<Exp>");
        }

        return expNode;
    }

    public ExpNode checkExp() {
        AddExpNode addExpNode = checkAddExp();
        ExpNode expNode = new ExpNode(addExpNode);

        return expNode;
    }

    public boolean isLBRACK() {
        return getToken().getTokenType() == TokenType.LBRACK;
    }

    public boolean isRBRACK() {
        return getToken().getTokenType() == TokenType.RBRACK;
    }

    //LVal → Ident {'[' Exp ']'}
    public LValNode parseLValNode() {
        if (!isIdent()) {
            System.out.println("error in parseLValNode of Ident");
        }
        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        } //输出词法成分

        Token ident = getToken();

        //检查错误C
        ErrorTool.checkC(ident, symbolTable);
        //检查错误C

        nextToken();
        int dimension = 1;
        ArrayList<ExpNode> dimensionOfExp = new ArrayList<>();
        while (isLBRACK()) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
            ExpNode expNode = parseExp();
            dimensionOfExp.add(expNode);
            dimension++;

            if (!isRBRACK()) {
                ErrorTool.checkK(getPreToken());
            } else {
                if (outputParser) {
                    System.out.println(getToken().getTokenType() + " " + getToken().getValue());
                } //输出词法成分

                nextToken();
            }
        }

        LValNode lValNode = new LValNode(ident, dimensionOfExp);

        if (outputParser) {
            System.out.println("<LVal>");
        }
        lValNode.setBlockDepth(blockDepth);
        lValNode.setSymbolTable(symbolTable);

        return lValNode;
    }

    public LValNode checkLValNode() {

        Token ident = getToken();

        nextToken();
        int dimension = 1;
        ArrayList<ExpNode> dimensionOfExp = new ArrayList<>();
        while (isLBRACK()) {

            nextToken();
            ExpNode expNode = checkExp();
            dimensionOfExp.add(expNode);
            dimension++;

            if (!isRBRACK()) {
                continue;
            } else {

                nextToken();
            }
        }

        LValNode lValNode = new LValNode(ident, dimensionOfExp);

        return lValNode;
    }

    //Number → IntConst
    public NumberNode parseNumber() {
        if (!isIntConst()) {
            System.out.println("error in parseNumber");
        }

        if (outputParser) {
            System.out.println(getToken().getTokenType() + " " + getToken().getValue());
        } //输出词法成分

        Token intConst = getToken();
        nextToken();

        if (outputParser) {
            System.out.println("<Number>");
        }

        return new NumberNode(intConst);
    }

    public NumberNode checkNumber() {

        Token intConst = getToken();
        nextToken();

        return new NumberNode(intConst);
    }

    //Cond → LOrExp
    public CondNode parseCond() {
        LOrExpNode lOrExpNode = parseLOrExp();

        if (outputParser) {
            System.out.println("<Cond>");
        }

        return new CondNode(lOrExpNode);
    }

    //LOrExp → LAndExp | LOrExp '||' LAndExp
    public LOrExpNode parseLOrExp() {
        LAndExpNode lAndExpNode = parseLAndExp();
        LOrExpNode lOrExpNode = new LOrExpNode(lAndExpNode);

        if (outputParser) {
            System.out.println("<LOrExp>");
        }

        while (getToken().getTokenType() == TokenType.OR) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
            lAndExpNode = parseLAndExp();
            lOrExpNode = new LOrExpNode(lOrExpNode, TokenType.OR, lAndExpNode);

            if (outputParser) {
                System.out.println("<LOrExp>");
            }

        }

        return lOrExpNode;
    }

    //LAndExp → EqExp | LAndExp '&&' EqExp
    public LAndExpNode parseLAndExp() {
        EqExpNode eqExpNode = parseEqExp();
        LAndExpNode lAndExpNode = new LAndExpNode(eqExpNode);

        if (outputParser) {
            System.out.println("<LAndExp>");
        }

        while (getToken().getTokenType() == TokenType.AND) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            nextToken();
            eqExpNode = parseEqExp();
            lAndExpNode = new LAndExpNode(lAndExpNode, TokenType.AND, eqExpNode);

            if (outputParser) {
                System.out.println("<LAndExp>");
            }

        }

        return lAndExpNode;
    }

    //EqExp → RelExp | EqExp ('==' | '!=') RelExp
    public EqExpNode parseEqExp() {
        RelExpNode relExpNode = parseRelExp();
        EqExpNode eqExpNode = new EqExpNode(relExpNode);

        if (outputParser) {
            System.out.println("<EqExp>");
        }

        while (isNEQ() || isEQL()) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            TokenType op = getToken().getTokenType();
            nextToken();
            relExpNode = parseRelExp();
            eqExpNode = new EqExpNode(eqExpNode, op, relExpNode);

            if (outputParser) {
                System.out.println("<EqExp>");
            }
        }

        return eqExpNode;
    }

    public boolean isNEQ() {
        return getToken().getTokenType() == TokenType.NEQ;
    }

    public boolean isEQL() {
        return getToken().getTokenType() == TokenType.EQL;
    }

    public boolean isLSS() {
        return getToken().getTokenType() == TokenType.LSS;
    }

    public boolean isGRE() {
        return getToken().getTokenType() == TokenType.GRE;
    }

    public boolean isGEQ() {
        return getToken().getTokenType() == TokenType.GEQ;
    }

    public boolean isLEQ() {
        return getToken().getTokenType() == TokenType.LEQ;
    }

    //RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    public RelExpNode parseRelExp() {
        AddExpNode addExpNode = parseAddExp();
        RelExpNode relExpNode = new RelExpNode(addExpNode);

        if (outputParser) {
            System.out.println("<RelExp>");
        }

        while (isLSS() || isGRE() || isGEQ() || isLEQ()) {

            if (outputParser) {
                System.out.println(getToken().getTokenType() + " " + getToken().getValue());
            } //输出词法成分

            TokenType op = getToken().getTokenType();
            nextToken();
            addExpNode = parseAddExp();
            relExpNode = new RelExpNode(relExpNode, op, addExpNode);

            if (outputParser) {
                System.out.println("<RelExp>");
            }

        }

        return relExpNode;
    }
}
