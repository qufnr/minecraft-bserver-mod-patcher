package space.byeoruk.patcher.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.archivers.zip.ZipFile;
import space.byeoruk.patcher.exception.*;
import space.byeoruk.patcher.form.PatchForm;
import space.byeoruk.patcher.utils.BStringUtils;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;

public class PatchService {
    private static final String SERVER_ENDPOINT = "http://26.232.135.66:8008/minecraft-bserver/";
    private static final String FILE_URL = SERVER_ENDPOINT + "patch.zip";
    private static final String VERSION_CHECK_URL = SERVER_ENDPOINT + "version.json";
    private static final String SAVE_DIR = System.getenv("APPDATA") + "\\.minecraft";
    private static final String MODS_DIR = SAVE_DIR + "\\mods";

    private static final String VERSION_FILENAME = "bserver-version.json";

    public static int[] readVersion() {
        Integer latestVersion = getLatestVersionFromServer(VERSION_CHECK_URL);
        Integer clientVersion = getClientVersion(SAVE_DIR);

        if(latestVersion == null)
            throw new LatestVersionNotFoundException();

        if(clientVersion == null)
            throw new ClientVersionNotFoundException();

        return new int[] { latestVersion, clientVersion };
    }

    private static Integer getLatestVersionFromServer(String versionCheckUrl) {
        try {
            var client = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(versionCheckUrl))
                    .GET()
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var body = BStringUtils.toMap(response.body());

            return body.get("version") != null ? (Integer) body.get("version") : null;
        }
        catch(Exception e) {
            throw new ServerConnectFailedException();
        }
    }

    private static Integer getClientVersion(String saveDir) {
        try {
            var file = new File(saveDir, VERSION_FILENAME);
            if(!file.exists())
                return null;

            var om = new ObjectMapper();
            var content = om.readValue(file, new TypeReference<HashMap<String, Object>>() {});

            return (int) content.get("version");
        }
        catch(IOException e) {
            throw new ClientVersionReadFailedException();
        }
    }

    public static boolean patch() throws IOException, InterruptedException {
        //  1. Download the patch file
        var zipFilePath = downloadPatchFile(FILE_URL, SAVE_DIR);

        //  2. Delete the .minecraft/mods directory or its contents
        deleteDirectory(new File(MODS_DIR));

        //  3. Unzip the downloaded patch file to the .minecraft folder
        unzipPatchFile(zipFilePath, SAVE_DIR);

        //  4. Delete the downloaded patch ZIP file.
        deleteFile(zipFilePath);

        //  5. Create the version.txt file
        createVersionFile(SAVE_DIR, 1);

        return true;
    }

    private static String downloadPatchFile(String fileUrl, String saveDir) throws IOException, InterruptedException {
        var appdataGameDir = new File(saveDir);
        if(!appdataGameDir.exists())
            throw new MinecraftNotFoundException();

        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(fileUrl))
                .GET()
                .build();

        var filename = getDownloadFilename(fileUrl);
        var filePath = SAVE_DIR + File.separator + filename;

        System.out.println("모드 패치 파일 다운로드 중...");

        var response = client.send(request, HttpResponse.BodyHandlers.ofFile(Paths.get(filePath)));

        if(response.statusCode() != 200)
            throw new PatchDownloadFailedException(response.statusCode());

        System.out.println("다운로드 완료!");

        return filePath;
    }

    private static void unzipPatchFile(String zipFilePath, String destDir) throws IOException {
        var dir = new File(destDir);
        ensureDirectoryExists(dir);

        //  Use try-with-resources for automatic resource management
        try(var zip = ZipFile.builder().setPath(zipFilePath).setCharset(Charset.forName("CP437")).get()) {
            var entries = zip.getEntries();

            while(entries.hasMoreElements()) {
                var entry = entries.nextElement();
                var file = new File(destDir, entry.getName());

                System.out.println("[INFO] 압축 해제 중... " + file);

                //  Validate file path to prevent directory traversal attacks
                validateFilePath(file, dir);

                if(entry.isDirectory())
                    ensureDirectoryExists(file);
                else {
                    var parent = file.getParentFile();
                    ensureDirectoryExists(parent);

                    //  Write the file content
                    try(var fileOutputStream = new FileOutputStream(file)) {
                        zip.getInputStream(entry).transferTo(fileOutputStream);
                    }
                }
            }
        }
    }

    private static void deleteDirectory(File directoryToBeDeleted) throws IOException {
        if(directoryToBeDeleted.exists()) {
            Files.walk(directoryToBeDeleted.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    private static void deleteFile(String filePath) {
        var file = new File(filePath);
        if(file.exists() && !file.delete())
            throw new RuntimeException("Failed to delete file: " + filePath);
    }

    private static void createVersionFile(String saveDir, Integer version) throws IOException {
        var file = new File(saveDir, VERSION_FILENAME);
        var content = """
{
    "version": %d,
    "desc": "Do not touch this file!!!"
}
                """.formatted(version);
        try(var writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    private static String getDownloadFilename(String downloadUrl) {
        return downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
    }

    private static void ensureDirectoryExists(File dir) throws IOException {
        if(!dir.exists())
            if(!dir.mkdirs() && !dir.isDirectory())
                throw new IOException("디렉토리를 생성하는 데 실패했습니다: %s".formatted(dir));
    }

    private static void validateFilePath(File file, File destDir) throws IOException {
        var destDirPath = destDir.getAbsolutePath();
        var filePath = file.getAbsolutePath();

        if(!filePath.startsWith(destDirPath + File.separator))
            throw new IOException("대상 디렉토리에서 벗어났습니다: %s".formatted(filePath));
    }
}