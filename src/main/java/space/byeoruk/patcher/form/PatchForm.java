package space.byeoruk.patcher.form;

import space.byeoruk.patcher.exception.PatchFailedException;
import space.byeoruk.patcher.service.PatchService;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.PrintStream;
import java.net.ConnectException;

public class PatchForm {
    private static final String APP_NAME = "벼룩서버 모드 패치";
    private static final String VERSION = "1.0.0";

    private JButton patchButton;
    private JTextArea logTextArea;

    private PatchForm() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("%s v%s".formatted(APP_NAME, VERSION));
            frame.setSize(550, 220);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            var panel = new JPanel();
            panel.setLayout(new BorderLayout());

            var titleLabel = new JLabel(APP_NAME);
            titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);

            this.logTextArea = new JTextArea();
            this.logTextArea.setBackground(Color.WHITE);
            this.logTextArea.setEditable(false);
            var scrollPanel = new JScrollPane(this.logTextArea);

            this.patchButton = new JButton("설치");
            this.patchButton.addActionListener(e -> onPatchStart());

            panel.add(titleLabel, BorderLayout.NORTH);          //  Title
            panel.add(scrollPanel, BorderLayout.CENTER);        //  Logging Area
            panel.add(this.patchButton, BorderLayout.SOUTH);    //  Button

            frame.setContentPane(panel);

            frame.setResizable(false);
            frame.setVisible(true);

            var consoleInfoStream = new PrintStream(new LogTextAreaOutputStream(logTextArea, "INFO"));
            var consoleErrorStream = new PrintStream(new LogTextAreaOutputStream(logTextArea, "ERROR", true));
            System.setOut(consoleInfoStream);
            System.setErr(consoleErrorStream);

            Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> System.err.println(exception.getMessage()));

            onValidateVersion();
        });
    }

    public static void run() {
        var font = new FontUIResource(new Font("굴림체", Font.PLAIN, 12));
        UIManager.put("Label.font", font);
        UIManager.put("TextArea.font", font);
        UIManager.put("Button.font", font);

        new PatchForm();
    }

    private void onValidateVersion() {
        var versions = new int[2];

        try {
            this.patchButton.setEnabled(false);
            System.out.println("버전을 확인하고 있습니다. 잠시만 기다려주세요...");

            versions = PatchService.readVersion();

            if(versions[0] == versions[1])
                System.out.println("최신 버전입니다.");
        }
        catch(Exception e) {
            System.err.println(e.getMessage());
            this.patchButton.setEnabled(true);

            e.printStackTrace();
        }
        finally {
            if(versions[0] > versions[1]) {
                this.patchButton.setText("업데이트");
                this.patchButton.setEnabled(true);
            }
        }
    }

    private void onPatchStart() {
        var success = false;
        try {
            this.patchButton.setEnabled(false);
            success = PatchService.patch();
            if(success) {
                System.out.println("뽜밤뽜밤! 패치 완료!");
                JOptionPane.showMessageDialog(null, "뽜밤뽜밤! 패치 완료!");
                return;
            }
            throw new PatchFailedException();
        }
        catch(Exception e) {
            var message = e.getMessage();

            if(e instanceof ConnectException)
                message = "서버에 연결하는 데 실패했습니다.";

            System.err.println(message);

            e.printStackTrace();
        }
        finally {
            if(!success)
                this.patchButton.setEnabled(true);
        }
    }
}
