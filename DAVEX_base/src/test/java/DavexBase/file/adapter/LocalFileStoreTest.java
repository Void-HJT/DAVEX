package DavexBase.file.adapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 阶段 5.2 文件存储测试：正式文件只能在完整写入后通过原子移动变为可见。
 */
class LocalFileStoreTest {

    @TempDir
    Path temporaryDirectory;

    private final LocalFileStore fileStore = new LocalFileStore();

    @Test
    void keepsTargetInvisibleUntilTemporaryFileIsMoved() throws Exception {
        Path target = temporaryDirectory.resolve("input").resolve("dataset.csv");
        byte[] content = "id,value\n1,42".getBytes(StandardCharsets.UTF_8);

        Path staged = fileStore.writeTemporary(target, new ByteArrayInputStream(content));

        assertTrue(Files.exists(staged));
        assertTrue(staged.getParent().equals(target.getParent()));
        assertFalse(Files.exists(target));
        assertArrayEquals(content, Files.readAllBytes(staged));

        fileStore.moveAtomically(staged, target);

        assertTrue(Files.exists(target));
        assertFalse(Files.exists(staged));
        assertArrayEquals(content, Files.readAllBytes(target));
    }

    @Test
    void refusesToOverwriteAnExistingTarget() throws Exception {
        Path target = temporaryDirectory.resolve("dataset.csv");
        Files.writeString(target, "old-content");
        Path staged = fileStore.writeTemporary(
                target,
                new ByteArrayInputStream("new-content".getBytes(StandardCharsets.UTF_8)));

        assertThrows(FileAlreadyExistsException.class,
                () -> fileStore.moveAtomically(staged, target));
        assertTrue(Files.exists(staged));
        assertTrue("old-content".equals(Files.readString(target)));
    }

    @Test
    void cleanupIsIdempotent() throws Exception {
        Path path = temporaryDirectory.resolve("temporary.upload");
        Files.writeString(path, "partial-content");

        fileStore.deleteIfExists(path);
        fileStore.deleteIfExists(path);

        assertFalse(Files.exists(path));
    }

    @Test
    void stagesAFileForRecoverableDeletionAndRestoresIt() throws Exception {
        Path target = temporaryDirectory.resolve("dataset.csv");
        Files.writeString(target, "important-content");

        Path staged = fileStore.stageForDeletion(target);

        assertFalse(Files.exists(target));
        assertTrue(Files.exists(staged));
        assertTrue(staged.getParent().equals(target.getParent()));

        fileStore.restore(staged, target);

        assertTrue(Files.exists(target));
        assertFalse(Files.exists(staged));
        assertTrue("important-content".equals(Files.readString(target)));
    }
}
