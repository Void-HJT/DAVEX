package DavexAgent;

import DavexAgent.config.BoostConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.sql.DataSource;

import DavexBase.service.programs.GarnetService;

@SpringBootTest(
        classes = DavexAgentApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "spring.config.name=application-template"
)
class DavexAgentApplicationTest {

    @MockBean
    private DataSource dataSource;

    @MockBean
    private BoostConfig boostConfig;

    @MockBean
    private GarnetService garnetService;

    @Test
    void contextLoads() {
    }
}
