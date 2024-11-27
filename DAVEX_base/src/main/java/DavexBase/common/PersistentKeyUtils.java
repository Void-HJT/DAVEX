package DavexBase.common;

import DavexBase.entity.KeysStorage;
import DavexBase.mapper.KeysStorageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import java.util.Objects;

@Component
public class PersistentKeyUtils {

    @Autowired
    private KeysStorageMapper keysStorageMapper;

    private static final String CENTER_PRIVATE_KEY_FILE = "center_private_key.pem";
    private static final String AGENT_PRIVATE_KEY_FILE = "agent_private_key.pem";

    // 初始化密钥对：如果密钥文件存在则加载，否则生成并保存
    public KeyPair initializeKeyPair(String ownerName,String identity) throws Exception {
        String private_key_file = AGENT_PRIVATE_KEY_FILE;
        if(identity.equals("center")){
            private_key_file = CENTER_PRIVATE_KEY_FILE;
        }

        PrivateKey privateKey;
        PublicKey publicKey;

        // 检查私钥文件是否存在
        if (Files.exists(Paths.get(private_key_file))) {
            privateKey = loadPrivateKey(private_key_file);
        } else {
            // 如果私钥文件不存在，则生成密钥对
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();

            // 保存私钥到本地文件
            savePrivateKey(pair.getPrivate(), private_key_file);

            // 保存公钥到数据库
            savePublicKeyToDB(ownerName, pair.getPublic());

            return pair;
        }

        // 从数据库加载公钥
        publicKey = loadPublicKeyFromDB(ownerName);

        return new KeyPair(publicKey, privateKey);
    }

    // 保存私钥到 PEM 文件
    private static void savePrivateKey(PrivateKey privateKey, String filename) throws  IOException {
        String privateKeyPEM = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getEncoder().encodeToString(privateKey.getEncoded()) +
                "\n-----END PRIVATE KEY-----";
        Files.write(Paths.get(filename), privateKeyPEM.getBytes());
    }

//    // 保存公钥到数据库
    private void savePublicKeyToDB(String ownerName, PublicKey publicKey) {
        String publicKeyPEM = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        // 检查是否已有记录
        LambdaQueryWrapper<KeysStorage> queryWrapper = Wrappers.<KeysStorage>lambdaQuery()
                .eq(KeysStorage::getOwnerName, ownerName);
        KeysStorage existingKey = keysStorageMapper.selectOne(queryWrapper);

        if (existingKey == null) {
            // 如果没有记录，插入新记录
            KeysStorage newKey = new KeysStorage();
            newKey.setOwnerName(ownerName);
            newKey.setPublicKey(publicKeyPEM);
            keysStorageMapper.insert(newKey);
        } else {
            // 如果已有记录，更新公钥
            existingKey.setPublicKey(publicKeyPEM);
            keysStorageMapper.updateById(existingKey);
        }
    }
//
//    // 从数据库加载公钥
    private PublicKey loadPublicKeyFromDB(String ownerName) throws Exception {
        LambdaQueryWrapper<KeysStorage> queryWrapper = Wrappers.<KeysStorage>lambdaQuery()
                .eq(KeysStorage::getOwnerName, ownerName);
        KeysStorage queryKey = keysStorageMapper.selectOne(queryWrapper);

        if (queryKey != null) {
            return parseString2PublicKey(queryKey.getPublicKey());
        } else {
            throw new Exception("Public key not found for owner: " + ownerName);
        }
    }

    // 从 PEM 文件加载私钥
    private static PrivateKey loadPrivateKey(String filename) throws Exception {
        String key = new String(Files.readAllBytes(Paths.get(filename)))
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    // 将Base64字符串转换为PublicKey对象
    public static PublicKey parseString2PublicKey(String publicKeyPEM) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyPEM);
        // 创建 X509EncodedKeySpec
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);

        // 使用 KeyFactory 生成 PublicKey 对象
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
}