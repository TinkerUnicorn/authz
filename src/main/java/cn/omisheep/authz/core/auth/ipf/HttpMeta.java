package cn.omisheep.authz.core.auth.ipf;

import cn.omisheep.authz.core.AuthzException;
import cn.omisheep.authz.core.ExceptionStatus;
import cn.omisheep.authz.core.auth.deviced.UserDevicesDict;
import cn.omisheep.authz.core.auth.rpd.PermRolesMeta;
import cn.omisheep.authz.core.config.AuthzAppVersion;
import cn.omisheep.authz.core.config.Constants;
import cn.omisheep.authz.core.helper.BaseHelper;
import cn.omisheep.authz.core.tk.AccessToken;
import cn.omisheep.authz.core.util.LogUtils;
import cn.omisheep.commons.util.CollectionUtils;
import cn.omisheep.web.utils.HttpUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NonNull;
import org.springframework.boot.logging.LogLevel;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import static cn.omisheep.authz.core.util.LogUtils.export;

/**
 * @author zhouxinchen[1269670415@qq.com]
 * @since 1.0.0
 */
@Data
@SuppressWarnings("all")
public class HttpMeta extends BaseHelper {

    @JsonIgnore
    private final HttpServletRequest          request;
    private final String                      ip;
    private final String                      uri;
    private final String                      api;
    private       String                      servletPath;
    private       String                      path;
    private final String                      method;
    private final String                      userAgent;
    private final String                      refer;
    private       String                      body;
    private final Date                        now;
    private       AccessToken                 token;
    private       Object                      userId;
    private       boolean                     hasToken;
    private       Set<String>                 roles;
    private       Set<String>                 permissions;
    private       Set<String>                 scope;
    private       boolean                     requireProtect;
    private       boolean                     requireLogin;
    private       PermRolesMeta               permRolesMeta;
    private       boolean                     ignore              = false;
    private       UserDevicesDict.UserStatus  userStatus;
    @JsonIgnore
    private       LinkedList<Object>          exceptionObjectList = new LinkedList<>();
    @JsonIgnore
    private       LinkedList<ExceptionStatus> exceptionStatusList = new LinkedList<>();

    public HttpMeta setRoles(Set<String> roles) {
        if (roles == null) return this;
        this.roles = roles;
        return this;
    }

    public HttpMeta setPermissions(Set<String> permissions) {
        if (permissions == null) return this;
        this.permissions = permissions;
        return this;
    }

    @NonNull
    public Set<String> getRoles() {
        if (userId == null) return new HashSet<>();
        roles = Optional.ofNullable(roles)
                .orElse(Optional.ofNullable(permLibrary.getRolesByUserId(userId)).orElse(new HashSet<>()));
        return roles;
    }

    @NonNull
    public Set<String> getPermissions() {
        if (userId == null) return new HashSet<>();
        permissions = Optional.ofNullable(permissions).orElseGet(() -> {
            HashSet<String> perms = new HashSet<>();
            for (String role : Optional.ofNullable(getRoles()).orElse(new HashSet<>())) {
                Set<String> permissionsByRole = permLibrary.getPermissionsByRole(role);
                if (permissionsByRole != null) perms.addAll(permissionsByRole);
            }
            return perms;
        });
        return permissions;
    }

    @NonNull
    public Set<String> getScope() {
        if (userId == null) return new HashSet<>();
        scope = Optional.ofNullable(scope).orElseGet(() -> {
            String s = token.getScope();
            if (s == null || s.equals("")) return new HashSet<>();
            return CollectionUtils.ofSet(s.split(AuthzAppVersion.scopeSeparator));
        });
        return scope;
    }

    public static AccessToken currentToken() {
        try {
            return ((HttpMeta) HttpUtils.getCurrentRequest().getAttribute(Constants.HTTP_META)).token;
        } catch (Exception e) {
            return null;
        }
    }

    public static Object currentUserId() {
        try {
            return currentToken().getUserId();
        } catch (Exception e) {
            return null;
        }
    }

    public HttpMeta error(AuthzException authzException) {
        if (authzException == null) return this;
        return error(authzException.getExceptionStatus());
    }

    public HttpMeta error(ExceptionStatus exceptionStatus) {
        if (exceptionStatus != null) this.exceptionStatusList.add(exceptionStatus);
        return this;
    }

    public HttpMeta clearError() {
        this.exceptionStatusList.clear();
        return this;
    }

    public void log(String formatMsg, Object... args) {
        LogUtils.push(LogLevel.INFO, formatMsg, args);
    }

    public void log(LogLevel logLevel, String formatMsg, Object... args) {
        LogUtils.push(logLevel, formatMsg, args);
    }

    public void exportLog() {
        export();
    }

    public boolean setHasToken(boolean hasToken) {
        this.hasToken = hasToken;
        return hasToken;
    }

    /**
     * post时生效
     * 从包装过的httpRequest中读取，读取body行为只进行一次，读取之后会备份body
     *
     * @return 请求体
     */
    public String getBody() {
        if (!"POST".equals(method) || StringUtils.startsWithIgnoreCase(request.getContentType(), "multipart/")) {
            return null;
        }
        if (body == null) {
            try {
                body = new BufferedReader(new InputStreamReader(request.getInputStream()))
                        .lines().collect(Collectors.joining(System.lineSeparator()));
            } catch (IOException e) {
                LogUtils.error("read body error");
                return null;
            }
        }
        return body;
    }

    public void setToken(AccessToken token) {
        if (this.token == null) {
            this.token  = token;
            this.userId = token.getUserId();
            this.hasToken = true;
        }
    }

    public HttpMeta(HttpServletRequest request, String ip, String uri, String api,
                    String method, Date now) {
        this.request   = request;
        this.refer     = request.getHeader("Referer");
        this.ip        = ip;
        this.uri       = uri;
        this.api       = api;
        this.method    = method.toUpperCase();
        this.userAgent = request.getHeader("user-agent");
        this.now       = now;
    }

    public boolean isMethod(String method) {
        if (method != null) {
            return this.method.equals(method.toUpperCase());
        }
        return false;
    }

}