package DavexBase.common;

import DavexBase.info.ContractVerificationSubmitRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Base64;

public class JsonFileUtil {

    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * 将任意对象以追加的方式写入 JSON 文件。
     * @param filePath JSON 文件的路径
     * @param data 要写入的对象
     * @throws IOException 当文件读写出现问题时抛出
     */
    public static void appendToJsonFile(String filePath, Object data) throws IOException {
        File file = new File(filePath);
        ArrayNode arrayNode;

        if (file.exists()) {
            // 如果文件存在，读取现有内容
            arrayNode = (ArrayNode) mapper.readTree(file);
        } else {
            // 文件不存在时，创建新的数组节点
            arrayNode = mapper.createArrayNode();
        }

        // 将新数据转换为 JSON 节点并追加到数组
        ObjectNode objectNode = mapper.valueToTree(data);
        arrayNode.add(objectNode);

        // 将更新后的内容写回文件
        mapper.writeValue(file, arrayNode);
    }

    /**
     * 生成 requestHash，用于链上验证
     * @param request ContractVerificationSubmitRequest 实例
     * @return Base64 编码的 SHA-256 哈希
     * @throws Exception 当序列化或哈希计算失败时
     */
    public static String generateRequestHash(ContractVerificationSubmitRequest request) throws Exception {
        // 序列化请求对象为 JSON 字符串
        ObjectMapper mapper = new ObjectMapper();
        byte[] jsonBytes = mapper.writeValueAsBytes(request);

        // 计算 JSON 字符串的 SHA-256 哈希
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(jsonBytes);

        // 返回 Base64 编码的哈希值
        return Base64.getEncoder().encodeToString(hashBytes);
    }
}
