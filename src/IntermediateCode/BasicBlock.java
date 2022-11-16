package IntermediateCode;

import java.util.ArrayList;
import java.util.HashSet;

import Tool.Pair;

public class BasicBlock {
    private HashSet<String> tags;
    private HashSet<BasicBlock> successor;
    private HashSet<BasicBlock> precursor;
    private ArrayList<IntermediateCode> intermediateCodes;
    private HashSet<Operand> usedSet;
    private HashSet<Operand> defSet;
    private HashSet<Operand> activeInSet = new HashSet<>();
    private HashSet<Operand> activeOutSet = new HashSet<>();
    private HashSet<IntermediateCode> reachInSet = new HashSet<>();
    private HashSet<IntermediateCode> reachOutSet = new HashSet<>();
    private boolean isBegin;
    private int id;

    public BasicBlock(int id) {
        this.intermediateCodes = new ArrayList<>();
        this.tags = new HashSet<>();
        this.successor = new HashSet<>();
        this.precursor = new HashSet<>();
        this.usedSet = new HashSet<>();
        this.defSet = new HashSet<>();
        this.id = id;
    }

    public void addSuccessor(BasicBlock basicBlock) {
        successor.add(basicBlock);
    }

    public void addPrecursor(BasicBlock basicBlock) {
        precursor.add(basicBlock);
    }

    public boolean isBegin() {
        return isBegin;
    }

    public void setBegin(boolean begin) {
        isBegin = begin;
    }

    public ArrayList<IntermediateCode> getIntermediateCodes() {
        return intermediateCodes;
    }

    public void addIntermediateCode(IntermediateCode intermediateCode) {
        intermediateCodes.add(intermediateCode);
    }

    public IntermediateCode getLastCode() {
        return intermediateCodes.get(intermediateCodes.size() - 1);
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public HashSet<String> getTags() {
        return tags;
    }

    public boolean containTag(String tag) {
        return tags.contains(tag);
    }

    public void calUsedDefSet() {
        //什么是in和out
        HashSet<Operand> seenVarSet = new HashSet<>();
        for (IntermediateCode intermediateCode : intermediateCodes) {
            Operand leftVal = intermediateCode.getLeftVal();
            Pair<Operand, Operand> rightVals = intermediateCode.getRightVal();
            if (rightVals != null) {
                addUsedSet(seenVarSet, rightVals.getFirst());
                addUsedSet(seenVarSet, rightVals.getSecond());
            }
            if (leftVal != null) {
                addDefSet(seenVarSet, leftVal);
            }
        }
    }

    public void addUsedSet(HashSet<Operand> seenVarSet, Operand rightVal) {
        if (rightVal != null && rightVal.isLocal()) {
            if (!rightVal.getName().equals("RET") && !seenVarSet.contains(rightVal)) {
                seenVarSet.add(rightVal);
                usedSet.add(rightVal);
            }
        }
    }

    public void addDefSet(HashSet<Operand> seenVarSet, Operand leftVal) {
        if (leftVal != null && leftVal.isLocal()) {
            if (!leftVal.getName().equals("RET") && !seenVarSet.contains(leftVal)) {
                seenVarSet.add(leftVal);
                defSet.add(leftVal);
            }
        }
    }

    public HashSet<Operand> getActiveInSet() {
        return activeInSet;
    }

    public HashSet<Operand> getActiveOutSet() {
        return activeOutSet;
    }

    public void calNewActiveOutSet() {
        for (BasicBlock basicBlock : successor) {
            activeOutSet.addAll(basicBlock.getActiveInSet());
        }
    }

    public void calNewActiveInSet() {
        HashSet<Operand> temp = new HashSet<>(activeOutSet);
        temp.removeAll(defSet);
        HashSet<Operand> ans = new HashSet<>(usedSet);
        ans.addAll(temp);
        activeInSet = ans;
    }

    public HashSet<Integer> getSuccessorIds() {
        HashSet<Integer> ans = new HashSet<>();
        for (BasicBlock basicBlock : successor) {
            ans.add(basicBlock.id);
        }
        return ans;
    }

    public void testPrint() {
        System.err.print("next: ");
        System.err.println(getSuccessorIds());
        System.err.print("in: ");
        for (Operand operand : activeInSet) {
            System.err.print(operand + " ");
        }
        System.err.println();
        System.err.print("out: ");
        for (Operand operand : activeOutSet) {
            System.err.print(operand + " ");
        }
        System.err.println();
        System.err.print("used: ");
        for (Operand operand : usedSet) {
            System.err.println(operand + " ");
        }
        System.err.println();
    }

    public void output() {
        for (IntermediateCode intermediateCode : intermediateCodes) {
            intermediateCode.output();
        }
    }
}
