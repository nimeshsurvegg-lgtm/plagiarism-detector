import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class PlagiarismDetector1 {

    // --- Helper Class to Store and Sort Results ---
    static class CompareResult implements Comparable<CompareResult> {
        String file1;
        String file2;
        double similarity;

        public CompareResult(String file1, String file2, double similarity) {
            this.file1 = file1;
            this.file2 = file2;
            this.similarity = similarity;
        }

        // Sorts in descending order (highest similarity at the top)
        @Override
        public int compareTo(CompareResult other) {
            return Double.compare(other.similarity, this.similarity);
        }
    }

    // --- Core Logic Methods ---

    public static String[] normalizeAndTokenize(String text) {
        if (text == null || text.trim().isEmpty()) return new String[0];
        String normalized = text.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();
        return normalized.trim().split("\\s+");
    }

    public static int getLCSLength(String[] words1, String[] words2) {
        int m = words1.length;
        int n = words2.length;
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (words1[i - 1].equals(words2[j - 1])) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[m][n];
    }

    public static double calculateSimilarity(String doc1, String doc2) {
        String[] words1 = normalizeAndTokenize(doc1);
        String[] words2 = normalizeAndTokenize(doc2);

        if (words1.length == 0 && words2.length == 0) return 100.0;
        if (words1.length == 0 || words2.length == 0) return 0.0;

        int lcsLength = getLCSLength(words1, words2);
        int maxLength = Math.max(words1.length, words2.length);

        return ((double) lcsLength / maxLength) * 100.0;
    }

    public static String readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readString(path);
    }

    public static String getRemark(double similarity) {
        if (similarity >= 80.0) return "HIGH Plagiarism";
        if (similarity >= 60.0) return "MODERATE Review";
        if (similarity >= 40.0) return "LOW Similarity";
        return "Original";
    }

    private static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    // --- Main Execution ---

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<String> fileNames = new ArrayList<>();
        List<String> fileContents = new ArrayList<>();
        List<CompareResult> allResults = new ArrayList<>();

        final int MAX_FILES = 63;

        // 1. Welcome Message
        System.out.println("=========================================================");
        System.out.println("   ██████╗  █████╗ ████████╗ ██████╗██╗  ██╗             ");
        System.out.println("   ██╔══██╗██╔══██╗╚══██╔══╝██╔════╝██║  ██║             ");
        System.out.println("   ██████╦╝███████║   ██║   ██║     ███████║             ");
        System.out.println("   ██╔══██╗██╔══██║   ██║   ██║     ██╔══██║             ");
        System.out.println("   ██████╦╝██║  ██║   ██║   ╚██████╗██║  ██║             ");
        System.out.println("   ╚═════╝ ╚═╝  ╚═╝   ╚═╝    ╚═════╝╚═╝  ╚═╝             ");
        System.out.println("          PLAGIARISM DETECTOR PRO v3.0                   ");
        System.out.println("=========================================================\n");

        // 2. Choose Input Mode
        System.out.println("How would you like to load the files?");
        System.out.println("  1. Enter file names manually");
        System.out.println("  2. Scan a specific folder for all .txt files");
        System.out.print("Choice (1 or 2): ");
        
        int choice = 1;
        if (scanner.hasNextInt()) {
            choice = scanner.nextInt();
        }
        scanner.nextLine(); // consume newline

        // 3. Load Files based on choice
        if (choice == 2) {
            System.out.print("\nEnter the absolute folder path (e.g., C:\\Users\\Name\\Documents): ");
            String folderPath = scanner.nextLine().trim();
            File folder = new File(folderPath);

            if (folder.exists() && folder.isDirectory()) {
                // Find all .txt files in the folder
                File[] listOfFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
                
                if (listOfFiles != null) {
                    for (File file : listOfFiles) {
                        if (fileNames.size() >= MAX_FILES) {
                            System.out.println("\n[WARNING] Reached maximum limit of " + MAX_FILES + " files. Ignoring the rest.");
                            break;
                        }
                        try {
                            fileContents.add(readFile(file.getAbsolutePath()));
                            fileNames.add(file.getName());
                        } catch (IOException e) {
                            System.out.println(" -> [ERROR] Skipping " + file.getName() + ": Unreadable.");
                        }
                    }
                }
            } else {
                System.out.println("[ERROR] Invalid folder path. Exiting.");
                return;
            }
        } else {
            // Manual Entry Mode
            int numFiles = 0;
            while (true) {
                System.out.print("\nHow many files do you want to compare? (Min 2, Max " + MAX_FILES + "): ");
                if (scanner.hasNextInt()) {
                    numFiles = scanner.nextInt();
                    if (numFiles >= 2 && numFiles <= MAX_FILES) {
                        scanner.nextLine(); 
                        break;
                    }
                } else {
                    scanner.next(); 
                }
                System.out.println("Invalid input. Please enter a number between 2 and " + MAX_FILES + ".");
            }

            System.out.println("\n--- Enter File Names (e.g., paper1.txt) ---");
            for (int i = 0; i < numFiles; i++) {
                System.out.print("Name of File " + (i + 1) + ": ");
                String name = scanner.nextLine().trim();
                try {
                    fileContents.add(readFile(name));
                    fileNames.add(name);
                } catch (IOException e) {
                    System.out.println("  -> [ERROR] Could not read '" + name + "'. Skipping this file.");
                }
            }
        }

        int loadedCount = fileNames.size();
        if (loadedCount < 2) {
            System.out.println("\n[ERROR] Not enough valid files loaded to compare. Exiting.");
            return;
        }

        System.out.println("\nSuccessfully loaded " + loadedCount + " files. Processing combinations...");

        // 4. Generate combinations, compare, and store results
        System.out.println("\n================================================================================");
        System.out.println("                          ALL COMPARISONS TABLE");
        System.out.println("================================================================================");
        System.out.printf("%-20s | %-20s | %-12s | %-20s%n", "File A", "File B", "Similarity", "Remarks");
        System.out.println("--------------------------------------------------------------------------------");

        for (int i = 0; i < loadedCount; i++) {
            for (int j = i + 1; j < loadedCount; j++) {
                double similarity = calculateSimilarity(fileContents.get(i), fileContents.get(j));
                String remark = getRemark(similarity);
                
                // Store result for the summary table
                allResults.add(new CompareResult(fileNames.get(i), fileNames.get(j), similarity));

                System.out.printf("%-20s | %-20s | %11.2f%% | %-20s%n", 
                        truncate(fileNames.get(i), 20), 
                        truncate(fileNames.get(j), 20), 
                        similarity, 
                        remark);
            }
        }
        System.out.println("================================================================================\n");

        // 5. Generate High Similarity Summary Table (>= 60%)
        System.out.println("\n================================================================================");
        System.out.println("              🚨 ACTION REQUIRED: SUMMARY (>= 60% SIMILARITY) 🚨");
        System.out.println("================================================================================");
        System.out.printf("%-20s | %-20s | %-12s | %-20s%n", "File A", "File B", "Similarity", "Remarks");
        System.out.println("--------------------------------------------------------------------------------");

        // Sort the results using the CompareResult class logic (highest to lowest)
        Collections.sort(allResults);
        
        boolean foundHighSimilarity = false;
        
        for (CompareResult result : allResults) {
            if (result.similarity >= 60.0) {
                foundHighSimilarity = true;
                System.out.printf("%-20s | %-20s | %11.2f%% | %-20s%n", 
                        truncate(result.file1, 20), 
                        truncate(result.file2, 20), 
                        result.similarity, 
                        getRemark(result.similarity));
            }
        }

        if (!foundHighSimilarity) {
            System.out.println("No comparisons yielded a similarity of 60% or higher. All clear!");
        }
        
        System.out.println("================================================================================\n");
        scanner.close();
    }
}