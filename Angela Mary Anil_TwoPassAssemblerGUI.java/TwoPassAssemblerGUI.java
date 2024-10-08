import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

// Custom JButton to support rounded corners
class RoundedButton extends JButton {
    private final int radius;

    public RoundedButton(String text, int radius) {
        super(text);
        this.radius = radius;
        setBackground(Color.decode("#edede4")); // Set button color
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(180, 40); // Customize the button size as needed
    }
}

public class TwoPassAssemblerGUI {

    private static Map<String, Integer> symbolTable = new HashMap<>();
    private static Map<String, String> opcodeTable = new HashMap<>();
    private static int startingAddress = 0;
    private static int programLength = 0;

    // UI Components
    private static JTextArea intermediateArea;
    private static JTextArea symtabArea;
    private static JTextArea lengthArea;
    private static JTextArea outputArea;
    private static JTextArea objectCodeArea;
    private static JTextArea inputArea; // For displaying input file content
    private static JTextArea optabArea; // For displaying opcode table content

    private static File inputFile;
    private static File optabFile;

    public static void main(String[] args) {
        // Create JFrame
        JFrame frame = new JFrame("Two-Pass Assembler");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700); // Increased size for more components
        frame.getContentPane().setBackground(Color.decode("#d6f0e2")); // Light blue background

        // Create text areas for displaying results
        intermediateArea = createTextArea();
        symtabArea = createTextArea();
        lengthArea = createTextArea();
        outputArea = createTextArea();
        objectCodeArea = createTextArea();
        inputArea = createTextArea(); // Input file content
        optabArea = createTextArea(); // Opcode table content

        // Layout setup for input and optab
        JPanel inputPanel = new JPanel(new GridLayout(1, 2));
        inputPanel.add(createLabeledScrollPane("Input File:", inputArea));
        inputPanel.add(createLabeledScrollPane("Optab File:", optabArea));

        // Layout setup for Pass 1 and Pass 2
        JPanel passPanel = new JPanel(new GridLayout(1, 2));

        // Pass 1 Panel
        JPanel pass1Panel = new JPanel();
        pass1Panel.setLayout(new BoxLayout(pass1Panel, BoxLayout.Y_AXIS));

        // Center and enlarge the "Pass 1" label
        JLabel pass1Label = new JLabel("Pass 1", SwingConstants.CENTER);
        pass1Label.setFont(pass1Label.getFont().deriveFont(Font.BOLD, 18f)); // Bold and increased font size
        pass1Label.setAlignmentX(Component.CENTER_ALIGNMENT); // Center alignment

        // Add the centered label to the panel
        pass1Panel.add(pass1Label);

        // Create a new panel for the intermediate and symbol table areas
        JPanel intermediateSymtabPanel = new JPanel(new GridLayout(1, 2)); // Horizontal layout
        intermediateSymtabPanel.add(createLabeledScrollPane("Intermediate:", intermediateArea));
        intermediateSymtabPanel.add(createLabeledScrollPane("Symbol Table:", symtabArea));

        // Add the intermediate and symbol table panel to the pass 1 panel
        pass1Panel.add(intermediateSymtabPanel);

        // Add the program length area below
        pass1Panel.add(createLabeledScrollPane("Program Length:", lengthArea));

        // Pass 2 Panel
        JPanel pass2Panel = new JPanel();
        pass2Panel.setLayout(new BoxLayout(pass2Panel, BoxLayout.Y_AXIS));

        // Center and enlarge the "Pass 2" label
        JLabel pass2Label = new JLabel("Pass 2", SwingConstants.CENTER);
        pass2Label.setFont(pass2Label.getFont().deriveFont(Font.BOLD, 18f)); // Bold and increased font size
        pass2Label.setAlignmentX(Component.CENTER_ALIGNMENT); // Center alignment

        // Add the centered label to the panel
        pass2Panel.add(pass2Label);
        pass2Panel.add(createLabeledScrollPane("Output:", outputArea));
        pass2Panel.add(createLabeledScrollPane("Object Code:", objectCodeArea));


        // Add Pass panels to the main pass panel
        passPanel.add(pass1Panel);
        passPanel.add(pass2Panel);

        // Main panel to hold everything
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(inputPanel);
        mainPanel.add(passPanel);

        // Add main panel to frame
        frame.add(mainPanel, BorderLayout.CENTER);

