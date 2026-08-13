package DavexCenter;

import DavexCenter.config.BoostConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.sql.DataSource;

import DavexBase.service.programs.GarnetService;

@SpringBootTest(
        classes = DavexCenterApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.config.name=application-template",
                "my.base_path=${java.io.tmpdir}/davex-center-test-data"
        }
)
class DavexCenterApplicationTest {

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
