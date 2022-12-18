package IntermediateCode;

import java.util.ArrayList;
import java.util.HashSet;

import IntermediateCode.AllCode.BranchCode;
import IntermediateCode.AllCode.JumpCode;
import IntermediateCode.AllCode.LabelCode;
import IntermediateCode.FunctionCode.ExitCode;
import IntermediateCode.FunctionCode.FunctionReturnCode;


public class FlowGraph {
    private ArrayList<BasicBlock> basicBlocks;

    public FlowGraph() {
        this.basicBlocks = new ArrayList<>();
    }

    public ArrayList<IntermediateCode> getInterMediateCodes() {
        ArrayList<IntermediateCode> intermediateCodes = new ArrayList<>();
        basicBlocks.forEach((a) -> intermediateCodes.addAll(a.getIntermediateCodes()));
        return intermediateCodes;
    }

    public void buildBasicBlocks(ArrayList<IntermediateCode> intermediateCodes, Function function) {
        intermediateCodes.forEach((a) -> a.setBasicBlockBegin(false));
        this.basicBlocks = new ArrayList<>();
        intermediateCodes.get(0).setBasicBlockBegin(true);
        intermediateCodes.get(intermediateCodes.size() - 1).setBasicBlockBegin(true);
        //基本块开头
        for (int i = 0; i < intermediateCodes.size(); i++) {
            IntermediateCode intermediateCode = intermediateCodes.get(i);
            if (intermediateCode instanceof BranchCode || intermediateCode instanceof JumpCode) {
                String tag = intermediateCode.target.getName();
                //跳转语句跳转到的语句
                int j = 0;
                for (; j < intermediateCodes.size(); j++) {
                    if (intermediateCodes.get(j) instanceof LabelCode &&
                        ((LabelCode) intermediateCodes.get(j)).getLabel().equals(tag)) {
                        while (j >= 0 && intermediateCodes.get(j) instanceof LabelCode) {
                            j--;
                        }
                        break;
                    }
                }
//                for (int k = 0; k < intermediateCodes.size(); k++) {
//                    intermediateCodes.get(k).output();
//                }
//                intermediateCode.output();
//                System.err.println(tag);
                intermediateCodes.get(j + 1).setBasicBlockBegin(true);
                int k = i + 1;
                //跳转下一条语句
//                while (k < intermediateCodes.size() &&
//                    intermediateCodes.get(k) instanceof LabelCode) {
//                    k++;
//                }
                    intermediateCodes.get(k).setBasicBlockBegin(true);

            } else if (intermediateCode instanceof FunctionReturnCode ||
                intermediateCode instanceof ExitCode) {
                intermediateCodes.get(i + 1).setBasicBlockBegin(true);
            }
        }

        int i = 0;
        int blockCnt = 0;
        BasicBlock basicBlock = new BasicBlock(blockCnt++, function);
        while (i < intermediateCodes.size()) {
            if (intermediateCodes.get(i).isBasicBlockBegin) {
                if (intermediateCodes.get(i) instanceof LabelCode) {
                    basicBlock.addTag(((LabelCode) intermediateCodes.get(i)).getLabel());
                }
                basicBlock.addIntermediateCode(intermediateCodes.get(i));
                i++;
                while (i < intermediateCodes.size() &&
                    !intermediateCodes.get(i).isBasicBlockBegin) {
                    if (intermediateCodes.get(i) instanceof LabelCode) {
                        basicBlock.addTag(((LabelCode) intermediateCodes.get(i)).getLabel());
                    }
                    basicBlock.addIntermediateCode(intermediateCodes.get(i));
                    i++;
                }
                basicBlocks.add(basicBlock);
                basicBlock = new BasicBlock(blockCnt++, function);
            } else {
                i++;
            }
        }
        basicBlocks.get(0).setBegin(true);
        buildFlowGraph();
    }

    public void buildFlowGraph() {
        for (int i = 0; i < basicBlocks.size(); i++) {
            IntermediateCode lastCode = basicBlocks.get(i).getLastCode();
            if (lastCode instanceof BranchCode) {
                //条件跳转
                String label = lastCode.target.getName();
                for (int j = 0; j < basicBlocks.size(); j++) {
                    if (basicBlocks.get(j).containTag(label)) {
                        basicBlocks.get(i).addSuccessor(basicBlocks.get(j));
                        basicBlocks.get(j).addPrecursor(basicBlocks.get(i));
                        if (i < basicBlocks.size() - 1) {
                            basicBlocks.get(i).addSuccessor(basicBlocks.get(i + 1));
                            basicBlocks.get(i + 1).addPrecursor(basicBlocks.get(i));
                        }
                    }
                }
            } else if (lastCode instanceof JumpCode) {
                String label = lastCode.target.getName();
                for (int j = 0; j < basicBlocks.size(); j++) {
                    if (basicBlocks.get(j).containTag(label)) {
                        basicBlocks.get(i).addSuccessor(basicBlocks.get(j));
                        basicBlocks.get(j).addPrecursor(basicBlocks.get(i));
                    }
                }
            } else {
                if (i < basicBlocks.size() - 1) {
                    basicBlocks.get(i).addSuccessor(basicBlocks.get(i + 1));
                    basicBlocks.get(i + 1).addPrecursor(basicBlocks.get(i));
                }
            }
        }
        removeUnReachedBlocks(basicBlocks);
    }

    public void removeUnReachedBlocks(ArrayList<BasicBlock> basicBlocks) {
        BasicBlock beginBlock = basicBlocks.get(0);
        HashSet<BasicBlock> reachBlocks = new HashSet<>();
        dfsForReachedBlock(beginBlock, reachBlocks);
        ArrayList<BasicBlock> newBlocks = new ArrayList<>();
        for (int i = 0; i < basicBlocks.size(); i++) {
            if (reachBlocks.contains(basicBlocks.get(i))) {
                newBlocks.add(basicBlocks.get(i));
            }
        }
        this.basicBlocks = newBlocks;
    }

    public void dfsForReachedBlock(BasicBlock basicBlock, HashSet<BasicBlock> reachBlocks) {
        if (reachBlocks.contains(basicBlock)) {
            return;
        }
        reachBlocks.add(basicBlock);
        HashSet<BasicBlock> nextBlocks = basicBlock.getSuccessor();
        nextBlocks.forEach((basicBlock1 -> dfsForReachedBlock(basicBlock1, reachBlocks)));
    }

    public ArrayList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public void testPrint() {
        int cnt = 0;
        for (BasicBlock basicBlock : basicBlocks) {
            System.err.println(cnt++);
            basicBlock.testPrint();
        }
    }
}
