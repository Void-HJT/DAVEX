package DavexBase.service.MQ;


import DavexBase.common.Body;
import DavexBase.mapper.ApplicationMapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.lang.reflect.Field;

@Service
public class MessageService {
    @Autowired
    private ApplicationMapper applicationMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final MapperFactory mapperFactory;

    @Autowired
    public MessageService(MapperFactory mapperFactory) {
        this.objectMapper.registerModule(new JavaTimeModule());
        this.mapperFactory = mapperFactory;
    }

    public String getMessage(String operation,String entity,ObjectMapper objectMapper,Object obj){

        String message = null;
        try {
            message = operation+" "+entity+":" + objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "error: create message fail +" +e;
        }
        return message;
    }

    public <T> void handleMessage(String message) {
        if (message.startsWith("Create") || message.startsWith("Delete") || message.startsWith("Update")) {

            // 1. 找到第一个冒号的位置
            int colonIndex = message.indexOf(":");
            if (colonIndex == -1) {
                logError("Invalid message format: ",message);
                return;
            }

            // 2. 提取冒号前的部分，并通过空格分割为操作类型和类名
            String operationAndClass = message.substring(0, colonIndex).trim();  // 冒号前的部分
            String[] operationAndClassParts = operationAndClass.split("\\s+");   // 通过空格分割

            if (operationAndClassParts.length < 2) {
                logError("Invalid operation or class name format: ",operationAndClass);
                return;
            }

            String operation = operationAndClassParts[0];  // 操作类型 (Create, Delete, Update)
            String className = operationAndClassParts[1];  // 类名 (application 等)

            // 3. 提取冒号后的 JSON 部分
            String jsonPart = message.substring(colonIndex + 1).trim();  // 冒号后的部分

            try {
                // 获取 Mapper 接口，通过类名识别
                BaseMapper<T> mapper = (BaseMapper<T>) mapperFactory.getMapper(className);
                if (mapper == null) {
                    logError("Unrecognized class", className);
                    return;  // 不抛出异常，记录日志后返回
                }

                // 获取对应的实体类
                Class<T> entityType = (Class<T>) mapperFactory.getEntityType(className);
                if (entityType == null) {
                    logError("Entity type not found for class", className);
                    return;  // 不抛出异常，记录日志后返回
                }

                // 根据类名反序列化为对应的对象
                T obj;
                try {
                    obj = objectMapper.readValue(jsonPart, entityType);
                } catch (Exception e) {
                    logError("Failed to deserialize JSON", jsonPart, e);
                    return;  // 反序列化失败时记录日志并返回
                }

                // 执行操作
                switch (operation) {
                    case "Create":
                        mapper.insert(obj);
                        System.out.println("成功添加 " + obj);
                        break;
                    case "Delete":
                        Object primaryKeyValue = getPrimaryKeyValue(obj, entityType);
                        if (primaryKeyValue != null) {
                            mapper.deleteById((Serializable) primaryKeyValue);
                            System.out.println("成功删除 " + obj);
                        } else {
                            logError("无法找到实体类的主键值", obj.toString());
                        }
                        break;
                    case "Update":
                        mapper.updateById(obj);
                        System.out.println("成功更新 " + obj);
                        break;
                    default:
                        logError("未识别的操作", operation);
                        break;
                }
            } catch (Exception e) {
                logError("Failed to handle message", message, e);
            }
        } else {
            System.out.println("Message: " + message);
        }
    }

    private void logError(String message, String detail) {
        // 使用合适的日志工具记录错误
        System.err.println("Error: " + message + ". Detail: " + detail);
    }

    private void logError(String message, String detail, Exception e) {
        // 使用合适的日志工具记录带有异常的错误
        System.err.println("Error: " + message + ". Detail: " + detail + ". Exception: " + e.getMessage());
    }


    private <T> Object getPrimaryKeyValue(T obj, Class<T> entityType) throws Exception {
        // 获取实体类的所有字段
        for (Field field : entityType.getDeclaredFields()) {
            // 如果字段上有 @TableId 注解，则它是主键
            if (field.isAnnotationPresent(com.baomidou.mybatisplus.annotation.TableId.class)) {
                field.setAccessible(true);  // 允许访问私有字段
                return field.get(obj);  // 返回主键字段的值
            }
        }
        return null;  // 如果没有找到主键字段，返回 null
    }

}
