package IntermediateCode.FunctionCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operand;
import IntermediateCode.Operator;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;

import java.util.ArrayList;

public class FunctionReturnCode extends IntermediateCode {

    public FunctionReturnCode(Operand target) {
        super(target, null, null, Operator.RETURN);
    }

    public FunctionReturnCode() {
        super(null, null, null, Operator.RETURN);
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {

        if (target == null) {
            ArrayList<String> usedGlobalRegs = registerPool.getGlobalRegs();
            for (String usedGlobalReg : usedGlobalRegs) {
                MipsCode storeUsedGlobalRegs = MipsCode.generateLW(usedGlobalReg,
                    String.valueOf(varAddressOffset.getRegOffset(usedGlobalReg)), "$sp");
                mipsVisitor.addMipsCode(storeUsedGlobalRegs);
            }

            mipsVisitor.addMipsCode(
                MipsCode.generateLW("$ra", String.valueOf(varAddressOffset.getRegOffset("$ra")),
                    "$sp"));

            //reset sp
            int offset = varAddressOffset.getCurOffset();
            MipsCode mipsCode = MipsCode.generateADDIU("$sp", "$sp", String.valueOf(offset));
            mipsVisitor.addMipsCode(mipsCode);

            mipsVisitor.addMipsCode(MipsCode.generateJR("$ra"));
            return;
        }

        if (target.isNUMBER()) {
            mipsVisitor.addMipsCode(MipsCode.generateLi("$v0", target.getName()));
        } else {
            mipsVisitor.addMipsCode(MipsCode.generateMOVE("$v0",
                registerPool.allocateRegToVarLoad(target.getName(), varAddressOffset,
                    mipsVisitor)));
        }

        ArrayList<String> usedGlobalRegs = registerPool.getGlobalRegs();
        for (String usedGlobalReg : usedGlobalRegs) {
            MipsCode storeUsedGlobalRegs = MipsCode.generateLW(usedGlobalReg,
                String.valueOf(varAddressOffset.getRegOffset(usedGlobalReg)), "$sp");
            mipsVisitor.addMipsCode(storeUsedGlobalRegs);
        }

        mipsVisitor.addMipsCode(
            MipsCode.generateLW("$ra", String.valueOf(varAddressOffset.getRegOffset("$ra")),
                "$sp"));

        //reset sp
        int offset = varAddressOffset.getCurOffset();
        MipsCode mipsCode = MipsCode.generateADDIU("$sp", "$sp", String.valueOf(offset));
        mipsVisitor.addMipsCode(mipsCode);

        mipsVisitor.addMipsCode(MipsCode.generateJR("$ra"));
    }

    @Override
    public void output() {
        if (target == null) {
            //void
            System.out.println(op);
        } else {
            System.out.println(op + " " + target);
        }
    }
}
