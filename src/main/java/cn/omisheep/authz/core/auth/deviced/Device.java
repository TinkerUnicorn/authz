package cn.omisheep.authz.core.auth.deviced;

import cn.omisheep.authz.core.tk.GrantType;

import java.util.Map;

/**
 * @author zhouxinchen[1269670415@qq.com]
 * @since 1.0.0
 */
public interface Device extends Map<Object, Object>, java.io.Serializable {

    // 设备id
    String getDeviceId();

    Device setDeviceId(String id);

    // 设备类型
    String getDeviceType();

    Device setDeviceType(String type);

    // 过期时间
    Long getExpiresAt();

    Device setExpiresAt(Long expiresAt);

    // accessTokenId
    String getAccessTokenId();

    Device setAccessTokenId(String accessTokenId);

    // scope
    String getScope();

    Device setScope(String scope);

    // grantType
    GrantType getGrantType();

    Device setGrantType(GrantType grantType);

    // clientId
    String getClientId();

    Device setClientId(String clientId);

}
