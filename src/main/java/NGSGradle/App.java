/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package NGSGradle;

//import com.example.NGS.Leftover.SSHConnectionDialog;
//import com.example.NGS.Leftover.InstallationDialog;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreeSelectionModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JFrame;
import java.beans.PropertyVetoException;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

/**
 *
 * @author iffy
 */
public class App extends javax.swing.JFrame {

    public static Map<String, String> toolPaths;
    private static final String CONFIG_FILE = System.getProperty("user.dir").concat("/src/main/resources/config.properties");

    static {
        /* too late ! */
        System.setProperty("java.awt.headless", "true");
        System.out.println(java.awt.GraphicsEnvironment.isHeadless());
        //System.setProperty("logPath", System.getProperty("user.sir").concat("/logs"));
        /* ---> prints false */
    }

//  
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
  //  public static String vcf2maf_PATH;
    public static String HTSLIB_PATH;
    
    public static final String LOG_PATH = System.getProperty("user.dir").concat("/logs/");
    public static org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger("NGSGradle.App");
    public static StringWriter stack = new StringWriter();
    public static BufferedImage bgLoginMain, bgSplit, bgInstall, bgInput;

    static {
        toolPaths = new HashMap<>();
        ensureLogFileExists();
        loadPaths();
        // Set the static constants
        FastQC_PATH = toolPaths.getOrDefault("fastqc", "Not_Found");
        FastP_PATH = toolPaths.getOrDefault("fastp", "Not_Found");
        MULTIQC_PATH = toolPaths.getOrDefault("multiqc", "Not_Found");
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
      //  vcf2maf_PATH = toolPaths.getOrDefault("vcf2maf", "Not_Found");
        HTSLIB_PATH=toolPaths.getOrDefault("HTS", "Not_Found");
    }//static block

//ICONS
    public static final Icon icons[] = {
        new ImageIcon(System.getProperty("user.dir").concat("/images/help1.png")),//0
        new ImageIcon(System.getProperty("user.dir").concat("/images/help2.png")),//1
        new ImageIcon(System.getProperty("user.dir").concat("/images/minus.png")),//2
        new ImageIcon(System.getProperty("user.dir").concat("/images/plus.png")),//3
        new ImageIcon(System.getProperty("user.dir").concat("/images/back1.png")),//4
        new ImageIcon(System.getProperty("user.dir").concat("/images/next1.png")),//5
        new ImageIcon(System.getProperty("user.dir").concat("/images/home1.png")),//6
        new ImageIcon(System.getProperty("user.dir").concat("/images/graph1.png")),//7
        new ImageIcon(System.getProperty("user.dir").concat("/images/align2.png")),//8
        new ImageIcon(System.getProperty("user.dir").concat("/images/report3.png")),//9
        new ImageIcon(System.getProperty("user.dir").concat("/images/graph5.png")),//10
        new ImageIcon(System.getProperty("user.dir").concat("/images/graph4.png")),//11
        new ImageIcon(System.getProperty("user.dir").concat("/images/updateProduct1.png")), //12
        new ImageIcon(System.getProperty("user.dir").concat("/images/reset1.png")), //13   
        new ImageIcon(System.getProperty("user.dir").concat("/images/report3.png")), //14
        new ImageIcon(System.getProperty("user.dir").concat("/images/cancle1.png")), //15
        new ImageIcon(System.getProperty("user.dir").concat("/images/bill2.png")), //16
        new ImageIcon(System.getProperty("user.dir").concat("/images/code1.png")), //17
        new ImageIcon(System.getProperty("user.dir").concat("/images/code2.png")) //18
    };

