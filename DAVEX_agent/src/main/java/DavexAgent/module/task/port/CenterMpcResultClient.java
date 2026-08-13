package DavexAgent.module.task.port;

import java.nio.file.Path;

import DavexBase.entity.MpcTaskOutput;

/**
 * Agent 向 Center 上传 MPC/PSI 结果文件的业务端口。
 *
 * 端口使用 Path 和结果元数据，不向业务层暴露 Multipart 与 WebClient。
 */
public interface CenterMpcResultClient {

    void uploadPsiResult(
            String centerId,
            Path resultFile,
            MpcTaskOutput metadata) throws Exception;
}
