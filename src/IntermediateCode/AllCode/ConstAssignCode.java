package IntermediateCode.AllCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import IntermediateCode.Operand;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;
import Tool.Pair;

import java.util.ArrayList;
import java.util.List;

public class ConstAssignCode extends IntermediateCode {
    private ArrayList<Integer> initVal;
    private ArrayList<Integer> dimensions;

    public ConstAssignCode(Operand name, ArrayList<Integer> initVal, ArrayList<Integer> dimensions) {
        super(name, null, null, Operator.ASSIGN);
        this.initVal = initVal;
        this.dimensions = dimensions;
    }

    @Override
    public Operand getLeftVal() {
        return null;
    }

    @Override
    public Pair<Operand, Operand> getRightVal() {
        return null;
    }

    public int getSize() {
        int size = 1;
        for (Integer dimension : dimensions) {
            size *= dimension;
        }
        return size;
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {
        String tempReg =
            registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
        if (dimensions.size() == 0) {
            return;
        } else {
//            System.err.println(initVal);
            for (int i = 0; i < getSize(); i++) {
                int offset = varAddressOffset.getArrayOffset(target, i);
                int val = initVal.get(i);
                MipsCode mipsCode = MipsCode.generateLi(tempReg, String.valueOf(val));
                mipsVisitor.addMipsCode(mipsCode);
                mipsCode = MipsCode.generateSW(tempReg, String.valueOf(offset), "$sp");
                mipsVisitor.addMipsCode(mipsCode);
            }
        }

    }

    @Override
    public void output() {
        if (dimensions.size() == 0) {
            System.out.println(target + " = " + initVal.get(0));
        } else {
            String constDef;
            constDef = "const";
            StringBuilder dimensionShow = new StringBuilder();
            for (Integer dimension : dimensions) {
                dimensionShow.append("[").append(dimension).append("]");
            }

            String intermediateCode =
                constDef + " " + target + " " + dimensionShow + " = " +
                    initVal;
            System.out.println(intermediateCode);
        }
    }
}
