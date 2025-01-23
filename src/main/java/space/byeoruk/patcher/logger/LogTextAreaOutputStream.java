package space.byeoruk.patcher.logger;

import javax.swing.*;
import java.io.OutputStream;

public class LogTextAreaOutputStream extends OutputStream {
    private final JTextArea textArea;
    private final String prefix;
    private final boolean suppressStackTrace;

    private final StringBuffer buffer = new StringBuffer();

    public LogTextAreaOutputStream(JTextArea textArea, String prefix) {
        this(textArea, prefix, false);
    }

    public LogTextAreaOutputStream(JTextArea textArea, String prefix, boolean suppressStackTrace) {
        this.textArea = textArea;
        this.prefix = "[%s] ".formatted(prefix);
        this.suppressStackTrace = suppressStackTrace;
    }

    @Override
    public void write(int b) {
        synchronized (buffer) {
            buffer.append(prefix);
            buffer.append((char) b);
            if ((char) b == '\n')
                flushBuffer();
        }
    }

    @Override
    public void write(byte[] b, int off, int len) {
        synchronized (buffer) {
            String text = new String(b, off, len);
            if (suppressStackTrace && text.startsWith("\tat "))
                return;

            buffer.append(prefix);
            buffer.append(text);
            flushBuffer();
        }
    }

    private void flushBuffer() {
        SwingUtilities.invokeLater(() -> {
            synchronized (buffer) {
                textArea.append(buffer.toString());
                textArea.setCaretPosition(textArea.getDocument().getLength());

                buffer.setLength(0);    //  Clear buffer
            }
        });
    }
}
