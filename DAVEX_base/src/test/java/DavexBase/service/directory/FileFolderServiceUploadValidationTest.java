package DavexBase.service.directory;

import DavexBase.common.Body;
import DavexBase.common.My;
import DavexBase.entity.Center;
import DavexBase.entity.File;
import DavexBase.entity.Folder;
import DavexBase.file.service.AtomicFileDeleteService;
import DavexBase.file.service.AtomicFileUploadService;
import DavexBase.file.service.SafeFilePathResolver;
import DavexBase.mapper.AgentMapper;
import DavexBase.mapper.CenterMapper;
import DavexBase.mapper.FileMapper;
import DavexBase.mapper.FolderMapper;
import DavexBase.service.auth.AgentWebClientService;
import DavexBase.service.auth.CenterWebClientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 阶段 5.4 上传入口测试：空文件、同名覆盖和远端同步失败均有明确行为。
 */
@ExtendWith(MockitoExtension.class)
class FileFolderServiceUploadValidationTest {

    @Mock private FolderMapper folderMapper;
    @Mock private CenterMapper centerMapper;
    @Mock private FileMapper fileMapper;
    @Mock private AgentMapper agentMapper;
    @Mock private AgentWebClientService agentWebClientService;
    @Mock private CenterWebClientService centerWebClientService;
    @Mock private My my;
    @Mock private AtomicFileUploadService atomicFileUploadService;
    @Mock private AtomicFileDeleteService atomicFileDeleteService;
    @Mock private SafeFilePathResolver safeFilePathResolver;

    @InjectMocks
    private FileFolderService service;

    @Test
    void rejectsEmptyUploadBeforeAccessingDatabaseOrDisk() throws Exception {
        MockMultipartFile empty = new MockMultipartFile(
                "file", "empty.csv", "text/csv", new byte[0]);

        Body<String> result = service.uploadFile(
                "AGENT-1", "FOLDER-1", empty, "/data", 0);

        assertEquals(0, result.getCode());
        assertTrue(result.getMessage().contains("不能为空"));
        verify(folderMapper, never()).selectOne(any());
        verify(atomicFileUploadService, never()).upload(any(), any(), any());
    }

    @Test
    void replacesSameNameWhileKeepingUidAndCreationTime() throws Exception {
        Folder folder = folder("FOLDER-1", "input");
        MockMultipartFile upload = file("dataset.csv");
        Timestamp createdAt = Timestamp.valueOf("2026-07-24 10:00:00");
        File existing = new File();
        existing.setUid("AGENT-1-D7");
        existing.setCreateDate(createdAt);
        when(folderMapper.selectOne(any())).thenReturn(folder);
        when(fileMapper.selectOne(any())).thenReturn(existing);
        when(safeFilePathResolver.resolve(
                eq(Path.of("/data")), any(), eq("dataset.csv")))
                .thenReturn(Path.of("/data/input/dataset.csv"));
        when(centerMapper.selectList(any())).thenReturn(List.of());

        Body<String> result = service.uploadFile(
                "AGENT-1", "FOLDER-1", upload, "/data", 0);

        assertEquals(1, result.getCode());
        verify(atomicFileUploadService, never()).upload(any(), any(), any());
        org.mockito.ArgumentCaptor<File> metadata =
                org.mockito.ArgumentCaptor.forClass(File.class);
        verify(atomicFileUploadService).replace(
                eq(Path.of("/data/input/dataset.csv")),
                any(),
                metadata.capture());
        assertEquals("AGENT-1-D7", metadata.getValue().getUid());
        assertEquals(createdAt, metadata.getValue().getCreateDate());
    }

    @Test
    void keepsSuccessfulLocalUploadWhenRemoteMetadataSyncFails() throws Exception {
        Folder folder = folder("FOLDER-1", "input");
        MockMultipartFile upload = file("dataset.csv");
        Center remoteCenter = new Center();
        remoteCenter.setUid("CENTER-REMOTE");

        when(folderMapper.selectOne(any())).thenReturn(folder);
        when(fileMapper.selectOne(any())).thenReturn(null);
        when(fileMapper.selectObjs(any())).thenReturn(List.of());
        when(safeFilePathResolver.resolve(
                eq(Path.of("/data")), any(), eq("dataset.csv")))
                .thenReturn(Path.of("/data/input/dataset.csv"));
        when(centerMapper.selectList(any())).thenReturn(List.of(remoteCenter));
        when(my.getId()).thenReturn("CENTER-LOCAL");
        when(agentWebClientService.agent2CenterWebClient("CENTER-REMOTE"))
                .thenThrow(new IOException("remote unavailable"));

        Body<String> result = service.uploadFile(
                "AGENT-1", "FOLDER-1", upload, "/data", 0);

        assertEquals(1, result.getCode());
        verify(atomicFileUploadService).upload(
                eq(Path.of("/data/input/dataset.csv")), any(), any());
    }

    @Test
    void fullMetadataResyncDoesNotDuplicateAnExistingUid() {
        File metadata = new File();
        metadata.setUid("FILE-1");
        when(fileMapper.selectOne(any())).thenReturn(metadata);

        Body<String> result = service.syncFiles(List.of(metadata));

        assertEquals(1, result.getCode());
        verify(fileMapper, never()).insert(any());
    }

    private Folder folder(String uid, String name) {
        Folder folder = new Folder();
        folder.setUid(uid);
        folder.setName(name);
        folder.setParentId("-1");
        return folder;
    }

    private MockMultipartFile file(String name) {
        return new MockMultipartFile(
                "file", name, "text/csv", "id,value\n1,42".getBytes());
    }
}
