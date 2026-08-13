package DavexBase.file.port;

import DavexBase.entity.File;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 阶段 5.1 架构测试：锁定文件系统与元数据持久化的独立端口，
 * 防止后续原子上传和失败补偿继续直接依赖 Files 与 MyBatis Mapper。
 */
class FileBoundaryContractTest {

    @Test
    void fileStoreExposesTemporaryWriteAtomicMoveAndCleanupOperations() throws Exception {
        Class<?> fileStore = Class.forName("DavexBase.file.port.FileStore");

        assertTrue(fileStore.isInterface(), "FileStore 必须是端口接口");
        assertReturnType(fileStore, "writeTemporary", Path.class, Path.class, InputStream.class);
        assertReturnType(fileStore, "moveAtomically", void.class, Path.class, Path.class);
        assertReturnType(fileStore, "stageForDeletion", Path.class, Path.class);
        assertReturnType(fileStore, "restore", void.class, Path.class, Path.class);
        assertReturnType(fileStore, "deleteIfExists", void.class, Path.class);
    }

    @Test
    void metadataRepositoryExposesIdempotentPersistenceOperations() throws Exception {
        Class<?> repository = Class.forName("DavexBase.file.port.FileMetadataRepository");

        assertTrue(repository.isInterface(), "FileMetadataRepository 必须是端口接口");
        assertReturnType(repository, "findById", Optional.class, String.class);
        assertReturnType(repository, "upsert", void.class, File.class);
        assertReturnType(repository, "deleteById", void.class, String.class);
    }

    private void assertReturnType(
            Class<?> owner, String methodName, Class<?> returnType, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = owner.getMethod(methodName, parameterTypes);
        assertEquals(returnType, method.getReturnType());
    }
}
