package IntermediateCode;

import java.util.HashMap;
import java.util.HashSet;

public class ConflictGraph {
    private HashMap<Operand, HashSet<Operand>> nodesAndEdges = new HashMap<>();
    private HashMap<Operand, String> usedGlobalRegs = new HashMap<>();

    public ConflictGraph() {

    }

    public HashMap<Operand, HashSet<Operand>> getNodesAndEdges() {
        HashMap<Operand, HashSet<Operand>> ans = new HashMap<>();
        nodesAndEdges.forEach((k, v) -> ans.put(k, new HashSet<>(v)));
        return ans;
    }

    public void addEdge(Operand a, Operand b) {
        if (a.equals(b)) {
            return;
        }
        if (!nodesAndEdges.containsKey(a)) {
            addNode(a);
        }
        HashSet<Operand> edges = nodesAndEdges.get(a);
        edges.add(b);
    }

    public void addNode(Operand operand) {
        if (nodesAndEdges.containsKey(operand)) {
            return;
        } else {
            nodesAndEdges.put(operand, new HashSet<>());
        }
    }

    public void addUsedGlobalRegs(Operand var, String reg) {
        usedGlobalRegs.put(var, reg);
    }

    public HashSet<String> getUsedGlobalRegs() {
        return new HashSet<>(usedGlobalRegs.values());
    }
}
