package NGSGradle;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */



import NGSGradle.App;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 *
 * @author iffy
 */
public class InstallationDialog extends javax.swing.JDialog {

    private DefaultListModel model, model1;

    private String[] cranPkgs = {"BiocManager","Rcpp", "cpp11", "tidyverse", "RColorBrewer", "pheatmap", "gplots",
        "ggplot2", "gridExtra", "scales", "Cairo","gsalib"};
    private String[] biocPkgs = {"affy", "Biostrings", "limma", "shiny", "vsn", "PCAtools", "DESeq2", "edgeR", "AnnotationDbi",
        "clusterProfiler", "KEGGREST"};
    private Map<String, String> envVars;

    public InstallationDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        model = new DefaultListModel();
        model1 = new DefaultListModel();
        for (String s : cranPkgs) {
            model.addElement(s);
        }
        for (String s : biocPkgs) {
            model1.addElement(s);
        }

        initComponents();

       
        // SETTING SCREEN SIZE
        // Get screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Set dialog size as a percentage of screen size
        int width = (int) (screenSize.width * 0.5);  // 50% of screen width
        int height = (int) (screenSize.height * 0.5); // 50% of screen height
        setSize(width, height);

        // Center the dialog on screen
        setLocationRelativeTo(parent);

        //
//        projectPathTxt.setText(System.getProperty("user.dir").concat("/bin/"));
        jTextField1.setText(System.getProperty("user.dir").concat("/R_Libraries"));
        cranBinaries.setText(System.getProperty("user.dir").concat("/bin/R_Packages"));
        bioBinaries.setText(System.getProperty("user.dir").concat("/bin/R_Packages"));
        p1.setText(App.FastQC_PATH);
        p2.setText(App.Trimmomatic_PATH);
        p3.setText(App.STAR_PATH);
        p4.setText(App.BWA_PATH);
        p5.setText(App.GATK_PATH);
        p6.setText(App.PICARD_PATH);
        p7.setText(App.SAMTOOLS_PATH);
        p8.setText(App.BCFTOOLS_PATH);
        p9.setText(App.SNPEFF_PATH);
        p10.setText(App.SUBREAD_PATH);
        p11.setText(App.FastP_PATH);

        buttonGroup2.add(offline1);
        buttonGroup2.add(online1);

