package cn.moon.bts.tool;

import android.text.TextUtils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinyinTool {


    public static boolean match(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        char[] arr1 = a.toCharArray();
        char[] arr2 = b.toCharArray();

        for (int i = 0; i < arr1.length; i++) {
            if (!match(arr1[i], arr2[i])) {
                return false;
            }
        }

        return true;
    }

    public static boolean match(char a, char b) {
        try {
            HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
            format.setCaseType(HanyuPinyinCaseType.LOWERCASE);//设置为小写字母
            format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);//用音调符号表示
            String[] arr1 = PinyinHelper.toHanyuPinyinStringArray(a, format);
            String[] arr2 = PinyinHelper.toHanyuPinyinStringArray(b, format);

            for (String str1 : arr1) {
                for (String str2 : arr2) {
                    if (str1.equals(str2)) {
                        return true;
                    }

                }
            }


        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();// empty
        }

        return false;
    }

    public static void main(String[] args) {
        PinyinTool manager = new PinyinTool();
        manager.match('床', '窗');
        System.out.printf("11");
    }
}
