package MipsCode;

public class GlobalStrInit {
    private String content;
    private String name;

    public GlobalStrInit(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public void output() {
        System.out.println(name + ":   .asciiz " + "\"" + content + "\"");
    }
}
