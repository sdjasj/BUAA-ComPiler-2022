package IntermediateCode;

import MipsCode.MipsCode.MipsCode;
import MipsCode.MipsVisitor;
import MipsCode.RegisterPool;
import MipsCode.VarAddressOffset;

import java.util.ArrayList;
import java.util.HashMap;

public class IntermediateVisitor {
    private ArrayList<GlobalDecl> globalDecls;
    private ArrayList<IntermediateCode> intermediateCodes;
    private ArrayList<Function> functions;
    private Function curFunction;
    private HashMap<String, VarAddressOffset> functionMapVarAddressOffset;
    private RegisterPool registerPool = new RegisterPool();

    public IntermediateVisitor() {
        this.globalDecls = new ArrayList<>();
        this.intermediateCodes = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.functionMapVarAddressOffset = new HashMap<>();
    }

    public void addGlobalDecl(GlobalDecl globalDecl) {
        globalDecls.add(globalDecl);
    }

    public void output() {
        for (GlobalDecl globalDecl : globalDecls) {
            globalDecl.output();
        }
        for (Function function : functions) {
            function.output();
        }
    }

    public void addIntermediateCode(IntermediateCode intermediateCode) {
        this.intermediateCodes.add(intermediateCode);
        curFunction.addIntermediateCode(intermediateCode);
    }

    public void changeNewFunction(boolean isMain, String name) {
        Function function = new Function(name);
        function.setMain(isMain);
        functions.add(function);
        curFunction = function;
    }

    public void optimize() {
        for (Function function : functions) {
            function.buildBasicBlocks();
            function.basicBlockOptimize();
            function.colorAllocate();
        }
    }

    public void testPrint() {
        for (Function function : functions) {
            System.err.println("\n\n" + function.getName());
            function.testPrint();
        }
    }

    public boolean isMainFuncNow() {
        return curFunction.isMain();
    }

    public void IntermediateToMips(MipsVisitor mipsVisitor) {
        for (GlobalDecl globalDecl : globalDecls) {
            if (globalDecl instanceof GlobalVarDecl) {
                mipsVisitor.addGlobalVarInits(globalDecl.name,
                    ((GlobalVarDecl) globalDecl).toMips());
            } else if (globalDecl instanceof GlobalArrayDecl) {
                mipsVisitor.addGlobalArrayInit(globalDecl.name,
                    ((GlobalArrayDecl) globalDecl).toMips());
            } else if (globalDecl instanceof GlobalStrDecl) {
                mipsVisitor.addGlobalStrInits(globalDecl.name,
                    ((GlobalStrDecl) globalDecl).toMips());
            }
        }
        //添加代码
        for (Function function : functions) {
            function.toMips(mipsVisitor);
        }
    }
}
