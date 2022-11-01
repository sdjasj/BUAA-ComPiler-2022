package MipsCode;

import MipsCode.MipsCode.MipsCode;

import java.util.ArrayList;
import java.util.HashMap;

public class MipsVisitor {
    private HashMap<String, GlobalArrayInit> globalArrayInits;
    private HashMap<String, GlobalVarInit> globalVarInits;
    private HashMap<String, GlobalStrInit> globalStrInits;
    private ArrayList<MipsCode> mipsCodes;

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

    public boolean varIsGlobal(String name) {
        return globalVarInits.containsKey(name) || globalArrayInits.containsKey(name);
    }

    public void addMipsCode(MipsCode mipsCode) {
//        mipsCode.output();
        mipsCodes.add(mipsCode);
    }

    public void output() {
        //数据区
        System.out.println(".data");
        //数组
        globalArrayInits.forEach((s, globalArrayInit) -> {
            globalArrayInit.output();
        });
        //全局变量
        globalVarInits.forEach((s, globalVarInit) -> {
            globalVarInit.output();
        });
        //字符串
        globalStrInits.forEach((s, globalStrInit) -> {
            globalStrInit.output();
        });
        System.out.println("\n\n\n");
        //代码区
        System.out.println(".text");

        for (MipsCode mipsCode : mipsCodes) {
            mipsCode.output();
        }
    }
}
