package IntermediateCode.FunctionCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operator;
import IntermediateCode.Operand;

public class FunctionEndCode extends IntermediateCode {
    public FunctionEndCode(Operand target) {
        super(target, null, null, Operator.FUNC_END);
    }

    @Override
    public void output() {
        System.out.println(op + " " + target);
        System.out.println();
        System.out.println();
    }
}
