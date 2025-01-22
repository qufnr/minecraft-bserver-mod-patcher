package space.byeoruk.patcher.form;

import javax.swing.*;
import java.io.OutputStream;

public class LogTextAreaOutputStream extends OutputStream {
    private final JTextArea textArea;
    private final String prefix;
    private final boolean suppressStackTrace;

    private boolean newLine = true;

    public LogTextAreaOutputStream(JTextArea textArea, String prefix) {
        this(textArea, prefix, false);
    }

    public LogTextAreaOutputStream(JTextArea textArea, String prefix, boolean suppressStackTrace) {
        this.textArea = textArea;
        this.prefix = "[%s]".formatted(prefix);
        this.suppressStackTrace = suppressStackTrace;
    }

    @Override
    public void write(int b) {
        SwingUtilities.invokeLater(() -> {
            if (newLine) {
                textArea.append(prefix);
                newLine = false;
            }
            textArea.append(String.valueOf((char) b));
            if ((char) b == '\n')
                newLine = true;
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }

    @Override
    public void write(byte[] b, int off, int len) {
        String text = new String(b, off, len);
        if (suppressStackTrace && text.startsWith("\tat "))
            return;

        SwingUtilities.invokeLater(() -> {
            for (char c : text.toCharArray()) {
                if (newLine) {
                    textArea.append(prefix);
                    newLine = false;
                }
                textArea.append(String.valueOf(c));
                if (c == '\n')
                    newLine = true;
            }
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }
}