        buttonGroup3.add(cran1);
        buttonGroup3.add(bio1);

    }

    /////////////////////////////////////////////
    public void runMake(String rFilePath) {

        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "INSTALATTION STARTED: Unzipping Files...", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new Color(255,255,255)));

        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

            @Override
            protected Integer doInBackground() {
                Process p;
                try {
                    List<String> cmdList = new ArrayList<String>();

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
                        App.LOGGER.info("\r\n" + line);
                        publish("\r\n" + line);

                    }
                    if (!isCancelled()) {
                        status = p.waitFor();
                    }

                    p.destroy();

                } catch (IOException | InterruptedException ex) {

                    ex.printStackTrace(new PrintWriter(App.stack));
                    App.LOGGER.error("\r\n ERROR in UNZIPPING FILES: " + App.stack);
                    App.textArea.append("\r\n ERROR in UNZIPPING FILES: " + App.stack);

                }
                return status;
            }

            @Override
            protected void process(java.util.List<String> messages) {
                for (String message : messages) {
                    App.textArea.append(message + "\n");
                }

            }

            @Override
            protected void done() {
                App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new Color(255,255,255)));
                App.bar.setIndeterminate(false);

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
                    //Change the paths to folders
                    //set file path

                    // Base directory (replace with the actual path if necessary)
                    String binDirPath = System.getProperty("user.dir").concat("/bin/");
                    File binDir = new File(binDirPath);

                    // Tools to look for
                    String[] toolPrefixes = {"fastqc", "fastp", "Trimmomatic", "STAR", "bwa", "GATK", "picard", "samtools", "bcftools", "subread", "snpeff", "IGV"};
                    App.toolPaths = new HashMap<>();

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
                                                App.toolPaths.put(prefix, file.getAbsolutePath().concat("/Linux_x86_64"));
                                            } else if(prefix.equalsIgnoreCase("Trimmomatic")){
                                              App.toolPaths.put(prefix, file.getAbsolutePath().concat("/trimmomatic-0.39.jar"));
                                
                                }else {
                                                App.toolPaths.put(prefix, file.getAbsolutePath());
                                            }
                                            break;
                                        }
                                    }
                                } else if (file.isFile() && file.getName().endsWith(".jar")) {
                                    // Handle .jar files
                                    String jarName = file.getName().toLowerCase();
                                    for (String prefix : toolPrefixes) {
                                        if (jarName.startsWith(prefix.toLowerCase())) {
                                            App.toolPaths.put(prefix, file.getAbsolutePath());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        System.out.println("Directory " + binDirPath + " does not exist.");
                    }
                    // Set the static constants
                    App.FastQC_PATH = App.toolPaths.getOrDefault("fastqc", "Not Found");
                    App.FastP_PATH = App.toolPaths.getOrDefault("fastp", "Not Found");
                    App.Trimmomatic_PATH = App.toolPaths.getOrDefault("Trimmomatic", "Not Found");
                    App.STAR_PATH = App.toolPaths.getOrDefault("STAR", "Not Found");
                    App.BWA_PATH = App.toolPaths.getOrDefault("bwa", "Not Found");
                    App.GATK_PATH = App.toolPaths.getOrDefault("GATK", "Not Found");
                    App.PICARD_PATH = App.toolPaths.getOrDefault("picard", "Not Found");
                    App.SAMTOOLS_PATH = App.toolPaths.getOrDefault("samtools", "Not Found");
                    App.BCFTOOLS_PATH = App.toolPaths.getOrDefault("bcftools", "Not Found");
                    App.SUBREAD_PATH = App.toolPaths.getOrDefault("subread", "Not Found");
                    App.SNPEFF_PATH = App.toolPaths.getOrDefault("snpeff", "Not Found");
                    App.IGV_PATH = App.toolPaths.getOrDefault("IGV", "Not Found");

//SETTING INSTALLATION PATHS
//                    projectPathTxt.setText(System.getProperty("user.dir").concat("/bin/"));
                    jTextField1.setText(System.getProperty("user.dir").concat("/R_Libraries"));
                    cranBinaries.setText(System.getProperty("user.dir").concat("/bin/R_Packages"));
                    bioBinaries.setText(System.getProperty("user.dir").concat("/bin/R_Packages"));
                    p1.setText(App.FastQC_PATH);
                    p2.setText(App.Trimmomatic_PATH);
                    p3.setText(App.STAR_PATH);
                    p4.setText(App.BWA_PATH);
                    p5.setText(App.GATK_PATH);
                    p6.setText(App.PICARD_PATH);
                    p7.setText(App.SAMTOOLS_PATH);
                    p8.setText(App.BCFTOOLS_PATH);
                    p9.setText(App.SNPEFF_PATH);
                    p10.setText(App.SUBREAD_PATH);
                    p11.setText(App.FastP_PATH);

                    //Insatting Softwares
                    runMakee(System.getProperty("user.dir").concat("/bin/install.sh"), password);

                } else {
                    App.textArea.append("ERROR: Installation can't be completed without administrative privilages " + "\n");
                }

            }

        };

        worker.execute();

    }

    public void runMakee(String rFilePath, String password) {

        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "INSTALATTION IN PROGRESS: Installing System Libraries...", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new Color(255,255,255)));

        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

            @Override
            protected Integer doInBackground() {
                Process p;
                try {

                    List<String> scriptArgs = Arrays.asList(App.FastP_PATH, App.STAR_PATH,
                            App.BWA_PATH, App.SAMTOOLS_PATH, App.BCFTOOLS_PATH, password, App.LOG_PATH);
                    String joinedArgs = String.join(" ", scriptArgs);

                    ProcessBuilder pb = new ProcessBuilder("bash", "-c", "source " + rFilePath + " " + joinedArgs + " && env");

                    pb.redirectErrorStream(true);
                    p = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    envVars = new HashMap<>();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        App.LOGGER.info("\r\n" + line);
                        publish("\r\n" + line);

                        if (line.contains("=")) {
                            String[] parts = line.split("=", 2);
                            envVars.put(parts[0], parts[1]);
                        }

                    }//while

                    if (!isCancelled()) {
                        status = p.waitFor();
                    }

                    p.destroy();

                } catch (IOException | InterruptedException ex) {

                    ex.printStackTrace(new PrintWriter(App.stack));
                    App.LOGGER.error("\r\n ERROR in MAKING FILES: " + App.stack);
                    App.textArea.append("\r\n ERROR in MAKING FILES: " + App.stack);

                }
                return status;
            }

            @Override
            protected void process(java.util.List<String> messages) {
                for (String message : messages) {
                    App.textArea.append(message + "\n");
                }

            }

            @Override
            protected void done() {
                App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
                App.bar.setIndeterminate(false);

//Display R Package Tab
              
//              FirstPanel.setVisible(false); SecondPanel.setVisible(true);
       //         InstallationDialog.this.setVisible(true);//  jTabbedPane1.setSelectedIndex(1);
              String cp = String.join(",", cranPkgs);
                String bp = String.join(",", biocPkgs);  
               //Installing Softwares
                SwingUtilities.invokeLater(() -> runR(System.getProperty("user.dir").concat("/bin/installRLibraries.R"),
                        System.getProperty("user.dir").concat("/R_Libraries"),
                        System.getProperty("user.dir").concat("/bin/R_Packages"),
                        cp, bp, "offline", "ALL", "","4.5.1"));

            }

        };

        worker.execute();

    }

