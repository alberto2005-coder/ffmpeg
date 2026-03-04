import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class FFmpegSuit {

    public static final String FFMPEG_URL = "https://ffmpeg.org/download.html";
    private static File ffmpegExe = null;
    private static File ffprobeExe = null;

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
            }
        }

        boolean forceConsole = args.length > 0 && args[0].equalsIgnoreCase("-x");
        boolean isHeadless = GraphicsEnvironment.isHeadless();

        if (isHeadless || forceConsole) {
            System.out.println(">>> Inicializando FFmpegSuit (Modo Consola)...");
            try {
                locateBinaries();
            } catch (FileNotFoundException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
            new ConsoleMode().start();
        } else {
            SwingUtilities.invokeLater(() -> {
                GUIMode gui = new GUIMode();
                gui.setVisible(true);
                try {
                    locateBinaries();
                } catch (FileNotFoundException e) {
                    gui.showFFmpegMissingDialog(e.getMessage());
                }
            });
        }
    }

    private static void locateBinaries() throws FileNotFoundException {
        String os = System.getProperty("os.name").toLowerCase();
        String ffmpegName = os.contains("win") ? "ffmpeg.exe" : "ffmpeg";
        String ffprobeName = os.contains("win") ? "ffprobe.exe" : "ffprobe";

        if (checkCommand("ffmpeg -version")) {
            ffmpegExe = new File(ffmpegName);
            ffprobeExe = new File(ffprobeName);
            return;
        }

        ffmpegExe = findFileRecursively(new File("."), ffmpegName);
        ffprobeExe = findFileRecursively(new File("."), ffprobeName);

        if (ffmpegExe == null || !ffmpegExe.exists()) {
            throw new FileNotFoundException("No se pudo encontrar " + ffmpegName);
        }
    }

    private static boolean checkCommand(String command) {
        try {
            new ProcessBuilder(command.split(" ")).start().waitFor();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static File findFileRecursively(File directory, String fileName) {
        try (Stream<Path> stream = Files.walk(directory.toPath(), 5)) {
            return stream
                    .filter(p -> p.getFileName().toString().equalsIgnoreCase(fileName))
                    .findFirst()
                    .map(Path::toFile)
                    .orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    static class VideoParams {
        String resolution;
        String videoCodec;
        String audioCodec;
        String bitrateVideo;
        String bitrateAudio;
        String frameRate;
        int sampleRate;
        String extension;
        String crf;
        String preset;
        boolean sameFolder;
        File outputFolder;
    }

    static class FFmpegEngine {

        public void processFile(File input, File outputDir, VideoParams params, LogCallback logger) {
            try {
                List<String> cmd = new ArrayList<>();
                cmd.add(ffmpegExe.getAbsolutePath());
                cmd.add("-y");
                cmd.add("-i");
                cmd.add(input.getAbsolutePath());

                if (params.videoCodec != null) {
                    cmd.add("-c:v");
                    cmd.add(params.videoCodec);
                }

                if (params.audioCodec != null) {
                    cmd.add("-c:a");
                    cmd.add(params.audioCodec);
                }

                if (params.resolution != null && !params.resolution.equals("Original")) {
                    cmd.add("-vf");
                    cmd.add("scale=" + params.resolution);
                }
                if (params.bitrateVideo != null && !params.bitrateVideo.equals("Default")) {
                    cmd.add("-b:v");
                    cmd.add(params.bitrateVideo);
                }

                if (params.bitrateAudio != null && !params.bitrateAudio.equals("Default")) {
                    cmd.add("-b:a");
                    cmd.add(params.bitrateAudio);
                }
                if (params.frameRate != null && !params.frameRate.equals("Original")) {
                    cmd.add("-r");
                    cmd.add(params.frameRate);
                }

                if (params.sampleRate > 0) {
                    cmd.add("-ar");
                    cmd.add(String.valueOf(params.sampleRate));
                }
                if (params.crf != null && !params.crf.startsWith("Default")) {
                    String val = params.crf.split(" ")[0];
                    cmd.add("-crf");
                    cmd.add(val);
                }

                if (params.preset != null && !params.preset.startsWith("Default")) {
                    String val = params.preset.split(" ")[0];
                    cmd.add("-preset");
                    cmd.add(val);
                }

                String outName = input.getName().substring(0, input.getName().lastIndexOf('.')) + "_new."
                        + params.extension;
                File targetDir = params.sameFolder ? input.getParentFile() : params.outputFolder;
                if (targetDir == null)
                    targetDir = input.getParentFile();

                File outputFile = new File(targetDir, outName);
                cmd.add(outputFile.getAbsolutePath());

                logger.log("Ejecutando: " + String.join(" ", cmd));
                executeProcess(cmd, logger);

            } catch (Exception e) {
                logger.log("Error procesando " + input.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        private void executeProcess(List<String> cmd, LogCallback logger) {
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.redirectErrorStream(true);
                Process p = pb.start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.trim().isEmpty())
                            logger.log(line);
                    }
                }
                p.waitFor();
            } catch (Exception e) {
                logger.log("Error crítico: " + e.getMessage());
            }
        }
    }

    interface LogCallback {
        void log(String message);
    }

    static class GUIMode extends JFrame {
        private DefaultListModel<File> listModel = new DefaultListModel<>();
        private JList<File> fileList;
        private JTextField txtFilter;

        private JComboBox<String> cmbRes, cmbExt, cmbCodecV, cmbCodecA, cmbFps, cmbBitrateV, cmbBitrateA, cmbSample,
                cmbCrf, cmbPreset;
        private JComboBox<String> cmbSimpleQuality;
        private JRadioButton rbSimple, rbAdvanced;
        private JPanel advancedPanel;
        private JCheckBox chkSameFolder;
        private JTextField txtOutputFolder;
        private JButton btnBrowseFolder;

        public GUIMode() {
            setTitle("Fast Video Converter");
            setSize(1100, 750);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JLabel titleLabel = new JLabel("Fast Video Converter", SwingConstants.CENTER);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
            add(titleLabel, BorderLayout.NORTH);

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createLeftPanel(), createRightPanel());
            splitPane.setDividerLocation(400);
            splitPane.setResizeWeight(0.4);
            add(splitPane, BorderLayout.CENTER);
        }

        private JPanel createLeftPanel() {
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel top = new JPanel(new BorderLayout(5, 5));
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));

            JButton btnClean = new JButton("Limpiar");
            btnClean.setBackground(new Color(220, 53, 69));
            btnClean.setForeground(Color.WHITE);
            btnClean.addActionListener(e -> listModel.clear());

            JButton btnSelect = new JButton("Seleccionar");
            btnSelect.setBackground(new Color(0, 123, 255));
            btnSelect.setForeground(Color.WHITE);
            btnSelect.addActionListener(e -> selectFiles());

            buttons.add(btnClean);
            buttons.add(Box.createHorizontalStrut(5));
            buttons.add(btnSelect);

            JLabel lblQueue = new JLabel("Cola de Archivos");
            top.add(lblQueue, BorderLayout.WEST);
            top.add(buttons, BorderLayout.EAST);

            txtFilter = new JTextField("Filtro (Regex): (?i).*\\.(mp4|mkv|avi|mov|flv|wmv|webm)$");

            JPanel topContainer = new JPanel(new BorderLayout());
            topContainer.add(top, BorderLayout.NORTH);
            topContainer.add(txtFilter, BorderLayout.SOUTH);

            panel.add(topContainer, BorderLayout.NORTH);

            fileList = new JList<>(listModel);
            fileList.setDragEnabled(true);
            fileList.setDropMode(DropMode.INSERT);
            fileList.setTransferHandler(new FileTransferHandler(listModel));
            panel.add(new JScrollPane(fileList), BorderLayout.CENTER);

            return panel;
        }

        private JPanel createRightPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(new EmptyBorder(10, 20, 10, 20));

            JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            ButtonGroup bgMode = new ButtonGroup();
            rbSimple = new JRadioButton("Modo Simple", true);
            rbAdvanced = new JRadioButton("Modo Avanzado");
            bgMode.add(rbSimple);
            bgMode.add(rbAdvanced);
            modePanel.add(rbSimple);
            modePanel.add(rbAdvanced);

            JPanel topConfig = new JPanel(new BorderLayout());
            topConfig.add(modePanel, BorderLayout.WEST);

            JButton btnConfig = new JButton("Configurar FFmpeg");
            btnConfig.addActionListener(e -> showFFmpegConfigDialog());
            topConfig.add(btnConfig, BorderLayout.EAST);

            panel.add(topConfig, BorderLayout.NORTH);

            JPanel form = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;

            int row = 0;

            addFormRow(form, "Resolución:",
                    cmbRes = new JComboBox<>(new String[] { "Original", "1920:1080", "1280:720", "854:480" }), gbc,
                    row++);
            addFormRow(form, "Extensión:",
                    cmbExt = new JComboBox<>(new String[] { "mp4", "webm", "mkv", "avi", "mov", "mp3" }), gbc, row++);

            cmbSimpleQuality = new JComboBox<>(new String[] {
                    "Baja (Rápido, Archivo Muy Pequeño)",
                    "Media (Balanceado)",
                    "Alta (Recomendado)",
                    "Ultra (Lento, Archivo Grande)"
            });
            cmbSimpleQuality.setSelectedIndex(2); // "Alta" default
            addFormRow(form, "Calidad Simple:", cmbSimpleQuality, gbc, row++);

            // Advanced Panel Container
            advancedPanel = new JPanel(new GridBagLayout());
            gbc.gridx = 0;
            gbc.gridy = row++;
            gbc.gridwidth = 2;
            advancedPanel.setBorder(new TitledBorder("Configuración Avanzada"));
            form.add(advancedPanel, gbc);

            panel.add(new JScrollPane(form), BorderLayout.CENTER);
            GridBagConstraints advGbc = new GridBagConstraints();
            advGbc.insets = new Insets(5, 5, 5, 5);
            advGbc.fill = GridBagConstraints.HORIZONTAL;
            advGbc.weightx = 1.0;
            int advRow = 0;

            addFormRow(advancedPanel, "Codec Video:",
                    cmbCodecV = new JComboBox<>(new String[] { "libx264", "libx265", "mpeg4", "copy", "vp9" }), advGbc,
                    advRow++);
            addFormRow(advancedPanel, "Codec Audio:",
                    cmbCodecA = new JComboBox<>(new String[] { "aac", "mp3", "copy", "opus" }),
                    advGbc, advRow++);
            addFormRow(advancedPanel, "Frame Rate:",
                    cmbFps = new JComboBox<>(new String[] { "Original", "24", "30", "60" }),
                    advGbc, advRow++);

            cmbBitrateV = new JComboBox<>(new String[] { "Default", "500k", "1000k", "2500k", "5000k", "8000k" });
            cmbBitrateV.setEditable(true);
            addFormRow(advancedPanel, "Video Bitrate:", cmbBitrateV, advGbc, advRow++);

            cmbBitrateA = new JComboBox<>(new String[] { "Default", "128k", "192k", "320k" });
            cmbBitrateA.setEditable(true);
            addFormRow(advancedPanel, "Audio Bitrate:", cmbBitrateA, advGbc, advRow++);

            addFormRow(advancedPanel, "Sample Rate:",
                    cmbSample = new JComboBox<>(new String[] { "Default", "44100", "48000" }),
                    advGbc, advRow++);
            addFormRow(advancedPanel, "CRF (Calidad Constante):",
                    cmbCrf = new JComboBox<>(
                            new String[] { "Default", "15 (Ultra)", "18 (High)", "23 (Standard)", "28 (Low)" }),
                    advGbc, advRow++);
            addFormRow(advancedPanel, "Preset (Velocidad):", cmbPreset = new JComboBox<>(new String[] { "Default",
                    "ultrafast", "superfast", "veryfast", "faster", "fast", "medium", "slow", "slower", "veryslow" }),
                    advGbc, advRow++);

            setupLogic();

            JPanel bottom = new JPanel();
            bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

            JPanel outputPanel = new JPanel(new BorderLayout(5, 5));
            outputPanel.setBorder(new TitledBorder("Carpeta de Salida:"));
            chkSameFolder = new JCheckBox("Guardar en la misma carpeta", true);
            txtOutputFolder = new JTextField();
            txtOutputFolder.setEnabled(false);
            btnBrowseFolder = new JButton("...");
            btnBrowseFolder.setEnabled(false);

            chkSameFolder.addActionListener(e -> {
                boolean sel = !chkSameFolder.isSelected();
                txtOutputFolder.setEnabled(sel);
                btnBrowseFolder.setEnabled(sel);
            });
            btnBrowseFolder.addActionListener(e -> browseOutputFolder());

            JPanel pathPanel = new JPanel(new BorderLayout(5, 0));
            pathPanel.add(txtOutputFolder, BorderLayout.CENTER);
            pathPanel.add(btnBrowseFolder, BorderLayout.EAST);

            outputPanel.add(chkSameFolder, BorderLayout.NORTH);
            outputPanel.add(pathPanel, BorderLayout.CENTER);

            JButton btnConvert = new JButton("CONVERTIR");
            btnConvert.setBackground(new Color(40, 167, 69));
            btnConvert.setForeground(Color.WHITE);
            btnConvert.setFont(new Font("Segoe UI", Font.BOLD, 16));
            btnConvert.setFocusPainted(false);
            btnConvert.setPreferredSize(new Dimension(0, 50));
            btnConvert.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            btnConvert.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnConvert.addActionListener(e -> startProcessing());

            bottom.add(outputPanel);
            bottom.add(Box.createVerticalStrut(10));
            bottom.add(btnConvert);
            bottom.add(Box.createVerticalStrut(5));

            cmbBitrateV.setSelectedItem("2500k");
            cmbRes.setSelectedItem("Original");

            panel.add(bottom, BorderLayout.SOUTH);
            return panel;
        }

        private void setupLogic() {
            ActionListener modeListener = e -> {
                boolean advanced = rbAdvanced.isSelected();
                advancedPanel.setVisible(advanced);
                cmbSimpleQuality.setEnabled(!advanced);

                if (!advanced) {
                    applySimpleQuality();
                }
                SwingUtilities.getWindowAncestor(advancedPanel).pack();
            };
            rbSimple.addActionListener(modeListener);
            rbAdvanced.addActionListener(modeListener);

            advancedPanel.setVisible(false);
            if (cmbSimpleQuality.getItemCount() > 0)
                applySimpleQuality();
            cmbSimpleQuality.addActionListener(e -> {
                if (rbSimple.isSelected()) {
                    applySimpleQuality();
                }
            });

            cmbExt.addActionListener(e -> {
                String ext = (String) cmbExt.getSelectedItem();
                if ("webm".equals(ext)) {
                    cmbCodecV.setSelectedItem("vp9");
                    cmbCodecA.setSelectedItem("opus");
                } else if ("mp4".equals(ext)) {
                    cmbCodecV.setSelectedItem("libx264");
                    cmbCodecA.setSelectedItem("aac");
                }
            });

            cmbBitrateV.addActionListener(e -> {
                if (rbAdvanced.isSelected()) {
                    String bit = (String) cmbBitrateV.getSelectedItem();
                    if (bit != null && !bit.equals("Default") && !bit.trim().isEmpty()) {
                        cmbCrf.setSelectedItem("Default");
                        cmbCrf.setSelectedItem("Default");
                    }
                }
            });

            cmbCrf.addActionListener(e -> {
                if (rbAdvanced.isSelected()) {
                    String crf = (String) cmbCrf.getSelectedItem();
                    if (crf != null && !crf.startsWith("Default")) {
                        cmbBitrateV.setSelectedItem("Default");
                    }
                }
            });
        }

        private void applySimpleQuality() {
            String q = (String) cmbSimpleQuality.getSelectedItem();
            if (q == null)
                return;

            if (q.startsWith("Baja")) {
                cmbCrf.setSelectedItem("28 (Low)");
                cmbPreset.setSelectedItem("veryfast");
            } else if (q.startsWith("Media")) {
                cmbCrf.setSelectedItem("23 (Standard)");
                cmbPreset.setSelectedItem("medium");
            } else if (q.startsWith("Alta")) {
                cmbCrf.setSelectedItem("18 (High)");
                cmbPreset.setSelectedItem("slow");
            } else if (q.startsWith("Ultra")) {
                cmbCrf.setSelectedItem("15 (Ultra)");
                cmbPreset.setSelectedItem("slower");
            }
            cmbBitrateV.setSelectedItem("Default");
        }

        private void addFormRow(JPanel panel, String label, JComponent comp, GridBagConstraints gbc, int row) {
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0;
            panel.add(new JLabel(label), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            panel.add(comp, gbc);
        }

        private void selectFiles() {
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(true);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                for (File f : fc.getSelectedFiles())
                    listModel.addElement(f);
            }
        }

        private void browseOutputFolder() {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                txtOutputFolder.setText(fc.getSelectedFile().getAbsolutePath());
            }
        }

        public void showFFmpegMissingDialog(String error) {
            Object[] options = { "Localizar FFmpeg...", "Buscar Automáticamente", "Descargar FFmpeg", "Salir" };
            int n = JOptionPane.showOptionDialog(this,
                    "FFmpeg no se encuentra en el sistema.\nEs necesario para convertir videos.\n\n" + error,
                    "Falta FFmpeg",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (n == 0) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Localizar ffmpeg.exe");
                if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    setFFmpeg(fc.getSelectedFile());
                } else {
                    System.exit(0);
                }
            } else if (n == 1) {
                searchSystemForFFmpeg();
            } else if (n == 2) {
                try {
                    Desktop.getDesktop().browse(java.net.URI.create(FFMPEG_URL));
                } catch (Exception ex) {
                }
                System.exit(0);
            } else {
                System.exit(0);
            }
        }

        private void setFFmpeg(File file) {
            ffmpegExe = file;
            File probe = new File(ffmpegExe.getParent(), "ffprobe.exe");
            if (probe.exists())
                ffprobeExe = probe;
            else
                ffprobeExe = ffmpegExe;
            JOptionPane.showMessageDialog(this, "FFmpeg localizado en: " + ffmpegExe.getAbsolutePath());
        }

        private void searchSystemForFFmpeg() {
            ProgressDialog progress = new ProgressDialog(this, 100);
            progress.progressBar.setIndeterminate(true);
            progress.progressBar.setString("Buscando ffmpeg.exe en todo el sistema...");
            progress.setTitle("Buscando FFmpeg...");
            progress.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                }
            });

            new SearchWorker(progress).execute();
            progress.setVisible(true);
        }

        class SearchWorker extends SwingWorker<Void, String> {
            private ProgressDialog progress;
            private boolean found = false;

            public SearchWorker(ProgressDialog progress) {
                this.progress = progress;
            }

            @Override
            protected Void doInBackground() {
                String binaryName = System.getProperty("os.name").toLowerCase().contains("win") ? "ffmpeg.exe"
                        : "ffmpeg";

                // PHASE 1: Priority Locations
                String userHome = System.getProperty("user.home");
                List<File> priorityDirs = new ArrayList<>();
                priorityDirs.add(new File(userHome, "Downloads"));
                priorityDirs.add(new File(userHome, "Desktop"));
                priorityDirs.add(new File(userHome, "Documents"));
                priorityDirs.add(new File("C:\\ffmpeg"));
                priorityDirs.add(new File("C:\\Program Files\\ffmpeg"));

                publish("Fase 1: Buscando en carpetas frecuentes...");
                for (File dir : priorityDirs) {
                    if (found || isCancelled())
                        return null;
                    if (dir.exists()) {
                        searchDirectory(dir, binaryName, true);
                    }
                }

                // PHASE 2 & 3: Full System Scan
                if (!found && !isCancelled()) {
                    File[] roots = File.listRoots();
                    if (roots == null)
                        return null;
                    List<File> sortedRoots = new ArrayList<>(Arrays.asList(roots));
                    // Sort C: to top
                    sortedRoots.sort((f1, f2) -> {
                        String p1 = f1.getAbsolutePath().toLowerCase();
                        String p2 = f2.getAbsolutePath().toLowerCase();
                        if (p1.startsWith("c"))
                            return -1;
                        if (p2.startsWith("c"))
                            return 1;
                        return p1.compareTo(p2);
                    });

                    for (File root : sortedRoots) {
                        if (found || isCancelled())
                            break;
                        publish("Buscando en unidad completa: " + root.getAbsolutePath());
                        searchDirectory(root, binaryName, false);
                    }
                }
                return null;
            }

            private void searchDirectory(File startDir, String targetName, boolean isPriority) {
                try {
                    Files.walkFileTree(startDir.toPath(), new SimpleFileVisitor<Path>() {
                        int count = 0;

                        @Override
                        public FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attrs) {
                            if (found || isCancelled())
                                return FileVisitResult.TERMINATE;

                            if (file.getFileName().toString().equalsIgnoreCase(targetName)) {
                                try {
                                    final boolean[] accepted = { false };
                                    SwingUtilities.invokeAndWait(() -> {
                                        JDialog dialog = new JDialog();
                                        dialog.setAlwaysOnTop(true);
                                        int r = JOptionPane.showConfirmDialog(GUIMode.this,
                                                "¡ENCONTRADO!\n\nArchivo: " + file.toAbsolutePath()
                                                        + "\n\n¿Desea utilizar este ejecutable?",
                                                "FFmpeg Encontrado",
                                                JOptionPane.YES_NO_OPTION,
                                                JOptionPane.QUESTION_MESSAGE);
                                        accepted[0] = (r == JOptionPane.YES_OPTION);
                                        dialog.dispose();
                                    });

                                    if (accepted[0]) {
                                        SwingUtilities.invokeAndWait(() -> setFFmpeg(file.toFile()));
                                        found = true;
                                        return FileVisitResult.TERMINATE;
                                    }
                                } catch (Exception e) {
                                }
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) {
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir,
                                java.nio.file.attribute.BasicFileAttributes attrs) {
                            if (found || isCancelled())
                                return FileVisitResult.TERMINATE;
                            count++;
                            int rate = isPriority ? 10 : 250;
                            if (count % rate == 0) {
                                publish("Escaneando: " + dir.toString());
                            }

                            String name = dir.getFileName().toString().toLowerCase();
                            // Windows folder is still skipped because it is massive and 99.9% irrelevant.
                            if (name.equals("$recycle.bin") || name.equals("system volume information")
                                    || name.equals("windows") || name.equals("boot")) {
                                return FileVisitResult.SKIP_SUBTREE;
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException e) {
                }
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    progress.logArea.setText(chunks.get(chunks.size() - 1));
                }
            }

            @Override
            protected void done() {
                progress.dispose();
                if (!found && ffmpegExe == null) {
                    showFFmpegMissingDialog("La búsqueda automática finalizó sin seleccionar ningún archivo.");
                }
            }
        }

        private void showFFmpegConfigDialog() {
            String currentPath = (ffmpegExe != null) ? ffmpegExe.getAbsolutePath() : "No configurado";
            JOptionPane.showMessageDialog(this, "FFmpeg está configurado en:\n" + currentPath, "Configurar FFmpeg",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        private void startProcessing() {
            if (listModel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "La cola de archivos está vacía.");
                return;
            }
            if (ffmpegExe == null) {
                showFFmpegMissingDialog("Binarios no cargados.");
                return;
            }

            VideoParams params = new VideoParams();
            params.resolution = (String) cmbRes.getSelectedItem();
            params.extension = (String) cmbExt.getSelectedItem();
            params.videoCodec = (String) cmbCodecV.getSelectedItem();
            params.audioCodec = (String) cmbCodecA.getSelectedItem();
            params.frameRate = (String) cmbFps.getSelectedItem();
            params.bitrateVideo = (String) cmbBitrateV.getSelectedItem();
            params.bitrateAudio = (String) cmbBitrateA.getSelectedItem();

            String sRate = (String) cmbSample.getSelectedItem();
            params.sampleRate = sRate.equals("Default") ? 0 : Integer.parseInt(sRate);

            params.crf = (String) cmbCrf.getSelectedItem();
            params.preset = (String) cmbPreset.getSelectedItem();

            params.sameFolder = chkSameFolder.isSelected();
            if (!params.sameFolder && !txtOutputFolder.getText().trim().isEmpty()) {
                params.outputFolder = new File(txtOutputFolder.getText());
            }

            ProgressDialog progress = new ProgressDialog(this, listModel.size());
            progress.setVisible(true);

            new SwingWorker<Void, String>() {
                @Override
                protected Void doInBackground() {
                    FFmpegEngine engine = new FFmpegEngine();
                    for (int i = 0; i < listModel.size(); i++) {
                        File f = listModel.get(i);
                        publish("Procesando: " + f.getName());
                        engine.processFile(f, null, params, msg -> publish(msg));
                        progress.incrementProgress();
                    }
                    return null;
                }

                @Override
                protected void process(List<String> chunks) {
                    for (String s : chunks)
                        progress.appendLog(s);
                }

                @Override
                protected void done() {
                    progress.finish();
                }
            }.execute();
        }
    }

    static class ProgressDialog extends JDialog {
        JTextArea logArea;
        JProgressBar progressBar;

        public ProgressDialog(Frame owner, int max) {
            super(owner, "Procesando...", false);
            setSize(600, 400);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());

            progressBar = new JProgressBar(0, max);
            progressBar.setStringPainted(true);
            add(progressBar, BorderLayout.NORTH);

            logArea = new JTextArea();
            logArea.setBackground(Color.BLACK);
            logArea.setForeground(Color.GREEN);
            logArea.setEditable(false);
            add(new JScrollPane(logArea), BorderLayout.CENTER);
        }

        public void appendLog(String text) {
            logArea.append(text + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }

        public void incrementProgress() {
            progressBar.setValue(progressBar.getValue() + 1);
        }

        public void finish() {
            progressBar.setString("¡Completado!");
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        }
    }

    static class FileTransferHandler extends TransferHandler {
        private DefaultListModel<File> model;

        public FileTransferHandler(DefaultListModel<File> model) {
            this.model = model;
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            try {
                List<File> files = (List<File>) support.getTransferable()
                        .getTransferData(DataFlavor.javaFileListFlavor);
                for (File f : files)
                    model.addElement(f);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    static class ConsoleMode {
        public void start() {
            System.out.println("Modo consola no actualizado para el nuevo formato.");
        }
    }
}