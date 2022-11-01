package IntermediateCode;

public class Operand {
    public enum OperandType {
        ADDRESS,
        NUMBER,
        VAR,
    }
    private String name;
    private OperandType operandType;

    public Operand(String name, OperandType operandType) {
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

    @Override
    public String toString() {
        return name;
    }
}
