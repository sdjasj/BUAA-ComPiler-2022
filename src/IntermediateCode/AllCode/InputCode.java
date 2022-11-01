package IntermediateCode.AllCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operand;
import IntermediateCode.Operator;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;


public class InputCode extends IntermediateCode {

    public InputCode(Operand target) {
        super(target, null, null, Operator.SCANF);
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {
        //comment
        mipsVisitor.addMipsCode(MipsCode.generateComment("scanf " + target));


        registerPool.clearSpecialReg("$v0", varAddressOffset, mipsVisitor);
        MipsCode mipsCode = MipsCode.generateLi("$v0", "5");
        mipsVisitor.addMipsCode(mipsCode);
        mipsVisitor.addMipsCode(MipsCode.generateSYSCALL());
        if (mipsVisitor.varIsGlobal(target.getName())) {
            mipsVisitor.addMipsCode(MipsCode.generateSW("$v0", target.getName(), "$0"));
        } else {
            String res =
                registerPool.allocateRegToVarNotLoad(target.getName(), varAddressOffset, mipsVisitor);
            mipsVisitor.addMipsCode(MipsCode.generateMOVE(res, "$v0"));
        }
    }

    @Override
    public void output() {
        System.out.println(op + " " + target);
    }
}
