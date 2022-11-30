package IntermediateCode.FunctionCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import MipsCode.MipsCode.MipsBranchCode;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;
import IntermediateCode.Operator;
import IntermediateCode.Operand;
import Tool.Optimizer;
import Tool.Pair;

import java.util.List;

public class ExitCode extends IntermediateCode {
    public static int fuck = 0;

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
        int curFuck = fuck;
        if (Optimizer.Mark) {
            mipsVisitor.addMipsCode(MipsCode.generateLi("$4", "10000"));
            mipsVisitor.addMipsCode(MipsCode.generateLi("$5", "0"));
            mipsVisitor.addMipsCode(MipsCode.generateLable("fuck" + curFuck));
            mipsVisitor.addMipsCode(new MipsBranchCode("bge", "fuck" + curFuck + 1, "$5", "$4"));
            mipsVisitor.addMipsCode(MipsCode.generateDIV("$4", "$5"));
            mipsVisitor.addMipsCode(MipsCode.generateADDIU("$5", "$5", "1"));
            mipsVisitor.addMipsCode(MipsCode.generateJ("fuck" + curFuck));
            mipsVisitor.addMipsCode(MipsCode.generateLable("fuck" + curFuck + 1));
        }
        mipsVisitor.addMipsCode(MipsCode.generateLi("$v0", "10"));
        mipsVisitor.addMipsCode(MipsCode.generateSYSCALL());
        fuck += 2;
    }

    @Override
    public void output() {
        System.out.println(op);
        System.out.println();
        System.out.println();
    }
}
