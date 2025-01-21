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
        ensureDirectoryExists(dir);

        try(var zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;

            while((entry = zipInputStream.getNextEntry()) != null) {
                System.out.printf("Extracting file... %s\n", entry.getName());

                //  Resolve the new file path
                var newFile = new File(destDir, entry.getName());

                //  Validate the new file path
                validateFilePath(newFile, dir);

                if(entry.isDirectory())
                    ensureDirectoryExists(newFile);
                else {
                    var parent = newFile.getParentFile();
                    ensureDirectoryExists(parent);

                    //  Delete existing file if any
                    deleteFileIfExists(newFile);

                    if(!parent.exists() && !parent.mkdirs())
                        throw new RuntimeException("Failed to create directory: %s".formatted(parent));

                    try(var fileOutputStream = new FileOutputStream(newFile)) {
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

    private static void ensureDirectoryExists(File dir) {
        if(!dir.exists())
            if(!dir.mkdirs() && !dir.isDirectory())
                throw new RuntimeException("Failed to create directory: %s".formatted(dir));
    }

    private static void deleteFileIfExists(File file) {
        if(file.exists() && !file.delete())
            throw new RuntimeException("Failed to delete file: %s".formatted(file));
    }

    private static void validateFilePath(File file, File destDir) {
        var destDirPath = destDir.getAbsolutePath();
        var filePath = file.getAbsolutePath();

        if(!filePath.startsWith(destDirPath + File.separator))
            throw new RuntimeException("Entry is outside of the target directory: %s".formatted(filePath));
    }
}