package DavexBase.file.service;

import DavexBase.entity.File;
import DavexBase.file.port.FileMetadataRepository;
import DavexBase.file.port.FileStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
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

    /**
     * 用新内容和元数据替换现有文件。
     * 旧文件先进入隔离区；新文件或元数据提交失败时恢复旧文件。
     */
    public void replace(
            Path targetPath,
            InputStream content,
            File metadata) throws IOException {

        Path previousFile;

        try {
            previousFile = fileStore.stageForDeletion(targetPath);
        } catch (NoSuchFileException missingFile) {
            // 元数据存在但磁盘文件已丢失时，按原 UID 重新创建文件。
            upload(targetPath, content, metadata);
            return;
        }

        try {
            upload(targetPath, content, metadata);
        } catch (IOException | RuntimeException exception) {
            try {
                fileStore.restore(previousFile, targetPath);
            } catch (IOException restoreException) {
                exception.addSuppressed(restoreException);
            }
            throw exception;
        }

        // 新文件和元数据均成功后，旧文件备份不再需要。
        fileStore.deleteIfExists(previousFile);
    }

    private void cleanup(Path path, Exception originalException) {
        try {
            fileStore.deleteIfExists(path);
        } catch (IOException cleanupException) {
            originalException.addSuppressed(cleanupException);
        }
    }
}
