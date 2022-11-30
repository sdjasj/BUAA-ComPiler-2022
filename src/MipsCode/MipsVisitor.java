package MipsCode;

import IntermediateCode.Operand;
import MipsCode.MipsCode.MipsCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MipsVisitor {
    private HashMap<String, GlobalArrayInit> globalArrayInits;
    private HashMap<String, GlobalVarInit> globalVarInits;
    private HashMap<String, GlobalStrInit> globalStrInits;
    private ArrayList<MipsCode> mipsCodes;
    private HashMap<String, Integer> globalVarToOffset = new HashMap<>();
    private int gloBalVarOffset = 0;

    public MipsVisitor() {
        this.globalArrayInits = new HashMap<>();
        this.globalVarInits = new HashMap<>();
        this.globalStrInits = new HashMap<>();
        this.mipsCodes = new ArrayList<>();
    }

    public void addGlobalArrayInit(String name, GlobalArrayInit globalArrayInit) {
        globalArrayInits.put(name, globalArrayInit);
    }

    public void addGlobalVarInits(String name, GlobalVarInit globalVarInit) {
        globalVarInits.put(name, globalVarInit);
    }

    public void addGlobalStrInits(String name, GlobalStrInit globalStrInit) {
        globalStrInits.put(name, globalStrInit);
    }

    public void addMipsCode(MipsCode mipsCode) {
//        mipsCode.output();
        mipsCodes.add(mipsCode);
    }

    public int setOffsetByVar(String name, int arrayOffset) {
        int offset = globalVarToOffset.get(name) + arrayOffset;
        int delta = offset - gloBalVarOffset;
        gloBalVarOffset = offset;
        return delta;
    }

    public int getOffsetByVar(String name, int arrayOffset) {
//        System.err.println(name);
        int offset = globalVarToOffset.get(name) + arrayOffset;
        int delta = offset - gloBalVarOffset;
//        System.err.println(delta);
        return delta;
    }

    public void generateGlobal() {
        final int[] offset = {0};
        globalVarInits.forEach((s, globalVarInit) -> {
            globalVarToOffset.put(s, offset[0]);
            offset[0] += 4;
        });

        globalArrayInits.forEach((s, globalArrayInit) -> {
            globalVarToOffset.put(s, offset[0]);
            offset[0] += globalArrayInit.getSize() * 4;
        });
    }

    public void output() {
        //数据区
        System.out.println(".data");
        //字符串
        globalStrInits.forEach((s, globalStrInit) -> {
            globalStrInit.output();
        });
        //全局变量
        globalVarInits.forEach((s, globalVarInit) -> {
            globalVarInit.output();
        });
        //数组
        globalArrayInits.forEach((s, globalArrayInit) -> {
            globalArrayInit.output();
        });

        System.out.println("\n\n\n");
        //代码区
        System.out.println(".text");

        MipsOptimizer.optimizeCalculate(mipsCodes);

        String beginVar = null;
        for (Map.Entry<String, Integer> entry : globalVarToOffset.entrySet()) {
            if (entry.getValue() == 0) {
                beginVar = entry.getKey();
            }
        }
        if (beginVar != null) {
            System.out.println("la $28, " + beginVar);
        }

        for (MipsCode mipsCode : mipsCodes) {
            mipsCode.output();
        }
    }
}
