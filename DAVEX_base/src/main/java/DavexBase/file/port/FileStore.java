package DavexBase.file.port;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * 文件存储端口。
 * 统一隔离临时写入、原子移动和失败清理等磁盘操作。
 */
public interface FileStore {

    /**
     * 将文件内容写入目标文件附近的临时文件，并返回临时文件路径。
     */
    Path writeTemporary(Path targetPath, InputStream content) throws IOException;

    /**
     * 将校验通过的临时文件原子移动到正式位置。
     */
    void moveAtomically(Path temporaryPath, Path targetPath) throws IOException;

    /**
     * 将正式文件移动到不可见的隔离位置，返回隔离文件路径。
     */
    Path stageForDeletion(Path targetPath) throws IOException;

    /**
     * 元数据删除失败时，将隔离文件恢复到原位置。
     */
    void restore(Path stagedPath, Path targetPath) throws IOException;

    /**
     * 删除指定文件；文件不存在时不报错。
     */
    void deleteIfExists(Path path) throws IOException;
}