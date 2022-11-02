package IntermediateCode.AllCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operand;
import IntermediateCode.Operator;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;

public class JumpCode extends IntermediateCode {
    public JumpCode(Operand target, Operator op) {
        super(target, null, null, op);
    }

    @Override
    public void output() {
        System.out.println("JUMP " + target);
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {
        mipsVisitor.addMipsCode(MipsCode.generateJ(target.getName()));
    }
}
