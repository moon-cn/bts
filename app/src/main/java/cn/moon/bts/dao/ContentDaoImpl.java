package cn.moon.bts.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.moon.bts.domain.Content;

public class ContentDaoImpl implements ContentDao {

    @Override
    public Content random() {
        String str = "床前明月光\n" +
                "疑是地上霜\n" +
                "举头望明月\n" +
                "低头思故乡";

        String[] arr = str.split("\n");

        Content content = new Content();
        content.setLines(Arrays.asList(arr));
        return content;
    }
}
