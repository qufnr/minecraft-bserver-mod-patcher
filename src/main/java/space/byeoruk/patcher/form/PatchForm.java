package space.byeoruk.patcher.form;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.archivers.zip.ZipFile;
import space.byeoruk.patcher.exception.*;
import space.byeoruk.patcher.logger.LogTextAreaOutputStream;
import space.byeoruk.patcher.logger.LoggerType;
import space.byeoruk.patcher.utils.BFileUtils;
import space.byeoruk.patcher.utils.BStringUtils;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class PatchForm {
    private static final String APP_NAME = "벼룩서버 모드팩 설치 마법사";
    private static final String VERSION = "1.0.0";

    private static final String SERVER_ENDPOINT = "http://26.232.135.66:8008/minecraft-bserver/";
    private static final String FILE_URL = SERVER_ENDPOINT + "patch.zip";
    private static final String VERSION_CHECK_URL = SERVER_ENDPOINT + "version.json";
    private static final String SAVE_DIR = System.getenv("APPDATA") + "\\.minecraft";
    private static final List<String> DELETE_DIRS = List.of(
            SAVE_DIR + "\\mods",
            SAVE_DIR + "\\config",
            SAVE_DIR + "\\resourcepacks",
            SAVE_DIR + "\\shaderpacks",
            SAVE_DIR + "\\options.txt"
    );

    private static final String VERSION_FILENAME = "bserver-version.json";

    private JLabel titleLabel;
    private JButton button;
    private JTextArea logTextArea;

    private Integer clientVersion;
    private Integer serverVersion;

    private PatchForm() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("%s v%s".formatted(APP_NAME, VERSION));
            frame.setSize(550, 220);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);

            var panel = new JPanel();
            panel.setLayout(new BorderLayout());

            titleLabel = new JLabel(APP_NAME);
            titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);

            logTextArea = new JTextArea();
            logTextArea.setBackground(Color.WHITE);
            logTextArea.setEditable(false);
            var scrollPanel = new JScrollPane(logTextArea);

            button = new JButton("설치");
            button.addActionListener(event -> onPatchStart());

            panel.add(titleLabel, BorderLayout.NORTH);          //  Title
            panel.add(scrollPanel, BorderLayout.CENTER);        //  Logging Area
            panel.add(button, BorderLayout.SOUTH);              //  Button

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

    /**
     * Runs the JFrame on Main class.
     */
    public static void run() {
        var font = new FontUIResource(new Font("굴림체", Font.PLAIN, 12));
        UIManager.put("Label.font", font);
        UIManager.put("TextArea.font", font);
        UIManager.put("Button.font", font);

        new PatchForm();
    }

    /**
     * Validations the client version.
     */
    private void onValidateVersion() {
        this.button.setEnabled(false);
        System.out.println("모드팩 버전을 확인하고 있습니다. 잠시만 기다려주세요.");

        try {
            //  Read the server side version
            var client = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder().uri(URI.create(VERSION_CHECK_URL)).GET().build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var body = BStringUtils.toMap(response.body());
            serverVersion = (Integer) body.get("version");

            //  Read the client version
            var versionFile = new File(SAVE_DIR, VERSION_FILENAME);
            if(!versionFile.exists())
                throw new ClientVersionNotFoundException();

            var om = new ObjectMapper();
            var versionContent = om.readValue(versionFile, new TypeReference<HashMap<String, Object>>() {});
            clientVersion = (Integer) versionContent.get("version");

            if(clientVersion == null) {
                System.out.println("클라이언트의 모드팩 버전을 확인할 수 없습니다. 설치 해주세요.");
                button.setEnabled(true);
            }
            else if(serverVersion == null)
                System.out.println("서버에서 모드팩 버전을 가져오는 데 실패했습니다. 관리자에게 문의 해주세요.");
            else if(clientVersion.equals(serverVersion))
                System.out.println("클라이언트의 모드팩 버전이 최신 버전입니다!");
            else if(clientVersion < serverVersion) {
                System.out.println("새로운 모드팩 버전이 발견되었습니다. 업데이트 해주세요.");
                button.setEnabled(true);
                button.setText("업데이트");
            }

        }
        catch(Exception e) {
            exceptionAlertHandle(e);
            this.button.setEnabled(true);
        }
        finally {
            var c = clientVersion == null ? "-" : clientVersion.toString();
            var s = serverVersion == null ? "-" : serverVersion.toString();
            titleLabel.setText(APP_NAME + " C: " + c + " | S: " + s);
        }
    }

    /**
     * The installation or update start when click button.
     */
    private void onPatchStart() {
        button.setEnabled(false);

        var worker = new SwingWorker<Void, String>() {
            /**
             * 로그 메시지를 퍼블리싱합니다.
             *
             * @param type 애러 유형
             * @param message 메시지
             */
            private void publish(LoggerType type, String message) {
                super.publish("[%s] %s".formatted(type.toString(), message));
            }

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    //  Find a minecraft directory
                    var appdataGameDir = new File(SAVE_DIR);
                    if(!appdataGameDir.exists())
                        throw new MinecraftNotFoundException();

                    //  Download mod pack from server mod pack storage
                    var client = HttpClient.newHttpClient();
                    var request = HttpRequest.newBuilder().uri(URI.create(FILE_URL)).GET().build();
                    var filename = FILE_URL.substring(FILE_URL.lastIndexOf("/") + 1);
                    var zipFilename = SAVE_DIR + File.separator + filename;

                    publish(LoggerType.INFO, "다운로드 중...");

                    var response = client.send(request, HttpResponse.BodyHandlers.ofFile(Paths.get(zipFilename)));
                    if(response.statusCode() != 200)
                        throw new ModPackDownloadFailedException(response.statusCode());

                    publish(LoggerType.INFO, "다운로드 완료!");

                    DELETE_DIRS.forEach(path -> BFileUtils.deleteDirOrFile(new File(path)));

                    //  Unzip
                    BFileUtils.ensureDirectoryExists(appdataGameDir);

                    //  Use try-with-resources for automatic resource management
                    try(var zip = ZipFile.builder().setPath(zipFilename).setCharset(Charset.forName("CP437")).get()) {
                        var entries = zip.getEntries();

                        while(entries.hasMoreElements()) {
                            var entry = entries.nextElement();
                            var file = new File(SAVE_DIR, entry.getName());

                            publish(LoggerType.INFO, "압축 해제 중... %s".formatted(file));

                            //  Validate file path to prevent directory traversal attacks
                            BFileUtils.validateFilePath(file, appdataGameDir);

                            if(entry.isDirectory())
                                BFileUtils.ensureDirectoryExists(file);
                            else {
                                var parent = file.getParentFile();
                                BFileUtils.ensureDirectoryExists(parent);

                                try(var fileOutputStream = new FileOutputStream(file)) {
                                    zip.getInputStream(entry).transferTo(fileOutputStream);
                                }
                            }
                        }
                    }

                    //  Deletes the downloaded zip file
                    var zipFile = new File(zipFilename);
                    if(zipFile.exists() && !zipFile.delete())
                        throw new FileDeleteFailedException(zipFilename);

                    //  Creation the version file
                    var versionFile = new File(SAVE_DIR, VERSION_FILENAME);
                    var content = """
{
    "version": %d,
    "desc": "Do not touch this file!!!"
}
                    """.formatted(serverVersion);
                    try(var writer = new FileWriter(versionFile)) {
                        writer.write(content);
                    }

                    clientVersion = serverVersion;
                    publish(LoggerType.INFO, "빠밤빠밤~! 완료 되었어요!");
                }
                catch(Exception e) {
                    publish(LoggerType.ERROR, e.getMessage());
                    throw e;
                }

                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for(var chunk : chunks) {
                    logTextArea.append("%s\n".formatted(chunk));
                    logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(null, "빠밤빠밤~! 완료 되었어요!");
                }
                catch(Exception e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    button.setEnabled(true);
                }
                finally {
                    if(!clientVersion.equals(serverVersion))
                        button.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    /**
     * 예외처리 핸들링
     *
     * @param e 예외
     */
    private void exceptionAlertHandle(Exception e) {
        var message = e.getMessage();

        if(e instanceof ConnectException)
            message = "끄앙! 서버에 연결하는 데 실패했어요.";

        System.err.println(message);
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
