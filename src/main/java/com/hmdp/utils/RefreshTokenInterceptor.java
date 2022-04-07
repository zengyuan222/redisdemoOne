package com.hmdp.utils;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 拦截一切路径
 * 1.获取token
 * 2.查询Redis用户
 * 3.保存到ThreadLocal
 * 4.刷新token有效期
 * 5.放行
 *
 * 先放行，在下一个拦截器做处理
 */
public class RefreshTokenInterceptor implements HandlerInterceptor {

    // 不能使用 @Autowired @Resource 等交由spring管理的注入，只能使用构造函数
    // 因为LoginInterceptor 是手动new出来
//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;

    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 前置拦截
    // 进入contronller之前进行校验
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //  1.获取请求头中的token
        String token = request.getHeader("authorization");
        if(StrUtil.isBlank(token)){
            // .如果不存在直接放行
            return true;
        }
        //   2.基于token获取redis中的用户
        String key = RedisConstants.LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        // 3.判断用户是否存在
        if(userMap.isEmpty()){
            // 4.不存在直接放行
            return true;
        }
        //   5.将查询到的Hash数据转为UserDTO对象
        // 用一个map来填充bean
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        //   6.存在，保存用户信息到 ThreadLocal
        UserHolder.saveUser(userDTO);
        //   7.刷新token有效期
        stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 8.放行
        return true;
    }

    // 渲染之后返回给我们执行
    // 业务执行完毕，销毁用户信息，避免内存泄露
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //  移除用户
        UserHolder.removeUser();
    }
}
