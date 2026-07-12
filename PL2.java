import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;

public class PL2 extends JFrame {

    // --- Core Data & Setup ---
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password123";
    private static final int MAX_FILES = 63;
    
    // Common Stop Words to filter out
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "the", "and", "a", "an", "to", "of", "in", "i", "is", "that", 
            "it", "on", "you", "this", "for", "but", "with", "are", "as", "be"
    ));

    private List<CompareResult> allResults = new ArrayList<>();
    
    // GUI Components
    private DefaultTableModel allTableModel;
    private DefaultTableModel summaryTableModel;
    private JLabel statusLabel;
    private JButton btnSelectFolder, btnRun, btnExport;
    private JCheckBox chkStopWords;
    private File selectedFolder;

    // --- Result Object ---
    static class CompareResult implements Comparable<CompareResult> {
        String file1, file2, remark;
        double similarity;

        public CompareResult(String file1, String file2, double similarity, String remark) {
            this.file1 = file1;
            this.file2 = file2;
            this.similarity = similarity;
            this.remark = remark;
        }

        @Override
        public int compareTo(CompareResult other) {
            return Double.compare(other.similarity, this.similarity);
        }
    }

    public PlagiarismDetectorGUI() {
        setTitle("Plagiarism Detector Pro v5.0");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen
        initUI();
    }

    // --- GUI Initialization ---
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        
        btnSelectFolder = new JButton("1. Select Folder");
        chkStopWords = new JCheckBox("Filter Stop Words (Yes/No)", true);
        btnRun = new JButton("2. Run Analysis");
        btnExport = new JButton("3. Export to CSV");
        btnExport.setEnabled(false); // Disabled until results exist
        statusLabel = new JLabel("Status: Waiting for folder...");
        statusLabel.setForeground(Color.BLUE);

        controlPanel.add(btnSelectFolder);
        controlPanel.add(chkStopWords);
        controlPanel.add(btnRun);
        controlPanel.add(btnExport);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(statusLabel);

        // Tables Setup
        String[] columnNames = {"File A", "File B", "Similarity (%)", "Remarks"};
        allTableModel = new DefaultTableModel(columnNames, 0);
        summaryTableModel = new DefaultTableModel(columnNames, 0);

        JTable allTable = new JTable(allTableModel);
        JTable summaryTable = new JTable(summaryTableModel);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("All Comparisons", new JScrollPane(allTable));
        tabbedPane.addTab("🚨 Action Required (>= 60%)", new JScrollPane(summaryTable));

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);

        // Button Actions
        btnSelectFolder.addActionListener(e -> selectFolder());
        btnRun.addActionListener(e -> runAnalysisLogic());
        btnExport.addActionListener(e -> exportToCSV());
    }

    // --- Application Actions ---
    private void selectFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int choice = chooser.showOpenDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            selectedFolder = chooser.getSelectedFile();
            statusLabel.setText("Status: Folder selected - " + selectedFolder.getName());
            statusLabel.setForeground(Color.BLACK);
        }
    }

    private void runAnalysisLogic() {
        if (selectedFolder == null) {
            JOptionPane.showMessageDialog(this, "Please select a folder first!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File[] files = selectedFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (files == null || files.length < 2) {
            JOptionPane.showMessageDialog(this, "Need at least 2 .txt files in the folder.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Limit files
        File[] limitedFiles = files.length > MAX_FILES ? Arrays.copyOfRange(files, 0, MAX_FILES) : files;
        boolean useStopWords = chkStopWords.isSelected();

        statusLabel.setText("Status: Analyzing " + limitedFiles.length + " files... Please wait.");
        statusLabel.setForeground(Color.RED);
        btnRun.setEnabled(false);
        allTableModel.setRowCount(0);
        summaryTableModel.setRowCount(0);
        allResults.clear();

        // Use SwingWorker to prevent the UI from freezing during heavy math
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Read all files into memory
                List<String> fileContents = new ArrayList<>();
                List<String> fileNames = new ArrayList<>();
                
                for (File file : limitedFiles) {
                    try {
                        fileContents.add(Files.readString(file.toPath()));
                        fileNames.add(file.getName());
                    } catch (IOException ignored) {}
                }

                // Setup Multithreading for fast processing
                ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                List<Future<CompareResult>> futures = new ArrayList<>();

                for (int i = 0; i < fileContents.size(); i++) {
                    for (int j = i + 1; j < fileContents.size(); j++) {
                        final String doc1 = fileContents.get(i);
                        final String doc2 = fileContents.get(j);
                        final String name1 = fileNames.get(i);
                        final String name2 = fileNames.get(j);

                        futures.add(executor.submit(() -> {
                            double sim = calculateSimilarity(doc1, doc2, useStopWords);
                            return new CompareResult(name1, name2, sim, getRemark(sim));
                        }));
                    }
                }

                // Gather results
                for (Future<CompareResult> future : futures) {
                    allResults.add(future.get());
                }
                executor.shutdown();
                Collections.sort(allResults); // Sort highest to lowest
                return null;
            }

            @Override
            protected void done() {
                // Update UI tables safely after background thread finishes
                for (CompareResult r : allResults) {
                    Object[] row = {r.file1, r.file2, String.format("%.2f%%", r.similarity), r.remark};
                    allTableModel.addRow(row);
                    if (r.similarity >= 60.0) {
                        summaryTableModel.addRow(row);
                    }
                }
                
                statusLabel.setText("Status: Analysis complete!");
                statusLabel.setForeground(new Color(0, 153, 0)); // Dark green
                btnRun.setEnabled(true);
                btnExport.setEnabled(true);
            }
        };
        worker.execute();
    }

    private void exportToCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Plagiarism_Report.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(chooser.getSelectedFile())) {
                fw.write("File A,File B,Similarity (%),Remarks\n");
                for (CompareResult r : allResults) {
                    fw.write(r.file1 + "," + r.file2 + "," + String.format("%.2f", r.similarity) + "," + r.remark + "\n");
                }
                JOptionPane.showMessageDialog(this, "Report Exported Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- Core Math & Algorithms ---
    private static double calculateSimilarity(String doc1, String doc2, boolean filterStopWords) {
        String[] w1 = tokenize(doc1, filterStopWords);
        String[] w2 = tokenize(doc2, filterStopWords);

        if (w1.length == 0 && w2.length == 0) return 100.0;
        if (w1.length == 0 || w2.length == 0) return 0.0;

        int m = w1.length, n = w2.length;
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (w1[i - 1].equals(w2[j - 1])) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        
        int maxLength = Math.max(w1.length, w2.length);
        return ((double) dp[m][n] / maxLength) * 100.0;
    }

    private static String[] tokenize(String text, boolean filterStopWords) {
        if (text == null || text.trim().isEmpty()) return new String[0];
        String[] rawWords = text.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase().trim().split("\\s+");
        
        if (!filterStopWords) return rawWords;

        // Apply Stop Word Filtering if checked
        List<String> filtered = new ArrayList<>();
        for (String word : rawWords) {
            if (!STOP_WORDS.contains(word)) {
                filtered.add(word);
            }
        }
        return filtered.toArray(new String[0]);
    }

    private static String getRemark(double sim) {
        if (sim >= 80.0) return "HIGH Plagiarism";
        if (sim >= 60.0) return "MODERATE Review";
        if (sim >= 40.0) return "LOW Similarity";
        return "Original";
    }

    // --- Entry Point & Login Security ---
    public static void main(String[] args) {
        // Set modern look and feel for the GUI
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}

        // 1. Launch Login Dialog
        JPanel loginPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(userField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passField);

        int result = JOptionPane.showConfirmDialog(null, loginPanel, "System Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String u = userField.getText().trim();
            String p = new String(passField.getPassword()).trim();
            
            if (u.equals(USERNAME) && p.equals(PASSWORD)) {
                JOptionPane.showMessageDialog(null, "Welcome back, " + u + "!", "Access Granted", JOptionPane.INFORMATION_MESSAGE);
                // 2. Launch Main Application
                SwingUtilities.invokeLater(() -> {
                    new PlagiarismDetectorGUI().setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Credentials. Access Denied.", "Security Alert", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        } else {
            System.exit(0); // User clicked Cancel
        }
    }
}