package IntermediateCode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class RegisterAllocator {
    private ConflictGraph conflictGraph;

    public static ArrayList<String> globalRegs = new ArrayList<String>() {
        {
            add("$s0");
            add("$s1");
            add("$s2");
            add("$s3");
            add("$s4");
            add("$s5");
            add("$s6");
            add("$s7");
            add("$fp");
            add("$gp");
            add("$k0");
            add("$k1");
            add("$ra");
            add("$v1");
        }
    };

    public RegisterAllocator(ConflictGraph conflictGraph) {
        this.conflictGraph = conflictGraph;
    }

    public static void setGlobalRegs(ArrayList<String> globalRegs) {
        RegisterAllocator.globalRegs = globalRegs;
    }

    public void allocGlobalReg() {
        ArrayList<Operand> stack = new ArrayList<>();
        HashMap<Operand, HashSet<Operand>> edges = conflictGraph.getNodesAndEdges();
        ArrayList<Operand> nodes = new ArrayList<>(edges.keySet());
        while (nodes.size() > 0) {
//            for (Map.Entry<Operand, HashSet<Operand>> entry : edges.entrySet()) {
//                System.err.println(entry.getKey());
//                System.err.println(entry.getValue().size());
//            }
            nodes.sort(Comparator.comparingInt((Operand a) -> edges.get(a).size()));
            Operand node = nodes.get(0);
            if (edges.get(node).size() >= globalRegs.size()) {
//                System.err.println(edges.get(node).size());
                node = nodes.get(nodes.size() - 1);
                stack.add(node);
            } else {
//                System.err.println(edges.get(node).size());
//                System.err.println(globalRegs.size());
                node.setAllocatedReg(true);
                stack.add(node);
            }
            Operand finalNode = node;
            edges.forEach((k, v) -> edges.get(k).remove(finalNode));
            edges.remove(node);
            nodes.remove(node);
        }
        HashMap<Operand, HashSet<Operand>> colorEdge = conflictGraph.getNodesAndEdges();
        for (int i = stack.size() - 1; i >= 0; i--) {
            Operand node = stack.get(i);
            if (node.isAllocatedReg()) {
                HashSet<String> unUsedReg = new HashSet<>(globalRegs);
                HashSet<Operand> adjNodes = colorEdge.get(node);
                for (Operand adjNode : adjNodes) {
                    if (adjNode.isAllocatedReg() && adjNode.getReg() != null) {
                        unUsedReg.remove(adjNode.getReg());
                    }
                }
                if (unUsedReg.size() == 0) {
                    System.err.println(node);
                    System.err.println("error in color alloc reg");
                }
                String reg = unUsedReg.iterator().next();
                node.setReg(reg);
                conflictGraph.addUsedGlobalRegs(node, reg);
            }
        }
//        for (Operand node : stack) {
//            if (node.isAllocatedReg()) {
//                System.err.println(node.getName() + " " + node.getReg());
//            }
//        }
//        System.err.println();
//        System.err.println();
    }

}
