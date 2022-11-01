package Lexer;

import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    private ArrayList<Token> tokens;
    private int curLine;
    private String token;
    private String program;
    private int programLength;
    private int curPos;
    private HashMap<String, TokenType> keywords;
    private HashMap<String, TokenType> singleDelimiter;
    private HashMap<String, TokenType> doubleDelimiter;

    public Lexer(String program) {
        this.tokens = new ArrayList<>();
        this.token = "";
        this.curLine = 1;
        this.program = program;
        this.programLength = program.length();
        this.curPos = 0;
        this.keywords = new HashMap<String, TokenType>() {
            {
                put("main", TokenType.MAINTK);
                put("const", TokenType.CONSTTK);
                put("int", TokenType.INTTK);
                put("break", TokenType.BREAKTK);
                put("continue", TokenType.CONTINUETK);
                put("if", TokenType.IFTK);
                put("else", TokenType.ELSETK);
                put("while", TokenType.WHILETK);
                put("getint", TokenType.GETINTTK);
                put("printf", TokenType.PRINTFTK);
                put("return", TokenType.RETURNTK);
                put("void", TokenType.VOIDTK);
            }
        };
        this.singleDelimiter = new HashMap<String, TokenType>() {
            {
                put("+", TokenType.PLUS);
                put("-", TokenType.MINU);
                put("*", TokenType.MULT);
                put("!", TokenType.NOT);
                put("/", TokenType.DIV);
                put("%", TokenType.MOD);
                put("<", TokenType.LSS);
                put(">", TokenType.GRE);
                put("=", TokenType.ASSIGN);
                put(";", TokenType.SEMICN);
                put(",", TokenType.COMMA);
                put("(", TokenType.LPARENT);
                put(")", TokenType.RPARENT);
                put("[", TokenType.LBRACK);
                put("]", TokenType.RBRACK);
                put("{", TokenType.LBRACE);
                put("}", TokenType.RBRACE);
            }
        };
        this.doubleDelimiter = new HashMap<String, TokenType>() {
            {
                put("&&", TokenType.AND);
                put("||", TokenType.OR);
                put("<=", TokenType.LEQ);
                put(">=", TokenType.GEQ);
                put("==", TokenType.EQL);
                put("!=", TokenType.NEQ);
            }
        };
    }

    public ArrayList<Token> analyzeLexicals() {
        while (posIsValid()) {
            while (isSpace() || isTab() || isEnter()) {
                if (curPos < programLength - 1 && program.charAt(curPos) == '\r' &&
                    program.charAt(curPos + 1) == '\n') {
                    nextChar();
                    nextChar();
                } else {
                    nextChar();
                }
            }
            if (!posIsValid()) {
                break;
            }
            clearToken();
            if (isIdentifierNondigit()) {
                while (isIdentifierNondigit() || isDigit()) {
                    catToken();
                    nextChar();
                }
                if (keywords.containsKey(token)) {
                    tokens.add(new Token(keywords.get(token), token, curLine));
                } else {
                    tokens.add(new Token(TokenType.IDENFR, token, curLine));
                }
            } else if (isDigit()) {
                if (program.charAt(curPos) == '0') {
                    catToken();
                    nextChar();
                } else {
                    while (isDigit()) {
                        catToken();
                        nextChar();
                    }
                }
                tokens.add(new Token(TokenType.INTCON, token, curLine));
            } else if (isSingleDelimiterNotParadox()) {
                catToken();
                nextChar();
                tokens.add(new Token(singleDelimiter.get(token), token, curLine));
            } else {
                switch (program.charAt(curPos)) {
                    case '!':
                    case '<':
                    case '>':
                    case '=':
                    {
                        catToken();
                        nextChar();
                        if (posIsValid() && program.charAt(curPos) == '=') {
                            catToken();
                            nextChar();
                            tokens.add(new Token(doubleDelimiter.get(token), token, curLine));
                        } else {
                            tokens.add(new Token(singleDelimiter.get(token), token, curLine));
                        }
                        break;
                    }
                    case '&':
                    {
                        catToken();
                        nextChar();
                        if (posIsValid() && program.charAt(curPos) == '&') {
                            catToken();
                            nextChar();
                            tokens.add(new Token(doubleDelimiter.get(token), token, curLine));
                        } else {
                            tokens.add(new Token(singleDelimiter.get(token), token, curLine));
                        }
                        break;
                    }
                    case '|':
                    {
                        catToken();
                        nextChar();
                        if (posIsValid() && program.charAt(curPos) == '|') {
                            catToken();
                            nextChar();
                            tokens.add(new Token(doubleDelimiter.get(token), token, curLine));
                        } else {
                            tokens.add(new Token(singleDelimiter.get(token), token, curLine));
                        }
                        break;
                    }
                    case '/':
                    {
                        catToken();
                        nextChar();
                        if (posIsValid() && program.charAt(curPos) == '/') {
                            nextChar();
                            skipCommentsOfSingleLine();
                        } else if (posIsValid() && program.charAt(curPos) == '*') {
                            nextChar();
                            skipCommentsOfMulLine();
                        } else {
                            tokens.add(new Token(singleDelimiter.get(token), token, curLine));
                        }
                        break;
                    }
                    case '"':
                    {
                        catToken();
                        nextChar();
                        while (program.charAt(curPos) != '"') {
                            catToken();
                            nextChar();
                        }
                        catToken();
                        nextChar();
                        tokens.add(new Token(TokenType.STRCON, token, curLine));
                        break;
                    }
                    default:
                        break;
                }
            }
        }
        return tokens;
    }

    public boolean posIsValid() {
        return curPos < programLength;
    }

    public boolean isTab() {
        return posIsValid() && program.charAt(curPos) == '\t';
    }

    public boolean isSpace() {
        return posIsValid() && program.charAt(curPos) == ' ';
    }

    public boolean isEnter() {
        if (!posIsValid()) {
            return false;
        }
        if (program.charAt(curPos) == '\n') {
            ++curLine;
            return true;
        } else if (curPos < programLength - 1 && program.charAt(curPos) == '\r' &&
            program.charAt(curPos + 1) == '\n') {
            ++curLine;
            return true;
        }
        return false;
    }

    public boolean isIdentifierNondigit() {
        return posIsValid() && (Character.isLetter(program.charAt(curPos)) ||
            program.charAt(curPos) == '_');
    }

    public boolean isDigitNonZero() {
        return posIsValid() && "123456789".indexOf(program.charAt(curPos)) != -1;
    }

    public boolean isDigit() {
        return posIsValid() && Character.isDigit(program.charAt(curPos));
    }

    public void clearToken() {
        token = "";
    }

    public void catToken() {
        token += program.charAt(curPos);
    }

    public void nextChar() {
        ++curPos;
    }

    public boolean isSingleDelimiterNotParadox() {
        if (!posIsValid()) {
            return false;
        }
        char curChar = program.charAt(curPos);
        return "+-*%,;()[]{}".indexOf(curChar) != -1;
    }

    public void skipCommentsOfSingleLine() {
        while (posIsValid() && !isEnter()) {
            nextChar();
        }
        if (curPos < programLength - 1 && program.charAt(curPos) == '\r' &&
            program.charAt(curPos + 1) == '\n') {
            nextChar();
            nextChar();
        } else {
            nextChar();
        }
    }

    public void skipCommentsOfMulLine() {
        while (posIsValid()) {
            if (program.charAt(curPos) == '*' &&
                curPos < programLength - 1 && program.charAt(curPos + 1) == '/') {
                nextChar();
                nextChar();
                break;
            }
            isEnter();
            if (curPos < programLength - 1 && program.charAt(curPos) == '\r' &&
                program.charAt(curPos + 1) == '\n') {
                nextChar();
                nextChar();
            } else {
                nextChar();
            }
        }
    }
}
