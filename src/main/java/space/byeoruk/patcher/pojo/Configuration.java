package space.byeoruk.patcher.pojo;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import space.byeoruk.patcher.utils.BStringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Configuration {
    private Boolean debug;
    private String server;
    private Endpoint endpoint;
    private List<String> deleteFiles;
    private String versionFilename;

    public Configuration() {}

    public Configuration(Boolean debug, String server, Endpoint endpoint, List<String> deleteFiles, String versionFilename) {
        this.debug = debug;
        this.server = server;
        this.endpoint = endpoint;
        this.deleteFiles = deleteFiles;
        this.versionFilename = versionFilename;
    }

    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public List<String> getDeleteFiles() {
        return deleteFiles;
    }

    public void setDeleteFiles(List<String> deleteFiles) {
        this.deleteFiles = deleteFiles;
    }

    public String getVersionFilename() {
        return versionFilename;
    }

    public void setVersionFilename(String versionFilename) {
        this.versionFilename = versionFilename;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public String getVersionCheckUrl() {
        return this.server + this.endpoint.getVersionCheck();
    }

    public String getDownloadUrl() {
        return this.server + this.endpoint.getDownload();
    }

    /**
     * Creation the default config.yml file.
     */
    public static Configuration create(String filename) {
        System.out.println("`config.yml` 파일을 생성하고 있습니다...");

        try {
            var newConfiguration = getDefaultConfiguration();

            var dumper = new DumperOptions();
            dumper.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            dumper.setPrettyFlow(true);
            var yaml = new Yaml(dumper);
            var content = yaml.dump(newConfiguration);

            Files.write(Path.of(filename), content.getBytes(), StandardOpenOption.CREATE_NEW);

            System.out.println("`config.yml` 파일을 생성했습니다.");

            return newConfiguration;
        }
        catch(IOException e) {
            System.err.println("`config.yml` 파일을 생성하는 데 실패했습니다. " + e.getMessage());
        }

        return null;
    }

    /**
     * Reads the config.yml file.
     *
     * @param filename The name of file
     * @return Configuration Object
     * @throws IOException Input Output Exception
     */
    public static Configuration read(String filename) throws IOException {
        var file = new File(filename);

        try(var inputStream = new FileInputStream(file)) {
            var yaml = new Yaml(new Constructor(Configuration.class, new LoaderOptions()));
            return yaml.loadAs(inputStream, Configuration.class);
        }
    }

    /**
     * 속성 값들의 유효성을 검사합니다. 유효성 검사 후 값 정리
     */
    public void validate() {
        if(BStringUtils.isEmptyOrBlank(this.server))
            throw new IllegalArgumentException("server 속성이 정의되지 않았습니다.");
        if(BStringUtils.isEmptyOrBlank(this.endpoint.getVersionCheck()))
            throw new IllegalArgumentException("endpoint.versionCheck 속성이 정의되지 않았습니다.");
        if(BStringUtils.isEmptyOrBlank(this.endpoint.getDownload()))
            throw new IllegalArgumentException("endpoint.download 속성이 정의되지 않았습니다.");
        if(BStringUtils.isEmptyOrBlank(this.versionFilename))
            throw new IllegalArgumentException("versionFilename 속성이 정의되지 않았습니다.");
        if(!this.server.startsWith("http://") && !this.server.startsWith("https://"))
            throw new IllegalArgumentException("유효하지 않은 server 속성 값입니다.");
        if(this.server.endsWith("/"))
            throw new IllegalArgumentException("server 속성 값의 끝 부분에 \"/\" 를 제거 해주세요.");
        if(!this.endpoint.getVersionCheck().startsWith("/") || !this.endpoint.getDownload().startsWith("/"))
            throw new IllegalArgumentException("endpoint.* 속성의 값은 무조건 첫 부분에 \"/\" 가 있어야 합니다.");
        if(!this.versionFilename.endsWith(".json"))
            throw new IllegalArgumentException("versionFilename 값은 JSON 외의 유형은 사용할 수 없습니다.");
        for(var i = 0; i < this.deleteFiles.size(); i++) {
            if(this.deleteFiles.get(i).startsWith(File.separator))
                throw new IllegalArgumentException("deleteFiles[] 값의 첫 부분에 \"%s\" 을(를) 제거 해주세요. (잘못된 값: %s)".formatted(File.separator, this.deleteFiles.get(i)));

            this.deleteFiles.set(i, this.deleteFiles.get(i).trim());
        }

        this.server = this.server.trim();
        this.endpoint.setVersionCheck(this.endpoint.getVersionCheck().trim());
        this.endpoint.setDownload(this.endpoint.getDownload().trim());
        this.versionFilename = this.versionFilename.trim();
    }

    private static Configuration getDefaultConfiguration() {
        var defaultDeleteFiles = new ArrayList<String>();
        defaultDeleteFiles.add("mods");
        defaultDeleteFiles.add("config");
        defaultDeleteFiles.add("resourcepacks");
        defaultDeleteFiles.add("shaderpacks");

        return new Configuration(
                false,
                "http://26.232.135.66:8008/minecraft-bserver",
                new Endpoint("/version.json", "/patch.zip"),
                defaultDeleteFiles,
                "bserver-version.json");
    }
}
