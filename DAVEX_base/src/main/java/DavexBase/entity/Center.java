package DavexBase.entity;

import java.time.LocalDateTime;
import java.util.Arrays;

import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class Center {

    @TableId
    private String uid;

    private String name;
    private String ip;
    private int port;
    private byte[] cert;
    private LocalDateTime lastUpdated;
    private String description;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Center other = (Center) obj;
        if (uid == null) {
            if (other.uid != null)
                return false;
        } else if (!uid.equals(other.uid))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (ip == null) {
            if (other.ip != null)
                return false;
        } else if (!ip.equals(other.ip))
            return false;
        if (port != other.port)
            return false;
        if (!Arrays.equals(cert, other.cert))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uid == null) ? 0 : uid.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((ip == null) ? 0 : ip.hashCode());
        result = prime * result + port;
        result = prime * result + Arrays.hashCode(cert);
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        return result;
    }

}
