package IntermediateCode;

import IntermediateCode.AllCode.AssignCode;
import IntermediateCode.AllCode.BranchCode;
import IntermediateCode.AllCode.CalculateCode;
import IntermediateCode.AllCode.CompareCode;
import IntermediateCode.AllCode.DeclCode;
import IntermediateCode.AllCode.InputCode;
import IntermediateCode.AllCode.MemoryCode;
import IntermediateCode.AllCode.SingleCalculateCode;
import IntermediateCode.FunctionCode.FunctionParam;
import IntermediateCode.FunctionCode.FunctionReturnCode;
import MipsCode.MipsCode.MipsCode;
import Tool.Optimizer;
import Tool.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BlockOptimizer {
    public static void activeVarAnalyze(FlowGraph flowGraph) {
        ArrayList<BasicBlock> basicBlocks = flowGraph.getBasicBlocks();
        for (BasicBlock basicBlock : basicBlocks) {
            basicBlock.calUsedDefSet();
            basicBlock.clearActiveInOutSet();
        }
        boolean flag = true;
        while (flag) {
            flag = false;
            for (int i = basicBlocks.size() - 1; i >= 0; i--) {
                BasicBlock basicBlock = basicBlocks.get(i);
                basicBlock.calNewActiveOutSet();
//                int oldInSize = basicBlock.getActiveInSet().size();
                HashSet<Operand> oldSet = basicBlock.getActiveInSet();
                basicBlock.calNewActiveInSet();
//                int newInSize = basicBlock.getActiveInSet().size();
                HashSet<Operand> newSet = basicBlock.getActiveInSet();
//                if (oldInSize > newInSize) {
//                    System.err.println("error in activeVarAnalyze of inSet size");
//                }
                if (!oldSet.equals(newSet)) {
                    flag = true;
                }
            }
        }
    }

    public static void reachDefineAnalyze(FlowGraph flowGraph) {
        ArrayList<BasicBlock> basicBlocks = flowGraph.getBasicBlocks();
        basicBlocks.forEach(basicBlock -> basicBlock.calGenSet());
        HashMap<BasicBlock, HashMap<Operand, HashSet<IntermediateCode>>> defSetOfBasicBlock =
            new HashMap<>();
        basicBlocks.forEach(
            basicBlock -> defSetOfBasicBlock.put(basicBlock, basicBlock.getAllDefSet()));
        //cal killset
        for (int i = 0; i < basicBlocks.size(); i++) {
            HashMap<Operand, HashSet<IntermediateCode>> killSet = new HashMap<>();
            HashSet<Operand> defSet = basicBlocks.get(i).getGenDefVarSet();
            defSet.forEach(operand -> killSet.put(operand, new HashSet<>()));
            for (int j = 0; j < basicBlocks.size(); j++) {
                HashMap<Operand, HashSet<IntermediateCode>> allDefSet =
                    basicBlocks.get(j).getAllDefSet();
                for (Operand defVar : defSet) {
                    if (allDefSet.containsKey(defVar)) {
                        if (i == j) {
                            HashSet<IntermediateCode> intermediateCodes = allDefSet.get(defVar);
                            if (intermediateCodes.size() == 1) {
                                intermediateCodes.remove(basicBlocks.get(i).getGenSet().get(defVar));
                            }
                            killSet.get(defVar).addAll(intermediateCodes);
                        } else {
                            killSet.get(defVar).addAll(allDefSet.get(defVar));
                        }
                    }
                }
            }
//            System.err.println(killSet);
            basicBlocks.get(i).setKillSet(killSet);
        }

        boolean flag = true;
        while (flag) {
            flag = false;
            for (int i = 0; i < basicBlocks.size(); i++) {
                BasicBlock basicBlock = basicBlocks.get(i);
                basicBlock.calReachInSet();
                HashSet<IntermediateCode> oldOutSet = basicBlock.getReachOutSet();
                basicBlock.calReachOutSet();
                HashSet<IntermediateCode> newOutSet = basicBlock.getReachOutSet();
                if (!oldOutSet.equals(newOutSet)) {
                    flag = true;
                }
            }
        }
    }

    public static boolean ReplicaPropagation(FlowGraph flowGraph) {
        ArrayList<BasicBlock> basicBlocks = flowGraph.getBasicBlocks();
        boolean flag = false;
        for (BasicBlock basicBlock : basicBlocks) {
            ArrayList<IntermediateCode> intermediateCodes = basicBlock.getIntermediateCodes();
            HashSet<IntermediateCode> reachInDefSet = basicBlock.getReachInSet();
            HashMap<Operand, HashSet<IntermediateCode>> defSet = new HashMap<>();
            reachInDefSet.forEach(intermediateCode -> {
                if (defSet.containsKey(intermediateCode.target)) {
                    defSet.get(intermediateCode.target).add(intermediateCode);
                } else {
                    HashSet<IntermediateCode> temp = new HashSet<>();
                    temp.add(intermediateCode);
                    defSet.put(intermediateCode.target, temp);
                }
            });
            for (int i = 0; i < intermediateCodes.size(); i++) {
                IntermediateCode intermediateCode = intermediateCodes.get(i);
//                if (intermediateCode instanceof MemoryCode) {
//                    if (!intermediateCode.target.isGlobal()) {
//                        if (defSet.containsKey(intermediateCode.target)) {
//                            defSet.get(intermediateCode.target).clear();
//                            defSet.get(intermediateCode.target).add(intermediateCode);
//                        } else {
//                            HashSet<IntermediateCode> temp = new HashSet<>();
//                            temp.add(intermediateCode);
//                            defSet.put(intermediateCode.target, temp);
//                        }
//                    }
//                    continue;
//                }
                Pair<Operand, Operand> rightVals = intermediateCode.getRightVal();
                if (rightVals != null) {
                    Operand first = rightVals.getFirst();
                    Operand second = rightVals.getSecond();
                    if (first != null && defSet.containsKey(first) && defSet.get(first).size() == 1) {
                        IntermediateCode assignCode = defSet.get(first).iterator().next();
                        if (assignCode instanceof AssignCode && !assignCode.source1.isGlobal() &&
                            !assignCode.target.isGlobal() &&
                            !first.equals(assignCode.source1) && !assignCode.source1.getName().equals("RET")) {
                            HashSet<IntermediateCode> reWriteCodes = defSet.get(assignCode.source1);
                            if (reWriteCodes == null || reWriteCodes.size() == 0 ||
                                !hasPath(assignCode, reWriteCodes)) {
                                if (intermediateCode instanceof MemoryCode) {
                                    if (intermediateCode.op == Operator.LOAD) {
                                        System.err.println("error in copy");
                                    } else {
                                        intermediateCode.setTarget(assignCode.source1);
                                    }
                                } else {
                                    intermediateCode.setSource1(assignCode.source1);
                                }
                                flag = true;
                            }
                        }
//                        else if (assignCode instanceof CalculateCode &&
//                            intermediateCode instanceof AssignCode &&
//                            !assignCode.source1.isGlobal() &&
//                            !assignCode.target.isGlobal()) {
//                            HashSet<IntermediateCode> reWriteCodes = defSet.get(assignCode.source1);
//                            HashSet<IntermediateCode> reWriteCodes2 =
//                                defSet.get(assignCode.source2);
//                            if ((reWriteCodes == null || reWriteCodes.size() == 0 ||
//                                !hasPath(assignCode, reWriteCodes)) &&
//                                (reWriteCodes2 == null || reWriteCodes2.size() == 0 ||
//                                    !hasPath(assignCode, reWriteCodes2))) {
//                                CalculateCode calculateCode =
//                                    new CalculateCode(intermediateCode.target, assignCode.source1,
//                                        assignCode.source2, assignCode.op);
//                                intermediateCodes.add(i, calculateCode);
//                                intermediateCodes.remove(i + 1);
//                                flag = true;
//                                continue;
//                            }
//                        }
                    }
                    if (second != null && defSet.containsKey(second) && defSet.get(second).size() == 1) {
                        IntermediateCode assignCode = defSet.get(second).iterator().next();
                        if (assignCode instanceof AssignCode && !assignCode.source1.isGlobal() &&
                            !second.equals(assignCode.source1) &&
                            !assignCode.source1.getName().equals("RET")) {
                            HashSet<IntermediateCode> reWriteCodes = defSet.get(assignCode.source1);
                            if (reWriteCodes == null || reWriteCodes.size() == 0 ||
                                !hasPath(assignCode, reWriteCodes)) {
                                intermediateCode.setSource2(assignCode.source1);
                                flag = true;
                            }

                        }
                    }
                }
                if (intermediateCode instanceof CalculateCode ||
                    intermediateCode instanceof AssignCode ||
                    intermediateCode instanceof SingleCalculateCode ||
                    intermediateCode instanceof FunctionParam ||
                    intermediateCode instanceof InputCode ||
                    intermediateCode instanceof CompareCode ||
                    intermediateCode instanceof DeclCode ||
                    intermediateCode instanceof MemoryCode) {
                    if (!intermediateCode.target.isGlobal()) {
                        if (defSet.containsKey(intermediateCode.target)) {
                            defSet.get(intermediateCode.target).clear();
                            defSet.get(intermediateCode.target).add(intermediateCode);
                        } else {
                            HashSet<IntermediateCode> temp = new HashSet<>();
                            temp.add(intermediateCode);
                            defSet.put(intermediateCode.target, temp);
                        }
                    }
                }
            }
        }
        return flag;
    }

    public static boolean hasPath(IntermediateCode beginCode, HashSet<IntermediateCode> endCodes) {
        for (IntermediateCode endCode : endCodes) {
            BasicBlock beginBlock = beginCode.basicBlock;
            ArrayList<IntermediateCode> codesAftBeginCode = beginBlock.getIntermediateCodes();

            for (int pos = codesAftBeginCode.indexOf(beginCode) + 1; pos < codesAftBeginCode.size();
                 pos++) {
                if (endCode.equals(codesAftBeginCode.get(pos))) {
                    return true;
                }
            }
            HashSet<BasicBlock> nextBlocks = beginBlock.getAdjBlocks();
            for (BasicBlock nextBlock : nextBlocks) {
                ArrayList<IntermediateCode> intermediateCodes = nextBlock.getIntermediateCodes();
                for (IntermediateCode intermediateCode : intermediateCodes) {
                    if (endCode.equals(intermediateCode)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void buildConflictGraph(ConflictGraph conflictGraph, FlowGraph flowGraph) {
        ArrayList<BasicBlock> basicBlocks = flowGraph.getBasicBlocks();
        for (BasicBlock basicBlock : basicBlocks) {
            HashSet<Operand> activeInSet = basicBlock.getActiveInSet();
            ArrayList<IntermediateCode> intermediateCodes = basicBlock.getIntermediateCodes();
//            for (Operand a : activeInSet) {
//                for (Operand b : activeInSet) {
//                    conflictGraph.addEdge(a, b);
//                    conflictGraph.addEdge(b, a);
//                }
//            }
            HashSet<Operand> originActiveOutSet = new HashSet<>(basicBlock.getActiveOutSet());
            HashSet<Operand> activeOutSet = new HashSet<>(basicBlock.getActiveOutSet());
            for (int i = intermediateCodes.size() - 1; i >= 0; i--) {
                Operand leftVal = intermediateCodes.get(i).getLeftVal();
                if (leftVal == null || !leftVal.isLocalAndVar()) {
                    HashSet<Operand> usedVal = intermediateCodes.get(i).getUsedSet();
                    if (activeOutSet.contains(leftVal) && leftVal != null && leftVal.isTemp()) {
                        activeOutSet.forEach(conflictGraph::addNode);
                        activeOutSet.forEach(j -> {
                            if (leftVal.isLocalAndVar() || (activeOutSet.contains(leftVal) && leftVal.isTemp())) {
                                conflictGraph.addEdge(leftVal, j);
                                conflictGraph.addEdge(j, leftVal);
                            }
                        });
                        activeOutSet.remove(leftVal);
                    }
                    for (Operand operand : usedVal) {
                        if (operand.isLocalAndVar() ||
                            (activeOutSet.contains(operand) && operand.isTemp())) {
                            activeOutSet.add(operand);
                        }
                    }
//                    System.err.println(intermediateCodes.get(i));
//                    System.err.println(usedVal);
                    continue;
                }
//                if (leftVal.getName().equals("z_1")) {
//                    intermediateCodes.get(i).output();
//                }
                activeOutSet.forEach(conflictGraph::addNode);
                HashSet<Operand> usedVal = intermediateCodes.get(i).getUsedSet();
//                System.err.println(intermediateCodes.get(i));
//                System.err.println(usedVal);
                activeOutSet.forEach(j -> {
                    if (leftVal.isLocalAndVar() || (activeOutSet.contains(leftVal) && leftVal.isTemp())) {
//                        System.err.println(activeOutSet);
                        conflictGraph.addEdge(leftVal, j);
                        conflictGraph.addEdge(j, leftVal);
                    }
                });
                activeOutSet.remove(leftVal);
                for (Operand operand : usedVal) {
                    if (operand.isLocalAndVar() ||
                        (activeOutSet.contains(operand) && operand.isTemp())) {
                        activeOutSet.add(operand);
                    }
                }
            }
        }
    }

    public static boolean deleteDeadCode(FlowGraph flowGraph) {
        ArrayList<BasicBlock> basicBlocks = flowGraph.getBasicBlocks();
        boolean flag = false;
        for (BasicBlock basicBlock : basicBlocks) {
            flag = basicBlock.deadCodesDelete();
        }
        return flag;
    }

    public static boolean peepHolesForReWrite(FlowGraph flowGraph) {
        boolean flag = false;
        ArrayList<BasicBlock> basicBlocks = flowGraph.getBasicBlocks();
        for (BasicBlock basicBlock : basicBlocks) {
            ArrayList<IntermediateCode> intermediateCodes = basicBlock.getIntermediateCodes();
            for (int i = 0; i < intermediateCodes.size(); i++) {
                IntermediateCode intermediateCode = intermediateCodes.get(i);
                if (intermediateCode instanceof CalculateCode) {
//                    System.err.println(intermediateCode);
                    if (intermediateCode.source1.isNUMBER() &&
                        intermediateCode.source2.isNUMBER()) {
                        AssignCode assignCode = new AssignCode(intermediateCode.target,
                            ((CalculateCode) intermediateCode).getValue());
                        assignCode.setBasicBlock(intermediateCode.getBasicBlock());
                        intermediateCodes.add(i, assignCode);
                        intermediateCodes.remove(i + 1);
                        i--;
                        flag = true;
                    } else if (intermediateCode.source1.isNUMBER() ||
                        intermediateCode.source2.isNUMBER()) {
                        Operand newVar = ((CalculateCode) intermediateCode).getValue();
                        if (newVar != null) {
                            AssignCode assignCode = new AssignCode(intermediateCode.target,
                                ((CalculateCode) intermediateCode).getValue());
                            assignCode.setBasicBlock(intermediateCode.getBasicBlock());
                            intermediateCodes.add(i, assignCode);
                            intermediateCodes.remove(i + 1);
                            i--;
                            flag = true;
                        }
                    }
                } else if (intermediateCode instanceof SingleCalculateCode) {
                    if (intermediateCode.source1.isNUMBER()) {
                        AssignCode assignCode = new AssignCode(intermediateCode.target,
                            ((SingleCalculateCode) intermediateCode).getValue());
                        assignCode.setBasicBlock(intermediateCode.getBasicBlock());
                        intermediateCodes.add(i, assignCode);
                        intermediateCodes.remove(i + 1);
                        i--;
                        flag = true;
                    }
                } else if (intermediateCode instanceof AssignCode) {
                    if (intermediateCode.target.equals(intermediateCode.source1)) {
                        intermediateCodes.remove(i);
                        i--;
                        flag = true;
                    } else if (intermediateCode.target.isTemp()) {
                        int j = i + 1;
                        if (j < intermediateCodes.size() &&
                            intermediateCodes.get(j) instanceof CalculateCode) {
                            IntermediateCode intermediateCode1 = intermediateCodes.get(j);
                            if (intermediateCode1.source1.equals(intermediateCode.target)) {
                                intermediateCode1.setSource1(intermediateCode.source1);
                                intermediateCodes.remove(i);
                                i--;
                                flag = true;
                            } else if (intermediateCode1.source2.equals(intermediateCode.target)) {
                                intermediateCode1.setSource2(intermediateCode.source1);
                                intermediateCodes.remove(i);
                                i--;
                                flag = true;
                            }
                        }
                    }
                } else if (intermediateCode instanceof MemoryCode &&
                    intermediateCode.op == Operator.LOAD && intermediateCode.source2.isNUMBER() && intermediateCode.source1.isGlobal()) {
                    if (Optimizer.intermediateVisitor.arrayIsConst(
                        intermediateCode.source1.getName())) {
                        int val = Optimizer.intermediateVisitor.getValOfConstGlobalArray(
                            intermediateCode.source1.getName(),
                            Integer.parseInt(intermediateCode.source2.getName()));
                        AssignCode assignCode = new AssignCode(intermediateCode.target,
                            Operand.getNewOperand(String.valueOf(val), Operand.OperandType.NUMBER));
                        assignCode.setBasicBlock(intermediateCode.getBasicBlock());
                        intermediateCodes.add(i, assignCode);
                        intermediateCodes.remove(i + 1);
                        flag = true;
                    }
                }
            }
            for (int i = 0; i < intermediateCodes.size(); i++) {
                IntermediateCode intermediateCode = intermediateCodes.get(i);
                if (intermediateCode instanceof CalculateCode) {
                    int j = i + 1;
                    if (j < intermediateCodes.size() &&
                        intermediateCodes.get(i) instanceof CalculateCode) {
                        IntermediateCode intermediateCode1 = intermediateCodes.get(j);
                        if (intermediateCode.op == Operator.ADD &&
                            intermediateCode1.op == Operator.ADD &&
                            intermediateCode.target.isTemp() &&
                            intermediateCode1.source1.equals(intermediateCode.target) &&
                            intermediateCode.source2.isNUMBER() &&
                            intermediateCode1.source2.isNUMBER()) {
                            intermediateCode1.setSource1(intermediateCode.source1);
                            intermediateCode1.setSource2(Operand.getNewOperand(String.valueOf(
                                    Integer.parseInt(intermediateCode.source2.getName()) +
                                        Integer.parseInt(intermediateCode1.source2.getName())),
                                Operand.OperandType.NUMBER));
                            intermediateCodes.remove(i);
                            i--;
                            flag = true;
                        } else if (intermediateCode.op == Operator.ADD &&
                            intermediateCode1.op == Operator.SUB &&
                            intermediateCode.target.isTemp() &&
                            intermediateCode1.source1.equals(intermediateCode.target) &&
                            intermediateCode.source2.isNUMBER() &&
                            intermediateCode1.source2.isNUMBER()) {
                            intermediateCode1.setOp(Operator.ADD);
                            intermediateCode1.setSource1(intermediateCode.source1);
                            intermediateCode1.setSource2(Operand.getNewOperand(String.valueOf(
                                    Integer.parseInt(intermediateCode.source2.getName()) -
                                        Integer.parseInt(intermediateCode1.source2.getName())),
                                Operand.OperandType.NUMBER));
                            intermediateCodes.remove(i);
                            i--;
                            flag = true;
                        }
                    }
                }
            }

            for (int i = 0; i < intermediateCodes.size(); i++) {
                IntermediateCode intermediateCode = intermediateCodes.get(i);
                if (intermediateCode instanceof CalculateCode) {
                    if (intermediateCode.op == Operator.DIV && intermediateCode.source1.getName()
                        .equals(intermediateCode.source2.getName())) {
                        AssignCode assignCode = new AssignCode(intermediateCode.target,
                            Operand.getNewOperand("1", Operand.OperandType.NUMBER));
                        intermediateCodes.add(i, assignCode);
                        intermediateCodes.remove(i + 1);
                    }
                }
            }
        }
        return flag;
    }

    public static boolean peepholes(FlowGraph flowGraph) {
        boolean flag = false;
        ArrayList<BasicBlock> basicBlocks = flowGraph.getBasicBlocks();
        for (BasicBlock basicBlock : basicBlocks) {
            ArrayList<IntermediateCode> intermediateCodes = basicBlock.getIntermediateCodes();
            for (int i = 0; i < intermediateCodes.size(); i++) {
                IntermediateCode intermediateCode = intermediateCodes.get(i);
                if (intermediateCode instanceof CalculateCode ||
                    intermediateCode instanceof InputCode ||
                    intermediateCode instanceof AssignCode ||
                    intermediateCode instanceof SingleCalculateCode) {
//                    System.err.println(intermediateCode);
                    int j = i + 1;
                    if (j < intermediateCodes.size() &&
                        intermediateCodes.get(j) instanceof AssignCode) {
                        IntermediateCode intermediateCode1 = intermediateCodes.get(j);
                        if (intermediateCode.target.isTemp() &&
                            intermediateCode1.source1.equals(intermediateCode.target)) {
                            intermediateCode.setTarget(intermediateCode1.getTarget());
                            intermediateCodes.remove(j);
                            flag = true;
                        }
                    }
                }
            }
            for (int i = 0; i < intermediateCodes.size(); i++) {
                IntermediateCode intermediateCode = intermediateCodes.get(i);
                if (intermediateCode instanceof SingleCalculateCode &&
                    intermediateCode.target.isTemp()) {
                    int j = i + 1;
                    if (j < intermediateCodes.size() &&
                        intermediateCodes.get(j) instanceof SingleCalculateCode) {
                        IntermediateCode intermediateCode1 = intermediateCodes.get(j);
                        if (intermediateCode1.source1.equals(intermediateCode.target)) {
                            if (intermediateCode.op == Operator.NEG &&
                                intermediateCode1.op == Operator.NEG) {
                                intermediateCode1.setSource1(intermediateCode.source1);
                                intermediateCode1.setOp(Operator.PLUS);
                                intermediateCodes.remove(i);
                                i--;
                                flag = true;
                            } else if (intermediateCode.op == Operator.PLUS &&
                                intermediateCode1.op == Operator.NEG) {
                                intermediateCode1.setSource1(intermediateCode.source1);
                                intermediateCode1.setOp(Operator.NEG);
                                intermediateCodes.remove(i);
                                i--;
                                flag = true;
                            } else if (intermediateCode.op == Operator.NEG &&
                                intermediateCode1.op == Operator.PLUS) {
                                intermediateCode1.setSource1(intermediateCode.source1);
                                intermediateCode1.setOp(Operator.NEG);
                                intermediateCodes.remove(i);
                                i--;
                                flag = true;
                            } else if (intermediateCode.op == Operator.PLUS &&
                                intermediateCode1.op == Operator.PLUS) {
                                intermediateCode1.setSource1(intermediateCode.source1);
                                intermediateCode1.setOp(Operator.PLUS);
                                intermediateCodes.remove(i);
                                i--;
                                flag = true;
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < intermediateCodes.size(); i++) {
                IntermediateCode intermediateCode = intermediateCodes.get(i);
                if (intermediateCode instanceof SingleCalculateCode &&
                    intermediateCode.target.isTemp()) {
                    int j = i + 1;
                    if (j < intermediateCodes.size() &&
                        intermediateCodes.get(j) instanceof AssignCode) {
                        IntermediateCode intermediateCode1 = intermediateCodes.get(j);
                        if (intermediateCode.target.equals(intermediateCode1.source1)) {
                            intermediateCode.setTarget(intermediateCode1.getTarget());
                            intermediateCodes.remove(j);
                            flag = true;
                        }
                    }
                }
            }
            for (int i = 0; i < intermediateCodes.size(); i++) {
                IntermediateCode intermediateCode = intermediateCodes.get(i);
                if (intermediateCode instanceof CalculateCode) {
                    int j = i + 1;
                    if (j < intermediateCodes.size() &&
                        intermediateCodes.get(j) instanceof CalculateCode) {
                        IntermediateCode intermediateCode1 = intermediateCodes.get(j);
                        if (intermediateCode.op == Operator.ADD &&
                            intermediateCode1.op == Operator.ADD &&
                            intermediateCode.target.isTemp()) {
                            if (intermediateCode.target.equals(intermediateCode1.source1)) {
                                if (intermediateCode.source2.isNUMBER() &&
                                    intermediateCode1.source2.isNUMBER()) {
                                    intermediateCode1.setSource2(
                                        Operand.getNewOperand(String.valueOf(
                                                Integer.parseInt(intermediateCode.source2.getName()) +
                                                    Integer.parseInt(
                                                        intermediateCode1.source2.getName())),
                                            Operand.OperandType.NUMBER));
                                    intermediateCode1.setSource1(intermediateCode.source1);
                                    intermediateCodes.remove(i);
                                    i--;
                                    flag = true;
                                }
                            }
                        } else if (intermediateCode.op == Operator.SUB &&
                            intermediateCode1.op == Operator.ADD &&
                            intermediateCode.target.isTemp()) {
                            //t0 = 3 - a
                            //b = t0 + 4
                            if (intermediateCode.target.equals(intermediateCode1.source1)) {
                                if (intermediateCode.source1.isNUMBER() &&
                                    intermediateCode1.source2.isNUMBER()) {
                                    intermediateCode.setSource1(
                                        Operand.getNewOperand(String.valueOf(
                                                Integer.parseInt(intermediateCode.source1.getName()) +
                                                    Integer.parseInt(
                                                        intermediateCode1.source2.getName())),
                                            Operand.OperandType.NUMBER));
                                    intermediateCode.setTarget(intermediateCode1.target);
                                    intermediateCodes.remove(j);
                                    flag = true;
                                }
                            }
                        } else if (intermediateCode.op == Operator.MUL &&
                            intermediateCode1.op == Operator.MUL &&
                            intermediateCode.target.isTemp()) {
                            if (intermediateCode.target.equals(intermediateCode1.source1)) {
                                if (intermediateCode.source2.isNUMBER() &&
                                    intermediateCode1.source2.isNUMBER()) {
                                    intermediateCode1.setSource2(
                                        Operand.getNewOperand(String.valueOf(
                                                Integer.parseInt(intermediateCode.source2.getName()) *
                                                    Integer.parseInt(
                                                        intermediateCode1.source2.getName())),
                                            Operand.OperandType.NUMBER));
                                    intermediateCode1.setSource1(intermediateCode.source1);
                                    intermediateCodes.remove(i);
                                    i--;
                                    flag = true;
                                }
                            }
                        } else if (intermediateCode.op == Operator.DIV &&
                            intermediateCode1.op == Operator.DIV &&
                            intermediateCode.target.isTemp()) {
                            if (intermediateCode.target.equals(intermediateCode1.source1)) {
                                if (intermediateCode.source2.isNUMBER() &&
                                    intermediateCode1.source2.isNUMBER()) {
                                    intermediateCode1.setSource2(
                                        Operand.getNewOperand(String.valueOf(
                                                Integer.parseInt(intermediateCode.source2.getName()) *
                                                    Integer.parseInt(
                                                        intermediateCode1.source2.getName())),
                                            Operand.OperandType.NUMBER));
                                    intermediateCode1.setSource1(intermediateCode.source1);
                                    intermediateCodes.remove(i);
                                    i--;
                                    flag = true;
                                }
                            }
                        } else if (intermediateCode.op == Operator.MUL &&
                            intermediateCode1.op == Operator.DIV &&
                            intermediateCode.target.isTemp()) {
                            if (intermediateCode.target.equals(intermediateCode1.source1)) {
                                if (intermediateCode.source2.isVar() &&
                                    intermediateCode1.source2.isVar() &&
                                    intermediateCode.source2.equals(intermediateCode1.source2)) {
                                    intermediateCode1.setSource2(
                                        Operand.getNewOperand("1", Operand.OperandType.NUMBER));
                                    intermediateCode1.setSource1(intermediateCode.source1);
                                    intermediateCodes.remove(i);
                                    i--;
                                    flag = true;
                                } else if (intermediateCode.source2.isNUMBER() &&
                                    intermediateCode1.source2.isNUMBER() &&
                                    Integer.parseInt(intermediateCode.source2.getName()) >=
                                        Integer.parseInt(intermediateCode1.source2.getName())) {
                                    intermediateCode.setSource2(
                                        Operand.getNewOperand(String.valueOf(
                                                Integer.parseInt(intermediateCode.source2.getName()) /
                                                    Integer.parseInt(
                                                        intermediateCode1.source2.getName())),
                                            Operand.OperandType.NUMBER));
                                    intermediateCode.setTarget(intermediateCode1.target);
                                    intermediateCodes.remove(i + 1);
                                    flag = true;
                                }
                            }
                        }
                    }
                }
            }
            //bug
//            for (int i = 0; i < intermediateCodes.size(); i++) {
//                IntermediateCode intermediateCode = intermediateCodes.get(i);
//
//                Operand src1 = intermediateCode.source1;
//                if (src1 != null && src1.isGlobal() && src1.isVar()) {
//                    if (!Optimizer.intermediateVisitor.globalVarIsAssign(src1.getName())) {
//                        intermediateCode.setSource1(Operand.getNewOperand(
//                            String.valueOf(Optimizer.intermediateVisitor.getValOfNonAssignVar(
//                                src1.getName())), Operand.OperandType.NUMBER));
//                        flag = true;
//                    }
//                }
//                Operand src2 = intermediateCode.source2;
//                if (src2 != null && src2.isGlobal() && src2.isVar()) {
//                    if (!Optimizer.intermediateVisitor.globalVarIsAssign(src2.getName())) {
//                        intermediateCode.setSource2(Operand.getNewOperand(
//                            String.valueOf(Optimizer.intermediateVisitor.getValOfNonAssignVar(
//                                src2.getName())), Operand.OperandType.NUMBER));
//                        flag = true;
//                    }
//                }
//
//                Operand target = intermediateCode.target;
//                if (target != null && target.isGlobal() && target.isVar()) {
//                    if (!(intermediateCode instanceof MemoryCode &&
//                        intermediateCode.op == Operator.LOAD)) {
//                        Optimizer.intermediateVisitor.globalVarSetAssign(target.getName());
//                    }
//                }
//            }

            for (int i = 0; i < intermediateCodes.size(); i++) {
                IntermediateCode intermediateCode = intermediateCodes.get(i);
                if (intermediateCode instanceof CalculateCode &&
                    !intermediateCode.target.isGlobal()) {
                    int j = i + 1;
                    if (j < intermediateCodes.size() &&
                        intermediateCodes.get(j) instanceof FunctionReturnCode) {
                        IntermediateCode intermediateCode1 = intermediateCodes.get(j);
                        if (intermediateCode1.source1 != null &&
                            intermediateCode.target.equals(intermediateCode1.source1)) {
                            intermediateCode.setTarget(
                                Operand.getNewOperand("RET", Operand.OperandType.VAR));
                            intermediateCode1.setSource1(Operand.getNewOperand("RET", Operand.OperandType.VAR));
                        }
                    }
                }
            }
        }
        return flag;
    }
}
