package IntermediateCode.FunctionCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operand;
import IntermediateCode.Operator;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;
import Tool.Optimizer;
import Tool.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FunctionReturnCode extends IntermediateCode {

    public FunctionReturnCode(Operand target) {
        super(null, target, null, Operator.RETURN);
    }

    public FunctionReturnCode() {
        super(null, null, null, Operator.RETURN);
    }

    @Override
    public Operand getLeftVal() {
        return null;
    }

    @Override
    public Pair<Operand, Operand> getRightVal() {
        return new Pair<Operand, Operand>(source1, null);
    }

    @Override
    public HashSet<Operand> getUsedSet() {
        HashSet<Operand> ans = new HashSet<>();
        if (source1 != null) {
            ans.add(source1);
        }
        return ans;
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {

        ArrayList<String> regs = registerPool.getGlobalUsedTempRegs();

        for (String reg : regs) {
//            System.err.println(reg);
//            System.err.println(registerPool.getVarNameOfTempReg(reg));
            registerPool.getVarNameOfTempReg(reg).storeToMemory(mipsVisitor, varAddressOffset, reg);
        }

        if (source1 == null) {
//            ArrayList<String> usedGlobalRegs = new ArrayList<>(conflictGraph.getUsedGlobalRegs());
//            for (String usedGlobalReg : usedGlobalRegs) {
//                MipsCode storeUsedGlobalRegs = MipsCode.generateLW(usedGlobalReg,
//                    String.valueOf(varAddressOffset.getRegOffset(usedGlobalReg)), "$sp");
//                mipsVisitor.addMipsCode(storeUsedGlobalRegs);
//            }

            if (Optimizer.RaOptimizer && basicBlock.getFunction().isCallOtherFunc()) {
                mipsVisitor.addMipsCode(
                    MipsCode.generateLW("$ra", String.valueOf(varAddressOffset.getRegOffset("$ra")),
                        "$sp"));
            }

            //reset sp
            int offset = varAddressOffset.getCurOffset();
            MipsCode mipsCode = MipsCode.generateADDIU("$sp", "$sp", String.valueOf(offset));
            mipsVisitor.addMipsCode(mipsCode);

            mipsVisitor.addMipsCode(MipsCode.generateJR("$ra"));
            return;
        }

        if (source1.isNUMBER()) {
            mipsVisitor.addMipsCode(MipsCode.generateLi("$v0", source1.getName()));
        } else {
            String targetReg = getSrcReg(source1, varAddressOffset, mipsVisitor, registerPool);
            mipsVisitor.addMipsCode(MipsCode.generateMOVE("$v0", targetReg));
            registerPool.unFreeze(targetReg);
        }

//        ArrayList<String> usedGlobalRegs = new ArrayList<>(conflictGraph.getUsedGlobalRegs());
//        for (String usedGlobalReg : usedGlobalRegs) {
//            MipsCode storeUsedGlobalRegs = MipsCode.generateLW(usedGlobalReg,
//                String.valueOf(varAddressOffset.getRegOffset(usedGlobalReg)), "$sp");
//            mipsVisitor.addMipsCode(storeUsedGlobalRegs);
//        }

        if (Optimizer.RaOptimizer && basicBlock.getFunction().isCallOtherFunc()) {
            mipsVisitor.addMipsCode(
                MipsCode.generateLW("$ra", String.valueOf(varAddressOffset.getRegOffset("$ra")),
                    "$sp"));
        }

        //reset sp
        int offset = varAddressOffset.getCurOffset();
        MipsCode mipsCode = MipsCode.generateADDIU("$sp", "$sp", String.valueOf(offset));
        mipsVisitor.addMipsCode(mipsCode);

        mipsVisitor.addMipsCode(MipsCode.generateJR("$ra"));
    }

    @Override
    public void output() {
        if (source1 == null) {
            //void
            System.out.println(op);
        } else {
            System.out.println(op + " " + source1);
        }
    }
}