//public void runMakee(String bashScriptPath, String password) {
//
//    App.bar.setIndeterminate(true);
//    App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, 
//        "INSTALLATION IN PROGRESS:", 
//        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
//        javax.swing.border.TitledBorder.DEFAULT_POSITION, 
//        new java.awt.Font("Liberation Sans", 1, 15), 
//        new java.awt.Color(255, 255, 255)));
//
//    SwingWorker<Integer, String> worker = new SwingWorker<Integer, String>() {
//        private int status;
//
//        @Override
//        protected Integer doInBackground() {
//            try {
//                // Step 1: Run Bash script with arguments & capture environment variables
//                List<String> scriptArgs = Arrays.asList(App.FastP_PATH, App.STAR_PATH, 
//                    App.BWA_PATH, App.SAMTOOLS_PATH, App.BCFTOOLS_PATH, password, App.Log_PATH);
//
//               envVars = runBashScriptAndCaptureEnv(bashScriptPath, scriptArgs);
//
//                // Step 2: Pass environment variables to R script
////                runRScriptWithEnv(App.R_SCRIPT_PATH, envVars, scriptArgs);
//
//                    publish("\n🔹 Captured Environment Variables from install.sh:");
//                        for (Map.Entry<String, String> entry : envVars.entrySet()) {
//                            publish(entry.getKey() + " = " + entry.getValue());
//                        }
//            } catch (IOException | InterruptedException ex) {
//                ex.printStackTrace(new PrintWriter(App.stack));
//                App.LOGGER.error("\r\n ERROR in INSTALLATION: " + App.stack);
//                App.textArea.append("\r\n ERROR in INSTALLATION: " + App.stack);
//            }
//            return status;
//        }
//                    @Override
//            protected void process(java.util.List<String> messages) {
//                for (String message : messages) {
//                    App.textArea.append(message + "\n");
//                }
//
//            }
//
//            @Override
//            protected void done() {
//                App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
//                App.bar.setIndeterminate(false);
//    
////Display R Package Tab
//                jTabbedPane1.setSelectedIndex(1);
//                InstallationDialog.this.setVisible(true);
//
//            }
//
//        
//
//    };
//
//    worker.execute();
//}
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
            App.LOGGER.info("\r\n" + line);
            System.out.println("\r\n" + line);
            if (line.contains("=")) {
                String[] parts = line.split("=", 2);
                envVars.put(parts[0], parts[1]);
            }
        }

        process.waitFor();
        process.destroy();

        return envVars;
    }

    //////////////////////////////////////////////
    public void runR(String rFilePath, String installIn, String cranB, String cpkg, String bpkg, String mode,
            String pkgMode, String pkgName, String RVersion) {

        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "INSTALLATION IN PROGRESS: Installing R Packages...", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new Color(255,255,255)));
    
        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

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
                 //   pb.environment().putAll(envVars);
                    pb.redirectErrorStream(true);
                    p = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        App.LOGGER.info("\r\n" + line);
                        publish("\r\n" + line);

                    }
                    if (!isCancelled()) {
                        status = p.waitFor();
                    }

                    p.destroy();

                } catch (IOException | InterruptedException ex) {

                    ex.printStackTrace(new PrintWriter(App.stack));
                    App.LOGGER.error("\r\n ERROR in R analysis: " + App.stack);
                    App.textArea.append("\r\n ERROR in R analysis: " + App.stack);

                }
                return status;
            }

            @Override
            protected void process(java.util.List<String> messages) {
                for (String message : messages) {
                    App.textArea.append(message + "\n");
                }

            }

            @Override
            protected void done() {
                App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "INSTALLATION COMPLETED", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
                App.bar.setIndeterminate(false);
                //  jTabbedPane1.setSelectedIndex(2);
                
            }

        };

        worker.execute();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        singlePanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgInstall, 0, 0, null);
            }
        }

        ;
        jLabel8 = new javax.swing.JLabel();
        pkgName = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        offline1 = new javax.swing.JRadioButton();
        online1 = new javax.swing.JRadioButton();
        jLabel11 = new javax.swing.JLabel();
        cran1 = new javax.swing.JRadioButton();
        bio1 = new javax.swing.JRadioButton();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        pathPanel = new javax.swing.JPanel(){

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgInstall, 0, 0, null);
            }

        }

        ;
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
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("NGS-CDS: R Package Installer");

        singlePanel.setBackground(new java.awt.Color(0, 0, 0));

        jLabel8.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Package Name:");

        jButton1.setText("Install");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        offline1.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        offline1.setForeground(new java.awt.Color(255, 255, 255));
        offline1.setSelected(true);
        offline1.setText("Offline(From Default Binaries Folder)");

        online1.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        online1.setForeground(new java.awt.Color(255, 255, 255));
        online1.setText("Online (From Internet)");

        jLabel11.setFont(new java.awt.Font("Liberation Sans", 0, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("If you encounter any errors during the installation process,  or if you need to install a specific R package or update an existing one");

        cran1.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        cran1.setForeground(new java.awt.Color(255, 255, 255));
        cran1.setSelected(true);
        cran1.setText("CRAN");

        bio1.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        bio1.setForeground(new java.awt.Color(255, 255, 255));
        bio1.setText("Bioconductor");

        jLabel12.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("Package Type:");

        jLabel13.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("Installation Mode:");

        jLabel52.setFont(new java.awt.Font("Liberation Sans", 0, 14)); // NOI18N
        jLabel52.setForeground(new java.awt.Color(255, 255, 255));
        jLabel52.setText("you can use this");

        jLabel53.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        jLabel53.setForeground(new java.awt.Color(255, 255, 255));
        jLabel53.setText("Install R Package(s) Setup Wizard");

        jLabel54.setFont(new java.awt.Font("Liberation Sans", 0, 14)); // NOI18N
        jLabel54.setForeground(new java.awt.Color(255, 255, 255));
        jLabel54.setText("You have the option to  install packages offline from local files or directly from the internet.");

        jLabel58.setFont(new java.awt.Font("Liberation Sans", 0, 14)); // NOI18N
        jLabel58.setForeground(new java.awt.Color(255, 255, 255));
        jLabel58.setText("For multiple packages, separate them with commas (e.g., edgeR, limma, ggplot2)");

        javax.swing.GroupLayout singlePanelLayout = new javax.swing.GroupLayout(singlePanel);
        singlePanel.setLayout(singlePanelLayout);
        singlePanelLayout.setHorizontalGroup(
            singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(singlePanelLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(singlePanelLayout.createSequentialGroup()
                            .addGroup(singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel12)
                                .addComponent(jLabel13))
                            .addGap(18, 18, 18)
                            .addGroup(singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(singlePanelLayout.createSequentialGroup()
                                    .addComponent(cran1)
                                    .addGap(51, 51, 51)
                                    .addComponent(bio1))
                                .addGroup(singlePanelLayout.createSequentialGroup()
                                    .addComponent(offline1)
                                    .addGap(51, 51, 51)
                                    .addComponent(online1))))
                        .addGroup(singlePanelLayout.createSequentialGroup()
                            .addComponent(jLabel8)
                            .addGap(18, 18, 18)
                            .addComponent(pkgName, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(198, 198, 198))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, singlePanelLayout.createSequentialGroup()
                            .addGap(251, 251, 251)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(singlePanelLayout.createSequentialGroup()
                        .addComponent(jLabel52)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel53))
                    .addComponent(jLabel11)
                    .addComponent(jLabel54)
                    .addComponent(jLabel58))
                .addContainerGap(182, Short.MAX_VALUE))
        );
        singlePanelLayout.setVerticalGroup(
            singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(singlePanelLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel52, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel53, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel54, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel58, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(singlePanelLayout.createSequentialGroup()
                        .addGroup(singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(pkgName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(cran1)
                            .addComponent(bio1))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel13))
                    .addGroup(singlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(offline1)
                        .addComponent(online1)))
                .addGap(65, 65, 65)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(56, Short.MAX_VALUE))
        );

        jScrollPane4.setViewportView(singlePanel);

        jTabbedPane1.addTab("Install R Pakage(s)", jScrollPane4);

        pathPanel.setOpaque(false);

        jLabel3.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("R_Libraries Folder Path:");

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

        jLabel5.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Installation Complete!");

        jLabel9.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Installed Software Paths:");

        jLabel10.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("NGS Suite has been successfully installed.");

        javax.swing.GroupLayout pathPanelLayout = new javax.swing.GroupLayout(pathPanel);
        pathPanel.setLayout(pathPanelLayout);
        pathPanelLayout.setHorizontalGroup(
            pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pathPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator2)
                .addContainerGap())
            .addGroup(pathPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pathPanelLayout.createSequentialGroup()
                        .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 511, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(398, Short.MAX_VALUE))
                    .addGroup(pathPanelLayout.createSequentialGroup()
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
                            .addComponent(jLabel4)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextField1)
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
                            .addComponent(p11, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(109, 109, 109))))
        );
        pathPanelLayout.setVerticalGroup(
            pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pathPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel9)
                .addGap(18, 18, 18)
                .addGroup(pathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane6.setViewportView(pathPanel);

        jTabbedPane1.addTab("Installation Paths", jScrollPane6);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1078, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 581, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String cp = "", bp = "";
        if (cran1.isSelected()) {
            cp = "YES";
            bp = "NO";
        } else if (bio1.isSelected()) {
            cp = "NO";
            bp = "YES";
        }
        if (offline1.isSelected()) {

            runR(System.getProperty("user.dir").concat("/bin/installRLibraries.R"),
                    System.getProperty("user.dir").concat("/R_Libraries"),
                    System.getProperty("user.dir").concat("/bin/R_Packages"),
                    cp, bp, "offline", "SINGLE", pkgName.getText(),"4.5.1");

        } else if (online1.isSelected()) {

            runR(System.getProperty("user.dir").concat("/bin/installRLibraries.R"),
                    System.getProperty("user.dir").concat("/R_Libraries"),
                    System.getProperty("user.dir").concat("/bin/R_Packages"),
                    cp, bp, "online", "SINGLE", pkgName.getText(),"4.5.1");
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
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(InstallationDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(InstallationDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(InstallationDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(InstallationDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
 /*   java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                InstallationDialog dialog = new InstallationDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                       // System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });*/
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton bio1;
    private javax.swing.JTextField bioBinaries;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JRadioButton cran1;
    private javax.swing.JTextField cranBinaries;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
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
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JRadioButton offline1;
    private javax.swing.JRadioButton online1;
    private javax.swing.JTextField p1;
    private javax.swing.JTextField p10;
    private javax.swing.JTextField p11;
    private javax.swing.JTextField p2;
    private javax.swing.JTextField p3;
    private javax.swing.JTextField p4;
    private javax.swing.JTextField p5;
    private javax.swing.JTextField p6;
    private javax.swing.JTextField p7;
    private javax.swing.JTextField p8;
    private javax.swing.JTextField p9;
    private javax.swing.JPanel pathPanel;
    private javax.swing.JTextField pkgName;
    private javax.swing.JPanel singlePanel;
    // End of variables declaration//GEN-END:variables
}
