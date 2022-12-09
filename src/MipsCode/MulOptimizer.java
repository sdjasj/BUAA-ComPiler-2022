package MipsCode;

import IntermediateCode.IntermediateCode;
import MipsCode.MipsCode.MipsCode;
import Tool.Shift;

import java.util.ArrayList;
import java.util.HashMap;

public class MulOptimizer {


    //先位移再加法 x*9
    //先位移再减法 x*7
    //两次位移 x*24 $t1 <- sll $t1, $t1, 3    $t2 <- sll $t2, $t2, 4  addu res, $t1, $t2
    public static HashMap<Integer, ArrayList<Shift>> numberOfShift = new HashMap<>();
    public static HashMap<Integer, Integer> numberOfCost = new HashMap<>();

    public static void addShift(Shift... shifts) {
        ArrayList<Shift> shiftArrayList = new ArrayList<>();
        int ans = 0;
        for (Shift shift : shifts) {
            if (shift.neg) {
                ans -= 1 << shift.bitCount;
            } else {
                ans += 1 << shift.bitCount;
            }
            shiftArrayList.add(shift);
        }
        int cost = 0;
        if (shifts[0].neg) {
            if (shifts[0].bitCount == 0) {
                cost++;
            } else {
                cost += 2;
            }
        } else {
            cost++;
        }
        for (int i = 1; i < shifts.length; i++) {
            if (shifts[i].bitCount == 0) {
                cost++;
            } else {
                cost += 2;
            }
        }

        if (!numberOfShift.containsKey(ans) ||
            numberOfCost.get(ans) > cost) {
            numberOfShift.put(ans, shiftArrayList);
            numberOfCost.put(ans, cost);
        }
    }

    public static void init() {
        for (int i = 0; i < 32; i++) {
            Shift shiftPosI = new Shift(false, i);
            Shift shiftNegI = new Shift(true, i);
            addShift(shiftPosI);
            addShift(shiftNegI);
            for (int j = 0; j < 32; j++) {
                Shift shiftPosJ = new Shift(false, j);
                Shift shiftNegJ = new Shift(true, j);
                addShift(shiftPosI, shiftPosJ);
                addShift(shiftPosI, shiftNegJ);
                addShift(shiftNegI, shiftPosJ);
                addShift(shiftNegI, shiftNegJ);
                for (int k = 0; k < 32; k++) {
                    Shift shiftPosK = new Shift(false, k);
                    Shift shiftNegK = new Shift(true, k);
                    addShift(shiftPosI, shiftPosJ, shiftPosK);
                    addShift(shiftPosI, shiftNegJ, shiftPosK);
                    addShift(shiftNegI, shiftPosJ, shiftPosK);
                    addShift(shiftNegI, shiftNegJ, shiftPosK);
                    addShift(shiftPosI, shiftPosJ, shiftNegK);
                    addShift(shiftPosI, shiftNegJ, shiftNegK);
                    addShift(shiftNegI, shiftPosJ, shiftNegK);
                    addShift(shiftNegI, shiftNegJ, shiftNegK);
                }
            }
        }
    }

    public static boolean canUseShift(int val) {
        if (!numberOfCost.containsKey(val)) {
            return false;
        }
        if (val <= 0xffff && val >= -32768) {
            return numberOfCost.get(val) < 5;
        } else {
            return numberOfCost.get(val) < 6;
        }
    }