        // Create buttons to load files
        RoundedButton loadInputButton = new RoundedButton("Load Input File", 20);
        RoundedButton loadOptabButton = new RoundedButton("Load Optab File", 20);
        RoundedButton runButton = new RoundedButton("Run Two-Pass Assembler", 20);
        
        // File chooser
        JFileChooser fileChooser = new JFileChooser();

        // Load Input File button action
        loadInputButton.addActionListener(e -> loadFile(fileChooser, true, frame));

        // Load Optab File button action
        loadOptabButton.addActionListener(e -> loadFile(fileChooser, false, frame));

        // Run button action
        runButton.addActionListener(e -> runAssembler(frame));

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.decode("#b8a9a3")); // Light blue for button panel
        buttonPanel.add(loadInputButton);
        buttonPanel.add(loadOptabButton);
        buttonPanel.add(runButton);

        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private static JTextArea createTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(Color.decode("#f1efeb")); // Light gray text area
        textArea.setBorder(BorderFactory.createLineBorder(Color.decode("#d3c4be"), 5)); // Light blue border
        Font currentFont = textArea.getFont();
        textArea.setFont(new Font(currentFont.getFontName(), Font.BOLD, (int)(currentFont.getSize() * 1)));

        // Set the font color to black
        textArea.setForeground(Color.BLACK);
        return textArea;
    }

    private static JScrollPane createLabeledScrollPane(String label, JTextArea textArea) {
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(label));
        return scrollPane;
    }

    private static void loadFile(JFileChooser fileChooser, boolean isInputFile, JFrame frame) {
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                if (isInputFile) {
                    inputFile = selectedFile;
                    loadTextFile(inputFile, inputArea);
                    JOptionPane.showMessageDialog(frame, "Input file loaded: " + inputFile.getName());
                } else {
                    optabFile = selectedFile;
                    loadTextFile(optabFile, optabArea);
                    JOptionPane.showMessageDialog(frame, "Optab file loaded: " + optabFile.getName());
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error loading file: " + ex.getMessage());
            }
        }
    }

    private static void loadTextFile(File file, JTextArea textArea) throws IOException {
        StringBuilder content = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }
        reader.close();
        textArea.setText(content.toString());
    }

    private static void runAssembler(JFrame frame) {
        if (inputFile == null || optabFile == null) {
            JOptionPane.showMessageDialog(frame, "Please load both input and optab files.");
            return;
        }

        try {
            loadOptabFromFile(optabFile);
            pass1(inputFile.getAbsolutePath());
            loadSymtab();
            pass2();
            JOptionPane.showMessageDialog(frame, "Assembling Completed!");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Error during assembling: " + ex.getMessage());
        }
    }

    // Load the Opcode Table from optab file
    private static void loadOptabFromFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        opcodeTable.clear();
        String line;

        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\\s+");
            if (parts.length == 2) {
                opcodeTable.put(parts[0], parts[1]);
            }
        }

        reader.close();
    }

    // Load the Symbol Table from symtab
    private static void loadSymtab() {
        StringBuilder symtabContent = new StringBuilder();
        for (Map.Entry<String, Integer> entry : symbolTable.entrySet()) {
            symtabContent.append(entry.getKey())
                    .append("\t")
                    .append(Integer.toHexString(entry.getValue()).toUpperCase())
                    .append("\n");
        }
        symtabArea.setText(symtabContent.toString());
    }

    // Pass 1 implementation
    private static void pass1(String inputFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        StringBuilder intermediateContent = new StringBuilder();
        StringBuilder lengthContent = new StringBuilder();

        String line;
        int locationCounter = 0;
        boolean isStartFound = false;

        while ((line = reader.readLine()) != null) {
            String[] parts = line.trim().split("\\s+");

            String label = "-", opcode = "-", operand = "-";

            if (parts.length > 0) {
                label = parts[0].equals("-") ? "-" : parts[0];
                opcode = (parts.length > 1 && !parts[1].equals("-")) ? parts[1] : "-";
                operand = (parts.length > 2 && !parts[2].equals("-")) ? parts[2] : "-";

                if (opcode.equals("START") && !isStartFound) {
                    startingAddress = Integer.parseInt(operand, 16);
                    locationCounter = startingAddress;
                    isStartFound = true;
                    intermediateContent.append("-\t").append(label).append("\t").append(opcode).append("\t").append(operand).append("\n");
                    continue;
                }

                if (!label.equals("-")) {
                    if (symbolTable.containsKey(label)) {
                        System.out.println("Error: Duplicate label - " + label);
                    } else {
                        symbolTable.put(label, locationCounter);
                    }
                }

                intermediateContent.append(Integer.toHexString(locationCounter).toUpperCase())
                        .append("\t")
                        .append(label)
                        .append("\t")
                        .append(opcode)
                        .append("\t")
                        .append(operand)
                        .append("\n");

                if (opcode.equals("WORD")) {
                    locationCounter += 3;
                } else if (opcode.equals("RESW")) {
                    locationCounter += 3 * Integer.parseInt(operand);
                } else if (opcode.equals("RESB")) {
                    locationCounter += Integer.parseInt(operand);
                } else if (opcode.equals("BYTE")) {
                    locationCounter += operand.length() - 3;
                } else if (opcodeTable.containsKey(opcode)) {
                    locationCounter += 3;
                }
            }
        }

        programLength = locationCounter - startingAddress;
        lengthContent.append("Program Length: ").append(Integer.toHexString(programLength).toUpperCase()).append("\n");

        intermediateArea.setText(intermediateContent.toString());
        lengthArea.setText(lengthContent.toString());

        reader.close();
    }

    // Pass 2 implementation
    private static void pass2() throws IOException {
        StringBuilder outputContent = new StringBuilder();
        StringBuilder objectCodeContent = new StringBuilder();
        StringBuilder textRecord = new StringBuilder();
        StringBuilder currentTextRecord = new StringBuilder();
        String currentTextStartAddress = "";
        int textRecordLength = 0;

        BufferedReader intermediateReader = new BufferedReader(new StringReader(intermediateArea.getText()));
        String line;

        String programName = "";
        String startAddressHex = Integer.toHexString(startingAddress).toUpperCase();

        while ((line = intermediateReader.readLine()) != null) {
            String[] parts = line.trim().split("\\s+");

            if (parts.length < 4) continue;

            String address = parts[0];
            String label = parts[1];
            String opcode = parts[2];
            String operand = parts[3];

            if (opcode.equals("START")) {
                programName = label;
                programName = String.format("%-6s", programName);
                break;
            }
        }

        objectCodeContent.append(String.format("H^%s^%06X^%06X\n", programName, startingAddress, programLength));

        intermediateReader.close();
        intermediateReader = new BufferedReader(new StringReader(intermediateArea.getText()));

        while ((line = intermediateReader.readLine()) != null) {
            String[] parts = line.trim().split("\\s+");

            if (parts.length < 4) continue;

            String address = parts[0];
            String label = parts[1];
            String opcode = parts[2];
            String operand = parts[3];
            String objectCode = "";

            if (opcodeTable.containsKey(opcode)) {
                String opCodeValue = opcodeTable.get(opcode);
                int operandAddress = 0;

                if (!operand.equals("-") && symbolTable.containsKey(operand)) {
                    operandAddress = symbolTable.get(operand);
                }

                objectCode = String.format("%s%04X", opCodeValue, operandAddress);
            } else if (opcode.equals("WORD")) {
                objectCode = String.format("%06X", Integer.parseInt(operand));
            } else if (opcode.equals("BYTE")) {
                if (operand.startsWith("C'")) {
                    StringBuilder asciiHex = new StringBuilder();
                    for (char c : operand.substring(2, operand.length() - 1).toCharArray()) {
                        asciiHex.append(String.format("%02X", (int) c));
                    }
                    objectCode = asciiHex.toString();
                } else if (operand.startsWith("X'")) {
                    objectCode = operand.substring(2, operand.length() - 1);
                }
            }

            outputContent.append(address).append("\t").append(label).append("\t").append(opcode).append("\t").append(operand).append("\t").append(objectCode).append("\n");

            if (!objectCode.isEmpty()) {
                if (currentTextRecord.length() == 0) {
                    currentTextStartAddress = address;
                }
                currentTextRecord.append("^").append(objectCode);
                textRecordLength += objectCode.length() / 2;
            }

            if (opcode.equals("END")) {
                break;
            }
        }

        if (currentTextRecord.length() > 0) {
            textRecord.append(String.format("T^%06X^%02X%s\n", Integer.parseInt(currentTextStartAddress, 16), textRecordLength, currentTextRecord.toString()));
        }

        objectCodeContent.append(textRecord.toString());
        objectCodeContent.append("E^").append(startAddressHex).append("\n");

        outputArea.setText(outputContent.toString());
        objectCodeArea.setText(objectCodeContent.toString());

        intermediateReader.close();
    }
}
