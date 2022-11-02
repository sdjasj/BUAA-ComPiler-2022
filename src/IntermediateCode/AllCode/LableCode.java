package IntermediateCode.AllCode;

import IntermediateCode.IntermediateCode;
import IntermediateCode.Operand;
import IntermediateCode.Operator;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;


public class LableCode extends IntermediateCode {
    private String lable;

    public LableCode(String lable) {
        super(null, null, null, null);
        this.lable = lable;
    }

    @Override
    public void output() {
        System.out.println(lable + ":");
    }

    @Override
    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {
        mipsVisitor.addMipsCode(MipsCode.generateLable(lable));
    }
}
