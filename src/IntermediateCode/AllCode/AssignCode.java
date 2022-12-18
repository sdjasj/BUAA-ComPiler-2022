package IntermediateCode.AllCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import IntermediateCode.Operand;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;

public class AssignCode extends IntermediateCode {

    public AssignCode(Operand target, Operand source1) {
        super(target, source1, null, Operator.ASSIGN);
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {
        //comment
        mipsVisitor.addMipsCode(MipsCode.generateComment("assign " + target + " by " + source1));

//        System.err.println(this);
        String src1Reg = getSrcReg(source1, varAddressOffset, mipsVisitor, registerPool);
        String targetReg =
            registerPool.allocateRegToVarNotLoad(target, varAddressOffset,
                mipsVisitor, this);
        MipsCode moveCode =
            MipsCode.generateMOVE(targetReg, src1Reg);
//        System.err.println(this);
//        System.err.println(targetReg +" " + src1Reg);
        mipsVisitor.addMipsCode(moveCode);

        registerPool.unFreeze(src1Reg);


//        if (mipsVisitor.varIsGlobal(target.getName())) {
//            if (getSource1().isNUMBER()) {
//                String tempReg =
//                    registerPool.getTempReg(true, varAddressOffset, mipsVisitor);
//                MipsCode liCode = MipsCode.generateLi(tempReg, source1.getName());
//                mipsVisitor.addMipsCode(liCode);
//                MipsCode storeCode = MipsCode.generateSW(tempReg, target.getName(), "$0");
//                mipsVisitor.addMipsCode(storeCode);
//            } else if (mipsVisitor.varIsGlobal(source1.getName())) {
//                String tempReg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
//                MipsCode loadCode = MipsCode.generateLW(tempReg, source1.getName(), "$0");
//                mipsVisitor.addMipsCode(loadCode);
//
//                mipsVisitor.addMipsCode(MipsCode.generateSW(tempReg, target.getName(), "$0"));
//
//            } else {
//                MipsCode storeCode =
//                    MipsCode.generateSW(
//                        registerPool.allocateRegToVarLoad(source1.getName(), varAddressOffset,
//                            mipsVisitor),
//                        target.getName(), "$0");
//                mipsVisitor.addMipsCode(storeCode);
//            }
//        } else {
//            if (getSource1().isNUMBER()) {
//                String tempReg =
//                    registerPool.allocateRegToVarNotLoad(target.getName(), varAddressOffset,
//                        mipsVisitor);
//                MipsCode liCode = MipsCode.generateLi(tempReg, source1.getName());
//                mipsVisitor.addMipsCode(liCode);
//            } else if (mipsVisitor.varIsGlobal(source1.getName())) {
//                String tempReg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
//                MipsCode loadCode = MipsCode.generateLW(tempReg, source1.getName(), "$0");
//                mipsVisitor.addMipsCode(loadCode);
//
//                String targetReg =
//                    registerPool.allocateRegToVarNotLoad(target.getName(), varAddressOffset,
//                        mipsVisitor);
//
//                mipsVisitor.addMipsCode(MipsCode.generateMOVE(targetReg, tempReg));
//            } else {
//                String targetReg =
//                    registerPool.allocateRegToVarNotLoad(target.getName(), varAddressOffset,
//                        mipsVisitor);
//                MipsCode moveCode =
//                    MipsCode.generateMOVE(targetReg,
//                        registerPool.allocateRegToVarLoad(source1.getName(), varAddressOffset,
//                            mipsVisitor));
//                mipsVisitor.addMipsCode(moveCode);
//            }
//        }
    }

    @Override
    public void output() {
        System.out.println(target + " = " + source1);
    }
}
