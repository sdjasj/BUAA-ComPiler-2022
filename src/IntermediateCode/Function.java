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
import Tool.Optimizer;

public class Function {
    private ArrayList<IntermediateCode> intermediateCodes;
    private boolean isMain;
    private String name;
    private FlowGraph flowGraph;
    private VarAddressOffset varAddressOffset;
    private RegisterPool registerPool;
    private ConflictGraph conflictGraph;
    private boolean callOtherFunc = false;
    public static ArrayList<String> regs = new ArrayList<String>() {
        {
            //12
            add("$s0");
            add("$s1");
            add("$s2");
            add("$s3");
            add("$s4");
            add("$s5");
            add("$s6");
            add("$s7");
            add("$fp");
            add("$k0");
            add("$k1");
            add("$v1");
            //15
            add("$t0");
            add("$t1");
            add("$t2");
            add("$t3");
            add("$t4");
            add("$t5");
            add("$t6");
            add("$t7");
            add("$t8");
            add("$t9");
            add("$a0");
            add("$a1");
            add("$a2");
            add("$a3");
        }
    };

    public Function(String name) {
        this.name = name;
        this.intermediateCodes = new ArrayList<>();
        this.flowGraph = new FlowGraph();
    }

    public boolean isCallOtherFunc() {
        return callOtherFunc;
    }

    public void setCallOtherFunc(boolean callOtherFunc) {
        this.callOtherFunc = callOtherFunc;
    }

    public void addIntermediateCode(IntermediateCode intermediateCode) {
        intermediateCodes.add(intermediateCode);
    }

    public HashSet<String> getUsedGlobalRegs() {
        return conflictGraph.getUsedGlobalRegs();
    }

    public void setIntermediateCodes(
        ArrayList<IntermediateCode> intermediateCodes) {
        this.intermediateCodes = intermediateCodes;
    }

    public HashSet<Operand> getUsedGlobalVar() {
        return conflictGraph.getLocalVarHasReg();
    }

    public FlowGraph getFlowGraph() {
        return flowGraph;
    }

