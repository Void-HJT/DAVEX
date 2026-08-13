package DavexBase.file.port;

import DavexBase.entity.File;

import java.util.Optional;

/**
 * 文件元数据持久化端口。
 * 隔离业务流程与 MyBatis FileMapper，并为失败重试提供幂等写入能力。
 */
public interface FileMetadataRepository {

    /**
     * 根据文件编号查询元数据。
     */
    Optional<File> findById(String fileId);

    /**
     * 新增或更新元数据，重复执行时不产生重复记录。
     */
    void upsert(File metadata);

    /**
     * 根据文件编号删除元数据。
     */
    void deleteById(String fileId);
}