package space.byeoruk.patcher.exception;

public class LatestVersionNotFoundException extends RuntimeException {
    public LatestVersionNotFoundException() {
        super("최신 버전을 확인할 수 없습니다. 관리자에게 문의 해주세요.");
    }
}
