package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.CACHE_SHOPTYPE_KEY;
import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public Object queryAllList() {
        List<ShopType> renturnList = new ArrayList<>();
        List<String> saveRedisList = new ArrayList<>();
        String key = CACHE_SHOPTYPE_KEY ;
        // 1.从redis查询商铺缓存
            //String shopJson = stringRedisTemplate.opsForValue().get(key);

        List<String> shopTypeJsonList = stringRedisTemplate.opsForList().range(key, 0, -1);
        // 2.判断是否存在
        if(shopTypeJsonList.size() > 0){
            // 3.存在,直接返回
            //Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            for(String shopType: shopTypeJsonList){
                renturnList.add(JSONUtil.toBean(shopType, ShopType.class));
            }
            return renturnList;
        }
        // 4.不存在，根据id查询数据库
        List<ShopType> strs = list();
        //List<String> strs = (List<String>)(List)list();
        // 5.不存在，返回错误
        if(strs == null){
            return Result.fail("商店类型不存在");
        }
        // 遍历转json
        for(ShopType str : strs){
            //String jsonString = JSONObject.toJSONString(str);
            String jsonString = JSONUtil.toJsonStr(str);
            saveRedisList.add(jsonString);
        }
        // 6.存在，写入redis
        stringRedisTemplate.opsForList().rightPushAll(key, saveRedisList);
        // 7.返回


//        List<ShopType> list = list();
//
//        List<String> strs1 = (List<String>)(List)list();

        return strs;
        //return null;
    }
}