    //Create the nodes.
    //      DefaultTreeModel model;
    //  DefaultMutableTreeNode root;
    FastQCInternalFrame fqc;
    TrimmomaticInternalFrame trim;
    STARInternalFrame star;
    PicardInternalFrame picard;
    GATKFrame gatk;
    VariantPipelineInternalFrame variantAnalysisPipeline;
    RNASeqPipeLineInternalFrame rnaSeqPipeline;
    DEGenesAnalysis deGenesAnalysis;
    MainInstallationDialog dialog;
    InstallationDialog rpackageDialog;
    //ConsoleFrame frame;
// MergeBamAlignmentInternalFrame mergeBAMAlign;
    public App() {

        //frame = new ConsoleFrame();
        try {

            bgLoginMain = ImageIO.read(new File(System.getProperty("user.dir").concat("/images/w19.jpg")));//w12
            bgSplit = ImageIO.read(new File(System.getProperty("user.dir").concat("/images/w6.jpg")));//w6,w12
            bgInstall = ImageIO.read(new File(System.getProperty("user.dir").concat("/images/w13.jpg")));//w13
            bgInput = ImageIO.read(new File(System.getProperty("user.dir").concat("/images/w12.jpg")));//w13

        } catch (IOException ex) {

            LOGGER.error("Error loading background image: " + ex);
            textArea.append("Error loading background image: " + ex);

        }

        initComponents();

        textArea.setText("====================== SESSION STARTED ======================\r\n");
        
            // Right-click listener
        textArea.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Save Displayed Results As (*.txt)");

                    int userSelection = fileChooser.showSaveDialog(null);

                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                        File fileToSave = fileChooser.getSelectedFile();

                        // Ensure .txt extension
                        if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
                            fileToSave = new File(fileToSave.getAbsolutePath() + ".txt");
                        }

                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                            writer.write(textArea.getText());
                            JOptionPane.showMessageDialog(null, "File saved: " + fileToSave.getAbsolutePath());
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "Error saving file", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
        
        
        //////////////////////////////////
        LOGGER.info(textArea.getText());

        //INTERNAL FRAMES
        fqc = new FastQCInternalFrame();
        trim = new TrimmomaticInternalFrame();
        star = new STARInternalFrame();
        picard = new PicardInternalFrame();
        gatk = new GATKFrame();
        variantAnalysisPipeline = new VariantPipelineInternalFrame();
        rnaSeqPipeline = new RNASeqPipeLineInternalFrame();
        deGenesAnalysis = new DEGenesAnalysis();
        dialog = new MainInstallationDialog(this, true);
        rpackageDialog=new InstallationDialog(this, true);
//mergeBAMAlign=new MergeBamAlignmentInternalFrame();  
        //   model = (DefaultTreeModel)MainTree.getModel();
        //   root = (DefaultMutableTreeNode)model.getRoot();
        //   createNodes(root);
        dP.add(fqc);
        dP.add(trim);
        dP.add(star);
        dP.add(gatk);
        dP.add(picard);
        dP.add(variantAnalysisPipeline);
        dP.add(rnaSeqPipeline);
        dP.add(deGenesAnalysis);

        //dP.add(frame);
//        frame.setVisible(true);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        SplitPane.setDividerLocation(190);
        innerSplitPane.setDividerLocation(500);
        innerSplitPane.setResizeWeight(0.5);
        MainTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        //PROGRESSBAR
        UIManager.put("ProgressBar.background", Color.WHITE);
        UIManager.put("ProgressBar.foreground", Color.BLUE);
        downloadR.setEnabled(false);

