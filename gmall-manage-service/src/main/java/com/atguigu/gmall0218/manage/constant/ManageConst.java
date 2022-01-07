package com.atguigu.gmall0218.manage.constant;

//定义项目中key的redis前后缀以及过期时间
public class ManageConst {

    public static final String SKUKEY_PREFIX="sku:";

    public static final String SKUKEY_SUFFIX=":info";

    public static final int SKUKEY_TIMEOUT=24*60*60;

    public static final int SKULOCK_EXPIRE_PX=10000;

    public static final String SKULOCK_SUFFIX=":lock";

}
