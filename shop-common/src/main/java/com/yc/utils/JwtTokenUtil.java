package com.yc.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenUtil {

    private String key = "itcastitcastitcastitcastitcastitcastitcastitcastitcastitcastitcastitcastitcastitcastitcastitcastitcastitcast";

    public String encodeJWT(Map payloadMap){
        //1.定义header部分内容
        Map headerMap = new HashMap();
        headerMap.put("alg", SignatureAlgorithm.HS256.getValue());
        headerMap.put("typ","JWT");

        //3.生成token
        String jwtToken = Jwts.builder()
                .setHeaderParams(headerMap)
                .setClaims(payloadMap)
                .signWith(SignatureAlgorithm.HS256,key)
                //过期时间一天
                .setExpiration(new Date(System.currentTimeMillis()+1*60*60*24*1000))
                .compact();
        return jwtToken;
    }

    public Claims decodeJWTWithKey(String token){
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody();
            return claims;
        }catch (Exception e){
            e.printStackTrace();
            log.error("令牌过期");
        }
        return null;
    }



    //其中key是密钥，一般保存在配置文件中
    //jsonwebtoken的jar包规定,key必须字节数大于等于你所要用的加密算法的最小字节数
    //这里使用HS256，最小key长度要256
    public static String encodeJWT(String key){
        //1.电影以header部分内容
        Map headerMap = new HashMap();
        headerMap.put("alg", SignatureAlgorithm.HS256.getValue());
        headerMap.put("typ","JWT");

        //2.定义payload部分内容
        Map payloadMap = new HashMap();
        payloadMap.put("sub","测试jwt生成token");
        payloadMap.put("iat", UUID.randomUUID());
        payloadMap.put("exp",1);//过期时间
        payloadMap.put("name","yc");
        payloadMap.put("role","admin");

        //3.生成token
        String jwtToken = Jwts.builder()
                .setHeaderParams(headerMap)
                .setClaims(payloadMap)
                .signWith(SignatureAlgorithm.HS256,key)
                .setExpiration(new Date(System.currentTimeMillis()+1*60*60*24*1000))
                .compact();

        return jwtToken;
    }


    public static void decodeJWT(String jwtToken,String key){
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(jwtToken)
                    .getBody();
            Object sub = claims.get("sub");
            Object name = claims.get("name");
            Object role = claims.get("role");
            Object exp = claims.get("exp");
            Object iat = claims.get("iat");

            System.out.println("sub:"+sub+"\t"+"name:"+name+"\t"+"role:"+role+"\t"+"exp:"+exp+"\t"+"iat:"+iat);
        }catch (Exception e){
            e.printStackTrace();
            log.error("令牌过期");
        }
    }
}
