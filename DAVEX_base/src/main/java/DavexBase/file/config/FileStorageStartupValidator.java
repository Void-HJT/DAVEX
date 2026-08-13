package DavexBase.file.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * 文件数据目录启动门禁。
 * 服务启动前创建并实际写入数据目录，避免任务运行后才发现目录不可用。
 */
@Component
public class FileStorageStartupValidator {

    private final String configuredPath;

    public FileStorageStartupValidator(@Value("${my.base_path}") String configuredPath) {
        this.configuredPath = configuredPath;
    }

    @PostConstruct
    public void validate() {
        if (configuredPath == null || configuredPath.isBlank()) {
            throw new IllegalStateException("my.base_path 未配置");
        }

        Path dataRoot;

        try {
            dataRoot = Path.of(configuredPath)
                    .toAbsolutePath()
                    .normalize();
        } catch (InvalidPathException exception) {
            throw new IllegalStateException("my.base_path 不是有效路径: " + configuredPath, exception);
        }

        try {
            Files.createDirectories(dataRoot);

            if (!Files.isDirectory(dataRoot)) {
                throw new IOException("目标路径不是目录");
            }

            // 创建、写入并删除探针，验证真实写入权限。
            Path probe = Files.createTempFile(
                    dataRoot,
                    ".davex-write-probe-",
                    ".tmp");

            try {
                Files.writeString(probe, "DAVEX");
            } finally {
                Files.deleteIfExists(probe);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("DAVEX 数据目录不可写: " + dataRoot, exception);
        }
    }
}