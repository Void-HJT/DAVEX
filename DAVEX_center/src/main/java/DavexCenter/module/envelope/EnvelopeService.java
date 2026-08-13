package DavexCenter.module.envelope;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.dsg.davex.contract.mpc.MpcTaskCreateRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import DavexBase.common.My;
import DavexBase.common.envelope.Audit;
import DavexBase.common.envelope.Header;
import DavexBase.common.envelope.RequestEnvelope;
import DavexBase.common.envelope.ResponseBody;
import DavexBase.common.envelope.ResponseEnvelope;
import DavexBase.common.envelope.Results;
import DavexBase.common.envelope.Sharing;
import DavexBase.info.ComparisonInfo;
import DavexBase.info.FileExchangeInfo;
import DavexBase.info.InferenceInfo;
import DavexBase.info.QueryInfo;

import DavexCenter.module.comparison.ComparisonController;
import DavexCenter.module.file.controller.FileController;
import DavexCenter.module.query.controller.DatabaseController;
import DavexCenter.module.task.controller.MpcTaskController;
import DavexCenter.module.task.controller.SecretFlowController;
import DavexCenter.module.task.controller.SecureInferenceController;

@Service
public class EnvelopeService {

    @Autowired
    private My my;

    @Autowired
    private FileController fileController;

    @Autowired(required = false)
    private SecureInferenceController secureInferenceController;

    @Autowired
    private ComparisonController comparisonController;

    @Autowired(required = false)
    private MpcTaskController mpcTaskController;

    @Autowired
    private DatabaseController databaseController;

    @Autowired
    private SecretFlowController secretFlowController;

    @Autowired
    private ObjectMapper objectMapper;

    public void check(RequestEnvelope requestEnvelope) throws Exception {
        if (!requestEnvelope.verifyHash()) {
            throw new Exception("Hash verification failed");
        }
        if (!requestEnvelope.getHeader().getReceiver().equals(my.getId())) {
            throw new Exception("Receiver verification failed");
        }
        if (!requestEnvelope.getHeader().getVersion().equals(my.getVersion())) {
            throw new Exception("Version verification failed");
        }
    }

    public ResponseEnvelope failureResponseWithReversedHeader(Header header, int statusCode, String message)
            throws Exception {
        ResponseEnvelope responseEnvelope = new ResponseEnvelope();
        responseEnvelope.setHeader(fromReversedHeader(header));
        responseEnvelope.setBody(new ResponseBody(statusCode, message));
        responseEnvelope.getHeader().generateHash_Response(responseEnvelope.getBody());
        return responseEnvelope;
    }

    public Header fromReversedHeader(Header others) {
        Header header = new Header();
        header.setVersion(my.getVersion());
        header.setTimeStamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        header.setRequestId(others.getRequestId());
        header.setSender(others.getReceiver());
        header.setReceiver(others.getSender());
        return header;
    }

    public void sharingDispatch(Sharing sharing, MultipartFile data) throws Exception {
        switch (sharing.getType()) {
            case FILE_EXCHANGE:
                FileExchangeInfo fileExchangeInfo = sharing.getSetting().toJavaObject(FileExchangeInfo.class);
                fileController.getFile(fileExchangeInfo.getFileId(), fileExchangeInfo.getAgentId(),
                        fileExchangeInfo.getFolderId(), fileExchangeInfo.getFileId());
                break;
            case COMPARISON:
                ComparisonInfo comparisonInfo = sharing.getSetting().toJavaObject(ComparisonInfo.class);
                if (data != null) {
                    String data_name = data.getOriginalFilename();
                    if (data_name != null) {
                        if (data_name.endsWith(".csv")) {
                            comparisonController.compareFromCsv(comparisonInfo.getApplicationId(),
                                    comparisonInfo.getAgentId(),
                                    comparisonInfo.getFileId(), comparisonInfo.getFolderId(), data);
                            return;
                        } else if (data_name.endsWith(".txt")) {
                            comparisonController.compareFromTXT(comparisonInfo.getApplicationId(),
                                    comparisonInfo.getAgentId(),
                                    comparisonInfo.getFileId(), comparisonInfo.getFolderId(), data);
                            return;
                        }
                    }
                } else {
                    comparisonController.compare(comparisonInfo.getApplicationId(), comparisonInfo.getAgentId(),
                            comparisonInfo.getFileId(), comparisonInfo.getFolderId(), comparisonInfo.getAttributes(),
                            comparisonInfo.getValuesList());
                }
                break;
            case QUERY:
                var queryInfo = sharing.getSetting().toJavaObject(QueryInfo.class);
                databaseController.query2Agent(queryInfo.getRequest(), queryInfo.getApplicationId(),
                        queryInfo.getAgentId(), queryInfo.getDatabaseId());
                break;
            case MPC:
            case PSI:
                mpcTaskController.createWithInput(
                        data,
                        objectMapper.readValue(
                                sharing.getSetting().toJSONString(),
                                MpcTaskCreateRequest.class));
                break;
            case SECURITY_INFERENCE:
                secureInferenceController.create(data, sharing.getSetting().toJavaObject(InferenceInfo.class));
                break;
            case FEDERATED_LEARNING:
                // TODO
                break;
            default:
                break;
        }
    }

    public void resultsDispatch(Results sharing) {

    }

    public void auditDispatch(Audit sharing) {

    }

}
