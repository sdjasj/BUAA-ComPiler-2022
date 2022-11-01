package IntermediateCode;

import java.util.ArrayList;
import java.util.HashSet;

import IntermediateCode.AllCode.AssignCode;
import IntermediateCode.AllCode.CalculateCode;
import IntermediateCode.AllCode.DeclCode;
import IntermediateCode.AllCode.InputCode;
import IntermediateCode.AllCode.MemoryCode;
import IntermediateCode.AllCode.OutputCode;
import IntermediateCode.AllCode.SingleCalculateCode;
import IntermediateCode.FunctionCode.FunctionParam;
import IntermediateCode.FunctionCode.FunctionReturnCode;
import IntermediateCode.IntermediateCode;
import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;

public class Function {
    private ArrayList<IntermediateCode> intermediateCodes;
    private boolean isMain;
    private String name;

    public Function(String name) {
        this.name = name;
        this.intermediateCodes = new ArrayList<>();
    }

    public void addIntermediateCode(IntermediateCode intermediateCode) {
        intermediateCodes.add(intermediateCode);
    }

    public void setMain(boolean main) {
        isMain = main;
    }

    public boolean isMain() {
        return isMain;
    }

    public String getName() {
        return name;
    }

    public void getParamOffset(VarAddressOffset varAddressOffset) {
        //变量偏移，还需要加上函数参数、保存的全局寄存器的偏移
        for (IntermediateCode intermediateCode : intermediateCodes) {
            if (intermediateCode instanceof FunctionParam) {
                //函数参数
                String name = intermediateCode.getTarget().getName();
                varAddressOffset.addParam(name, 4);
            }
        }
    }

    public void getVarOffset(VarAddressOffset varAddressOffset) {
        for (IntermediateCode intermediateCode : intermediateCodes) {
            if (intermediateCode instanceof DeclCode) {
                //局部变量
                String name = intermediateCode.getTarget().getName();
                int size = ((DeclCode) intermediateCode).getVarSize();
                varAddressOffset.addVar(name, size);
            }
        }

        HashSet<String> tempVarSet = new HashSet<>();
        for (IntermediateCode intermediateCode : intermediateCodes) {
            if (intermediateCode instanceof CalculateCode || intermediateCode instanceof InputCode
                || intermediateCode instanceof OutputCode ||
                intermediateCode instanceof SingleCalculateCode ||
                intermediateCode instanceof AssignCode || intermediateCode instanceof MemoryCode) {

                //临时变量
                String name = intermediateCode.getTarget().getName();
                if (name.startsWith("t@") && !tempVarSet.contains(name)) {
                    tempVarSet.add(name);
                    varAddressOffset.addVar(name, 4);
                }
            }
        }
    }

    public void toMips(VarAddressOffset varAddressOffset, MipsVisitor mipsVisitor,
                       RegisterPool registerPool) {
        for (IntermediateCode intermediateCode : intermediateCodes) {
            intermediateCode.toMips(mipsVisitor, varAddressOffset, registerPool);
        }
    }

    public void output() {
        for (IntermediateCode intermediateCode : intermediateCodes) {
            intermediateCode.output();
        }
    }
}
