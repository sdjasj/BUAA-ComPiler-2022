package MipsCode;

import IntermediateCode.ConflictGraph;
import IntermediateCode.Operand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class VarAddressOffset {
    private HashMap<Operand, Integer> varOffsetMap;
    private HashMap<String, Integer> regOffsetMap;
    private int curOffset;
    private HashSet<Operand> params;

    public VarAddressOffset(int curOffset) {
        this.varOffsetMap = new HashMap<>();
        this.regOffsetMap = new HashMap<>();
        this.curOffset = curOffset;
        this.params = new HashSet<>();
    }

    public void addVar(Operand name, int offset) {
        curOffset += offset;
        varOffsetMap.put(name, curOffset);
    }

    public void addParam(Operand name, int offset) {
        addVar(name, offset);
        params.add(name);
    }

    public boolean isParam(Operand name) {
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

    public void addGlobalRegsAddress(ConflictGraph conflictGraph) {
        ArrayList<String> regs = new ArrayList<>(conflictGraph.getUsedGlobalRegs());
        addRegList(regs);
    }

    public int getVarOffset(Operand name) {
//        System.err.println(name);
        return curOffset - varOffsetMap.get(name);
    }

    public int getArrayOffset(Operand name, int offset) {
//        System.err.println(name.getName() + " " + name.getOperandType());
//        varOffsetMap.forEach((k,v) -> {
//            if (k.getName().equals(name.getName())) {
//                System.err.println(k.getName() + " " + k.getOperandType());
//            }
//        });
        return curOffset - varOffsetMap.get(name) + offset * 4;
    }

    public int getRegOffset(String name) {
        return curOffset - regOffsetMap.get(name);
    }

    public int getCurOffset() {
        return curOffset;
    }
}
