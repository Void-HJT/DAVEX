package DveCenter.common;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class R<T> {

    private Integer version; //版本
    private Timestamp timestamp;//时间
    private String sender;//发送者
    private String receiver;//接受者
    private String type;//功能分区
    private String auth;//密钥

    private Body<T> body = new Body<>();//请求体

    public static <T> R<T> success(T object,String msg) {
        R<T> r = new R<T>();
        r.body.setData(object);
        r.body.setMessage(msg);
        r.body.setCode(1);
        return r;
    }

    public static <T> R<T> success(String msg) {
        R<T> r = new R<T>();
        r.body.setMessage(msg);
        r.body.setCode(1);
        return r;
    }

    public static <T> R<T> error(String msg) {
        R r = new R();
        r.body.setMessage(msg);
        r.body.setCode(0);
        return r;
    }

}

