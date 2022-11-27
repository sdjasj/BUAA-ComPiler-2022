package MipsCode;

import MipsCode.MipsCode.MipsCode;

import java.util.ArrayList;

public class MipsOptimizer {
    public static void optimizeCalculate(ArrayList<MipsCode> mipsCodes) {
        for (int i = 0; i < mipsCodes.size(); i++) {
            MipsCode mipsCode = mipsCodes.get(i);
            String codeType = mipsCode.getCodeName();
//            if (codeType.equals("addu") || codeType.equals("addiu") || codeType.equals("subu") ||
//                codeType.equals("subiu") || codeType.equals("mul")) {
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
//            }
            if (codeType.equals("li")) {
                int j = i + 1;
                while (mipsCodes.get(j).getCodeName().equals("comment")) {
                    j++;
                }
                if (j < mipsCodes.size() && mipsCodes.get(j).getCodeName().equals("move")) {
                    MipsCode mipsCode1 = mipsCodes.get(j);
                    if (mipsCode1.getSource1().equals(mipsCode.getTarget())) {
                        mipsCode.setTarget(mipsCode1.getTarget());
                        mipsCodes.remove(j);
                    }
                }
            }
            if (codeType.equals("move")) {
                if (mipsCode.getTarget().equals(mipsCode.getSource1())) {
                    mipsCodes.remove(i);
                    i--;
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
