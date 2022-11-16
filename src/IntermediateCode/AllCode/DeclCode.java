package IntermediateCode.AllCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operand;
import IntermediateCode.Operator;
import Tool.Pair;

import java.util.ArrayList;
import java.util.List;

public class DeclCode extends IntermediateCode {
    private ArrayList<Integer> dimensions;

    public DeclCode(Operand target, ArrayList<Integer> dimensions) {
        super(target, null, null, Operator.DECL);
        this.dimensions = dimensions;
    }

    @Override
    public Pair<Operand, Operand> getRightVal() {
        return null;
    }

    public int getVarSize() {
        if (dimensions.size() == 0) {
            return 4;
        }
        int size = 1;
        for (Integer dimension : dimensions) {
            size *= dimension;
        }
        return size * 4;
    }

    @Override
    public void output() {
        if (dimensions.size() == 0) {
            System.out.println(Operator.DECL + " " + target);
        } else {
            System.out.println(Operator.DECL + " " + target + " " + dimensions);
        }
    }
}
