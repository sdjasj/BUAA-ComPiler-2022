package IntermediateCode.AllCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operand;
import IntermediateCode.Operator;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;
import Tool.Pair;

import java.util.List;


public class InputCode extends IntermediateCode {

    public InputCode(Operand target) {
        super(target, null, null, Operator.SCANF);
    }

    @Override
    public Pair<Operand, Operand> getRightVal() {
        return null;
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {
        //comment
        mipsVisitor.addMipsCode(MipsCode.generateComment("scanf " + target));


        registerPool.clearSpecialReg(target,"$v0", varAddressOffset, mipsVisitor, this);
        MipsCode mipsCode = MipsCode.generateLi("$v0", "5");
        mipsVisitor.addMipsCode(mipsCode);
        mipsVisitor.addMipsCode(MipsCode.generateSYSCALL());
        if (target.isGlobal()) {
            mipsVisitor.addMipsCode(MipsCode.generateSW("$v0", target.getName(), "$0"));
        } else {
            String res =
                registerPool.allocateRegToVarNotLoad(target, varAddressOffset, mipsVisitor, this);
            mipsVisitor.addMipsCode(MipsCode.generateMOVE(res, "$v0"));
        }
    }

    @Override
    public void output() {
        System.out.println(op + " " + target);
    }
}
