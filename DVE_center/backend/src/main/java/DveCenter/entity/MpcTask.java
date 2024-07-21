package DveCenter.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class MpcTask {

  @TableId
  private long uid;
  private long applicationId;
  private long centerId;
  private long mpcId;
  private String parameter;
  private long pn;
  private String host;
  private long port;
  private long data;
  private String protocol;


  public long getUid() {
    return uid;
  }

  public void setUid(long uid) {
    this.uid = uid;
  }


  public long getApplicationId() {
    return applicationId;
  }

  public void setApplicationId(long applicationId) {
    this.applicationId = applicationId;
  }


  public long getCenterId() {
    return centerId;
  }

  public void setCenterId(long centerId) {
    this.centerId = centerId;
  }


  public long getMpcId() {
    return mpcId;
  }

  public void setMpcId(long mpcId) {
    this.mpcId = mpcId;
  }


  public String getParameter() {
    return parameter;
  }

  public void setParameter(String parameter) {
    this.parameter = parameter;
  }


  public long getPn() {
    return pn;
  }

  public void setPn(long pn) {
    this.pn = pn;
  }


  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }


  public long getPort() {
    return port;
  }

  public void setPort(long port) {
    this.port = port;
  }


  public long getData() {
    return data;
  }

  public void setData(long data) {
    this.data = data;
  }


  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

}
