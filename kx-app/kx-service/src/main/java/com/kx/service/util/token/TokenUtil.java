package com.kx.service.util.token;

import com.google.gson.Gson;
import com.kx.common.enums.SecurityEnum;
import com.kx.common.enums.UserEnums;
import com.kx.common.utils.CachePrefix;
import com.kx.common.utils.RedisOperator;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * TokenUtil
 *
 * @author Chopper
 * @version v1.0
 * 2020-11-12 18:44
 */
@Component
public class TokenUtil {
    @Autowired
    private JWTTokenProperties tokenProperties;

    @Autowired
    private RedisOperator redisOperator;

    /**
     * 构建token
     *
     * @param username  主体
     * @param claim     私有声明
     * @param longTerm  长时间特殊token 如：移动端，微信小程序等
     * @param userEnums 用户枚举
     * @return TOKEN
     */
    public Token createToken(String username, Object claim, boolean longTerm, UserEnums userEnums) {
        Token token = new Token();
        //访问token
        String accessToken = createToken(username, claim, tokenProperties.getTokenExpireTime());

        redisOperator.set(CachePrefix.ACCESS_TOKEN.getPrefix(userEnums) + accessToken, "1",
                tokenProperties.getTokenExpireTime() * 60);
        //刷新token生成策略：如果是长时间有效的token（用于app），则默认15天有效期刷新token。如果是普通用户登录，则刷新token为普通token2倍数
        Long expireTime = longTerm ? 15 * 24 * 60L : tokenProperties.getTokenExpireTime() * 2;
        String refreshToken = createToken(username, claim, expireTime);

        redisOperator.set(CachePrefix.REFRESH_TOKEN.getPrefix(userEnums) + refreshToken, "1", expireTime * 60);

        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        return token;
    }

    /**
     * 刷新token
     *
     * @param oldRefreshToken 刷新token
     * @param userEnums       用户枚举
     * @return token
     */
    public Token refreshToken(String oldRefreshToken, UserEnums userEnums) {

        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(SecretKeyUtil.generalKeyByDecoders())
                    .parseClaimsJws(oldRefreshToken).getBody();
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            //token 过期 认证失败等
            return null;
        }

        //获取存储在claims中的用户信息
        String json = claims.get(SecurityEnum.USER_CONTEXT.getValue()).toString();
        User authUser = new Gson().fromJson(json, User.class);


        String username = authUser.getUsername();
        //获取是否长期有效的token
//        boolean longTerm = authUser.getLongTerm();


        //如果缓存中有刷新token &&
        if (redisOperator.keyIsExist(CachePrefix.REFRESH_TOKEN.getPrefix(userEnums) + oldRefreshToken)) {
            Token token = new Token();
            //访问token
            String accessToken = createToken(username, authUser, tokenProperties.getTokenExpireTime());
            redisOperator.set(CachePrefix.ACCESS_TOKEN.getPrefix(userEnums) + accessToken, "1", tokenProperties.getTokenExpireTime() * 60);

            //如果是信任登录设备，则刷新token长度继续延长
            Long expirationTime = tokenProperties.getTokenExpireTime() * 2;
//            if (longTerm) {
//                expirationTime = 60 * 24 * 15L;
//            }

            //刷新token生成策略：如果是长时间有效的token（用于app），则默认15天有效期刷新token。如果是普通用户登录，则刷新token为普通token2倍数
            String refreshToken = createToken(username, authUser, expirationTime);

            redisOperator.set(CachePrefix.REFRESH_TOKEN.getPrefix(userEnums) + refreshToken, "1", expirationTime * 60);
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            redisOperator.del(CachePrefix.REFRESH_TOKEN.getPrefix(userEnums) + oldRefreshToken);
            return token;
        } else {
            return null;
        }
    }

    /**
     * 生成token
     *
     * @param username       主体
     * @param claim          私有神明内容
     * @param expirationTime 过期时间（分钟）
     * @return token字符串
     */
    private String createToken(String username, Object claim, Long expirationTime) {
        //JWT 生成
        return Jwts.builder()
                //jwt 私有声明
                .claim(SecurityEnum.USER_CONTEXT.getValue(), new Gson().toJson(claim))
                //JWT的主体
                .setSubject(username)
                //失效时间 当前时间+过期分钟
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime * 60 * 1000))
                //签名算法和密钥
                .signWith(SecretKeyUtil.generalKey())
                .compact();
    }
}
