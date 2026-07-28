/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package NGSGradle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import com.formdev.flatlaf.FlatLightLaf;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author iffy
 */
public class MainInstallationDialog extends javax.swing.JDialog {

    //INSTANCE VARIABLES
    private DefaultListModel model, model1;
    private String[] cranPkgs = { "BiocManager","Rcpp", "cpp11", "tidyverse", "RSQLite", "RColorBrewer", "pheatmap",
        "ggplot2", "gridExtra", "scales", "Cairo" ,"tidyr","dplyr","jsonlite","openxlsx", 
    "codetools", "Matrix", "nlme"
    };
    /*
    markdown(litedown), HDF5Array, glmGamPoi
    */
    
    private String[] biocPkgs = {"affy", "Biostrings", "limma", "shiny", "vsn","DelayedArray", "PCAtools", "DESeq2", "edgeR","AnnotationDbi",
        "enrichplot", "clusterProfiler","KEGGREST", "pathview", "ReactomePA"};
    private Map<String, String> envVars;
    private static final String CONFIG_FILE = System.getProperty("user.dir").concat("/src/main/resources/config.properties");
    public static final String LOG_PATH = System.getProperty("user.dir").concat("/logs/");

    public Map<String, String> toolPaths;

    public static String FastQC_PATH;
    public static String FastP_PATH;
    public static String MULTIQC_PATH;
    public static String Trimmomatic_PATH;
    public static String STAR_PATH;
    public static String BWA_PATH;
    public static String GATK_PATH;
    public static String PICARD_PATH;
    public static String SAMTOOLS_PATH;
    public static String BCFTOOLS_PATH;
    public static String SUBREAD_PATH;
    public static String SNPEFF_PATH;
    public static String IGV_PATH;
    public static String VEP_PATH;
   // public static String vcf2maf_PATH;
    public static String HTSLIB_PATH;
    public static BufferedImage bgInstall;
    public static org.apache.logging.log4j.Logger INSTALL_LOGGER = LogManager.getLogger("NGSGradle.App");
    public static StringWriter stack = new StringWriter();

    /**
     * Creates new form MainInstallationDialog
     */
    public MainInstallationDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        model = new DefaultListModel();
        model1 = new DefaultListModel();
        for (String s : cranPkgs) {
            model.addElement(s);
        }
        for (String s : biocPkgs) {
            model1.addElement(s);
        }
        try {
            bgInstall = ImageIO.read(new File(System.getProperty("user.dir").concat("/images/w13.jpg")));//w13
        } catch (IOException ex) {
            INSTALL_LOGGER.error("ERROR loading background image:" + ex);
        }

        initComponents();

        // SETTING SCREEN SIZE
        // Get screen size
//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//
//        // Set dialog size as a percentage of screen size
//        int width = (int) (screenSize.width * 0.5);  // 50% of screen width
//        int height = (int) (screenSize.height * 0.7); // 70% of screen height
//        setSize(width, height);
//
//        // Center the dialog on screen
//        setLocationRelativeTo(parent);
        //Creating LOGS Directory
        File logDir = new File(System.getProperty("user.dir").concat("/logs/"));

        if (!logDir.exists()) {
            if (logDir.mkdirs()) {
                System.out.println("Logs directory created.");
            } else {
                System.err.println("Failed to create logs directory!");
            }
        } else {
            System.out.println("Logs directory already exists.");
        }

