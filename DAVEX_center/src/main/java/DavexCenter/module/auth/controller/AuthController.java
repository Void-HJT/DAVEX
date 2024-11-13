package DavexCenter.module.auth.controller;


import DavexBase.common.AuthTokenCache;
import DavexBase.info.TokenResult;
import DavexBase.service.auth.TokenValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


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
    public TokenResult getTokenFromCache(@RequestParam("KeycloakUrl") String KeycloakUrl)
    {
        TokenResult tokenResult = authTokenCache.getToken(KeycloakUrl);
        return tokenResult;
    }

    @PostMapping("/deleteCache")
    public boolean deleteCache(){
        authTokenCache.clearCache();
        return true;
    }
}
