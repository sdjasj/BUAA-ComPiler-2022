package IntermediateCode.FunctionCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import IntermediateCode.Operand;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;

import java.util.ArrayList;

public class FunctionCallCode extends IntermediateCode {

    public FunctionCallCode(Operand target) {
        super(target, null, null, Operator.CALL);
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {

        ArrayList<String> regs = registerPool.getUsedTempRegs();
        for (String reg : regs) {
//            System.err.println(reg);
//            System.err.println(registerPool.getVarNameOfTempReg(reg));
            MipsCode storeUsedGlobalRegs = MipsCode.generateSW(reg,
                String.valueOf(
                    varAddressOffset.getVarOffset(registerPool.getVarNameOfTempReg(reg))), "$sp");
            mipsVisitor.addMipsCode(storeUsedGlobalRegs);
        }
        registerPool.clearAllTempRegs();
        mipsVisitor.addMipsCode(MipsCode.generateJAL(target.getName()));
    }

    @Override
    public void output() {
        System.out.println("CALL " + target);

    }
}
