package com.atguigu.gmall0218.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0218.bean.CartInfo;
import com.atguigu.gmall0218.bean.SkuInfo;
import com.atguigu.gmall0218.cart.Mapper.CartInfoMapper;
import com.atguigu.gmall0218.cart.constant.CartConst;
import com.atguigu.gmall0218.config.RedisUtil;
import com.atguigu.gmall0218.service.CartService;
import com.atguigu.gmall0218.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Reference
    private ManageService manageService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void addToCart(String skuId, int skuNum, String userId) {
        //1.查询需要添加的数据是否存在于数据库
        //2.存在的话，商品数量相加，不存在则直接添加到数据库中
        //3.将数据添加至缓存
        //查询数据库中是否存在当前商品
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        cartInfo.setSkuId(skuId);
        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfo);
        if (cartInfoExist != null){
            //表示数据库中存在当前商品，数量相加
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);
            //给skuPrice初始化，值为cartPrice
            cartInfoExist.setCartPrice(cartInfoExist.getSkuPrice());
            //更新数据库
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
        }else {
            //数据库中没有相同的商品
            //商品信息来自于商品详情页，即来自于skuInfo表中，可以根据skuId对skuInfo进行查询
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            //创建cartInfo对象，将skuInfo对象属性值赋值给cartInfo
            CartInfo cartInfoNew = new CartInfo();
            // 属性赋值
            cartInfoNew.setSkuId(skuId);
            cartInfoNew.setCartPrice(skuInfo.getPrice());
            cartInfoNew.setSkuPrice(skuInfo.getPrice());
            cartInfoNew.setSkuName(skuInfo.getSkuName());
            cartInfoNew.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfoNew.setUserId(userId);
            cartInfoNew.setSkuNum(skuNum);
            //将cartInfoNew信息添加至数据库
            cartInfoMapper.insertSelective(cartInfoNew);
        }
        //将购物车数据放入缓存
        //构造数据的key
        String userCartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        //获取jedis连接
        Jedis jedis = redisUtil.getJedis();
        //将需要储存的数据序列化
        String cartJson = JSON.toJSONString(cartInfoExist);
        //使用hashset存储数据
        jedis.hset(userCartKey, userId, cartJson);
        //更新购物车的过期时间
        String userInfoKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USERINFOKEY_SUFFIX;
        //取出当前用户的过期时间
        Long ttl = jedis.ttl(userInfoKey);
        jedis.expire(userCartKey, ttl.intValue());
        jedis.close();
    }

    @Override
    public List<CartInfo> getCartList(String userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        //从缓存redis中取数据，取出的数据要序列化，如果缓存中没有数据，要从数据库中取数据，并且要存入缓存中，设定过期时间
        Jedis jedis = redisUtil.getJedis();

        //拼接key
        String userCartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        List<String> cartJsons = jedis.hvals(userCartKey);
        if (cartJsons != null && cartJsons.size() > 0){
            //redis中取到数据
//            ArrayList<CartInfo> cartInfoList = new ArrayList<>();
            for (String cartJson : cartJsons) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
            //排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getId().compareTo(o2.getId());
                }
            });
            return cartInfoList;
        }else {
            //reids中未取到数据，需要从数据库中取数据
            cartInfoList = loadCartCache(userId);
            return cartInfoList;
        }
    }

    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId) {
        //首先从数据库查出当前CartInfo表中所包含的商品信息，然后跟
        //cookie中的购物车信息进行比对，相同则数量相加，不同则将
        // cookie中商品信息存入数据库中，最后合并完毕更新缓存
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);
        for (CartInfo cartInfoCk : cartListFromCookie) {
            //设置匹配标志位
            boolean isMatch = false;
            for (CartInfo cartInfoDB : cartInfoListDB) {
                if (cartInfoCk.getSkuId().equals(cartInfoDB.getSkuId())){
                    cartInfoDB.setSkuNum(cartInfoDB.getSkuNum()+cartInfoCk.getSkuNum());
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                    isMatch = true;
                }
            }
            if (!isMatch){
                //如果数据库中不存在当前的商品，那么就将当前商品添加进购物车中
                cartInfoCk.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCk);
            }
        }
        //将数据库中最新的购物车信息存入缓存中
        List<CartInfo> cartInfoList = loadCartCache(userId);
        //将未登录状态下，用户选中的商品合并到缓存中
        for (CartInfo cartInfo : cartInfoList) {
            for (CartInfo info : cartListFromCookie) {
                if (cartInfo.getSkuId().equals(info.getSkuId())){
                    // 只有被勾选的才会进行更改
                    if (info.getIsChecked().equals("1")){
                        cartInfo.setIsChecked(info.getIsChecked());
                        // 更新redis中的isChecked
                        checkCart(cartInfo.getSkuId(),info.getIsChecked(),userId);
                    }
                }
            }
        }
        return cartInfoList;
    }

    @Override
    public void checkCart(String skuId, String isChecked, String userId) {
        //将勾选的商品从缓存中取出，修改isChecked标志，再存入新生成的购物车中
        //生成新的购物车key
        String userCartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        //获取redis
        Jedis jedis = redisUtil.getJedis();
        String cartJson = jedis.hget(userCartKey, skuId);
        //将json转换为对象
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        //修改isChecked标志位
        cartInfo.setIsChecked(isChecked);
        //将对象转换为json格式存入redis
        String cartCheckedJson = JSON.toJSONString(cartInfo);
        jedis.hset(userCartKey, skuId, cartCheckedJson);

        //将勾选的商品存到新的购物车中
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        if (isChecked.equals("1")){
            //被选中的商品
            jedis.hset(userCheckedKey, skuId, cartCheckedJson);
        }else {
            //未被选中的商品，删除掉
            jedis.hdel(userCheckedKey, skuId);
        }
        jedis.close();
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        //获取被选中商品的key
        String userCheckedkey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        //获取redis
        Jedis jedis = redisUtil.getJedis();
        //获取redis中的被选中的商品
        List<String> cartCheckedList = jedis.hvals(userCheckedkey);
        List<CartInfo> newCartCheckedList = new ArrayList<>();
        for (String s : cartCheckedList) {
            CartInfo cartInfo = JSON.parseObject(s, CartInfo.class);
            newCartCheckedList.add(cartInfo);
        }
        return newCartCheckedList;
    }

    //根据userId到数据库中查询购物车内容，并且要取到实时价格skuPrice，将数据存入缓存中
    private List<CartInfo> loadCartCache(String userId) {
        //如果要查询出实时价格，就需要从cartInfo和skuInfo两张表查询出bean中的cartInfo所需的所有信息
        List<CartInfo> cartInfoList =  cartInfoMapper.selectCartListWithCurPrice(userId);
        //获取jedis
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            //定义key
            String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
            Map<String, String> map = new HashMap<>();
            for (CartInfo cartInfo : cartInfoList) {
                map.put(cartInfo.getSkuId(), JSON.toJSONString(cartInfo));
            }
            //redis一次性存入多条数据
            jedis.hmset(cartKey, map);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null){
                jedis.close();
            }
        }
        return cartInfoList;
    }
}