    public ArrayList<IntermediateCode> getIntermediateCodes() {
        return intermediateCodes;
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

    public void resize() {
        int blockNums = flowGraph.getBasicBlocks().size();
        RegisterPool.tempRegs.clear();
        RegisterAllocator.globalRegs.clear();
        if (blockNums < 1 << 2) {
            RegisterAllocator.setGlobalRegs(new ArrayList<>(regs.subList(0, 9)));
            RegisterPool.setTempRegs(new ArrayList<>(regs.subList(9, regs.size())));
        } else {
            RegisterAllocator.setGlobalRegs(new ArrayList<>(regs.subList(0, 18)));
            RegisterPool.setTempRegs(new ArrayList<>(regs.subList(18, regs.size())));
        }
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
                String name = intermediateCode.getTarget().getName();
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
        flowGraph.buildBasicBlocks(intermediateCodes, this);
        ArrayList<BasicBlock> basicBlocks = flowGraph.getBasicBlocks();
        for (BasicBlock basicBlock : basicBlocks) {
            connecteBlocks(basicBlock, new HashSet<>());
        }
    }

    public boolean jumpOptimizer() {
        return JumpOptimizer.ConstJumpOptimizer(intermediateCodes);
    }

    public HashSet<BasicBlock> connecteBlocks(BasicBlock beginBlock, HashSet<BasicBlock> visited) {
        HashSet<BasicBlock> basicBlocks = beginBlock.getSuccessor();
        HashSet<BasicBlock> ans = new HashSet<>();
        for (BasicBlock basicBlock : basicBlocks) {
            if (!visited.contains(basicBlock)) {
                visited.add(basicBlock);
                HashSet<BasicBlock> temp = connecteBlocks(basicBlock, visited);
                beginBlock.addAdjBlocks(temp);
                ans.addAll(temp);
            }
        }
        ans.add(beginBlock);
        return ans;
    }

    public void basicBlockOptimize() {

//        System.err.println(name);
        while (true) {
            boolean flag = BlockOptimizer.peepholes(flowGraph);
            BlockOptimizer.reachDefineAnalyze(flowGraph);
            flag |= BlockOptimizer.ReplicaPropagation(flowGraph);
            flag |= BlockOptimizer.peepHolesForReWrite(flowGraph);
            BlockOptimizer.activeVarAnalyze(flowGraph);
            flag |= BlockOptimizer.deleteDeadCode(flowGraph);

            if (!flag) {
                break;
            }
//            System.err.println(flag1 + "1");
//            System.err.println(flag2 + "2");
        }
    }

    public void colorAllocate() {
        conflictGraph = new ConflictGraph();
        BlockOptimizer.buildConflictGraph(conflictGraph, flowGraph);
//        conflictGraph.testPrint();
        RegisterAllocator registerAllocator = new RegisterAllocator(conflictGraph);
        boolean flag = false;
//        while (RegisterPool.tempRegs.size() >= 4) {
//            flag = registerAllocator.allocGlobalReg(false);
//            if (flag) {
//                break;
//            }
//            RegisterAllocator.globalRegs.add(
//                RegisterPool.tempRegs.get(RegisterPool.tempRegs.size() - 1));
//            RegisterPool.tempRegs.remove(RegisterPool.tempRegs.size() - 1);
//        }
//        if (!flag) {
//            registerAllocator.allocGlobalReg(true);
//        }
        registerAllocator.allocGlobalReg(true);
    }


    public void toMips(MipsVisitor mipsVisitor) {
        varAddressOffset = new VarAddressOffset(0);
        registerPool = new RegisterPool();
        ArrayList<BasicBlock> basicBlocks = flowGraph.getBasicBlocks();
        MipsCode.setRegisterPool(registerPool);
        int cnt = 0;
        for (BasicBlock basicBlock : basicBlocks) {
            mipsVisitor.addMipsCode(MipsCode.generateComment("BasicBlock: " + cnt));
            cnt++;
            if (basicBlock.isBegin()) {
                mipsVisitor.addMipsCode(MipsCode.generateTag(getName()));
                //确定变量的空间
                //函数参数
                getParamOffset(varAddressOffset);
                //ra
                if (Optimizer.RaOptimizer && isCallOtherFunc()) {
                    varAddressOffset.addReg("$ra");
                }
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

                if (Optimizer.RaOptimizer && isCallOtherFunc()) {
                    MipsCode raStored =
                        MipsCode.generateSW("$ra", String.valueOf(varAddressOffset.getRegOffset("$ra")),
                            "$sp");
                    mipsVisitor.addMipsCode(raStored);
                }

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
            ArrayList<IntermediateCode> intermediateCodes = basicBlock.getIntermediateCodes();
            for (int i = 0; i < intermediateCodes.size(); i++) {
                IntermediateCode intermediateCode = intermediateCodes.get(i);
                intermediateCode.setConflictGraph(conflictGraph);
                if (intermediateCode instanceof BranchCode ||
                    intermediateCode instanceof JumpCode) {
                    storeRegs(mipsVisitor, basicBlock, intermediateCode);
                    intermediateCode.toMips(mipsVisitor, varAddressOffset, registerPool);
                } else if (i == intermediateCodes.size() - 1) {
                    intermediateCode.toMips(mipsVisitor, varAddressOffset, registerPool);
                    storeRegs(mipsVisitor, basicBlock, intermediateCode);
                } else {
                    intermediateCode.toMips(mipsVisitor, varAddressOffset, registerPool);
                }
            }

            registerPool.clearAllTempRegs();
        }
    }

    public void storeRegs(MipsVisitor mipsVisitor, BasicBlock basicBlock,
                          IntermediateCode intermediateCode) {

//        ArrayList<String> regs = registerPool.getUsedTempRegs();
        HashSet<Operand> activeVar = basicBlock.getActiveOutSet();
        ArrayList<Operand> varOfRegs = registerPool.getVarInReg();
        HashMap<Operand, String> varToReg = registerPool.getVarToTempRegMap();
        //将还要用到的全局寄存器写回内存
        HashSet<String> localRegs = new HashSet<>();
        for (Operand varOfReg : varOfRegs) {
            if (activeVar.contains(varOfReg) && registerPool.isDirtyTempReg(varToReg.get(varOfReg))) {
//                System.err.println(varOfReg);
                MipsCode storeUsedGlobalRegs = MipsCode.generateSW(varToReg.get(varOfReg),
                    String.valueOf(
                        varAddressOffset.getVarOffset(varOfReg)), "$sp");
                mipsVisitor.addMipsCode(storeUsedGlobalRegs);
//                localRegs.add(varToReg.get(varOfReg));
            } else if (varOfReg.isGlobal() && varOfReg.isVar() &&
                registerPool.isDirtyTempReg(varToReg.get(varOfReg))) {
                varOfReg.storeToMemory(mipsVisitor, varAddressOffset, varToReg.get(varOfReg));
            }
        }

//        registerPool.clearWithOutLocal(localRegs);

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
