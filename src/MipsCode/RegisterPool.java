package MipsCode;

import IntermediateCode.Operand;
import MipsCode.MipsCode.MipsCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class RegisterPool {
    private HashMap<String, Operand> tempRegToVarMap;
    private HashMap<Operand, String> varToTempRegMap;
    private LinkedList<String> LRUTempRegs;
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

    public RegisterPool() {
        this.tempRegToVarMap = new HashMap<>();
        this.varToTempRegMap = new HashMap<>();
        this.LRUTempRegs = new LinkedList<>(tempRegs);
        this.usedTempRegs = new HashSet<>();
    }

    public Operand getVarNameOfTempReg(String reg) {
        return tempRegToVarMap.get(reg);
    }

    public String getLongestNotUsedTempReg() {
        return LRUTempRegs.get(LRUTempRegs.size() - 1);
    }

    public void setRecentLyUsedTempReg(String reg) {
        LRUTempRegs.remove(reg);
        LRUTempRegs.addFirst(reg);
    }

    public String allocateRegToVarLoad(Operand operand, VarAddressOffset varAddressOffset,
                                       MipsVisitor mipsVisitor) {
        String varName = operand.getName();
        if (varName.equals("RET")) {
            return "$v0";
        }
        if (operand.isTemp()) {
            return allocateTempRegToVar(operand, varAddressOffset, mipsVisitor, true);
        } else {
            if (operand.isAllocatedReg()) {
                return operand.getReg();
            }
            return allocateTempRegToVar(operand, varAddressOffset, mipsVisitor, true);
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
            Operand tempVarName = tempRegToVarMap.get(tempReg);

            int offset = varAddressOffset.getVarOffset(tempVarName);
            MipsCode mipsCode = MipsCode.generateSW(tempReg, String.valueOf(offset), "$sp");
            mipsVisitor.addMipsCode(mipsCode);
            varToTempRegMap.remove(tempVarName);
            tempRegToVarMap.remove(tempReg);
        }


        return tempReg;
    }

    public String allocateTempRegToVar(Operand varName, VarAddressOffset varAddressOffset,
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
                        MipsCode.generateLW(tempReg,
                            String.valueOf(varAddressOffset.getVarOffset(varName)), "$sp");
                    mipsVisitor.addMipsCode(loadNewReg);
                }

                return tempReg;
            }
        }
        String tempReg = getLongestNotUsedTempReg();
        setRecentLyUsedTempReg(tempReg);
        if (tempRegToVarMap.containsKey(tempReg)) {
            Operand tempVarName = tempRegToVarMap.get(tempReg);

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

//    public String allocateGlobalRegToVar(String varName, VarAddressOffset varAddressOffset,
//                                         MipsVisitor mipsVisitor, boolean load) {
////        if (varName.equals("10")) {
////            System.err.println("1111");
////        }
//        if (varToGlobalRegMap.containsKey(varName)) {
//            String reg = varToGlobalRegMap.get(varName);
//            setRecentLyUsedGlobalReg(reg);
//            return reg;
//        }
//
//        for (String globalReg : globalRegs) {
//            if (!globalRegToVarMap.containsKey(globalReg)) {
//                //不会有全局变量
//                setRecentLyUsedGlobalReg(globalReg);
//                globalRegToVarMap.put(globalReg, varName);
//                varToGlobalRegMap.put(varName, globalReg);
//
//                if (load) {
//                    if (mipsVisitor.varIsGlobal(varName)) {
//                        MipsCode loadNewReg =
//                            MipsCode.generateLW(globalReg,
//                                varName,
//                                "$0");
//                        mipsVisitor.addMipsCode(loadNewReg);
//                    } else {
//                        MipsCode loadNewReg =
//                            MipsCode.generateLW(globalReg,
//                                String.valueOf(varAddressOffset.getVarOffset(varName)),
//                                "$sp");
//                        mipsVisitor.addMipsCode(loadNewReg);
//                    }
//                }
//
//                return globalReg;
//            }
//        }
//
//        String globalReg = getLongestNotUsedGlobalReg();
//        setRecentLyUsedGlobalReg(globalReg);
//        String globalVarName = globalRegToVarMap.get(globalReg);
//
////        System.err.println(globalReg);
////        System.err.println(globalVarName);
//        int offset = varAddressOffset.getVarOffset(globalVarName);
//        MipsCode mipsCode = MipsCode.generateSW(globalReg, String.valueOf(offset), "$sp");
//        mipsVisitor.addMipsCode(mipsCode);
//
//        varToGlobalRegMap.remove(globalVarName);
//        varToGlobalRegMap.put(varName, globalReg);
//        globalRegToVarMap.put(globalReg, varName);
//
//
//
//        if (load) {
//            if (mipsVisitor.varIsGlobal(varName)) {
//                MipsCode loadNewReg =
//                    MipsCode.generateLW(globalReg,
//                        varName,
//                        "$0");
//                mipsVisitor.addMipsCode(loadNewReg);
//            } else {
//                MipsCode loadNewReg =
//                    MipsCode.generateLW(globalReg,
//                        String.valueOf(varAddressOffset.getVarOffset(varName)),
//                        "$sp");
//                mipsVisitor.addMipsCode(loadNewReg);
//            }
//        }
//
//        return globalReg;
//    }

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
            Operand tempVarName = tempRegToVarMap.get(tempReg);
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
        if (tempRegs.contains(reg)) {
            setRecentLyUsedTempReg(reg);
            if (!tempRegToVarMap.containsKey(reg)) {
                return;
            }
            Operand tempVarName = tempRegToVarMap.get(reg);
            int offset = varAddressOffset.getVarOffset(tempVarName);
            MipsCode mipsCode = MipsCode.generateSW(reg, String.valueOf(offset), "$sp");
            mipsVisitor.addMipsCode(mipsCode);
            varToTempRegMap.remove(tempVarName);
            tempRegToVarMap.remove(reg);
        }
    }

    public String allocateRegToVarNotLoad(Operand operand, VarAddressOffset varAddressOffset,
                                          MipsVisitor mipsVisitor) {
        if (operand.isTemp()) {
            return allocateTempRegToVar(operand, varAddressOffset, mipsVisitor, false);
        } else {
            if (operand.isAllocatedReg()) {
                return operand.getReg();
            }
            return allocateTempRegToVar(operand, varAddressOffset, mipsVisitor, false);
        }
    }

    public void clearAllTempRegs() {
        tempRegToVarMap = new HashMap<>();
        varToTempRegMap = new HashMap<>();
        LRUTempRegs = new LinkedList<>();
        usedTempRegs = new HashSet<>();
    }

    public ArrayList<String> getUsedTempRegs() {
        return new ArrayList<>(tempRegToVarMap.keySet());
    }

    public HashMap<Operand, String> getVarToTempRegMap() {
        return new HashMap<>(varToTempRegMap);
    }

    public ArrayList<Operand> getVarInReg() {
        return new ArrayList<>(tempRegToVarMap.values());
    }
}
