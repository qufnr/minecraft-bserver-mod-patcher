package space.byeoruk.patcher.exception;

public class ModPackDownloadFailedException extends RuntimeException {
    public ModPackDownloadFailedException(int httpStatus) {
        super("모드팩을 다운로드하는 데 실패했습니다. HTTP Status Code: %d".formatted(httpStatus));
    }
}
