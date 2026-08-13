package DavexBase.file.service;

import DavexBase.entity.File;
import DavexBase.file.port.FileMetadataRepository;
import DavexBase.file.port.FileStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * 协调文件落盘和元数据提交。
 * 任一步失败时清理本次上传产生的临时文件或正式文件。
 */
@Service
public class AtomicFileUploadService {

    private final FileStore fileStore;
    private final FileMetadataRepository metadataRepository;

    public AtomicFileUploadService(
            FileStore fileStore,
            FileMetadataRepository metadataRepository) {
        this.fileStore = fileStore;
        this.metadataRepository = metadataRepository;
    }

    public void upload(
            Path targetPath,
            InputStream content,
            File metadata) throws IOException {

        Path temporaryPath = fileStore.writeTemporary(targetPath, content);
        boolean moved = false;

        try {
            fileStore.moveAtomically(temporaryPath, targetPath);
            moved = true;

            // 正式文件可见后才提交对应的成功元数据。
            metadataRepository.upsert(metadata);
        } catch (IOException | RuntimeException exception) {
            cleanup(temporaryPath, exception);

            if (moved) {
                cleanup(targetPath, exception);
            }

            throw exception;
        }
    }

    private void cleanup(Path path, Exception originalException) {
        try {
            fileStore.deleteIfExists(path);
        } catch (IOException cleanupException) {
            originalException.addSuppressed(cleanupException);
        }
    }
}