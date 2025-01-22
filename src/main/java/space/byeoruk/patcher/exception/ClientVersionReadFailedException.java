package space.byeoruk.patcher.exception;

public class ClientVersionReadFailedException extends RuntimeException {
    public ClientVersionReadFailedException() {
        super("버전 파일을 읽는 데 실패했습니다. 관리자에게 문의 해주세요.");
    }
}
