package MipsCode;

public class GlobalVarInit {
    private int initVal;
    private String name;

    public GlobalVarInit(String name, int initVal) {
        this.name = name;
        this.initVal = initVal;
    }

    public void output() {
        System.out.println(name + ":   .word  " + initVal);
    }
}
