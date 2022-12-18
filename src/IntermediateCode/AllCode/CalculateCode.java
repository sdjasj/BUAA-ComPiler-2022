package IntermediateCode.AllCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import IntermediateCode.Operand;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;
import MipsCode.MulOptimizer;
import Tool.Optimizer;
import MipsCode.DivOptimizer;

import java.util.ArrayList;
import java.util.List;

public class CalculateCode extends IntermediateCode {

    public CalculateCode(Operand target, Operand source1, Operand source2,
                         Operator op) {
        super(target, source1, source2, op);
        if (!source2.isNUMBER() && source1.isNUMBER() && op == Operator.ADD) {
            this.source1 = source2;
            this.source2 = source1;
        }
    }

    public Operand getValue() {
        if (source1.isNUMBER() && source2.isNUMBER()) {
            switch (op) {
                case ADD:
                    return Operand.getNewOperand(String.valueOf(
                            Integer.parseInt(source1.getName()) + Integer.parseInt(source2.getName())),
                        Operand.OperandType.NUMBER);
                case SUB:
                    return Operand.getNewOperand(String.valueOf(
                            Integer.parseInt(source1.getName()) - Integer.parseInt(source2.getName())),
                        Operand.OperandType.NUMBER);
                case MUL:
                    return Operand.getNewOperand(String.valueOf(
                            Integer.parseInt(source1.getName()) * Integer.parseInt(source2.getName())),
                        Operand.OperandType.NUMBER);
                case DIV:
                    return Operand.getNewOperand(String.valueOf(
                            Integer.parseInt(source1.getName()) / Integer.parseInt(source2.getName())),
                        Operand.OperandType.NUMBER);
                case MOD:
                    return Operand.getNewOperand(String.valueOf(
                            Integer.parseInt(source1.getName()) % Integer.parseInt(source2.getName())),
                        Operand.OperandType.NUMBER);
            }

        } else if (source1.isNUMBER()) {
            switch (op) {
                case MUL:
                    if (source1.getName().equals("0")) {
                        return Operand.getNewOperand("0", Operand.OperandType.NUMBER);
                    } else if (source1.getName().equals("1")) {
                        return source2;
                    }
                    return null;
                case DIV:
                case MOD:
                    if (source1.getName().equals("0")) {
                        return Operand.getNewOperand("0", Operand.OperandType.NUMBER);
                    }
                    return null;
                case ADD:
                    if (source1.getName().equals("0")) {
                        return source2;
                    }
                    return null;
                default:
                    return null;
            }
        } else if (source2.isNUMBER()) {
            switch (op) {
                case MUL:
                    if (source2.getName().equals("0")) {
                        return Operand.getNewOperand("0", Operand.OperandType.NUMBER);
                    } else if (source2.getName().equals("1")) {
                        return source1;
                    }
                    return null;
                case ADD:
                case SUB:
                    if (source2.getName().equals("0")) {
                        return source1;
                    }
                    return null;
                case DIV:
                    if (source2.getName().equals("1")) {
                        return source1;
                    }
                default:
                    return null;
            }
        }
        return null;
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {
        //comment
        mipsVisitor.addMipsCode(MipsCode.generateComment("calculate " + target));
        String source1Reg;
        String source2Reg;

        if (source1.isNUMBER()) {
            source1Reg = source1.getName();
        } else {
            source1Reg = getSrcReg(source1, varAddressOffset, mipsVisitor, registerPool);
//            System.err.println(source1Reg);
        }

        if (source2.isNUMBER()) {
            source2Reg = source2.getName();
        } else {
            source2Reg = getSrcReg(source2, varAddressOffset, mipsVisitor, registerPool);
        }

        String resReg;

        if (target.isGlobal()) {
            resReg =
                registerPool.allocateRegToVarNotLoad(target, varAddressOffset, mipsVisitor, this);
        } else {
            resReg =
                registerPool.allocateRegToVarNotLoad(target, varAddressOffset, mipsVisitor, this);
        }

        if (op == Operator.ADD) {
            MipsCode mipsCode = null;
            if (source1.isNUMBER()) {
                mipsCode = MipsCode.generateADDIU(resReg, source2Reg, source1Reg);
            } else if (source2.isNUMBER()) {
                mipsCode = MipsCode.generateADDIU(resReg, source1Reg, source2Reg);
            } else {
                mipsCode = MipsCode.generateADDU(resReg, source1Reg, source2Reg);
            }
            mipsVisitor.addMipsCode(mipsCode);
        } else if (op == Operator.SUB) {
            MipsCode mipsCode = null;
            if (source1.isNUMBER()) {
                mipsVisitor.addMipsCode(MipsCode.generateLi("$1", source1Reg));
                mipsCode = MipsCode.generateSUBU(resReg, "$1", source2Reg);
            } else if (source2.isNUMBER()) {
                mipsCode = MipsCode.generateADDIU(resReg, source1Reg, String.valueOf(-Integer.parseInt(source2Reg)));
            } else {
                mipsCode = MipsCode.generateSUBU(resReg, source1Reg, source2Reg);
            }
            mipsVisitor.addMipsCode(mipsCode);
        } else if (op == Operator.MUL) {
            MipsCode mipsCode = null;
            if (source1.isNUMBER()) {
                if (Optimizer.MulOptimizer && MulOptimizer.canUseShift(Integer.parseInt(source1.getName()))) {
                    MulOptimizer.simplifyMul(resReg, source2Reg,
                        Integer.parseInt(source1.getName()), mipsVisitor, registerPool,
                        varAddressOffset, this);
                } else {
                    mipsVisitor.addMipsCode(MipsCode.generateLi("$1", source1Reg));
                    mipsCode = MipsCode.generateMUL(resReg, "$1", source2Reg);
                    mipsVisitor.addMipsCode(mipsCode);
                }
            } else if (source2.isNUMBER()) {
                if (Optimizer.MulOptimizer && MulOptimizer.canUseShift(Integer.parseInt(source2.getName()))) {
                    MulOptimizer.simplifyMul(resReg, source1Reg,
                        Integer.parseInt(source2.getName()), mipsVisitor, registerPool,
                        varAddressOffset, this);
                } else {
                    mipsVisitor.addMipsCode(MipsCode.generateLi("$1", source2Reg));
                    mipsCode = MipsCode.generateMUL(resReg, source1Reg, "$1");
                    mipsVisitor.addMipsCode(mipsCode);
                }
            } else {
//                if (source1.isGlobal() || source2.isGlobal()) {
//                    mipsVisitor.addMipsCode(MipsCode.generateNOP());
//                }
                mipsCode = MipsCode.generateMUL(resReg, source1Reg, source2Reg);
                mipsVisitor.addMipsCode(mipsCode);
            }
        } else if (op == Operator.DIV) {
            MipsCode mipsCode = null;
            if (source1.isNUMBER()) {
                mipsVisitor.addMipsCode(MipsCode.generateLi("$1", source1Reg));
                mipsCode = MipsCode.generateDIV("$1", source2Reg);
                mipsVisitor.addMipsCode(mipsCode);
                MipsCode mipsCode1 = MipsCode.generateMFLO(resReg);
                mipsVisitor.addMipsCode(mipsCode1);
            } else if (source2.isNUMBER() && !source2.getName().equals("0")) {
                if (Optimizer.DivModOptimizer) {
                    DivOptimizer.simplifyDiv(resReg, source1Reg, Integer.parseInt(source2Reg),
                        mipsVisitor, registerPool, varAddressOffset, this);
                } else {
                    mipsVisitor.addMipsCode(MipsCode.generateLi("$1", source2Reg));
                    mipsCode = MipsCode.generateDIV(source1Reg, "$1");
                    mipsVisitor.addMipsCode(mipsCode);
                    MipsCode mipsCode1 = MipsCode.generateMFLO(resReg);
                    mipsVisitor.addMipsCode(mipsCode1);
                }
            } else {
                mipsCode = MipsCode.generateDIV(source1Reg, source2Reg);
                mipsVisitor.addMipsCode(mipsCode);
                MipsCode mipsCode1 = MipsCode.generateMFLO(resReg);
                mipsVisitor.addMipsCode(mipsCode1);
            }
        } else if (op == Operator.MOD) {
            MipsCode mipsCode = null;
            if (source1.isNUMBER()) {
                mipsVisitor.addMipsCode(MipsCode.generateLi("$1", source1Reg));
                mipsCode = MipsCode.generateDIV("$1", source2Reg);
                mipsVisitor.addMipsCode(mipsCode);
                MipsCode mipsCode1 = MipsCode.generateMFHI(resReg);
                mipsVisitor.addMipsCode(mipsCode1);
            } else if (source2.isNUMBER()) {
                if (Optimizer.DivModOptimizer) {
                    DivOptimizer.simplifyMod(resReg, source1Reg, Integer.parseInt(source2Reg),
                        mipsVisitor, registerPool, varAddressOffset, this);
                } else {
                    mipsVisitor.addMipsCode(MipsCode.generateLi("$1", source2Reg));
                    mipsCode = MipsCode.generateDIV(source1Reg, "$1");
                    mipsVisitor.addMipsCode(mipsCode);
                    MipsCode mipsCode1 = MipsCode.generateMFHI(resReg);
                    mipsVisitor.addMipsCode(mipsCode1);
                }
            } else {
                mipsCode = MipsCode.generateDIV(source1Reg, source2Reg);
                mipsVisitor.addMipsCode(mipsCode);
                MipsCode mipsCode1 = MipsCode.generateMFHI(resReg);
                mipsVisitor.addMipsCode(mipsCode1);
            }
        }


        //左值为全局变量
//        if (target.isGlobal()) {
//            MipsCode mipsCode1 =
//                MipsCode.generateSW(resReg, String.valueOf(mipsVisitor.getOffsetByVar(
//                    target.getName(), 0)), "$gp");
//            mipsVisitor.addMipsCode(mipsCode1);
//        }
        registerPool.unFreeze(source1Reg);
        registerPool.unFreeze(source2Reg);
        registerPool.unFreeze(resReg);
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
