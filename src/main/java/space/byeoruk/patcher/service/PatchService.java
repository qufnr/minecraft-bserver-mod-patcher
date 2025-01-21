package space.byeoruk.patcher.service;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PatchService {
    private static final String FILE_URL = "http://26.232.135.66:8008/minecraft-bserver/patch.zip";
    private static final String SAVE_DIR = System.getenv("APPDATA") + "\\.minecraft";
    private static final String MODS_DIR = SAVE_DIR + "\\mods";

    public static boolean patch() throws IOException, InterruptedException {
        //  1. Download the patch file
        var zipFilePath = downloadPatchFile(FILE_URL, SAVE_DIR);
        //  2. Delete the .minecraft/mods directory or its contents
        deleteDirectory(new File(MODS_DIR));
        //  3. Unzip the downloaded patch file to the .minecraft folder
        unzipPatchFile(zipFilePath, SAVE_DIR);
        //  4. Delete the downloaded patch ZIP file.
        deleteFile(zipFilePath);

        System.out.println("Success!!!");

        return true;
    }

    private static String downloadPatchFile(String fileUrl, String saveDir) throws IOException, InterruptedException {
        var appdataGameDir = new File(saveDir);
        if(!appdataGameDir.exists())
            throw new RuntimeException("Cannot find Minecraft directory. Please try again after installing the game.");

        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(fileUrl))
                .GET()
                .build();

        var filename = getDownloadFilename(fileUrl);
        var filePath = SAVE_DIR + File.separator + filename;

        System.out.println("Downloading...");

        var response = client.send(request, HttpResponse.BodyHandlers.ofFile(Paths.get(filePath)));

        if(response.statusCode() != 200)
            throw new RuntimeException("Failed to download patch file. HTTP Status Code: " + response.statusCode());

        System.out.println("Downloaded!");

        return filePath;
    }

    private static void unzipPatchFile(String zipFilePath, String destDir) throws IOException {
        var dir = new File(destDir);

        if(!dir.exists())
            throw new RuntimeException("Destination directory not found: %s".formatted(destDir));

        try(var zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;

            while((entry = zipInputStream.getNextEntry()) != null) {
                //  Resolve the new file path.
                var file = new File(destDir, entry.getName());

                //  Prevent directory traversal attacks and invalid paths
                var dirPath = dir.getCanonicalPath();
                var filePath = file.getCanonicalPath();

                System.out.printf("Extracting... %s\n", entry.getName());
                System.out.printf("Resolved path: %s\n", filePath);

                if(!filePath.startsWith(dirPath))
                    throw new RuntimeException("Entry is outside of the target directory: %s".formatted(entry.getName()));

                if(entry.isDirectory())
                    if(!file.isDirectory() && !file.mkdirs())
                        throw new RuntimeException("Failed to create directory: %s".formatted(file));
                else {
                    var parent = file.getParentFile();
                    if(!parent.exists() && !parent.mkdirs())
                        throw new RuntimeException("Failed to create directory: %s".formatted(parent));

                    try(var fileOutputStream = new FileOutputStream(file)) {
                        var buffer = new byte[4096];
                        int len;
                        while((len = zipInputStream.read(buffer)) > 0) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                    }
                }

                zipInputStream.closeEntry();
            }
        }

        System.out.printf("Unzipped to: %s\n", dir);
    }

    private static void deleteDirectory(File directoryToBeDeleted) throws IOException {
        if(directoryToBeDeleted.exists()) {
            Files.walk(directoryToBeDeleted.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    private static void deleteFile(String filePath) throws IOException {
        var file = new File(filePath);
        if(file.exists() && !file.delete())
            throw new RuntimeException("Failed to delete file: " + filePath);

        System.out.printf("Delete file %s\n", filePath);
    }

    private static String getDownloadFilename(String downloadUrl) {
        var filename = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
        System.out.printf("Zip filename %s\n", filename);
        return filename;
    }
}