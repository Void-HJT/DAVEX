package DavexCenter.module.envelope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import DavexBase.common.envelope.RequestWrapper;
import DavexBase.common.envelope.ResponseWrapper;

@RestController
@RequestMapping("/Envelope")
public class EnvelopeController {

    @Autowired
    private EnvelopeService envelopeService;
    private static final Logger logger = LoggerFactory.getLogger(EnvelopeController.class);

    @PostMapping("/send")
    public ResponseWrapper postMethodName(@RequestPart("envelope") RequestWrapper wrapper,
            @RequestPart("data") MultipartFile data)
            throws Exception {
        // try {
        // envelopeService.check(wrapper.getRequestEnvelope());
        // } catch (Exception e) {
        // return new ResponseWrapper(envelopeService
        // .failureResponseWithReversedHeader(wrapper.getRequestEnvelope().getHeader(),
        // 400, e.getMessage()));
        // }
        logger.debug(wrapper.toString());
        envelopeService.sharingDispatch(wrapper.getRequestEnvelope().getBody().getSharing(), data);
        return null;
    }

    @PostMapping("/test")
    public RequestWrapper test(@RequestBody RequestWrapper wrapper)
            throws Exception {
        // try {
        // envelopeService.check(wrapper.getRequestEnvelope());
        // } catch (Exception e) {
        // return new ResponseWrapper(envelopeService
        // .failureResponseWithReversedHeader(wrapper.getRequestEnvelope().getHeader(),
        // 400, e.getMessage()));
        // }
        logger.info(wrapper.toString());
        return wrapper;
    }

}
