package MipsCode;

import MipsCode.MipsCode.MipsCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class RegisterPool {
    private HashMap<String, String> tempRegToVarMap;
    private HashMap<String, String> globalRegToVarMap;
    private HashMap<String, String> varToTempRegMap;
    private HashMap<String, String> varToGlobalRegMap;
    private LinkedList<String> LRUTempRegs;
    private LinkedList<String> LRUGlobalRegs;
    private HashSet<String> usedTempRegs;
    private ArrayList<String> tempRegs = new ArrayList<String>() {
        {
            add("$v1");
            add("$a0");
            add("$a1");
            add("$a2");
            add("$a3");
            add("$t0");
            add("$t1");
            add("$t2");
            add("$t3");
            add("$t4");
            add("$t5");
            add("$t6");
            add("$t7");
            add("$t8");
            add("$t9");
        }
    };
    private ArrayList<String> globalRegs = new ArrayList<String>() {
        {
            add("$s0");
            add("$s1");
            add("$s2");
            add("$s3");
            add("$s4");
            add("$s5");
            add("$s6");
            add("$s7");
            add("$fp");
        }
    };

    public RegisterPool() {
        this.globalRegToVarMap = new HashMap<>();
        this.tempRegToVarMap = new HashMap<>();
        this.varToGlobalRegMap = new HashMap<>();
        this.varToTempRegMap = new HashMap<>();
        this.LRUGlobalRegs = new LinkedList<>(globalRegs);
        this.LRUTempRegs = new LinkedList<>(tempRegs);
        this.usedTempRegs = new HashSet<>();
    }

    public String getVarNameOfTempReg(String reg) {
        return tempRegToVarMap.get(reg);
    }

    public String getLongestNotUsedTempReg() {
        return LRUTempRegs.get(LRUTempRegs.size() - 1);
    }

    public void setRecentLyUsedTempReg(String reg) {
        LRUTempRegs.remove(reg);
        LRUTempRegs.addFirst(reg);
    }

    public String getLongestNotUsedGlobalReg() {
        return LRUGlobalRegs.get(LRUGlobalRegs.size() - 1);
    }

    public void setRecentLyUsedGlobalReg(String reg) {
        LRUGlobalRegs.remove(reg);
        LRUGlobalRegs.addFirst(reg);
    }

    public ArrayList<String> getGlobalUsedRegs() {
        ArrayList<String> regs = new ArrayList<>();
        for (String globalReg : globalRegs) {
            if (globalRegToVarMap.containsKey(globalReg)) {
                regs.add(globalReg);
            }
        }
        return regs;
    }

    public String allocateRegToVarLoad(String varName, VarAddressOffset varAddressOffset,
                                   MipsVisitor mipsVisitor) {
        if (varName.equals("RET")) {
            return "$v0";
        }
        if (varName.startsWith("t@")) {
            return allocateTempRegToVar(varName, varAddressOffset, mipsVisitor, true);
        }  else {
            return allocateGlobalRegToVar(varName, varAddressOffset, mipsVisitor, true);
        }
    }

    public String allocateTempRegToNumber(VarAddressOffset varAddressOffset,
                                          MipsVisitor mipsVisitor) {

        for (String tempReg : tempRegs) {
            if (!usedTempRegs.contains(tempReg)) {
                setRecentLyUsedTempReg(tempReg);
                usedTempRegs.add(tempReg);
                return tempReg;
            }
        }
        String tempReg = getLongestNotUsedTempReg();

        setRecentLyUsedTempReg(tempReg);
        if (tempRegToVarMap.containsKey(tempReg)) {
            String tempVarName = tempRegToVarMap.get(tempReg);

            int offset = varAddressOffset.getVarOffset(tempVarName);
            MipsCode mipsCode = MipsCode.generateSW(tempReg, String.valueOf(offset), "$sp");
            mipsVisitor.addMipsCode(mipsCode);
            varToTempRegMap.remove(tempVarName);
            tempRegToVarMap.remove(tempReg);
        }


        return tempReg;
    }

    public String allocateTempRegToVar(String varName, VarAddressOffset varAddressOffset,
                                       MipsVisitor mipsVisitor, boolean load) {
        if (varToTempRegMap.containsKey(varName)) {
            String reg = varToTempRegMap.get(varName);
            setRecentLyUsedTempReg(reg);
            return reg;
        }
        for (String tempReg : tempRegs) {
            if (!usedTempRegs.contains(tempReg)) {
                setRecentLyUsedTempReg(tempReg);
                tempRegToVarMap.put(tempReg, varName);
                varToTempRegMap.put(varName, tempReg);
                usedTempRegs.add(tempReg);
                if (load) {
                    MipsCode loadNewReg =
                        MipsCode.generateLW(tempReg, String.valueOf(varAddressOffset.getVarOffset(varName)),
                            "$sp");
                    mipsVisitor.addMipsCode(loadNewReg);
                }

                return tempReg;
            }
        }
        String tempReg = getLongestNotUsedTempReg();
        setRecentLyUsedTempReg(tempReg);
        if (tempRegToVarMap.containsKey(tempReg)) {
            String tempVarName = tempRegToVarMap.get(tempReg);

            int offset = varAddressOffset.getVarOffset(tempVarName);
            MipsCode mipsCode = MipsCode.generateSW(tempReg, String.valueOf(offset), "$sp");
            mipsVisitor.addMipsCode(mipsCode);
            varToTempRegMap.remove(tempVarName);
        }
        varToTempRegMap.put(varName, tempReg);
        tempRegToVarMap.put(tempReg, varName);

        if (load) {
            MipsCode loadNewReg =
                MipsCode.generateLW(tempReg, String.valueOf(varAddressOffset.getVarOffset(varName)),
                    "$sp");
            mipsVisitor.addMipsCode(loadNewReg);
        }

        return tempReg;
    }

    public boolean varIsNumber(String var) {
        return var.matches("\\d+");
    }

    public String allocateGlobalRegToVar(String varName, VarAddressOffset varAddressOffset,
                                         MipsVisitor mipsVisitor, boolean load) {
//        if (varName.equals("10")) {
//            System.err.println("1111");
//        }
        if (varToGlobalRegMap.containsKey(varName)) {
            String reg = varToGlobalRegMap.get(varName);
            setRecentLyUsedGlobalReg(reg);
            return reg;
        }

        for (String globalReg : globalRegs) {
            if (!globalRegToVarMap.containsKey(globalReg)) {
                //不会有全局变量
                setRecentLyUsedGlobalReg(globalReg);
                globalRegToVarMap.put(globalReg, varName);
                varToGlobalRegMap.put(varName, globalReg);

                if (load) {
                    if (mipsVisitor.varIsGlobal(varName)) {
                        MipsCode loadNewReg =
                            MipsCode.generateLW(globalReg,
                                varName,
                                "$0");
                        mipsVisitor.addMipsCode(loadNewReg);
                    } else {
                        MipsCode loadNewReg =
                            MipsCode.generateLW(globalReg,
                                String.valueOf(varAddressOffset.getVarOffset(varName)),
                                "$sp");
                        mipsVisitor.addMipsCode(loadNewReg);
                    }
                }

                return globalReg;
            }
        }

        String globalReg = getLongestNotUsedGlobalReg();
        setRecentLyUsedGlobalReg(globalReg);
        String globalVarName = globalRegToVarMap.get(globalReg);

//        System.err.println(globalReg);
//        System.err.println(globalVarName);
        int offset = varAddressOffset.getVarOffset(globalVarName);
        MipsCode mipsCode = MipsCode.generateSW(globalReg, String.valueOf(offset), "$sp");
        mipsVisitor.addMipsCode(mipsCode);

        varToGlobalRegMap.remove(globalVarName);
        varToGlobalRegMap.put(varName, globalReg);
        globalRegToVarMap.put(globalReg, varName);



        if (load) {
            if (mipsVisitor.varIsGlobal(varName)) {
                MipsCode loadNewReg =
                    MipsCode.generateLW(globalReg,
                        varName,
                        "$0");
                mipsVisitor.addMipsCode(loadNewReg);
            } else {
                MipsCode loadNewReg =
                    MipsCode.generateLW(globalReg,
                        String.valueOf(varAddressOffset.getVarOffset(varName)),
                        "$sp");
                mipsVisitor.addMipsCode(loadNewReg);
            }
        }

        return globalReg;
    }

    //得到寄存器，并把其中的值写回内存
    public String getTempReg(boolean isNumber, VarAddressOffset varAddressOffset,
                             MipsVisitor mipsVisitor) {
        if (isNumber) {
            return allocateTempRegToNumber(varAddressOffset, mipsVisitor);
        }
        for (String tempReg : tempRegs) {
            if (!usedTempRegs.contains(tempReg)) {
                setRecentLyUsedTempReg(tempReg);
                usedTempRegs.add(tempReg);
                return tempReg;
            }
        }
        String tempReg = getLongestNotUsedTempReg();
        setRecentLyUsedTempReg(tempReg);

        if (tempRegToVarMap.containsKey(tempReg)) {
            String tempVarName = tempRegToVarMap.get(tempReg);
            int offset = varAddressOffset.getVarOffset(tempVarName);
            MipsCode mipsCode = MipsCode.generateSW(tempReg, String.valueOf(offset), "$sp");
            mipsVisitor.addMipsCode(mipsCode);
            varToTempRegMap.remove(tempVarName);
            tempRegToVarMap.remove(tempReg);
        }

        return tempReg;
    }

    public void clearSpecialReg(String reg, VarAddressOffset varAddressOffset,
                                MipsVisitor mipsVisitor) {
        if (globalRegs.contains(reg)) {
            setRecentLyUsedGlobalReg(reg);
            if (!globalRegToVarMap.containsKey(reg)) {
                return;
            }
            String globalVarName = tempRegToVarMap.get(reg);
            int offset = varAddressOffset.getVarOffset(globalVarName);
            MipsCode mipsCode = MipsCode.generateSW(reg, String.valueOf(offset), "$sp");
            mipsVisitor.addMipsCode(mipsCode);
            varToGlobalRegMap.remove(globalVarName);
            globalRegToVarMap.remove(reg);
        } else if (tempRegs.contains(reg)) {
            setRecentLyUsedTempReg(reg);
            if (!tempRegToVarMap.containsKey(reg)) {
                return;
            }
            String tempVarName = tempRegToVarMap.get(reg);
            int offset = varAddressOffset.getVarOffset(tempVarName);
            MipsCode mipsCode = MipsCode.generateSW(reg, String.valueOf(offset), "$sp");
            mipsVisitor.addMipsCode(mipsCode);
            varToTempRegMap.remove(tempVarName);
            tempRegToVarMap.remove(reg);
        }
    }

    public String allocateRegToVarNotLoad(String varName, VarAddressOffset varAddressOffset,
                                          MipsVisitor mipsVisitor) {
        if (varName.startsWith("t@")) {
            return allocateTempRegToVar(varName, varAddressOffset, mipsVisitor, false);
        } else {
            return allocateGlobalRegToVar(varName, varAddressOffset, mipsVisitor, false);
        }
    }

    public void clearAllTempRegs() {
        tempRegToVarMap = new HashMap<>();
        varToTempRegMap = new HashMap<>();
        LRUTempRegs = new LinkedList<>();
        usedTempRegs = new HashSet<>();
    }

    public void clearAllGlobalRegs() {
        globalRegToVarMap = new HashMap<>();
        varToGlobalRegMap = new HashMap<>();
        LRUGlobalRegs = new LinkedList<>();
        usedTempRegs = new HashSet<>();
    }

    public ArrayList<String> getTempRegs() {
        return tempRegs;
    }

    public ArrayList<String> getGlobalRegs() {
        return globalRegs;
    }

    public ArrayList<String> getUsedTempRegs() {
        return new ArrayList<>(tempRegToVarMap.keySet());
    }
}
