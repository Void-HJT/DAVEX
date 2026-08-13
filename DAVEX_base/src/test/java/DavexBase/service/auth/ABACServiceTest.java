package DavexBase.service.auth;

import DavexBase.entity.Application;
import DavexBase.entity.File;
import DavexBase.mapper.FileRuleMapper;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ABACServiceTest {

    @Mock
    private FileRuleMapper fileRuleMapper;

    @InjectMocks
    private ABACService abacService;

    private Application application;
    private File file;

    @BeforeEach
    void setUp() {
        application = new Application();
        application.setUid("APP-1");

        file = new File();
        file.setUid("FILE-1");
    }

    @Test
    void deniesAccessWhenFileHasNoRules() {
        when(fileRuleMapper.getRuleExpressions("FILE-1"))
                .thenReturn(List.of());

        boolean allowed = abacService.getAccess(application, file, "read");

        assertFalse(allowed);
    }

    @Test
    void allowsAccessWhenEveryRuleMatches() {
        JSONObject attributes = new JSONObject();
        attributes.put("department", "research");
        application.setAttribute(attributes);

        when(fileRuleMapper.getRuleExpressions("FILE-1"))
                .thenReturn(List.of(
                        "#action == 'read'",
                        "#subjectAttribute['department'] == 'research'"
                ));

        boolean allowed = abacService.getAccess(application, file, "read");

        assertTrue(allowed);
    }

    @Test
    void deniesAccessWhenAnyRuleRejects() {
        when(fileRuleMapper.getRuleExpressions("FILE-1"))
                .thenReturn(List.of(
                        "#action == 'read'",
                        "#action == 'write'"
                ));

        boolean allowed = abacService.getAccess(application, file, "read");

        assertFalse(allowed);
    }

    @Test
    void deniesAccessWhenRuleCannotBeEvaluated() {
        when(fileRuleMapper.getRuleExpressions("FILE-1"))
                .thenReturn(List.of("#missingAttribute.value == 'allowed'"));

        boolean allowed = abacService.getAccess(application, file, "read");

        assertFalse(allowed);
    }

    @Test
    void passActionDoesNotBypassRules() {
        when(fileRuleMapper.getRuleExpressions("FILE-1"))
                .thenReturn(List.of());

        boolean allowed = abacService.getAccess(application, file, "pass");

        assertFalse(allowed);
    }
}
