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
    protected BasicBlock basicBlock;
    protected int id = cnt++;
    protected ConflictGraph conflictGraph;

    public IntermediateCode(Operand target, Operand source1, Operand source2, Operator op) {
        this.target = target;
        this.source1 = source1;
        this.source2 = source2;
        this.op = op;
    }

    public void setBasicBlock(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
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

    public void setOp(Operator op) {
        this.op = op;
    }

    public void setTarget(Operand target) {
        this.target = target;
    }

    public void setSource1(Operand source1) {
        this.source1 = source1;
    }

    public void setSource2(Operand source2) {
        this.source2 = source2;
    }

    public void output() {

    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
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
            if (src.getName().equals("0")) {
                return "$0";
            }
            reg =
                registerPool.getTempReg(true, varAddressOffset, mipsVisitor, this);
            MipsCode mipsCode = MipsCode.generateLi(reg, src.getName());
            mipsVisitor.addMipsCode(mipsCode);
        } else if (src.isGlobal()) {
            //全局变量
            if (src.isVar()) {
                reg = registerPool.allocateRegToVarLoad(src, varAddressOffset, mipsVisitor, this);
            } else if (src.isAddress()) {
//                reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor, this);
//                mipsVisitor.addMipsCode(MipsCode.generateLA(src.getName(), reg));
                reg = registerPool.allocateRegToVarNotLoad(src, varAddressOffset, mipsVisitor, this);
            } else {
                System.err.println("error in CalculateCode of source1 global");
            }
        } else if (varAddressOffset.isParam(src)) {
            //参数
            if (src.isVar()) {
                reg = registerPool.allocateRegToVarLoad(src, varAddressOffset,
                    mipsVisitor, this);
            } else if (src.isAddress()) {
                reg =
                    registerPool.allocateRegToVarLoad(src, varAddressOffset, mipsVisitor, this);
            } else {
                System.err.println("error in CalculateCode of source1 params");
            }
        } else {
            //局部变量
            if (src.isVar()) {
                reg = registerPool.allocateRegToVarLoad(src, varAddressOffset,
                    mipsVisitor, this);
//                if (reg == null) {
//                    System.err.println(src);
//                }
            } else if (src.isAddress()) {
//                if (src.getName().startsWith("t@")) {
//                    System.err.println(151515151);
//                    reg = registerPool.allocateRegToVarLoad(src, varAddressOffset,
//                        mipsVisitor);
//                } else {
//                int arrayOffset = varAddressOffset.getVarOffset(src);
//                reg = registerPool.getTempReg(false, varAddressOffset, mipsVisitor, this);
//                mipsVisitor.addMipsCode(
//                    MipsCode.generateADDIU(reg, "$sp", String.valueOf(arrayOffset)));
                reg =
                    registerPool.allocateRegToVarNotLoad(src, varAddressOffset, mipsVisitor, this);
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
        if (source1 != null && source1.isLocalAndVar()) {
            usedSet.add(source1);
        }
        if (source2 != null && source2.isLocalAndVar()) {
            usedSet.add(source2);
        }
        return usedSet;
    }

//    public HashSet<Operand> getDefinedVal() {
//
//    }

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

    @Override
    public String toString() {
        return target + " " + source1 + " " + op + " " + source2;
    }
}
