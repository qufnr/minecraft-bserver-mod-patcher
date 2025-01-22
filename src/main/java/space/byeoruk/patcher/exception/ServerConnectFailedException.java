package space.byeoruk.patcher.exception;

public class ServerConnectFailedException extends RuntimeException {
    public ServerConnectFailedException() {
        super("서버와 연결하는 데 실패했습니다.");
    }
}
