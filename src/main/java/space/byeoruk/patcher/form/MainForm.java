package space.byeoruk.patcher.form;

import space.byeoruk.patcher.service.PatchService;

import javax.swing.*;
import java.awt.*;
import java.net.ConnectException;

public class MainForm extends JFrame {
    private final JButton patchButton;

    private MainForm() {
        super("BServer Mod Patcher");
        super.setSize(600, 200);
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        super.setLocationRelativeTo(null);

        super.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 50));

        var panel = new JPanel(new BorderLayout());

        var label = new JLabel("BServer Mod Patcher");
        panel.add(label, BorderLayout.NORTH);

        this.patchButton = new JButton("Patch!");
        this.patchButton.addActionListener(e -> onPatchStart());
        panel.add(this.patchButton, BorderLayout.SOUTH);

        super.add(panel);

        super.setVisible(true);
    }

    public static void run() {
        new MainForm();
    }

    private void onPatchStart() {
        this.patchButton.setEnabled(false);

        try {
            var success = PatchService.patch();
            if(success) {
                JOptionPane.showMessageDialog(null, "Successfully patched!");
                return;
            }
            throw new RuntimeException("Failed to patch.");
        }
        catch(Exception e) {
            var message = e.getMessage();

            if(e instanceof ConnectException)
                message = "다운로드 서버에 연결할 수 없습니다.";

            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);

            e.printStackTrace();
        }
        finally {
            this.patchButton.setEnabled(true);
        }
    }
}
