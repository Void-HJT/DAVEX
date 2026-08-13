package DavexBase.file.adapter;

import DavexBase.file.port.FileStore;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.NoSuchFileException;

import java.util.UUID;

/**
 * 基于本地磁盘的文件存储实现。
 * 临时文件与目标文件位于同一目录，以支持原子移动。
 */
@Component
public class LocalFileStore implements FileStore {

    @Override
    public Path writeTemporary(Path targetPath, InputStream content) throws IOException {
        Path target = targetPath.toAbsolutePath().normalize();
        Path parent = target.getParent();

        if (parent == null) {
            throw new IOException("目标文件缺少父目录: " + target);
        }

        Files.createDirectories(parent);

        Path temporary = Files.createTempFile(
                parent,
                "." + target.getFileName() + ".",
                ".upload");

        try {
            Files.copy(content, temporary, StandardCopyOption.REPLACE_EXISTING);
            return temporary;
        } catch (IOException exception) {
            Files.deleteIfExists(temporary);
            throw exception;
        }
    }

    @Override
    public void moveAtomically(Path temporaryPath, Path targetPath) throws IOException {
        Path target = targetPath.toAbsolutePath().normalize();

        // 现阶段拒绝覆盖，避免元数据失败时误删原有同名文件。
        if (Files.exists(target)) {
            throw new FileAlreadyExistsException(target.toString());
        }

        Files.move(
                temporaryPath,
                target,
                StandardCopyOption.ATOMIC_MOVE);
    }

    @Override
    public Path stageForDeletion(Path targetPath) throws IOException {
        Path target = targetPath.toAbsolutePath().normalize();

        if (!Files.isRegularFile(target)) {
            throw new NoSuchFileException(target.toString());
        }

        // 隔离文件与原文件位于同一目录，可使用原子移动。
        Path staged = target.resolveSibling(
                "." + target.getFileName()
                        + "." + UUID.randomUUID()
                        + ".delete");

        Files.move(
                target,
                staged,
                StandardCopyOption.ATOMIC_MOVE);

        return staged;
    }

    @Override
    public void restore(Path stagedPath, Path targetPath) throws IOException {
        Path staged = stagedPath.toAbsolutePath().normalize();
        Path target = targetPath.toAbsolutePath().normalize();

        // 不覆盖后来生成的新文件，避免补偿过程破坏其他上传。
        if (Files.exists(target)) {
            throw new FileAlreadyExistsException(target.toString());
        }

        Files.move(
                staged,
                target,
                StandardCopyOption.ATOMIC_MOVE);
    }

    @Override
    public void deleteIfExists(Path path) throws IOException {
        Files.deleteIfExists(path);
    }
}