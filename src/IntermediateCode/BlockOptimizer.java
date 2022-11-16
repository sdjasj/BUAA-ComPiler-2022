package IntermediateCode;

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
}
