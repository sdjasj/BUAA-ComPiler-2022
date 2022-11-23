package IntermediateCode;

import java.util.ArrayList;
import java.util.HashSet;

import IntermediateCode.AllCode.InputCode;
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
        intermediateCode.setBasicBlock(this);
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

    public int findUsedVarNextCode(IntermediateCode curCode, Operand operand) {
        int pos = intermediateCodes.indexOf(curCode);
        for (int i = pos + 1; i < intermediateCodes.size(); i++) {
            IntermediateCode intermediateCode = intermediateCodes.get(i);
            Operand leftVal = intermediateCode.getLeftVal();
            if (leftVal != null && (leftVal.isTemp() || leftVal.isLocal() || leftVal.isGlobal())) {
                if (operand.equals(leftVal)) {
                    return Integer.MAX_VALUE;
                }
            }
            Pair<Operand, Operand> rightVals = intermediateCode.getRightVal();
            if (rightVals != null) {
                Operand operand1 = rightVals.getFirst();
                if (operand1 != null && (operand1.isTemp() || operand1.isLocal() || operand1.isGlobal())) {
                    if (operand.equals(operand1)) {
                        return i - pos;
                    }
                }
                Operand operand2 = rightVals.getSecond();
                if (operand2 != null && (operand2.isTemp() || operand2.isLocal() || operand2.isGlobal())) {
                    if (operand.equals(operand2)) {
                        return i - pos;
                    }
                }
            }
        }
        if (activeOutSet.contains(operand)) {
            return intermediateCodes.size() - pos;
        }
        return Integer.MAX_VALUE;
    }

    public void addUsedTempVarForFunctionCall(HashSet<Operand> usedTempVars,
                                              IntermediateCode callCode) {
        int pos = intermediateCodes.indexOf(callCode);
        for (int i = intermediateCodes.size() - 1; i > pos; i--) {
            IntermediateCode intermediateCode = intermediateCodes.get(i);
            Operand leftVal = intermediateCode.getLeftVal();
            if (leftVal != null && (leftVal.isTemp() || leftVal.isLocal() || leftVal.isGlobal())) {
                usedTempVars.remove(leftVal);
            }
            Pair<Operand, Operand> rightVals = intermediateCode.getRightVal();
            if (rightVals != null) {
                Operand operand1 = rightVals.getFirst();
                if (operand1 != null && (operand1.isTemp() || operand1.isLocal() || operand1.isGlobal())) {
                    usedTempVars.add(operand1);
                }
                Operand operand2 = rightVals.getSecond();
                if (operand2 != null && (operand2.isTemp() || operand2.isLocal() || operand2.isGlobal())) {
                    usedTempVars.add(operand2);
                }
            }
        }
    }

    public boolean deadCodesDelete() {
        HashSet<Integer> removeSet = new HashSet<>();
        HashSet<Operand> activeSet = new HashSet<>(activeOutSet);
        for (int i = intermediateCodes.size() - 1; i >= 0; i--) {
            IntermediateCode intermediateCode = intermediateCodes.get(i);
            Operand leftVal = intermediateCode.getLeftVal();
            Pair<Operand, Operand> rightVal = intermediateCode.getRightVal();
            if (leftVal != null && !leftVal.isGlobal() && leftVal.isVar() && !(intermediateCode instanceof InputCode) &&
                !activeSet.contains(leftVal)) {
                removeSet.add(i);
//                HashSet<Operand> waitToDelete = new HashSet<>();
//                if (rightVal != null) {
//                    Operand first = rightVal.getFirst();
//                    Operand second = rightVal.getSecond();
//                    if (first != null && first.isTemp()) {
//                        waitToDelete.add(first);
//                    }
//                    if (second != null && second.isTemp()) {
//                        waitToDelete.add(second);
//                    }
//                }
//                if (!waitToDelete.isEmpty()) {
//                    for (int j = i - 1; j >= 0; j--) {
//                        IntermediateCode intermediateCode1 = intermediateCodes.get(j);
//                        Operand defineVar = intermediateCode1.target;
//                        if (defineVar != null && waitToDelete.contains(defineVar)) {
//                            removeSet.add(j);
//                            waitToDelete.remove(defineVar);
//                            Pair<Operand, Operand> usedVal = intermediateCode1.getRightVal();
//                            if (usedVal != null) {
//                                Operand first1 = usedVal.getFirst();
//                                Operand second1 = usedVal.getSecond();
//                                if (first1 != null && first1.isTemp()) {
//                                    waitToDelete.add(first1);
//                                }
//                                if (second1 != null && second1.isTemp()) {
//                                    waitToDelete.add(second1);
//                                }
//                            }
//                        }
//                    }
//                    if (!waitToDelete.isEmpty()) {
//                        System.err.println("error in dead code delete " + waitToDelete);
//                    }
//                }
            } else {
                if (leftVal != null && leftVal.isVar()) {
                    activeSet.remove(leftVal);
                }
                if (rightVal != null) {
                    Operand first = rightVal.getFirst();
                    Operand second = rightVal.getSecond();
                    if (first != null && first.isVar()) {
                        activeSet.add(first);
                    }
                    if (second != null && second.isVar()) {
                        activeSet.add(second);
                    }
                }
            }
        }
        ArrayList<IntermediateCode> newCodes = new ArrayList<>();
        for (int i = 0; i < intermediateCodes.size(); i++) {
            if (!removeSet.contains(i)) {
                newCodes.add(intermediateCodes.get(i));
            }
        }
        this.intermediateCodes = newCodes;
        return !removeSet.isEmpty();
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
