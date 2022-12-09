package IntermediateCode;

import java.util.HashMap;

public class TCode {
    public static int cnt = 0;
    public static int cntStr = 0;
    public static int cntLabel = 0;
    public static int loopDepth = 0;

    public static String genNewT() {
        String target = "t@" + cnt;
        cnt++;
        return target;
    }

    public static String reName(String oldName, int depth) {
        return oldName + "_" + depth;
    }

    public static String genNewStr() {
        String target = "str_" + cntStr;
        cntStr++;
        return target;
    }

    public static String genNewLable() {
        String lable = "$lable_" + cntLabel;
        cntLabel++;
        return lable;
    }
}
