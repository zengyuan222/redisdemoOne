package com.hmdp.utils;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 从 RefreshTokenInterceptor 拦截器过来的判断用户是否存在
 * 不存在就拦截，存在就在这处理
 */
public class LoginInterceptor implements HandlerInterceptor {

    // 不能使用 @Autowired @Resource 等交由spring管理的注入，只能使用构造函数
    // 因为LoginInterceptor 是手动new出来
//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;

    /**
     * 不需要 StringRedisTemplate 留给上一个拦截器做处理,刷新处理
     */
//    private StringRedisTemplate stringRedisTemplate;
//
//    public LoginInterceptor(StringRedisTemplate stringRedisTemplate){
//        this.stringRedisTemplate = stringRedisTemplate;
//    }

    // 前置拦截
    // 进入contronller之前进行校验
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1.判断是否需要拦截（ThreadLocal中是否有用户）
        if(UserHolder.getUser() == null){
            // 没有，需要拦截，设置状态码
            response.setStatus(401);
            // 拦截
            return true;
        }
        // 有放行
        return true;
    }


}
