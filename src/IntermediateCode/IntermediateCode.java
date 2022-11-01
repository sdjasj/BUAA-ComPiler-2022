package IntermediateCode;

import IntermediateCode.Operand;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;

public class IntermediateCode {
    protected Operand target;
    protected Operand source1;
    protected Operand source2;
    protected Operator op;

    public IntermediateCode(Operand target, Operand source1, Operand source2, Operator op) {
        this.target = target;
        this.source1 = source1;
        this.source2 = source2;
        this.op = op;
    }

    public Operand getTarget() {
        return target;
    }

    public Operand getSource1() {
        return source1;
    }

    public Operand getSource2() {
        return source2;
    }

    public Operator getOp() {
        return op;
    }

    public void output() {

    }

    public String addressMul4(String addr) {
        return String.valueOf(Integer.parseInt(addr) * 4);
    }

    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {

    }

}
