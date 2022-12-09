package IntermediateCode.FunctionCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import IntermediateCode.Operand;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;
import Tool.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FunctionCallCode extends IntermediateCode {

    public FunctionCallCode(Operand target) {
        super(target, null, null, Operator.CALL);
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
        HashSet<Operand> usedTempVars = new HashSet<>();
        basicBlock.addUsedTempVarForFunctionCall(usedTempVars, this);
        HashSet<String> regs = new HashSet<>(registerPool.getUsedTempRegs(usedTempVars));


        for (String reg : regs) {
//            System.err.println(reg);
//            System.err.println(registerPool.getVarNameOfTempReg(reg));
            registerPool.getVarNameOfTempReg(reg).storeToMemory(mipsVisitor, varAddressOffset, reg);
//            MipsCode storeUsedGlobalRegs = MipsCode.generateSW(reg,
//                String.valueOf(
//                    varAddressOffset.getVarOffset(registerPool.getVarNameOfTempReg(reg))), "$sp");
//            mipsVisitor.addMipsCode(storeUsedGlobalRegs);
        }
        mipsVisitor.addMipsCode(MipsCode.generateJAL(target.getName()));
        registerPool.clearAllTempRegs();
    }

    @Override
    public void output() {
        System.out.println("CALL " + target);

    }
}
