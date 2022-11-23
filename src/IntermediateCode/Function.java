package IntermediateCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import IntermediateCode.AllCode.AssignCode;
import IntermediateCode.AllCode.BranchCode;
import IntermediateCode.AllCode.CalculateCode;
import IntermediateCode.AllCode.CompareCode;
import IntermediateCode.AllCode.DeclCode;
import IntermediateCode.AllCode.InputCode;
import IntermediateCode.AllCode.JumpCode;
import IntermediateCode.AllCode.LabelCode;
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
    private FlowGraph flowGraph;
    private VarAddressOffset varAddressOffset;
    private RegisterPool registerPool;
    private ConflictGraph conflictGraph;

    public Function(String name) {
        this.name = name;
        this.intermediateCodes = new ArrayList<>();
        this.flowGraph = new FlowGraph();
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
                varAddressOffset.addParam(intermediateCode.getTarget(), 4);
            }
        }
    }

    public void getVarOffset(VarAddressOffset varAddressOffset) {
        for (IntermediateCode intermediateCode : intermediateCodes) {
            if (intermediateCode instanceof DeclCode) {
                //局部变量
                int size = ((DeclCode) intermediateCode).getVarSize();
                varAddressOffset.addVar(intermediateCode.getTarget(), size);
            }
        }

        HashSet<String> tempVarSet = new HashSet<>();
        for (IntermediateCode intermediateCode : intermediateCodes) {
            if (intermediateCode instanceof CalculateCode ||
                intermediateCode instanceof InputCode ||
                intermediateCode instanceof SingleCalculateCode ||
                intermediateCode instanceof AssignCode || intermediateCode instanceof MemoryCode ||
                intermediateCode instanceof CompareCode) {

                //临时变量
                String name = intermediateCode.getTarget().getName();
                if (name.startsWith("t@") && !tempVarSet.contains(name)) {
                    tempVarSet.add(name);
                    varAddressOffset.addVar(intermediateCode.getTarget(), 4);
                }
            }
        }
    }

    public void buildBasicBlocks() {
        flowGraph.buildBasicBlocks(intermediateCodes);
    }

    public void basicBlockOptimize() {
        BlockOptimizer.activeVarAnalyze(flowGraph);
        BlockOptimizer.deleteDeadCode(flowGraph);
    }

    public void colorAllocate() {
        conflictGraph = new ConflictGraph();
        BlockOptimizer.buildConflictGraph(conflictGraph, flowGraph);
        RegisterAllocator registerAllocator = new RegisterAllocator(conflictGraph);
        registerAllocator.allocGlobalReg();
    }


    public void toMips(MipsVisitor mipsVisitor) {
        varAddressOffset = new VarAddressOffset(0);
        registerPool = new RegisterPool();
        ArrayList<BasicBlock> basicBlocks = flowGraph.getBasicBlocks();
        for (BasicBlock basicBlock : basicBlocks) {
            mipsVisitor.addMipsCode(MipsCode.generateComment("BasicBlock"));
            if (basicBlock.isBegin()) {
                mipsVisitor.addMipsCode(MipsCode.generateTag(getName()));
                //确定变量的空间
                //函数参数
                getParamOffset(varAddressOffset);
                //ra
                varAddressOffset.addReg("$ra");
                //全局寄存器
                varAddressOffset.addGlobalRegsAddress(conflictGraph);
//                varAddressOffset.addAllRegs(registerPool);
                //局部变量 && 临时变量
                getVarOffset(varAddressOffset);
//                System.err.println(varAddressOffset.getVarOffset("t@1"));


                //sp偏移
                int offset = varAddressOffset.getCurOffset();
                MipsCode mipsCode = MipsCode.generateADDIU("$sp", "$sp", String.valueOf(-offset));
                mipsVisitor.addMipsCode(mipsCode);

                MipsCode raStored =
                    MipsCode.generateSW("$ra", String.valueOf(varAddressOffset.getRegOffset("$ra")),
                        "$sp");
                mipsVisitor.addMipsCode(raStored);

                //现在全局寄存器全存
                if (!isMain) {
                    ArrayList<String> usedGlobalRegs = new ArrayList<>(
                        conflictGraph.getUsedGlobalRegs());
                    for (String usedGlobalReg : usedGlobalRegs) {
                        MipsCode storeUsedGlobalRegs = MipsCode.generateSW(usedGlobalReg,
                            String.valueOf(varAddressOffset.getRegOffset(usedGlobalReg)), "$sp");
                        mipsVisitor.addMipsCode(storeUsedGlobalRegs);
                    }
                }

                for (IntermediateCode intermediateCode : intermediateCodes) {
                    if (intermediateCode instanceof FunctionParam) {
                        //函数参数
                        Operand var = intermediateCode.target;
                        if (var.isAllocatedReg()) {
                            mipsVisitor.addMipsCode(
                                MipsCode.generateLW(var.getReg(), String.valueOf(varAddressOffset.getVarOffset(var)),
                                    "$sp"));
                        }
                    }
                }


            }
            registerPool.clearAllTempRegs();
            ArrayList<IntermediateCode> intermediateCodes = basicBlock.getIntermediateCodes();
            for (int i = 0; i < intermediateCodes.size(); i++) {
                IntermediateCode intermediateCode = intermediateCodes.get(i);
                intermediateCode.setConflictGraph(conflictGraph);
                if (intermediateCode instanceof BranchCode ||
                    intermediateCode instanceof JumpCode) {
                    storeRegs(mipsVisitor, basicBlock);
                    intermediateCode.toMips(mipsVisitor, varAddressOffset, registerPool);
                } else if (i == intermediateCodes.size() - 1) {
                    intermediateCode.toMips(mipsVisitor, varAddressOffset, registerPool);
                } else {
                    intermediateCode.toMips(mipsVisitor, varAddressOffset, registerPool);
                }
            }

            registerPool.clearAllTempRegs();
        }
    }

    public void storeRegs(MipsVisitor mipsVisitor, BasicBlock basicBlock) {
//        ArrayList<String> regs = registerPool.getUsedTempRegs();
        HashSet<Operand> activeVar = basicBlock.getActiveOutSet();
        ArrayList<Operand> varOfRegs = registerPool.getVarInReg();
        HashMap<Operand, String> varToReg = registerPool.getVarToTempRegMap();
        //将还要用到的全局寄存器写回内存
        for (Operand varOfReg : varOfRegs) {
            if (activeVar.contains(varOfReg)) {
                MipsCode storeUsedGlobalRegs = MipsCode.generateSW(varToReg.get(varOfReg),
                    String.valueOf(
                        varAddressOffset.getVarOffset(varOfReg)), "$sp");
                mipsVisitor.addMipsCode(storeUsedGlobalRegs);
            } else if (varOfReg.isGlobal()) {
                mipsVisitor.addMipsCode(
                    MipsCode.generateSW(varToReg.get(varOfReg), varOfReg.getName(), "$0"));
            }
        }

//        for (String reg : regs) {
//            MipsCode storeUsedGlobalRegs = MipsCode.generateSW(reg,
//                String.valueOf(
//                    varAddressOffset.getVarOffset(
//                        registerPool.getVarNameOfTempReg(reg))), "$sp");
//            mipsVisitor.addMipsCode(storeUsedGlobalRegs);
//        }

//        for (String reg : regs) {
////            System.err.println(registerPool.getVarNameOfGlobalReg(reg));
////            System.err.println(reg);
//            MipsCode storeUsedGlobalRegs = MipsCode.generateSW(reg,
//                String.valueOf(
//                    varAddressOffset.getVarOffset(
//                        registerPool.getVarNameOfGlobalReg(reg))), "$sp");
//            mipsVisitor.addMipsCode(storeUsedGlobalRegs);
//        }
    }

    public void output() {
//        for (IntermediateCode intermediateCode : intermediateCodes) {
//            intermediateCode.output();
//        }
        ArrayList<BasicBlock> basicBlocks = flowGraph.getBasicBlocks();
        int cnt = 0;
        for (BasicBlock basicBlock : basicBlocks) {
            System.out.println("######### BasicBlock" + cnt++);
            basicBlock.output();
        }
    }

    public void testPrint() {
        flowGraph.testPrint();
    }
}
