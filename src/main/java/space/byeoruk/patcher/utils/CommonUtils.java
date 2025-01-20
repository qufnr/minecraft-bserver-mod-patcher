package space.byeoruk.patcher.utils;

import java.io.File;

public class CommonUtils {
    public static void findMinecraft() {
        var appdata = System.getenv("APPDATA");
        if(appdata == null)
            throw new RuntimeException("Could not retrieve the '%appdata%' path.");

        var dir = new File("%s/.minecraft".formatted(appdata));
        if(!dir.isDirectory())
            throw new RuntimeException("Cannot find Minecraft directory. Please try again after installing the game.");
    }
}
