package IntermediateCode.FunctionCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import IntermediateCode.Operand;

public class FunctionCode extends IntermediateCode {

    public FunctionCode(Operand name) {
        super(name, null, null, Operator.FUNC_BEGIN);
    }

    @Override
    public void output() {
        System.out.println();
        System.out.println();
        System.out.println(Operator.FUNC_BEGIN + ": " + target);
    }
}
