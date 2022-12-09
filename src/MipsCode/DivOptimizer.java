package MipsCode;

import IntermediateCode.IntermediateCode;
import MipsCode.MipsCode.MipsCode;

import java.math.BigInteger;

public class DivOptimizer {
    public static long mHigh;
    public static int l;
    public static int shPost;
    public static final int N = 32;

    public static void CHOOSE_MULTIPLIER(long d) {
        l = 0;
        for (int i = 0; i < 32; i++) {
            if (d > (1L << i)) {
                l = i + 1;
            }
        }
//        System.err.println(l);
        shPost = l;
        long mLow = BigInteger.valueOf(1).shiftLeft(N + l).divide(BigInteger.valueOf(d)).longValue();
        mHigh =  BigInteger.valueOf(1).shiftLeft(N +  l).add(BigInteger.valueOf(1)
            .shiftLeft(1 + l)).divide(BigInteger.valueOf(d)).longValue();
//        System.err.println(mLow);
//        System.err.println(mHigh);
        while ((mLow >> 1) < (mHigh >> 1) && shPost > 0) {
            mLow >>= 1;
            mHigh >>= 1;
            shPost -= 1;
        }
//        System.err.println(l + " " + mHigh + " " + shPost);
    }

    public static void simplifyDiv(String target, String src, int val, MipsVisitor mipsVisitor,
                                   RegisterPool registerPool, VarAddressOffset varAddressOffset,
                                   IntermediateCode intermediateCode) {
        CHOOSE_MULTIPLIER(Math.abs(val));
        if (val == 1) {
            mipsVisitor.addMipsCode(MipsCode.generateMOVE(target, src));
            return;
        } else if (val == -1) {
            mipsVisitor.addMipsCode(MipsCode.generateSUBU(target, "$0", src));
            return;
        }

        if (Math.abs(val) == (1L << l)) {
            mipsVisitor.addMipsCode(MipsCode.generateSRA("$1", src, String.valueOf(l - 1)));
            if (N != l) {
                mipsVisitor.addMipsCode(MipsCode.generateSRL("$1", "$1", String.valueOf(N - l)));
            }
            mipsVisitor.addMipsCode(MipsCode.generateADDU(target, src, "$1"));
            mipsVisitor.addMipsCode(MipsCode.generateSRA(target, target, String.valueOf(l)));
        } else if (mHigh < (1L << (N - 1))) {
            String tempReg =
                registerPool.getTempReg(false, varAddressOffset, mipsVisitor, intermediateCode);
            mipsVisitor.addMipsCode(MipsCode.generateLi("$1", String.valueOf(mHigh)));
            mipsVisitor.addMipsCode(MipsCode.generateMULT(src, "$1"));
            mipsVisitor.addMipsCode(MipsCode.generateMFHI("$1"));
            mipsVisitor.addMipsCode(MipsCode.generateSRA("$1", "$1", String.valueOf(shPost)));
            mipsVisitor.addMipsCode(MipsCode.generateSLT(tempReg, src, "$0"));
            mipsVisitor.addMipsCode(MipsCode.generateADDU(target, "$1", tempReg));
            registerPool.unFreeze(tempReg);
        } else {
            String tempReg =
                registerPool.getTempReg(false, varAddressOffset, mipsVisitor, intermediateCode);
            mipsVisitor.addMipsCode(MipsCode.generateLi("$1", String.valueOf(mHigh - (1L << N))));
            mipsVisitor.addMipsCode(MipsCode.generateMULT(src, "$1"));
            mipsVisitor.addMipsCode(MipsCode.generateMFHI("$1"));
            mipsVisitor.addMipsCode(MipsCode.generateADDU("$1", src, "$1"));
            mipsVisitor.addMipsCode(MipsCode.generateSRA("$1", "$1", String.valueOf(shPost)));
            mipsVisitor.addMipsCode(MipsCode.generateSLT(tempReg, src, "$0"));
            mipsVisitor.addMipsCode(MipsCode.generateADDU(target, "$1", tempReg));
            registerPool.unFreeze(tempReg);
        }

        if (val < 0) {
            mipsVisitor.addMipsCode(MipsCode.generateSUBU(target, "$0", target));
        }
    }

