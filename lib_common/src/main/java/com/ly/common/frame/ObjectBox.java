package com.ly.common.frame;



import com.ly.common.entity.MyObjectBox;

import io.objectbox.BoxStore;

/**
 * 数据库对象box
 *
 * @author ly
 * date 2019/7/27 11:26
 */
public class ObjectBox {
    private static BoxStore boxStore;

    public static BoxStore get() {
        if (boxStore == null) {
            synchronized (ObjectBox.class) {
                if (boxStore == null) {
                    boxStore = MyObjectBox.builder()
                            .androidContext(BaseApp.get().getApplicationContext())
                            .build();
                }
            }
        }
        return boxStore;
    }
}