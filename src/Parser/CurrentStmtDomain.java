package Parser;

//声明当前parse的语句处于什么函数内
public enum CurrentStmtDomain {
    GLOBAL,
    VOID_FUNC,
    INT_FUNC
}
