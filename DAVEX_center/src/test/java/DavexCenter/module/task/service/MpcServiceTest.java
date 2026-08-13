package DavexCenter.module.task.service;

import DavexBase.common.My;
import DavexBase.entity.Mpc;
import DavexBase.mapper.MpcMapper;
import DavexCenter.module.task.port.AgentMpcArtifactClient;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MpcServiceTest {

    @Mock
    private AgentMpcArtifactClient artifactClient;

    @Mock
    private My my;

    @Mock
    private MpcMapper mpcMapper;

    @InjectMocks
    private MpcService service;

    @Test
    void downloadsAgentArtifactAndPersistsItsLocalPath(@TempDir Path tempDir)
            throws Exception {
        Mpc mpc = new Mpc();
        mpc.setUid("MPC-1");
        mpc.setName("demo");
        byte[] program = new byte[] {1, 2, 3};
        when(my.getBase_path()).thenReturn(tempDir.toString());
        when(artifactClient.fetchArtifact("AGENT-1", "MPC-1"))
                .thenReturn(new AgentMpcArtifactClient.Artifact(
                        mpc, "demo.mpc", program));

        service.downloadMPC("AGENT-1", "MPC-1");

        Path saved = tempDir.resolve("programs/demo.mpc");
        assertArrayEquals(program, Files.readAllBytes(saved));
        assertEquals(Path.of("programs/demo.mpc").toString(), mpc.getPath());
        verify(mpcMapper).insert(mpc);
    }
}
