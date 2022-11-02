package IntermediateCode;

public class TCode {
    public static int cnt = 0;
    public static int cntStr = 0;
    public static int cntLable = 0;

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
        String lable = "$lable_" + cntLable;
        cntLable++;
        return lable;
    }
}
