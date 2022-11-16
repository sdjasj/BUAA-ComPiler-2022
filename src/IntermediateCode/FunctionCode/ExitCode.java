package IntermediateCode.FunctionCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;
import IntermediateCode.Operator;
import IntermediateCode.Operand;
import Tool.Pair;

import java.util.List;

public class ExitCode extends IntermediateCode {

    public ExitCode() {
        super(null, null, null, Operator.EXIT);
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
        mipsVisitor.addMipsCode(MipsCode.generateLi("$v0", "10"));
        mipsVisitor.addMipsCode(MipsCode.generateSYSCALL());
    }

    @Override
    public void output() {
        System.out.println(op);
        System.out.println();
        System.out.println();
    }
}
