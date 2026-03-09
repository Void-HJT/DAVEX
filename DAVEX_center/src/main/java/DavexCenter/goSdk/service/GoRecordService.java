package DavexCenter.goSdk.service;

import DavexCenter.goSdk.entity.GoDidRecord;
import DavexCenter.goSdk.entity.GoPrivacyGroupRecord;
import DavexCenter.goSdk.entity.GoPrivacyVpRecord;
import DavexCenter.goSdk.entity.GoVcRecord;
import DavexCenter.mapper.GoDidRecordMapper;
import DavexCenter.mapper.GoPrivacyGroupRecordMapper;
import DavexCenter.mapper.GoPrivacyVpRecordMapper;
import DavexCenter.mapper.GoVcRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class GoRecordService {
    private static final Logger logger = LoggerFactory.getLogger(GoRecordService.class);

    private final GoDidRecordMapper didRecordMapper;
    private final GoVcRecordMapper vcRecordMapper;
    private final GoPrivacyGroupRecordMapper groupRecordMapper;
    private final GoPrivacyVpRecordMapper privacyVpRecordMapper;
    private final ObjectMapper objectMapper;

    public GoRecordService(GoDidRecordMapper didRecordMapper,
                           GoVcRecordMapper vcRecordMapper,
                           GoPrivacyGroupRecordMapper groupRecordMapper,
                           GoPrivacyVpRecordMapper privacyVpRecordMapper,
                           ObjectMapper objectMapper) {
        this.didRecordMapper = didRecordMapper;
        this.vcRecordMapper = vcRecordMapper;
        this.groupRecordMapper = groupRecordMapper;
        this.privacyVpRecordMapper = privacyVpRecordMapper;
        this.objectMapper = objectMapper;
    }

    public void saveDidRegistration(JsonNode registerRequest, JsonNode goData) {
        JsonNode didDocument = registerRequest == null ? null : registerRequest.get("didDocument");
        String did = firstNonBlank(
                text(goData, "did"),
                text(didDocument, "id")
        );
        if (did == null) {
            return;
        }

        GoDidRecord record = findDidByDid(did);
        if (record == null) {
            record = new GoDidRecord();
            record.setDid(did);
        }
        if (didDocument != null && !didDocument.isNull() && !didDocument.isMissingNode()) {
            record.setDidDocument(toJson(didDocument));
        }
        record.setTxId(text(goData, "txId"));
        record.setBlockHeight(longValue(goData, "blockHeight"));
        touch(record);
        upsertDid(record);
    }

    public void upsertDidFromQuery(String didParam, JsonNode goData) {
        String did = firstNonBlank(text(goData, "did"), didParam);
        if (did == null) {
            return;
        }
        GoDidRecord record = findDidByDid(did);
        if (record == null) {
            record = new GoDidRecord();
            record.setDid(did);
        }

        JsonNode resultNode = goData == null ? null : goData.get("result");
        if (resultNode != null && !resultNode.isNull()) {
            String resultText = resultNode.isTextual() ? resultNode.asText() : toJson(resultNode);
            record.setChainResult(resultText);

            JsonNode parsed = parseMaybeJson(resultText);
            if (parsed != null) {
                JsonNode possibleDoc = parsed.has("id") ? parsed : parsed.get("didDocument");
                if (possibleDoc != null && !possibleDoc.isMissingNode() && !possibleDoc.isNull()) {
                    record.setDidDocument(toJson(possibleDoc));
                }
            }
        }
        touch(record);
        upsertDid(record);
    }

    public void saveVcIssue(JsonNode goData) {
        JsonNode vc = goData == null ? null : goData.get("vc");
        if (vc == null || vc.isNull() || vc.isMissingNode()) {
            return;
        }

        String vcId = text(vc, "id");
        GoVcRecord record = vcId == null ? null : findVcById(vcId);
        if (record == null) {
            record = new GoVcRecord();
            record.setVcId(vcId);
        }
        record.setIssuerDid(text(vc, "issuer"));
        record.setHolderDid(extractVcHolderDid(vc));
        record.setCredentialType(extractCredentialType(vc.path("type")));
        record.setVcJson(toJson(vc));
        record.setTxId(text(goData, "txId"));
        record.setBlockHeight(longValue(goData, "blockHeight"));
        touch(record);
        upsertVc(record);
    }

    public void saveVcVerify(JsonNode verifyRequest, JsonNode goData) {
        JsonNode vc = verifyRequest == null ? null : verifyRequest.get("vc");
        if (vc == null || vc.isNull() || vc.isMissingNode()) {
            return;
        }

        String vcId = text(vc, "id");
        if (vcId == null) {
            return;
        }
        GoVcRecord record = findVcById(vcId);
        if (record == null) {
            record = new GoVcRecord();
            record.setVcId(vcId);
            record.setIssuerDid(text(vc, "issuer"));
            record.setHolderDid(extractVcHolderDid(vc));
            record.setCredentialType(extractCredentialType(vc.path("type")));
            record.setVcJson(toJson(vc));
        }
        record.setLatestVerifyValid(boolToInt(boolValue(goData, "valid")));
        touch(record);
        upsertVc(record);
    }

    public void saveGroupCreate(JsonNode createRequest, JsonNode goData) {
        String groupId = firstNonBlank(text(goData, "groupId"), text(createRequest, "groupId"));
        if (groupId == null) {
            return;
        }

        GoPrivacyGroupRecord record = findGroupByGroupId(groupId);
        if (record == null) {
            record = new GoPrivacyGroupRecord();
            record.setGroupId(groupId);
            record.setMemberCount(0);
            record.setMemberPublicKeysJson("[]");
        }
        record.setGroupName(firstNonBlank(text(createRequest, "groupName"), record.getGroupName()));
        record.setIssuerDid(firstNonBlank(text(createRequest, "issuerDid"), record.getIssuerDid()));
        record.setCredentialType(firstNonBlank(text(createRequest, "credentialType"), record.getCredentialType()));
        record.setAttributePolicy(firstNonBlank(text(createRequest, "attributePolicy"), record.getAttributePolicy()));
        record.setMinRingSize(intValue(createRequest, "minRingSize"));
        record.setTxId(text(goData, "txId"));
        record.setBlockHeight(longValue(goData, "blockHeight"));
        touch(record);
        upsertGroup(record);
    }

    public void saveGroupMember(JsonNode addMemberRequest, JsonNode goData) {
        String groupId = firstNonBlank(text(goData, "groupId"), text(addMemberRequest, "groupId"));
        String publicKeyHex = text(addMemberRequest, "publicKeyHex");
        if (groupId == null || publicKeyHex == null) {
            return;
        }

        GoPrivacyGroupRecord record = findGroupByGroupId(groupId);
        if (record == null) {
            record = new GoPrivacyGroupRecord();
            record.setGroupId(groupId);
            record.setMemberPublicKeysJson("[]");
            record.setMemberCount(0);
        }

        Set<String> keys = new LinkedHashSet<>(parseStringList(record.getMemberPublicKeysJson()));
        keys.add(publicKeyHex);
        record.setMemberPublicKeysJson(toJson(keys));
        record.setMemberCount(keys.size());
        record.setTxId(firstNonBlank(text(goData, "txId"), record.getTxId()));
        if (longValue(goData, "blockHeight") != null) {
            record.setBlockHeight(longValue(goData, "blockHeight"));
        }
        touch(record);
        upsertGroup(record);
    }

    public void upsertGroupFromQuery(JsonNode groupData) {
        String groupId = text(groupData, "groupId");
        if (groupId == null) {
            return;
        }

        GoPrivacyGroupRecord record = findGroupByGroupId(groupId);
        if (record == null) {
            record = new GoPrivacyGroupRecord();
            record.setGroupId(groupId);
        }
        record.setGroupName(firstNonBlank(text(groupData, "groupName"), record.getGroupName()));
        record.setIssuerDid(firstNonBlank(text(groupData, "issuerDid"), record.getIssuerDid()));
        record.setCredentialType(firstNonBlank(text(groupData, "credentialType"), record.getCredentialType()));
        record.setAttributePolicy(firstNonBlank(text(groupData, "attributePolicy"), record.getAttributePolicy()));
        record.setMinRingSize(intValue(groupData, "minRingSize"));
        record.setMemberCount(intValue(groupData, "memberCount"));

        JsonNode memberKeys = groupData.get("memberPublicKeys");
        if (memberKeys != null && memberKeys.isArray()) {
            record.setMemberPublicKeysJson(toJson(memberKeys));
        }
        touch(record);
        upsertGroup(record);
    }

    public void savePrivacyVpGenerate(JsonNode generateRequest, JsonNode goData) {
        if (goData == null || goData.isNull() || goData.isMissingNode()) {
            return;
        }
        String vpId = text(goData, "id");
        if (vpId == null) {
            return;
        }

        GoPrivacyVpRecord record = findPrivacyVpById(vpId);
        if (record == null) {
            record = new GoPrivacyVpRecord();
            record.setVpId(vpId);
        }
        record.setGroupId(firstNonBlank(text(goData, "groupId"), text(generateRequest, "groupId")));
        record.setHolderKeyImage(text(goData, "holderKeyImage"));
        record.setCredentialType(firstNonBlank(text(goData, "credentialType"), text(generateRequest, "credentialType")));
        record.setIssuerDid(firstNonBlank(text(goData, "issuerDid"), text(generateRequest, "issuerDid")));
        record.setChallenge(firstNonBlank(text(goData, "challenge"), text(generateRequest, "challenge")));
        record.setVpJson(toJson(goData));
        touch(record);
        upsertPrivacyVp(record);
    }

    public void savePrivacyVpVerify(JsonNode verifyRequest, JsonNode goData, boolean localVerify) {
        JsonNode privacyVp = verifyRequest == null ? null : verifyRequest.get("privacyVp");
        String vpId = text(privacyVp, "id");
        if (vpId == null) {
            return;
        }

        GoPrivacyVpRecord record = findPrivacyVpById(vpId);
        if (record == null) {
            record = new GoPrivacyVpRecord();
            record.setVpId(vpId);
            record.setGroupId(text(privacyVp, "groupId"));
            record.setHolderKeyImage(text(privacyVp, "holderKeyImage"));
            record.setCredentialType(text(privacyVp, "credentialType"));
            record.setIssuerDid(text(privacyVp, "issuerDid"));
            record.setChallenge(firstNonBlank(text(privacyVp, "challenge"), text(verifyRequest, "challenge")));
            if (privacyVp != null && !privacyVp.isNull() && !privacyVp.isMissingNode()) {
                record.setVpJson(toJson(privacyVp));
            }
        }

        Integer validValue = boolToInt(boolValue(goData, "valid"));
        if (localVerify) {
            record.setLocalVerifyValid(validValue);
        } else {
            record.setChainVerifyValid(validValue);
        }
        if (goData != null && !goData.isNull() && !goData.isMissingNode()) {
            record.setVerifyResultJson(toJson(goData));
        }
        touch(record);
        upsertPrivacyVp(record);
    }

    public List<GoDidRecord> listDidRecords() {
        LambdaQueryWrapper<GoDidRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(GoDidRecord::getUpdatedAt);
        return didRecordMapper.selectList(wrapper);
    }

    public List<GoVcRecord> listVcRecords(String holderDid, String issuerDid) {
        LambdaQueryWrapper<GoVcRecord> wrapper = new LambdaQueryWrapper<>();
        if (holderDid != null && !holderDid.isBlank()) {
            wrapper.eq(GoVcRecord::getHolderDid, holderDid);
        }
        if (issuerDid != null && !issuerDid.isBlank()) {
            wrapper.eq(GoVcRecord::getIssuerDid, issuerDid);
        }
        wrapper.orderByDesc(GoVcRecord::getUpdatedAt);
        return vcRecordMapper.selectList(wrapper);
    }

    public List<GoPrivacyGroupRecord> listGroupRecords(String groupId) {
        LambdaQueryWrapper<GoPrivacyGroupRecord> wrapper = new LambdaQueryWrapper<>();
        if (groupId != null && !groupId.isBlank()) {
            wrapper.eq(GoPrivacyGroupRecord::getGroupId, groupId);
        }
        wrapper.orderByDesc(GoPrivacyGroupRecord::getUpdatedAt);
        return groupRecordMapper.selectList(wrapper);
    }

    public List<GoPrivacyVpRecord> listPrivacyVpRecords(String groupId, String holderKeyImage) {
        LambdaQueryWrapper<GoPrivacyVpRecord> wrapper = new LambdaQueryWrapper<>();
        if (groupId != null && !groupId.isBlank()) {
            wrapper.eq(GoPrivacyVpRecord::getGroupId, groupId);
        }
        if (holderKeyImage != null && !holderKeyImage.isBlank()) {
            wrapper.eq(GoPrivacyVpRecord::getHolderKeyImage, holderKeyImage);
        }
        wrapper.orderByDesc(GoPrivacyVpRecord::getUpdatedAt);
        return privacyVpRecordMapper.selectList(wrapper);
    }

    private GoDidRecord findDidByDid(String did) {
        LambdaQueryWrapper<GoDidRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoDidRecord::getDid, did).last("limit 1");
        return didRecordMapper.selectOne(wrapper);
    }

    private GoVcRecord findVcById(String vcId) {
        LambdaQueryWrapper<GoVcRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoVcRecord::getVcId, vcId).last("limit 1");
        return vcRecordMapper.selectOne(wrapper);
    }

    private GoPrivacyGroupRecord findGroupByGroupId(String groupId) {
        LambdaQueryWrapper<GoPrivacyGroupRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoPrivacyGroupRecord::getGroupId, groupId).last("limit 1");
        return groupRecordMapper.selectOne(wrapper);
    }

    private GoPrivacyVpRecord findPrivacyVpById(String vpId) {
        LambdaQueryWrapper<GoPrivacyVpRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoPrivacyVpRecord::getVpId, vpId).last("limit 1");
        return privacyVpRecordMapper.selectOne(wrapper);
    }

    private void upsertDid(GoDidRecord record) {
        if (record.getUid() == null) {
            didRecordMapper.insert(record);
            return;
        }
        didRecordMapper.updateById(record);
    }

    private void upsertVc(GoVcRecord record) {
        if (record.getUid() == null) {
            vcRecordMapper.insert(record);
            return;
        }
        vcRecordMapper.updateById(record);
    }

    private void upsertGroup(GoPrivacyGroupRecord record) {
        if (record.getUid() == null) {
            groupRecordMapper.insert(record);
            return;
        }
        groupRecordMapper.updateById(record);
    }

    private void upsertPrivacyVp(GoPrivacyVpRecord record) {
        if (record.getUid() == null) {
            privacyVpRecordMapper.insert(record);
            return;
        }
        privacyVpRecordMapper.updateById(record);
    }

    private void touch(GoDidRecord record) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(now);
        }
        record.setUpdatedAt(now);
    }

    private void touch(GoVcRecord record) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(now);
        }
        record.setUpdatedAt(now);
    }

    private void touch(GoPrivacyGroupRecord record) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(now);
        }
        record.setUpdatedAt(now);
    }

    private void touch(GoPrivacyVpRecord record) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(now);
        }
        record.setUpdatedAt(now);
    }

    private String extractVcHolderDid(JsonNode vcNode) {
        JsonNode subject = vcNode == null ? null : vcNode.path("credentialSubject");
        if (subject == null || subject.isMissingNode() || subject.isNull()) {
            return null;
        }
        return firstNonBlank(
                text(subject, "id"),
                text(subject, "did"),
                text(subject, "holderDid")
        );
    }

    private String extractCredentialType(JsonNode typeNode) {
        if (typeNode == null || typeNode.isNull() || typeNode.isMissingNode()) {
            return null;
        }
        if (typeNode.isTextual()) {
            return typeNode.asText();
        }
        if (!typeNode.isArray() || typeNode.size() == 0) {
            return null;
        }
        if (typeNode.size() == 1) {
            return typeNode.get(0).asText();
        }
        for (JsonNode n : typeNode) {
            String value = n.asText();
            if (!"VerifiableCredential".equalsIgnoreCase(value)) {
                return value;
            }
        }
        return typeNode.get(typeNode.size() - 1).asText();
    }

    private JsonNode parseMaybeJson(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        if (!(trimmed.startsWith("{") || trimmed.startsWith("["))) {
            return null;
        }
        try {
            return objectMapper.readTree(trimmed);
        } catch (JsonProcessingException e) {
            logger.warn("parse maybe json failed: {}", e.getMessage());
            return null;
        }
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            logger.warn("serialize json failed: {}", e.getMessage());
            return null;
        }
    }

    private List<String> parseStringList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            logger.warn("parse string list failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private static String text(JsonNode node, String field) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        JsonNode value = field == null ? node : node.get(field);
        if (value == null || value.isNull() || value.isMissingNode()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? null : text;
    }

    private static Integer intValue(JsonNode node, String field) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.isMissingNode()) {
            return null;
        }
        if (value.isInt() || value.isLong()) {
            return value.asInt();
        }
        if (value.isTextual()) {
            try {
                return Integer.parseInt(value.asText());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Long longValue(JsonNode node, String field) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.isMissingNode()) {
            return null;
        }
        if (value.isLong() || value.isInt()) {
            return value.asLong();
        }
        if (value.isTextual()) {
            try {
                return Long.parseLong(value.asText());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static Boolean boolValue(JsonNode node, String field) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.isMissingNode()) {
            return null;
        }
        if (value.isBoolean()) {
            return value.asBoolean();
        }
        if (value.isTextual()) {
            return Boolean.parseBoolean(value.asText());
        }
        return null;
    }

    private static Integer boolToInt(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? 1 : 0;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
