package cn.nihility.http.constant;

/**
 * SSL 协议版本
 */
public enum SSLProtocolVersion {
    SSL("SSL"),
    SSLv3("SSLv3"),
    TLSv1("TLSv1"),
    TLSv1_1("TLSv1.1"),
    TLSv1_2("TLSv1.2");

    private String name;

    private SSLProtocolVersion(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public static SSLProtocolVersion find(String name){
        for (SSLProtocolVersion pv : SSLProtocolVersion.values()) {
            if(pv.getName().toUpperCase().equals(name.toUpperCase())){
                return pv;
            }
        }
        throw new RuntimeException("未支持当前 ssl 版本号："+name);
    }

}
