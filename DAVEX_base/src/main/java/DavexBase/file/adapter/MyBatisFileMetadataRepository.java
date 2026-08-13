package DavexBase.file.adapter;

import DavexBase.entity.File;
import DavexBase.file.port.FileMetadataRepository;
import DavexBase.mapper.FileMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 基于 MyBatis 的文件元数据适配器。
 * 相同 UID 再次写入时更新原记录，避免重试产生重复数据。
 */
@Repository
public class MyBatisFileMetadataRepository implements FileMetadataRepository {

    private final FileMapper fileMapper;

    public MyBatisFileMetadataRepository(FileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    @Override
    public Optional<File> findById(String fileId) {
        return Optional.ofNullable(fileMapper.selectById(fileId));
    }

    @Override
    public void upsert(File metadata) {
        if (fileMapper.selectById(metadata.getUid()) == null) {
            fileMapper.insert(metadata);
        } else {
            fileMapper.updateById(metadata);
        }
    }

    @Override
    public void deleteById(String fileId) {
        fileMapper.deleteById(fileId);
    }
}