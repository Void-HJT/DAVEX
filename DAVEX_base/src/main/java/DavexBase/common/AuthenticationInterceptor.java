package DavexBase.common;

import DavexBase.service.auth.TokenValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    TokenValidationService tokenValidationService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头中提取认证者ID和Token
        String authId = request.getHeader("Authentication-ID");
        String token = request.getHeader("Token");

//        if (authId == null || authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
//            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing or invalid Authentication-ID or Authorization header");
//            return false;
//        }
//
//        String token = authorizationHeader.substring(7); // 切除"Bearer "前缀

        // TODO: 使用TokenValidationService对认证者ID和Token进行验证
        boolean isValid = tokenValidationService.validateToken(authId,token);
        if (!isValid) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid Token");
            return false;
        }


        return true; // 验证成功，继续处理请求
    }
}
