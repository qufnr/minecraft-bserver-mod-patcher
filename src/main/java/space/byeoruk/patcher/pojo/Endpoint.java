package space.byeoruk.patcher.pojo;

public class Endpoint {
    private String versionCheck;
    private String download;

    public Endpoint() {}

    public Endpoint(String versionCheck, String download) {
        this.versionCheck = versionCheck;
        this.download = download;
    }

    public String getVersionCheck() {
        return versionCheck;
    }

    public void setVersionCheck(String versionCheck) {
        this.versionCheck = versionCheck;
    }

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }
}
