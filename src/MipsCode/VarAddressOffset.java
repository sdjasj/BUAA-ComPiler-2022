package MipsCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class VarAddressOffset {
    private HashMap<String, Integer> varOffsetMap;
    private HashMap<String, Integer> regOffsetMap;
    private int curOffset;
    private HashSet<String> params;

    public VarAddressOffset(int curOffset) {
        this.varOffsetMap = new HashMap<>();
        this.regOffsetMap = new HashMap<>();
        this.curOffset = curOffset;
        this.params = new HashSet<>();
    }

    public void addVar(String name, int offset) {
        curOffset += offset;
        varOffsetMap.put(name, curOffset);
    }

    public void addParam(String name, int offset) {
        addVar(name, offset);
        params.add(name);
    }

    public boolean isParam(String name) {
        return params.contains(name);
    }

    public void addReg(String name) {
        curOffset += 4;
        regOffsetMap.put(name, curOffset);
    }

    public void addRegList(ArrayList<String> regs) {
        for (String reg : regs) {
            curOffset += 4;
            regOffsetMap.put(reg, curOffset);
        }
    }

    public void addGlobalRegsAddress(RegisterPool registerPool) {
        ArrayList<String> regs = registerPool.getGlobalUsedRegs();
        addRegList(regs);
    }

    public void addAllRegs(RegisterPool registerPool) {
        ArrayList<String> globalRegs = registerPool.getGlobalRegs();
        addRegList(globalRegs);
    }

    public int getVarOffset(String name) {
//        System.err.println(name);
        return curOffset - varOffsetMap.get(name);
    }

    public int getArrayOffset(String name, int offset) {
        return curOffset - varOffsetMap.get(name) - offset * 4;
    }

    public int getRegOffset(String name) {
        return curOffset - regOffsetMap.get(name);
    }

    public int getCurOffset() {
        return curOffset;
    }
}
