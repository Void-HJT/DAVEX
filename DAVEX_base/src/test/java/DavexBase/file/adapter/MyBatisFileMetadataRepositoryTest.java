package DavexBase.file.adapter;

import DavexBase.entity.File;
import DavexBase.mapper.FileMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 阶段 5.2 元数据适配器测试：相同 UID 的重试必须更新原记录，而不是重复插入。
 */
@ExtendWith(MockitoExtension.class)
class MyBatisFileMetadataRepositoryTest {

    @Mock
    private FileMapper fileMapper;

    @InjectMocks
    private MyBatisFileMetadataRepository repository;

    @Test
    void returnsOptionalMetadata() {
        File metadata = metadata("FILE-1");
        when(fileMapper.selectById("FILE-1")).thenReturn(metadata);

        Optional<File> result = repository.findById("FILE-1");

        assertTrue(result.isPresent());
        assertSame(metadata, result.get());
    }

    @Test
    void insertsMetadataThatDoesNotExist() {
        File metadata = metadata("FILE-1");
        when(fileMapper.selectById("FILE-1")).thenReturn(null);

        repository.upsert(metadata);

        verify(fileMapper).insert(metadata);
        verify(fileMapper, never()).updateById(metadata);
    }

    @Test
    void updatesMetadataThatAlreadyExists() {
        File metadata = metadata("FILE-1");
        when(fileMapper.selectById("FILE-1")).thenReturn(metadata("FILE-1"));

        repository.upsert(metadata);

        verify(fileMapper).updateById(metadata);
        verify(fileMapper, never()).insert(metadata);
    }

    @Test
    void deletesMetadataById() {
        repository.deleteById("FILE-1");

        verify(fileMapper).deleteById("FILE-1");
    }

    private File metadata(String uid) {
        File metadata = new File();
        metadata.setUid(uid);
        return metadata;
    }
}
