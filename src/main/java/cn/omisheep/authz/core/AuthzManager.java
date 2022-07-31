package cn.omisheep.authz.core;

import cn.omisheep.authz.core.auth.ipf.Blacklist;
import cn.omisheep.authz.core.auth.ipf.Httpd;
import cn.omisheep.authz.core.auth.rpd.PermissionDict;
import cn.omisheep.authz.core.cache.L2Cache;
import cn.omisheep.authz.core.config.AuthzAppVersion;
import cn.omisheep.authz.core.helper.BaseHelper;
import cn.omisheep.authz.core.msg.AuthzModifier;
import cn.omisheep.web.entity.Result;
import cn.omisheep.web.entity.ResultCode;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * @author zhouxinchen[1269670415@qq.com]
 * @since 1.0.0
 */
@SuppressWarnings("rawtypes")
public class AuthzManager extends BaseHelper {

    @Nullable
    public static Object op(@NonNull AuthzModifier authzModifier) {
        if (authzModifier.getTarget() == AuthzModifier.Target.RATE) {
            return Httpd.modify(authzModifier);
        } else if (authzModifier.getTarget() == AuthzModifier.Target.BLACKLIST) {
            return Blacklist.modify(authzModifier);
        } else {
            return PermissionDict.modify(authzModifier);
        }
    }

    @Nullable
    public static Object modify(@NonNull AuthzModifier authzModifier) {
        try {
            return op(authzModifier);
        } finally {
            if (cache instanceof L2Cache) AuthzAppVersion.send(authzModifier);
        }
    }

    @NonNull
    public static Result operate(@NonNull AuthzModifier authzModifier) {
        Object res = modify(authzModifier);
        if (res == null) return Result.SUCCESS.data(null);
        if (res instanceof Result) return (Result) res;
        if (res instanceof ResultCode) return ((ResultCode) res).data();
        return Result.SUCCESS.data(res);
    }

}
