package com.atguigu.gmall0218.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0218.config.RedisUtil;
import com.atguigu.gmall0218.bean.*;
import com.atguigu.gmall0218.manage.constant.ManageConst;
import com.atguigu.gmall0218.manage.mapper.*;
import com.atguigu.gmall0218.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ManageServiceImpl implements ManageService {

    // 调用mapper
    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<BaseCatalog1> getCatalog1() {

        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        // select * from baseCatalog2 where catalog1Id =?
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {

//        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
//        baseAttrInfo.setCatalog3Id(catalog3Id);
//        return baseAttrInfoMapper.select(baseAttrInfo);
        // 制作SKU页面  baseAttrInfo需要携带baseAttrValue的值
        return baseAttrInfoMapper.getAttrInfoListByCatalog3Id(catalog3Id);
    }

    //@Transactional为事务控制
    @Transactional
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //修改数据还是插入数据根据传进来的baseAttrInfo是否包含主键来进行判断
        //如果包含主键就根据主键进行修改数据，否则就保存新的数据
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0){
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        }else {
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }

        //对于baseAttrValue来说，无论baseAttrInfo是修改还是插入新数据，都可以按照
        //先删除原有数据再新增新数据的逻辑进行操作
        //先删除数据
        BaseAttrValue baseAttrValueDel = new BaseAttrValue();
        baseAttrValueDel.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValueDel);
        //再新增数据
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (attrValueList != null && attrValueList.size() > 0){
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(baseAttrValue);
            }
        }

    }

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        //查询属性，创建属性对象
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        //创建属性值对象
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        //根据attrId查询平台属性值，
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);
        //将平台属性值放到平台属性的attrValueList属性中
        baseAttrInfo.setAttrValueList(baseAttrValueList);
        return baseAttrInfo;
    }

    @Override
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {

        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {

        return baseSaleAttrMapper.selectAll();
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {
        // 保存数据
        //        spuInfo
        //        spuImage
        //        spuSaleAttr
        //        spuSaleAttrValue

        spuInfoMapper.insertSelective(spuInfo);

        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList!=null && spuImageList.size()>0){
            for (SpuImage spuImage : spuImageList) {
                // 设置spuId
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);
            }
        }


        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList!=null && spuSaleAttrList.size()>0){
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(spuSaleAttr);

                // spuSaleAttrValue
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList!=null && spuSaleAttrValueList.size()>0){
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                    }
                }
            }
        }

    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {

        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {
        //保存SpuInfo数据
        //SkuInfo
        skuInfoMapper.insertSelective(skuInfo);
        //SkuImage
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList != null && skuImageList.size() > 0){
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(skuImage);
            }
        }
        //SkuAttrValue
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList != null && skuAttrValueList.size() > 0){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }
        //SkuSaleAttrValue
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList != null && skuSaleAttrValueList.size() > 0){
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
        }
    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {
//        testJedis(skuId);
        return getSkuInfoRedission(skuId);
//        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoRedission(String skuId){
        Jedis jedis = null;
        SkuInfo skuInfo = null;
        RLock lock = null;
        try {
            //创建redission的配置对象config
            Config config = new Config();
            //本项目使用单节点redis，故使用.useSingleServer().setAddress("redis://192.168.61.128:6379");
            //如果使用的是集群的redis，应该使用.useClusterServers().addNodeAddress("redis://192.168.61.128:6379");
            config.useSingleServer().setAddress("redis://192.168.61.128:6379");
            //创建redission锁的api
            RedissonClient redissonClient = Redisson.create(config);
            //获取redission的锁，来自java.util.concurrent.locks.lock包
            lock = redissonClient.getLock("myLock");
//            -------------------------------------------------
            //加锁 10表示加锁的时间，后边是单位seconds
            lock.lock(10, TimeUnit.SECONDS);
            //以下是业务代码
            //获取jedis对象
            jedis = redisUtil.getJedis();
            //定义key
            String skuKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKUKEY_SUFFIX;
            //从redis中获取skuKey对应的值
            String skukeyValue = jedis.get(skuKey);
            //判断skukeyValue是否为空
            if (skukeyValue == null || skukeyValue.length() == 0){
                System.out.println("缓存中没有值，从数据库中取值");
                //表明redis中没有skuKey对应的值，需要从数据库中取值
                skuInfo = getSkuInfoDB(skuId);
                //将skuInfo存入redis中
                jedis.setex(skuKey, ManageConst.SKUKEY_TIMEOUT, JSON.toJSONString(skuInfo));
                return skuInfo;
            }else {
                //redis中存在skuId相应的值
                String skuJson = jedis.get(skuId);
                //将skuJson转换成对象返回给控制层
                skuInfo = JSON.parseObject(skuJson, skuInfo.getClass());
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //关闭jedis
            if (jedis != null){
                jedis.close();
            }
            //关闭lock
            if (lock != null){
                lock.unlock();
            }
        }
        //如果上面的都不符合条件，就是redis宕机了，直接到数据库中查询相应的数据
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoJedis(String skuId) {
        Jedis jedis = null;
        SkuInfo skuInfo = null;
        try {
            //获取jedis
            jedis = redisUtil.getJedis();
            //定义Key sku:skuId:info
            String skuKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKUKEY_SUFFIX;
            //从redis中获取数据
            String skuJson = jedis.get(skuKey);
            //判断skuJson是否有数据，如果没有数据，需要加锁，从数据库mysql中获取数据然后放入redis中
            if (skuJson == null || skuJson.length() == 0){
                //redis中未查到相关数据，尝试加锁，避免缓存击穿
                System.out.println("缓存中没有数据，尝试使用set命令加锁");
                String skuLockKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKULOCK_SUFFIX;
                String lockKey = jedis.set(skuLockKey, "good", "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);
                if (lockKey.equals("OK")){
                    //进入此条判断语句，证明加锁成功
                    //从数据库中查询相应的数据
                    skuInfo = getSkuInfoDB(skuId);
                    //将所查询数据由对象转换成字符串，然后放入缓存
                    String skuRedisStr = JSON.toJSONString(skuInfo);
                    jedis.setex(skuKey, ManageConst.SKUKEY_TIMEOUT, skuRedisStr);
                    //锁使用完毕，删除锁
                    jedis.del(skuLockKey);
                    return skuInfo;
                }else {
                    //else中，就是指未拿到锁的线程，进行等待
                    Thread.sleep(1000);
                    //调用getSkuInfo()
                    return getSkuInfo(skuId);
                }
            }else {
                //进入这个else表示redis中有数据，将数据转换格式，传递给前端页面即可
                skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
                return skuInfo;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (jedis != null){
                jedis.close();
            }
        }
        return getSkuInfoDB(skuId);
    }

    //测试redis
    private void testJedis(String skuId) {
        Jedis jedis = redisUtil.getJedis();
        jedis.set("ok", "good");
        jedis.close();
        SkuInfo skuInfo = getSkuInfoDB(skuId);
    }

    //从数据库得到skuInfo
    private SkuInfo getSkuInfoDB(String skuId) {
        //根据skuId得到skuInfo
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        //根据skuId得到skuImage
        List<SkuImage> skuImageList = getSkuImageList(skuId);
        //将skuImage赋值给skuInfo
        skuInfo.setSkuImageList(skuImageList);
        //查询skuAttrValue
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        //将skuAttrValue放入skuInfo中一并返回
        skuInfo.setSkuAttrValueList(skuAttrValueList);
        return skuInfo;
    }

    //得到skuImage的集合
    private List<SkuImage> getSkuImageList(String skuId) {
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        return skuImageMapper.select(skuImage);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {

        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
        return skuSaleAttrValueList;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {
        //将attrValueIdList中包含的数据格式化为sql语句可用的形式
        String attrValueIds = StringUtils.join(attrValueIdList.toArray(), ",");
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectAttrInfoListByIds(attrValueIds);
        return baseAttrInfoList;
    }
}
