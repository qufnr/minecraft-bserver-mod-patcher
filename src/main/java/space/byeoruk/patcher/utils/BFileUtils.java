package space.byeoruk.patcher.utils;

import space.byeoruk.patcher.exception.FileDeleteFailedException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class BFileUtils {
    public static void deleteDirOrFile(File fileToBeDeleted) {
        try {
            if(fileToBeDeleted.exists()) {
                if(fileToBeDeleted.isDirectory())
                    Files.walk(fileToBeDeleted.toPath())
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(file -> {
                                if(!file.delete())
                                    throw new FileDeleteFailedException(file.getName());
                            });
                else if(!fileToBeDeleted.delete())
                    throw new FileDeleteFailedException(fileToBeDeleted.getName());
            }
        }
        catch(IOException e) {
            throw new FileDeleteFailedException();
        }
    }

    public static void ensureDirectoryExists(File directory) throws IOException {
        if(!directory.exists())
            if(!directory.mkdirs() && !directory.isDirectory())
                throw new IOException("Failed to creation directory. %s".formatted(directory));
    }

    public static void validateFilePath(File file, File destDir) throws IOException {
        var filePath = file.getAbsolutePath();
        var destDirPath = destDir.getAbsolutePath();

        if(!filePath.startsWith(destDirPath + File.separator))
            throw new IOException("Invalid path. %s".formatted(filePath));
    }
}
