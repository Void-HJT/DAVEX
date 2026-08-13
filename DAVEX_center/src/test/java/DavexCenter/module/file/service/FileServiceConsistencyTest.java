package DavexCenter.module.file.service;

import DavexBase.common.Body;
import DavexBase.common.My;
import DavexCenter.entity.DownloadTask;
import DavexCenter.entity.Output;
import DavexCenter.mapper.DownloadTaskMapper;
import DavexCenter.mapper.OutputMapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 验证 Center 下载副本、下载记录和结果删除保持一致。
 */
@ExtendWith(MockitoExtension.class)
class FileServiceConsistencyTest {

    @BeforeAll
    static void initializeMyBatisMetadata() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant =
                new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, Output.class);
        TableInfoHelper.initTableInfo(assistant, DownloadTask.class);
    }

    @TempDir
    Path temporaryDirectory;

    @Mock
    private OutputMapper outputMapper;
    @Mock
    private DownloadTaskMapper downloadTaskMapper;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private My my;

    @InjectMocks
    private FileService service;

    @Test
    void doesNotCreateDownloadRecordWhenCopyFails() {
        Output output = output(1L, "dataset.csv", temporaryDirectory.resolve("missing.csv"));
        when(outputMapper.selectOne(any())).thenReturn(output);
        when(my.getBase_path()).thenReturn(temporaryDirectory.toString());

        Body<String> result = service.fetchFile(1L, "APP-1");

        assertEquals(0, result.getCode());
        verify(downloadTaskMapper, never()).insert(any());
        verify(downloadTaskMapper, never()).delete(any());
    }

    @Test
    void replacesDownloadRecordOnlyAfterCopySucceeds() throws Exception {
        Path source = temporaryDirectory.resolve("source.csv");
        Files.writeString(source, "new-content");
        Output output = output(1L, "dataset.csv", source);
        when(outputMapper.selectOne(any())).thenReturn(output);
        when(my.getBase_path()).thenReturn(temporaryDirectory.toString());

        Body<String> result = service.fetchFile(1L, "APP-1");

        assertEquals(1, result.getCode());
        Path downloaded = temporaryDirectory
                .resolve("download/APP-1/common/dataset.csv");
        assertEquals("new-content", Files.readString(downloaded));
        InOrder order = inOrder(downloadTaskMapper);
        order.verify(downloadTaskMapper).delete(any());
        order.verify(downloadTaskMapper).insert(any());
    }

    @Test
    void deletingHistoricalResultKeepsCurrentCommonCopy() throws Exception {
        Path source = temporaryDirectory.resolve("source.csv");
        Files.writeString(source, "historical-source");
        Path common = temporaryDirectory
                .resolve("download/APP-1/common/dataset.csv");
        Files.createDirectories(common.getParent());
        Files.writeString(common, "current-copy");

        Output output = output(1L, "dataset.csv", source);
        DownloadTask current = downloadTask(2L);
        when(outputMapper.selectOne(any())).thenReturn(output);
        when(downloadTaskMapper.selectOne(any())).thenReturn(current);
        when(my.getBase_path()).thenReturn(temporaryDirectory.toString());

        Body<String> result = service.deleteFile("APP-1", 1L);

        assertEquals(1, result.getCode());
        assertFalse(Files.exists(source));
        assertTrue(Files.exists(common));
        verify(downloadTaskMapper, never()).delete(any());
    }

    @Test
    void deletingCurrentResultRemovesItsCommonCopyAndRecord() throws Exception {
        Path source = temporaryDirectory.resolve("source.csv");
        Files.writeString(source, "current-source");
        Path common = temporaryDirectory
                .resolve("download/APP-1/common/dataset.csv");
        Files.createDirectories(common.getParent());
        Files.writeString(common, "current-copy");

        Output output = output(1L, "dataset.csv", source);
        when(outputMapper.selectOne(any())).thenReturn(output);
        when(downloadTaskMapper.selectOne(any())).thenReturn(downloadTask(1L));
        when(my.getBase_path()).thenReturn(temporaryDirectory.toString());

        Body<String> result = service.deleteFile("APP-1", 1L);

        assertEquals(1, result.getCode());
        assertFalse(Files.exists(source));
        assertFalse(Files.exists(common));
        verify(downloadTaskMapper).delete(any());
    }

    private Output output(Long uid, String name, Path path) {
        Output output = new Output();
        output.setUid(uid);
        output.setApplicationId("APP-1");
        output.setName(name);
        output.setPath(path.toString());
        return output;
    }

    private DownloadTask downloadTask(Long outputId) {
        DownloadTask task = new DownloadTask();
        task.setOutputId(outputId);
        return task;
    }
}
