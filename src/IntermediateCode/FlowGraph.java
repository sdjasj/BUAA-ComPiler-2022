package IntermediateCode;

import java.util.ArrayList;

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

    public void buildBasicBlocks(ArrayList<IntermediateCode> intermediateCodes) {
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
        while (i < intermediateCodes.size()) {
            if (intermediateCodes.get(i).isBasicBlockBegin) {
                ArrayList<IntermediateCode> basicBlocksCodes = new ArrayList<>();
                basicBlocksCodes.add(intermediateCodes.get(i));
                i++;
                while (i < intermediateCodes.size() &&
                    !intermediateCodes.get(i).isBasicBlockBegin) {
                    basicBlocksCodes.add(intermediateCodes.get(i));
                    i++;
                }
                BasicBlock basicBlock = new BasicBlock(basicBlocksCodes);
                basicBlocks.add(basicBlock);
            } else {
                i++;
            }
        }
        basicBlocks.get(0).setBegin(true);
    }

    public ArrayList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }
}
