package IntermediateCode.FunctionCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import IntermediateCode.Operand;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;

import java.util.ArrayList;

public class FunctionParam extends IntermediateCode {
    public enum ParamType {
         int_type,
    }

    private ParamType paramType;
    private ArrayList<Integer> dimension;

    //dimension 0 非数组
    //dimension 1 一维数组，长度空缺
    //dimension >= 2, 多维数组, [][a][b][c][d]
    public FunctionParam(Operand name, ArrayList<Integer> dimension) {
        super(name, null, null, Operator.PARAM);
        this.dimension = dimension;
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {

    }

    @Override
    public void output() {
        if (dimension.size() == 0) {
            System.out.println(Operator.PARAM + " " + target);
        } else {
            System.out.println(Operator.PARAM + " " + dimension + " " + target);
        }
    }
}
