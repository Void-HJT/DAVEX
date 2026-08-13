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

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 阶段 5.3 删除协调测试：文件先进入隔离区，元数据失败时必须恢复原文件。
 */
@ExtendWith(MockitoExtension.class)
class AtomicFileDeleteServiceTest {

    @Mock
    private FileStore fileStore;

    @Mock
    private FileMetadataRepository metadataRepository;

    @InjectMocks
    private AtomicFileDeleteService deleteService;

    @Test
    void purgesStagedFileOnlyAfterMetadataIsDeleted() throws Exception {
        Path target = Path.of("/data/dataset.csv");
        Path staged = Path.of("/data/.dataset.csv.delete");
        File metadata = metadata("FILE-1");
        when(fileStore.stageForDeletion(target)).thenReturn(staged);

        deleteService.delete(target, metadata);

        InOrder order = inOrder(fileStore, metadataRepository);
        order.verify(fileStore).stageForDeletion(target);
        order.verify(metadataRepository).deleteById("FILE-1");
        order.verify(fileStore).deleteIfExists(staged);
    }

    @Test
    void leavesMetadataUntouchedWhenFileCannotBeStaged() throws Exception {
        Path target = Path.of("/data/dataset.csv");
        File metadata = metadata("FILE-1");
        IOException failure = new IOException("disk unavailable");
        when(fileStore.stageForDeletion(target)).thenThrow(failure);

        IOException actual = assertThrows(IOException.class,
                () -> deleteService.delete(target, metadata));

        assertSame(failure, actual);
        verify(metadataRepository, never()).deleteById("FILE-1");
    }

    @Test
    void restoresOriginalFileWhenMetadataDeletionFails() throws Exception {
        Path target = Path.of("/data/dataset.csv");
        Path staged = Path.of("/data/.dataset.csv.delete");
        File metadata = metadata("FILE-1");
        IllegalStateException failure = new IllegalStateException("database unavailable");
        when(fileStore.stageForDeletion(target)).thenReturn(staged);
        doThrow(failure).when(metadataRepository).deleteById("FILE-1");

        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> deleteService.delete(target, metadata));

        assertSame(failure, actual);
        verify(fileStore).restore(staged, target);
        verify(fileStore, never()).deleteIfExists(staged);
    }

    @Test
    void reportsRestoreFailureWithoutHidingOriginalDatabaseFailure() throws Exception {
        Path target = Path.of("/data/dataset.csv");
        Path staged = Path.of("/data/.dataset.csv.delete");
        File metadata = metadata("FILE-1");
        IllegalStateException databaseFailure = new IllegalStateException("database unavailable");
        IOException restoreFailure = new IOException("restore failed");
        when(fileStore.stageForDeletion(target)).thenReturn(staged);
        doThrow(databaseFailure).when(metadataRepository).deleteById("FILE-1");
        doThrow(restoreFailure).when(fileStore).restore(staged, target);

        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> deleteService.delete(target, metadata));

        assertSame(databaseFailure, actual);
        assertSame(restoreFailure, actual.getSuppressed()[0]);
    }

    private File metadata(String uid) {
        File metadata = new File();
        metadata.setUid(uid);
        return metadata;
    }
}
