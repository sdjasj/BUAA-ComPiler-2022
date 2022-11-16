package IntermediateCode;

import IntermediateCode.Operand;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;
import Tool.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class IntermediateCode {
    protected Operand target;
    protected Operand source1;
    protected Operand source2;
    protected Operator op;
    protected boolean isBasicBlockBegin;
    public static int cnt = 0;
    protected int id = cnt++;
    protected ConflictGraph conflictGraph;

    public IntermediateCode(Operand target, Operand source1, Operand source2, Operator op) {
        this.target = target;
        this.source1 = source1;
        this.source2 = source2;
        this.op = op;
    }

    public Operand getTarget() {
        return target;
    }

    public Operand getSource1() {
        return source1;
    }

    public Operand getSource2() {
        return source2;
    }

    public Operator getOp() {
        return op;
    }

    public void output() {

    }

    public void setBasicBlockBegin(boolean basicBlockBegin) {
        this.isBasicBlockBegin = basicBlockBegin;
    }

    public boolean isBasicBlockBegin() {
        return isBasicBlockBegin;
    }

    public String addressMul4(String addr) {
        return String.valueOf(Integer.parseInt(addr) * 4);
    }

    public void toMips(MipsVisitor mipsVisitor, VarAddressOffset varAddressOffset,
                       RegisterPool registerPool) {

    }

    public String getSrcReg(Operand src, VarAddressOffset varAddressOffset,
                            MipsVisitor mipsVisitor, RegisterPool registerPool) {
        String reg = null;
        if (src.isNUMBER()) {
            //数字
            reg =
                registerPool.getTempReg(true, varAddressOffset, mipsVisitor);
            MipsCode mipsCode = MipsCode.generateLi(reg, src.getName());
            mipsVisitor.addMipsCode(mipsCode);
        } else if (mipsVisitor.varIsGlobal(src.getName())) {
            //全局变量
            if (src.isVar()) {
                reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
                mipsVisitor.addMipsCode(MipsCode.generateLW(reg, src.getName(), "$0"));
            } else if (src.isAddress()) {
                reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor);
                mipsVisitor.addMipsCode(MipsCode.generateLA(src.getName(), reg));
            } else {
                System.err.println("error in CalculateCode of source1 global");
            }
        } else if (varAddressOffset.isParam(src)) {
            //参数
            if (src.isVar()) {
                reg = registerPool.allocateRegToVarLoad(src, varAddressOffset,
                    mipsVisitor);
            } else if (src.isAddress()) {
                reg =
                    registerPool.allocateRegToVarLoad(src, varAddressOffset, mipsVisitor);
            } else {
                System.err.println("error in CalculateCode of source1 params");
            }
        } else {
            //局部变量
            if (src.isVar()) {
                reg = registerPool.allocateRegToVarLoad(src, varAddressOffset,
                    mipsVisitor);
            } else if (src.isAddress()) {
//                if (src.getName().startsWith("t@")) {
//                    System.err.println(151515151);
//                    reg = registerPool.allocateRegToVarLoad(src, varAddressOffset,
//                        mipsVisitor);
//                } else {
                int arrayOffset = varAddressOffset.getVarOffset(src);
                reg = registerPool.getTempReg(true, varAddressOffset, mipsVisitor);
                mipsVisitor.addMipsCode(
                    MipsCode.generateADDIU(reg, "$sp", String.valueOf(arrayOffset)));
            } else {
                System.err.println("error in CalculateCode of source1 params");
            }
        }
        return reg;
    }

    public Operand getLeftVal() {
        return target;
    }

    public Pair<Operand, Operand> getRightVal() {
        return new Pair<>(source1, source2);
    }

    public HashSet<Operand> getUsedSet() {
        HashSet<Operand> usedSet = new HashSet<>();
        if (source1 != null && source1.isLocal()) {
            usedSet.add(source1);
        }
        if (source2 != null && source2.isLocal()) {
            usedSet.add(source2);
        }
        return usedSet;
    }

    public void setConflictGraph(ConflictGraph conflictGraph) {
        this.conflictGraph = conflictGraph;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return id == ((IntermediateCode) obj).id;
    }
}
