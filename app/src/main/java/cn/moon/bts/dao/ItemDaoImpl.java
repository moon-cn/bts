package cn.moon.bts.dao;

import java.util.Arrays;

import cn.moon.bts.domain.Item;

public class ItemDaoImpl implements ItemDao {

    @Override
    public Item random() {
        return new Item("静夜思", "疑是地上霜,床前明月光,举头望明月,低头思故乡");
    }
}
