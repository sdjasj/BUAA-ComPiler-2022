package MipsCode;

import IntermediateCode.BasicBlock;
import IntermediateCode.IntermediateCode;
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
    private HashSet<String> freezeRegs;
    public static ArrayList<String> tempRegs = new ArrayList<String>() {
        {
            add("$ra");
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
        }
    };

    public RegisterPool() {
        this.tempRegToVarMap = new HashMap<>();
        this.varToTempRegMap = new HashMap<>();
        this.LRUTempRegs = new LinkedList<>();
        this.usedTempRegs = new HashSet<>();
        this.freezeRegs = new HashSet<>();
    }

    public static void setTempRegs(ArrayList<String> tempRegs) {
        RegisterPool.tempRegs = tempRegs;
    }

    public void freeze(String reg) {
        freezeRegs.add(reg);
    }

    public void unFreeze(String reg) {
        freezeRegs.remove(reg);
    }

    public Operand getVarNameOfTempReg(String reg) {
        return tempRegToVarMap.get(reg);
    }

    public boolean containVarInReg(Operand var) {
        return varToTempRegMap.containsKey(var);
    }

    public String getLongestNotUsedTempReg() {
        for (int i = LRUTempRegs.size() - 1; i >= 0; i--) {
            if (!tempRegToVarMap.containsKey(LRUTempRegs.get(i)) ||
                tempRegToVarMap.containsKey(LRUTempRegs.get(i)) &&
                    tempRegToVarMap.get(LRUTempRegs.get(i)).isTemp()) {
                return LRUTempRegs.get(i);
            }
        }
        return LRUTempRegs.get(LRUTempRegs.size() - 1);
    }

    public String getOptTempReg(IntermediateCode intermediateCode, MipsVisitor mipsVisitor,
                                VarAddressOffset varAddressOffset) {
        String ans = null;
        Operand ansVar = null;
        int maxv = Integer.MIN_VALUE;
        for (String tempReg : tempRegs) {
            if (!freezeRegs.contains(tempReg)) {
                Operand varName = tempRegToVarMap.get(tempReg);
                if (varName == null) {
//                    System.err.println(intermediateCode.getTarget());
//                    System.err.println(varToTempRegMap);
//                    System.err.println(freezeRegs);
                    ans = tempReg;
                    maxv = Integer.MAX_VALUE;
                    break;
                }
                int t = intermediateCode.getBasicBlock().findUsedVarNextCode(intermediateCode, varName);
//                if (varName.getName().equals("t@26")) {
//                    System.err.println(intermediateCode);
//                    System.err.println(varName);
//                    System.err.println(t);
//                }
                if (t > maxv) {
                    maxv = t;
                    ans = tempReg;
                    ansVar = varName;
                }
            }
        }
//        if (ansVar != null && ansVar.getName().equals("t@26")) {
//            System.err.println("inter " + intermediateCode);
//            System.err.println(ansVar);
//            System.err.println(maxv);
//        }
//        System.err.println("inter " + intermediateCode.getTarget());
//        System.err.println(ansVar);
//        System.err.println(maxv)
        if (ansVar != null && maxv != Integer.MAX_VALUE &&
            (!ansVar.isGlobal() || (ansVar.isTemp() && ansVar.isAddress()))) {

            ansVar.storeToMemory(mipsVisitor, varAddressOffset, ans);
        }
        return ans;
    }

    public void setRecentLyUsedTempReg(String reg) {
        LRUTempRegs.remove(reg);
        LRUTempRegs.addFirst(reg);
    }

    public String allocateRegToVarLoad(Operand operand, VarAddressOffset varAddressOffset,
                                       MipsVisitor mipsVisitor, IntermediateCode intermediateCode) {
        String varName = operand.getName();
        if (varName.equals("RET")) {
            return "$v0";
        }
        if (operand.isAllocatedReg()) {
            return operand.getReg();
        }
        return allocateTempRegToVar(operand, varAddressOffset, mipsVisitor, intermediateCode,
            true);
    }

    public String allocateTempRegToNumber(VarAddressOffset varAddressOffset,
                                          MipsVisitor mipsVisitor,
                                          IntermediateCode intermediateCode) {

        for (String tempReg : tempRegs) {
            if (!usedTempRegs.contains(tempReg)) {
                usedTempRegs.add(tempReg);
                freeze(tempReg);
                return tempReg;
            }
        }

        String tempReg = getOptTempReg(intermediateCode, mipsVisitor, varAddressOffset);

        if (tempRegToVarMap.containsKey(tempReg)) {
            Operand tempVarName = tempRegToVarMap.get(tempReg);
            varToTempRegMap.remove(tempVarName);
            tempRegToVarMap.remove(tempReg);
        }

        freeze(tempReg);

        return tempReg;
    }

    public String allocateTempRegToVar(Operand varName, VarAddressOffset varAddressOffset,
                                       MipsVisitor mipsVisitor, IntermediateCode intermediateCode,
                                       boolean load) {
        if (varToTempRegMap.containsKey(varName)) {
            String reg = varToTempRegMap.get(varName);
//            System.err.println(varName);
//            System.err.println(reg);
            return reg;
        }

        for (String tempReg : tempRegs) {
            if (!usedTempRegs.contains(tempReg)) {
                tempRegToVarMap.put(tempReg, varName);
                varToTempRegMap.put(varName, tempReg);
                usedTempRegs.add(tempReg);
                if (load) {
                    varName.loadToReg(mipsVisitor, varAddressOffset, tempReg);
                }

                if (varName.isGlobal()) {
                    mipsVisitor.addMipsCode(MipsCode.generateADDIU(tempReg, "$gp",
                        String.valueOf(mipsVisitor.getOffsetByVar(varName.getName(), 0))));
                    return tempReg;
                }

                if (!varName.isTemp()) {
                    if (varName.isAddress() && !varAddressOffset.isParam(varName)) {
                        mipsVisitor.addMipsCode(MipsCode.generateADDIU(tempReg, "$sp",
                            String.valueOf(varAddressOffset.getArrayOffset(varName, 0))));
                    } else if (varName.isAddress() && varAddressOffset.isParam(varName)) {
                        mipsVisitor.addMipsCode(MipsCode.generateLW(tempReg,
                            String.valueOf(varAddressOffset.getArrayOffset(varName, 0)), "$sp"));
                    }

                    return tempReg;
                }

                return tempReg;
            }
        }

        String tempReg = getOptTempReg(intermediateCode, mipsVisitor, varAddressOffset);
        if (tempRegToVarMap.containsKey(tempReg)) {
            Operand tempVarName = tempRegToVarMap.get(tempReg);
            varToTempRegMap.remove(tempVarName);
        }
        varToTempRegMap.put(varName, tempReg);
        tempRegToVarMap.put(tempReg, varName);
//        System.err.println(varName + " " + tempReg + " " + tempRegToVarMap);

//        if (varName.getName().equals("t@26")) {
//            System.err.println(varName + " " +varToTempRegMap);
//            System.err.println(varName + " " + tempRegToVarMap);
//        } else if (varName.getName().equals("t@27")) {
//            System.err.println(varName + " " +varToTempRegMap);
//            System.err.println(varName + " " + tempRegToVarMap);
//        }

        if (load) {
            varName.loadToReg(mipsVisitor, varAddressOffset, tempReg);
        }

        if (varName.isGlobal()) {
            mipsVisitor.addMipsCode(MipsCode.generateADDIU(tempReg, "$gp",
                String.valueOf(mipsVisitor.getOffsetByVar(varName.getName(), 0))));
            return tempReg;
        }

        if (!varName.isTemp()) {
            if (varName.isAddress() && !varAddressOffset.isParam(varName)) {
                mipsVisitor.addMipsCode(MipsCode.generateADDIU(tempReg, "$sp",
                    String.valueOf(varAddressOffset.getArrayOffset(varName, 0))));
            } else if (varName.isAddress() && varAddressOffset.isParam(varName)) {
                mipsVisitor.addMipsCode(MipsCode.generateLW(tempReg,
                    String.valueOf(varAddressOffset.getArrayOffset(varName, 0)), "$sp"));
            }

            return tempReg;
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
                             MipsVisitor mipsVisitor, IntermediateCode intermediateCode) {
        if (isNumber) {
            return allocateTempRegToNumber(varAddressOffset, mipsVisitor, intermediateCode);
        }
        for (String tempReg : tempRegs) {
            if (!usedTempRegs.contains(tempReg)) {
                usedTempRegs.add(tempReg);
                freeze(tempReg);
                return tempReg;
            }
        }
        String tempReg = getOptTempReg(intermediateCode, mipsVisitor, varAddressOffset);
        if (tempRegToVarMap.containsKey(tempReg)) {
            Operand tempVarName = tempRegToVarMap.get(tempReg);
            varToTempRegMap.remove(tempVarName);
            tempRegToVarMap.remove(tempReg);
        }

        freeze(tempReg);

        return tempReg;
    }

    public void clearSpecialReg(Operand operand, String reg, VarAddressOffset varAddressOffset,
                                MipsVisitor mipsVisitor, IntermediateCode intermediateCode) {
        if (usedTempRegs.contains(reg)) {
            if (!tempRegToVarMap.containsKey(reg)) {
                return;
            }
            if (tempRegToVarMap.get(reg).equals(operand)) {
                return;
            }
            Operand tempVarName = tempRegToVarMap.get(reg);
            if (intermediateCode.getBasicBlock()
                .findUsedVarNextCode(intermediateCode, tempVarName) !=
                Integer.MAX_VALUE) {
                tempVarName.storeToMemory(mipsVisitor, varAddressOffset, reg);
            }
            varToTempRegMap.remove(tempVarName);
            tempRegToVarMap.remove(reg);
        }
    }

    public String allocateRegToVarNotLoad(Operand operand, VarAddressOffset varAddressOffset,
                                          MipsVisitor mipsVisitor, IntermediateCode intermediateCode) {
        if (operand.isAllocatedReg()) {
            return operand.getReg();
        }
        return allocateTempRegToVar(operand, varAddressOffset, mipsVisitor, intermediateCode,false);
    }

    public void clearAllTempRegs() {
        tempRegToVarMap = new HashMap<>();
        varToTempRegMap = new HashMap<>();
        LRUTempRegs = new LinkedList<>();
        usedTempRegs = new HashSet<>();
        freezeRegs = new HashSet<>();
    }

    public ArrayList<String> getUsedTempRegs(HashSet<Operand> usedTempVars) {
        ArrayList<String> regs = new ArrayList<>();
        varToTempRegMap.forEach((k,v) -> {
            if (usedTempVars.contains(k) || k.isLocal()) {
                regs.add(v);
            }
        });
        return regs;
    }

    public HashMap<Operand, String> getVarToTempRegMap() {
        return new HashMap<>(varToTempRegMap);
    }

    public ArrayList<Operand> getVarInReg() {
        return new ArrayList<>(tempRegToVarMap.values());
    }
}