    public static void simplifyMul(String target, String src, int val, MipsVisitor mipsVisitor,
                                   RegisterPool registerPool, VarAddressOffset varAddressOffset,
                                   IntermediateCode intermediateCode) {
        ArrayList<Shift> shifts = numberOfShift.get(val);
        if (shifts.size() == 1) {
            if (shifts.get(0).neg) {
                if (shifts.get(0).bitCount == 0) {
                    mipsVisitor.addMipsCode(MipsCode.generateSUBU(target, "$0", src));
                } else {
                    mipsVisitor.addMipsCode(
                        MipsCode.generateSLL("$1", src, String.valueOf(shifts.get(0).bitCount)));
                    mipsVisitor.addMipsCode(MipsCode.generateSUBU(target, "$0", "$1"));
                }
            } else {
                if (shifts.get(0).bitCount == 0) {
                    mipsVisitor.addMipsCode(MipsCode.generateADDU(target, "$0", src));
                } else {
                    mipsVisitor.addMipsCode(
                        MipsCode.generateSLL(target, src, String.valueOf(shifts.get(0).bitCount)));
                }
            }
            return;
        }


        if (target.equals(src)) {
            String tempReg =
                registerPool.getTempReg(false, varAddressOffset, mipsVisitor, intermediateCode);
            if (shifts.get(0).neg) {
                if (shifts.get(0).bitCount == 0) {
                    mipsVisitor.addMipsCode(MipsCode.generateSUBU(tempReg, "$0", src));
                } else {
                    mipsVisitor.addMipsCode(
                        MipsCode.generateSLL("$1", src, String.valueOf(shifts.get(0).bitCount)));
                    mipsVisitor.addMipsCode(MipsCode.generateSUBU(tempReg, "$0", "$1"));
                }
            } else {
                if (shifts.get(0).bitCount == 0) {
                    mipsVisitor.addMipsCode(MipsCode.generateADDU(tempReg, "$0", src));
                } else {
                    mipsVisitor.addMipsCode(
                        MipsCode.generateSLL(tempReg, src, String.valueOf(shifts.get(0).bitCount)));
                }
            }

            for (int i = 1; i < shifts.size() - 1; i++) {
                if (shifts.get(i).neg) {
                    if (shifts.get(i).bitCount == 0) {
                        mipsVisitor.addMipsCode(MipsCode.generateSUBU(tempReg, tempReg, src));
                    } else {
                        mipsVisitor.addMipsCode(
                            MipsCode.generateSLL("$1", src, String.valueOf(shifts.get(i).bitCount)));
                        mipsVisitor.addMipsCode(MipsCode.generateSUBU(tempReg, tempReg, "$1"));
                    }
                } else {
                    if (shifts.get(i).bitCount == 0) {
                        mipsVisitor.addMipsCode(MipsCode.generateADDU(tempReg, tempReg, src));
                    } else {
                        mipsVisitor.addMipsCode(
                            MipsCode.generateSLL("$1", src, String.valueOf(shifts.get(i).bitCount)));
                        mipsVisitor.addMipsCode(MipsCode.generateADDU(tempReg, tempReg, "$1"));
                    }
                }
            }

            int last = shifts.size() - 1;

            if (shifts.get(last).neg) {
                if (shifts.get(last).bitCount == 0) {
                    mipsVisitor.addMipsCode(MipsCode.generateSUBU(target, tempReg, src));
                } else {
                    mipsVisitor.addMipsCode(
                        MipsCode.generateSLL("$1", src, String.valueOf(shifts.get(last).bitCount)));
                    mipsVisitor.addMipsCode(MipsCode.generateSUBU(target, tempReg, "$1"));
                }
            } else {
                if (shifts.get(last).bitCount == 0) {
                    mipsVisitor.addMipsCode(MipsCode.generateADDU(target, tempReg, src));
                } else {
                    mipsVisitor.addMipsCode(
                        MipsCode.generateSLL("$1", src, String.valueOf(shifts.get(last).bitCount)));
                    mipsVisitor.addMipsCode(MipsCode.generateADDU(target, tempReg, "$1"));
                }
            }
            registerPool.unFreeze(tempReg);
        } else {
            if (shifts.get(0).neg) {
                if (shifts.get(0).bitCount == 0) {
                    mipsVisitor.addMipsCode(MipsCode.generateSUBU(target, "$0", src));
                } else {
                    mipsVisitor.addMipsCode(
                        MipsCode.generateSLL("$1", src, String.valueOf(shifts.get(0).bitCount)));
                    mipsVisitor.addMipsCode(MipsCode.generateSUBU(target, "$0", "$1"));
                }
            } else {
                if (shifts.get(0).bitCount == 0) {
                    mipsVisitor.addMipsCode(MipsCode.generateADDU(target, "$0", src));
                } else {
                    mipsVisitor.addMipsCode(
                        MipsCode.generateSLL(target, src, String.valueOf(shifts.get(0).bitCount)));
                }
            }

            for (int i = 1; i < shifts.size(); i++) {
                if (shifts.get(i).neg) {
                    if (shifts.get(i).bitCount == 0) {
                        mipsVisitor.addMipsCode(MipsCode.generateSUBU(target, target, src));
                    } else {
                        mipsVisitor.addMipsCode(
                            MipsCode.generateSLL("$1", src, String.valueOf(shifts.get(i).bitCount)));
                        mipsVisitor.addMipsCode(MipsCode.generateSUBU(target, target, "$1"));
                    }
                } else {
                    if (shifts.get(i).bitCount == 0) {
                        mipsVisitor.addMipsCode(MipsCode.generateADDU(target, target, src));
                    } else {
                        mipsVisitor.addMipsCode(
                            MipsCode.generateSLL("$1", src, String.valueOf(shifts.get(i).bitCount)));
                        mipsVisitor.addMipsCode(MipsCode.generateADDU(target, target, "$1"));
                    }
                }
            }
        }
    }

}
