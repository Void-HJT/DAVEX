package DavexBase.file.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 阶段 5.5 启动门禁测试：数据目录必须可创建、可写，并且不遗留探针文件。
 */
class FileStorageStartupValidatorTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void createsConfiguredDirectoryAndVerifiesItIsWritable() {
        Path dataRoot = temporaryDirectory.resolve("external-data");
        FileStorageStartupValidator validator =
                new FileStorageStartupValidator(dataRoot.toString());

        validator.validate();

        assertTrue(Files.isDirectory(dataRoot));
    }

    @Test
    void removesWriteProbeAfterSuccessfulValidation() throws Exception {
        Path dataRoot = temporaryDirectory.resolve("external-data");
        FileStorageStartupValidator validator =
                new FileStorageStartupValidator(dataRoot.toString());

        validator.validate();

        try (var entries = Files.list(dataRoot)) {
            assertFalse(entries.findAny().isPresent());
        }
    }

    @Test
    void rejectsBlankConfigurationInsteadOfUsingWorkingDirectory() {
        FileStorageStartupValidator validator =
                new FileStorageStartupValidator("  ");

        assertThrows(IllegalStateException.class, validator::validate);
    }

    @Test
    void rejectsARegularFileAsDataDirectory() throws Exception {
        Path regularFile = temporaryDirectory.resolve("not-a-directory");
        Files.writeString(regularFile, "content");
        FileStorageStartupValidator validator =
                new FileStorageStartupValidator(regularFile.toString());

        assertThrows(IllegalStateException.class, validator::validate);
    }
}
