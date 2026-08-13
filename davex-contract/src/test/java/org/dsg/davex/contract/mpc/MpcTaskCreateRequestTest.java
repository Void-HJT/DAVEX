package org.dsg.davex.contract.mpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/**
 * 固定现有前端、Center 和 Agent 使用的 MPC 创建请求格式。
 *
 * 如果字段名称或枚举值被意外修改，该测试应立即失败。
 */
class MpcTaskCreateRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 验证现有前端 JSON 可以被新协议读取，并保持原字段名称。
     */
    @Test
    void preservesCurrentMpcCreateJsonContract() throws Exception {
        String json = """
                {
                  "partInfo": [
                    {
                      "agentID": "DAVEX-C1-G1",
                      "part": 1,
                      "fileID": "FILE-1"
                    }
                  ],
                  "applicationId": "DAVEX-C1-A1",
                  "centerId": "DAVEX-C1",
                  "compileParameters": {
                    "bitLength": 32
                  },
                  "host": "10.176.37.50",
                  "mpcId": "decision-tree",
                  "mpcName": "ignored-internal-name",
                  "n": 2,
                  "part": 0,
                  "port": 6000,
                  "runtimeParameters": {
                    "protocol": "semi2k-party"
                  },
                  "status": "INIT",
                  "taskType": "GARNET_MPC",
                  "uid": null,
                  "futureField": "ignored"
                }
                """;

        MpcTaskCreateRequest request =
                objectMapper.readValue(json, MpcTaskCreateRequest.class);

        assertEquals("DAVEX-C1-A1", request.applicationId());
        assertEquals("DAVEX-C1", request.centerId());
        assertEquals("decision-tree", request.mpcId());
        assertEquals(2, request.n());
        assertEquals(0L, request.part());
        assertEquals(6000, request.port());
        assertEquals(MpcTaskCreateRequest.Status.INIT, request.status());
        assertEquals(
                MpcTaskCreateRequest.TaskType.GARNET_MPC,
                request.taskType()
        );

        assertEquals(32, request.compileParameters().get("bitLength"));
        assertEquals(
                "semi2k-party",
                request.runtimeParameters().get("protocol")
        );

        assertEquals(1, request.partInfo().size());
        assertEquals(
                "DAVEX-C1-G1",
                request.partInfo().get(0).agentID()
        );

        // 再次序列化，确认对外字段仍保持当前格式。
        JsonNode serialized = objectMapper.valueToTree(request);

        assertTrue(serialized.has("n"));
        assertFalse(serialized.has("N"));

        // 内部业务字段不应泄露到 Center-Agent 通信协议。
        assertFalse(serialized.has("mpcName"));
        assertFalse(serialized.has("message"));
    }
}
