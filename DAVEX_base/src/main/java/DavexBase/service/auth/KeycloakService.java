package DavexBase.service.auth;

import DavexBase.common.R;
import DavexBase.entity.Keycloak;
import DavexBase.entity.KeycloakCredentials;
import DavexBase.mapper.KeycloakCredentialsMapper;
import DavexBase.mapper.KeycloakMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KeycloakService {

    @Autowired
    KeycloakMapper keycloakMapper;

    @Autowired
    KeycloakCredentialsMapper keycloakCredentialsMapper;

    public R<String> addKeycloak(Keycloak keycloak){
        keycloakMapper.insert(keycloak);
        return R.success("添加成功");
    }

    public R<String> updateKeycloak(Keycloak keycloak){
        keycloakMapper.updateById(keycloak);
        return R.success("更新成功");

    }

    public R<String> deleteKeycloak(String authId){
        keycloakMapper.deleteById(authId);
        return R.success("删除成功");

    }

    public R<Keycloak> getKeycloak(String authenticationId){
        LambdaQueryWrapper<Keycloak> keycloakLambdaQueryWrapper = Wrappers.<Keycloak>lambdaQuery().eq(Keycloak::getAuthenticationId, authenticationId);
        Keycloak keycloak = keycloakMapper.selectOne(keycloakLambdaQueryWrapper);
        return R.success(keycloak,"返回keycloak信息");
    }

    public R<String> addKeycloakCredentials(KeycloakCredentials keycloakCredentials){
        keycloakCredentialsMapper.insert(keycloakCredentials);
        return R.success("添加成功");
    }

    public R<String> updateKeycloakCredentials(KeycloakCredentials keycloakCredentials){
        keycloakCredentialsMapper.updateById(keycloakCredentials);
        return R.success("更新成功");

    }

    public R<String> deleteKeycloakCredentials(String targetId){
        keycloakCredentialsMapper.deleteById(targetId);
        return R.success("删除成功");

    }

    public R<KeycloakCredentials> getKeycloakCredentials(String targetId){
        LambdaQueryWrapper<KeycloakCredentials> keycloakCredentialsLambdaQueryWrapper = Wrappers.<KeycloakCredentials>lambdaQuery().eq(KeycloakCredentials::getTargetId, targetId);
        KeycloakCredentials keycloakCredentials = keycloakCredentialsMapper.selectOne(keycloakCredentialsLambdaQueryWrapper);
        return R.success(keycloakCredentials,"返回keycloakCredentials信息");
    }
}
