package DavexCenter.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtTokenUtil; // JWT 工具类

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // 1. 从请求头提取 Token
        String token = jwtTokenUtil.resolveToken(request);
        String authId = request.getHeader("AuthId");
        if (token == null || !jwtTokenUtil.validateToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT");
            return false; // 拦截请求
        }

        // 2. 解析用户信息（如用户名）
        String username = jwtTokenUtil.getAgentUidFromToken(token);
        if(!username.equals(authId)){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT do not match authId");
            return false;
        }
        // 3. 将用户信息存入请求属性（后续Controller可通过 @RequestAttribute 获取）
        request.setAttribute("currentUser", username);
        return true; // 放行请求
    }
}