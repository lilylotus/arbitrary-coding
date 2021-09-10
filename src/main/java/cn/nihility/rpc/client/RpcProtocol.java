package cn.nihility.rpc.client;

import java.util.List;
import java.util.Objects;

public class RpcProtocol {

    private String host;
    private int port;

    // service info list
    private List<RpcServiceInfo> serviceInfoList;

    public RpcProtocol() {
    }

    public RpcProtocol(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcProtocol that = (RpcProtocol) o;
        return port == that.port &&
                Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<RpcServiceInfo> getServiceInfoList() {
        return serviceInfoList;
    }

    public void setServiceInfoList(List<RpcServiceInfo> serviceInfoList) {
        this.serviceInfoList = serviceInfoList;
    }

    @Override
    public String toString() {
        return "RpcProtocol{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }

}
