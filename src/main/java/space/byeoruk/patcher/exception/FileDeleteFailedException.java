package space.byeoruk.patcher.exception;

public class FileDeleteFailedException extends RuntimeException {
    public FileDeleteFailedException() {
        super("파일을 삭제하는 데 실패했습니다.");
    }

    public FileDeleteFailedException(String filename) {
        super("파일 %s 을(를) 삭제하는 데 실패했습니다.".formatted(filename));
    }
}
