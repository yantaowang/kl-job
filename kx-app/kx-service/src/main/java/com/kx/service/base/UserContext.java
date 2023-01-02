package com.kx.service.base;

import com.google.gson.Gson;
import com.kx.common.enums.ResultCode;
import com.kx.common.enums.SecurityEnum;
import com.kx.common.exceptions.ServiceException;
import com.kx.common.utils.CachePrefix;
import com.kx.common.utils.RedisOperator;
import com.kx.service.util.token.AuthUser;
import com.kx.service.util.token.SecretKeyUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户上下文
 *
 * @author Chopper
 * @version v4.0
 * @since 2020/11/14 20:27
 */
public class UserContext {

    /**
     * 根据request获取用户信息
     *
     * @return 授权用户
     */
    public static AuthUser getCurrentUser() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String accessToken = request.getHeader(SecurityEnum.HEADER_TOKEN.getValue());
            return getAuthUser(accessToken);
        }
        return null;
    }

    /**
     * 根据request获取用户信息
     *
     * @return 授权用户
     */
    public static String getUuid() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            return request.getHeader(SecurityEnum.UUID.getValue());
        }
        return null;
    }


    /**
     * 根据jwt获取token重的用户信息
     *
     * @param cache       缓存
     * @param accessToken token
     * @return 授权用户
     */
    public static AuthUser getAuthUser(RedisOperator redisOperator, String accessToken) {
        try {
            AuthUser authUser = getAuthUser(accessToken);
            assert authUser != null;

            if (!redisOperator.keyIsExist(CachePrefix.ACCESS_TOKEN.getPrefix(authUser.getRole()) + accessToken)) {
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
            }
            return authUser;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getCurrentUserToken() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            return request.getHeader(SecurityEnum.HEADER_TOKEN.getValue());
        }
        return null;
    }

    /**
     * 根据jwt获取token重的用户信息
     *
     * @param accessToken token
     * @return 授权用户
     */
    public static AuthUser getAuthUser(String accessToken) {
        try {
            //获取token的信息
            Claims claims
                    = Jwts.parser()
                    .setSigningKey(SecretKeyUtil.generalKeyByDecoders())
                    .parseClaimsJws(accessToken).getBody();
            //获取存储在claims中的用户信息
            String json = claims.get(SecurityEnum.USER_CONTEXT.getValue()).toString();
            return new Gson().fromJson(json, AuthUser.class);
        } catch (Exception e) {
            return null;
        }
    }
}
