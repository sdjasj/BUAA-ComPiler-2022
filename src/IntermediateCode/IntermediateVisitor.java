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

    public boolean arrayIsConst(String name) {
        for (GlobalDecl globalDecl : globalDecls) {
            if (globalDecl instanceof GlobalArrayDecl && globalDecl.isConst &&
                globalDecl.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public int getValOfConstGlobalArray(String name, int offset) {
        for (GlobalDecl globalDecl : globalDecls) {
            if (globalDecl instanceof GlobalArrayDecl && globalDecl.isConst &&
                globalDecl.name.equals(name)) {
                return ((GlobalArrayDecl) globalDecl).getInitVal().get(offset / 4);
            }
        }
        return Integer.MAX_VALUE;
    }

    public void output() {
        for (GlobalDecl globalDecl : globalDecls) {
            globalDecl.output();
        }
        for (Function function : functions) {
            function.output();
        }
    }

    public void setCallFunction() {
        curFunction.setCallOtherFunc(true);
    }

    public IntermediateCode getCurIntermediateCode() {
        return intermediateCodes.get(intermediateCodes.size() - 1);
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
            while (true) {
//                System.err.println(function.getIntermediateCodes().size());
                function.buildBasicBlocks();
//                System.err.println(function.getIntermediateCodes().size());
//                System.err.println(function.getIntermediateCodes().size());
                function.basicBlockOptimize();
                function.setIntermediateCodes(function.getFlowGraph().getInterMediateCodes());
                if (!function.jumpOptimizer()) {
                    break;
                }
            }
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

        mipsVisitor.generateGlobal();

        //添加代码
        for (Function function : functions) {
//            function.resize();
            function.toMips(mipsVisitor);
        }
    }
}
