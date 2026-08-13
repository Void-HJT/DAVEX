package DavexAgent.module.task.service;

import DavexAgent.module.task.port.CenterMpcArtifactClient;
import DavexBase.common.My;
import DavexBase.entity.Mpc;
import DavexBase.mapper.MpcMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class MpcServiceTest {

    @Mock
    private CenterMpcArtifactClient artifactClient;

    @Mock
    private My my;

    @Mock
    private MpcMapper mpcMapper;

    @InjectMocks
    private MpcService service;

    @Test
    void downloadsCenterArtifactAndPersistsItsLocalPath(@TempDir Path tempDir)
            throws Exception {
        Mpc mpc = new Mpc();
        mpc.setUid("MPC-1");
        mpc.setName("demo");
        byte[] program = new byte[] {1, 2, 3};
        when(my.getBase_path()).thenReturn(tempDir.toString());
        when(artifactClient.fetchArtifact("CENTER-1", "MPC-1"))
                .thenReturn(new CenterMpcArtifactClient.Artifact(
                        mpc, "demo.mpc", program));

        service.downloadMPC("CENTER-1", "MPC-1");

        Path saved = tempDir.resolve("programs/demo.mpc");
        assertArrayEquals(program, Files.readAllBytes(saved));
        assertEquals(Path.of("programs/demo.mpc").toString(), mpc.getPath());
        verify(mpcMapper).insert(mpc);
    }

    @Test
    void updatesMetadataWhenRedownloadingMissingLocalArtifact(
            @TempDir Path tempDir) throws Exception {
        Mpc existing = new Mpc();
        existing.setUid("MPC-1");
        existing.setPath(Path.of("programs/missing.mpc").toString());

        Mpc downloaded = new Mpc();
        downloaded.setUid("MPC-1");
        downloaded.setName("demo");
        byte[] program = new byte[] {4, 5, 6};

        when(mpcMapper.selectById("MPC-1")).thenReturn(existing);
        when(my.getBase_path()).thenReturn(tempDir.toString());
        when(artifactClient.fetchArtifact("CENTER-1", "MPC-1"))
                .thenReturn(new CenterMpcArtifactClient.Artifact(
                        downloaded, "demo.mpc", program));

        service.downloadMPC("CENTER-1", "MPC-1");

        assertArrayEquals(program, Files.readAllBytes(
                tempDir.resolve("programs/demo.mpc")));
        assertEquals(
                Path.of("programs/demo.mpc").toString(),
                downloaded.getPath());
        verify(mpcMapper).updateById(downloaded);
        verify(mpcMapper, never()).insert(any());
    }
}
