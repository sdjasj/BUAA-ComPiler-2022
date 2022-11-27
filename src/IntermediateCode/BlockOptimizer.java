package IntermediateCode;

import IntermediateCode.AllCode.AssignCode;
import IntermediateCode.AllCode.CalculateCode;
import IntermediateCode.AllCode.InputCode;
import IntermediateCode.AllCode.SingleCalculateCode;
import MipsCode.MipsCode.MipsCode;
import Tool.Pair;

import java.util.ArrayList;
import java.util.HashSet;

public class BlockOptimizer {
    public static void activeVarAnalyze(FlowGraph flowGraph) {
        ArrayList<BasicBlock> basicBlocks = flowGraph.getBasicBlocks();
        for (BasicBlock basicBlock : basicBlocks) {
            basicBlock.calUsedDefSet();
        }
        boolean flag = true;
        while (flag) {
            flag = false;
            for (int i = basicBlocks.size() - 1; i >= 0; i--) {
                BasicBlock basicBlock = basicBlocks.get(i);
                basicBlock.calNewActiveOutSet();
                int oldInSize = basicBlock.getActiveInSet().size();
                basicBlock.calNewActiveInSet();
                int newInSize = basicBlock.getActiveInSet().size();
                if (oldInSize > newInSize) {
                    System.err.println("error in activeVarAnalyze of inSet size");
                }
                if (oldInSize < newInSize) {
                    flag = true;
                }
            }
        }
    }

    public static void buildConflictGraph(ConflictGraph conflictGraph, FlowGraph flowGraph) {
        ArrayList<BasicBlock> basicBlocks = flowGraph.getBasicBlocks();
        for (BasicBlock basicBlock : basicBlocks) {
            HashSet<Operand> activeInSet = basicBlock.getActiveInSet();
            ArrayList<IntermediateCode> intermediateCodes = basicBlock.getIntermediateCodes();
            for (Operand a : activeInSet) {
                for (Operand b : activeInSet) {
                    conflictGraph.addEdge(a, b);
                    conflictGraph.addEdge(b, a);
                }
            }
            HashSet<Operand> activeOutSet = new HashSet<>(basicBlock.getActiveOutSet());
            for (int i = intermediateCodes.size() - 1; i >= 0; i--) {
                Operand leftVal = intermediateCodes.get(i).getLeftVal();
                if (leftVal == null || !leftVal.isLocal()) {
                    HashSet<Operand> usedVal = intermediateCodes.get(i).getUsedSet();
                    activeOutSet.addAll(usedVal);
                    continue;
                }
//                if (leftVal.getName().equals("z_1")) {
//                    intermediateCodes.get(i).output();
//                }
                activeOutSet.forEach(conflictGraph::addNode);
                HashSet<Operand> usedVal = intermediateCodes.get(i).getUsedSet();
                activeOutSet.forEach(j -> {
                    conflictGraph.addEdge(leftVal, j);
                    conflictGraph.addEdge(j, leftVal);
                });
                activeOutSet.remove(leftVal);
                activeOutSet.addAll(usedVal);
            }
        }
    }

    public static void deleteDeadCode(FlowGraph flowGraph) {
        ArrayList<BasicBlock> basicBlocks = flowGraph.getBasicBlocks();
        for (BasicBlock basicBlock : basicBlocks) {
            while (basicBlock.deadCodesDelete()) {}
        }
    }

    public static void peepholes(FlowGraph flowGraph) {
        ArrayList<BasicBlock> basicBlocks = flowGraph.getBasicBlocks();
        for (BasicBlock basicBlock : basicBlocks) {
            ArrayList<IntermediateCode> intermediateCodes = basicBlock.getIntermediateCodes();
            for (int i = 0; i < intermediateCodes.size(); i++) {
                IntermediateCode intermediateCode = intermediateCodes.get(i);
                if (intermediateCode instanceof CalculateCode ||
                    intermediateCode instanceof InputCode ||
                    intermediateCode instanceof AssignCode) {
                    int j = i + 1;
                    if (j < intermediateCodes.size() &&
                        intermediateCodes.get(j) instanceof AssignCode) {
                        IntermediateCode intermediateCode1 = intermediateCodes.get(j);
                        if (intermediateCode instanceof CalculateCode &&
                            intermediateCode1.target.isGlobal()) {
                            continue;
                        }
                        if (intermediateCode.target.isTemp() &&
                            intermediateCode1.source1.equals(intermediateCode.target)) {
                            intermediateCode.setTarget(intermediateCode1.getTarget());
                            intermediateCodes.remove(j);
                        }
                    }
                } else if (intermediateCode instanceof SingleCalculateCode) {
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
                            } else if (intermediateCode.op == Operator.PLUS &&
                                intermediateCode1.op == Operator.NEG) {
                                intermediateCode1.setSource1(intermediateCode.source1);
                                intermediateCode1.setOp(Operator.NEG);
                                intermediateCodes.remove(i);
                                i--;
                            } else if (intermediateCode.op == Operator.NEG &&
                                intermediateCode1.op == Operator.PLUS) {
                                intermediateCode1.setSource1(intermediateCode.source1);
                                intermediateCode1.setOp(Operator.NEG);
                                intermediateCodes.remove(i);
                                i--;
                            } else if (intermediateCode.op == Operator.PLUS &&
                                intermediateCode1.op == Operator.PLUS) {
                                intermediateCode1.setSource1(intermediateCode.source1);
                                intermediateCode1.setOp(Operator.PLUS);
                                intermediateCodes.remove(i);
                                i--;
                            }
                        }
                    } else if (j < intermediateCodes.size() &&
                        intermediateCodes.get(j) instanceof AssignCode) {
                        IntermediateCode intermediateCode1 = intermediateCodes.get(j);
                        if (intermediateCode.target.equals(intermediateCode1.source1)) {
                            intermediateCode.setTarget(intermediateCode1.getTarget());
                            intermediateCodes.remove(j);
                        }
                    }
                }
            }
        }
    }
}
