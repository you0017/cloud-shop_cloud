package com.yc.interceptor;

import com.yc.context.BaseContext;
import com.yc.utils.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String userId = request.getHeader("userId");
        if (userId!=null&&userId.length()>0&&!userId.equals("")){
            //说明是通过openfeign调用，传了userId
            //直接放行
            BaseContext.setCurrentId(userId);
            return true;
        }

        String token = request.getHeader("token");
        if (token != null && jwtTokenUtil.decodeJWTWithKey(token)!=null){
            //token可用
            try {
                //取出token中的用户信息
                Claims claims = jwtTokenUtil.decodeJWTWithKey(token);
                BaseContext.setCurrentId(claims.get("id").toString());
                return true;
            }catch (Exception e){
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView){
        //防止内存泄漏
        BaseContext.removeCurrentId();
    }
}
