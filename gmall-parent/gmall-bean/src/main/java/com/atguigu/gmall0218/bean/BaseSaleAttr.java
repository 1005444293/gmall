package com.atguigu.gmall0218.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@Data
public class BaseSaleAttr implements Serializable {
    @Id
    @Column
    String id ;

    @Column
    String name;
}
