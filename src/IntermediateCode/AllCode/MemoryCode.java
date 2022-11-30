package IntermediateCode.AllCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import IntermediateCode.Operand;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;
import Tool.Pair;

import java.util.HashSet;

public class MemoryCode extends IntermediateCode {

    public MemoryCode(Operand target, Operand source1, Operand source2, Operator op) {
        super(target, source1, source2, op);
    }

    @Override
    public Operand getLeftVal() {
        if (op == Operator.LOAD) {
            return target;
        } else {
            return null;
        }
    }

    @Override
    public Pair<Operand, Operand> getRightVal() {
        if (op == Operator.LOAD) {
            return new Pair<Operand, Operand>(source1, source2);
        } else {
            return new Pair<Operand, Operand>(target, source2);
        }
    }

    @Override
    public HashSet<Operand> getUsedSet() {
        HashSet<Operand> usedSet = new HashSet<>();
        if (target != null && target.isLocal()) {
            usedSet.add(target);
        }
        if (source2 != null && source2.isLocal()) {
            usedSet.add(source2);
        }
        return usedSet;
    }



    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {
        if (op == Operator.LOAD) {
            mipsVisitor.addMipsCode(MipsCode.generateComment(
                Operator.LOAD + " " + target + " FROM " + source1 + " OFFSET " + source2));
        } else {
            mipsVisitor.addMipsCode(MipsCode.generateComment(
                Operator.STORE + " " + target + " TO " + source1 + " OFFSET " + source2));
        }


        String targetReg;

        String source2Reg = null;
        if (source2.isNUMBER()) {
            source2Reg = source2.getName();
        } else {
            source2Reg = getSrcReg(source2, varAddressOffset, mipsVisitor, registerPool);
        }
//        if (source2.isNUMBER()) {
//            source2Reg = registerPool.getTempReg(true, varAddressOffset, mipsVisitor);
//            mipsVisitor.addMipsCode(
//                MipsCode.generateLi(source2Reg, addressMul4(source2.getName())));
//        } else if (mipsVisitor.varIsGlobal(source2.getName())) {
//            source2Reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
//            mipsVisitor.addMipsCode(MipsCode.generateLW(source2Reg, source2.getName(), "$0"));
//        } else {
//            source2Reg =
//                registerPool.allocateRegToVarLoad(source2.getName(), varAddressOffset,
//                    mipsVisitor);
//        }


        if (source1.isGlobal()) {
            //操作的内存名称为全局变量
            if (op == Operator.LOAD) {
                targetReg =
                    registerPool.allocateRegToVarNotLoad(target, varAddressOffset, mipsVisitor,
                        this);
            } else {
                targetReg = getSrcReg(target, varAddressOffset, mipsVisitor, registerPool);
            }
            if (op == Operator.LOAD) {
                if (source2.isNUMBER()) {
                    mipsVisitor.addMipsCode(
                        MipsCode.generateLW(targetReg, String.valueOf(
                            mipsVisitor.getOffsetByVar(source1.getName(),
                                Integer.parseInt(source2Reg))), "$gp"));
                } else {
                    mipsVisitor.addMipsCode(MipsCode.generateADDU("$1", "$gp", source2Reg));
                    mipsVisitor.addMipsCode(
                        MipsCode.generateLW(targetReg, String.valueOf(mipsVisitor.getOffsetByVar(source1.getName(), 0)), "$1"));
                }
            } else if (op == Operator.STORE) {
                if (source2.isNUMBER()) {
                    mipsVisitor.addMipsCode(
                        MipsCode.generateSW(targetReg, String.valueOf(
                            mipsVisitor.getOffsetByVar(source1.getName(),
                                Integer.parseInt(source2Reg))), "$gp"));
                } else {
                    mipsVisitor.addMipsCode(MipsCode.generateADDU("$1", "$gp", source2Reg));
                    mipsVisitor.addMipsCode(
                        MipsCode.generateSW(targetReg,
                            String.valueOf(mipsVisitor.getOffsetByVar(source1.getName(), 0)),
                            "$1"));
                }
            }

            if (target.isNUMBER()) {
                registerPool.unFreeze(targetReg);
            }

            if (source2.isNUMBER()) {
                registerPool.unFreeze(source2Reg);
            }
            return;
        } else {
            //局部变量
            String arrayReg =
                registerPool.allocateRegToVarNotLoad(source1, varAddressOffset, mipsVisitor, this);
            String tempReg = null;
            if (varAddressOffset.isParam(source1)) {
                //参数
                if (source2.isNUMBER()) {

                } else {
                    tempReg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor, this);
                    mipsVisitor.addMipsCode(MipsCode.generateADDU(tempReg, source2Reg, arrayReg));
                    registerPool.unFreeze(source2Reg);
                    source2Reg = tempReg;
                }
            } else {
                //普通局部变量
                if (source2.isNUMBER()) {

                } else {
                    tempReg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor, this);
                    mipsVisitor.addMipsCode(MipsCode.generateADDU(tempReg, source2Reg, arrayReg));
                    registerPool.unFreeze(source2Reg);
                    source2Reg = tempReg;
                }
            }



            if (op == Operator.LOAD) {
                targetReg = registerPool.allocateRegToVarNotLoad(target, varAddressOffset, mipsVisitor, this);
            } else {
                targetReg = getSrcReg(target, varAddressOffset, mipsVisitor, registerPool);
            }

            if (op == Operator.LOAD) {
                if (source2.isNUMBER()) {
                    mipsVisitor.addMipsCode(MipsCode.generateLW(targetReg, source2Reg, arrayReg));
                } else {
                    mipsVisitor.addMipsCode(MipsCode.generateLW(targetReg, "0", source2Reg));
                }
            } else {
                if (source2.isNUMBER()) {
                    mipsVisitor.addMipsCode(MipsCode.generateSW(targetReg, source2Reg, arrayReg));
                } else {
                    mipsVisitor.addMipsCode(MipsCode.generateSW(targetReg, "0", source2Reg));
                }

            }
        }

        registerPool.unFreeze(targetReg);

        registerPool.unFreeze(source2Reg);

    }

    @Override
    public void output() {
        if (op == Operator.LOAD) {
            System.out.println(
                Operator.LOAD + " " + target + " FROM " + source1 + " OFFSET " + source2);
        } else if (op == Operator.STORE) {
            System.out.println(
                Operator.STORE + " " + target + " TO " + source1 + " OFFSET " + source2);
        }
    }

    @Override
    public String toString() {
        if (op == Operator.LOAD) {
            return Operator.LOAD + " " + target + " FROM " + source1 + " OFFSET " + source2;
        } else if (op == Operator.STORE) {
            return Operator.STORE + " " + target + " TO " + source1 + " OFFSET " + source2;
        }
        return null;
    }
}
