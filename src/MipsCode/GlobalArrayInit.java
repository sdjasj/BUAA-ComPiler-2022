package MipsCode;

import java.util.ArrayList;

public class GlobalArrayInit {
    private String name;
    private int size;
    private ArrayList<Integer> initVals;

    public GlobalArrayInit(String name, int size, ArrayList<Integer> initVals) {
        this.name = name;
        this.size = size;
        this.initVals = initVals;
    }

    public void output() {
        StringBuilder init = new StringBuilder(name + ":   .word  ");
        for (Integer initVal : initVals) {
            init.append(initVal).append(",");
        }
        System.out.println(init.substring(0, init.length() - 1));
    }
}