        ////END Progressbar
    }

    private static void loadPaths() {
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try ( FileReader reader = new FileReader(configFile)) {
                Properties properties = new Properties();
                properties.load(reader);

                // Load properties into toolPaths map
                for (String key : properties.stringPropertyNames()) {
                    toolPaths.put(key, properties.getProperty(key));
                }
            } catch (IOException e) {

                LOGGER.error("ERROR in loading CONFIG_FILE: " + e.getMessage());
            }
        }
    }

    private static void ensureLogFileExists() {
        File logFile = new File(LOG_PATH);
        File logDir = logFile.getParentFile();

        try {
            if (!logDir.exists()) {
                logDir.mkdirs(); // Create logs directory if missing
            }
            if (!logFile.exists()) {
                logFile.createNewFile(); // Create log file if missing
            }
        } catch (IOException e) {
            LOGGER.error("ERROR creating LOG Directory: " + e.getMessage());
        }
    }

    ////////////////////////////////////STATIC FUNCTIONS
    /////////////////////////////////////////////////////
    private void openSearchDialog() {
        String searchText = JOptionPane.showInputDialog(null, "Enter text to search:", "Search", JOptionPane.QUESTION_MESSAGE);
        if (searchText != null && !searchText.isEmpty()) {
            highlightText(searchText);
        }
    }

    private void highlightText(String searchText) {
        try {
            Highlighter highlighter = textArea.getHighlighter();
            highlighter.removeAllHighlights();
            String text = textArea.getText();
            int index = text.indexOf(searchText);

            while (index >= 0) {
                int endIndex = index + searchText.length();
                highlighter.addHighlight(index, endIndex, new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
                index = text.indexOf(searchText, endIndex);
            }
        } catch (BadLocationException e) {
            LOGGER.error("ERROR in highlighting text: " + e.getMessage());
        }
    }

    public static void helpWebpage(String s) {
        try {
            Desktop.getDesktop().browse(new URL(s).toURI());
        } catch (MalformedURLException | URISyntaxException ex) {
            LOGGER.error("\r\n ERROR loading webpage: " + ex);
            textArea.append("\r\n ERROR loading webpage: " + ex);
        } catch (IOException ex) {
            LOGGER.error("\r\n ERROR loading webpage: " + ex);
            textArea.append("\r\n ERROR loading webpage: " + ex);
        }
    }

    public static String outputFile(FileNameExtensionFilter filter, String extension) {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCurrentDirectory(new java.io.File("."));

        fileChooser.addChoosableFileFilter(filter);

        fileChooser.setAcceptAllFileFilterUsed(false);
        int option = fileChooser.showSaveDialog(null);
        if (option == JFileChooser.APPROVE_OPTION) {

            File file = fileChooser.getSelectedFile();

            if (file == null) {
                return "";
            } else {

                return (file.getAbsolutePath() + extension);
            }

        }
        return null;

    }

    public static List<Path> findByFileExtension(Path path, String fileExtension)
            throws IOException {

        if (!Files.isDirectory(path)) {
            LOGGER.error("Path must be a directory!");
            textArea.append("Path must be a directory!");
        }

        List<Path> result;
        try ( Stream<Path> walk = Files.walk(path)) {
            result = walk
                    .filter(Files::isRegularFile) // is a file
                    .filter(p -> p.getFileName().toString().endsWith(fileExtension))
                    .collect(Collectors.toList());
        }
        return result;

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        SplitPane = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        MainTree = new javax.swing.JTree();
        innerSplitPane = new javax.swing.JSplitPane();
        dP = new javax.swing.JDesktopPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgSplit, 0, 0, null);
            }
        }

        ;
        sP = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        bar = new javax.swing.JProgressBar();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem10 = new javax.swing.JMenuItem();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenu8 = new javax.swing.JMenu();
        installR = new javax.swing.JMenuItem();
        downloadR = new javax.swing.JMenuItem();
        checkR = new javax.swing.JMenuItem();
        jMenuItem17 = new javax.swing.JMenuItem();
        jMenuItem18 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenu6 = new javax.swing.JMenu();
        jMenuItem12 = new javax.swing.JMenuItem();
        jMenuItem13 = new javax.swing.JMenuItem();
        jMenuItem14 = new javax.swing.JMenuItem();
        jMenuItem15 = new javax.swing.JMenuItem();
        jMenuItem16 = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        openlogMenu = new javax.swing.JMenuItem();
        clearLogsMenu = new javax.swing.JMenuItem();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenu7 = new javax.swing.JMenu();
        jMenuItem19 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("NGS Cancer Diagnostic Suite");

        SplitPane.setDividerSize(2);

        MainTree.setFont(new java.awt.Font("Noto Sans CJK TC", 1, 14)); // NOI18N
        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        javax.swing.tree.DefaultMutableTreeNode treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("NGS Analysis Tools");
        javax.swing.tree.DefaultMutableTreeNode treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("FastQC");
        treeNode2.add(treeNode3);
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Trimmomatic");
        treeNode2.add(treeNode3);
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("STAR");
        treeNode2.add(treeNode3);
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("PicardTools");
        treeNode2.add(treeNode3);
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("GATK");
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Analysis Pipelines");
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("Variant Calling Pipeline");
        treeNode2.add(treeNode3);
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("RNAseq Analysis Pipeline");
        treeNode2.add(treeNode3);
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("DEGenes Analysis Only");
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        MainTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        MainTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                MainTreeValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(MainTree);

        SplitPane.setLeftComponent(jScrollPane1);

        innerSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        javax.swing.GroupLayout dPLayout = new javax.swing.GroupLayout(dP);
        dP.setLayout(dPLayout);
        dPLayout.setHorizontalGroup(
            dPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 709, Short.MAX_VALUE)
        );
        dPLayout.setVerticalGroup(
            dPLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        innerSplitPane.setLeftComponent(dP);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Console Window", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Mono", 1, 16), new java.awt.Color(255, 255, 255))); // NOI18N
        jPanel1.setLayout(new java.awt.BorderLayout());

        sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255))); // NOI18N

        textArea.setEditable(false);
        textArea.setColumns(20);
        textArea.setFont(new java.awt.Font("Linux Libertine Mono O", 1, 14)); // NOI18N
        textArea.setLineWrap(true);
        textArea.setRows(5);
        textArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                textAreaKeyPressed(evt);
            }
        });
        sP.setViewportView(textArea);

        jPanel1.add(sP, java.awt.BorderLayout.CENTER);

        bar.setMinimum(1);
        jPanel1.add(bar, java.awt.BorderLayout.PAGE_START);

        jScrollPane2.setViewportView(jPanel1);

        innerSplitPane.setRightComponent(jScrollPane2);

        SplitPane.setBottomComponent(innerSplitPane);

        jMenu1.setText("File");

        jMenuItem10.setText("connectSSH");
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem10);

        jMenuItem9.setText("FastQC");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem9);

        jMenuItem1.setText("STAR");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenu3.setText("Picard Tools");

        jMenuItem3.setText("SamToFastq");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem3);

        jMenuItem4.setText("MergeBamAlignment");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem4);

        jMenuItem5.setText("MarkDuplicates");
        jMenu3.add(jMenuItem5);

        jMenuItem6.setText("MergeSamFiles");
        jMenu3.add(jMenuItem6);

        jMenuItem7.setText("MergeVcfs");
        jMenu3.add(jMenuItem7);

        jMenuItem8.setText("NormalizeFasta");
        jMenu3.add(jMenuItem8);

        jMenu1.add(jMenu3);

        jMenu4.setText("GATK Tools");
        jMenu1.add(jMenu4);

        jMenu8.setText("R Packages");

        installR.setText("Install R Package");
        installR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                installRActionPerformed(evt);
            }
        });
        jMenu8.add(installR);

        downloadR.setText("Update Binaries");
        downloadR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadRActionPerformed(evt);
            }
        });
        jMenu8.add(downloadR);

        checkR.setText("Check Binaries");
        checkR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkRActionPerformed(evt);
            }
        });
        jMenu8.add(checkR);

        jMenu1.add(jMenu8);

        jMenuItem17.setText("Setup Wizard");
        jMenuItem17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem17ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem17);

        jMenuItem18.setText("About");
        jMenuItem18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem18ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem18);

        jMenuItem2.setText("Exit");
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");

        jMenu6.setText("Change Background");

        jMenuItem12.setText("BG1");
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem12);

        jMenuItem13.setText("BG2");
        jMenuItem13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem13ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem13);

        jMenuItem14.setText("BG3");
        jMenuItem14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem14ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem14);

        jMenuItem15.setText("BG4");
        jMenuItem15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem15ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem15);

        jMenuItem16.setText("BG5");
        jMenuItem16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem16ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem16);

        jMenu2.add(jMenu6);

        jMenuBar1.add(jMenu2);

        jMenu5.setText("Logs");

        openlogMenu.setText("Open Log File");
        openlogMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openlogMenuActionPerformed(evt);
            }
        });
        jMenu5.add(openlogMenu);

        clearLogsMenu.setText("Clear Logs");
        clearLogsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearLogsMenuActionPerformed(evt);
            }
        });
        jMenu5.add(clearLogsMenu);

        jMenuItem11.setText("Clear Console Window");
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem11);

        jMenuBar1.add(jMenu5);

        jMenu7.setText("Help");

        jMenuItem19.setText("UserMannual");
        jMenuItem19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem19ActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItem19);

        jMenuBar1.add(jMenu7);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(SplitPane)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(SplitPane)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void MainTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_MainTreeValueChanged

        String node = evt.getNewLeadSelectionPath().getLastPathComponent().toString();
        try {
            if (node.equals("FastQC")) {
                fqc.setVisible(true);
                fqc.setMaximum(true);
                fqc.moveToFront();

//                star.setVisible(false);gatk.setVisible(false);
//                picard.setVisible(false);
//                trim.setVisible(false);
            } else if (node.equals("Trimmomatic")) {

                trim.setVisible(true);
                trim.setMaximum(true);
                trim.moveToFront();

//                fqc.setVisible(false);
//                picard.setVisible(false);
//                star.setVisible(false);gatk.setVisible(false);
            } else if (node.equals("STAR")) {

                star.setVisible(true);
                star.setMaximum(true);
                star.moveToFront();

//                fqc.setVisible(false);trim.setVisible(false);
//                picard.setVisible(false);gatk.setVisible(false);
            } else if (node.equals("PicardTools")) {

                picard.setVisible(true);
                picard.setMaximum(true);
                picard.moveToFront();

//                fqc.setVisible(false);trim.setVisible(false);
//                star.setVisible(false);gatk.setVisible(false);
            } else if (node.equals("GATK")) {

                gatk.setVisible(true);
                gatk.setMaximum(true);
                gatk.moveToFront();

//                fqc.setVisible(false);trim.setVisible(false);
//                star.setVisible(false);
//                picard.setVisible(false);
            } else if (node.equals("Variant Calling Pipeline")) {

                variantAnalysisPipeline.setVisible(true);
                variantAnalysisPipeline.setMaximum(true);
                variantAnalysisPipeline.moveToFront();

//                gatk.setVisible(false);
//                fqc.setVisible(false);trim.setVisible(false);
//                star.setVisible(false);
//                picard.setVisible(false);
            } else if (node.equals("RNAseq Analysis Pipeline")) {

                rnaSeqPipeline.setVisible(true);
                rnaSeqPipeline.setMaximum(true);
                rnaSeqPipeline.moveToFront();

//                variantAnalysisPipeline.setVisible(false);
//
//                gatk.setVisible(false);trim.setVisible(false);
//                fqc.setVisible(false);
//                star.setVisible(false);
//                picard.setVisible(false);
            } else if (node.equals("DEGenes Analysis Only")) {

                deGenesAnalysis.setVisible(true);
                deGenesAnalysis.setMaximum(true);
                deGenesAnalysis.moveToFront();

            }

        } catch (PropertyVetoException ex) {
            LOGGER.error("ERROR loading Frame: " + ex);
            textArea.append("ERROR loading Frame: " + ex);
        }

