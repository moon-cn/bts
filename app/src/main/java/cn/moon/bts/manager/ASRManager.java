package cn.moon.bts.manager;

public class ASRManager {


    public boolean match(String input, String target) {


        return true;
    }


    private boolean match(char input, char target) {
        return true;
    }

    public static void main(String[] args) {
        ASRManager manager = new ASRManager();
        manager.match('床','窗');
        System.out.printf("11");
    }
}
