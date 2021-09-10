package cn.nihility.rpc.client;

import java.io.Serializable;
import java.util.Objects;

public class RpcServiceInfo implements Serializable {

    private static final long serialVersionUID = 497461467903045557L;

    // interface name
    private String serviceName;
    // service version
    private String version;

    public RpcServiceInfo() {
    }

    public RpcServiceInfo(String serviceName, String version) {
        this.serviceName = serviceName;
        this.version = version;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcServiceInfo that = (RpcServiceInfo) o;
        return Objects.equals(serviceName, that.serviceName) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, version);
    }

}
