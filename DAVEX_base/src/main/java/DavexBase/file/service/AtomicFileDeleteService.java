package DavexBase.file.service;

import DavexBase.entity.File;
import DavexBase.file.port.FileMetadataRepository;
import DavexBase.file.port.FileStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 协调实际文件与元数据删除。
 * 文件先进入隔离区，元数据删除失败时恢复原文件。
 */
@Service
public class AtomicFileDeleteService {

    private final FileStore fileStore;
    private final FileMetadataRepository metadataRepository;

    public AtomicFileDeleteService(
            FileStore fileStore,
            FileMetadataRepository metadataRepository) {
        this.fileStore = fileStore;
        this.metadataRepository = metadataRepository;
    }

    public void delete(Path targetPath, File metadata) throws IOException {
        // 文件无法进入隔离区时，不允许删除元数据。
        Path stagedPath = fileStore.stageForDeletion(targetPath);

        try {
            metadataRepository.deleteById(metadata.getUid());
        } catch (RuntimeException exception) {
            restore(stagedPath, targetPath, exception);
            throw exception;
        }

        // 元数据成功删除后，隔离文件不再对业务可见。
        fileStore.deleteIfExists(stagedPath);
    }

    private void restore(
            Path stagedPath,
            Path targetPath,
            RuntimeException originalException) {
        try {
            fileStore.restore(stagedPath, targetPath);
        } catch (IOException restoreException) {
            // 保留数据库异常作为主异常，同时记录补偿失败。
            originalException.addSuppressed(restoreException);
        }
    }
}