package space.byeoruk.patcher.exception;

public class PatchDownloadFailedException extends RuntimeException {
    public PatchDownloadFailedException(int httpStatus) {
        super("패치 파일을 내려받는 데 실패했습니다. HTTP Status Code: %d".formatted(httpStatus));
    }
}
