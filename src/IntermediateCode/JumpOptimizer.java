package IntermediateCode;

import IntermediateCode.AllCode.BranchCode;
import IntermediateCode.AllCode.JumpCode;
import IntermediateCode.AllCode.LabelCode;

import java.util.ArrayList;

public class JumpOptimizer {
    public static boolean ConstJumpOptimizer(ArrayList<IntermediateCode> intermediateCodes) {
        boolean flag = false;
        for (int i = 0; i < intermediateCodes.size(); i++) {
            if (intermediateCodes.get(i) instanceof BranchCode) {
                IntermediateCode branchCode = intermediateCodes.get(i);
                Operator op = branchCode.op;
                switch (branchCode.op) {
                    case BEQ:
                    case BNE:
                    case BGE:
                    case BGT:
                    case BLE:
                    case BLT:
                        if (branchCode.source1.isNUMBER() && branchCode.source2.isNUMBER()) {
                            int src1 = Integer.parseInt(branchCode.source1.getName());
                            int src2 = Integer.parseInt(branchCode.source2.getName());
                            if ((op == Operator.BEQ && src1 == src2) ||
                                (op == Operator.BNE && src1 != src2) ||
                                (op == Operator.BGE && src1 >= src2) ||
                                (op == Operator.BGT && src1 > src2) ||
                                (op == Operator.BLE && src1 <= src2) ||
                                (op == Operator.BLT && src1 < src2)) {
                                String target = branchCode.target.getName();
                                JumpCode jumpCode = new JumpCode(
                                    Operand.getNewOperand(target, Operand.OperandType.ADDRESS),
                                    Operator.JUMP);
                                intermediateCodes.add(i, jumpCode);
                                intermediateCodes.remove(i + 1);
                            } else {
                                intermediateCodes.remove(i);
                                i--;
                            }
                            flag = true;
                        }
                        break;
                    case BEQZ:
                    case BNEZ:
                        if (branchCode.source1.isNUMBER()) {
                            int src1 = Integer.parseInt(branchCode.source1.getName());
                            if ((op == Operator.BEQZ && src1 == 0) ||
                                (op == Operator.BNEZ && src1 != 0)) {
                                String target = branchCode.target.getName();
                                JumpCode jumpCode = new JumpCode(
                                    Operand.getNewOperand(target, Operand.OperandType.ADDRESS),
                                    Operator.JUMP);
                                intermediateCodes.add(i, jumpCode);
                                intermediateCodes.remove(i + 1);
                            } else {
                                intermediateCodes.remove(i);
                                i--;
                            }
                            flag = true;
                        }
                        break;
                }
            }
        }
        for (int i = 0; i < intermediateCodes.size(); i++) {
            if (intermediateCodes.get(i) instanceof JumpCode) {
                IntermediateCode jumpCode = intermediateCodes.get(i);
                int j = i + 1;
                while (j < intermediateCodes.size() &&
                    intermediateCodes.get(j) instanceof LabelCode) {
                    if (((LabelCode) intermediateCodes.get(j)).getLabel()
                        .equals(jumpCode.target.getName())) {
                        intermediateCodes.remove(i);
                        i--;
                        flag = true;
                        break;
                    }
                    j++;
                }
            }
        }

        for (int i = 0; i < intermediateCodes.size(); i++) {
            if (intermediateCodes.get(i) instanceof JumpCode) {
                IntermediateCode jumpCode = intermediateCodes.get(i);
                int j = 0;
                while (j < intermediateCodes.size() &&
                    intermediateCodes.get(j) instanceof LabelCode) {
                    if (j + 1 < intermediateCodes.size() &&
                        intermediateCodes.get(j + 1) instanceof JumpCode && i != j + 1) {
                        jumpCode.setTarget(intermediateCodes.get(j + 1).target);
                        intermediateCodes.remove(j + 1);
                        if (i >= j + 1) {
                            i--;
                        }
                        flag = true;
                        break;
                    }
                    j++;
                }
            }
        }

        for (int i = 0; i < intermediateCodes.size(); i++) {
            if (intermediateCodes.get(i) instanceof JumpCode ||
                intermediateCodes.get(i) instanceof BranchCode) {
                IntermediateCode jumpCode = intermediateCodes.get(i);
                int j = i + 1;
                while (j < intermediateCodes.size() &&
                    intermediateCodes.get(j) instanceof LabelCode) {
                    LabelCode labelCode = (LabelCode) intermediateCodes.get(j);
                    if (labelCode.getLabel().equals(jumpCode.target.getName())) {
                        intermediateCodes.remove(i);
                        i--;
                        flag = true;
                        break;
                    }
                    j++;
                }
            }
        }
        return flag;
    }
}
