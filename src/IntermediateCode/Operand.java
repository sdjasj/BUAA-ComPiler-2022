package IntermediateCode;

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
    private boolean isParamLoaded = false;

    private static HashSet<Operand> oldName = new HashSet<>();

    public static Operand getNewOperand(String name, OperandType operandType) {
        for (Operand operand : oldName) {
            if (operand.operandType.equals(operandType) && operand.getName().equals(name)) {
                return operand;
            }
        }
        Operand operand = new Operand(name, operandType);
        oldName.add(operand);
        return operand;
    }

    public static void clearOperands() {
        oldName.clear();
    }

    private Operand(String name, OperandType operandType) {
        this.name = name;
        this.operandType = operandType;
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

    public boolean isGlobal() {
        return name.endsWith("_0");
    }

    public boolean isTemp() {
        return name.startsWith("t@");
    }

    public boolean isLocal() {
        return isVar() && !isGlobal() && !isTemp();
    }

    public void setReg(String reg) {
        this.reg = reg;
    }

    public void setAllocatedReg(boolean allocatedReg) {
        this.allocatedReg = allocatedReg;
    }

    public String getReg() {
        return reg;
    }

    public boolean isAllocatedReg() {
        return allocatedReg;
    }

    public void setParamLoaded(boolean paramLoaded) {
        isParamLoaded = paramLoaded;
    }

    public boolean isParamLoaded() {
        return isParamLoaded;
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