//        if (node.equals("ConsoleOutput")) {
//            frame.setVisible(true);//.moveToFront();
//        }

    }//GEN-LAST:event_MainTreeValueChanged

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
        fqc.setVisible(true);
        try {
            fqc.setMaximum(true);
        } catch (PropertyVetoException ex) {
            LOGGER.error("ERROR loading FASTQC Frame: " + ex);
            textArea.append("ERROR loading FASTQC Frame: " + ex);
        }
    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        star.setVisible(true);

        try {
            star.setMaximum(true);
        } catch (PropertyVetoException ex) {
            LOGGER.error("ERROR loading STAR Frame: " + ex);
            textArea.append("ERROR loading STAR Frame: " + ex);
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
  //      SSHConnectionDialog dialog = new SSHConnectionDialog(new javax.swing.JFrame(), true);
   //     dialog.setVisible(true);
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    private void openlogMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openlogMenuActionPerformed

        String todayLog = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File logFile = new File(LOG_PATH + "app-" + todayLog + ".log");
        if (logFile.exists()) {
            try {
                Desktop.getDesktop().open(logFile);
            } catch (IOException ex) {
                textArea.append("\nERROR opening application log: " + ex.getMessage());
            }
        } else {    
            textArea.append("\nNo application log found for today.");
        }

//        try {
//            Desktop.getDesktop().open(new File(System.getProperty("user.dir").concat("/logs/app.log")));
//
//        } catch (IOException ex) {
//            App.LOGGER.error("\r\nERROR loading logs file: " + ex);
//            App.textArea.append("\r\nERROR loading logs file: " + ex);
//        }
    }//GEN-LAST:event_openlogMenuActionPerformed

    private void clearLogsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearLogsMenuActionPerformed
        int x = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear all logs?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (x == JOptionPane.YES_OPTION) {

            try ( PrintWriter writer = new PrintWriter(System.getProperty("user.dir").concat("/logs/app.log"))) {
                writer.print("");
                // other operations
                writer.close();
                textArea.append("\r\nLogs cleared successfully");
            } catch (FileNotFoundException ex) {
                LOGGER.error("ERROR: Log file not found or Invalid access" + ex);
            }
        }
        //Files.deleteIfExists(Paths.get(System.getProperty("user.dir").concat("/logs/app.log")));

    }//GEN-LAST:event_clearLogsMenuActionPerformed

    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
        textArea.setText("");
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed
        try {

            bgLoginMain = ImageIO.read(new File(System.getProperty("user.dir").concat("/images/w13.jpg")));//w12
            bgSplit = ImageIO.read(new File(System.getProperty("user.dir").concat("/images/w13.jpg")));//w6
            jPanel1.repaint();
        } catch (IOException ex) {

           LOGGER.error("Error loading background image: " + ex);
            textArea.append("Error loading background image: " + ex);

        }


    }//GEN-LAST:event_jMenuItem12ActionPerformed

    private void jMenuItem13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem13ActionPerformed
        try {

            bgLoginMain = ImageIO.read(new File(System.getProperty("user.dir").concat("/images/w14.jpg")));//w12
            bgSplit = ImageIO.read(new File(System.getProperty("user.dir").concat("/images/w14.jpg")));//w6
            jPanel1.repaint();
        } catch (IOException ex) {

            LOGGER.error("Error loading background image: " + ex);
            textArea.append("Error loading background image: " + ex);

        }


    }//GEN-LAST:event_jMenuItem13ActionPerformed

    private void jMenuItem14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem14ActionPerformed
        try {

            bgLoginMain = ImageIO.read(new File(System.getProperty("user.dir").concat("/images/w12.jpg")));//w12
            bgSplit = ImageIO.read(new File(System.getProperty("user.dir").concat("/images/w12.jpg")));//w6
            jPanel1.repaint();
        } catch (IOException ex) {

            LOGGER.error("Error loading background image: " + ex);
            textArea.append("Error loading background image: " + ex);

        }

    }//GEN-LAST:event_jMenuItem14ActionPerformed

    private void jMenuItem15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem15ActionPerformed
        try {

            bgLoginMain = ImageIO.read(new File(System.getProperty("user.dir").concat("/images/w11.jpg")));//w12
            bgSplit = ImageIO.read(new File(System.getProperty("user.dir").concat("/images/w11.jpg")));//w6
            jPanel1.repaint();
        } catch (IOException ex) {

            LOGGER.error("Error loading background image: " + ex);
            textArea.append("Error loading background image: " + ex);

        }

    }//GEN-LAST:event_jMenuItem15ActionPerformed

    private void jMenuItem16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem16ActionPerformed
        try {

            bgLoginMain = ImageIO.read(new File(System.getProperty("user.dir").concat("/images/w4.jpg")));//w12
            bgSplit = ImageIO.read(new File(System.getProperty("user.dir").concat("/images/w4.jpg")));//w6
            jPanel1.repaint();
        } catch (IOException ex) {

            LOGGER.error("Error loading background image: " + ex);
            textArea.append("Error loading background image: " + ex);

        }

    }//GEN-LAST:event_jMenuItem16ActionPerformed

    private void jMenuItem17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem17ActionPerformed
