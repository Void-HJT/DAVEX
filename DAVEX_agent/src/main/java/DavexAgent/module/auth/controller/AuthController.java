package DavexAgent.module.auth.controller;


import DavexBase.common.AuthTokenCache;
import DavexBase.service.auth.TokenValidationService;
import DavexBase.info.TokenResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    TokenValidationService tokenValidationService;

    @Autowired
    AuthTokenCache authTokenCache;

    @PostMapping("/getToken")
    public TokenResult getToken(@RequestParam String username,
                                @RequestParam String password,
                                @RequestParam String authId){
        return tokenValidationService.getToken(username,password,authId);
    }

    @PostMapping("/isTokenExpired")
    public boolean isTokenExpired(@RequestParam String authId){
        return tokenValidationService.isTokenExpired(authId);
    }

    @PostMapping("/updatePublicKey")
    public void updatePublicKey(@RequestParam String username,
                                @RequestParam String password,
                                @RequestParam String authId){
        try {
            tokenValidationService.updatePublicKey(username,password,authId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @PostMapping("/getTokenFromCache")
    public TokenResult getTokenFromCache(@RequestParam String authId)
    {
        TokenResult tokenResult = authTokenCache.getToken(authId);
        return tokenResult;
    }

    @PostMapping("/deleteCache")
    public boolean deleteCache(){
        authTokenCache.clearCache();
        return true;
    }
}
