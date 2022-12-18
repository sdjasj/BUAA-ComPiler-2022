package Tool;

import IntermediateCode.IntermediateVisitor;

public class Optimizer {
    public static boolean ConstOptimizer = true;
    public static boolean PeepholesOptimizer = true;
    public static boolean BranchOptimizer = true;
    public static boolean Mark = false;
    public static boolean OutputOptimizer = true;
    public static boolean RaOptimizer = true;
    public static boolean MulOptimizer = true;
    public static boolean DivModOptimizer = true;
    public static IntermediateVisitor intermediateVisitor;
}
