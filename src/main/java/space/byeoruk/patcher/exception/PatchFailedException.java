package space.byeoruk.patcher.exception;

public class PatchFailedException extends RuntimeException {
    public PatchFailedException() {
        super("모드 패치하는 데 실패했습니다. 관리자에게 문의 해주세요.");
    }
}
