package IntermediateCode.AllCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import IntermediateCode.Operand;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;

public class CalculateCode extends IntermediateCode {

    public CalculateCode(Operand target, Operand source1, Operand source2,
                         Operator op) {
        super(target, source1, source2, op);
    }


    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {
        //comment
        mipsVisitor.addMipsCode(MipsCode.generateComment("calculate " + target));


        String source1Reg = getSrcReg(source1, varAddressOffset, mipsVisitor, registerPool);
        String source2Reg = getSrcReg(source2, varAddressOffset, mipsVisitor, registerPool);
//        if (source1.isNUMBER()) {
//            source1Reg =
//                registerPool.getTempReg(true, varAddressOffset, mipsVisitor);
//            MipsCode mipsCode = MipsCode.generateLi(source1Reg, source1.getName());
//            mipsVisitor.addMipsCode(mipsCode);
//        } else if (source1.isAddress()) {
//            if (mipsVisitor.varIsGlobal(source1.getName())) {
//                source1Reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
//                mipsVisitor.addMipsCode(MipsCode.generateLA(source1.getName(), source1Reg));
//            } else {
//                source1Reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
//                mipsVisitor.addMipsCode(MipsCode.generateADDIU(source1Reg, "$sp",
//                    String.valueOf(varAddressOffset.getArrayOffset(source1.getName(), 0))));
//            }
//        } else {
//            if (mipsVisitor.varIsGlobal(source1.getName())) {
//                source1Reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
//                mipsVisitor.addMipsCode(MipsCode.generateLW(source1Reg, source1.getName(), "$0"));
//            } else {
//                source1Reg =
//                    registerPool.allocateRegToVarLoad(source1.getName(), varAddressOffset, mipsVisitor);
//            }
//        }

//        if (source2.isNUMBER()) {
//            source2Reg = registerPool.getTempReg(true, varAddressOffset, mipsVisitor);
//            MipsCode mipsCode = MipsCode.generateLi(source2Reg, source2.getName());
//            mipsVisitor.addMipsCode(mipsCode);
//        } else if (mipsVisitor.varIsGlobal(source2.getName())) {
//            source2Reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
//            mipsVisitor.addMipsCode(MipsCode.generateLW(source2Reg, source2.getName(), "$0"));
//        } else {
//            source2Reg =
//                registerPool.allocateRegToVarLoad(source2.getName(), varAddressOffset, mipsVisitor);
//        }


        String resReg =
            registerPool.allocateRegToVarNotLoad(target.getName(), varAddressOffset, mipsVisitor);
        if (op == Operator.ADD) {
            MipsCode mipsCode = MipsCode.generateADDU(resReg, source1Reg, source2Reg);
            mipsVisitor.addMipsCode(mipsCode);
        } else if (op == Operator.SUB) {
            MipsCode mipsCode = MipsCode.generateSUBU(resReg, source1Reg, source2Reg);
            mipsVisitor.addMipsCode(mipsCode);
        } else if (op == Operator.MUL) {
            MipsCode mipsCode = MipsCode.generateMULT(source1Reg, source2Reg);
            mipsVisitor.addMipsCode(mipsCode);
            MipsCode mipsCode1 = MipsCode.generateMFLO(resReg);
            mipsVisitor.addMipsCode(mipsCode1);
        } else if (op == Operator.DIV) {
            MipsCode mipsCode = MipsCode.generateDIV(source1Reg, source2Reg);
            mipsVisitor.addMipsCode(mipsCode);
            MipsCode mipsCode1 = MipsCode.generateMFLO(resReg);
            mipsVisitor.addMipsCode(mipsCode1);
        } else if (op == Operator.MOD) {
            MipsCode mipsCode = MipsCode.generateDIV(source1Reg, source2Reg);
            mipsVisitor.addMipsCode(mipsCode);
            MipsCode mipsCode1 = MipsCode.generateMFHI(resReg);
            mipsVisitor.addMipsCode(mipsCode1);
        }


        //左值为全局变量
        if (mipsVisitor.varIsGlobal(target.getName())) {
            MipsCode mipsCode1 = MipsCode.generateSW(resReg, target.getName(), "$0");
            mipsVisitor.addMipsCode(mipsCode1);
        }

    }

    @Override
    public void output() {
        if (op == Operator.ADD) {
            System.out.println(target + " = " + source1 + " + " + source2);
        } else if (op == Operator.SUB) {
            System.out.println(target + " = " + source1 + " - " + source2);
        } else if (op == Operator.MUL) {
            System.out.println(target + " = " + source1 + " * " + source2);
        } else if (op == Operator.DIV) {
            System.out.println(target + " = " + source1 + " / " + source2);
        } else if (op == Operator.MOD) {
            System.out.println(target + " = " + source1 + " % " + source2);
        }
    }
}
