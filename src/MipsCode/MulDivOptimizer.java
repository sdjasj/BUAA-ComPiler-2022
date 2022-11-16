package MipsCode;

public class MulDivOptimizer {
    //乘法两个立即数相乘，优化哪边的
    //先位移再加法 x*9
    //先位移再减法 x*7
    //两次位移 x*24 $t1 <- sll $t1, $t1, 3    $t2 <- sll $t2, $t2, 4  addu res, $t1, $t2
}
