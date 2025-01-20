package space.byeoruk.patcher.service;

import java.io.File;
import java.io.InputStream;

public class PatchService {
//    private static final String DOWNLOAD_URL = "http://26.232.135.66:8080/minecraft-bserver/patch.zip";
//    private static final String GAME_DIR = System.getenv("APPDATA") + "\\.minecraft";
//    private static final String GAME_MOD_DIR = GAME_DIR + "\\mods";
//
//    public static void patch() {
//        //  Delete the .minecraft/mods directory
//        deleteModsDir(new File(GAME_MOD_DIR));
//
//        //  Download the patch file
//        var zipFilePath = downloadPatchFile(DOWNLOAD_URL, GAME_DIR);
//
//        //  Unzip the downloaded patch file
//        unzipPatch(zipFilePath, GAME_MOD_DIR);
//    }
//
//    private static String downloadPatchFile(String downloadUrl, String gameDir) {
//        var appdataGameDir = new File(gameDir);
//        if(!appdataGameDir.exists())
//            throw new RuntimeException("Cannot find Minecraft directory. Please try again after installing the game.");
//
//        var filename = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
//        var filePath = GAME_DIR + File.separator + filename;
//
//        try (InputStream inputStream = Request)
//    }
}
