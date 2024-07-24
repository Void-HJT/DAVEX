package DveAgent.common;

import lombok.Data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class R<T> implements Serializable {

    private Integer version; // 版本
    // private Timestamp timestamp;// 时间
    private String sender;// 发送者
    private String receiver;// 接受者
    private String type;// 功能分区
    private transient String auth;// 密钥
    private LocalDateTime timestamp;

    private Body<T> body = new Body<>();// 请求体

    public static <T> R<T> success(T object, String msg) {
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

    public static R<String> error(String msg) {
        R<String> r = new R<String>();
        r.body.setMessage(msg);
        r.body.setCode(0);
        return r;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject(); // 默认序列化所有非transient字段
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject(); // 默认反序列化所有非transient字段
    }

    // 将对象序列化为字节数组
    public static byte[] serialize(R<?> obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        }
    }

    // 从字节数组反序列化对象
    public static R<?> deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (R<?>) ois.readObject();
        }
    }

}
