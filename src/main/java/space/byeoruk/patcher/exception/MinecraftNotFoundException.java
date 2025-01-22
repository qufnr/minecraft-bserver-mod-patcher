package space.byeoruk.patcher.exception;

public class MinecraftNotFoundException extends RuntimeException {
    public MinecraftNotFoundException() {
        super("모드를 패치하기 전에 Minecraft 를 설치해주세요.");
    }
}
