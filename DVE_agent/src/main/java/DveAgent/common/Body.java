package DveAgent.common;

import java.io.Serializable;

import lombok.Data;

@Data
public class Body<T> implements Serializable{
    private String method;
    private int code;
    private String message;
    private T data;

    public static <T> Body<T> success(T object,String msg) {
        Body<T> body = new Body<>();
        body.setData(object);
        body.setMessage(msg);
        body.setCode(1);
        return body;
    }

    public static <T> Body<T> success(String msg) {
        Body<T> body = new Body<>();
        body.setMessage(msg);
        body.setCode(1);
        return body;
    }

    public static <T> Body<T> error(T object,String msg) {
        Body<T> body = new Body<>();
        body.setData(object);
        body.setMessage(msg);
        body.setCode(0);
        return body;
    }

    public static <T> Body<T> error(String msg) {
        Body<T> body = new Body<>();
        body.setMessage(msg);
        body.setCode(0);
        return body;
    }
}
