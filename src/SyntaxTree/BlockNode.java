package SyntaxTree;

import IntermediateCode.IntermediateVisitor;
import MySymbolTable.SymbolTable;
import Tool.Pair;

import java.util.ArrayList;

public class BlockNode extends ParserNode {
    private ArrayList<BlockItemNode> blockItemNodes;

    public BlockNode() {
        this.blockItemNodes = new ArrayList<>();
    }

    public void addBlockItemNode(BlockItemNode blockItemNode) {
        if (blockItemNode != null) {
            blockItemNodes.add(blockItemNode);
        }
    }

    public boolean blockOfIntFuncHasReturnInTheLastStmt() {
        if (blockItemNodes.size() == 0) {
            return false;
        }
        return blockItemNodes.get(blockItemNodes.size() - 1).intFuncHasReturnInTheLastStmt();
    }

    public void generateIntermediate(IntermediateVisitor intermediateVisitor,
                                     Pair<String, String> loop) {
        for (BlockItemNode blockItemNode : blockItemNodes) {
            blockItemNode.generateIntermediate(intermediateVisitor, loop);
        }
    }
}
