
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class TagExtractorGUI extends JFrame {

    private JTextArea outputArea;
    private JLabel fileLabel;
    private JLabel stopFileLabel;

    private File selectedTextFile;
    private File selectedStopFile;

    private Map<String, Integer> wordCounts = new TreeMap<>();
    private Set<String> stopWords = new TreeSet<>();

    public TagExtractorGUI() {

        setTitle("Tag / Keyword Extractor");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel filePanel = new JPanel(new GridLayout(2, 1));

        fileLabel = new JLabel("Selected Text File: None");
        stopFileLabel = new JLabel("Selected Stop Words: None");

        filePanel.add(fileLabel);
        filePanel.add(stopFileLabel);
        add(filePanel, BorderLayout.NORTH);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton textButton = new JButton("Select Text File");
        JButton stopButton = new JButton("Select Stop Words");
        JButton extractButton = new JButton("Extract Tags");
        JButton saveButton = new JButton("Save Results");
        JButton quitButton = new JButton("Quit");

        textButton.addActionListener(e -> selectTextFile());
        stopButton.addActionListener(e -> selectStopFile());
        extractButton.addActionListener(e -> extractTags());
        saveButton.addActionListener(e -> saveResults());
        quitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(textButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(extractButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(quitButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    // Select literature file
    private void selectTextFile() {

        JFileChooser chooser = new JFileChooser();

        if (chooser.showOpenDialog(this)
                == JFileChooser.APPROVE_OPTION) {

            selectedTextFile = chooser.getSelectedFile();

            fileLabel.setText(
                    "Selected Text File: " + selectedTextFile.getName());

            wordCounts.clear();

            outputArea.setText(
                    "Selected text file: " + selectedTextFile.getName()
                            + "\nClick Extract Tags to process it.");
        }
    }

    // Select stop words file
    private void selectStopFile() {

        JFileChooser chooser = new JFileChooser();

        if (chooser.showOpenDialog(this)
                == JFileChooser.APPROVE_OPTION) {

            selectedStopFile = chooser.getSelectedFile();

            stopFileLabel.setText(
                    "Selected Stop Words: " + selectedStopFile.getName());

            wordCounts.clear();

            outputArea.setText(
                    "Stop words file selected: "
                            + selectedStopFile.getName());
        }
    }

    // Load stop words into a TreeSet
    private void loadStopWords() throws IOException {

        stopWords.clear();

        try (Scanner scanner = new Scanner(selectedStopFile)) {

            while (scanner.hasNext()) {

                String word = scanner.next()
                        .toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-z]", "");

                if (!word.isEmpty()) {
                    stopWords.add(word);
                }
            }
        }
    }

    // Extract and count keywords
    private void extractTags() {

        if (selectedTextFile == null || selectedStopFile == null) {

            JOptionPane.showMessageDialog(this,
                    "Please select both a text file and a stop words file.");
            return;
        }

        try {

            loadStopWords();
            wordCounts.clear();

            // DEBUGGING INFORMATION
            System.out.println("========== DEBUG ==========");
            System.out.println(
                    "Text file: " + selectedTextFile.getAbsolutePath());
            System.out.println(
                    "Text file size: " + selectedTextFile.length());
            System.out.println(
                    "Stop words file: " + selectedStopFile.getAbsolutePath());
            System.out.println(
                    "Stop words loaded: " + stopWords.size());

            int totalWordsRead = 0;
            int acceptedWords = 0;

            // Read literature using a punctuation delimiter
            try (Scanner scanner = new Scanner(
                    selectedTextFile,
                    Charset.forName("windows-1252").name())) {

                scanner.useDelimiter("[^A-Za-z]+");

                while (scanner.hasNext()) {

                    String word = scanner.next()
                            .toLowerCase(Locale.ROOT);

                    if (!word.isEmpty()) {

                        totalWordsRead++;

                        if (!stopWords.contains(word)) {

                            wordCounts.put(
                                    word,
                                    wordCounts.getOrDefault(word, 0) + 1);

                            acceptedWords++;
                        }
                    }
                }
            }

            // MORE DEBUGGING INFORMATION
            System.out.println("Total words read: " + totalWordsRead);
            System.out.println("Accepted words: " + acceptedWords);
            System.out.println("Unique tags: " + wordCounts.size());
            System.out.println("===========================");

            displayResults();

            JOptionPane.showMessageDialog(this,
                    "Tag extraction completed!\n"
                            + "Unique tags: " + wordCounts.size());

        } catch (IOException ex) {

            JOptionPane.showMessageDialog(this,
                    "Error reading files: " + ex.getMessage(),
                    "File Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Display keywords and frequencies
    private void displayResults() {

        StringBuilder results = new StringBuilder();

        results.append("TAG / KEYWORD EXTRACTION RESULTS\n");
        results.append("File: ")
                .append(selectedTextFile.getName())
                .append("\n");

        results.append("Stop Words: ")
                .append(selectedStopFile.getName())
                .append("\n");

        results.append("Unique Tags: ")
                .append(wordCounts.size())
                .append("\n\n");

        results.append(String.format(
                "%-30s %s%n", "TAG", "FREQUENCY"));

        results.append(
                "------------------------------------------\n");

        for (Map.Entry<String, Integer> entry :
                wordCounts.entrySet()) {

            results.append(String.format(
                    "%-30s %d%n",
                    entry.getKey(),
                    entry.getValue()));
        }

        outputArea.setText(results.toString());
        outputArea.setCaretPosition(0);
    }

    // Save extracted keywords to a file
    private void saveResults() {

        if (wordCounts.isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "Please extract tags before saving.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("extracted_tags.txt"));

        if (chooser.showSaveDialog(this)
                == JFileChooser.APPROVE_OPTION) {

            File outputFile = chooser.getSelectedFile();

            if (outputFile.exists()) {

                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        "This file already exists. Overwrite it?",
                        "Confirm Save",
                        JOptionPane.YES_NO_OPTION);

                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            try (PrintWriter writer =
                         new PrintWriter(outputFile)) {

                writer.print(outputArea.getText());

                JOptionPane.showMessageDialog(this,
                        "Results saved successfully to:\n"
                                + outputFile.getAbsolutePath());

            } catch (FileNotFoundException ex) {

                JOptionPane.showMessageDialog(this,
                        "Error saving file: " + ex.getMessage(),
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
