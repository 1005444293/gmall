package com.atguigu.gmall0218.bean;

import lombok.Data;

import javax.persistence.Transient;
import java.io.Serializable;
import javax.persistence.*;

@Data
public class BaseAttrValue implements Serializable {
    @Id
    @Column
    private String id;
    @Column
    private String valueName;
    @Column
    private String attrId;

    @Transient
    private String urlParam;

}
