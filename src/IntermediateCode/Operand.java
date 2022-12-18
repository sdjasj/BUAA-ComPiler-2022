package IntermediateCode;

import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.VarAddressOffset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class Operand {
    public enum OperandType {
        ADDRESS,
        NUMBER,
        VAR,
    }
    private String name;
    private OperandType operandType;
    private String reg;
    private boolean allocatedReg;
    private boolean global = false;
    private boolean isConst = false;
    public static int curLoopDepth = 0;
    private int loopDepth = 0;
    private boolean crossBlock = false;

    private static HashSet<Operand> oldName = new HashSet<>();

    public static Operand getNewOperand(String name, OperandType operandType) {
        for (Operand operand : oldName) {
            if (operand.operandType.equals(operandType) && operand.getName().equals(name)) {
                operand.setLoopDepth(curLoopDepth);
                return operand;
            }
        }
        Operand operand = new Operand(name, operandType);
        oldName.add(operand);
        operand.setLoopDepth(curLoopDepth);
        return operand;
    }

    public void setCrossBlock(boolean crossBlock) {
        this.crossBlock = crossBlock;
    }

    public boolean isCrossBlock() {
        return crossBlock;
    }

    public static void clearOperands() {
        oldName.clear();
    }

    private Operand(String name, OperandType operandType) {
        this.name = name;
        this.operandType = operandType;
    }

    public void setLoopDepth(int loopDepth) {
        this.loopDepth = Math.max(loopDepth, 1 << loopDepth);
    }

    public boolean isConst() {
        return isConst;
    }

    public void setConst(boolean aConst) {
        isConst = aConst;
    }

    public int getLoopDepth() {
        return loopDepth;
    }

    public String getName() {
        return name;
    }

    public OperandType getOperandType() {
        return operandType;
    }

    public boolean isNUMBER() {
        return operandType == OperandType.NUMBER;
    }

    public boolean isVar() {
        return operandType == OperandType.VAR;
    }

    public boolean isAddress() {
        return operandType == OperandType.ADDRESS;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public boolean isGlobal() {
        return name.endsWith("_0") || global;
    }

    public boolean isTemp() {
        return name.startsWith("t@");
    }

    public boolean isLocal() {
        return !name.startsWith("$") && !isGlobal() && !isTemp();
    }

    public boolean isLocalAndVar() {
        return isVar() && !isGlobal() && !isTemp() && !name.equals("RET");
    }

    public void setReg(String reg) {
        this.reg = reg;
    }

    public void setAllocatedReg(boolean allocatedReg) {
        this.allocatedReg = allocatedReg;
    }

    public String getReg() {
//        System.err.println(name + " " + reg);
        return reg;
    }

    public boolean isAllocatedReg() {
        return allocatedReg;
    }

    public void storeToMemory(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset, String reg) {
        mipsVisitor.addMipsCode(MipsCode.generateComment("store " + name));
        if (isGlobal()) {
            if (isVar()) {
                mipsVisitor.addMipsCode(
                    MipsCode.generateSW(reg, String.valueOf(mipsVisitor.getOffsetByVar(name, 0)),
                        "$gp"));
            } else {
                System.err.println("error in store global reg");
            }
        } else if (isLocalAndVar() || isTemp()) {
            mipsVisitor.addMipsCode(
                MipsCode.generateSW(reg, String.valueOf(varAddressOffset.getVarOffset(this)),
                    "$sp"));
        }
    }

    public void loadToReg(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset, String reg) {
        if (isGlobal()) {
            if (isVar()) {
                mipsVisitor.addMipsCode(
                    MipsCode.generateLW(reg, String.valueOf(mipsVisitor.getOffsetByVar(name, 0)),
                        "$gp"));
            } else {
                System.err.println("error in load global reg");
            }
        } else {
            mipsVisitor.addMipsCode(
                MipsCode.generateLW(reg, String.valueOf(varAddressOffset.getVarOffset(this)),
                    "$sp"));
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, operandType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(name, ((Operand) obj).name) &&
            Objects.equals(operandType, ((Operand) obj).operandType);
    }
}