    public static void simplifyMod(String target, String src, int val, MipsVisitor mipsVisitor,
                                   RegisterPool registerPool, VarAddressOffset varAddressOffset,
                                   IntermediateCode intermediateCode) {
        CHOOSE_MULTIPLIER(Math.abs(val));
        if (val == 1 || val == -1) {
            mipsVisitor.addMipsCode(MipsCode.generateMOVE(target, "$0"));
            return;
        }

        if (Math.abs(val) == (1L << l)) {
            mipsVisitor.addMipsCode(MipsCode.generateSRA("$1", src, String.valueOf(l - 1)));
            if (N != l) {
                mipsVisitor.addMipsCode(MipsCode.generateSRL("$1", "$1", String.valueOf(N - l)));
            }
            mipsVisitor.addMipsCode(MipsCode.generateADDU("$1", src, "$1"));
            mipsVisitor.addMipsCode(MipsCode.generateSRA("$1", "$1", String.valueOf(l)));
            if (val < 0) {
                mipsVisitor.addMipsCode(MipsCode.generateSUBU("$1", "$0", "$1"));
            }
            mipsVisitor.addMipsCode(MipsCode.generateSLL("$1", "$1", String.valueOf(l)));
            mipsVisitor.addMipsCode(MipsCode.generateSUBU(target, src, "$1"));

        } else if (mHigh < (1L << (N - 1))) {
            String tempReg =
                registerPool.getTempReg(false, varAddressOffset, mipsVisitor, intermediateCode);
            mipsVisitor.addMipsCode(MipsCode.generateLi("$1", String.valueOf(mHigh)));
            mipsVisitor.addMipsCode(MipsCode.generateMULT(src, "$1"));
            mipsVisitor.addMipsCode(MipsCode.generateMFHI("$1"));
            mipsVisitor.addMipsCode(MipsCode.generateSRA("$1", "$1", String.valueOf(shPost)));
            mipsVisitor.addMipsCode(MipsCode.generateSLT(tempReg, src, "$0"));
            mipsVisitor.addMipsCode(MipsCode.generateADDU(tempReg, "$1", tempReg));
            if (val < 0) {
                mipsVisitor.addMipsCode(MipsCode.generateSUBU(tempReg, "$0", tempReg));
            }
            mipsVisitor.addMipsCode(MipsCode.generateLi("$1", String.valueOf(val)));
            mipsVisitor.addMipsCode(MipsCode.generateMUL(tempReg, tempReg, "$1"));
            mipsVisitor.addMipsCode(MipsCode.generateSUBU(target, src, tempReg));
            registerPool.unFreeze(tempReg);
        } else {
            String tempReg =
                registerPool.getTempReg(false, varAddressOffset, mipsVisitor, intermediateCode);
            mipsVisitor.addMipsCode(MipsCode.generateLi("$1", String.valueOf(mHigh - (1L << N))));
            mipsVisitor.addMipsCode(MipsCode.generateMULT(src, "$1"));
            mipsVisitor.addMipsCode(MipsCode.generateMFHI("$1"));
            mipsVisitor.addMipsCode(MipsCode.generateADDU("$1", src, "$1"));
            mipsVisitor.addMipsCode(MipsCode.generateSRA("$1", "$1", String.valueOf(shPost)));
            mipsVisitor.addMipsCode(MipsCode.generateSLT(tempReg, src, "$0"));
            mipsVisitor.addMipsCode(MipsCode.generateADDU(tempReg, "$1", tempReg));
            if (val < 0) {
                mipsVisitor.addMipsCode(MipsCode.generateSUBU(tempReg, "$0", tempReg));
            }
            mipsVisitor.addMipsCode(MipsCode.generateLi("$1", String.valueOf(val)));
            mipsVisitor.addMipsCode(MipsCode.generateMUL(tempReg, tempReg, "$1"));
            mipsVisitor.addMipsCode(MipsCode.generateSUBU(target, src, tempReg));
            registerPool.unFreeze(tempReg);
        }
    }


}
