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

    public int getSize() {
        return size;
    }

    public void output() {
        StringBuilder init = new StringBuilder(name + ":   .word  ");
        if (initVals == null) {
            init.append("0:" + size);
            System.out.println(init);
        } else {
            for (Integer initVal : initVals) {
                init.append(initVal).append(",");
            }
            System.out.println(init.substring(0, init.length() - 1));
        }

    }
}
