package DavexBase.file.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 阶段 5.4 路径安全测试：文件名和数据库目录名均不得逃逸配置的数据根目录。
 */
class SafeFilePathResolverTest {

    @TempDir
    Path dataRoot;

    private final SafeFilePathResolver resolver = new SafeFilePathResolver();

    @Test
    void resolvesARegularFileInsideDataRoot() {
        Path resolved = resolver.resolve(
                dataRoot,
                List.of("agent", "input"),
                "dataset.csv");

        assertEquals(
                dataRoot.resolve("agent").resolve("input").resolve("dataset.csv")
                        .toAbsolutePath().normalize(),
                resolved);
    }

    @Test
    void rejectsParentDirectoryTraversalInFileName() {
        assertThrows(IllegalArgumentException.class,
                () -> resolver.resolve(dataRoot, List.of("input"), "../secret.txt"));
    }

    @Test
    void rejectsUnixAndWindowsPathSeparatorsInFileName() {
        assertThrows(IllegalArgumentException.class,
                () -> resolver.resolve(dataRoot, List.of("input"), "nested/file.csv"));
        assertThrows(IllegalArgumentException.class,
                () -> resolver.resolve(dataRoot, List.of("input"), "nested\\file.csv"));
    }

    @Test
    void rejectsUnsafeDirectoryNameLoadedFromDatabase() {
        assertThrows(IllegalArgumentException.class,
                () -> resolver.resolve(dataRoot, List.of("..", "input"), "file.csv"));
    }

    @Test
    void rejectsBlankFileName() {
        assertThrows(IllegalArgumentException.class,
                () -> resolver.resolve(dataRoot, List.of("input"), "  "));
    }
}
