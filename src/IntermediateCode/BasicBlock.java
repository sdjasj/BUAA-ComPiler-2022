package IntermediateCode;

import java.util.ArrayList;
import java.util.HashSet;
import IntermediateCode.BasicBlock;

public class BasicBlock {
    private HashSet<String> tags;
    private HashSet<BasicBlock> successor;
    private HashSet<BasicBlock> precursor;
    private ArrayList<IntermediateCode> intermediateCodes;

    public BasicBlock(ArrayList<IntermediateCode> intermediateCodes) {
        this.intermediateCodes = intermediateCodes;
        this.tags = new HashSet<>();
        this.successor = new HashSet<>();
        this.precursor = new HashSet<>();
    }

    public void addSuccessor(BasicBlock basicBlock) {
        successor.add(basicBlock);
    }

    public void addPrecursor(BasicBlock basicBlock) {
        precursor.add(basicBlock);
    }


}
