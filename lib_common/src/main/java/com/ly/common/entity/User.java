package com.ly.common.entity;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by ly on 2021/3/23 15:05
 */
@Entity
public class User {
    @Id
    public Long id;
}
