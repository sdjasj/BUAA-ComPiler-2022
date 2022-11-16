package IntermediateCode.FunctionCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import IntermediateCode.Operand;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;
import Tool.Pair;

import java.util.List;

public class FunctionEndCode extends IntermediateCode {
    public FunctionEndCode(Operand target) {
        super(target, null, null, Operator.FUNC_END);
    }

    @Override
    public Operand getLeftVal() {
        return null;
    }

    @Override
    public Pair<Operand, Operand> getRightVal() {
        return null;
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {
        mipsVisitor.addMipsCode(MipsCode.generateComment("end " + target.getName() + "\n\n\n"));
    }

    @Override
    public void output() {
        System.out.println(op + " " + target);
        System.out.println();
        System.out.println();
    }
}