        progressBar.setPreferredSize(new Dimension(300, 40)); // Fix height
        bottomPanel.add(installLabel, BorderLayout.NORTH);
        bottomPanel.add(progressBar, BorderLayout.SOUTH);
        projectPathTxt.setText(System.getProperty("user.dir"));
        FirstPanel.setVisible(true);
        pathPanel.setVisible(false);
        SecondPanel.setVisible(false);

    }

    //MEMBER FUNCTIONS:
    //1- UNZIPPED FILES
    public void runMake(String rFilePath) {

        installLabel.setText("INSTALLATION STARTED: Unzipping Files...");
        INSTALL_LOGGER.info("INSTALLATION STARTED: Unzipping Files...");

        progressBar.setValue(0);

        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;
            private int progressCounter = 0;

            @Override
            protected Integer doInBackground() {
                Process p;
                try {
                    List<String> cmdList = new ArrayList<String>();
msgTextArea.setText("");
                    cmdList.add("sh");
                    cmdList.add(rFilePath); //R Script Path
                    cmdList.add(System.getProperty("user.dir").concat("/zipbin"));  //Source
                    cmdList.add(System.getProperty("user.dir").concat("/bin"));// Bin

                    ProcessBuilder pb = new ProcessBuilder(cmdList);
                    pb.redirectErrorStream(true);
                    p = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        INSTALL_LOGGER.info("\r\n" + line);
                        publish("\r\n" + line);

                        // 🔹 Update progress every 10 lines (adjust as needed)
                        progressCounter++;
                        if (progressCounter % 50 == 0) {
                            int newProgress = Math.min(progressBar.getValue() + 5, 20);
                            setProgress(newProgress); // Update progress safely
                        }

                    }
                    if (!isCancelled()) {
                        status = p.waitFor();
                    }

                    p.destroy();

                } catch (IOException | InterruptedException ex) {

                    // ex.printStackTrace(new PrintWriter(stack));
                    INSTALL_LOGGER.error("\r\n ERROR in UNZIPPING FILES: " + stack.toString());
                    msgTextArea.append("\r\n ERROR in UNZIPPING FILES: " + stack.toString());

                }
                return status;
            }

            @Override
            protected void process(java.util.List<String> messages) {
                for (String message : messages) {
                    msgTextArea.append(message + "\n");
                }
            }

            @Override
            protected void done() {

                JPasswordField passwordField = new JPasswordField(15);
                // Display a prompt and the password field in the dialog
                Object[] dialogContent = {"Enter Administrative Password:", passwordField};
                int option = JOptionPane.showConfirmDialog(
                        null,
                        dialogContent,
                        "Password Prompt",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );

                if ((option == JOptionPane.OK_OPTION) && (passwordField.getText().isBlank() == false)) {
                    char[] pass = passwordField.getPassword();
                    String password = new String(pass);
                    //Change the paths to folders Base Directory
                    String binDirPath = System.getProperty("user.dir").concat("/bin/");
                    File binDir = new File(binDirPath);

                    // Tools to look for 
                    String[] toolPrefixes = {"fastqc", "fastp","multiqc", "Trimmomatic", "STAR", "bwa", "GATK", "picard", "samtools", "bcftools", "subread", "snpeff", "IGV", "ensembl-vep", "HTS"};
                    toolPaths = new HashMap<>();

                    // Initialize tool paths if /bin directory exists
                    if (binDir.exists() && binDir.isDirectory()) {
                        File[] files = binDir.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                if (file.isDirectory()) {
                                    // Handle directories
                                    String folderName = file.getName().toLowerCase();

                                    for (String prefix : toolPrefixes) {
                                        if (folderName.startsWith(prefix.toLowerCase())) {

                                            if (prefix.equalsIgnoreCase("STAR")) {
                                                toolPaths.put(prefix, file.getAbsolutePath().concat("/Linux_x86_64"));
                                            } else if (prefix.equalsIgnoreCase("multiqc")) {
                                                toolPaths.put(prefix, file.getAbsolutePath().concat("/multiqc"));

                                            } else if (prefix.equalsIgnoreCase("Trimmomatic")) {
                                                toolPaths.put(prefix, file.getAbsolutePath().concat("/trimmomatic-0.39.jar"));

                                            } else if(prefix.equalsIgnoreCase("snpeff")) {
                                                toolPaths.put(prefix, file.getAbsolutePath());

                                            } 
                                            else if(prefix.equalsIgnoreCase("subread")) {
                                                toolPaths.put(prefix, file.getAbsolutePath().concat("/bin"));

                                            }
                                            else {
                                                toolPaths.put(prefix, file.getAbsolutePath());
                                            }
                                            break;
                                        }
                                    }
                                } else if (file.isFile() && file.getName().endsWith(".jar")) {
                                    // Handle .jar files
                                    String jarName = file.getName().toLowerCase();
                                    for (String prefix : toolPrefixes) {
                                        if (jarName.startsWith(prefix.toLowerCase())) {
                                            toolPaths.put(prefix, file.getAbsolutePath());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        savePaths(toolPaths);
                    } else {
                        JOptionPane.showMessageDialog(MainInstallationDialog.this, "Directory " + binDirPath + " does not exist.", "ERROR", JOptionPane.ERROR_MESSAGE);
                        progressBar.setValue(0);
                    }
                    // Set the static constants
                    FastQC_PATH = toolPaths.getOrDefault("fastqc", "Not_Found");
                    FastP_PATH = toolPaths.getOrDefault("fastp", "Not_Found");
                    MULTIQC_PATH= toolPaths.getOrDefault("multiqc", "Not_Found");
                    Trimmomatic_PATH = toolPaths.getOrDefault("Trimmomatic", "Not_Found");
                    STAR_PATH = toolPaths.getOrDefault("STAR", "Not_Found");
                    BWA_PATH = toolPaths.getOrDefault("bwa", "Not_Found");
                    GATK_PATH = toolPaths.getOrDefault("GATK", "Not_Found");
                    PICARD_PATH = toolPaths.getOrDefault("picard", "Not_Found");
                    SAMTOOLS_PATH = toolPaths.getOrDefault("samtools", "Not_Found");
                    BCFTOOLS_PATH = toolPaths.getOrDefault("bcftools", "Not_Found");
                    SUBREAD_PATH = toolPaths.getOrDefault("subread", "Not_Found");
                    SNPEFF_PATH = toolPaths.getOrDefault("snpeff", "Not_Found");
                    IGV_PATH = toolPaths.getOrDefault("IGV", "Not_Found");
                    VEP_PATH = toolPaths.getOrDefault("ensembl-vep", "Not_Found");
                 //   vcf2maf_PATH = toolPaths.getOrDefault("vcf2maf", "Not_Found");
                    HTSLIB_PATH=toolPaths.getOrDefault("HTS", "Not_Found");

//SETTING INSTALLATION PATHS
                    jTextField1.setText(System.getProperty("user.dir").concat("/R_Libraries"));
                    cranBinaries.setText(System.getProperty("user.dir").concat("/bin/R_Packages"));
                    bioBinaries.setText(System.getProperty("user.dir").concat("/bin/R_Packages"));
                    p1.setText(FastQC_PATH);
                    p14.setText(MULTIQC_PATH);
                    p2.setText(Trimmomatic_PATH);
                    p3.setText(STAR_PATH);
                    p4.setText(BWA_PATH);
                    p5.setText(GATK_PATH);
                    p6.setText(PICARD_PATH);
                    p7.setText(SAMTOOLS_PATH);
                    p8.setText(BCFTOOLS_PATH);
                    p9.setText(SNPEFF_PATH);
                    p10.setText(SUBREAD_PATH);
                    p11.setText(FastP_PATH);
                    p12.setText(VEP_PATH);
                  //  p15.setText(vcf2maf_PATH);
                    p13.setText(HTSLIB_PATH);

                    int result;
                    try {
                        result = get();
                        if (result == 0) {
                            progressBar.setValue(20);
                            //Installing Softwares
                            SwingUtilities.invokeLater(() -> runMakee(System.getProperty("user.dir").concat("/bin/install.sh"), password));

                        } else {
                            installLabel.setText("ERROR: Unzipping Failed!");
                            progressBar.setValue(0);
                        }

                    } catch (InterruptedException | ExecutionException ex) {

                        //ex.printStackTrace(new PrintWriter(stack));
                        INSTALL_LOGGER.error("\r\n ERROR in UNZIPPING FILES: " + stack.toString());
                        msgTextArea.append("\r\n ERROR in UNZIPPING FILES: " + stack.toString());
                    }

                } else {
                    JOptionPane.showMessageDialog(MainInstallationDialog.this, "ERROR: Installation can't be completed without administrative privilages ", "ERROR", JOptionPane.ERROR_MESSAGE);

                }

            }

        };

        // Attach progress bar to SwingWorker
        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });

        worker.execute();

    }

    //2- INSTALL SOFTWARES
    public void runMakee(String rFilePath, String password) {

        installLabel.setText("INSTALATTION IN PROGRESS: Installing System Libraries...");
        INSTALL_LOGGER.info("INSTALATTION IN PROGRESS: Installing System Libraries...");
        progressBar.setValue(20);

        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;
            private int progressCounter = 0;

            @Override
            protected Integer doInBackground() {
                Process p;
                try {
                    String todayLog = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    String logFilePath = LOG_PATH + "app-" + todayLog + ".log";

                    File logFile = new File(logFilePath);
                    try {
                        if (!logFile.exists() && logFile.createNewFile()) {
                            msgTextArea.append("Log file created: " + logFilePath);
                        }
                    } catch (IOException e) {
                        INSTALL_LOGGER.error("Error while creating log file: " + e.getMessage());
                        msgTextArea.append("Error while creating log file: " + e.getMessage());
                    }

                    List<String> scriptArgs = Arrays.asList(FastP_PATH, STAR_PATH,
                            BWA_PATH, SAMTOOLS_PATH, BCFTOOLS_PATH, VEP_PATH,HTSLIB_PATH, password, logFilePath,System.getProperty("user.dir").concat("/R_Libraries"));
                    String joinedArgs = String.join(" ", scriptArgs);

                    ProcessBuilder pb = new ProcessBuilder("bash", "-c", "source " + rFilePath + " " + joinedArgs + " && env");

                    pb.redirectErrorStream(true);
                    p = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    envVars = new HashMap<>();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        INSTALL_LOGGER.info("\r\n" + line);
                        publish("\r\n" + line);

                        if (line.contains("=")) {
                            String[] parts = line.split("=", 2);
                            envVars.put(parts[0], parts[1]);
                        }

                        progressCounter++;
                        if (progressCounter % 50 == 0) {
                            int newProgress = Math.min(progressBar.getValue() + 5, 40);
                            setProgress(newProgress); // Update progress safely
                        }

                    }//while

                    if (!isCancelled()) {
                        status = p.waitFor();
                    }

                    p.destroy();

                } catch (IOException | InterruptedException ex) {

                    ex.printStackTrace(new PrintWriter(stack));
                    INSTALL_LOGGER.error("\r\n ERROR in MAKING FILES: " + stack);
                    msgTextArea.append("\r\n ERROR in MAKING FILES: " + stack);

                }
                return status;
            }

            @Override
            protected void process(java.util.List<String> messages) {
                for (String message : messages) {
                    msgTextArea.append(message + "\n");
                }
            }

            @Override
            protected void done() {

                String cp = String.join(",", cranPkgs);
                String bp = String.join(",", biocPkgs);
                //Display R Package Tab
                FirstPanel.setVisible(false);
                SecondPanel.setVisible(true);

                int result;
                try {
                    result = get();

                    if (result == 0) {
                        progressBar.setValue(40);
                        //Installing Softwares
                        SwingUtilities.invokeLater(() -> runR(System.getProperty("user.dir").concat("/bin/installRLibraries.R"),
                                System.getProperty("user.dir").concat("/R_Libraries"),
                                System.getProperty("user.dir").concat("/bin/R_Packages"),
                                cp, bp, "offline", "ALL", "","4.5.1"));

                    } else {
                        installLabel.setText("ERROR: Software Installation Failed!");
                        progressBar.setValue(0);
                    }

                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace(new PrintWriter(stack));
                    INSTALL_LOGGER.error("\r\n ERROR in SOFTWARE INSTALLATION: " + stack.toString());
                    msgTextArea.append("\r\n ERROR in SOFTWARE INSTALLATION: " + stack.toString());
                }

            }

        };

        // Attach progress bar to SwingWorker
        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });
        worker.execute();

    }

    //3- CAPTURING ENVIRONMENT VARIABLES
    private Map<String, String> runBashScriptAndCaptureEnv(String scriptPath, List<String> args) throws IOException, InterruptedException {
        // Convert args to a single string
        String joinedArgs = String.join(" ", args);

        // Run the script with arguments & capture env
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", "source " + scriptPath + " " + joinedArgs + " && env");
        pb.redirectErrorStream(true);

        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        Map<String, String> envVars = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null) {
            INSTALL_LOGGER.info("\r\n" + line);

            if (line.contains("=")) {
                String[] parts = line.split("=", 2);
                envVars.put(parts[0], parts[1]);
            }
        }

        process.waitFor();
        process.destroy();

        return envVars;
    }

    //4- INSTALLING R PACKAGES
    public void runR(String rFilePath, String installIn, String cranB, String cpkg, String bpkg, String mode,
            String pkgMode, String pkgName,String RVersion) {

        installLabel.setText("INSTALLATION IN PROGRESS: Installing R Packages...");
        INSTALL_LOGGER.info("INSTALLATION IN PROGRESS: Installing R Packages...");
        progressBar.setValue(40); // Reset progress

        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;
            private int progressCounter = 0;

            @Override
            protected Integer doInBackground() {
                Process p;
                try {
                    List<String> cmdList = new ArrayList<String>();

                    cmdList.add("Rscript");
                    cmdList.add(rFilePath); //R Script Path
                    cmdList.add(installIn);  //R Library Path: Install in 1
                    
                    cmdList.add(cranB);// 2 Binary folder
                    cmdList.add("INSTALL"); //3
                    
                    cmdList.add(RVersion);//4
                    cmdList.add(cpkg);//5
                    cmdList.add(bpkg);//6
                    cmdList.add(mode);//7
                    //Settings for Single
                    cmdList.add(pkgMode);//8 "All" "Single
                    cmdList.add(pkgName);//9

                    ProcessBuilder pb = new ProcessBuilder(cmdList);
                    //if(pkgMode.equalsIgnoreCase("all"))
                    pb.environment().putAll(envVars);
                    pb.redirectErrorStream(true);
                    p = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        INSTALL_LOGGER.info("\r\n" + line);
                        publish("\r\n" + line);

                        progressCounter++;
                        if (progressCounter % 100 == 0) {
                            int newProgress = Math.min(progressBar.getValue() + 5, 100);
                            setProgress(newProgress); // Update progress safely
                        }

                    }
                    if (!isCancelled()) {
                        status = p.waitFor();
                    }

                    p.destroy();

                } catch (IOException | InterruptedException ex) {

                    ex.printStackTrace(new PrintWriter(stack));
                    INSTALL_LOGGER.error("\r\n ERROR in R Packages: " + stack);

                }

                return status;
            }

            @Override
            protected void process(java.util.List<String> messages) {
                for (String message : messages) {
                    msgTextArea.append(message + "\n");
                }
            }

            @Override
            protected void done() {
                installLabel.setText("INSTALLATION COMPLETED");
                INSTALL_LOGGER.info("INSTALLATION COMPLETED");
                SecondPanel.setVisible(false);
                pathPanel.setVisible(true);

                int result;
                try {
                    result = get();
                    if (result == 0) {
                        progressBar.setValue(100);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace(new PrintWriter(stack));
                    INSTALL_LOGGER.error("\r\n ERROR in SOFTWARE INSTALLATION: " + stack.toString());
                    msgTextArea.append("\r\n ERROR in SOFTWARE INSTALLATION: " + stack.toString());
                }

            }

        };

        // Attach progress bar to worker
        worker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });
        worker.execute();

    }
 

    //5- Saving toolspath to Config File
    private void savePaths(Map<String, String> toolPaths) {
        File configFile = new File(CONFIG_FILE);
        configFile.getParentFile().mkdirs(); // Ensure directory exists

        try ( FileWriter writer = new FileWriter(configFile)) {
            Properties properties = new Properties();
            for (Map.Entry<String, String> entry : toolPaths.entrySet()) {
                properties.setProperty(entry.getKey(), entry.getValue());
            }
            properties.store(writer, "Tool Paths Configuration");
            INSTALL_LOGGER.info("Installation completed. Paths saved at: " + CONFIG_FILE);
        } catch (IOException e) {

            INSTALL_LOGGER.error("ERROR in CONFIG_FILE:" + e.getMessage());
            msgTextArea.append("ERROR in CONFIG_FILE:" + e.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new javax.swing.JPanel(){
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(bgInstall, 0, 0, null);
            }
        };
        FirstPanel = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jLabel32 = new javax.swing.JLabel();
        projectPathTxt = new javax.swing.JTextField();
        jLabel55 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        SecondPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>(model);
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList<>(model1);
        jLabel15 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        pathPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        cranBinaries = new javax.swing.JTextField();
        bioBinaries = new javax.swing.JTextField();
        jLabel42 = new javax.swing.JLabel();
        p1 = new javax.swing.JTextField();
        jLabel43 = new javax.swing.JLabel();
        p2 = new javax.swing.JTextField();
        p3 = new javax.swing.JTextField();
        jLabel44 = new javax.swing.JLabel();
        p4 = new javax.swing.JTextField();
        jLabel45 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        p5 = new javax.swing.JTextField();
        p6 = new javax.swing.JTextField();
        jLabel47 = new javax.swing.JLabel();
        p7 = new javax.swing.JTextField();
        jLabel48 = new javax.swing.JLabel();
        jLabel49 = new javax.swing.JLabel();
        p8 = new javax.swing.JTextField();
        p9 = new javax.swing.JTextField();
        jLabel50 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        p10 = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel56 = new javax.swing.JLabel();
        p11 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel54 = new javax.swing.JLabel();
        p12 = new javax.swing.JTextField();
        jLabel57 = new javax.swing.JLabel();
        p13 = new javax.swing.JTextField();
        jLabel58 = new javax.swing.JLabel();
        p14 = new javax.swing.JTextField();
        jLabel59 = new javax.swing.JLabel();
        p15 = new javax.swing.JTextField();
        bottomPanel = new javax.swing.JPanel(){
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(bgInstall, 0, 0, null);
            }

        };
        progressBar = new javax.swing.JProgressBar();
        installLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        msgTextArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("NGS-CDS INSTALLER");

        topPanel.setLayout(new java.awt.CardLayout());

        FirstPanel.setOpaque(false);

        jLabel7.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Welcome to the NGS Cancer Diagnostic Suite Installer!  ");

        jLabel14.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setText("This wizard will guide you through the installation process of all essential pipeline software and R packages required.");

        jLabel16.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("Please ensure you have administrative privileges on your system and that the necessary dependencies , such as");

        jLabel17.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setText("in its latest version and ");

        jLabel18.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(255, 255, 255));
        jLabel18.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/python.png"))); // NOI18N
        jLabel18.setText("Python");

        jLabel19.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(255, 255, 255));
        jLabel19.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/java.png"))); // NOI18N
        jLabel19.setText("JDK");

        jLabel20.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(255, 255, 255));
        jLabel20.setText(" 11 or higher and enough disk space before proceeding.");

        jLabel21.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(255, 255, 255));
        jLabel21.setText("The NGS Cancer Diagnostic Suite will automatically install the following pre-existing binaries from within the suite itself:");

        jLabel22.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(255, 255, 255));
        jLabel22.setText("fastqc_v0.12.1");

        jLabel23.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(255, 255, 255));
        jLabel23.setText("Trimmomatic-0.40");

        jLabel24.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setText("Picard 3.4.0");

        jLabel25.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(255, 255, 255));
        jLabel25.setText("STAR_2.7.11b");

        jLabel26.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(255, 255, 255));
        jLabel26.setText("bwa-0.7.19");

        jLabel27.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(255, 255, 255));
        jLabel27.setText("gatk-4.6.2.0");

        jLabel28.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(255, 255, 255));
        jLabel28.setText("samtools-1.23");

        jLabel29.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(255, 255, 255));
        jLabel29.setText("bcftools-1.23");

        jLabel30.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(255, 255, 255));
        jLabel30.setText("snpeff 5.2c");

        jLabel31.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(255, 255, 255));
        jLabel31.setText("subread-2.1.1");

        jButton6.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jButton6.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/report3.png"))); // NOI18N
        jButton6.setText("Begin Installation");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel32.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(255, 255, 255));
        jLabel32.setText("By default, all pipeline software and R packages will be installed in project folder:");

        projectPathTxt.setEditable(false);
        projectPathTxt.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N

        jLabel55.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel55.setForeground(new java.awt.Color(255, 255, 255));
        jLabel55.setText("fastp-1.1.0");

        jLabel52.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel52.setForeground(new java.awt.Color(255, 255, 255));
        jLabel52.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/R.png"))); // NOI18N
        jLabel52.setText("R version 4.5.1");

        jLabel35.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(255, 255, 255));
        jLabel35.setText("IGV_Linux_2.19.4");

        jLabel36.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel36.setForeground(new java.awt.Color(255, 255, 255));
        jLabel36.setText("bamtools-1.23");

        jLabel37.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel37.setForeground(new java.awt.Color(255, 255, 255));
        jLabel37.setText("VEP 115");

        javax.swing.GroupLayout FirstPanelLayout = new javax.swing.GroupLayout(FirstPanel);
        FirstPanel.setLayout(FirstPanelLayout);
        FirstPanelLayout.setHorizontalGroup(
            FirstPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FirstPanelLayout.createSequentialGroup()
                .addGroup(FirstPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(FirstPanelLayout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jLabel7))
                    .addGroup(FirstPanelLayout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jLabel14))
                    .addGroup(FirstPanelLayout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jLabel16))
                    .addGroup(FirstPanelLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jLabel21))
                    .addGroup(FirstPanelLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jLabel22)
                        .addGap(84, 84, 84)
                        .addComponent(jLabel23))
                    .addGroup(FirstPanelLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(FirstPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel28)
                            .addComponent(jLabel31)
                            .addComponent(jLabel36))
                        .addGap(86, 86, 86)
                        .addGroup(FirstPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel30)
                            .addComponent(jLabel35)
                            .addComponent(jLabel37)))
                    .addGroup(FirstPanelLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jLabel32))
                    .addGroup(FirstPanelLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(projectPathTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 910, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(FirstPanelLayout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jLabel52)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel18)
                        .addGap(6, 6, 6)
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel19)
                        .addGap(6, 6, 6)
                        .addComponent(jLabel20))
                    .addGroup(FirstPanelLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(FirstPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel24)
                            .addComponent(jLabel26)
                            .addComponent(jLabel55))
                        .addGap(100, 100, 100)
                        .addGroup(FirstPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel25)
                            .addGroup(FirstPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel27)
                                .addGroup(FirstPanelLayout.createSequentialGroup()
                                    .addComponent(jLabel29)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(25, 25, 25))))))
                .addContainerGap(202, Short.MAX_VALUE))
        );
        FirstPanelLayout.setVerticalGroup(
            FirstPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FirstPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addGroup(FirstPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(FirstPanelLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jLabel19))
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(FirstPanelLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel18))
                    .addComponent(jLabel52))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(FirstPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(FirstPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel55, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(FirstPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(FirstPanelLayout.createSequentialGroup()
                        .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(FirstPanelLayout.createSequentialGroup()
                        .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addGroup(FirstPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(FirstPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(FirstPanelLayout.createSequentialGroup()
                        .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(6, 6, 6)
                        .addGroup(FirstPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(FirstPanelLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(12, 12, 12)
                .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addComponent(projectPathTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(80, Short.MAX_VALUE))
        );

        topPanel.add(FirstPanel, "card2");

        SecondPanel.setBackground(new java.awt.Color(0, 0, 0));
        SecondPanel.setOpaque(false);

        jLabel1.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Default CRAN Packages:");

        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.setToolTipText("");
        jScrollPane1.setViewportView(jList1);

        jLabel2.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Default BiocConductor Packages:");

        jList2.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList2.setToolTipText("");
        jScrollPane2.setViewportView(jList2);

        jLabel15.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setText("All necessary R package binaries are pre-included in the suite. ");

        jLabel33.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel33.setForeground(new java.awt.Color(255, 255, 255));
        jLabel33.setText("The following R and Bioconductor packages will be installed from local binaries");

        jLabel34.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(255, 255, 255));
        jLabel34.setText("This step may take a few minutes. Please wait...");

        jLabel38.setFont(new java.awt.Font("Liberation Sans", 0, 14)); // NOI18N
        jLabel38.setForeground(new java.awt.Color(255, 255, 255));
        jLabel38.setText("If you encounter any issues during the installation process, you can install individual packages separately using the ");

        jLabel39.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        jLabel39.setForeground(new java.awt.Color(255, 255, 255));
        jLabel39.setText("Note: ");

        jLabel40.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(255, 255, 255));
        jLabel40.setText("\"Install Single Package\"");

        jLabel41.setFont(new java.awt.Font("Liberation Sans", 0, 14)); // NOI18N
        jLabel41.setForeground(new java.awt.Color(255, 255, 255));
        jLabel41.setText("dialog tab laters.");

        javax.swing.GroupLayout SecondPanelLayout = new javax.swing.GroupLayout(SecondPanel);
        SecondPanel.setLayout(SecondPanelLayout);
        SecondPanelLayout.setHorizontalGroup(
            SecondPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SecondPanelLayout.createSequentialGroup()
                .addGroup(SecondPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SecondPanelLayout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addGroup(SecondPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(SecondPanelLayout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(218, 218, 218)
                                .addComponent(jLabel2))
                            .addGroup(SecondPanelLayout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(24, 24, 24)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(SecondPanelLayout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addGroup(SecondPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(SecondPanelLayout.createSequentialGroup()
                                .addComponent(jLabel38)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel40)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel41))
                            .addComponent(jLabel15)
                            .addComponent(jLabel33)
                            .addGroup(SecondPanelLayout.createSequentialGroup()
                                .addComponent(jLabel39)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel34)))))
                .addContainerGap(100, Short.MAX_VALUE))
            .addGroup(SecondPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator1))
        );
        SecondPanelLayout.setVerticalGroup(
            SecondPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SecondPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SecondPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(SecondPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel38, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel40, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel41, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(SecondPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(SecondPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        topPanel.add(SecondPanel, "card3");

        pathPanel.setOpaque(false);

        jLabel3.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Installation Complete!");

        jLabel4.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("R Binaries CRAN Folder Path:");

        jLabel6.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("R Binaries Biocondutor Folder Path:");

        jTextField1.setEditable(false);
        jTextField1.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        cranBinaries.setEditable(false);
        cranBinaries.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        bioBinaries.setEditable(false);
        bioBinaries.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        jLabel42.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel42.setForeground(new java.awt.Color(255, 255, 255));
        jLabel42.setText("FastQC Path:");

        p1.setEditable(false);
        p1.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        jLabel43.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel43.setForeground(new java.awt.Color(255, 255, 255));
        jLabel43.setText("Trimmomatic Path:");

        p2.setEditable(false);
        p2.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        p3.setEditable(false);
        p3.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        jLabel44.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel44.setForeground(new java.awt.Color(255, 255, 255));
        jLabel44.setText("STAR Path:");

        p4.setEditable(false);
        p4.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        jLabel45.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel45.setForeground(new java.awt.Color(255, 255, 255));
        jLabel45.setText("Bowtie2 Path:");

        jLabel46.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel46.setForeground(new java.awt.Color(255, 255, 255));
        jLabel46.setText("GATK Path:");

        p5.setEditable(false);
        p5.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        p6.setEditable(false);
        p6.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        jLabel47.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(255, 255, 255));
        jLabel47.setText("Picard Path:");

        p7.setEditable(false);
        p7.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        jLabel48.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel48.setForeground(new java.awt.Color(255, 255, 255));
        jLabel48.setText("SAMTools Path");

        jLabel49.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel49.setForeground(new java.awt.Color(255, 255, 255));
        jLabel49.setText("BCFTools Path:");

        p8.setEditable(false);
        p8.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        p9.setEditable(false);
        p9.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        jLabel50.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel50.setForeground(new java.awt.Color(255, 255, 255));
        jLabel50.setText("SNPEff Path:");

        jLabel51.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel51.setForeground(new java.awt.Color(255, 255, 255));
        jLabel51.setText("Subread Path:");

        p10.setEditable(false);
        p10.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        jLabel56.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel56.setForeground(new java.awt.Color(255, 255, 255));
        jLabel56.setText("FastP Path:");

        p11.setEditable(false);
        p11.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("NGS Suite has been successfully installed.");

        jLabel8.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("R_Libraries Folder Path:");

        jLabel9.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Installed Software Paths:");

        jLabel53.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel53.setForeground(new java.awt.Color(255, 255, 255));
        jLabel53.setText("Click FINISH to launch the application");

        jButton1.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButton1.setText("FINISH");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel54.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel54.setForeground(new java.awt.Color(255, 255, 255));
        jLabel54.setText("VEP Path:");

        p12.setEditable(false);
        p12.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        jLabel57.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel57.setForeground(new java.awt.Color(255, 255, 255));
        jLabel57.setText("HTSLIB Path");

        p13.setEditable(false);
        p13.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        jLabel58.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel58.setForeground(new java.awt.Color(255, 255, 255));
        jLabel58.setText("MultiQC Path:");

        p14.setEditable(false);
        p14.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        jLabel59.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel59.setForeground(new java.awt.Color(255, 255, 255));
        jLabel59.setText("Annovar Path:");

        p15.setEditable(false);
        p15.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        javax.swing.GroupLayout pathPanelLayout = new javax.swing.GroupLayout(pathPanel);
        pathPanel.setLayout(pathPanelLayout);
        pathPanelLayout.setHorizontalGroup(
            pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pathPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator2)
                .addContainerGap())
            .addGroup(pathPanelLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel42)
                    .addComponent(jLabel43)
                    .addComponent(jLabel44)
                    .addComponent(jLabel45)
                    .addComponent(jLabel46)
                    .addComponent(jLabel47)
                    .addComponent(jLabel48)
                    .addComponent(jLabel49)
                    .addComponent(jLabel50)
                    .addComponent(jLabel51)
                    .addComponent(jLabel56)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9)
                    .addComponent(jLabel54)
                    .addComponent(jLabel57)
                    .addComponent(jLabel58)
                    .addComponent(jLabel59))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pathPanelLayout.createSequentialGroup()
                        .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 794, Short.MAX_VALUE)
                            .addComponent(cranBinaries, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(bioBinaries, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(p1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(p2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(p3, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(p4, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(p5, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(p6, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(p7, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(p8, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(p9, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(p10, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(p11, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(p12, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(p13, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(p14, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(p15, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(109, 109, 109))
                    .addGroup(pathPanelLayout.createSequentialGroup()
                        .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 511, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(pathPanelLayout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(jLabel53)
                                .addGap(18, 18, 18)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        pathPanelLayout.setVerticalGroup(
            pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pathPanelLayout.createSequentialGroup()
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pathPanelLayout.createSequentialGroup()
                        .addGap(91, 91, 91)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pathPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel9)
                        .addGap(32, 32, 32)
                        .addComponent(jLabel8)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cranBinaries, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(bioBinaries, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel42)
                    .addComponent(p1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(p11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel56))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(p14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel58))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel43)
                    .addComponent(p2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel44)
                    .addComponent(p3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel45)
                    .addComponent(p4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel46)
                    .addComponent(p5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel47)
                    .addComponent(p6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel48)
                    .addComponent(p7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel49)
                    .addComponent(p8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel50)
                    .addComponent(p9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel51)
                    .addComponent(p10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel54)
                    .addComponent(p12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel59)
                    .addComponent(p15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel57)
                    .addComponent(p13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel53)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40))
        );

        topPanel.add(pathPanel, "card4");

        getContentPane().add(topPanel, java.awt.BorderLayout.CENTER);

        bottomPanel.setLayout(new java.awt.BorderLayout());

        progressBar.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        progressBar.setForeground(javax.swing.UIManager.getDefaults().getColor("Actions.Blue"));
        progressBar.setStringPainted(true);
        bottomPanel.add(progressBar, java.awt.BorderLayout.LINE_END);

        installLabel.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        installLabel.setForeground(new java.awt.Color(255, 255, 255));
        installLabel.setText("Begin Installation:");
        bottomPanel.add(installLabel, java.awt.BorderLayout.LINE_START);

        msgTextArea.setEditable(false);
        msgTextArea.setColumns(20);
        msgTextArea.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N
        msgTextArea.setLineWrap(true);
        msgTextArea.setRows(8);
        jScrollPane3.setViewportView(msgTextArea);

        bottomPanel.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed

        runMake(System.getProperty("user.dir").concat("/bin/unzip.sh"));

    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        this.dispose();
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", "NGSSuite.jar");
            pb.start();  // Launch the main app
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(MainInstallationDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(MainInstallationDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(MainInstallationDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(MainInstallationDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>

        try {
            UIManager.setLookAndFeel(new FlatLightLaf()); // Use FlatDarkLaf() for dark mode
        } catch (UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainInstallationDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }


        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                MainInstallationDialog dialog = new MainInstallationDialog(new javax.swing.JFrame(), true);
              //  dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        int choice = JOptionPane.showConfirmDialog(
                                dialog,
                                "Installation is in progress. Do you really want to exit?",
                                "Exit Installation",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE
                        );

                        if (choice == JOptionPane.YES_OPTION) {
                            System.exit(0); // Exit application
                        }

                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel FirstPanel;
    private javax.swing.JPanel SecondPanel;
    private javax.swing.JTextField bioBinaries;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JTextField cranBinaries;
    private javax.swing.JLabel installLabel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList<String> jList1;
    private javax.swing.JList<String> jList2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextArea msgTextArea;
    private javax.swing.JTextField p1;
    private javax.swing.JTextField p10;
    private javax.swing.JTextField p11;
    private javax.swing.JTextField p12;
    private javax.swing.JTextField p13;
    private javax.swing.JTextField p14;
    private javax.swing.JTextField p15;
    private javax.swing.JTextField p2;
    private javax.swing.JTextField p3;
    private javax.swing.JTextField p4;
    private javax.swing.JTextField p5;
    private javax.swing.JTextField p6;
    private javax.swing.JTextField p7;
    private javax.swing.JTextField p8;
    private javax.swing.JTextField p9;
    private javax.swing.JPanel pathPanel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JTextField projectPathTxt;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}
