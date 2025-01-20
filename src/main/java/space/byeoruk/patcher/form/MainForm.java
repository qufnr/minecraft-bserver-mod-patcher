package space.byeoruk.patcher.form;

import space.byeoruk.patcher.utils.CommonUtils;

import javax.swing.*;
import java.awt.*;

public class MainForm extends JFrame {
    private final JButton patchButton;

    public MainForm() {
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

    private void onPatchStart() {
        this.patchButton.setEnabled(false);

        try {
            CommonUtils.findMinecraft();
        }
        catch(Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        finally {
            this.patchButton.setEnabled(true);
        }
    }
}