dialog = new MainInstallationDialog(this, true);
        dialog.setVisible(true);
    }//GEN-LAST:event_jMenuItem17ActionPerformed

    private void jMenuItem18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem18ActionPerformed
  String aboutMessage = "<html><body style='width: 320px; font-family: Segoe UI, sans-serif; padding: 4px;'>"
        + "<div style='background-color: #F7FAFC; border-radius: 6px; padding: 10px; border: 1px solid #E2E8F0;'>"
        + "  <h2 style='color: #1A365D; margin: 0; font-size: 16px;'>NGS-CDS</h2>"
        + "  <p style='color: #4A5568; font-size: 10px; margin: 2px 0 0 0;'><i>NGS Cancer Diagnostic Suite</i></p>"
        + "</div>"
        + "<table style='font-size: 11px; color: #2D3748; margin-top: 12px; border-collapse: collapse;' cellpadding='2'>"
        + "  <tr>"
        + "    <td style='vertical-align: top; font-weight: bold; color: #4A5568; padding-right: 12px;'>Supervised By:</td>"
        + "    <td><b style='color: #2D3748;'>Dr. Adnan Ahmed Ansari</b></td>"
        + "  </tr>"
        + "  <tr><td colspan='2' style='height: 8px;'></td></tr>"
        + "  <tr>"
        + "    <td style='vertical-align: top; font-weight: bold; color: #4A5568; padding-right: 12px;'>Programmed By:</td>"
        + "    <td>"
        + "      <b style='color: #2D3748;'>Ms. Iffat Anjum</b><br/>"
        + "      <span style='color: #718096; font-size: 10px;'>Ph.D. Researcher in Bioinformatics</span><br/>"
        + "      <span style='color: #718096; font-size: 10px;'>National Center for Bioinformatics (NCB)</span><br/>"
        + "      <span style='color: #718096; font-size: 10px;'>Quaid-i-Azam University (QAU)</span>"
        + "      <div style='margin-top: 4px;'><span style='color: #2B6CB0; font-size: 10px;'><b>Email:</b> iffat.anjum@msn.com</span></div>"
        + "    </td>"
        + "  </tr>"
        + "</table>"
        + "<hr style='border: 0; border-top: 1px solid #E2E8F0; margin-top: 12px;'/>"
        + "<p style='font-size: 9px; color: #A0AEC0; text-align: right; margin: 4px 0 0 0;'>Version 1.0.0 &bull; In Active Development</p>"
        + "</body></html>";
