package DavexBase.file.service;

import DavexBase.entity.File;
import DavexBase.file.port.FileMetadataRepository;
import DavexBase.file.port.FileStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

/**
 * 阶段 5.2 上传协调测试：锁定“临时写入、原子移动、提交元数据”的顺序及失败补偿。
 */
@ExtendWith(MockitoExtension.class)
class AtomicFileUploadServiceTest {

    @Mock
    private FileStore fileStore;

    @Mock
    private FileMetadataRepository metadataRepository;

    @InjectMocks
    private AtomicFileUploadService uploadService;

    @Test
    void commitsMetadataOnlyAfterFileBecomesVisible() throws Exception {
        Path target = Path.of("/data/input.csv");
        Path staged = Path.of("/data/.input.csv.upload");
        InputStream content = new ByteArrayInputStream(new byte[] {1, 2, 3});
        File metadata = metadata("FILE-1");
        when(fileStore.writeTemporary(target, content)).thenReturn(staged);

        uploadService.upload(target, content, metadata);

        InOrder order = inOrder(fileStore, metadataRepository);
        order.verify(fileStore).writeTemporary(target, content);
        order.verify(fileStore).moveAtomically(staged, target);
        order.verify(metadataRepository).upsert(metadata);
    }

    @Test
    void removesTemporaryFileWhenAtomicMoveFails() throws Exception {
        Path target = Path.of("/data/input.csv");
        Path staged = Path.of("/data/.input.csv.upload");
        InputStream content = new ByteArrayInputStream(new byte[] {1});
        File metadata = metadata("FILE-1");
        IOException failure = new IOException("atomic move failed");
        when(fileStore.writeTemporary(target, content)).thenReturn(staged);
        doThrow(failure).when(fileStore).moveAtomically(staged, target);

        IOException actual = assertThrows(IOException.class,
                () -> uploadService.upload(target, content, metadata));

        assertSame(failure, actual);
        verify(fileStore).deleteIfExists(staged);
        verify(fileStore, never()).deleteIfExists(target);
        verify(metadataRepository, never()).upsert(metadata);
    }

    @Test
    void removesVisibleFileWhenMetadataCommitFails() throws Exception {
        Path target = Path.of("/data/input.csv");
        Path staged = Path.of("/data/.input.csv.upload");
        InputStream content = new ByteArrayInputStream(new byte[] {1});
        File metadata = metadata("FILE-1");
        IllegalStateException failure = new IllegalStateException("database unavailable");
        when(fileStore.writeTemporary(target, content)).thenReturn(staged);
        doThrow(failure).when(metadataRepository).upsert(metadata);

        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> uploadService.upload(target, content, metadata));

        assertSame(failure, actual);
        verify(fileStore).deleteIfExists(staged);
        verify(fileStore).deleteIfExists(target);
    }

    @Test
    void doesNotCommitMetadataWhenTemporaryWriteFails() throws Exception {
        Path target = Path.of("/data/input.csv");
        InputStream content = new ByteArrayInputStream(new byte[] {1});
        File metadata = metadata("FILE-1");
        IOException diskFailure = new IOException("no space left on device");
        when(fileStore.writeTemporary(target, content)).thenThrow(diskFailure);

        IOException actual = assertThrows(IOException.class,
                () -> uploadService.upload(target, content, metadata));

        assertSame(diskFailure, actual);
        verify(fileStore, never()).moveAtomically(any(), any());
        verify(metadataRepository, never()).upsert(metadata);
    }

    private File metadata(String uid) {
        File metadata = new File();
        metadata.setUid(uid);
        return metadata;
    }
}
