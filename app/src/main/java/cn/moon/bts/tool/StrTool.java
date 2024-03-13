package cn.moon.bts.tool;

import java.util.ArrayList;
import java.util.List;

public class StrTool {

    public static String removeNonChineseCharacters(String input) {
        return input.replaceAll("[^\\u4E00-\\u9FA5]", "");
    }
    public static List<String> split(String str){
        String[] arr = str.split("[、，。；？！,.;?!\n\\s]");
        List<String> list = new ArrayList<>();
        for (String s : arr) {
            s = s.trim();
            if(!s.isEmpty()){
                list.add(s);
            }
        }
        return list;
    }

}
