package space.byeoruk.patcher.exception;

public class ClientVersionNotFoundException extends RuntimeException {
    public ClientVersionNotFoundException() {
        super("클라이언트 버전을 확인할 수 없습니다. 업데이트 해주세요.");
    }
}
