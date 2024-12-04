package DavexBase.common;

import lombok.Data;

@Data
public class ContractResponse {
    private boolean success;
    private String message;
    private Object data;

    public static ContractResponse success(Object data) {
        ContractResponse response = new ContractResponse();
        response.setSuccess(true);
        response.setData(data);
        response.setMessage("success");
        return response;
    }

    public static ContractResponse error(String message) {
        ContractResponse response = new ContractResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

}