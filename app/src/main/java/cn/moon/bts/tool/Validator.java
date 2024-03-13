package cn.moon.bts.tool;

import java.util.ArrayList;
import java.util.List;

import cn.moon.bts.tool.PinyinTool;
import cn.moon.bts.tool.StrTool;

public class Validator {

    private List<String> lines = new ArrayList<>();


    private int lineIndex = 0;



    public synchronized boolean validate(String input) {
        String currentLine = getCurrentLine();

        if (input.isEmpty()  || currentLine == null) {
            return true;
        }

        input = StrTool.removeNonChineseCharacters(input);
        currentLine = StrTool.removeNonChineseCharacters(currentLine);
        boolean match = PinyinTool.match(input, currentLine);


        System.out.println("比较" + input + ", " + currentLine + " 结果：" + match);
        if (match) {
            lineIndex++;
            return true;
        }
        return false;

    }

    public List<String> getLines() {
        return lines;
    }


    public int getLineIndex() {
        return lineIndex;
    }

    public String getCurrentLine() {
        if (lineIndex + 1 > lines.size()) {
            return null;
        }
        return lines.get(lineIndex);
    }

    public List<String> getSuccessLines() {
        return lines.subList(0, lineIndex);
    }

}
