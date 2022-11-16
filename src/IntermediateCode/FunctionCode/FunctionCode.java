package IntermediateCode.FunctionCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import IntermediateCode.Operand;
import Tool.Pair;

import java.util.List;

public class FunctionCode extends IntermediateCode {

    public FunctionCode(Operand name) {
        super(name, null, null, Operator.FUNC_BEGIN);
    }

    @Override
    public Operand getLeftVal() {
        return null;
    }

    @Override
    public Pair<Operand, Operand> getRightVal() {
        return null;
    }

    @Override
    public void output() {
        System.out.println();
        System.out.println();
        System.out.println(Operator.FUNC_BEGIN + ": " + target);
    }
}
