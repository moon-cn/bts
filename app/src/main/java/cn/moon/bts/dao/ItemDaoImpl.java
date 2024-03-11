package cn.moon.bts.dao;

import java.util.Arrays;

import cn.moon.bts.domain.Item;

public class ItemDaoImpl implements ItemDao {

    @Override
    public Item random() {
        return new Item("静夜思", "唐 李白","床前明月光\n疑是地上霜\n举头望明月\n低头思故乡");
    }
}
