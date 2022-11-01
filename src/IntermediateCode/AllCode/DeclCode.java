package IntermediateCode.AllCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operand;
import IntermediateCode.Operator;

import java.util.ArrayList;

public class DeclCode extends IntermediateCode {
    private ArrayList<Integer> dimensions;

    public DeclCode(Operand target, ArrayList<Integer> dimensions) {
        super(target, null, null, Operator.DECL);
        this.dimensions = dimensions;
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
