package MipsCode;

import MipsCode.MipsCode.MipsCode;

import java.util.ArrayList;

public class MipsOptimizer {
    public static void optimizeCalculate(ArrayList<MipsCode> mipsCodes) {
        for (int i = 0; i < mipsCodes.size(); i++) {
            MipsCode mipsCode = mipsCodes.get(i);
            String codeType = mipsCode.getCodeName();
//            if (codeType.equals("addu") || codeType.equals("subu") || codeType.equals("addiu") ||
//                codeType.equals("subiu")) {
//                int j = i + 1;
//                while (mipsCodes.get(j).getCodeName().equals("comment")) {
//                    j++;
//                }
//                if (j < mipsCodes.size() && (
//                    mipsCodes.get(j).getCodeName().equals("addu") ||
//                        mipsCodes.get(j).getCodeName().equals("subu") ||
//                        mipsCodes.get(j).getCodeName().equals("addou") ||
//                        mipsCodes.get(j).getCodeName().equals("subiu")
//                    )) {
//                    MipsCode mipsCode1 = mipsCodes.get(j);
//                    if (mipsCode1.getTarget().equals(mipsCode.getTarget())) {
//                        mipsCodes.remove(i);
//                        i--;
//                    }
//                }
//                continue;
//            }
            //bug
//            if (codeType.equals("li")) {
//                int j = i + 1;
//                while (mipsCodes.get(j).getCodeName().equals("comment")) {
//                    j++;
//                }
//                if (j < mipsCodes.size() && mipsCodes.get(j).getCodeName().equals("move")) {
//                    MipsCode mipsCode1 = mipsCodes.get(j);
//                    if (mipsCode1.getSource1().equals(mipsCode.getTarget())) {
//                        mipsCode.setTarget(mipsCode1.getTarget());
//                        mipsCodes.remove(j);
//                    }
//                }
//                continue;
//            }
            if (codeType.equals("move")) {
                if (mipsCode.getTarget().equals(mipsCode.getSource1())) {
                    mipsCodes.remove(i);
                    i--;
                    continue;
                }
                int j = i + 1;
                while (mipsCodes.get(j).getCodeName().equals("comment")) {
                    j++;
                }
                if (j < mipsCodes.size() && mipsCodes.get(j).getCodeName().equals("move")) {
                    MipsCode mipsCode1 = mipsCodes.get(j);
                    if (mipsCode1.getSource1().equals(mipsCode.getTarget()) &&
                        mipsCode1.getTarget().equals(mipsCode.getSource1())) {
                        mipsCode.setTarget(mipsCode1.getTarget());
                        mipsCodes.remove(j);
                        continue;
                    }
                }
            }
            if (codeType.equals("lw")) {
                int j = i + 1;
                while (mipsCodes.get(j).getCodeName().equals("comment")) {
                    j++;
                }
                if (j < mipsCodes.size() && mipsCodes.get(j).getCodeName().equals("sw")) {
                    MipsCode mipsCode1 = mipsCodes.get(j);
                    if (mipsCode.getTarget().equals(mipsCode1.getTarget()) &&
                        mipsCode.getSource1().equals(mipsCode1.getSource1()) &&
                        mipsCode.getSource2().equals(mipsCode1.getSource2())) {
                        mipsCodes.remove(j);
                        continue;
                    }
                }
            }
//            if (codeType.equals("move")) {
//                int j = i + 1;
//                while (mipsCodes.get(j).getCodeName().equals("comment")) {
//                    j++;
//                }
//                if (j < mipsCodes.size() && mipsCodes.get(j).getCodeName().equals("move")) {
//                    MipsCode mipsCode1 = mipsCodes.get(j);
//                    if (mipsCode.getSource1().equals("$v0") && mipsCode1.getSource1().equals(mipsCode.getTarget())) {
//                        mipsCode.setTarget(mipsCode1.getTarget());
//                        mipsCodes.remove(j);
//                    }
//                }
//            }
        }
    }
}
