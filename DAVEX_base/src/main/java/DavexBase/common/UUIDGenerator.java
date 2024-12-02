package DavexBase.common;

import java.util.UUID;

public class UUIDGenerator {
    // 生成带有请求类型、任务类型的 UUID
    public static String generateUUID(String requestType, String taskType, String participantID) {
        // 验证请求类型是否有效
        if (!isValidRequestType(requestType)) {
            throw new IllegalArgumentException("Invalid request type. Must be 'request' or 'response'.");
        }

        // 验证任务类型是否有效
        if (!isValidTaskType(taskType)) {
            throw new IllegalArgumentException("Invalid task type. Must be 'query', 'comparison', or 'other'.");
        }

        // 生成 UUID
        UUID uuid = UUID.randomUUID();

        // 拼接字符串：请求类型-任务类型-生成的 UUID
        return String.format("%s-%s-%s-%s", participantID, requestType, taskType, uuid.toString());
    }

    // 验证请求类型是否有效
    private static boolean isValidRequestType(String requestType) {
        return "request".equalsIgnoreCase(requestType) || "response".equalsIgnoreCase(requestType);
    }

    // 验证任务类型是否有效
    private static boolean isValidTaskType(String taskType) {
        return "query".equalsIgnoreCase(taskType) || "comparison".equalsIgnoreCase(taskType) || "other".equalsIgnoreCase(taskType);
    }
}