JOptionPane.showMessageDialog(
    null, 
    aboutMessage, 
    "About NGS-CDS", 
    JOptionPane.INFORMATION_MESSAGE, 
    App.icons[17]
);
        
        
        
        
   //     JOptionPane.showMessageDialog(null, "In Progress....\nSupervised by:\n\t Dr. Adnan Ahmed Ansari \nProgrammed By:\n\t Ms. Iffat Anjum \n\t PhD Bioinformatics\n\t NCB,QAU", "About", JOptionPane.INFORMATION_MESSAGE, App.icons[17]);
    }//GEN-LAST:event_jMenuItem18ActionPerformed

    private void jMenuItem19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem19ActionPerformed
        try {
            Desktop.getDesktop().open(new File(System.getProperty("user.dir").concat("/bin/usermannual.pdf")));
        } catch (IOException ex) {
            LOGGER.error("Error loading pdf File: " + ex);
              }


    }//GEN-LAST:event_jMenuItem19ActionPerformed

    private void textAreaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textAreaKeyPressed
        if (evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_F) {
            openSearchDialog();
        }
    }//GEN-LAST:event_textAreaKeyPressed

    private void installRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_installRActionPerformed
rpackageDialog.setVisible(true);
    }//GEN-LAST:event_installRActionPerformed

    private void downloadRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadRActionPerformed
   App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
       
        
        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;
            @Override
            protected Integer doInBackground() {
                Process p;
                try {
                    List<String> cmdList = new ArrayList<String>();

                    cmdList.add("Rscript");
                    cmdList.add(System.getProperty("user.dir").concat("/bin/installRLibraries.R")); //R Script Path
                    cmdList.add(System.getProperty("user.dir").concat("/R_Libraries"));//1
                    cmdList.add(System.getProperty("user.dir").concat("/bin/R_Packages"));  //2
                    cmdList.add("DOWNLOAD");//3
                     cmdList.add("4.5.1");//4 R VersioN
                    ProcessBuilder pb = new ProcessBuilder(cmdList);
                   
                    pb.redirectErrorStream(true);
                    p = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        LOGGER.info("\r\n" + line);
                        publish("\r\n" + line);


                    }
                    if (!isCancelled()) {
                        status = p.waitFor();
                    }

                    p.destroy();

                } catch (IOException | InterruptedException ex) {

                    ex.printStackTrace(new PrintWriter(stack));
                    LOGGER.error("\r\n ERROR in R Packages: " + stack);

                }

                return status;
            }

            @Override
            protected void process(java.util.List<String> messages) {
                for (String message : messages) {
                    textArea.append(message + "\n");
                }
            }

            @Override
            protected void done() {
                App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
                App.bar.setIndeterminate(false);

            }

        };

     
        worker.execute();

    }//GEN-LAST:event_downloadRActionPerformed

    private void checkRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkRActionPerformed
   App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
       
   
        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;
            @Override
            protected Integer doInBackground() {
                Process p;
                try {
                    List<String> cmdList = new ArrayList<String>();

                    cmdList.add("Rscript");
                    cmdList.add(System.getProperty("user.dir").concat("/bin/installRLibraries.R")); //R Script Path
                    cmdList.add("");//1 installIN
                    cmdList.add(System.getProperty("user.dir").concat("/bin/R_Packages"));  //2
                    cmdList.add("CHECK");//3
                     cmdList.add("4.5.1");//4 R VersioN
                    ProcessBuilder pb = new ProcessBuilder(cmdList);
                   
                    pb.redirectErrorStream(true);
                    p = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        LOGGER.info("\r\n" + line);
                        publish("\r\n" + line);


                    }
                    if (!isCancelled()) {
                        status = p.waitFor();
                    }

                    p.destroy();

                } catch (IOException | InterruptedException ex) {

                    ex.printStackTrace(new PrintWriter(stack));
                    LOGGER.error("\r\n ERROR in R Packages: " + stack);

                }

                return status;
            }

            @Override
            protected void process(java.util.List<String> messages) {
                for (String message : messages) {
                    textArea.append(message + "\n");
                }
            }

            @Override
            protected void done() {
            
    App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
                App.bar.setIndeterminate(false);
            }

        };

     
        worker.execute();

    }//GEN-LAST:event_checkRActionPerformed

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
            java.util.logging.Logger.getLogger(App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(App.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }



//        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new App().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTree MainTree;
    private javax.swing.JSplitPane SplitPane;
    public static javax.swing.JProgressBar bar;
    private javax.swing.JMenuItem checkR;
    private javax.swing.JMenuItem clearLogsMenu;
    private javax.swing.JDesktopPane dP;
    private javax.swing.JMenuItem downloadR;
    private javax.swing.JSplitPane innerSplitPane;
    private javax.swing.JMenuItem installR;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenu jMenu8;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem13;
    private javax.swing.JMenuItem jMenuItem14;
    private javax.swing.JMenuItem jMenuItem15;
    private javax.swing.JMenuItem jMenuItem16;
    private javax.swing.JMenuItem jMenuItem17;
    private javax.swing.JMenuItem jMenuItem18;
    private javax.swing.JMenuItem jMenuItem19;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JMenuItem openlogMenu;
    public static javax.swing.JScrollPane sP;
    public static javax.swing.JTextArea textArea;
    // End of variables declaration//GEN-END:variables
}
