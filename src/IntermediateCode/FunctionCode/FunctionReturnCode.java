package IntermediateCode.FunctionCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operand;
import IntermediateCode.Operator;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;
import Tool.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FunctionReturnCode extends IntermediateCode {

    public FunctionReturnCode(Operand target) {
        super(target, null, null, Operator.RETURN);
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
        return new Pair<Operand, Operand>(target, null);
    }

    @Override
    public HashSet<Operand> getUsedSet() {
        HashSet<Operand> ans = new HashSet<>();
        if (target != null) {
            ans.add(target);
        }
        return ans;
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {

        if (target == null) {
            ArrayList<String> usedGlobalRegs = new ArrayList<>(conflictGraph.getUsedGlobalRegs());
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
            String targetReg = getSrcReg(target, varAddressOffset, mipsVisitor, registerPool);
            mipsVisitor.addMipsCode(MipsCode.generateMOVE("$v0", targetReg));
        }

        ArrayList<String> usedGlobalRegs = new ArrayList<>(conflictGraph.getUsedGlobalRegs());
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
