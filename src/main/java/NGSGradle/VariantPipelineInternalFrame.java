/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JInternalFrame.java to edit this template
 */
package NGSGradle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.Vector;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.util.stream.Stream;

/**
 *
 * @author iffy
 */
public class VariantPipelineInternalFrame extends javax.swing.JInternalFrame {

    /*INPUT VARIABLES:*/
    private File[] allFiles;
    private DefaultListModel listModel, listNormal, listTumor, listKnownSites;
    private DefaultListModel<String> listModelreadSingle, listModelreadPair1, listModelreadPair2;
    private Map<String, List<String>> singlePathMap;
    private Map<String, Map<String, List<String>>> pairPathMap;

    public String selectedKey;
    public InputPathDialog inputDialog;
    public int fastCounter;
    public Frame parentFrame;

    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;
    private DefaultComboBoxModel<String> model;
    private DefaultTableModel dm;
    private JTableHeader headerQC;
    private List<String> filesForCombo; //fastqc
    private List<String> filesForComboP = new ArrayList<>(); //fastp

    private Vector<String> items;
    private List<String> sfileNames, sfilePaths, sfileExtensions;
    private List<String> p1fileNames, p1filePaths, p1fileExtensions, p1p2fileNames;
    private List<String> p2fileNames, p2filePaths, p2fileExtensions;

    //Components for addReplaceRGPanel
    private ArrayList<JLabel> labels;
    private ArrayList<JTextField> textFields;
    public static String pipelineStage = "germline_snpeff";
    private String germSomaticOP = "";
    private String somaticOP = "";
    boolean pipelineError = false;//No error
    Map<Integer, String> tooltips; //For FastQC Table

    private String[] snpSiftFilters = {"ANN[*].EFFECT has 'missense_variant'", "ANN[*].EFFECT has 'frameshift_variant'",
        "ANN[*].EFFECT has 'stop_gained'", "ANN[*].EFFECT has 'stop_lost'",
        "ANN[*].EFFECT has 'start_lost'", "ANN[*].EFFECT has 'splice_acceptor_variant'",
        "ANN[*].EFFECT has 'splice_donor_variant'", "ANN[*].EFFECT has 'synonymous_variant'",
        "ANN[*].EFFECT has 'intron_variant'", "ANN[*].EFFECT has '5_prime_UTR_variant'",
        "ANN[*].EFFECT has '3_prime_UTR_variant'", "ANN[*].EFFECT has 'start_retained_variant'",
        "ANN[*].EFFECT has 'stop_retained_variant'", "ANN[*].EFFECT has 'protein_altering_variant'",
        "ANN[*].EFFECT has 'non_coding_transcript_exon_variant'",
        // IMPACT filters
        "ANN[*].IMPACT = 'HIGH'", "ANN[*].IMPACT = 'MODERATE'",
        "ANN[*].IMPACT = 'LOW'", "ANN[*].IMPACT = 'MODIFIER'",
        "((ANN[*].IMPACT = 'HIGH') | (ANN[*].IMPACT = 'MODERATE'))",
        // Gene and Biotype filters
        "ANN[*].GENE = 'TP53'", "ANN[*].GENE = 'BRCA1'",
        "ANN[*].BIOTYPE = 'protein_coding'", "ANN[*].BIOTYPE = 'lncRNA'",
        // Combined logic filters
        "((ANN[*].EFFECT has 'missense_variant') | (ANN[*].EFFECT has 'stop_gained'))",
        "((ANN[*].IMPACT = 'HIGH') & (ANN[*].BIOTYPE = 'protein_coding'))",
        "((ANN[*].EFFECT has 'frameshift_variant') & (ANN[*].GENE = 'BRCA2'))",
        "((ANN[*].IMPACT = 'MODERATE') & (ANN[*].BIOTYPE = 'protein_coding') & (ANN[*].EFFECT has 'missense_variant'))",
        "isVariant", "isVariant & (VT = 'SNP')", "isVariant & (VT = 'INDEL')",
        "FILTER = 'PASS'",
        "QUAL > 30",
        "DP > 10"

    };

    private String[] snpSiftFiltersVEP = {
        // Consequence (VEP equivalent of EFFECT)
        "CSQ[*].Consequence has 'missense_variant'",
        "CSQ[*].Consequence has 'frameshift_variant'",
        "CSQ[*].Consequence has 'stop_gained'",
        "CSQ[*].Consequence has 'stop_lost'",
        "CSQ[*].Consequence has 'start_lost'",
        "CSQ[*].Consequence has 'splice_acceptor_variant'",
        "CSQ[*].Consequence has 'splice_donor_variant'",
        "CSQ[*].Consequence has 'synonymous_variant'",
        "CSQ[*].Consequence has 'intron_variant'",
        "CSQ[*].Consequence has '5_prime_UTR_variant'",
        "CSQ[*].Consequence has '3_prime_UTR_variant'",
        "CSQ[*].Consequence has 'start_retained_variant'",
        "CSQ[*].Consequence has 'stop_retained_variant'",
        "CSQ[*].Consequence has 'protein_altering_variant'",
        "CSQ[*].Consequence has 'non_coding_transcript_exon_variant'",
        // IMPACT filters (VEP CSQ field)
        "CSQ[*].IMPACT = 'HIGH'",
        "CSQ[*].IMPACT = 'MODERATE'",
        "CSQ[*].IMPACT = 'LOW'",
        "CSQ[*].IMPACT = 'MODIFIER'",
        "((CSQ[*].IMPACT = 'HIGH') | (CSQ[*].IMPACT = 'MODERATE'))",
        // Gene (SYMBOL in VEP) and Biotype filters
        "CSQ[*].SYMBOL = 'TP53'",
        "CSQ[*].SYMBOL = 'BRCA1'",
        "CSQ[*].BIOTYPE = 'protein_coding'",
        "CSQ[*].BIOTYPE = 'lncRNA'",
        // Combined logic filters
        "((CSQ[*].Consequence has 'missense_variant') | (CSQ[*].Consequence has 'stop_gained'))",
        "((CSQ[*].IMPACT = 'HIGH') & (CSQ[*].BIOTYPE = 'protein_coding'))",
        "((CSQ[*].Consequence has 'frameshift_variant') & (CSQ[*].SYMBOL = 'BRCA2'))",
        "((CSQ[*].IMPACT = 'MODERATE') & (CSQ[*].BIOTYPE = 'protein_coding') & (CSQ[*].Consequence has 'missense_variant'))",
        // Population frequency (gnomAD in VEP)
        "(CSQ[*].gnomAD_AF < 0.001 | !(exists CSQ[*].gnomAD_AF))",
        // General VCF quality filters
        "isVariant", "isVariant & (VT = 'SNP')", "isVariant & (VT = 'INDEL')",
        "FILTER = 'PASS'", "QUAL > 30", "DP > 10"
    };

    String[] combinedFilters = Stream.concat(
            Stream.of("--- SnpEff Filters ---"),
            Stream.concat(
                    Stream.of(snpSiftFilters),
                    Stream.concat(
                            Stream.of("--- VEP Filters ---"),
                            Stream.of(snpSiftFiltersVEP)
                    )
            )
    ).toArray(String[]::new);

    /**
     * Creates new form RNASeqAnalysisInternalFrame
     */
    public VariantPipelineInternalFrame() {
//For inputFastq
        listModelreadSingle = new DefaultListModel<>();
        listModelreadPair1 = new DefaultListModel<>();
        listModelreadPair2 = new DefaultListModel<>();
//For inputBam
        listModel = new DefaultListModel();
//For Tumor-Normal Pairs      
        listNormal = new DefaultListModel();
        listTumor = new DefaultListModel();
        listKnownSites = new DefaultListModel();
//Declaring Map
        singlePathMap = new HashMap<>();
        pairPathMap = new HashMap<>();
        fastCounter = 0;
        parentFrame = JOptionPane.getFrameForComponent(this);

        root = new DefaultMutableTreeNode("ROOT");

        tooltips = new HashMap<>();

        //FastQC Table Model
        String[] tblHead = {"Sample Name", "Basic Statistics", "Per base sequence quality", "Per sequence quality scores", "Per base sequence content", "Per sequence GC content", "Per base N content", "Sequence Length Distribution", "Sequence Duplication Levels", "Overrepresented sequences", "Adapter Content"};
        dm = new DefaultTableModel(tblHead, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column <= 11 ? false : true;
            }
        };

        // Create a list of text values for the combo box
        items = new Vector<>();
        items.add(""); // First value as empty string
        items.add("AS_BaseQualityRankSumTest (AS_BaseQRankSum)");
        items.add("AS_FisherStrand (AS_FS)");
        items.add("AS_InbreedingCoeff (AS_InbreedingCoeff)");
        items.add("AS_MappingQualityRankSumTest (AS_MQRankSum)");
        items.add("AS_QualByDepth (AS_QD)");
        items.add("AS_RMSMappingQuality (AS_MQ)");
        items.add("AS_ReadPosRankSumTest (AS_ReadPosRankSum)");
        items.add("AS_StrandOddsRatio (AS_SOR)");
        items.add("AlleleFraction");
        items.add("AllelePseudoDepth (DP)");
        items.add("AssemblyComplexity");
        items.add("BaseQuality (MBQ)");
        items.add("BaseQualityRankSumTest (BaseQRankSum)");
        items.add("ChromosomeCounts (AC,AF,AN)");
        items.add("ClippingRankSumTest (ClippingRankSum)");
        items.add("CountNs");
        items.add("Coverage (DP)");
        items.add("DepthPerAlleleBySample (AD)");
        items.add("DepthPerSampleHC (DP)");
        items.add("ExcessHet (ExcessHet)");
        items.add("FisherStrand (FS)");
        items.add("FragmentDepthPerAlleleBySample (AD)");
        items.add("FragmentLength (MFRL)");
        items.add("GenotypeSummaries (NCC, GQ_MEAN, GQ_STDDEV)");
        items.add("InbreedingCoeff (InbreedingCoeff)");
        items.add("LikelihoodRankSumTest (LikelihoodRankSum)");
        items.add("MappingQuality (MMQ)");
        items.add("MappingQualityRankSumTest (MQRankSum)");
        items.add("MappingQualityZero (MQ0)");
        items.add("OrientationBiasReadCounts (F1R2, F2R1)");
        items.add("OriginalAlignment");
        items.add("PossibleDeNovo(hiConfDeNovo, loConfDeNovo)");
        items.add("QualByDepth (QD)");
        items.add("RMSMappingQuality (MQ)");
        items.add("RawGtCount");
        items.add("ReadPosRankSumTest (ReadPosRankSum)");
        items.add("ReadPosition (MPOS)");
        items.add("ReferenceBases (REF_BASES)");
        items.add("SampleList (Samples)");
        items.add("StrandBiasBySample (SB)");
        items.add("StrandOddsRatio (SOR)");
        items.add("TandemRepeat (STR, RU, RPA)");
        items.add("TransmittedSingleton (transmittedSingleton, nonTransmittedSingleton)");
        items.add("UniqueAltReadCount (AS_UNIQ_ALT_READ_COUNT)");

        initComponents();

//SETTING FOR FASTQC TABLE MODEL
        //FASTQC DEFAULTS
        headerQC = fastqcTable.getTableHeader();
        headerQC.setDefaultRenderer(new HeaderRenderer()); //Table Headers
        fastqcTable.getTableHeader().setReorderingAllowed(false);
        fastqcTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 10));
        TooltipCellRenderer tooltipRenderer = new TooltipCellRenderer(tooltips);
        for (int i = 1; i < fastqcTable.getColumnCount(); i++) { // Apply renderer to all data columns
            fastqcTable.getColumnModel().getColumn(i).setCellRenderer(tooltipRenderer);
        }
        fastqcTable.setRowHeight(20); // Adjust row height for multiline display
        fastqcTable.setRowSelectionAllowed(false);  // Disable row selection
        fastqcTable.setColumnSelectionAllowed(false); // Allow column selection
        fastqcTable.setCellSelectionEnabled(true);  // Allow individual cell selection

        // Add auto-suggest feature
        JTextField textField = (JTextField) annotationGroupCombo.getEditor().getEditorComponent();
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String input = textField.getText();
                if (input.isEmpty()) {
                    return;
                }

                // Match input with items
                Vector<String> matchedItems = new Vector<>();
                matchedItems.add(""); // Keep the empty option
                for (String item : items) {
                    if (item.toLowerCase().startsWith(input.toLowerCase())) {
                        matchedItems.add(item);
                    }
                }

                // Update the combo box model dynamically
                model = new DefaultComboBoxModel<>(matchedItems);
                annotationGroupCombo.setModel(model);
                textField.setText(input); // Keep the user's text
                annotationGroupCombo.showPopup();
            }
        });

        jListTumor.setTransferHandler(new ListItemTransferHandler());
        jListNormal.setTransferHandler(new ListItemTransferHandler());

        //IGV Tree
        treeModel = (DefaultTreeModel) jTree1.getModel();
//Clear text listeners 
// Create a single context menu with a "Clear" option
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem clearMenuItem = new JMenuItem("Clear");
        contextMenu.add(clearMenuItem);

        // Add mouse listener to each text field
        MouseAdapter mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showContextMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showContextMenu(e);
            }

            private void showContextMenu(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    JTextField sourceField = (JTextField) e.getSource();  // Identify the clicked JTextField
                    clearMenuItem.addActionListener(event -> sourceField.setText(""));
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };

        //Mutect2
        germlineResTxt.addMouseListener(mouseListener);
        ponTxt.addMouseListener(mouseListener);
        alleleTxt.addMouseListener(mouseListener);
        f1r2.addMouseListener(mouseListener);
        //intervalText.addMouseListener(mouseListener);

        //INPUT FILES SETTING
        readBG.add(singleRadioButton);
        readBG.add(pairRadioButton);

        haplotypeGroup.add(singleModeH);
        haplotypeGroup.add(jointModeH);

        variantAnnotationBG.add(snpEffRadio);
        variantAnnotationBG.add(vepRadio);
        variantAnnotationBG.add(snpEffvepRadio);

        qualityCheckBG.add(fastqcRadio);
        qualityCheckBG.add(fastpRadio);
        qualityCheckBG.add(noneRadio);

        haploIntervalBG.add(uploadIntervalfileR);
        haploIntervalBG.add(createIntervalR);

        labels = new ArrayList<>();
        textFields = new ArrayList<>();

        jLabelHelp.setIcon(App.icons[0]);

        //PANEL SETTINGS
        nextBtn.setText("Next");
        inputPanel.setVisible(true);
        byFastq.setVisible(false);
        byBam.setVisible(false);

        QualityControlPanel.setVisible(false);
        fastpPanel.setVisible(false);
        fastPparameter.setVisible(false);
        fastqcPanel.setVisible(false);
        trimPanel.setVisible(false);

        analysisPanel.setVisible(false);
        genomicDBPanel.setVisible(false);
        bwaPanel.setVisible(false);
        normalPanel.setVisible(false);
        otherPanel.setVisible(false);

        visualPanel.setVisible(false);

        //Default HaplotypeInterval Setting
        intervalT1.setText("");
        intervalBtn2.setEnabled(true);

        intervalT2.setText("");
        intervalText1.setText("");
        add9.setEnabled(false);
        del9.setEnabled(false);

        //Default strings 
        defaultAnalysis();
        defaultSetting();

    }//Constructor

    /**
     * MEMBER FUNCTIONS
     */
    public void defaultAnalysis() {
        //DEFAULT ANALYSIS PANEL SETTINGS
        refTxt.setText("");
        thread.setText("4");
        bwaM.setSelected(false);

        textFields.clear();
        labels.clear();
        addReplaceRGPanel.removeAll();

        listKnownSites.clear();
        splitNcigar.setSelected(false);

        germline.setSelected(false);
        germlinePanel.setVisible(false);
        somatic.setSelected(false);
        mutect2Panel.setVisible(false);

        //DEFAULT GERMLINE SETTINGS
        singleModeH.setSelected(true);
        ercCombo.setSelectedIndex(0);

        g1Check.setSelected(false);

        annotationGroupCombo.setEditable(false);
        add7.setEnabled(false);
        del7.setEnabled(false);
        annotationGroupTxt.setText("");

        bamoutCheck.setSelected(false);
        bamoutTxt.setText("");
        //DEFAULT MUTECT2 SETTINGS

        somaticOP = "";
        germSomaticOP = "";

        germlineCheck.setSelected(false);
        germlineResTxt.setText("");
        germlineResBtn.setEnabled(false);

        ponCheck.setSelected(false);
        ponTxt.setText("");
        ponBtn.setEnabled(false);
        generatePON.setSelected(false);

        allelesCheck.setSelected(false);
        alleleTxt.setText("");
        alleleBtn.setEnabled(false);

        f1r2Check.setSelected(false);
        f1r2.setText("");
        f1r2Btn.setEnabled(false);

        intervalCheck.setSelected(false);
        intervalT.setText("");
        intervalT.setEditable(false);
        intervalBtn.setEnabled(false);
        add6.setEnabled(false);
        del6.setEnabled(false);
        intervalText.setText("");
        listNormal.removeAllElements();
        mutectCombo.setSelectedIndex(0);
        //DEFAULT SELECTVARIANTS    
        jCheckBox5.setSelected(true);
        selectTypeCombo.setSelectedIndex(1);
        add1.setEnabled(true);
        del1.setEnabled(true);
        jCheckBox6.setSelected(false);
        select.setText("");
        select.setEditable(false);
        add2.setEnabled(false);
        del2.setEnabled(false);
        jCheckBox7.setSelected(false);
        selectGenotype.setText("");
        selectGenotype.setEditable(false);
        add3.setEnabled(false);
        del3.setEnabled(false);
        jCheckBox8.setSelected(false);
        Linterval.setText("");
        Linterval.setEditable(false);
        intervalBtn1.setEnabled(false);
        add4.setEnabled(false);
        del4.setEnabled(false);

        selectVariantTxt.setText("");
        //DEFAULT VATIANTFILTARATION
        variantFilterTxtB.setText("");
        filterExprB.setText("");
        filterNameB.setText("");
        snpEff_DB.setText("GRCh38.p14");
    }

    public void defaultSetting() {

        //OUTPUT SETTING FOR Narc SERVER
        //  output_Dir.setText("//media/nigab/sda1/iffat/");
        //  refTxt.setText("/media/nigab/sda1/Iffat/Reference/bwa/human.fa");
        // germlineResTxt.setText("/home/iffy/TestFiles/RNAVariant/00-common_all.vcf.gz");
        //    output_Dir.setText("/mnt/e/Iffat/IffatProject/TESTDATA/VariantOutput/");
        //  refTxt.setText("/mnt/d/Zeeshan/Genomes_indexed/GRCh38.p13.genome.fa");
        //knownSNPTxt.setText("/mnt/d/Downloads/snpEff_latest_core/snpEff/00-All-output38.vcf");
        //knownDelTxt.setText("/home/iffy/TestFiles/RNAVariant/mgp.v3.snps.sorted.rsIDdbSNPv137.vcf");
        // germlineResTxt.setText("/home/iffy/TestFiles/RNAVariant/00-common_all.vcf.gz");
        // ponTxt.setText("/home/iffy/TestFiles/RNAVariant/mgp.v3.snps.sorted.rsIDdbSNPv137.vcf.gz");

        /*
        output_Dir.setText("/home/iffy/TestFiles/Output_Variant/");
        refTxt.setText("/home/iffy/TestFiles/MouseExample/INPUTS/mm9.fa");
        knownSNPTxt.setText("/home/iffy/TestFiles/RNAVariant/mm9DBSNP.vcf");
        // knownSNPTxt.setText("/home/iffy/TestFiles/RNAVariant/00-common_all.vcf");
        //knownDelTxt.setText("/home/iffy/TestFiles/RNAVariant/mgp.v3.snps.sorted.rsIDdbSNPv137.vcf");
        knownDelTxt.setText("");
        snpEff_DB.setText("mm10");

        germlineResTxt.setText("/home/iffy/TestFiles/RNAVariant/00-common_all.vcf.gz");
        ponTxt.setText("/home/iffy/TestFiles/RNAVariant/mgp.v3.snps.sorted.rsIDdbSNPv137.vcf.gz");
         */
    }

// Recursive method to add files and folders
    public void addNodes(DefaultMutableTreeNode node, File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {

                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(file.getName());
                node.add(childNode);
                if (file.isDirectory()) {
                    addNodes(childNode, file);  // Recursive call for subfolders
                }
            }
        }
    }

    public void runFastq_Variant() {
        warningLabel.setText(null);
        pipelineError = false;
        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));

        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

            @Override
            protected Integer doInBackground() {
                Process p;
                try {

                    /////////////////////////printing Pair MAP
//                    for (Map.Entry<String, Map<String, List<String>>> outerEntry : pairPathMap.entrySet()) {
//                        String key = outerEntry.getKey();
//                        Map<String, List<String>> pair = outerEntry.getValue();
//
//                        System.out.println("Key: " + key);
//
//                        List<String> r1List = pair.get("R1");
//                        List<String> r2List = pair.get("R2");
//
//                        System.out.println("   R1 Reads:");
//                        if (r1List != null) {
//                            for (String r1 : r1List) {
//                                System.out.println("      " + r1);
//                            }
//                        }
//
//                        System.out.println("   R2 Reads:");
//                        if (r2List != null) {
//                            for (String r2 : r2List) {
//                                System.out.println("      " + r2);
//                            }
//                        }
//                    }
////end printing PairMap

                    List<String> cmdList = new ArrayList<String>();

                    cmdList.add("sh");
                    cmdList.add(System.getProperty("user.dir").concat("/bin/RNA_ShortVariant.sh"));
                    //PATHS:
                    cmdList.add(App.BWA_PATH);//1
                    cmdList.add(App.SAMTOOLS_PATH);//2
                    cmdList.add(App.GATK_PATH);

                    //readpath,filename,@rg#
                    //
                    if (singleRadioButton.isSelected()) {

                        String myArray = "";

                        List<String> paths = singlePathMap.getOrDefault(selectedKey, List.of());

                        for (int i = 0; i < paths.size(); i++) {

                            myArray += (paths.get(i) + "#"
                                    + sfileNames.get(i) + "#"
                                    + retrieveData(i) + ",").trim();
                        }
                        myArray = StringUtils.chop(myArray).replaceAll("\"", "");
                        cmdList.add(myArray); //Comma seperated 3
                        cmdList.add("SINGLE");//4
                    } else if (pairRadioButton.isSelected()) //PAIR1 hash PAIR2 COMMA SEPARATED pair
                    {   //"readpath1"* "readpath2"#filename#"@RG"

                        String myArray = "";

                        Map<String, List<String>> innerMap = pairPathMap.getOrDefault(selectedKey, Map.of());

//                        if (innerMap.isEmpty()) {
//                            System.out.println("innerMap is empty for selectedKey: " + selectedKey);
//                        } else {
//                            System.out.println("innerMap for " + selectedKey + ": " + innerMap);
//                        }
//
//                        System.out.println("pairPathMap: " + pairPathMap);
                        List<String> r1List = innerMap.getOrDefault("R1", List.of());
                        List<String> r2List = innerMap.getOrDefault("R2", List.of());

                        for (int i = 0; i < r1List.size(); i++) {

                            myArray += (r1List.get(i) + "*"
                                    + r2List.get(i) + "#"
                                    // + p1fileNames.get(i) + "_" + p2fileNames.get(i) + "#"
                                    + p1p2fileNames.get(i) + "#"
                                    + retrieveData(i) + ",").trim();
                        }

                        myArray = StringUtils.chop(myArray).replaceAll("\"", "");//Removing last character and quotes
                        cmdList.add(myArray); //Comma seperated 3
                        cmdList.add("PAIR");//4
                    }

                    cmdList.add(thread.getText());//5 thread
                    if (bwaM.isSelected()) //6-M(yes)
                    {
                        cmdList.add("yes");
                    } else {
                        cmdList.add("no");
                    }

                    cmdList.add(refTxt.getText());//7 Reference File
                    cmdList.add(output_Dir.getText()); //8

                    //display("VariantCommandString", cmdList.toString());
                    ProcessBuilder pb = new ProcessBuilder(cmdList);
                    pb.redirectErrorStream(true);
                    p = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;

                    while ((line = reader.readLine()) != null) {

                        App.LOGGER.info("\r\n" + line);
                        publish("\r\n" + line);

                        if (line.contains("killed") || line.contains("aborted")
                                || line.contains("error") || line.contains("exception")
                                || line.contains("./bwa: not found") || line.contains("[E::bwa_")
                                || line.contains("[E::")) {
                            pipelineError = true;
                            break;
                        }

                    }
                    if (!isCancelled()) {
                        status = p.waitFor();
                    }

                    p.destroy();

                } catch (IOException | InterruptedException ex) {

                    ex.printStackTrace(new PrintWriter(App.stack));
                    App.LOGGER.error("\r\n ERROR in analysis: " + App.stack);
                    App.textArea.append("\r\n ERROR in analysis: " + App.stack);

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
                if (!pipelineError) {
//PREPARING BAM FILES DATA

                    listModel.clear();
                    if (singleRadioButton.isSelected()) {
                        for (int i = 0; i < sfilePaths.size(); i++) {
                            listModel.addElement(" \"" + output_Dir.getText() + sfileNames.get(i) + ".bam" + "\"");
                        }
                    } else if (pairRadioButton.isSelected()) {

                        for (int i = 0; i < p1filePaths.size(); i++) {
                            listModel.addElement(" \"" + output_Dir.getText() + p1p2fileNames.get(i) + ".bam" + "\"");
                            //     listModel.addElement(" \"" + output_Dir.getText() + p1fileNames.get(i) + "_" + p2fileNames.get(i) + ".bam" + "\"");

                        }

                    }
                    runBam_Variant();
                }//endif
            }//done

        };

        worker.execute();
    }//run Fastq

    public void runBam_Variant() {
        warningLabel.setText(null);
        pipelineError = false;
        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));

        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

            @Override
            protected Integer doInBackground() {
                Process p;
                try {

                    List<String> cmdList = new ArrayList<String>();

                    cmdList.add("sh");
                    cmdList.add(System.getProperty("user.dir").concat("/bin/RNA_ShortVariant2.sh"));
                    //PATHS:
                    cmdList.add(App.STAR_PATH); //$1
                    cmdList.add(App.PICARD_PATH);//2
                    cmdList.add(App.GATK_PATH);//3
                    cmdList.add(App.SNPEFF_PATH);//4
                    cmdList.add(App.VEP_PATH);//5

                    cmdList.add(System.getProperty("user.dir").concat("/R_Libraries")); //6

                    //BAM FILES
                    String bamfiles = "";
                    for (int i = 0; i < inputBamTxt.getModel().getSize(); i++) {

                        bamfiles += (inputBamTxt.getModel().getElementAt(i) + ",").trim();
                    }
                    bamfiles = StringUtils.chop(bamfiles).replaceAll("\"", "");

                    cmdList.add(bamfiles);//7 Bam file name
                    cmdList.add(refTxt.getText());//8 Refernce File
                    cmdList.add(output_Dir.getText()); //9

                    bamfiles = "--known-sites ";
                    for (int i = 0; i < knownSites.getModel().getSize(); i++) {
                        if (i == (knownSites.getModel().getSize() - 1)) {
                            bamfiles += (knownSites.getModel().getElementAt(i).trim());
                        } else {
                            bamfiles += (knownSites.getModel().getElementAt(i).trim() + " --known-sites ");
                        }
                    }

                    cmdList.add(bamfiles);//10 known sites
                    cmdList.add(StringUtils.chop(retrieveData()).replaceAll("\"", "")); //11 AddreplaceGroups

                    if (splitNcigar.isSelected()) {
                        cmdList.add("YES");//12 
                    } else {
                        cmdList.add("NO");//12 
                    }

                    if (singleModeH.isSelected()) {
                        cmdList.add("SINGLEMODE");//13 
                        cmdList.add(""); //14
                    } else if (jointModeH.isSelected()) {
                        cmdList.add("JOINTMODE");//13 
                        if (uploadIntervalfileR.isSelected()) {
                            cmdList.add(" -L " + intervalT1.getText());//14
                        } else {
                            cmdList.add(intervalText1.getText());//14
                        }

                    }

                    cmdList.add(thread.getText());  //15

                    //if snpEff, VEP, BOTH   16-19
                    if (snpEffRadio.isSelected()) {
                        cmdList.add("snpeff");//16
                        cmdList.add(snpEff_DB.getText());//17  
                        cmdList.add("");//18
                        cmdList.add("");//19

                    } else if (vepRadio.isSelected()) {
                        cmdList.add("vep");//16
                        cmdList.add(vepAssembly.getText());//17
                        cmdList.add(vepSpecies.getText());//18
                        cmdList.add("");//19
                    } else if (snpEffvepRadio.isSelected()) {
                        cmdList.add("both");//16
                        cmdList.add(snpEff_DB.getText());//17 
                        cmdList.add(vepAssembly.getText());//18
                        cmdList.add(vepSpecies.getText());//19
                    }
                    //20-23
                    if (selectVariantsOp.isSelected() && variantFilterA.isSelected()) {
                        cmdList.add("ALL");//20
                        cmdList.add(selectVariantTxt.getText().trim());//21
                        cmdList.add(variantFilterTxtB.getText().trim()); //22
                        cmdList.add(variantFilterTxtA.getText().trim()); //23
                    } else if (variantFilterA.isSelected()) {
                        cmdList.add("BA");//20
                        cmdList.add("");//21
                        cmdList.add(variantFilterTxtB.getText().trim()); //22
                        cmdList.add(variantFilterTxtA.getText().trim()); //23
                    }

                    //Checking germ or somatic selected  24
                    if ((germline.isSelected()) && (somatic.isSelected())) {
                        germSomaticOP = "BOTH";
                    } else if (germline.isSelected()) {
                        germSomaticOP = "GERMLINE";
                    } else if (somatic.isSelected()) {
                        germSomaticOP = "SOMATIC";

                    }

                    cmdList.add(germSomaticOP);// 24 Option 

                    cmdList.add(somaticOP);//25 somatic options

                    if (germline.isSelected() && somatic.isSelected()) {
                        //Germline Setting
                        cmdList.add(ercCombo.getSelectedItem().toString()); //26 -ERC

                        if (g1Check.isSelected()) {
                            cmdList.add(annotationGroupTxt.getText()); //27 -G
                        } else {
                            cmdList.add("");//27
                        }

                        if (bamoutCheck.isSelected()) {
                            cmdList.add(bamoutTxt.getText()); //-28 -bamout
                        } else {
                            cmdList.add("");//28
                        }
                        //Somatic setting  

                        if (mutectCombo.getSelectedIndex() == 1 || mutectCombo.getSelectedIndex() == 2) //Tumor with matched normal (single)
                        {

                            //Tumor-NORMAL BAM FILES
                            String nbamfiles = "";
                            for (int i = 0; i < jListNormal.getModel().getSize(); i++) {

                                nbamfiles += (jListTumor.getModel().getElementAt(i) + "#" + jListNormal.getModel().getElementAt(i) + ",").trim();

                            }
                            nbamfiles = StringUtils.chop(nbamfiles);
                            cmdList.add(nbamfiles);//27 Tumor-Normal Bam file name  

                        } else if (mutectCombo.getSelectedIndex() == 3) {

                            //Tumor only mode
                            if (jListNormal.getModel().getSize() == 0) {

                                //Tumor BAM FILES
                                String nbamfiles = "";
                                for (int i = 0; i < jListTumor.getModel().getSize(); i++) {

                                    nbamfiles += (jListTumor.getModel().getElementAt(i) + ",").trim();

                                }
                                nbamfiles = StringUtils.chop(nbamfiles);
                                cmdList.add(nbamfiles);//27 Tumor Bam file name  

                            }//tumorOnly
                            //Pon creation mode (normals only)
                            else if (jListNormal.getModel().getSize() == 0) {
                                //Tumor BAM FILES
                                String nbamfiles = "";
                                for (int i = 0; i < jListNormal.getModel().getSize(); i++) {

                                    nbamfiles += (jListNormal.getModel().getElementAt(i) + ",").trim();

                                }
                                nbamfiles = StringUtils.chop(nbamfiles);
                                cmdList.add(nbamfiles);//27 Tumor Bam file name 
                            }//normal only mode
                            //tumor with PON mode
                            else {

                                if (generatePON.isSelected()) {
                                    //Tumor-NORMAL BAM FILES
                                    String nbamfiles = "";
                                    for (int i = 0; i < jListNormal.getModel().getSize(); i++) {

                                        nbamfiles += (jListTumor.getModel().getElementAt(i) + "#" + jListNormal.getModel().getElementAt(i) + ",").trim();

                                    }
                                    nbamfiles = StringUtils.chop(nbamfiles);
                                    cmdList.add(nbamfiles);//27 Tumor-Normal Bam file name  

                                }

                            }//tumor with PON mode

                        } else if (mutectCombo.getSelectedIndex() == 4 || mutectCombo.getSelectedIndex() == 5) //Tumor Only mode
                        {

                            //Tumor BAM FILES
                            String nbamfiles = "";
                            for (int i = 0; i < jListTumor.getModel().getSize(); i++) {

                                nbamfiles += (jListTumor.getModel().getElementAt(i) + ",").trim();

                            }
                            nbamfiles = StringUtils.chop(nbamfiles);
                            cmdList.add(nbamfiles);//27 Tumor Bam file name  

                        }
                        cmdList.add(germlineResTxt.getText());// 28 germline resource
                        cmdList.add(ponTxt.getText());// 29 PON
                        cmdList.add(alleleTxt.getText());//30 allele
                        cmdList.add(f1r2.getText());//31 f1
                        cmdList.add(intervalText.getText());//32

                    } else if (germline.isSelected()) {

                        cmdList.add(ercCombo.getSelectedItem().toString()); //24 -ERC

                        if (g1Check.isSelected()) {
                            cmdList.add(annotationGroupTxt.getText()); //25 -G
                        } else {
                            cmdList.add("");//25
                        }

                        if (bamoutCheck.isSelected()) {
                            cmdList.add(bamoutTxt.getText()); //26 -bamout
                        } else {
                            cmdList.add("");//26
                        }

                    } else if (somatic.isSelected()) {

                        if (mutectCombo.getSelectedIndex() == 1 || mutectCombo.getSelectedIndex() == 2) //Tumor with matched normal (single)
                        {

                            //Tumor-NORMAL BAM FILES
                            String nbamfiles = "";
                            for (int i = 0; i < jListNormal.getModel().getSize(); i++) {

                                nbamfiles += (jListTumor.getModel().getElementAt(i) + "#" + jListNormal.getModel().getElementAt(i) + ",").trim();

                            }
                            nbamfiles = StringUtils.chop(nbamfiles);
                            cmdList.add(nbamfiles);//24 Tumor-Normal Bam file name  

                        } else if (mutectCombo.getSelectedIndex() == 3) {

                            //Tumor only mode
                            if (jListNormal.getModel().getSize() == 0) {

                                //Tumor BAM FILES
                                String nbamfiles = "";
                                for (int i = 0; i < jListTumor.getModel().getSize(); i++) {

                                    nbamfiles += (jListTumor.getModel().getElementAt(i) + ",").trim();

                                }
                                nbamfiles = StringUtils.chop(nbamfiles);
                                cmdList.add(nbamfiles);//24 Tumor Bam file name  

                            }//tumorOnly
                            //Pon creation mode (normals only)
                            else if (jListNormal.getModel().getSize() == 0) {
                                //Tumor BAM FILES
                                String nbamfiles = "";
                                for (int i = 0; i < jListNormal.getModel().getSize(); i++) {

                                    nbamfiles += (jListNormal.getModel().getElementAt(i) + ",").trim();

                                }
                                nbamfiles = StringUtils.chop(nbamfiles);
                                cmdList.add(nbamfiles);//24 Tumor Bam file name 
                            }//normal only mode
                            //tumor with PON mode
                            else {

                                if (generatePON.isSelected()) {
                                    //Tumor-NORMAL BAM FILES
                                    String nbamfiles = "";
                                    for (int i = 0; i < jListNormal.getModel().getSize(); i++) {

                                        nbamfiles += (jListTumor.getModel().getElementAt(i) + "#" + jListNormal.getModel().getElementAt(i) + ",").trim();

                                    }
                                    nbamfiles = StringUtils.chop(nbamfiles);
                                    cmdList.add(nbamfiles);//24 Tumor-Normal Bam file name  

                                }

                            }//tumor with PON mode

                        } else if (mutectCombo.getSelectedIndex() == 4 || mutectCombo.getSelectedIndex() == 5) //Tumor Only mode
                        {

                            //Tumor BAM FILES
                            String nbamfiles = "";
                            for (int i = 0; i < jListTumor.getModel().getSize(); i++) {

                                nbamfiles += (jListTumor.getModel().getElementAt(i) + ",").trim();

                            }
                            nbamfiles = StringUtils.chop(nbamfiles);
                            cmdList.add(nbamfiles);//24 Tumor Bam file name  

                        }
                        cmdList.add(germlineResTxt.getText());// 25 germline resource
                        cmdList.add(ponTxt.getText());// 26 PON
                        cmdList.add(alleleTxt.getText());//27 allele
                        cmdList.add(f1r2.getText());//28 f1
                        cmdList.add(intervalText.getText());//29

                    }
                    //display("VariantCommandString", cmdList.toString());
                    ProcessBuilder pb = new ProcessBuilder(cmdList);
                    pb.redirectErrorStream(true);
                    p = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;

                    while ((line = reader.readLine()) != null) {

                        App.LOGGER.info("\r\n" + line);
                        publish("\r\n" + line);

                        if (!pipelineError && (line.toLowerCase().contains("user error")
                                || line.toLowerCase().contains("exception")
                                || line.toLowerCase().contains("killed")
                                || line.toLowerCase().contains("aborted")
                                || line.toLowerCase().contains("no such file or directory"))) {

                            pipelineError = true;
                        }

                    }
                    if (!isCancelled()) {
                        status = p.waitFor();
                    }

                    p.destroy();

                } catch (IOException | InterruptedException ex) {

                    ex.printStackTrace(new PrintWriter(App.stack));
                    App.LOGGER.error("\r\n ERROR in analysis: " + App.stack);
                    App.textArea.append("\r\n ERROR in analysis: " + App.stack);

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
                 if (!pipelineError) {
                       //*.annH.sift.vcf,*_vep_annH.sift.vcf
                    if (snpEffvepRadio.isSelected()) {
                        
                        if(germline.isSelected()){
                            pipelineStage="germline_snpeff";
                       
                            VCFHandler(System.getProperty("user.dir").concat("/bin/extract_all_vcfs.sh"), output_Dir.getText(), "*.annH.sift.vcf");
                        
                     
                        
                        }
//                        if (somatic.isSelected()){
//                        VCFHandler(System.getProperty("user.dir").concat("/bin/extract_all_vcfs.sh"), output_Dir.getText(), "*.annM.sift.vcf");
//                        VCFHandler(System.getProperty("user.dir").concat("/bin/extract_all_vcfs.sh"), output_Dir.getText(), "*_vep_annM.sift.vcf");
//                            
//                        }
                        
                        
                        
                    } else if (snpEffRadio.isSelected()) {
                        if(germline.isSelected())
                            VCFHandler(System.getProperty("user.dir").concat("/bin/extract_all_vcfs.sh"), output_Dir.getText(), "*.annH.sift.vcf");
                 
                        if(somatic.isSelected())
                            VCFHandler(System.getProperty("user.dir").concat("/bin/extract_all_vcfs.sh"), output_Dir.getText(), "*.annM.sift.vcf");
                 
                    
                    } else {
                        if (germline.isSelected())
                            VCFHandler(System.getProperty("user.dir").concat("/bin/extract_all_vcfs.sh"), output_Dir.getText(), "*_vep_annH.sift.vcf");

                        if (somatic.isSelected())
                            VCFHandler(System.getProperty("user.dir").concat("/bin/extract_all_vcfs.sh"), output_Dir.getText(), "*_vep_annM.sift.vcf");

                    }

//                    //TREE SETTINGS
//                    root = new DefaultMutableTreeNode(new File(output_Dir.getText()).getName());
//
//                    addNodes(root, new File(output_Dir.getText()));
//
//                    treeModel.setRoot(root);
//                    treeModel.reload();
//                    // Expand the root node to show its contents
//                    jTree1.expandPath(new TreePath(root.getPath()));
//                    // Refresh the tree model
//                    //((DefaultTreeModel) jTree1.getModel()).reload();
//
//                    visualPanel.setVisible(true);
                }//if
                else{
                            publish("\r\n" + "ERROR: Pipeline failed in previous step. The following processes were NOT completed:\n"
                       + " - TSV Extraction from annotated VCF files\n"
                       + " - Combined Wide/Long TSV matrix generation\n\n"
                       + "Please resolve the errors above and re-run.");

                }
                
            }//done

        };

        worker.execute();

    }//runBam

    public void VCFHandler(String scriptPath, String directoryPath, String pattern) {

        warningLabel.setText(null);
        pipelineError = false;
        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress: Processing VCF Files....", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));

        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

            @Override
            protected Integer doInBackground() {
                Process process;
                try {

                    //*.annH.sift.vcf,*_vep_annH.sift.vcf
                    // Build the process
                    ProcessBuilder processBuilder = new ProcessBuilder("bash", scriptPath, directoryPath, pattern);
                    processBuilder.redirectErrorStream(true);
                    // Start the process
                    process = processBuilder.start();

                    // Read the output
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        App.textArea.append(line + "\n");
                    }

                    if (!isCancelled()) {
                        status = process.waitFor();
                    }

                    process.destroy();

                } catch (IOException | InterruptedException ex) {
                    pipelineError = true;
                    ex.printStackTrace(new PrintWriter(App.stack));
                    App.LOGGER.error("\r\n ERROR in analysis: " + App.stack);
                    App.textArea.append("\r\n ERROR in analysis: " + App.stack);

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
                if (!pipelineError) {
    
                    if (snpEffvepRadio.isSelected()) {

                        if(germline.isSelected()){
                      
                        if(pipelineStage=="germline_snpeff") {
                            VCF_PivotFiles(System.getProperty("user.dir").concat("/bin/VCF_PivotFiles.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"), output_Dir.getText().concat("/mergedLong_annH_sift_all.tsv"),System.getProperty("user.dir").concat("/bin/genomic_report.Rmd"));
                        }
                        else if(pipelineStage=="germline_vep") {
                           // pipelineStage="germline_snpeff";
                             VCF_PivotFiles(System.getProperty("user.dir").concat("/bin/VCF_PivotFiles.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"), output_Dir.getText().concat("/mergedLong_vep_annH_sift_all.tsv"),System.getProperty("user.dir").concat("/bin/genomic_report.Rmd"));   
                            
                        }
                                                
                        }
                        
                        if(somatic.isSelected()){
                        if(pipelineStage=="somatic_snpeff") {
                        
                            VCF_PivotFiles(System.getProperty("user.dir").concat("/bin/VCF_PivotFiles.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"), output_Dir.getText().concat("/mergedLong_annM_sift_all.tsv"),System.getProperty("user.dir").concat("/bin/genomic_report.Rmd"));
                        }
                        else if(pipelineStage=="somatic_vep") {
                        
                            VCF_PivotFiles(System.getProperty("user.dir").concat("/bin/VCF_PivotFiles.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"), output_Dir.getText().concat("/mergedLong_vep_annM_sift_all.tsv"),System.getProperty("user.dir").concat("/bin/genomic_report.Rmd"));    
                        }
                        
                        }

                    } else if (snpEffRadio.isSelected()) {
                      if(germline.isSelected())      
                        VCF_PivotFiles(System.getProperty("user.dir").concat("/bin/VCF_PivotFiles.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"), output_Dir.getText().concat("/mergedLong_annH_sift_all.tsv"),System.getProperty("user.dir").concat("/bin/genomic_report.Rmd"));
                       if(somatic.isSelected())      
                        VCF_PivotFiles(System.getProperty("user.dir").concat("/bin/VCF_PivotFiles.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"), output_Dir.getText().concat("/mergedLong_annM_sift_all.tsv"),System.getProperty("user.dir").concat("/bin/genomic_report.Rmd"));
                    } else {
                        if(germline.isSelected()){
                        VCF_PivotFiles(System.getProperty("user.dir").concat("/bin/VCF_PivotFiles.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"), output_Dir.getText().concat("/mergedLong_vep_annH_sift_all.tsv"),System.getProperty("user.dir").concat("/bin/genomic_report.Rmd"));
    
                        }
                        if(somatic.isSelected()){
                        VCF_PivotFiles(System.getProperty("user.dir").concat("/bin/VCF_PivotFiles.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"), output_Dir.getText().concat("/mergedLong_vep_annM_sift_all.tsv"),System.getProperty("user.dir").concat("/bin/genomic_report.Rmd"));
    
                        }
                    }

                }//endif
            }//done

        };

        worker.execute();

    }

    public void VCF_PivotFiles(String scriptPath, String directoryPath, String rLib, String filePath,String markDownFile) {

        warningLabel.setText(null);
        pipelineError = false;
        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress: Processing merged VCF Files....", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));

        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

            @Override
            protected Integer doInBackground() {
                Process process;
                try {

                    // Build the process
                    ProcessBuilder processBuilder = new ProcessBuilder("Rscript",
                            scriptPath,
                            directoryPath,
                            rLib,
                            filePath,markDownFile);
                    processBuilder.redirectErrorStream(true);
                    // Start the process
                    process = processBuilder.start();

                    // Read the output
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        App.textArea.append(line + "\n");
                    }

                    if (!isCancelled()) {
                        status = process.waitFor();
                    }

                    process.destroy();

                } catch (IOException | InterruptedException ex) {
                    pipelineError = true;
                    ex.printStackTrace(new PrintWriter(App.stack));
                    App.LOGGER.error("\r\n ERROR in analysis: " + App.stack);
                    App.textArea.append("\r\n ERROR in analysis: " + App.stack);

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
                if (!pipelineError) {
                
                if (snpEffvepRadio.isSelected()) {
                       
                        if(germline.isSelected()){
                        if(pipelineStage=="germline_snpeff"){
                            pipelineStage="germline_vep";
                            VCFHandler(System.getProperty("user.dir").concat("/bin/extract_all_vcfs.sh"), output_Dir.getText(), "*_vep_annH.sift.vcf");
                          
                        }
                             
                        }
                       
                        if(somatic.isSelected()){
                        VCF_PivotFiles(System.getProperty("user.dir").concat("/bin/VCF_PivotFiles.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"), output_Dir.getText().concat("/mergedLong_annM_sift_all.tsv"),System.getProperty("user.dir").concat("/bin/genomic_report.Rmd"));
                        VCF_PivotFiles(System.getProperty("user.dir").concat("/bin/VCF_PivotFiles.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"), output_Dir.getText().concat("/mergedLong_vep_annM_sift_all.tsv"),System.getProperty("user.dir").concat("/bin/genomic_report.Rmd"));    
                            
                        }

                    }
                
                
                
                
                    
                }//endif
            }//done

        };

        worker.execute();

    }

    public void inputFilesDataS(Map<String, List<String>> fileMap, String key) {

//RESETING ALL ARRAYS
        sfileNames = new ArrayList<>();
        sfilePaths = new ArrayList<>();
        sfileExtensions = new ArrayList<>();

//READ INPUT FILES
        List<String> filePaths = fileMap.getOrDefault(key, List.of());
        int i = 0;
        for (String fullPath : filePaths) {
            sfilePaths.add(i, fullPath.replaceAll("\"", "").trim());

            if ((sfilePaths.get(i).endsWith(".gz")) || (sfilePaths.get(i).endsWith(".bz2"))) {

                sfileNames.add(i, FilenameUtils.getBaseName(FilenameUtils.removeExtension(sfilePaths.get(i))));
                sfileExtensions.add(i, sfilePaths.get(i).substring(sfilePaths.get(i).lastIndexOf(sfileNames.get(i)) + sfileNames.get(i).length() + 1));

            } else if (sfilePaths.get(i).endsWith(".bam")) {

                String fullFileName = sfilePaths.get(i).substring(sfilePaths.get(i).lastIndexOf('/') + 1);
                String nameWithoutSuffix = fullFileName.replaceAll("(?i)\\.star\\.Aligned\\.sortedByCoord\\.out(?:\\.bam)?$", "");
// remove suffix ignoring case
                String baseName = nameWithoutSuffix.replaceAll("\\.bam$", ""); // remove .bam extension
                String extension = fullFileName.substring(baseName.length());

                sfileNames.add(i, baseName);
                sfileExtensions.add(i, extension);
            } else {

                sfileNames.add(i, FilenameUtils.getBaseName(sfilePaths.get(i)));
                sfileExtensions.add(i, sfilePaths.get(i).substring(sfilePaths.get(i).lastIndexOf(sfileNames.get(i)) + sfileNames.get(i).length() + 1));
            }
            i++;
        }//endFor

        addComponents(filePaths.size());

//END READ INPUT FILES
    }

    public void inputFilesDataP(Map<String, Map<String, List<String>>> pairMap, String key) {

//RESETING ALL ARRAYS
        p1fileNames = new ArrayList<>();
        p1filePaths = new ArrayList<>();
        p1fileExtensions = new ArrayList<>();

        p2fileNames = new ArrayList<>();
        p2filePaths = new ArrayList<>();
        p2fileExtensions = new ArrayList<>();

        p1p2fileNames = new ArrayList<>();

        Map<String, List<String>> innerMap = pairMap.getOrDefault(key, Map.of());
        List<String> r1List = innerMap.getOrDefault("R1", List.of());
        List<String> r2List = innerMap.getOrDefault("R2", List.of());

//READ INPUT FILES
        //PAIR 1
        int i = 0;
        for (String path : r1List) {

            p1filePaths.add(i, path.replaceAll("\"", "").trim());

            if ((p1filePaths.get(i).endsWith(".gz")) || (p1filePaths.get(i).endsWith(".bz2"))) {
                String baseName = FilenameUtils.getBaseName(FilenameUtils.removeExtension(p1filePaths.get(i)));
                p1p2fileNames.add(baseName.replaceAll("_[12]$", ""));
                p1fileNames.add(i, baseName);
                p1fileExtensions.add(i, p1filePaths.get(i).substring(p1filePaths.get(i).lastIndexOf(p1fileNames.get(i)) + p1fileNames.get(i).length() + 1));

            } else {
                String baseName = FilenameUtils.getBaseName(p1filePaths.get(i));
                p1p2fileNames.add(baseName.replaceAll("_[12]$", ""));
                p1fileNames.add(i, baseName);
                p1fileExtensions.add(i, p1filePaths.get(i).substring(p1filePaths.get(i).lastIndexOf(p1fileNames.get(i)) + p1fileNames.get(i).length() + 1));
            }

        }//for

        i = 0;
        //PAIR 2
        for (String path : r2List) {

            p2filePaths.add(i, path.replaceAll("\"", "").trim());

            if ((p2filePaths.get(i).endsWith(".gz")) || (p2filePaths.get(i).endsWith(".bz2"))) {
                String baseName = FilenameUtils.getBaseName(FilenameUtils.removeExtension(p2filePaths.get(i)));
                p2fileNames.add(i, baseName);
                p2fileExtensions.add(i, p2filePaths.get(i).substring(p2filePaths.get(i).lastIndexOf(p2fileNames.get(i)) + p2fileNames.get(i).length() + 1));

            } else {
                String baseName = FilenameUtils.getBaseName(p2filePaths.get(i));

                p2fileNames.add(i, baseName);
                p2fileExtensions.add(i, p2filePaths.get(i).substring(p2filePaths.get(i).lastIndexOf(p2fileNames.get(i)) + p2fileNames.get(i).length() + 1));
            }

        }//for

        addComponents(r1List.size());

//END READ INPUT FILES
    }

    public void inputFilesData() {

        if (inputOptions.getSelectedIndex() == 1) {
//RESETING ALL ARRAYS
            sfileNames = new ArrayList<>();
            sfilePaths = new ArrayList<>();
            sfileExtensions = new ArrayList<>();

            p1fileNames = new ArrayList<>();
            p1filePaths = new ArrayList<>();
            p1fileExtensions = new ArrayList<>();

            p2fileNames = new ArrayList<>();
            p2filePaths = new ArrayList<>();
            p2fileExtensions = new ArrayList<>();

            p1p2fileNames = new ArrayList<>();
//READ INPUT FILES
            if (singleRadioButton.isSelected()) {
                for (int i = 0; i < readFilesInSingle.getModel().getSize(); i++) {
                    sfilePaths.add(i, readFilesInSingle.getModel().getElementAt(i).replaceAll("\"", "").trim());

                    if ((sfilePaths.get(i).endsWith(".gz")) || (sfilePaths.get(i).endsWith(".bz2"))) {

                        sfileNames.add(i, FilenameUtils.getBaseName(FilenameUtils.removeExtension(sfilePaths.get(i))));
                        sfileExtensions.add(i, sfilePaths.get(i).substring(sfilePaths.get(i).lastIndexOf(sfileNames.get(i)) + sfileNames.get(i).length() + 1));

                    } else {

                        sfileNames.add(i, FilenameUtils.getBaseName(sfilePaths.get(i)));
                        sfileExtensions.add(i, sfilePaths.get(i).substring(sfilePaths.get(i).lastIndexOf(sfileNames.get(i)) + sfileNames.get(i).length() + 1));
                    }

                }//endFor

//                for (String s : sfilePaths) {
//                    System.out.println(s);
//                }
//                for (String s : sfileNames) {
//                    System.out.println(s);
//                }
//                for (String s : sfileExtensions) {
//                    System.out.println(s);
//                }
                addComponents(listModelreadSingle.size());

            } else if (pairRadioButton.isSelected()) {

                //PAIR 1
                for (int i = 0; i < readFilesInPair1.getModel().getSize(); i++) {

                    p1filePaths.add(i, readFilesInPair1.getModel().getElementAt(i).replaceAll("\"", "").trim());

                    if ((p1filePaths.get(i).endsWith(".gz")) || (p1filePaths.get(i).endsWith(".bz2"))) {
                        String baseName = FilenameUtils.getBaseName(FilenameUtils.removeExtension(p1filePaths.get(i)));
                        p1p2fileNames.add(baseName.replaceAll("_[12]$", ""));
                        p1fileNames.add(i, baseName);
                        p1fileExtensions.add(i, p1filePaths.get(i).substring(p1filePaths.get(i).lastIndexOf(p1fileNames.get(i)) + p1fileNames.get(i).length() + 1));

                    } else {
                        String baseName = FilenameUtils.getBaseName(p1filePaths.get(i));
                        p1p2fileNames.add(baseName.replaceAll("_[12]$", ""));
                        p1fileNames.add(i, baseName);
                        p1fileExtensions.add(i, p1filePaths.get(i).substring(p1filePaths.get(i).lastIndexOf(p1fileNames.get(i)) + p1fileNames.get(i).length() + 1));
                    }

                }//for
                //PAIR 2
                for (int i = 0; i < readFilesInPair2.getModel().getSize(); i++) {

                    p2filePaths.add(i, readFilesInPair2.getModel().getElementAt(i).replaceAll("\"", "").trim());

                    if ((p2filePaths.get(i).endsWith(".gz")) || (p2filePaths.get(i).endsWith(".bz2"))) {
                        String baseName = FilenameUtils.getBaseName(FilenameUtils.removeExtension(p2filePaths.get(i)));
                        p2fileNames.add(i, baseName);
                        p2fileExtensions.add(i, p2filePaths.get(i).substring(p2filePaths.get(i).lastIndexOf(p2fileNames.get(i)) + p2fileNames.get(i).length() + 1));

                    } else {
                        String baseName = FilenameUtils.getBaseName(p2filePaths.get(i));

                        p2fileNames.add(i, baseName);
                        p2fileExtensions.add(i, p2filePaths.get(i).substring(p2filePaths.get(i).lastIndexOf(p2fileNames.get(i)) + p2fileNames.get(i).length() + 1));
                    }

                }//for

                addComponents(listModelreadPair2.size());
            }

//            else if (pairRadioButton.isSelected()) {
//
//                //PAIR 1
//                for (int i = 0; i < readFilesInPair1.getModel().getSize(); i++) {
//
//                    p1filePaths.add(i, readFilesInPair1.getModel().getElementAt(i).replaceAll("\"", "").trim());
//
//                    if ((p1filePaths.get(i).endsWith(".gz")) || (p1filePaths.get(i).endsWith(".bz2"))) {
//
//                        p1fileNames.add(i, FilenameUtils.getBaseName(FilenameUtils.removeExtension(p1filePaths.get(i))));
//                        p1fileExtensions.add(i, p1filePaths.get(i).substring(p1filePaths.get(i).lastIndexOf(p1fileNames.get(i)) + p1fileNames.get(i).length() + 1));
//
//                    } else {
//
//                        p1fileNames.add(i, FilenameUtils.getBaseName(p1filePaths.get(i)));
//                        p1fileExtensions.add(i, p1filePaths.get(i).substring(p1filePaths.get(i).lastIndexOf(p1fileNames.get(i)) + p1fileNames.get(i).length() + 1));
//                    }
//
//                }//for
//
//                //PAIR 2
//                for (int i = 0; i < readFilesInPair2.getModel().getSize(); i++) {
//                    p2filePaths.add(i, readFilesInPair2.getModel().getElementAt(i).replaceAll("\"", "").trim());
//
//                    if ((p2filePaths.get(i).endsWith(".gz")) || (p2filePaths.get(i).endsWith(".bz2"))) {
//
//                        p2fileNames.add(i, FilenameUtils.getBaseName(FilenameUtils.removeExtension(p2filePaths.get(i))));
//                        p2fileExtensions.add(i, p2filePaths.get(i).substring(p2filePaths.get(i).lastIndexOf(p2fileNames.get(i)) + p2fileNames.get(i).length() + 1));
//
//                    } else {
//
//                        p2fileNames.add(i, FilenameUtils.getBaseName(p2filePaths.get(i)));
//                        p2fileExtensions.add(i, p2filePaths.get(i).substring(p2filePaths.get(i).lastIndexOf(p2fileNames.get(i)) + p2fileNames.get(i).length() + 1));
//                    }
//
//                }//for
//
//                addComponents(listModelreadPair2.size());
//            }//endif
        } else if (inputOptions.getSelectedIndex() == 2) {

            addComponents(listModel.size());

        }

//END READ INPUT FILES
    }

    public void display(String title, String value) {

        System.out.println("===========================================");
        System.out.println("=============== " + title.toUpperCase() + " ===================");
        System.out.println("===========================================");
        System.out.println(value);
        System.out.println("===========================================");
    }

    /////////////////////////////////////////////
    // Function for generating Dynamic Components
    /////////////////////////////////////////////

    public void addComponents(int numEntries) {

        addReplaceRGPanel.removeAll();

        // Clear the existing components
        labels.clear();
        textFields.clear();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Add new components based on the number of entries in the JList
        for (int i = 0; i < numEntries; i++) {

            JLabel label1 = new JLabel("ID:");
            JTextField textField1 = new JTextField(5);
            textField1.setText("" + (i + 1));
            label1.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
            label1.setForeground(new java.awt.Color(255, 255, 255));

            labels.add(label1);
            textFields.add(textField1);

            JLabel label2 = new JLabel("LB:");
            JTextField textField2 = new JTextField(5);
            textField2.setText("lib" + (i + 1));
            label2.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
            label2.setForeground(new java.awt.Color(255, 255, 255));

            labels.add(label2);
            textFields.add(textField2);

            JLabel label3 = new JLabel("PL:");
            JTextField textField3 = new JTextField(10);
            textField3.setText("ILLUMINA");
            label3.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
            label3.setForeground(new java.awt.Color(255, 255, 255));

            labels.add(label3);
            textFields.add(textField3);

            JLabel label4 = new JLabel("PU:");
            JTextField textField4 = new JTextField(5);
            textField4.setText("unit" + (i + 1));
            label4.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
            label4.setForeground(new java.awt.Color(255, 255, 255));

            labels.add(label4);
            textFields.add(textField4);

            JLabel label5 = new JLabel("SM:");
            JTextField textField5 = new JTextField(20);
            //Reading just file name from the list
            if (inputOptions.getSelectedIndex() == 1) {
                if (singleRadioButton.isSelected()) {
                    textField5.setText(sfileNames.get(i));
                } else {
                    textField5.setText(p1p2fileNames.get(i));
                }
            } else if (inputOptions.getSelectedIndex() == 2) {
                //Reading just file name from the list
                // Path path = Paths.get(inputBamTxt.getModel().getElementAt(i));
                Path path = Paths.get(sfileNames.get(i));
                textField5.setText(sfileNames.get(i));
                //textField5.setText(StringUtils.chop(path.getFileName().toString().replaceAll("\\.(?![^.]+$)", "")));
            }
            label5.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
            label5.setForeground(new java.awt.Color(255, 255, 255));

            labels.add(label5);
            textFields.add(textField5);

            addReplaceRGPanel.add(label1, gbc);
            gbc.gridx++;
            addReplaceRGPanel.add(textField1, gbc);
            gbc.gridx++;

            addReplaceRGPanel.add(label2, gbc);
            gbc.gridx++;
            addReplaceRGPanel.add(textField2, gbc);
            gbc.gridx++;

            addReplaceRGPanel.add(label3, gbc);
            gbc.gridx++;
            addReplaceRGPanel.add(textField3, gbc);
            gbc.gridx++;

            addReplaceRGPanel.add(label4, gbc);
            gbc.gridx++;
            addReplaceRGPanel.add(textField4, gbc);
            gbc.gridx++;

            addReplaceRGPanel.add(label5, gbc);
            gbc.gridx++;
            addReplaceRGPanel.add(textField5, gbc);
            gbc.gridx++;

            gbc.gridy++;

            gbc.gridx = 0;

        }//for

        // Repaint the UI to reflect the changes
        addReplaceRGPanel.revalidate();
        addReplaceRGPanel.repaint();

    

    /////////////////////////////////////////////////////////

    }

    public String retrieveData() {
        String value = "";
        for (int i = 0; i < textFields.size(); i++) {

            JTextField textField = textFields.get(i);
            value = value + textField.getText() + ",";
        }//end for

        return value;
    }

    public String retrieveData(int pos) {
        String value = "\'@RG";
        //pos 0*5+0=0,0*5+1=1,0*5+2=2,0*5+3=3,0*5+4=4
        //pos 1*5+0=5,1*5+1=6,1*5+2=7,1*5+3=8,1*5+4=9
        for (int i = 0; i < 5; i++) {
            JLabel label = labels.get((pos * 5) + i);
            JTextField textField = textFields.get((pos * 5) + i);

            value = value + "\\\\t" + label.getText() + textField.getText();

        }//end for
        value = value + "\'";
        return value;
    }

    ///////////////////////////////////////////////////////////////////////////
    public void runFastP(String selectedKey) {
        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress: Running Fastp...", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

            @Override
            protected Integer doInBackground() {
                try {
                    pipelineError = false;
                    ++fastCounter;
                    ProcessBuilder builder = new ProcessBuilder();
                    List<String> cmdList = new ArrayList<String>();
                    cmdList.add("sh");
                    cmdList.add(System.getProperty("user.dir").concat("/bin/fastp.sh"));
                    cmdList.add(App.FastP_PATH); //$1
                    cmdList.add(App.MULTIQC_PATH); //2
                    String myArray = "";

                    if (singleRadioButton.isSelected()) {
                        cmdList.add("SINGLE");//2

                        //Read List Data
                        List<String> paths = singlePathMap.getOrDefault(selectedKey, List.of());
                        int i = 0;
                        for (String path : paths) {
                            myArray += (path + ",");
                            filesForComboP.add("FastP" + fastCounter + "/" + sfileNames.get(i) + ".html");
                            i++;
                        }

//                        for (int i = 0; i < readFilesInSingle.getModel().getSize(); i++) {
//                            myArray += (readFilesInSingle.getModel().getElementAt(i) + ",");
//                            filesForComboP.add(sfileNames.get(i) + ".html");
//                        }//endFor
                        myArray = StringUtils.chop(myArray).replaceAll("\"", "");
                        myArray = myArray.trim();
                        cmdList.add(myArray); //3

                    } else if (pairRadioButton.isSelected()) {
                        cmdList.add("PAIR");//2

                        Map<String, List<String>> innerMap = pairPathMap.getOrDefault(selectedKey, Map.of());

                        List<String> r1List = innerMap.getOrDefault("R1", List.of());
                        List<String> r2List = innerMap.getOrDefault("R2", List.of());

                        for (int i = 0; i < r1List.size(); i++) {
                            myArray += (r1List.get(i).trim() + "#" + r2List.get(i).trim() + ",");
                            filesForComboP.add("FastP" + fastCounter + "/" + p1p2fileNames.get(i) + ".html");

                        }//for
                        myArray = StringUtils.chop(myArray).replaceAll("\"", "");
                        myArray = myArray.trim();
                        cmdList.add(myArray); //3

//                        if (readFilesInPair1.getModel().getSize() == readFilesInPair2.getModel().getSize()) {
//                            for (int i = 0; i < readFilesInPair1.getModel().getSize(); i++) {
//                                myArray += (readFilesInPair1.getModel().getElementAt(i) + "#" + readFilesInPair2.getModel().getElementAt(i) + ",");
//                                filesForComboP.add(p1p2fileNames.get(i) + ".html");
//                               
//                            }//for
//                            myArray = StringUtils.chop(myArray).replaceAll("\"", "");
//                            myArray = myArray.trim();
//                            cmdList.add(myArray); //3
//                            
//                        }
                    }//endif

                    cmdList.add(output_Dir.getText());//4
                    cmdList.add(fastCounter + "");//5

                    if (fastpTrimCombo.getSelectedIndex() == 1) {
                        cmdList.add(fastpTxtArea.getText());
                    } else {
                        cmdList.add("");//6
                    }

                    builder.command(cmdList);

                    builder.redirectErrorStream(true);

                    Process process = builder.start();

                    InputStream is = process.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line = null;
                    while ((line = br.readLine()) != null && (!isCancelled())) {
                        App.LOGGER.info(line + "\r\n");
                        publish(line);
                        if (line.contains("ERROR: igzip: unexpected eof") || line.contains("Fastp failed")
                                || line.contains("MultiQC failed!")) {
                            pipelineError = true;
                            App.LOGGER.error("Pipeline error detected: " + line);
                            break;
                        }
                    }//endWhile

                    if (!isCancelled()) {
                        status = process.waitFor();
                    }
                    process.destroy();

                } catch (IOException | InterruptedException ex) { //
                    ex.printStackTrace(new PrintWriter(App.stack));
                    App.LOGGER.error("\r\n ERROR in FastQC analysis: " + App.stack);
                    App.textArea.append("\r\n ERROR in FastQC analysis: " + App.stack);
                    pipelineError = true;

                }//catch

                return status;
            }//doInBackground

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

                if (!pipelineError) {

                    ////SETTING COMBO BOX HTMLS
                    fastpResultCombo.setModel(new DefaultComboBoxModel<String>(filesForComboP.toArray(new String[0])));
                    fastpResultCombo.setEnabled(true);
                    if (fastpTrimCombo.getSelectedIndex() == 0) {
                        nextBtn.setText("Proceed to Run Pipeline");
                    } else if (fastpTrimCombo.getSelectedIndex() == 1) {
                        nextBtn.setText("Continue Trimming & QC Refinement");
                    }
                    inputPanel.setVisible(false);
                    QualityControlPanel.setVisible(true);
                    fastpPanel.setVisible(true);

                    //Changing input lists path to fastp paths
                    if (singleRadioButton.isSelected()) {
                        List<String> pathList = new ArrayList<>();
                        for (int i = 0; i < sfileNames.size(); i++) {
                            pathList.add(" \"" + output_Dir.getText() + "FastP" + fastCounter + "/" + sfileNames.get(i) + "." + sfileExtensions.get(i) + "\"");

                        }//for
                        singlePathMap.put("FastP" + fastCounter, pathList);
                    } else if (pairRadioButton.isSelected()) {
                        Map<String, List<String>> pairReads = new HashMap<>();
                        List<String> read1List = new ArrayList<>();
                        for (int i = 0; i < p1fileNames.size(); i++) {

                            read1List.add(" \"" + output_Dir.getText() + "FastP" + fastCounter + "/" + p1fileNames.get(i) + "." + p1fileExtensions.get(i) + "\"");

                        }//for
                        List<String> read2List = new ArrayList<>();
                        for (int i = 0; i < p2fileNames.size(); i++) {
                            read2List.add(" \"" + output_Dir.getText() + "FastP" + fastCounter + "/" + p2fileNames.get(i) + "." + p2fileExtensions.get(i) + "\"");

                        }//for

                        pairReads.put("R1", read1List);
                        pairReads.put("R2", read2List);

                        pairPathMap.put("FastP" + fastCounter, pairReads);
                    }//if

//                    if (singleRadioButton.isSelected()) {
//                        listModelreadSingle.clear();
//                        for (int i = 0; i < sfileNames.size(); i++) {
//                            listModelreadSingle.addElement(" \"" +output_Dir.getText() + sfileNames.get(i) + "." + sfileExtensions.get(i)+"\"" );
//
//                        }//for
//
//                    } else if (pairRadioButton.isSelected()) {
//                        listModelreadPair1.clear();
//                        listModelreadPair2.clear();
//                        for (int i = 0; i < p1fileNames.size(); i++) {
//
//                            listModelreadPair1.addElement(" \"" +output_Dir.getText() + p1fileNames.get(i) + "." + p1fileExtensions.get(i)+"\"" );
//
//                        }//for
//                        for (int i = 0; i < p2fileNames.size(); i++) {
//                            listModelreadPair2.addElement(" \"" +output_Dir.getText() + p2fileNames.get(i) + "." + p2fileExtensions.get(i)+"\"" );
//
//                        }//for
//
//                    }//if
                }//error
            }//end Done

        };

        worker.execute();

    }//end runFastqc

    String[] readData(String filename) {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            List<String> cmdList = new ArrayList<String>();
            cmdList.add("sh");
            cmdList.add(System.getProperty("user.dir").concat("/bin/fastqc.sh"));
            cmdList.add("Step2");//1

            cmdList.add(output_Dir.getText() + filename);//2

            builder.command(cmdList);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            InputStream is = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            String[] samplerow = new String[11];

            boolean hasIssues = false;

            int counter = 1;

            while ((line = br.readLine()) != null) {
                App.LOGGER.info(line + "\r\n");

                String[] parts = line.split("\t");
                if (counter == 1) {
                    samplerow[0] = parts[2];
                }
                String recommendation = null;
                switch (parts[1]) {

                    case "Basic Statistics":
                        samplerow[1] = parts[0];
                        counter++;
                        break;
                    case "Per base sequence quality":
                        samplerow[2] = parts[0];
                        if (!parts[0].equalsIgnoreCase("PASS")) {
                            recommendation = "<html><b>Quality dropped at END?</b> Consider Trimmomatic or Cutadapt.<br>"
                                    + "<b>Middle?</b> Check machine logs.<br>"
                                    + "<b>Overall LOW?</b> Use quality filtering.</html>";
                        }
                        counter++;
                        break;
                    case "Per sequence quality scores":
                        samplerow[3] = parts[0];
                        if (!parts[0].equalsIgnoreCase("PASS")) {
                            recommendation = "<html>Use Trimmomatic or Cutadapt for trimming low-quality bases.</html>";
                        }
                        counter++;
                        break;
                    case "Per base sequence content":
                        samplerow[4] = parts[0];
                        if (!parts[0].equalsIgnoreCase("PASS")) {
                            recommendation = "<html><b>First few bases biased?</b>Use Cutadapt."
                                    + "<br><b>Large fluctuations?</b>Possible adapter contamination or bias.<br>"
                                    + "</html>";
                        }
                        counter++;
                        break;
                    case "Per sequence GC content":
                        samplerow[5] = parts[0];
                        if (!parts[0].equalsIgnoreCase("PASS")) {
                            recommendation = "<html><b>Multiple peaks?</b> Possible contamination.<br>"
                                    + "<b>Shifted distribution?</b> Check reference genome.</html>";
                        }
                        counter++;
                        break;
                    case "Per base N content":
                        samplerow[6] = parts[0];
                        if (!parts[0].equalsIgnoreCase("PASS")) {
                            recommendation = "<html><b>High N content detected;</b> Consider quality filtering.</html>";
                        }
                        counter++;
                        break;
                    case "Sequence Length Distribution":
                        samplerow[7] = parts[0];
                        if (!parts[0].equalsIgnoreCase("PASS")) {
                            recommendation = "<html><b>Unexpected variability?</b> Check for trimming.<br>"
                                    + "<b>Too many short reads?</b> Consider resequencing.</html>";
                        }
                        counter++;
                        break;
                    case "Sequence Duplication Levels":
                        samplerow[8] = parts[0];
                        if (!parts[0].equalsIgnoreCase("PASS")) {
                            recommendation = "<html><b>High duplication detected;</b> consider deduplication.</html>";
                        }
                        counter++;
                        break;
                    case "Overrepresented sequences":
                        samplerow[9] = parts[0];
                        if (!parts[0].equalsIgnoreCase("PASS")) {
                            recommendation = "<html><b>Overrepresented sequences found;</b> check for contamination.</html>";
                        }
                        counter++;
                        break;
                    case "Adapter Content":
                        samplerow[10] = parts[0];
                        if (!parts[0].equalsIgnoreCase("PASS")) {
                            recommendation = "<html><b>Adapters detected;</b> use Cutadapt for trimming.</html>";
                        }
                        counter++;
                        break;
                }

                // If there's a recommendation, store it in the tooltip map
                if (recommendation != null) {
                    tooltips.put(counter - 1, recommendation);
                }
            }

            process.destroy();
            return samplerow;
        } catch (IOException ex) {
            App.LOGGER.error("Error loading FastQC: " + ex);

        }
        return null;
    }

    public void runFastqc(String selectedKey) {
        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress: Running FastQC ...", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

            @Override
            protected Integer doInBackground() {
                try {
                    pipelineError = false;
                    ProcessBuilder builder = new ProcessBuilder();
                    List<String> cmdList = new ArrayList<String>();
                    cmdList.add("sh");
                    cmdList.add(System.getProperty("user.dir").concat("/bin/fastqc.sh"));
                    cmdList.add("Step1");//1
                    cmdList.add(App.FastQC_PATH); //$2
                    String myArray = "";
                    filesForCombo = new ArrayList<>();
                    if (singleRadioButton.isSelected()) {

                        List<String> paths = singlePathMap.getOrDefault(selectedKey, List.of());

                        for (int i = 0; i < paths.size(); i++) {

                            myArray += (paths.get(i) + " ");
                            filesForCombo.add("FastQC" + fastCounter + "/" + sfileNames.get(i) + "_fastqc.html");

                        }//endFor

                        myArray = StringUtils.chop(myArray).replaceAll("\"", "");
                        myArray = myArray.trim();
                        cmdList.add(myArray); //3
                    } else if (pairRadioButton.isSelected()) {

                        Map<String, List<String>> innerMap = pairPathMap.getOrDefault(selectedKey, Map.of());
                        List<String> r1List = innerMap.getOrDefault("R1", List.of());
                        List<String> r2List = innerMap.getOrDefault("R2", List.of());

                        for (int i = 0; i < r1List.size(); i++) {

                            myArray += (r1List.get(i) + " " + r2List.get(i) + " ");
                            filesForCombo.add("FastQC" + fastCounter + "/" + p1fileNames.get(i) + "_fastqc.html");
                            filesForCombo.add("FastQC" + fastCounter + "/" + p2fileNames.get(i) + "_fastqc.html");

                        }//for

                        myArray = StringUtils.chop(myArray).replaceAll("\"", "");
                        myArray = myArray.trim();

                        cmdList.add(myArray); //3

                    }//endif

                    cmdList.add(output_Dir.getText() + "FastQC" + fastCounter);//4

                    builder.command(cmdList);

                    builder.redirectErrorStream(true);

                    Process process = builder.start();

                    InputStream is = process.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line = null;
                    while ((line = br.readLine()) != null && (!isCancelled())) {
                        App.LOGGER.info(line + "\r\n");
                        publish(line);

                    }//endWhile
                    ////SETTING COMBO BOX HTMLS

                    fastqcResultCombo.setModel(new DefaultComboBoxModel<String>(filesForCombo.toArray(new String[0])));
                    fastqcResultCombo.setEnabled(true);

                    if (!isCancelled()) {
                        status = process.waitFor();
                    }
                    process.destroy();

//FASTQC Analysis DONE
//READING SUMMARY DATA from files
                    dm.setRowCount(0);
                    if (singleRadioButton.isSelected()) {
                        List<String> paths = singlePathMap.getOrDefault(selectedKey, List.of());
                        for (int i = 0; i < paths.size(); i++) {
                            dm.addRow(readData("FastQC" + fastCounter + "/" + sfileNames.get(i) + "_fastqc.zip"));
                        }
                    } else if (pairRadioButton.isSelected()) {
                        Map<String, List<String>> innerMap = pairPathMap.getOrDefault(selectedKey, Map.of());
                        List<String> r1List = innerMap.getOrDefault("R1", List.of());

                        for (int i = 0; i < r1List.size(); i++) {

                            dm.addRow(readData("FastQC" + fastCounter + "/" + p1fileNames.get(i) + "_fastqc.zip"));
                            dm.addRow(readData("FastQC" + fastCounter + "/" + p2fileNames.get(i) + "_fastqc.zip"));

                        }//for

                    }

                } catch (IOException | InterruptedException ex) { //
                    ex.printStackTrace(new PrintWriter(App.stack));
                    App.LOGGER.error("\r\n ERROR in FastQC analysis: " + App.stack);
                    App.textArea.append("\r\n ERROR in FastQC analysis: " + App.stack);
                    pipelineError = true;
                    nextBtn.setEnabled(true);

                }//catch

                return status;
            }//doInBackground

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

                nextBtn.setEnabled(true);

                if (!pipelineError) {

                    inputPanel.setVisible(false);
                    QualityControlPanel.setVisible(true);
                    fastqcPanel.setVisible(true);

                    if (fastqcTrimCombo.getSelectedIndex() == 0) {
                        nextBtn.setText("Proceed to Run Pipeline");
                    } else {
                        nextBtn.setText("Continue Trimming & QC Refinement");
                    }

                }

            }//end Done

        };

        worker.execute();

    }//end runFastqc
//

    public void runTrimommatic(String selectedKey) {

        warningLabel.setText("");
        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress: Running Trimmomatic...", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));

        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {

            private int status;

            @Override
            protected Integer doInBackground() {

                try {
                    pipelineError = false;
                    ++fastCounter;
//FASTQC Windows :Dir_with_FastQC>java -Xmx250m -classpath .;./sam-1.103.jar;./jbzip2-0.9.jar uk.ac.babraham.FastQC.FastQCApplication ju.fq
                    ProcessBuilder builder = new ProcessBuilder();

                    List<String> cmdList = new ArrayList<String>();
                    // adding command and args to the list
                    cmdList.add("sh");
                    cmdList.add(System.getProperty("user.dir").concat("/bin/trimmomatic.sh"));
                    cmdList.add(App.Trimmomatic_PATH); //$1
                    String myArray = "";

                    if (singleRadioButton.isSelected()) {  //Comma seperated paths
                        cmdList.add("SINGLE"); //2
                        List<String> paths = singlePathMap.getOrDefault(selectedKey, List.of());

                        for (int i = 0; i < paths.size(); i++) {
                            myArray += (paths.get(i) + ",");

                        }//endFor

                        myArray = StringUtils.chop(myArray).replaceAll("\"", "");
                        myArray = myArray.trim();
                        cmdList.add(myArray); //3
                    } else if (pairRadioButton.isSelected()) { //Comma seperated paths, Hash seperated pairs
                        cmdList.add("PAIR");//2

                        Map<String, List<String>> innerMap = pairPathMap.getOrDefault(selectedKey, Map.of());
                        List<String> r1List = innerMap.getOrDefault("R1", List.of());
                        List<String> r2List = innerMap.getOrDefault("R2", List.of());

                        for (int i = 0; i < r1List.size(); i++) {

                            myArray += (r1List.get(i).trim() + "#" + r2List.get(i).trim() + ",");

                        }//for

                        myArray = StringUtils.chop(myArray).replaceAll("\"", "");
                        myArray = myArray.trim();

                        cmdList.add(myArray); //3

                    }//endif

                    cmdList.add(output_Dir.getText() + "FastQC" + fastCounter + "/");//4

                    cmdList.add(phredEncodingCombo.getSelectedItem().toString());//5 phred
                    cmdList.add((logC.isSelected() ? "YES" : "NO"));//6
                    cmdList.add(adapterfile.getText());//7
                    cmdList.add(illumniaclip.getText());//8
                    cmdList.add((slidingC.isSelected() ? sliding.getText() : ""));//9
                    cmdList.add((leadingC.isSelected() ? leading.getText() : ""));//10
                    cmdList.add((trailingC.isSelected() ? trailing.getText() : ""));//11
                    cmdList.add((minlenC.isSelected() ? minlen.getText() : ""));//12
                    cmdList.add((threadC.isSelected() ? thread.getText() : ""));//13
                    cmdList.add((maxinfoC.isSelected() ? maxinfo.getText() : ""));//14
                    cmdList.add((cropC.isSelected() ? crop.getText() : ""));//15
                    cmdList.add((headcropC.isSelected() ? headcrop.getText() : ""));//16

                    builder.command(cmdList);

                    builder.redirectErrorStream(true);

                    Process process = builder.start();

                    InputStream is = process.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line = null;

                    while ((line = br.readLine()) != null && !isCancelled()) {
                        App.LOGGER.info(line + "\r\n");
                        publish(line);

                        //ERROR CHECK
                        if (line.toLowerCase().contains("exception") || line.toLowerCase().contains("error")) {
                            pipelineError = true;
                            break;
                        }

                    }

                    if (!isCancelled()) {
                        status = process.waitFor();
                    }

                    process.destroy();
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace(new PrintWriter(App.stack));
                    App.LOGGER.error("\r\n ERROR in TRIMMOMATIC analysis: " + App.stack);
                    App.textArea.append("\r\n ERROR in TRIMMOMATIC analysis: " + App.stack);
                    pipelineError = true;
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
                if (!pipelineError) {
                    //Changing input lists path to trimmomatic paths

                    if (singleRadioButton.isSelected()) {
                        List<String> pathList = new ArrayList<>();

                        for (int i = 0; i < sfileNames.size(); i++) {

                            String filename = sfileNames.get(i);

                            pathList.add(" \"" + output_Dir.getText() + "FastQC" + fastCounter + "/" + filename + "." + sfileExtensions.get(i));

                        }//for
                        singlePathMap.put("FastQC" + fastCounter, pathList);
                        inputFilesDataS(singlePathMap, "FastQC" + fastCounter);

                    } else if (pairRadioButton.isSelected()) {

                        Map<String, List<String>> pairReads = new HashMap<>();
                        List<String> read1List = new ArrayList<>();

                        for (int i = 0; i < p1fileNames.size(); i++) {
                            String filename = p1fileNames.get(i);

                            System.out.println(" \"" + output_Dir.getText() + "FastQC" + fastCounter + "/" + filename + "." + p1fileExtensions.get(i));

                            read1List.add(" \"" + output_Dir.getText() + "FastQC" + fastCounter + "/" + filename + "." + p1fileExtensions.get(i));
                        }//for
                        List<String> read2List = new ArrayList<>();
                        for (int i = 0; i < p2fileNames.size(); i++) {
                            String filename = p2fileNames.get(i);
                            read2List.add(" \"" + output_Dir.getText() + "FastQC" + fastCounter + "/" + filename + "." + p2fileExtensions.get(i));
                        }//for

                        pairReads.put("R1", read1List);
                        pairReads.put("R2", read2List);
                        pairPathMap.put("FastQC" + fastCounter, pairReads);
                        inputFilesDataP(pairPathMap, "FastQC" + fastCounter);

                    }//endif

                    //RERUN INPUT FUNCTION TO RESET ALL FILES NAME, EXTENSION, PATH
                    //  fastqcTrimCombo.setSelectedIndex(0);
                    runFastqc("FastQC" + fastCounter);
                }
            }//DONE

        };

        worker.execute();

    }//end Trimmomatic

    //////////////////////////////////////////////////////////////////////////
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        readBG = new javax.swing.ButtonGroup();
        haplotypeGroup = new javax.swing.ButtonGroup();
        variantAnnotationBG = new javax.swing.ButtonGroup();
        qualityCheckBG = new javax.swing.ButtonGroup();
        haploIntervalBG = new javax.swing.ButtonGroup();
        jScrollPane2 = new javax.swing.JScrollPane();
        MainPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgLoginMain, 0, 0, null);
            }
        }

        ;
        upPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgInput, 0, 0, null);
            }
        }

        ;
        jLabelHelp = new javax.swing.JLabel();
        warningLabel = new javax.swing.JLabel();
        nextBtn = new javax.swing.JButton();
        inputPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgInput, 0, 0, null);
            }
        }

        ;
        jLabel100 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        output_Dir = new javax.swing.JTextField();
        outputBtn = new javax.swing.JButton();
        byFastq = new javax.swing.JPanel();
        jButton8 = new javax.swing.JButton();
        singleRadioButton = new javax.swing.JRadioButton();
        pairRadioButton = new javax.swing.JRadioButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        readFilesInPair1 = new javax.swing.JList<>(listModelreadPair1);
        jScrollPane8 = new javax.swing.JScrollPane();
        readFilesInSingle = new javax.swing.JList<>(listModelreadSingle);
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jScrollPane9 = new javax.swing.JScrollPane();
        readFilesInPair2 = new javax.swing.JList<>(listModelreadPair2);
        byBam = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        inputBamTxt = new javax.swing.JList<>(listModel);
        inputbamBtn = new javax.swing.JButton();
        jLabel92 = new javax.swing.JLabel();
        inputOptions = new javax.swing.JComboBox<>();
        jLabel33 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        fastqcRadio = new javax.swing.JRadioButton();
        fastpRadio = new javax.swing.JRadioButton();
        noneRadio = new javax.swing.JRadioButton();
        QualityControlPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgInput, 0, 0, null);
            }
        }
        ;
        back1 = new javax.swing.JButton();
        fastqcPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgInput, 0, 0, null);
            }
        }

        ;
        jScrollPane15 = new javax.swing.JScrollPane();
        fastqcTable = new javax.swing.JTable(dm){
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                //Component c = super.prepareRenderer(renderer, row, column);
                JLabel c = (JLabel)super.prepareRenderer(renderer, row, column);
                Object obj = getModel().getValueAt(row, column);
                if(obj!=null){
                    if (obj.toString().equalsIgnoreCase("FAIL")) {
                        c.setForeground(Color.RED);

                    }else if (obj.toString().equalsIgnoreCase("WARN")) {
                        c.setForeground(Color.ORANGE);

                    }
                    else if (obj.toString().equalsIgnoreCase("PASS")) {
                        c.setForeground(Color.BLUE);

                    }
                    else {
                        c.setForeground(Color.BLACK);
                    }
                }//outerIF
                return c;
            }

        };
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        fastqcResultCombo = new javax.swing.JComboBox<>();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        fastqcTrimCombo = new javax.swing.JComboBox<>();
        trimPanel = new javax.swing.JPanel(){

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgInput, 0, 0, null);
            }
        }

        ;
        jLabel104 = new javax.swing.JLabel();
        jLabel105 = new javax.swing.JLabel();
        jLabel106 = new javax.swing.JLabel();
        jLabel107 = new javax.swing.JLabel();
        phredEncodingCombo = new javax.swing.JComboBox<>();
        adapterfile = new javax.swing.JTextField();
        sliding = new javax.swing.JTextField();
        leading = new javax.swing.JTextField();
        trailing = new javax.swing.JTextField();
        minlen = new javax.swing.JTextField();
        illumniaclip = new javax.swing.JTextField();
        maxinfo = new javax.swing.JTextField();
        crop = new javax.swing.JTextField();
        headcrop = new javax.swing.JTextField();
        thread1 = new javax.swing.JTextField();
        slidingC = new javax.swing.JCheckBox();
        jLabel108 = new javax.swing.JLabel();
        browseGTFBtn2 = new javax.swing.JButton();
        headcropC = new javax.swing.JCheckBox();
        threadC = new javax.swing.JCheckBox();
        maxinfoC = new javax.swing.JCheckBox();
        trailingC = new javax.swing.JCheckBox();
        leadingC = new javax.swing.JCheckBox();
        minlenC = new javax.swing.JCheckBox();
        logC = new javax.swing.JCheckBox();
        cropC = new javax.swing.JCheckBox();
        fastpPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgInput, 0, 0, null);
            }
        }

        ;
        jLabel51 = new javax.swing.JLabel();
        fastpResultCombo = new javax.swing.JComboBox<>();
        jLabel77 = new javax.swing.JLabel();
        fastpTrimCombo = new javax.swing.JComboBox<>();
        jLabel78 = new javax.swing.JLabel();
        fastPparameter = new javax.swing.JPanel();
        jScrollPane16 = new javax.swing.JScrollPane();
        fastpTxtArea = new javax.swing.JTextArea();
        jLabel50 = new javax.swing.JLabel();
        jLabel79 = new javax.swing.JLabel();
        jLabel80 = new javax.swing.JLabel();
        analysisPanel = new javax.swing.JPanel();
        jLabel93 = new javax.swing.JLabel();
        splitNcigar = new javax.swing.JCheckBox();
        jLabel94 = new javax.swing.JLabel();
        germline = new javax.swing.JCheckBox();
        somatic = new javax.swing.JCheckBox();
        jLabel95 = new javax.swing.JLabel();
        refTxt = new javax.swing.JTextField();
        refBtn = new javax.swing.JButton();
        jLabel96 = new javax.swing.JLabel();
        jLabel97 = new javax.swing.JLabel();
        mutect2Panel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        mutectCombo = new javax.swing.JComboBox<>();
        normalPanel = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        tumNorBtn = new javax.swing.JButton();
        norTumBtn = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        jListNormal = new javax.swing.JList<>(listNormal);
        jScrollPane14 = new javax.swing.JScrollPane();
        jListTumor = new javax.swing.JList<>(listTumor);
        jLabel29 = new javax.swing.JLabel();
        mainLabelWarning = new javax.swing.JLabel();
        Label1 = new javax.swing.JLabel();
        Label2 = new javax.swing.JLabel();
        Label3 = new javax.swing.JLabel();
        Label4 = new javax.swing.JLabel();
        Label5 = new javax.swing.JLabel();
        Label6 = new javax.swing.JLabel();
        otherPanel = new javax.swing.JPanel();
        germlineResTxt = new javax.swing.JTextField();
        ponTxt = new javax.swing.JTextField();
        alleleTxt = new javax.swing.JTextField();
        f1r2 = new javax.swing.JTextField();
        germlineResBtn = new javax.swing.JButton();
        ponBtn = new javax.swing.JButton();
        alleleBtn = new javax.swing.JButton();
        intervalBtn = new javax.swing.JButton();
        f1r2Btn = new javax.swing.JButton();
        jScrollPane12 = new javax.swing.JScrollPane();
        intervalText = new javax.swing.JTextArea();
        intervalT = new javax.swing.JTextField();
        add6 = new javax.swing.JButton();
        del6 = new javax.swing.JButton();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        ponLabel2 = new javax.swing.JLabel();
        generatePON = new javax.swing.JCheckBox();
        ponLabel3 = new javax.swing.JLabel();
        ponLabel4 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        germlineCheck = new javax.swing.JCheckBox();
        ponCheck = new javax.swing.JCheckBox();
        allelesCheck = new javax.swing.JCheckBox();
        f1r2Check = new javax.swing.JCheckBox();
        intervalCheck = new javax.swing.JCheckBox();
        germlinePanel = new javax.swing.JPanel();
        jLabel24 = new javax.swing.JLabel();
        bamoutTxt = new javax.swing.JTextField();
        bamoutB = new javax.swing.JButton();
        ercCombo = new javax.swing.JComboBox<>();
        g1Check = new javax.swing.JCheckBox();
        bamoutCheck = new javax.swing.JCheckBox();
        singleModeH = new javax.swing.JRadioButton();
        jointModeH = new javax.swing.JRadioButton();
        jScrollPane11 = new javax.swing.JScrollPane();
        annotationGroupTxt = new javax.swing.JTextArea();
        add7 = new javax.swing.JButton();
        del7 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        annotationGroupCombo = new javax.swing.JComboBox<>(items);
        genomicDBPanel = new javax.swing.JPanel();
        jLabel46 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        intervalT1 = new javax.swing.JTextField();
        jLabel49 = new javax.swing.JLabel();
        jScrollPane17 = new javax.swing.JScrollPane();
        intervalText1 = new javax.swing.JTextArea();
        intervalBtn2 = new javax.swing.JButton();
        add9 = new javax.swing.JButton();
        del9 = new javax.swing.JButton();
        intervalT2 = new javax.swing.JTextField();
        jLabel56 = new javax.swing.JLabel();
        jLabel57 = new javax.swing.JLabel();
        uploadIntervalfileR = new javax.swing.JRadioButton();
        createIntervalR = new javax.swing.JRadioButton();
        knownSNPBtn = new javax.swing.JButton();
        jLabel99 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        addReplaceRGPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgLoginMain, 0, 0, null);
            }
        };
        bwaPanel = new javax.swing.JPanel();
        bwaM = new javax.swing.JCheckBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        knownSites = new javax.swing.JList<>(listKnownSites);
        jLabel98 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jLabel102 = new javax.swing.JLabel();
        jLabel103 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        FilterPanel = new javax.swing.JPanel();
        jCheckBox5 = new javax.swing.JCheckBox();
        vepAssembly = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jCheckBox8 = new javax.swing.JCheckBox();
        jScrollPane7 = new javax.swing.JScrollPane();
        selectVariantTxt = new javax.swing.JTextArea();
        snpEff_DB = new javax.swing.JTextField();
        jScrollPane10 = new javax.swing.JScrollPane();
        variantFilterTxtB = new javax.swing.JTextArea();
        jLabel12 = new javax.swing.JLabel();
        del3 = new javax.swing.JButton();
        add1 = new javax.swing.JButton();
        selectTypeCombo = new javax.swing.JComboBox<>();
        jCheckBox6 = new javax.swing.JCheckBox();
        jLabel40 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        vepRadio = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        snpEffvepRadio = new javax.swing.JRadioButton();
        add8 = new javax.swing.JButton();
        filterNameB = new javax.swing.JTextField();
        snpEffRadio = new javax.swing.JRadioButton();
        del1 = new javax.swing.JButton();
        selectGenotype = new javax.swing.JTextField();
        jLabel43 = new javax.swing.JLabel();
        vepSpecies = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        Linterval = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        selectVariantsOp = new javax.swing.JCheckBox();
        select = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        filterExprB = new javax.swing.JTextField();
        add2 = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jCheckBox7 = new javax.swing.JCheckBox();
        del5 = new javax.swing.JButton();
        add3 = new javax.swing.JButton();
        intervalBtn1 = new javax.swing.JButton();
        jLabel26 = new javax.swing.JLabel();
        jScrollPane13 = new javax.swing.JScrollPane();
        variantFilterTxtA = new javax.swing.JTextArea();
        snpSiftCombobox = new javax.swing.JComboBox<>(snpSiftFilters);
        jLabel25 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        add4 = new javax.swing.JButton();
        jLabel35 = new javax.swing.JLabel();
        del2 = new javax.swing.JButton();
        del4 = new javax.swing.JButton();
        variantFilterA = new javax.swing.JCheckBox();
        add5 = new javax.swing.JButton();
        del8 = new javax.swing.JButton();
        jLabel36 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        excludeFiltered = new javax.swing.JCheckBox();
        restrictAlleles = new javax.swing.JCheckBox();
        restrictAlleleCombo = new javax.swing.JComboBox<>();
        add10 = new javax.swing.JButton();
        del10 = new javax.swing.JButton();
        jLabel32 = new javax.swing.JLabel();
        jLabel101 = new javax.swing.JLabel();
        thread = new javax.swing.JTextField();
        outputLabel = new javax.swing.JLabel();
        visualPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1Tree = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree(root);
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("VARIANT ANALYSIS PIPELINE (GERMLINE,SOMATIC)");

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        MainPanel.setBackground(new java.awt.Color(0, 0, 0));
        MainPanel.setPreferredSize(new java.awt.Dimension(600, 3460));
        MainPanel.setLayout(new org.jdesktop.swingx.VerticalLayout());

        upPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Purpose: Identify short variants (SNP and indels) in DNA or RNAseq data.", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N
        upPanel.setOpaque(false);

        jLabelHelp.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabelHelp.setForeground(new java.awt.Color(255, 255, 255));
        jLabelHelp.setText("Help?");
        jLabelHelp.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabelHelpMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabelHelpMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabelHelpMouseExited(evt);
            }
        });

        warningLabel.setFont(new java.awt.Font("Liberation Sans", 3, 14)); // NOI18N
        warningLabel.setForeground(new java.awt.Color(255, 0, 51));

        nextBtn.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        nextBtn.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/next1.png")); // NOI18N
        nextBtn.setText("Proceed to Run Pipeline");
        nextBtn.setBorderPainted(false);
        nextBtn.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        nextBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout upPanelLayout = new javax.swing.GroupLayout(upPanel);
        upPanel.setLayout(upPanelLayout);
        upPanelLayout.setHorizontalGroup(
            upPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upPanelLayout.createSequentialGroup()
                .addGap(487, 487, 487)
                .addComponent(nextBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(475, 475, 475)
                .addComponent(jLabelHelp)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(upPanelLayout.createSequentialGroup()
                .addComponent(warningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 1482, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        upPanelLayout.setVerticalGroup(
            upPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upPanelLayout.createSequentialGroup()
                .addGroup(upPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelHelp)
                    .addGroup(upPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(nextBtn)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(warningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        MainPanel.add(upPanel);

        inputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "INPUT OPTIONS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 14), new java.awt.Color(255, 255, 255))); // NOI18N
        inputPanel.setOpaque(false);
        inputPanel.setPreferredSize(new java.awt.Dimension(1000, 660));

        jLabel100.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel100.setForeground(new java.awt.Color(255, 255, 255));
        jLabel100.setText("Enter all the neccesary input files. NOTE: Path or Filename should not contain any spaces");

        jLabel2.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("***EXAMPLE DATA");
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });

        output_Dir.setEditable(false);

        outputBtn.setText("...");
        outputBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputBtnActionPerformed(evt);
            }
        });

        byFastq.setOpaque(false);
        byFastq.setPreferredSize(new java.awt.Dimension(1000, 408));

        jButton8.setText("...");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        singleRadioButton.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        singleRadioButton.setForeground(new java.awt.Color(255, 255, 255));
        singleRadioButton.setSelected(true);
        singleRadioButton.setText("readFilesIn (Single end reads)");
        singleRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                singleRadioButtonActionPerformed(evt);
            }
        });

        pairRadioButton.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        pairRadioButton.setForeground(new java.awt.Color(255, 255, 255));
        pairRadioButton.setText("readFilesIn (Paired end reads)");
        pairRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pairRadioButtonActionPerformed(evt);
            }
        });

        readFilesInPair1.setBorder(javax.swing.BorderFactory.createTitledBorder("All sample(s) read 1 only"));
        readFilesInPair1.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N
        readFilesInPair1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        readFilesInPair1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                readFilesInPair1KeyPressed(evt);
            }
        });
        jScrollPane5.setViewportView(readFilesInPair1);

        readFilesInSingle.setBorder(javax.swing.BorderFactory.createTitledBorder("All sample(s) single end reads"));
        readFilesInSingle.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N
        readFilesInSingle.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        readFilesInSingle.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                readFilesInSingleKeyPressed(evt);
            }
        });
        jScrollPane8.setViewportView(readFilesInSingle);

        jButton6.setText("...");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setText("...");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        readFilesInPair2.setBorder(javax.swing.BorderFactory.createTitledBorder("All sample(s) read 2 only"));
        readFilesInPair2.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N
        readFilesInPair2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                readFilesInPair2KeyPressed(evt);
            }
        });
        jScrollPane9.setViewportView(readFilesInPair2);

        javax.swing.GroupLayout byFastqLayout = new javax.swing.GroupLayout(byFastq);
        byFastq.setLayout(byFastqLayout);
        byFastqLayout.setHorizontalGroup(
            byFastqLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byFastqLayout.createSequentialGroup()
                .addGroup(byFastqLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(byFastqLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pairRadioButton))
                    .addGroup(byFastqLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(byFastqLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 449, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(byFastqLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 449, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8))
                    .addGroup(byFastqLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(singleRadioButton)))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        byFastqLayout.setVerticalGroup(
            byFastqLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, byFastqLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(singleRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(byFastqLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pairRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(byFastqLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton8)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton7)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25))
        );

        byBam.setOpaque(false);
        byBam.setPreferredSize(new java.awt.Dimension(1000, 284));

        inputBamTxt.setBorder(javax.swing.BorderFactory.createTitledBorder("Upload all bam(s) files"));
        inputBamTxt.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N
        inputBamTxt.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        inputBamTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                inputBamTxtKeyPressed(evt);
            }
        });
        jScrollPane4.setViewportView(inputBamTxt);

        inputbamBtn.setText("...");
        inputbamBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputbamBtnActionPerformed(evt);
            }
        });

        jLabel92.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel92.setForeground(new java.awt.Color(255, 255, 255));
        jLabel92.setText("Input BAM File(s):");

        javax.swing.GroupLayout byBamLayout = new javax.swing.GroupLayout(byBam);
        byBam.setLayout(byBamLayout);
        byBamLayout.setHorizontalGroup(
            byBamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byBamLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(byBamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(byBamLayout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 775, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(inputbamBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel92, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        byBamLayout.setVerticalGroup(
            byBamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(byBamLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel92)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(byBamLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(inputbamBtn))
                .addContainerGap(55, Short.MAX_VALUE))
        );

        inputOptions.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N
        inputOptions.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select", "Raw FASTQ Files", "Pre-aligned BAM Files" }));
        inputOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputOptionsActionPerformed(evt);
            }
        });

        jLabel33.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel33.setForeground(new java.awt.Color(255, 255, 255));
        jLabel33.setText("Set Output Directory:");

        jLabel44.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel44.setForeground(new java.awt.Color(255, 255, 255));
        jLabel44.setText("Input Option:");

        jLabel45.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel45.setForeground(new java.awt.Color(255, 255, 255));
        jLabel45.setText("Choose Quality Control Method:");

        fastqcRadio.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        fastqcRadio.setForeground(new java.awt.Color(255, 255, 255));
        fastqcRadio.setText("FastQC");
        fastqcRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fastqcRadioActionPerformed(evt);
            }
        });

        fastpRadio.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        fastpRadio.setForeground(new java.awt.Color(255, 255, 255));
        fastpRadio.setText("FastP");
        fastpRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fastpRadioActionPerformed(evt);
            }
        });

        noneRadio.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        noneRadio.setForeground(new java.awt.Color(255, 255, 255));
        noneRadio.setSelected(true);
        noneRadio.setText("None");
        noneRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noneRadioActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout inputPanelLayout = new javax.swing.GroupLayout(inputPanel);
        inputPanel.setLayout(inputPanelLayout);
        inputPanelLayout.setHorizontalGroup(
            inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(inputPanelLayout.createSequentialGroup()
                        .addComponent(jLabel33)
                        .addGap(77, 77, 77)
                        .addComponent(output_Dir, javax.swing.GroupLayout.PREFERRED_SIZE, 787, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24)
                        .addComponent(outputBtn))
                    .addGroup(inputPanelLayout.createSequentialGroup()
                        .addComponent(jLabel100)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(inputPanelLayout.createSequentialGroup()
                        .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel45)
                            .addComponent(jLabel44))
                        .addGap(18, 18, 18)
                        .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(inputOptions, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(inputPanelLayout.createSequentialGroup()
                                .addComponent(fastqcRadio)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(fastpRadio)
                                .addGap(18, 18, 18)
                                .addComponent(noneRadio))))
                    .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(byBam, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1016, Short.MAX_VALUE)
                        .addComponent(byFastq, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 1016, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        inputPanelLayout.setVerticalGroup(
            inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputPanelLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel100)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(output_Dir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(outputBtn)
                    .addComponent(jLabel33))
                .addGap(18, 18, 18)
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(inputPanelLayout.createSequentialGroup()
                        .addComponent(jLabel45)
                        .addGap(36, 36, 36))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, inputPanelLayout.createSequentialGroup()
                        .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fastqcRadio)
                            .addComponent(fastpRadio)
                            .addComponent(noneRadio))
                        .addGap(18, 18, 18)))
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel44)
                    .addComponent(inputOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(byBam, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(byFastq, javax.swing.GroupLayout.PREFERRED_SIZE, 426, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(48, Short.MAX_VALUE))
        );

        MainPanel.add(inputPanel);

        QualityControlPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Quality Control", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255))); // NOI18N
        QualityControlPanel.setOpaque(false);
        QualityControlPanel.setPreferredSize(new java.awt.Dimension(1000, 1418));
        QualityControlPanel.setLayout(new org.jdesktop.swingx.VerticalLayout());

        back1.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        back1.setText("BACK");
        back1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                back1ActionPerformed(evt);
            }
        });
        QualityControlPanel.add(back1);

        fastqcPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("FASTQC"), "FASTQC", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 14), new java.awt.Color(255, 255, 255))); // NOI18N
        fastqcPanel.setOpaque(false);

        fastqcTable.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        fastqcTable.setModel(dm);
        fastqcTable.setRowHeight(30);
        jScrollPane15.setViewportView(fastqcTable);

        jLabel30.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(255, 255, 255));
        jLabel30.setText("FastQC Results Summary:");

        jLabel31.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(255, 255, 255));
        jLabel31.setText("Html Results:");

        fastqcResultCombo.setFont(new java.awt.Font("Liberation Sans", 0, 13)); // NOI18N
        fastqcResultCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "" }));
        fastqcResultCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fastqcResultComboActionPerformed(evt);
            }
        });

        jLabel53.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel53.setForeground(new java.awt.Color(255, 255, 255));
        jLabel53.setText("FastQC analysis is complete. Review the results below.");

        jLabel54.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel54.setForeground(new java.awt.Color(255, 255, 255));
        jLabel54.setText("Do you want to run Trimmomatic for trimming and re-run FastQC?");

        fastqcTrimCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NO proceed to run the pipeline", "YES trim and re-run FastQC" }));
        fastqcTrimCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fastqcTrimComboActionPerformed(evt);
            }
        });

        trimPanel.setBackground(new java.awt.Color(0, 0, 0));
        trimPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Trimmomatic", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N
        trimPanel.setOpaque(false);

        jLabel104.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel104.setForeground(new java.awt.Color(255, 255, 255));
        jLabel104.setText("HELP?");
        jLabel104.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel104MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel104MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel104MouseExited(evt);
            }
        });

        jLabel105.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        jLabel105.setForeground(new java.awt.Color(255, 255, 255));
        jLabel105.setText("Trimmomatic Parameters:");

        jLabel106.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel106.setForeground(new java.awt.Color(255, 255, 255));
        jLabel106.setText("phred Encoding:");

        jLabel107.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel107.setForeground(new java.awt.Color(255, 255, 255));
        jLabel107.setText("Adapter Fasta File:");

        phredEncodingCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "-phred33", "-phred64" }));

        adapterfile.setEditable(false);

        sliding.setText("4:15");

        leading.setText("3");

        trailing.setText("3");

        minlen.setText("36");

        illumniaclip.setText("2:30:10");

        maxinfo.setEnabled(false);

        crop.setEnabled(false);

        headcrop.setEnabled(false);

        thread1.setEnabled(false);

        slidingC.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        slidingC.setForeground(new java.awt.Color(255, 255, 255));
        slidingC.setSelected(true);
        slidingC.setText("SLIDINGWINDOW:");
        slidingC.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                slidingCItemStateChanged(evt);
            }
        });

        jLabel108.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel108.setForeground(new java.awt.Color(255, 255, 255));
        jLabel108.setText("ILLUMNIACLIP:");

        browseGTFBtn2.setText("...");
        browseGTFBtn2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseGTFBtn2ActionPerformed(evt);
            }
        });

        headcropC.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        headcropC.setForeground(new java.awt.Color(255, 255, 255));
        headcropC.setText("HEADCROP:");
        headcropC.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                headcropCItemStateChanged(evt);
            }
        });

        threadC.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        threadC.setForeground(new java.awt.Color(255, 255, 255));
        threadC.setText("THREAD(S):");

        maxinfoC.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        maxinfoC.setForeground(new java.awt.Color(255, 255, 255));
        maxinfoC.setText("MAXINFO:");
        maxinfoC.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                maxinfoCItemStateChanged(evt);
            }
        });

        trailingC.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        trailingC.setForeground(new java.awt.Color(255, 255, 255));
        trailingC.setSelected(true);
        trailingC.setText("TRAILING:");
        trailingC.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                trailingCItemStateChanged(evt);
            }
        });

        leadingC.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        leadingC.setForeground(new java.awt.Color(255, 255, 255));
        leadingC.setSelected(true);
        leadingC.setText("LEADING:");
        leadingC.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                leadingCItemStateChanged(evt);
            }
        });

        minlenC.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        minlenC.setForeground(new java.awt.Color(255, 255, 255));
        minlenC.setSelected(true);
        minlenC.setText("MINLEN:");
        minlenC.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                minlenCItemStateChanged(evt);
            }
        });

        logC.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        logC.setForeground(new java.awt.Color(255, 255, 255));
        logC.setText("Generate Log File?");

        cropC.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        cropC.setForeground(new java.awt.Color(255, 255, 255));
        cropC.setText("CROP:");
        cropC.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cropCItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout trimPanelLayout = new javax.swing.GroupLayout(trimPanel);
        trimPanel.setLayout(trimPanelLayout);
        trimPanelLayout.setHorizontalGroup(
            trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, trimPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel105)
                    .addGroup(trimPanelLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel106)
                            .addComponent(jLabel107)
                            .addComponent(jLabel108)
                            .addComponent(maxinfoC)
                            .addComponent(slidingC))
                        .addGap(30, 30, 30)
                        .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(trimPanelLayout.createSequentialGroup()
                                .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(sliding, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                                    .addComponent(maxinfo, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(illumniaclip, javax.swing.GroupLayout.Alignment.LEADING))
                                .addGap(30, 30, 30)
                                .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(trimPanelLayout.createSequentialGroup()
                                        .addComponent(leadingC)
                                        .addGap(30, 30, 30)
                                        .addComponent(leading, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(trimPanelLayout.createSequentialGroup()
                                        .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(cropC)
                                            .addComponent(minlenC))
                                        .addGap(38, 38, 38)
                                        .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(minlen, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                                            .addComponent(crop))))
                                .addGap(18, 18, 18)
                                .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(trailingC)
                                    .addGroup(trimPanelLayout.createSequentialGroup()
                                        .addComponent(headcropC)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(headcrop, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(trimPanelLayout.createSequentialGroup()
                                .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(trailing, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, trimPanelLayout.createSequentialGroup()
                                        .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addGroup(trimPanelLayout.createSequentialGroup()
                                                .addComponent(phredEncodingCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(70, 70, 70)
                                                .addComponent(threadC)
                                                .addGap(18, 18, 18)
                                                .addComponent(thread1))
                                            .addGroup(trimPanelLayout.createSequentialGroup()
                                                .addComponent(adapterfile, javax.swing.GroupLayout.PREFERRED_SIZE, 395, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(browseGTFBtn2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGap(18, 18, 18)
                                        .addComponent(logC)))
                                .addGap(89, 89, 89)
                                .addComponent(jLabel104, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        trimPanelLayout.setVerticalGroup(
            trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(trimPanelLayout.createSequentialGroup()
                .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(trimPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel105)
                        .addGap(11, 11, 11)
                        .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel106)
                            .addComponent(phredEncodingCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(threadC)
                            .addComponent(thread1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(logC))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel107)
                            .addComponent(adapterfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(browseGTFBtn2)))
                    .addGroup(trimPanelLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jLabel104, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel108)
                    .addComponent(illumniaclip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(leading, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(trailingC)
                    .addComponent(trailing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(leadingC))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(slidingC)
                    .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(sliding, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(minlenC)
                        .addComponent(minlen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(maxinfoC)
                    .addComponent(maxinfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cropC)
                    .addComponent(crop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(headcropC)
                    .addComponent(headcrop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(34, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout fastqcPanelLayout = new javax.swing.GroupLayout(fastqcPanel);
        fastqcPanel.setLayout(fastqcPanelLayout);
        fastqcPanelLayout.setHorizontalGroup(
            fastqcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fastqcPanelLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(fastqcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(trimPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(fastqcPanelLayout.createSequentialGroup()
                        .addComponent(jLabel54, javax.swing.GroupLayout.PREFERRED_SIZE, 503, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fastqcTrimCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 385, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, 1299, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(fastqcPanelLayout.createSequentialGroup()
                        .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fastqcResultCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel53, javax.swing.GroupLayout.PREFERRED_SIZE, 503, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        fastqcPanelLayout.setVerticalGroup(
            fastqcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fastqcPanelLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel53, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(fastqcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fastqcResultCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fastqcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel54, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fastqcTrimCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(trimPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        QualityControlPanel.add(fastqcPanel);

        fastpPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Fastp Results", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N
        fastpPanel.setOpaque(false);

        jLabel51.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel51.setForeground(new java.awt.Color(255, 255, 255));
        jLabel51.setText("Html Results:");

        fastpResultCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "" }));
        fastpResultCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fastpResultComboActionPerformed(evt);
            }
        });

        jLabel77.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel77.setForeground(new java.awt.Color(255, 255, 255));
        jLabel77.setText("Do you want to perform additional trimming using Fastp?");

        fastpTrimCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NO proceed to run the pipeline", "YES show trimming options" }));
        fastpTrimCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fastpTrimComboActionPerformed(evt);
            }
        });

        jLabel78.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel78.setForeground(new java.awt.Color(255, 255, 255));
        jLabel78.setText("FastP analysis is complete. Review the results below.");

        fastPparameter.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Additional Parameters:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 12), new java.awt.Color(255, 255, 255))); // NOI18N
        fastPparameter.setOpaque(false);

        fastpTxtArea.setColumns(20);
        fastpTxtArea.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        fastpTxtArea.setLineWrap(true);
        fastpTxtArea.setRows(5);
        jScrollPane16.setViewportView(fastpTxtArea);

        jLabel50.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel50.setForeground(new java.awt.Color(255, 255, 255));
        jLabel50.setText("Enter any additional fastp parameters below (space-separated). Ensure proper formatting.");

        jLabel79.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel79.setForeground(new java.awt.Color(255, 255, 255));
        jLabel79.setText("https://github.com/OpenGene/fastp/blob/master/README.md");
        jLabel79.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel79MouseClicked(evt);
            }
        });

        jLabel80.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel80.setForeground(new java.awt.Color(255, 255, 255));
        jLabel80.setText("For a full list of parameters, refer to the fastp Documentation.");

        javax.swing.GroupLayout fastPparameterLayout = new javax.swing.GroupLayout(fastPparameter);
        fastPparameter.setLayout(fastPparameterLayout);
        fastPparameterLayout.setHorizontalGroup(
            fastPparameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fastPparameterLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(fastPparameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 1000, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel50, javax.swing.GroupLayout.PREFERRED_SIZE, 691, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(fastPparameterLayout.createSequentialGroup()
                        .addComponent(jLabel80, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel79, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        fastPparameterLayout.setVerticalGroup(
            fastPparameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fastPparameterLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel50, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fastPparameterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel80, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel79, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout fastpPanelLayout = new javax.swing.GroupLayout(fastpPanel);
        fastpPanel.setLayout(fastpPanelLayout);
        fastpPanelLayout.setHorizontalGroup(
            fastpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fastpPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fastpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(fastpPanelLayout.createSequentialGroup()
                        .addComponent(jLabel77)
                        .addGap(18, 18, 18)
                        .addComponent(fastpTrimCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 385, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(fastpPanelLayout.createSequentialGroup()
                        .addComponent(jLabel51, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fastpResultCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel78, javax.swing.GroupLayout.PREFERRED_SIZE, 446, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(256, Short.MAX_VALUE))
            .addGroup(fastpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(fastpPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(fastPparameter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        fastpPanelLayout.setVerticalGroup(
            fastpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fastpPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel78, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(fastpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel51, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fastpResultCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(fastpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel77, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fastpTrimCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(277, Short.MAX_VALUE))
            .addGroup(fastpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, fastpPanelLayout.createSequentialGroup()
                    .addContainerGap(166, Short.MAX_VALUE)
                    .addComponent(fastPparameter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        QualityControlPanel.add(fastpPanel);

        MainPanel.add(QualityControlPanel);

        analysisPanel.setBackground(new java.awt.Color(0, 0, 0));
        analysisPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Pipline Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N
        analysisPanel.setOpaque(false);
        analysisPanel.setPreferredSize(new java.awt.Dimension(1000, 3877));

        jLabel93.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        jLabel93.setForeground(new java.awt.Color(255, 255, 255));
        jLabel93.setText("SplitNCigarReads:");

        splitNcigar.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        splitNcigar.setForeground(new java.awt.Color(255, 255, 255));
        splitNcigar.setText("NO");
        splitNcigar.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                splitNcigarItemStateChanged(evt);
            }
        });

        jLabel94.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        jLabel94.setForeground(new java.awt.Color(255, 255, 255));
        jLabel94.setText("Variant Calling:");

        germline.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        germline.setForeground(new java.awt.Color(255, 255, 255));
        germline.setText("Germline (HaplotypeCaller)");
        germline.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                germlineItemStateChanged(evt);
            }
        });

        somatic.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        somatic.setForeground(new java.awt.Color(255, 255, 255));
        somatic.setText("Somatic(Mutect2)");
        somatic.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                somaticItemStateChanged(evt);
            }
        });

        jLabel95.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel95.setForeground(new java.awt.Color(255, 255, 255));
        jLabel95.setText("Reference Genome(fasta):");

        refTxt.setEditable(false);

        refBtn.setText("...");
        refBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refBtnActionPerformed(evt);
            }
        });

        jLabel96.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel96.setForeground(new java.awt.Color(255, 255, 255));
        jLabel96.setText("AddOrReplaceReadGroups:");

        jLabel97.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel97.setForeground(new java.awt.Color(255, 255, 255));
        jLabel97.setText("known Sites (dbSNP):");

        mutect2Panel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Mutect2 Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255))); // NOI18N
        mutect2Panel.setOpaque(false);

        jLabel4.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Select any option:");

        mutectCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "SELECT", "Tumor with matched normal (Single)", "Tumor with matched normal (Multiple)", "Tumor-only mode", "Mitochondrial mode", "Force-calling mode" }));
        mutectCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                mutectComboItemStateChanged(evt);
            }
        });

        normalPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Tumor-Normal Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255))); // NOI18N
        normalPanel.setOpaque(false);

        jLabel27.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(255, 255, 255));
        jLabel27.setText("Normal Sample(s):");

        tumNorBtn.setText(">>>");
        tumNorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tumNorBtnActionPerformed(evt);
            }
        });

        norTumBtn.setText("<<<");
        norTumBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                norTumBtnActionPerformed(evt);
            }
        });

        jListNormal.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListNormal.setDragEnabled(true);
        jListNormal.setDropMode(javax.swing.DropMode.INSERT);
        jListNormal.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListNormalValueChanged(evt);
            }
        });
        jScrollPane6.setViewportView(jListNormal);

        jListTumor.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListTumor.setDragEnabled(true);
        jListTumor.setDropMode(javax.swing.DropMode.INSERT);
        jListTumor.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListTumorValueChanged(evt);
            }
        });
        jScrollPane14.setViewportView(jListTumor);

        jLabel29.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(255, 255, 255));
        jLabel29.setText("Tumor Sample(s):");

        mainLabelWarning.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        mainLabelWarning.setForeground(new java.awt.Color(255, 255, 255));
        mainLabelWarning.setText("WARNING:  All samples are set as tumor samples by default. Please review and separate any normal samples before proceeding.");

        Label1.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        Label1.setForeground(new java.awt.Color(255, 255, 255));
        Label1.setText("Ensure the sequence of tumor-normal pairs is correct. The first tumor matches the first normal, the second with the second, and so on.");

        Label2.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        Label2.setForeground(new java.awt.Color(255, 255, 255));
        Label2.setText("Tumor-Only Mode: If all BAM files are in the Tumor Samples list, they will be processed independently to detect somatic variants with uploaded Panel of Normals (PoN).");

        Label3.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        Label3.setForeground(new java.awt.Color(255, 255, 255));
        Label3.setText("Tumor with PoN Mode: If you designate files in both Tumor and Normal lists:");

        Label4.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        Label4.setForeground(new java.awt.Color(255, 255, 255));
        Label4.setText("1-The normal samples will be processed first to create a Panel of Normals (PoN).");

        Label5.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        Label5.setForeground(new java.awt.Color(255, 255, 255));
        Label5.setText("2-This PoN will then be used to filter artifacts from the tumor samples, improving the accuracy of somatic variant detection.");

        Label6.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        Label6.setForeground(new java.awt.Color(255, 255, 255));
        Label6.setText("Note: Ensure you correctly classify your samples for optimal results.");

        javax.swing.GroupLayout normalPanelLayout = new javax.swing.GroupLayout(normalPanel);
        normalPanel.setLayout(normalPanelLayout);
        normalPanelLayout.setHorizontalGroup(
            normalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(normalPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(normalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(normalPanelLayout.createSequentialGroup()
                        .addComponent(jLabel29)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 443, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(normalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(normalPanelLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jLabel27))
                            .addGroup(normalPanelLayout.createSequentialGroup()
                                .addGap(33, 33, 33)
                                .addGroup(normalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(norTumBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                    .addComponent(tumNorBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 449, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(mainLabelWarning)
                    .addComponent(Label1)
                    .addComponent(Label2)
                    .addComponent(Label3)
                    .addGroup(normalPanelLayout.createSequentialGroup()
                        .addGap(124, 124, 124)
                        .addGroup(normalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Label4)
                            .addComponent(Label5)))
                    .addComponent(Label6))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        normalPanelLayout.setVerticalGroup(
            normalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, normalPanelLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(mainLabelWarning)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Label1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Label2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Label3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Label4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Label5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(Label6)
                .addGroup(normalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, normalPanelLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(normalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel27)
                            .addGroup(normalPanelLayout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addComponent(jLabel29)))
                        .addGap(22, 22, 22)
                        .addComponent(tumNorBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(norTumBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(126, 126, 126))
                    .addGroup(normalPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(normalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane14, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(22, 22, 22))))
        );

        otherPanel.setOpaque(false);

        germlineResTxt.setEditable(false);

        ponTxt.setEditable(false);

        alleleTxt.setEditable(false);

        f1r2.setEditable(false);

        germlineResBtn.setText("...");
        germlineResBtn.setEnabled(false);
        germlineResBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                germlineResBtnActionPerformed(evt);
            }
        });

        ponBtn.setText("...");
        ponBtn.setEnabled(false);
        ponBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ponBtnActionPerformed(evt);
            }
        });

        alleleBtn.setText("...");
        alleleBtn.setEnabled(false);
        alleleBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alleleBtnActionPerformed(evt);
            }
        });

        intervalBtn.setText("...");
        intervalBtn.setEnabled(false);
        intervalBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                intervalBtnActionPerformed(evt);
            }
        });

        f1r2Btn.setText("...");
        f1r2Btn.setEnabled(false);
        f1r2Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                f1r2BtnActionPerformed(evt);
            }
        });

        intervalText.setEditable(false);
        intervalText.setColumns(20);
        intervalText.setLineWrap(true);
        intervalText.setRows(5);
        jScrollPane12.setViewportView(intervalText);

        intervalT.setEditable(false);

        add6.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        add6.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/plusSs.png")); // NOI18N
        add6.setEnabled(false);
        add6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add6ActionPerformed(evt);
            }
        });

        del6.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        del6.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/removeSs.png")); // NOI18N
        del6.setEnabled(false);
        del6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del6ActionPerformed(evt);
            }
        });

        jLabel37.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel37.setForeground(new java.awt.Color(255, 255, 255));
        jLabel37.setText("To create an interval list:");

        jLabel38.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel38.setForeground(new java.awt.Color(255, 255, 255));
        jLabel38.setText("2- Click  add (+) to include it in the interval list or del (-) to remove it from the list.");

        jLabel39.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel39.setForeground(new java.awt.Color(255, 255, 255));
        jLabel39.setText("1- Type an interval (e.g., chr1:1000-2000) or browse for a file (...)");

        ponLabel2.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        ponLabel2.setForeground(new java.awt.Color(255, 255, 255));
        ponLabel2.setText("The generated PoN file will be used automatically in the pipeline.");

        generatePON.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        generatePON.setForeground(new java.awt.Color(255, 255, 255));
        generatePON.setText("Generate PON");
        generatePON.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generatePONActionPerformed(evt);
            }
        });

        ponLabel3.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        ponLabel3.setForeground(new java.awt.Color(255, 255, 255));
        ponLabel3.setText("OR");

        ponLabel4.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        ponLabel4.setForeground(new java.awt.Color(255, 255, 255));
        ponLabel4.setText("Check this 'Generate PoN' t box to genrate one from your selected normal samples:");

        jLabel42.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel42.setForeground(new java.awt.Color(255, 255, 255));
        jLabel42.setText("Interval list:");

        germlineCheck.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        germlineCheck.setForeground(new java.awt.Color(255, 255, 255));
        germlineCheck.setText("Upload your germline resource file (.vcf):");
        germlineCheck.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                germlineCheckItemStateChanged(evt);
            }
        });

        ponCheck.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        ponCheck.setForeground(new java.awt.Color(255, 255, 255));
        ponCheck.setText("Upload an existing PoN file (pon.vcf.gz):");
        ponCheck.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ponCheckItemStateChanged(evt);
            }
        });

        allelesCheck.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        allelesCheck.setForeground(new java.awt.Color(255, 255, 255));
        allelesCheck.setText("Upload the alleles file (.vcf or .bed):");
        allelesCheck.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                allelesCheckItemStateChanged(evt);
            }
        });

        f1r2Check.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        f1r2Check.setForeground(new java.awt.Color(255, 255, 255));
        f1r2Check.setText("Upload an F1R2 metrics file(.tar.gz):");
        f1r2Check.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                f1r2CheckItemStateChanged(evt);
            }
        });

        intervalCheck.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        intervalCheck.setForeground(new java.awt.Color(255, 255, 255));
        intervalCheck.setText("Specify your genomic intervals by either uploading an interval file or entering interval commands manually.");
        intervalCheck.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                intervalCheckItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout otherPanelLayout = new javax.swing.GroupLayout(otherPanel);
        otherPanel.setLayout(otherPanelLayout);
        otherPanelLayout.setHorizontalGroup(
            otherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(otherPanelLayout.createSequentialGroup()
                .addGroup(otherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(intervalT, javax.swing.GroupLayout.PREFERRED_SIZE, 554, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(intervalBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(add6, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(del6, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(ponLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(generatePON))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(germlineResTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 557, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(germlineResBtn))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(ponTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 557, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ponBtn))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addGap(228, 228, 228)
                        .addComponent(ponLabel3))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(ponLabel4))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(alleleTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 557, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(alleleBtn))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(f1r2, javax.swing.GroupLayout.PREFERRED_SIZE, 554, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(f1r2Btn))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel37))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel39, javax.swing.GroupLayout.PREFERRED_SIZE, 487, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel38))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 554, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel42))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(germlineCheck))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(ponCheck))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(allelesCheck))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(f1r2Check))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(intervalCheck)))
                .addContainerGap(319, Short.MAX_VALUE))
        );
        otherPanelLayout.setVerticalGroup(
            otherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(otherPanelLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(germlineCheck)
                .addGap(8, 8, 8)
                .addGroup(otherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(germlineResTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(germlineResBtn))
                .addGap(18, 18, 18)
                .addComponent(ponCheck)
                .addGap(10, 10, 10)
                .addGroup(otherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ponTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ponBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ponLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ponLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(otherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ponLabel2)
                    .addComponent(generatePON))
                .addGap(24, 24, 24)
                .addComponent(allelesCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(otherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(alleleTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(alleleBtn))
                .addGap(24, 24, 24)
                .addComponent(f1r2Check)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(otherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(f1r2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(f1r2Btn))
                .addGap(18, 18, 18)
                .addComponent(intervalCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel37)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel39)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel38)
                .addGap(23, 23, 23)
                .addGroup(otherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(otherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(intervalT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(intervalBtn))
                    .addComponent(del6, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(add6, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel42)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50))
        );

        javax.swing.GroupLayout mutect2PanelLayout = new javax.swing.GroupLayout(mutect2Panel);
        mutect2Panel.setLayout(mutect2PanelLayout);
        mutect2PanelLayout.setHorizontalGroup(
            mutect2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mutect2PanelLayout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(mutect2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mutect2PanelLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(mutectCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 559, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(normalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(otherPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        mutect2PanelLayout.setVerticalGroup(
            mutect2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mutect2PanelLayout.createSequentialGroup()
                .addContainerGap(10, Short.MAX_VALUE)
                .addGroup(mutect2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(mutectCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(normalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 399, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(otherPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 656, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        germlinePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "HaplotypeCaller Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255))); // NOI18N
        germlinePanel.setOpaque(false);

        jLabel24.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setText("Select Emit Ref Confidence (-ERC):");

        bamoutTxt.setEditable(false);

        bamoutB.setText("...");
        bamoutB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bamoutBActionPerformed(evt);
            }
        });

        ercCombo.setEditable(true);
        ercCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "GVCF", "BP_RESOLUTION", "NONE" }));

        g1Check.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        g1Check.setForeground(new java.awt.Color(255, 255, 255));
        g1Check.setText("Add Annotation Groups (-G):");
        g1Check.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                g1CheckItemStateChanged(evt);
            }
        });

        bamoutCheck.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        bamoutCheck.setForeground(new java.awt.Color(255, 255, 255));
        bamoutCheck.setText("-bamout");
        bamoutCheck.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                bamoutCheckItemStateChanged(evt);
            }
        });

        singleModeH.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        singleModeH.setForeground(new java.awt.Color(255, 255, 255));
        singleModeH.setText("Single Sample Mode");
        singleModeH.setToolTipText(" Calls variants for each sample independently.");
        singleModeH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                singleModeHActionPerformed(evt);
            }
        });

        jointModeH.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jointModeH.setForeground(new java.awt.Color(255, 255, 255));
        jointModeH.setSelected(true);
        jointModeH.setText("Joint Genotyping Mode");
        jointModeH.setToolTipText("Combines multiple GVCFs using GenotypeGVCFs for accurate variant calling across all samples.");
        jointModeH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jointModeHActionPerformed(evt);
            }
        });

        annotationGroupTxt.setEditable(false);
        annotationGroupTxt.setColumns(20);
        annotationGroupTxt.setLineWrap(true);
        annotationGroupTxt.setRows(5);
        jScrollPane11.setViewportView(annotationGroupTxt);

        add7.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        add7.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/plusSs.png")); // NOI18N
        add7.setEnabled(false);
        add7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add7ActionPerformed(evt);
            }
        });

        del7.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        del7.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/removeSs.png")); // NOI18N
        del7.setEnabled(false);
        del7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del7ActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Liberation Sans", 3, 15)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("GATK Annotation Group Reference");
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel6MouseClicked(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Selected Annotation Groups:");

        jLabel9.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Please type the name of an annotation group into the text field and click the (+) button to include it in the selected Annotation Group below. or remove it using (-)");

        jLabel10.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("If you want to remove an annotation group from the list, type the name of annotation group into the text field and click (-) button.");

        jLabel11.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("If you are unsure of the valid annotation groups (e.g., Standard, AS_Standard, etc.), please click GATK Annotation Group Reference");

        jLabel8.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Annotation Groups (-G):");

        annotationGroupCombo.setEditable(true);

        genomicDBPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "GenomicsDBImport Settings:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 12), new java.awt.Color(255, 255, 255))); // NOI18N
        genomicDBPanel.setOpaque(false);

        jLabel46.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel46.setForeground(new java.awt.Color(255, 255, 255));
        jLabel46.setText("To create an interval list, type an interval (e.g., chr1:1000-2000) or browse for a file (...) ");

        jLabel48.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel48.setForeground(new java.awt.Color(255, 255, 255));

        intervalT1.setEditable(false);
        intervalT1.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        jLabel49.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel49.setForeground(new java.awt.Color(255, 255, 255));
        jLabel49.setText("OR");

        intervalText1.setEditable(false);
        intervalText1.setColumns(20);
        intervalText1.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N
        intervalText1.setLineWrap(true);
        intervalText1.setRows(5);
        jScrollPane17.setViewportView(intervalText1);

        intervalBtn2.setText("...");
        intervalBtn2.setEnabled(false);
        intervalBtn2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                intervalBtn2ActionPerformed(evt);
            }
        });

        add9.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        add9.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/plusSs.png")); // NOI18N
        add9.setEnabled(false);
        add9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add9ActionPerformed(evt);
            }
        });

        del9.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        del9.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/removeSs.png")); // NOI18N
        del9.setEnabled(false);
        del9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del9ActionPerformed(evt);
            }
        });

        intervalT2.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N

        jLabel56.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel56.setForeground(new java.awt.Color(255, 255, 255));
        jLabel56.setText(" Click  add (+) to include it in the interval list or del (-) to remove it from the list.");

        jLabel57.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel57.setForeground(new java.awt.Color(255, 255, 255));
        jLabel57.setText("Type intervals here and then add or delete through (+) or (-):");

        uploadIntervalfileR.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        uploadIntervalfileR.setForeground(new java.awt.Color(255, 255, 255));
        uploadIntervalfileR.setSelected(true);
        uploadIntervalfileR.setText("To upload interval file:");

        createIntervalR.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        createIntervalR.setForeground(new java.awt.Color(255, 255, 255));
        createIntervalR.setText("To create interval list mannually:");
        createIntervalR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createIntervalRActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout genomicDBPanelLayout = new javax.swing.GroupLayout(genomicDBPanel);
        genomicDBPanel.setLayout(genomicDBPanelLayout);
        genomicDBPanelLayout.setHorizontalGroup(
            genomicDBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(genomicDBPanelLayout.createSequentialGroup()
                .addGroup(genomicDBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel46)
                    .addGroup(genomicDBPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(genomicDBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(genomicDBPanelLayout.createSequentialGroup()
                                .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 554, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(add9, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(del9, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(genomicDBPanelLayout.createSequentialGroup()
                                .addComponent(jLabel48)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(genomicDBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel57)
                                    .addGroup(genomicDBPanelLayout.createSequentialGroup()
                                        .addGroup(genomicDBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(intervalT2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE)
                                            .addComponent(intervalT1, javax.swing.GroupLayout.Alignment.LEADING))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(intervalBtn2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(jLabel56)
                            .addGroup(genomicDBPanelLayout.createSequentialGroup()
                                .addComponent(createIntervalR)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel49))
                            .addComponent(uploadIntervalfileR))))
                .addContainerGap(36, Short.MAX_VALUE))
        );
        genomicDBPanelLayout.setVerticalGroup(
            genomicDBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(genomicDBPanelLayout.createSequentialGroup()
                .addComponent(jLabel46)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(uploadIntervalfileR)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(genomicDBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(intervalT1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(intervalBtn2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel49)
                .addGroup(genomicDBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(genomicDBPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel48)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(genomicDBPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(createIntervalR)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel57)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(intervalT2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(jLabel56)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(genomicDBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(genomicDBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(add9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                        .addComponent(del9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        javax.swing.GroupLayout germlinePanelLayout = new javax.swing.GroupLayout(germlinePanel);
        germlinePanel.setLayout(germlinePanelLayout);
        germlinePanelLayout.setHorizontalGroup(
            germlinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(germlinePanelLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(germlinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(germlinePanelLayout.createSequentialGroup()
                        .addGroup(germlinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel24)
                            .addComponent(ercCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(germlinePanelLayout.createSequentialGroup()
                                .addComponent(bamoutTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bamoutB))
                            .addComponent(bamoutCheck))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, germlinePanelLayout.createSequentialGroup()
                        .addGroup(germlinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane11, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(germlinePanelLayout.createSequentialGroup()
                                .addComponent(annotationGroupCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(add7, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(del7, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(769, 769, 769))
                    .addGroup(germlinePanelLayout.createSequentialGroup()
                        .addComponent(singleModeH)
                        .addGap(18, 18, 18)
                        .addComponent(jointModeH)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(genomicDBPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(256, 256, 256))
                    .addGroup(germlinePanelLayout.createSequentialGroup()
                        .addGroup(germlinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11)
                            .addGroup(germlinePanelLayout.createSequentialGroup()
                                .addComponent(g1Check)
                                .addGap(207, 207, 207)
                                .addComponent(jLabel6)))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        germlinePanelLayout.setVerticalGroup(
            germlinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, germlinePanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(germlinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(germlinePanelLayout.createSequentialGroup()
                        .addGroup(germlinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(singleModeH)
                            .addComponent(jointModeH))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel24)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ercCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(genomicDBPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addGap(18, 18, 18)
                .addGroup(germlinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(g1Check)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(germlinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(add7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(del7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(annotationGroupCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bamoutCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(germlinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bamoutTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bamoutB))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        knownSNPBtn.setText("...");
        knownSNPBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                knownSNPBtnActionPerformed(evt);
            }
        });

        jLabel99.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        jLabel99.setForeground(new java.awt.Color(255, 255, 255));
        jLabel99.setText("Base Quality Recalibration:");

        jScrollPane1.setOpaque(false);

        addReplaceRGPanel.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(addReplaceRGPanel);

        bwaPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "BWA MEM Options:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255))); // NOI18N
        bwaPanel.setOpaque(false);

        bwaM.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        bwaM.setForeground(new java.awt.Color(255, 255, 255));
        bwaM.setSelected(true);
        bwaM.setText("-M");

        javax.swing.GroupLayout bwaPanelLayout = new javax.swing.GroupLayout(bwaPanel);
        bwaPanel.setLayout(bwaPanelLayout);
        bwaPanelLayout.setHorizontalGroup(
            bwaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bwaPanelLayout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(bwaM)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        bwaPanelLayout.setVerticalGroup(
            bwaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bwaPanelLayout.createSequentialGroup()
                .addComponent(bwaM)
                .addGap(0, 13, Short.MAX_VALUE))
        );

        knownSites.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                knownSitesKeyPressed(evt);
            }
        });
        jScrollPane3.setViewportView(knownSites);

        jLabel98.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel98.setForeground(new java.awt.Color(255, 255, 255));
        jLabel98.setText("Upload one or more known-sites files for BaseRecalibrator(e.g., .vcf,.vcf.gz).");

        jButton2.setText("BACK");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel102.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel102.setForeground(new java.awt.Color(255, 255, 255));
        jLabel102.setText("NOTE: Required only for RNA-seq data. Skip this step If you are working with Whole Exome (WES) or Whole Genome (WGS) sequencing data. ");

        jLabel103.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel103.setForeground(new java.awt.Color(255, 255, 255));
        jLabel103.setText("Run SplitNCigarReads:");

        jLabel28.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(255, 255, 255));
        jLabel28.setText("EXAMPLE DATA");
        jLabel28.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel28MouseClicked(evt);
            }
        });

        FilterPanel.setOpaque(false);

        jCheckBox5.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jCheckBox5.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBox5.setSelected(true);
        jCheckBox5.setText("Variant Type (--select-type-to-include)");
        jCheckBox5.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox5ItemStateChanged(evt);
            }
        });

        vepAssembly.setText("GRCh38");

        jLabel3.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("--filter-expression");
        jLabel3.setToolTipText("One or more expressions used with INFO fields to filter");

        jLabel41.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel41.setForeground(new java.awt.Color(255, 255, 255));
        jLabel41.setText("Filter String:");

        jLabel21.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(255, 255, 255));
        jLabel21.setText("Enter snpEff Database name:");

        jLabel17.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setText("Run SelectVariants?:");

        jCheckBox8.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jCheckBox8.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBox8.setText("Genomic Interval (-L)");
        jCheckBox8.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox8ItemStateChanged(evt);
            }
        });

        selectVariantTxt.setColumns(20);
        selectVariantTxt.setLineWrap(true);
        selectVariantTxt.setRows(5);
        jScrollPane7.setViewportView(selectVariantTxt);

        snpEff_DB.setText("GRCh38.p14");

        variantFilterTxtB.setColumns(20);
        variantFilterTxtB.setLineWrap(true);
        variantFilterTxtB.setRows(5);
        jScrollPane10.setViewportView(variantFilterTxtB);

        jLabel12.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("--filter-name");

        del3.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        del3.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/removeSs.png")); // NOI18N
        del3.setEnabled(false);
        del3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del3ActionPerformed(evt);
            }
        });

        add1.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        add1.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/plusSs.png")); // NOI18N
        add1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add1ActionPerformed(evt);
            }
        });

        selectTypeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NO_VARIATION", "SNP", "MNP", "INDEL", "SYMBOLIC", "MIXED" }));
        selectTypeCombo.setSelectedIndex(1);

        jCheckBox6.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jCheckBox6.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBox6.setText("Selection Expression (-select)");
        jCheckBox6.setToolTipText("A filtering expression in terms of either INFO fields or the VariantContext object. If the expression evaluates to true for a variant, it will be kept in the output vcf.");
        jCheckBox6.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox6ItemStateChanged(evt);
            }
        });

        jLabel40.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(255, 255, 255));
        jLabel40.setText("Build filter string for VariantFilteration by adding(+) or removing(-) below arguments");

        jLabel34.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(255, 255, 255));
        jLabel34.setText("Filter String:");
        jLabel34.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel34MouseClicked(evt);
            }
        });

        vepRadio.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        vepRadio.setForeground(new java.awt.Color(255, 255, 255));
        vepRadio.setText("VEP");
        vepRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vepRadioActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("For VEP enter following parameters:");

        snpEffvepRadio.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        snpEffvepRadio.setForeground(new java.awt.Color(255, 255, 255));
        snpEffvepRadio.setSelected(true);
        snpEffvepRadio.setText("Both");
        snpEffvepRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                snpEffvepRadioActionPerformed(evt);
            }
        });

        add8.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        add8.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/plusSs.png")); // NOI18N
        add8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add8ActionPerformed(evt);
            }
        });

        snpEffRadio.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        snpEffRadio.setForeground(new java.awt.Color(255, 255, 255));
        snpEffRadio.setText("snpEff");
        snpEffRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                snpEffRadioActionPerformed(evt);
            }
        });

        del1.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        del1.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/removeSs.png")); // NOI18N
        del1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del1ActionPerformed(evt);
            }
        });

        selectGenotype.setEditable(false);

        jLabel43.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel43.setForeground(new java.awt.Color(255, 255, 255));
        jLabel43.setText("Use OR (|) to allow any condition, AND (&) to require all conditions, and always use parentheses when combining multiple conditions to ensure proper grouping.");

        vepSpecies.setText("homo_sapiens");

        jLabel20.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(255, 255, 255));
        jLabel20.setText("SelectVariants Options:");

        Linterval.setEditable(false);

        jLabel19.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(255, 255, 255));
        jLabel19.setText("Variation Annotation:");

        selectVariantsOp.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        selectVariantsOp.setForeground(new java.awt.Color(255, 255, 255));
        selectVariantsOp.setSelected(true);
        selectVariantsOp.setText("YES");
        selectVariantsOp.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                selectVariantsOpItemStateChanged(evt);
            }
        });

        select.setEditable(false);

        jLabel23.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(255, 255, 255));
        jLabel23.setText("Variant Filteration  after Annotation (SnpSIFT):");

        add2.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        add2.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/plusSs.png")); // NOI18N
        add2.setEnabled(false);
        add2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add2ActionPerformed(evt);
            }
        });

        jLabel18.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(255, 255, 255));
        jLabel18.setText("Filteration Before Annotation:");

        jLabel13.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("SnpSIFT filter:");
        jLabel13.setToolTipText("One or more expressions used with INFO fields to filter");

        jCheckBox7.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jCheckBox7.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBox7.setText("Genotype Condition (-select-genotype)");
        jCheckBox7.setToolTipText("A filtering expression in terms of FORMAT fields. If the expression evaluates to true for a variant, it will be kept in the output vcf.");
        jCheckBox7.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox7ItemStateChanged(evt);
            }
        });

        del5.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        del5.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/removeSs.png")); // NOI18N
        del5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del5ActionPerformed(evt);
            }
        });

        add3.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        add3.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/plusSs.png")); // NOI18N
        add3.setEnabled(false);
        add3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add3ActionPerformed(evt);
            }
        });

        intervalBtn1.setText("...");
        intervalBtn1.setToolTipText("To create an interval list:\n1- Type an interval (e.g., chr1:1000-2000) or browse for a file (...)\n2- Click  add (+) to include it in the list or del (-) to remove it from the list.");
        intervalBtn1.setEnabled(false);
        intervalBtn1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                intervalBtn1ActionPerformed(evt);
            }
        });

        jLabel26.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(255, 255, 255));
        jLabel26.setText("Run VariantFilteration after annotation?");

        variantFilterTxtA.setColumns(20);
        variantFilterTxtA.setLineWrap(true);
        variantFilterTxtA.setRows(5);
        variantFilterTxtA.setToolTipText("\"<html>Use & for AND, | for OR, and () for grouping.<br>\" +     \"Example: isVariant & (VT = 'SNP') & (QUAL > 30)<br>\" +     \"Use quotes for strings: ANN[*].GENE = 'TP53'</html>\"");
        variantFilterTxtA.setWrapStyleWord(true);
        jScrollPane13.setViewportView(variantFilterTxtA);

        snpSiftCombobox.setEditable(true);
        snpSiftCombobox.setModel(new javax.swing.DefaultComboBoxModel<>(combinedFilters));

        jLabel25.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(255, 255, 255));
        jLabel25.setText("Don't know the database name? Click here to enlist all 'Human Genome' snpEff Databases");
        jLabel25.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel25MouseClicked(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setText("--assembly:");

        add4.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        add4.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/plusSs.png")); // NOI18N
        add4.setEnabled(false);
        add4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add4ActionPerformed(evt);
            }
        });

        jLabel35.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(255, 255, 255));
        jLabel35.setText("Filter String: Do not delete [SNPEFF] or [VEP] tags. Write/select your expressions directly below or beside each tag header.");
        jLabel35.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel35MouseClicked(evt);
            }
        });

        del2.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        del2.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/removeSs.png")); // NOI18N
        del2.setEnabled(false);
        del2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del2ActionPerformed(evt);
            }
        });

        del4.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        del4.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/removeSs.png")); // NOI18N
        del4.setEnabled(false);
        del4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del4ActionPerformed(evt);
            }
        });

        variantFilterA.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        variantFilterA.setForeground(new java.awt.Color(255, 255, 255));
        variantFilterA.setText("NO");
        variantFilterA.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                variantFilterAItemStateChanged(evt);
            }
        });

        add5.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        add5.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/plusSs.png")); // NOI18N
        add5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add5ActionPerformed(evt);
            }
        });

        del8.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        del8.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/removeSs.png")); // NOI18N
        del8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del8ActionPerformed(evt);
            }
        });

        jLabel36.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel36.setForeground(new java.awt.Color(255, 255, 255));
        jLabel36.setText("Build filter string for SelectVariants by adding(+) or removing(-) below arguments");

        jLabel16.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("--species");

        excludeFiltered.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        excludeFiltered.setForeground(new java.awt.Color(255, 255, 255));
        excludeFiltered.setSelected(true);
        excludeFiltered.setText("--exclude-filtered");
        excludeFiltered.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                excludeFilteredItemStateChanged(evt);
            }
        });

        restrictAlleles.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        restrictAlleles.setForeground(new java.awt.Color(255, 255, 255));
        restrictAlleles.setText("--restrict-alleles-to ");
        restrictAlleles.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                restrictAllelesItemStateChanged(evt);
            }
        });

        restrictAlleleCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ALL", "BIALLELIC", "MULTIALLELIC" }));
        restrictAlleleCombo.setSelectedIndex(1);

        add10.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        add10.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/plusSs.png")); // NOI18N
        add10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add10ActionPerformed(evt);
            }
        });

        del10.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        del10.setIcon(new javax.swing.ImageIcon("/home/iffy/NetBeansProjects/NGSGradle/app/images/removeSs.png")); // NOI18N
        del10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del10ActionPerformed(evt);
            }
        });

        jLabel32.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(255, 255, 255));
        jLabel32.setText("VariantFilteration:");

        javax.swing.GroupLayout FilterPanelLayout = new javax.swing.GroupLayout(FilterPanel);
        FilterPanel.setLayout(FilterPanelLayout);
        FilterPanelLayout.setHorizontalGroup(
            FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FilterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(FilterPanelLayout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 937, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel18)
                            .addComponent(jLabel40)
                            .addComponent(jLabel34)
                            .addGroup(FilterPanelLayout.createSequentialGroup()
                                .addComponent(jLabel17)
                                .addGap(29, 29, 29)
                                .addComponent(selectVariantsOp))
                            .addComponent(jLabel36)
                            .addComponent(jLabel20)
                            .addGroup(FilterPanelLayout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(FilterPanelLayout.createSequentialGroup()
                                        .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jCheckBox5)
                                            .addComponent(jCheckBox6)
                                            .addComponent(jCheckBox7)
                                            .addComponent(jCheckBox8))
                                        .addGap(12, 12, 12)
                                        .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                            .addComponent(selectTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(select, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(selectGenotype, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(Linterval, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(FilterPanelLayout.createSequentialGroup()
                                                .addComponent(add3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(del3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(FilterPanelLayout.createSequentialGroup()
                                                .addComponent(intervalBtn1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(add4, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(del4, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(FilterPanelLayout.createSequentialGroup()
                                                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(FilterPanelLayout.createSequentialGroup()
                                                        .addComponent(add1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(del1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                    .addGroup(FilterPanelLayout.createSequentialGroup()
                                                        .addComponent(add2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(del2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(53, 53, 53)
                                                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(excludeFiltered)
                                                    .addGroup(FilterPanelLayout.createSequentialGroup()
                                                        .addComponent(restrictAlleles)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(restrictAlleleCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(add10, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(del10, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 937, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel41)))
                            .addComponent(jLabel32)))
                    .addComponent(jLabel19)
                    .addComponent(jLabel25)
                    .addGroup(FilterPanelLayout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addGap(20, 20, 20)
                        .addComponent(snpEff_DB, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(FilterPanelLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 937, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel35)))
                    .addGroup(FilterPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel13)
                        .addGap(18, 18, 18)
                        .addComponent(snpSiftCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, 686, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(add8, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(del8, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel23)
                    .addComponent(jLabel43)
                    .addGroup(FilterPanelLayout.createSequentialGroup()
                        .addComponent(jLabel26)
                        .addGap(18, 18, 18)
                        .addComponent(variantFilterA))
                    .addGroup(FilterPanelLayout.createSequentialGroup()
                        .addComponent(snpEffRadio)
                        .addGap(18, 18, 18)
                        .addComponent(vepRadio)
                        .addGap(18, 18, 18)
                        .addComponent(snpEffvepRadio))
                    .addGroup(FilterPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(FilterPanelLayout.createSequentialGroup()
                                .addComponent(jLabel15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(vepAssembly, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(34, 34, 34)
                                .addComponent(jLabel16)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(vepSpecies, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel1)))
                    .addGroup(FilterPanelLayout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jLabel12)
                        .addGap(18, 18, 18)
                        .addComponent(filterNameB, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(155, 155, 155)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filterExprB, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(add5, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(del5, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        FilterPanelLayout.setVerticalGroup(
            FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FilterPanelLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel18)
                .addGap(15, 15, 15)
                .addComponent(jLabel32)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel40)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(filterExprB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3))
                    .addComponent(add5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(FilterPanelLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(filterNameB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(del5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel34)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(selectVariantsOp))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel36)
                .addGap(15, 15, 15)
                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox5)
                    .addComponent(add1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(del1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(excludeFiltered))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(FilterPanelLayout.createSequentialGroup()
                        .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(FilterPanelLayout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(jCheckBox6))
                            .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(del2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(select, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(add2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(restrictAlleles)
                                .addComponent(restrictAlleleCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(add10, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(del10, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jCheckBox7)
                                .addComponent(selectGenotype, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(add3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(del3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox8)
                    .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(Linterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(intervalBtn1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(add4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(del4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel41)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel19)
                .addGap(23, 23, 23)
                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(snpEffRadio)
                    .addComponent(vepRadio)
                    .addComponent(snpEffvepRadio))
                .addGap(18, 18, 18)
                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel21)
                    .addComponent(snpEff_DB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addComponent(jLabel25)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16)
                    .addComponent(vepAssembly, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vepSpecies, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(jLabel23)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(variantFilterA))
                .addGap(18, 18, 18)
                .addComponent(jLabel43)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(FilterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel13)
                        .addComponent(snpSiftCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(add8, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(del8, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19)
                .addComponent(jLabel35)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabel101.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel101.setForeground(new java.awt.Color(255, 255, 255));
        jLabel101.setText("Thread(s):");

        thread.setText("8");

        outputLabel.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        outputLabel.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout analysisPanelLayout = new javax.swing.GroupLayout(analysisPanel);
        analysisPanel.setLayout(analysisPanelLayout);
        analysisPanelLayout.setHorizontalGroup(
            analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(analysisPanelLayout.createSequentialGroup()
                .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(analysisPanelLayout.createSequentialGroup()
                        .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, analysisPanelLayout.createSequentialGroup()
                                .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(analysisPanelLayout.createSequentialGroup()
                                        .addGap(7, 7, 7)
                                        .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel99)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, analysisPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel97)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(analysisPanelLayout.createSequentialGroup()
                                                        .addComponent(jLabel103)
                                                        .addGap(80, 80, 80)
                                                        .addComponent(splitNcigar))
                                                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 798, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                                    .addGroup(analysisPanelLayout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(jLabel96)
                                        .addGap(18, 18, 18)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 798, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(bwaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(46, 46, 46))
                            .addGroup(analysisPanelLayout.createSequentialGroup()
                                .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(analysisPanelLayout.createSequentialGroup()
                                        .addGap(68, 68, 68)
                                        .addComponent(jLabel98))
                                    .addGroup(analysisPanelLayout.createSequentialGroup()
                                        .addGap(34, 34, 34)
                                        .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel93)
                                            .addComponent(jLabel102))))
                                .addGap(73, 73, 73)))
                        .addComponent(knownSNPBtn))
                    .addGroup(analysisPanelLayout.createSequentialGroup()
                        .addGap(134, 134, 134)
                        .addComponent(germline)
                        .addGap(38, 38, 38)
                        .addComponent(somatic))
                    .addGroup(analysisPanelLayout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(jLabel94))
                    .addGroup(analysisPanelLayout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(analysisPanelLayout.createSequentialGroup()
                                .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel95)
                                    .addComponent(jLabel28)
                                    .addComponent(jLabel101))
                                .addGap(55, 55, 55)
                                .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(thread, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jButton2)))
                            .addGroup(analysisPanelLayout.createSequentialGroup()
                                .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(outputLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 1038, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(refTxt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 798, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(refBtn))))
                    .addGroup(analysisPanelLayout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(FilterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(mutect2Panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(germlinePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        analysisPanelLayout.setVerticalGroup(
            analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(analysisPanelLayout.createSequentialGroup()
                .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(jButton2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(outputLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(refTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(refBtn)
                    .addComponent(jLabel95))
                .addGap(9, 9, 9)
                .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel101)
                    .addComponent(thread, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bwaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel96)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel99)
                .addGap(8, 8, 8)
                .addComponent(jLabel98)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(analysisPanelLayout.createSequentialGroup()
                        .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(knownSNPBtn)
                            .addComponent(jLabel97))
                        .addGap(100, 100, 100)
                        .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel93)
                            .addComponent(jLabel103)
                            .addComponent(splitNcigar)))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel102)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel94)
                .addGap(6, 6, 6)
                .addGroup(analysisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(germline)
                    .addComponent(somatic))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(germlinePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(mutect2Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(FilterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        MainPanel.add(analysisPanel);

        visualPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Visualization", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N
        visualPanel.setOpaque(false);
        visualPanel.setPreferredSize(new java.awt.Dimension(1000, 300));

        jLabel5.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("List of Files:");

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jTree1.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane1Tree.setViewportView(jTree1);

        jButton1.setText("View in IGV");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton3.setText("BACK");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout visualPanelLayout = new javax.swing.GroupLayout(visualPanel);
        visualPanel.setLayout(visualPanelLayout);
        visualPanelLayout.setHorizontalGroup(
            visualPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(visualPanelLayout.createSequentialGroup()
                .addGroup(visualPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(visualPanelLayout.createSequentialGroup()
                        .addGap(293, 293, 293)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42)
                        .addComponent(jButton1))
                    .addGroup(visualPanelLayout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1Tree, javax.swing.GroupLayout.PREFERRED_SIZE, 481, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        visualPanelLayout.setVerticalGroup(
            visualPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(visualPanelLayout.createSequentialGroup()
                .addGroup(visualPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton3))
                .addGap(18, 18, 18)
                .addGroup(visualPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jScrollPane1Tree, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        MainPanel.add(visualPanel);

        jScrollPane2.setViewportView(MainPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1404, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1195, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jLabelHelpMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelHelpMouseClicked
        App.helpWebpage("https://gatk.broadinstitute.org/hc/en-us/articles/360035531192-RNAseq-short-variant-discovery-SNPs-Indels-");
    }//GEN-LAST:event_jLabelHelpMouseClicked

    private void jLabelHelpMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelHelpMouseEntered
        jLabelHelp.setIcon(App.icons[1]);
    }//GEN-LAST:event_jLabelHelpMouseEntered

    private void jLabelHelpMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabelHelpMouseExited
        jLabelHelp.setIcon(App.icons[0]);
    }//GEN-LAST:event_jLabelHelpMouseExited

    private void outputBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputBtnActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new java.io.File("."));
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {

            String path = fileChooser.getSelectedFile().toString();

            output_Dir.setText(path + "/");

        }
    }//GEN-LAST:event_outputBtnActionPerformed

    public boolean checkInput() {
        if (output_Dir.getText().isBlank()) {
            warningLabel.setText("**ERROR: The output directory field is empty. Please provide a valid directory path where the output files will be stored.");
            return false;

        }

        if (inputOptions.getSelectedIndex() == 1) {//fastq

            if (singleRadioButton.isSelected()) {

                if (listModelreadSingle.isEmpty()) {
                    warningLabel.setText("**ERROR: Input read(s)/Sample(s) list is empty. Please upload or provide at least one read file to proceed.");

                    return false;
                }
            } else if (pairRadioButton.isSelected()) {

                if ((listModelreadPair1.isEmpty()) || (listModelreadPair2.isEmpty())) {
                    warningLabel.setText("**ERROR: Input read(s)/Sample(s) list is empty. Please upload or provide at least one read file to proceed.");
                    return false;

                } else {

                    if (listModelreadPair1.size() != listModelreadPair2.size()) {
                        warningLabel.setText("**ERROR: The number of reads in Sample Reads 1 and Sample Reads 2 do not match. Please ensure both lists contain an equal number of paired reads.");

                        return false;
                    }

                }
            }

        } else if (inputOptions.getSelectedIndex() == 2) {//bam
            if (listModel.isEmpty()) {
                warningLabel.setText("**ERROR: The input BAM list is empty. Please upload or provide at least one BAM file to continue.");
                return false;
            }

        } else {
            warningLabel.setText("**ERROR: No input option has been selected. Please choose a valid input option to proceed.");
            return false;
        }

        warningLabel.setText("");
        return true; //No error
    }

    public boolean checkAnalysis() {
        if (refTxt.getText().isBlank()) {
            warningLabel.setText("**ERROR: The reference genome file (FASTA) is required. Please provide a valid file path in the text field to proceed.");
            return false;

        }
        //For BWA   
        if (inputOptions.getSelectedIndex() == 1) {
            if (thread.getText().isBlank()) {
                warningLabel.setText("**ERROR: The thread field for BWA-MEM is empty. Please specify the number of threads to optimize performance and proceed.");
                return false;
            }
        }

//Checking READGROUP
        for (int i = 0; i < textFields.size(); i++) {
            if (textFields.get(i).getText().isBlank()) {
                warningLabel.setText("**ERROR: The AddOrReplaceReadGroups fields cannot be empty. Please ensure you enter the necessary details to continue.");
                return false;
            }

        }//end for

        if (knownSites.getModel().getSize() < 1) {
            warningLabel.setText("**ERROR: No known sites have been provided for BaseRecalibrator. Please upload at least one known site file to proceed.");
            return false;
        }

//Germline or somatic Check
        if (!germline.isSelected() && !somatic.isSelected()) {
            warningLabel.setText("**ERROR: No variant calling option selected. Please choose either 'Germline' or 'Somatic' to continue.");
            return false;
        }

        if (somatic.isSelected()) {
            if (mutectCombo.getSelectedIndex() == 0) {
                warningLabel.setText("**ERROR: Please select any option for Mutect2");
                return false;
            } else if (mutectCombo.getSelectedIndex() == 1 || mutectCombo.getSelectedIndex() == 2) {

                if (jListTumor.getModel().getSize() != jListNormal.getModel().getSize()) {
                    warningLabel.setText("**ERROR: Number of Tumor samples & Normal samples must be equal for the selected option. Please check your input.");
                    return false;
                }

                if (germlineResTxt.getText().isBlank()) {
                    warningLabel.setText("**ERROR: Germline resource is mandatory for the selected option. Please provide a valid Germline resource file");
                    return false;
                }

            } else if (mutectCombo.getSelectedIndex() == 3) {
                if (jListNormal.getModel().getSize() == 0) { //Tumor Only

                    if (germlineResTxt.getText().isBlank()) {
                        warningLabel.setText("**ERROR: Germline resource is mandatory for the selected option. Please provide a valid Germline resource file");
                        return false;
                    }

                } else if ((jListTumor.getModel().getSize() != 0) && (jListTumor.getModel().getSize() == jListNormal.getModel().getSize())) {
                    //Tumor with PON mode
                    //Run normal 1st and then use PON 
                    if (germlineResTxt.getText().isBlank()) {
                        warningLabel.setText("**ERROR: Germline resource is mandatory for the selected option. Please provide a valid Germline resource file");

                        return false;
                    }

                } else if (jListTumor.getModel().getSize() != jListNormal.getModel().getSize()) {
                    warningLabel.setText("**ERROR: Tumor-Normal Pairs are not equal");

                    return false;
                }

            } else if (mutectCombo.getSelectedIndex() == 4) {

                if (intervalText.getText().isBlank()) {
                    warningLabel.setText("**ERROR: Genomic Interval(s) are mandatory for the selected option. Please upload or specify genomic interval data.");
                    return false;
                }

            } else if (mutectCombo.getSelectedIndex() == 5) {

                if (alleleTxt.getText().isBlank() && f1r2.getText().isBlank()) {
                    warningLabel.setText("**ERROR: Force Calling requires at least one of the following inputs: Allele file or F1R2 file. Both cannot be empty.");
                    return false;
                }

            }

        }//End Somatic

        if (germline.isSelected()) {

            if (g1Check.isSelected()) {
                if (annotationGroupTxt.getText().isBlank()) {
                    warningLabel.setText("**ERROR: Annotation group list cannot be empty. Please add at least one valid annotation group before proceeding.");
                    return false;
                }
            }
            if (bamoutCheck.isSelected()) {
                if (bamoutTxt.getText().isBlank()) {
                    warningLabel.setText("**ERROR: The BAM output file (BamOut) path is missing. Please provide a valid file path for BAM output to proceed.");
                    return false;
                }

            }
        }//end germline

        if (selectVariantTxt.getText().isBlank()) {
            warningLabel.setText("**ERROR: The filter string for SelectVariants is empty. Please specify a valid filter string to apply the desired criteria.");
            return false;
        }
        if (variantFilterTxtB.getText().isBlank()) {
            warningLabel.setText("**ERROR: The VariantFiltration string is required. Please provide a valid string to proceed with filtering variants.");
            return false;
        }
        if (snpEff_DB.getText().isBlank()) {
            warningLabel.setText("**ERROR: The SnpEff database name is missing. Please enter a valid database name to proceed with annotation.");
            return false;
        }

        warningLabel.setText("");
        return true; //No error
    }

    private void nextBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextBtnActionPerformed

        if (nextBtn.getText().equalsIgnoreCase("next")) {

            if (checkInput()) {

                if (inputOptions.getSelectedIndex() == 1) { //fastq

                    if (singleRadioButton.isSelected()) {

                        // Create a list from the list model
                        List<String> paths = new ArrayList<>();
                        for (int i = 0; i < listModelreadSingle.getSize(); i++) {
                            paths.add(listModelreadSingle.getElementAt(i).toString());
                        }

                        // Put into the map
                        singlePathMap.put("DefaultInput", paths);
                        inputFilesDataS(singlePathMap, "DefaultInput");

                    } else if (pairRadioButton.isSelected()) {
                        // Create lists from both models
                        List<String> pair1 = new ArrayList<>();
                        List<String> pair2 = new ArrayList<>();

                        for (int i = 0; i < listModelreadPair1.getSize(); i++) {
                            pair1.add(listModelreadPair1.getElementAt(i).toString());
                        }
                        for (int i = 0; i < listModelreadPair2.getSize(); i++) {
                            pair2.add(listModelreadPair2.getElementAt(i).toString());
                        }

                        // Create a nested map with "R1" and "R2"
                        Map<String, List<String>> pairMap = new HashMap<>();
                        pairMap.put("R1", pair1);
                        pairMap.put("R2", pair2);

                        // Add to the main map
                        pairPathMap.put("DefaultInput", pairMap);

                        inputFilesDataP(pairPathMap, "DefaultInput");
                    }

                } else if (inputOptions.getSelectedIndex() == 2)//bam
                {
                    // Create a list from the list model
                    List<String> paths = new ArrayList<>();
                    for (int i = 0; i < listModel.getSize(); i++) {
                        paths.add(listModel.getElementAt(i).toString());
                    }

                    // Put into the map
                    singlePathMap.put("DefaultInput", paths);
                    inputFilesDataS(singlePathMap, "DefaultInput");

                }
                selectedKey = "DefaultInput";
                inputPanel.setVisible(false);
                analysisPanel.setVisible(true);
                nextBtn.setText("RUN PIPELINE");

            }
        } else if (nextBtn.getText().equalsIgnoreCase("Proceed to Quality Check")) {
            if (checkInput()) {

                if (inputOptions.getSelectedIndex() == 1) { //fastq

                    if (singleRadioButton.isSelected()) {

                        // Create a list from the list model
                        List<String> paths = new ArrayList<>();
                        for (int i = 0; i < listModelreadSingle.getSize(); i++) {
                            paths.add(listModelreadSingle.getElementAt(i).toString());
                        }

                        // Put into the map
                        singlePathMap.put("DefaultInput", paths);
                        inputFilesDataS(singlePathMap, "DefaultInput");

                    } else if (pairRadioButton.isSelected()) {
                        // Create lists from both models
                        List<String> pair1 = new ArrayList<>();
                        List<String> pair2 = new ArrayList<>();

                        for (int i = 0; i < listModelreadPair1.getSize(); i++) {
                            pair1.add(listModelreadPair1.getElementAt(i).toString());
                        }
                        for (int i = 0; i < listModelreadPair2.getSize(); i++) {
                            pair2.add(listModelreadPair2.getElementAt(i).toString());
                        }

                        // Create a nested map with "R1" and "R2"
                        Map<String, List<String>> pairMap = new HashMap<>();
                        pairMap.put("R1", pair1);
                        pairMap.put("R2", pair2);

                        // Add to the main map
                        pairPathMap.put("DefaultInput", pairMap);

                        inputFilesDataP(pairPathMap, "DefaultInput");
                    }

                }

                if (fastpRadio.isSelected()) {
                    fastCounter = 0;
                    filesForComboP = new ArrayList<>();
                    runFastP("DefaultInput");

                } else if (fastqcRadio.isSelected()) {
                    fastCounter = 0;

                    runFastqc("DefaultInput");
                }
            }

        } else if (nextBtn.getText().equalsIgnoreCase("Continue Trimming & QC Refinement")) {

            if (fastpRadio.isSelected()) {

                if (inputOptions.getSelectedIndex() == 1) {
                    if (singleRadioButton.isSelected()) {
                        inputFilesDataS(singlePathMap, "FastP" + fastCounter);

                    } else if (pairRadioButton.isSelected()) {

                        inputFilesDataP(pairPathMap, "FastP" + fastCounter);

                    }

                }
                runFastP("FastP" + fastCounter);

            } else if (fastqcRadio.isSelected()) {

                //Check trimmomatic inputs
                if ((threadC.isSelected() == true && thread.getText().isBlank())
                        || adapterfile.getText().isBlank()
                        || illumniaclip.getText().isBlank()
                        || (leadingC.isSelected() && leading.getText().isBlank())
                        || (trailingC.isSelected() && trailing.getText().isBlank())
                        || (slidingC.isSelected() && sliding.getText().isBlank())
                        || (minlenC.isSelected() && minlen.getText().isBlank())
                        || (maxinfoC.isSelected() && maxinfo.getText().isBlank())
                        || (cropC.isSelected() && crop.getText().isBlank())
                        || (headcropC.isSelected() && headcrop.getText().isBlank())) {
                    warningLabel.setText("**ERROR: Fill all fields");

                } else {
                    warningLabel.setText("");

                    if (fastCounter == 0) {
                        runTrimommatic("DefaultInput");
                    } else {
                        runTrimommatic("FastQC" + fastCounter);
                    }
                }

            }

        } else if (nextBtn.getText().equalsIgnoreCase("Proceed to Run Pipeline")) {

            if (checkInput()) {

                if (inputOptions.getSelectedIndex() == 1) {

                    if (singleRadioButton.isSelected()) {

                        inputDialog = new InputPathDialog(parentFrame, closable, singlePathMap);
                        inputDialog.setVisible(true);
                        selectedKey = inputDialog.getSelectedKey();

                    } else if (pairRadioButton.isSelected()) {
                        inputDialog = new InputPathDialog(parentFrame, closable, pairPathMap, true);
                        inputDialog.setModal(true);
                        inputDialog.setVisible(true);
                        selectedKey = inputDialog.getSelectedKey();

                    }

                } else if (inputOptions.getSelectedIndex() == 2) {
                    inputDialog = new InputPathDialog(parentFrame, closable, singlePathMap);
                    inputDialog.setVisible(true);
                    selectedKey = inputDialog.getSelectedKey();

                }

                QualityControlPanel.setVisible(false);
                analysisPanel.setVisible(true);
                nextBtn.setText("Run Pipeline");

            }//check Input

        } else if (nextBtn.getText().equalsIgnoreCase("Run Pipeline")) {
            if (checkAnalysis()) {

                if (inputOptions.getSelectedIndex() == 1) {
                    if (singleRadioButton.isSelected()) {
                        inputFilesDataS(singlePathMap, selectedKey);
                    } else if (pairRadioButton.isSelected()) {
                        inputFilesDataP(pairPathMap, selectedKey);
                    }

                    //  runFastq_Variant();
                    runBam_Variant();
                } else if (inputOptions.getSelectedIndex() == 2) {
                    inputFilesDataS(singlePathMap, selectedKey);

                    runBam_Variant();
                }

            }//analysisIf

        }


    }//GEN-LAST:event_nextBtnActionPerformed

    private void refBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refBtnActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Fasta Files (*.fa)", "fa"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Fasta Files (*.fasta)", "fasta"));
        fileChooser.setAcceptAllFileFilterUsed(true);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {

                refTxt.setText(fileChooser.getSelectedFile().toString());

            }
        }
    }//GEN-LAST:event_refBtnActionPerformed

    private void knownSNPBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_knownSNPBtnActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("VCF Files (*.vcf)", "vcf"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("VCF GZ Files (*.vcf.gz)", "gz"));

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setAcceptAllFileFilterUsed(true);

        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {

            if (fileChooser.getSelectedFiles() != null) {
                allFiles = fileChooser.getSelectedFiles();//File[] allfiles

                for (int i = 0; i < allFiles.length; i++) {
                    listKnownSites.addElement(allFiles[i].getAbsolutePath());
                }

            }
        }


    }//GEN-LAST:event_knownSNPBtnActionPerformed

    private void somaticItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_somaticItemStateChanged
//DEFAULT SOMATIC SETTINGS
        somaticOP = "";

        germlineCheck.setSelected(false);
        germlineResTxt.setText("");
        germlineResBtn.setEnabled(false);

        ponCheck.setSelected(false);
        ponTxt.setText("");
        ponBtn.setEnabled(false);
        generatePON.setSelected(false);

        allelesCheck.setSelected(false);
        alleleTxt.setText("");
        alleleBtn.setEnabled(false);

        f1r2Check.setSelected(false);
        f1r2.setText("");
        f1r2Btn.setEnabled(false);

        intervalCheck.setSelected(false);
        intervalT.setText("");
        intervalT.setEditable(false);
        intervalBtn.setEnabled(false);
        add6.setEnabled(false);
        del6.setEnabled(false);
        intervalText.setText("");

        listNormal.removeAllElements();

        mutectCombo.setSelectedIndex(0);
//ON SELECT
        if (somatic.isSelected()) {

            mutect2Panel.setVisible(true);

        } else {

            mutect2Panel.setVisible(false);

        }
    }//GEN-LAST:event_somaticItemStateChanged

    private void bamoutBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bamoutBActionPerformed
        bamoutTxt.setText(App.outputFile(new FileNameExtensionFilter("BAM Files (*.bam)", "bam"), ".bam"));
    }//GEN-LAST:event_bamoutBActionPerformed

    private void germlineItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_germlineItemStateChanged
//DEFAULT GERMLINE SETTINGS
        singleModeH.setSelected(true);
        ercCombo.setSelectedIndex(0);

        g1Check.setSelected(false);
        annotationGroupCombo.setEditable(false);
        add7.setEnabled(false);
        del7.setEnabled(false);

        annotationGroupTxt.setText("");

        bamoutCheck.setSelected(false);
        bamoutTxt.setText("");
//ON SELECT        
        if (germline.isSelected()) {

            germlinePanel.setVisible(true);
        } else {
            germlinePanel.setVisible(false);

        }

    }//GEN-LAST:event_germlineItemStateChanged

    private void jLabel25MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel25MouseClicked
        warningLabel.setText(null);
        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));

        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

            @Override
            protected Integer doInBackground() {
                Process p;
                try {

                    String cmdList = "java -jar " + App.SNPEFF_PATH + " databases |grep -i \"human genome\"  | cut -f1";

                    display("VariantCommandString", cmdList);
                    ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmdList);
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
                    App.LOGGER.error("\r\n ERROR in analysis: " + App.stack);
                    App.textArea.append("\r\n ERROR in analysis: " + App.stack);

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

            }//done

        };

        worker.execute();
        //}//else


    }//GEN-LAST:event_jLabel25MouseClicked

    private void alleleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alleleBtnActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("VCF Files (*.vcf)", "vcf"));
        fileChooser.setAcceptAllFileFilterUsed(true);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {

                alleleTxt.setText(fileChooser.getSelectedFile().toString());

            }
        }
    }//GEN-LAST:event_alleleBtnActionPerformed

    private void ponBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ponBtnActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("BED Files (*.bed)", "bed"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("VCF GZ Files (*.vcf.gz)", "vcf.gz"));
        fileChooser.setAcceptAllFileFilterUsed(true);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {

                ponTxt.setText(fileChooser.getSelectedFile().toString());
                generatePON.setSelected(false);
            }
        }
    }//GEN-LAST:event_ponBtnActionPerformed

    private void germlineResBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_germlineResBtnActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("VCF Files (*.vcf)", "vcf"));

        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("VCF GZ Files (*.vcf.gz)", "gz"));
        fileChooser.setAcceptAllFileFilterUsed(true);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {

                germlineResTxt.setText(fileChooser.getSelectedFile().toString());

            }
        }
    }//GEN-LAST:event_germlineResBtnActionPerformed

    private void tumNorBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tumNorBtnActionPerformed

        List<String> selectedItems = jListTumor.getSelectedValuesList();

        for (String item : selectedItems) {
            listNormal.addElement(item);
            listTumor.removeElement(item);
        }


    }//GEN-LAST:event_tumNorBtnActionPerformed

    private void intervalBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_intervalBtnActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("BED Files (*.bed)", "bed"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Interval List (*.interval_list)", "interval_list"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("VCF Files (*.vcf)", "vcf"));
        fileChooser.setAcceptAllFileFilterUsed(true);

        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {

                intervalT.setText(fileChooser.getSelectedFile().toString());

            }
        }


    }//GEN-LAST:event_intervalBtnActionPerformed

    private void f1r2BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f1r2BtnActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("TAR GZ Files (*.tar.gz)", "gz"));
        fileChooser.setAcceptAllFileFilterUsed(true);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {

                f1r2.setText(fileChooser.getSelectedFile().toString());

            }
        }
    }//GEN-LAST:event_f1r2BtnActionPerformed

    public void fillTumorList() {
        listTumor.removeAllElements();
        if (inputOptions.getSelectedIndex() == 1) {
            if (singleRadioButton.isSelected()) {
                for (int i = 0; i < readFilesInSingle.getModel().getSize(); i++) {
                    listTumor.addElement(sfileNames.get(i));
                }
            } else {
                for (int i = 0; i < readFilesInPair1.getModel().getSize(); i++) {
                    // listTumor.addElement(p1fileNames.get(i) + "_" + p2fileNames.get(i));
                    listTumor.addElement(p1fileNames.get(i));

                }
            }
        } else if (inputOptions.getSelectedIndex() == 2) {
            //Reading just file name from the list
            for (int i = 0; i < inputBamTxt.getModel().getSize(); i++) {
                Path path = Paths.get(inputBamTxt.getModel().getElementAt(i));
                listTumor.addElement(StringUtils.chop(path.getFileName().toString().replaceAll("\\.(?![^.]+$)", "")));
            }
        }
    }

    private void mutectComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_mutectComboItemStateChanged

        //Default TextF
        germlineResTxt.setText("");
        alleleTxt.setText("");
        ponTxt.setText("");
        intervalT.setText("");
        intervalText.setText("");
        f1r2.setText("");

//Default Panel
        normalPanel.setVisible(true);
        otherPanel.setVisible(true);

        Label1.setText("");
        Label2.setText("");
        Label3.setText("");
        Label4.setText("");
        Label5.setText("");
//Default List Setting
        listNormal.removeAllElements();
        fillTumorList();

        if (mutectCombo.getSelectedIndex() == 0) //SELECT
        {
            somaticOP = "";

            normalPanel.setVisible(false);
            otherPanel.setVisible(false);

        } else if ((mutectCombo.getSelectedIndex() == 1) || (mutectCombo.getSelectedIndex() == 2)) //Tumor with matched normal (single)
        {

            if (mutectCombo.getSelectedIndex() == 1) {

                somaticOP = "TUMORN";
            } else if (mutectCombo.getSelectedIndex() == 2) {
                somaticOP = "TUMORM";
            }

            Label1.setText("NOTE:  Ensure the sequence of tumor-normal pairs is correct. The first tumor matches the first normal, the second with the second, and so on.");

        } else if (mutectCombo.getSelectedIndex() == 3) //Tumor Only mode
        {

            Label1.setText("Execution Modes:");
            Label2.setText("Tumor-Only Mode: If all BAM files are in the Tumor Samples list, they will be processed independently to detect somatic variants with uploaded Panel of Normals (PoN).");
            Label3.setText("Tumor with PoN Mode: If you designate files in both Tumor and Normal lists:");
            Label4.setText("1-The normal samples will be processed first to create a Panel of Normals (PoN).");
            Label5.setText("2-This PoN will then be used to filter artifacts from the tumor samples, improving the accuracy of somatic variant detection.");

            if (jListNormal.getModel().getSize() == 0) {
                somaticOP = "TUMORO";
            } else if (jListTumor.getModel().getSize() == 0) {
                somaticOP = "TUMORON";
            } else {
                somaticOP = "TUMOROPON";
            }

        } else if (mutectCombo.getSelectedIndex() == 4) //Mitochondrial mode
        {
            somaticOP = "TUMORMIT";

        } else if (mutectCombo.getSelectedIndex() == 5) //Force-calling mode
        {

            somaticOP = "TUMORFOR";

        }


    }//GEN-LAST:event_mutectComboItemStateChanged

    private void bamoutCheckItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_bamoutCheckItemStateChanged
        bamoutTxt.setText("");        // TODO add your handling code here:
    }//GEN-LAST:event_bamoutCheckItemStateChanged

    private void singleRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_singleRadioButtonActionPerformed

        //Single read ON
        jButton6.setEnabled(true);
        //fileNameWithoutExtension.clear();
        //Pair read OFF
        listModelreadPair1.clear();
        listModelreadPair2.clear();
        jButton7.setEnabled(false);
        jButton8.setEnabled(false);
    }//GEN-LAST:event_singleRadioButtonActionPerformed

    private void pairRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pairRadioButtonActionPerformed

        //Single read OFF
        jButton6.setEnabled(false);
        listModelreadSingle.clear();
        //fileNameWithoutExtension.clear();
        //Pair read ON
        //listModelreadPair1.clear();listModelreadPair2.clear();
        jButton7.setEnabled(true);
        jButton8.setEnabled(true);
    }//GEN-LAST:event_pairRadioButtonActionPerformed

    private void readFilesInPair1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_readFilesInPair1KeyPressed
        if (evt.getKeyChar() == KeyEvent.VK_DELETE) {
            listModelreadPair1.remove(readFilesInPair1.getSelectedIndex());

        }
    }//GEN-LAST:event_readFilesInPair1KeyPressed

    private void readFilesInSingleKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_readFilesInSingleKeyPressed
        if (evt.getKeyChar() == KeyEvent.VK_DELETE) {
            listModelreadSingle.remove(readFilesInSingle.getSelectedIndex());

        }
    }//GEN-LAST:event_readFilesInSingleKeyPressed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Fastq Files (*.gz)", "gz"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Fastq Files (*.bz2)", "bz2"));

        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Fastq Files (*.fq)", "fq"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Fastq Files (*.fastq)", "fastq"));

        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Fasta Files (*.fa)", "fa"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Fasta Files (*.fasta)", "fasta"));

        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setAcceptAllFileFilterUsed(true);

        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {

            if (fileChooser.getSelectedFiles() != null) {
                allFiles = fileChooser.getSelectedFiles();//File[] allfiles

                for (int i = 0; i < allFiles.length; i++) {
                    listModelreadSingle.addElement(" \"" + allFiles[i].getAbsolutePath() + "\"");

                }

            }
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Add custom file filters
        fileChooser.addChoosableFileFilter(createFileFilter(
                "BZIP2 Fastq Files (*.fq.bz2, *.fastq.bz2)", new String[]{".fq.bz2", ".fastq.bz2"}));
        fileChooser.addChoosableFileFilter(createFileFilter(
                "GZIP Fastq Files (*.fq.gz, *.fastq.gz)", new String[]{".fq.gz", ".fastq.gz"}));
        fileChooser.addChoosableFileFilter(createFileFilter(
                "Fastq Files (*.fq, *.fastq)", new String[]{".fq", ".fastq"}));
        fileChooser.addChoosableFileFilter(createFileFilter(
                "Fasta Files (*.fa, *.fasta)", new String[]{".fa", ".fasta"}));

        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setAcceptAllFileFilterUsed(true);
        /// Apply custom filter using a method
        fileChooser.addChoosableFileFilter((javax.swing.filechooser.FileFilter) createFastqFilter());
        fileChooser.addChoosableFileFilter((javax.swing.filechooser.FileFilter) createFastqFilter1());

        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {

            if (fileChooser.getSelectedFiles() != null) {
                allFiles = fileChooser.getSelectedFiles();//File[] allfiles

                for (int i = 0; i < allFiles.length; i++) {
                    listModelreadPair1.addElement(" \"" + allFiles[i].getAbsolutePath() + "\"");

                }

            }
        }
    }//GEN-LAST:event_jButton7ActionPerformed
    private javax.swing.filechooser.FileFilter createFastqFilter() {
        return new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true; // Show directories
                }
                // Only allow files ending with _1 before extension
                String name = file.getName();
                return name.matches(".*_1(\\.fastq|\\.fq|\\.fastq\\.gz|\\.fq\\.gz|\\.fastq\\.bz2|\\.fq\\.bz2)$");
            }

            public String getDescription() {
                return "Paired-end Read 1 Files (*.fq, *.fastq, *.fq.gz, *.fastq.gz, *.bz2)";
            }
        };
    }

    private javax.swing.filechooser.FileFilter createFastqFilter1() {
        return new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true; // Show directories
                }
                // Only allow files ending with _1 before extension
                String name = file.getName();
                return name.matches(".*_1.*(\\.fastq|\\.fq|\\.fastq\\.gz|\\.fq\\.gz|\\.fastq\\.bz2|\\.fq\\.bz2)$");
            }

            public String getDescription() {
                return "Paired-end Read 1 Files (*.fq, *.fastq, *.fq.gz, *.fastq.gz, *.bz2)";
            }
        };
    }

    private void readFilesInPair2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_readFilesInPair2KeyPressed
        if (evt.getKeyChar() == KeyEvent.VK_DELETE) {
            listModelreadPair2.remove(readFilesInPair2.getSelectedIndex());

        }
    }//GEN-LAST:event_readFilesInPair2KeyPressed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // Add custom file filters
        fileChooser.addChoosableFileFilter(createFileFilter(
                "BZIP2 Fastq Files (*.fq.bz2, *.fastq.bz2)", new String[]{".fq.bz2", ".fastq.bz2"}));
        fileChooser.addChoosableFileFilter(createFileFilter(
                "GZIP Fastq Files (*.fq.gz, *.fastq.gz)", new String[]{".fq.gz", ".fastq.gz"}));
        fileChooser.addChoosableFileFilter(createFileFilter(
                "Fastq Files (*.fq, *.fastq)", new String[]{".fq", ".fastq"}));
        fileChooser.addChoosableFileFilter(createFileFilter(
                "Fasta Files (*.fa, *.fasta)", new String[]{".fa", ".fasta"}));

        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.addChoosableFileFilter((javax.swing.filechooser.FileFilter) createFastqFilter2());
        fileChooser.addChoosableFileFilter((javax.swing.filechooser.FileFilter) createFastqFilter4());

        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {

            if (fileChooser.getSelectedFiles() != null) {
                allFiles = fileChooser.getSelectedFiles();//File[] allfiles

                for (int i = 0; i < allFiles.length; i++) {
                    listModelreadPair2.addElement(" \"" + allFiles[i].getAbsolutePath() + "\"");

                }

            }
        }

    }//GEN-LAST:event_jButton8ActionPerformed
    // Method to create a custom FileFilter
    public javax.swing.filechooser.FileFilter createFileFilter(String description, String[] extensions) {
        return new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true; // Allow directories for navigation
                }
                String fileName = file.getName().toLowerCase();
                for (String ext : extensions) {
                    if (fileName.endsWith(ext)) {
                        return true; // Match valid extensions
                    }
                }
                return false; // Reject other files
            }

            @Override
            public String getDescription() {
                return description;
            }
        };
    }
// Method to create a FileFilter for "_1.extension" files

    private javax.swing.filechooser.FileFilter createFastqFilter2() {
        return new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true; // Show directories
                }
                // Only allow files ending with _1 before extension
                String name = file.getName();
                return name.matches(".*_2(\\.fastq|\\.fq|\\.fastq\\.gz|\\.fq\\.gz|\\.fastq\\.bz2|\\.fq\\.bz2)$");
            }

            public String getDescription() {
                return "Paired-end Read 2 Files (*.fq, *.fastq, *.fq.gz, *.fastq.gz, *.bz2)";
            }
        };
    }
    // Method to create a FileFilter for "_1.extension" files

    private javax.swing.filechooser.FileFilter createFastqFilter4() {
        return new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true; // Show directories
                }
                // Only allow files ending with _1 before extension
                String name = file.getName();
                return name.matches(".*_2.*(\\.fastq|\\.fq|\\.fastq\\.gz|\\.fq\\.gz|\\.fastq\\.bz2|\\.fq\\.bz2)$");
            }

            public String getDescription() {
                return "Paired-end Read 2 Files (*.fq, *.fastq, *.fq.gz, *.fastq.gz, *.bz2)";
            }
        };
    }

    void HCC_WES() {

        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972531_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972532_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972533_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972534_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972535_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972536_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972537_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972538_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972539_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972540_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972541_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972542_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972543_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972544_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972545_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972546_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972547_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972548_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972549_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972550_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972551_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972552_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972553_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972554_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972555_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972556_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972557_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972558_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972559_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972560_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972561_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972562_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972563_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972564_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972565_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972566_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972567_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972568_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972569_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972570_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972571_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972572_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972573_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972574_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972575_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972576_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972577_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972578_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972579_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972580_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972581_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972582_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972583_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972584_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972585_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972586_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972587_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972588_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972589_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972590_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972591_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972592_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972593_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972594_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972595_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972596_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972597_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972598_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972599_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972600_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972601_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972602_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972603_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972604_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972605_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972606_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972607_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972608_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972609_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972610_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972611_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972612_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972613_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972614_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972615_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972616_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972617_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972618_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972619_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972620_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972621_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972622_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972623_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972624_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972625_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972626_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972627_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972628_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972629_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972630_1.fastq.gz\"");

        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972531_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972532_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972533_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972534_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972535_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972536_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972537_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972538_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972539_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972540_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972541_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972542_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972543_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972544_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972545_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972546_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972547_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972548_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972549_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972550_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972551_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972552_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972553_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972554_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972555_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972556_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972557_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972558_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972559_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972560_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972561_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972562_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972563_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972564_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972565_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972566_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972567_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972568_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972569_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972570_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972571_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972572_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972573_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972574_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972575_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972576_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972577_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972578_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972579_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972580_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972581_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972582_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972583_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972584_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972585_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972586_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972587_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972588_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972589_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972590_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972591_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972592_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972593_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972594_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972595_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972596_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972597_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972598_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972599_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972600_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972601_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972602_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972603_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972604_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972605_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972606_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972607_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972608_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972609_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972610_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972611_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972612_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972613_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972614_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972615_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972616_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972617_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972618_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972619_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972620_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972621_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972622_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972623_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972624_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972625_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972626_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972627_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972628_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972629_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/home/iffy/HCC/InputFiles/trimmed/SRR20972630_2.fastq.gz\"");

    }//HCC_WES

    public void HCC_RNASeq() {
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972486.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972434.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972487.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972431.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972494.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972496.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972505.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972480.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972461.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972440.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972450.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972426.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972446.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972414.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972419.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972412.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972428.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972427.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972484.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972475.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972443.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972410.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972478.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972490.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972433.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972506.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972465.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972477.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972476.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972442.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972454.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972416.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972421.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972408.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972474.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972479.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972482.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972435.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972504.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972432.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972413.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972409.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972462.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972473.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972467.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972458.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972415.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972471.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972502.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972472.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972448.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972464.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972449.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972488.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972445.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972436.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972483.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972430.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972417.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972453.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972501.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972420.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972451.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972447.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972457.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972411.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972459.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972422.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972452.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972444.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972456.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972429.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972441.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972466.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972407.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972493.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972499.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972469.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972485.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972438.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972489.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972460.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972495.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972439.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972481.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972497.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972498.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972437.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972492.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972425.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972468.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972500.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972418.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972423.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972503.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972463.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972424.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972470.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972491.star.Aligned.sortedByCoord.out.bam\"");
        listModel.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/SRR20972455.star.Aligned.sortedByCoord.out.bam\"");
    }

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked

        //  output_Dir.setText("/media/nigab/sdc1/Wajya/Iffat/Output_WESsNew");
        output_Dir.setText("/home/iffy/PhD_Data/CASESTUDY/1-Hepatocellular_carcinoma_100/WES_PRJNA866195/VCF_WESsNew/");
        HCC_WES();
//output_Dir.setText("/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/VariantCalling_RNASeq1");
//HCC_RNASeq();
    }//GEN-LAST:event_jLabel2MouseClicked

    private void inputBamTxtKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_inputBamTxtKeyPressed
        if (evt.getKeyChar() == KeyEvent.VK_DELETE) {
            listModel.remove(inputBamTxt.getSelectedIndex());

        }
    }//GEN-LAST:event_inputBamTxtKeyPressed

    private void inputbamBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputbamBtnActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("BAM Files (*.bam)", "bam"));

        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setAcceptAllFileFilterUsed(true);

        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {

            if (fileChooser.getSelectedFiles() != null) {
                allFiles = fileChooser.getSelectedFiles();//File[] allfiles

                for (int i = 0; i < allFiles.length; i++) {
                    listModel.addElement(" \"" + allFiles[i].getAbsolutePath() + "\"");
                }

            }
        }


    }//GEN-LAST:event_inputbamBtnActionPerformed

    private void inputOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputOptionsActionPerformed

        if (inputOptions.getSelectedIndex() == 1) {
            byFastq.setVisible(true);
            byBam.setVisible(false);
            bwaPanel.setVisible(true);

            listModel.clear();

        } else if (inputOptions.getSelectedIndex() == 2) {
            byFastq.setVisible(false);
            byBam.setVisible(true);
            bwaPanel.setVisible(false);

            listModelreadSingle.clear();
            listModelreadPair1.clear();
            listModelreadPair2.clear();
        } else {
            byFastq.setVisible(false);
            byBam.setVisible(false);

        }
    }//GEN-LAST:event_inputOptionsActionPerformed

    private void jLabel34MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel34MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel34MouseClicked

    private void jCheckBox5ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox5ItemStateChanged
        if (jCheckBox5.isSelected()) {
            selectTypeCombo.setEnabled(true);
            add1.setEnabled(true);
            del1.setEnabled(true);
        } else {
            selectTypeCombo.setEnabled(false);
            add1.setEnabled(false);
            del1.setEnabled(false);
        }
        if (!jCheckBox5.isSelected() && !jCheckBox6.isSelected()
                && !jCheckBox7.isSelected() && !jCheckBox8.isSelected()) {
            selectVariantTxt.setText("");
        }
    }//GEN-LAST:event_jCheckBox5ItemStateChanged

    private void jCheckBox6ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox6ItemStateChanged
        if (jCheckBox6.isSelected()) {
            select.setEditable(true);
            add2.setEnabled(true);
            del2.setEnabled(true);
        } else {
            select.setText(null);
            select.setEditable(false);
            add2.setEnabled(false);
            del2.setEnabled(false);

        }

        if (!jCheckBox5.isSelected() && !jCheckBox6.isSelected()
                && !jCheckBox7.isSelected() && !jCheckBox8.isSelected()) {
            selectVariantTxt.setText("");
        }
    }//GEN-LAST:event_jCheckBox6ItemStateChanged

    private void jCheckBox7ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox7ItemStateChanged
        if (jCheckBox7.isSelected()) {
            selectGenotype.setEditable(true);
            add3.setEnabled(true);
            del3.setEnabled(true);
        } else {
            selectGenotype.setText(null);
            selectGenotype.setEditable(false);
            add3.setEnabled(false);
            del3.setEnabled(false);
        }

        if (!jCheckBox5.isSelected() && !jCheckBox6.isSelected()
                && !jCheckBox7.isSelected() && !jCheckBox8.isSelected()) {
            selectVariantTxt.setText("");
        }
    }//GEN-LAST:event_jCheckBox7ItemStateChanged

    private void jCheckBox8ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBox8ItemStateChanged
        if (jCheckBox8.isSelected()) {
            Linterval.setEditable(true);
            add4.setEnabled(true);
            del4.setEnabled(true);
        } else {
            Linterval.setText(null);
            Linterval.setEditable(false);
            add4.setEnabled(false);
            del4.setEnabled(false);
        }
        if (!jCheckBox5.isSelected() && !jCheckBox6.isSelected()
                && !jCheckBox7.isSelected() && !jCheckBox8.isSelected()) {
            selectVariantTxt.setText("");
        }
    }//GEN-LAST:event_jCheckBox8ItemStateChanged

    private void norTumBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_norTumBtnActionPerformed

        List<String> selectedItems = jListNormal.getSelectedValuesList();

        for (String item : selectedItems) {
            listTumor.addElement(item);
            listNormal.removeElement(item);
        }

    }//GEN-LAST:event_norTumBtnActionPerformed

    private void add1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add1ActionPerformed

        String txtContent = selectVariantTxt.getText();
        String txtAdd = "--select-type-to-include " + selectTypeCombo.getSelectedItem().toString() + " ";
        if (!txtContent.contains(txtAdd)) {
            selectVariantTxt.append(txtAdd);
        }

    }//GEN-LAST:event_add1ActionPerformed

    private void del1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_del1ActionPerformed
        String txtContent = selectVariantTxt.getText();
        String txtDel = "--select-type-to-include " + selectTypeCombo.getSelectedItem().toString() + " ";
        if (txtContent.contains(txtDel)) {
            txtContent = txtContent.replace(txtDel, "");
            selectVariantTxt.setText(txtContent);
        }


    }//GEN-LAST:event_del1ActionPerformed

    private void add2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add2ActionPerformed
        String txtContent = selectVariantTxt.getText();
        if (!select.getText().isBlank()) {
            String txtAdd = "-select " + select.getText().toUpperCase() + " ";
            if (!txtContent.contains(txtAdd)) {
                selectVariantTxt.append(txtAdd);
            }
        }

    }//GEN-LAST:event_add2ActionPerformed

    private void del2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_del2ActionPerformed
        String txtContent = selectVariantTxt.getText();
        if (!select.getText().isBlank()) {
            String txtDel = "-select " + select.getText().toUpperCase() + " ";
            if (txtContent.contains(txtDel)) {
                txtContent = txtContent.replace(txtDel, "");
                selectVariantTxt.setText(txtContent);
            }
        }


    }//GEN-LAST:event_del2ActionPerformed

    private void add3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add3ActionPerformed
        String txtContent = selectVariantTxt.getText();
        if (!selectGenotype.getText().isBlank()) {
            String txtAdd = "-select-genotype " + selectGenotype.getText().toUpperCase() + " ";
            if (!txtContent.contains(txtAdd)) {
                selectVariantTxt.append(txtAdd);
            }
        }
    }//GEN-LAST:event_add3ActionPerformed

    private void del3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_del3ActionPerformed
        String txtContent = selectVariantTxt.getText();
        if (!selectGenotype.getText().isBlank()) {
            String txtDel = "-select-genotype " + selectGenotype.getText().toUpperCase() + " ";
            if (txtContent.contains(txtDel)) {
                txtContent = txtContent.replace(txtDel, "");
                selectVariantTxt.setText(txtContent);
            }
        }

    }//GEN-LAST:event_del3ActionPerformed

    private void add4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add4ActionPerformed
        String txtContent = selectVariantTxt.getText();
        if (!Linterval.getText().isBlank()) {
            String txtAdd = "-L " + Linterval.getText().toLowerCase() + " ";
            if (!txtContent.contains(txtAdd)) {
                selectVariantTxt.append(txtAdd);
            }
        }
    }//GEN-LAST:event_add4ActionPerformed

    private void del4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_del4ActionPerformed
        String txtContent = selectVariantTxt.getText();
        if (!Linterval.getText().isBlank()) {
            String txtDel = "-L " + Linterval.getText().toLowerCase() + " ";
            if (txtContent.contains(txtDel)) {
                txtContent = txtContent.replace(txtDel, "");
                selectVariantTxt.setText(txtContent);
            }
        }
    }//GEN-LAST:event_del4ActionPerformed

    private void add5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add5ActionPerformed
        String txtContent = variantFilterTxtB.getText();
        if (!filterNameB.getText().isBlank() && !filterExprB.getText().isBlank()) {
            String txtAdd = "--filter-name \"" + filterNameB.getText().toLowerCase() + "\" " + "--filter-expression \"" + filterExprB.getText().toUpperCase() + "\" ";
            if (!txtContent.contains(txtAdd)) {
                variantFilterTxtB.append(txtAdd);
            }
        }

    }//GEN-LAST:event_add5ActionPerformed

    private void del5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_del5ActionPerformed
        String txtContent = variantFilterTxtB.getText();
        if (!filterNameB.getText().isBlank() && !filterExprB.getText().isBlank()) {
            String txtDel = "--filter-name \"" + filterNameB.getText().toLowerCase() + "\" " + "--filter-expression \"" + filterExprB.getText().toUpperCase() + "\" ";
            if (txtContent.contains(txtDel)) {
                txtContent = txtContent.replace(txtDel, "");
                variantFilterTxtB.setText(txtContent);
            }
        }
    }//GEN-LAST:event_del5ActionPerformed

    private void add6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add6ActionPerformed
        String txtContent = intervalText.getText();
        if (!intervalT.getText().isBlank()) {
            String txtAdd = "-L " + intervalT.getText().toLowerCase() + " ";
            if (!txtContent.contains(txtAdd)) {
                intervalText.append(txtAdd);
            }
        }
    }//GEN-LAST:event_add6ActionPerformed

    private void del6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_del6ActionPerformed
        String txtContent = intervalText.getText();
        if (!intervalT.getText().isBlank()) {
            String txtDel = "-L " + intervalT.getText().toLowerCase() + " ";
            if (!txtContent.contains(txtDel)) {
                txtContent = txtContent.replace(txtDel, "");
                intervalText.append(txtDel);
            }

        }
    }//GEN-LAST:event_del6ActionPerformed

    private void intervalBtn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_intervalBtn1ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("BED Files (*.bed)", "bed"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Interval List (*.interval_list)", "interval_list"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("VCF Files (*.vcf)", "vcf"));
        fileChooser.setAcceptAllFileFilterUsed(true);

        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {

                Linterval.setText(fileChooser.getSelectedFile().toString());

            }
        }

    }//GEN-LAST:event_intervalBtn1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jTree1.getLastSelectedPathComponent();
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, "Please select a file to view.", "No File Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedFilePath = output_Dir.getText() + selectedNode.getUserObject().toString();

        // Add code here to handle the file (e.g., open in IGV)
        ProcessBuilder pb = new ProcessBuilder(App.IGV_PATH, "-g", selectedFilePath);
        try {
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle error (e.g., IGV not found)
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        analysisPanel.setVisible(false);
        nextBtn.setText("NEXT");
        inputPanel.setVisible(true);

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        visualPanel.setVisible(false);
        nextBtn.setText("RUN PIPELINE");
        analysisPanel.setVisible(true);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jListTumorValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListTumorValueChanged
        if (!evt.getValueIsAdjusting()) {
            jListNormal.clearSelection();
        }
    }//GEN-LAST:event_jListTumorValueChanged

    private void jListNormalValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListNormalValueChanged
        if (!evt.getValueIsAdjusting()) {
            jListTumor.clearSelection();
        }
    }//GEN-LAST:event_jListNormalValueChanged

    private void generatePONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generatePONActionPerformed
        if (generatePON.isSelected()) {
            ponTxt.setText("");
            ponBtn.setEnabled(false);
            ponCheck.setSelected(false);
        }
    }//GEN-LAST:event_generatePONActionPerformed

    private void add7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add7ActionPerformed
        String txtContent = annotationGroupTxt.getText();
        String comboTxt = annotationGroupCombo.getSelectedItem().toString();
        if (!comboTxt.isBlank()) {
            int index = comboTxt.indexOf("(");

            if (index > 0) {
                comboTxt = comboTxt.substring(0, index).trim();
            }

            String txtAdd = "-G " + comboTxt.toUpperCase() + " ";
            if (!txtContent.contains(txtAdd)) {
                annotationGroupTxt.append(txtAdd);
            }
        }
    }//GEN-LAST:event_add7ActionPerformed

    private void del7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_del7ActionPerformed
        String txtContent = annotationGroupTxt.getText();
        String comboTxt = annotationGroupCombo.getSelectedItem().toString();
        if (!comboTxt.isBlank()) {
            int index = comboTxt.indexOf("(");

            if (index > 0) {
                comboTxt = comboTxt.substring(0, index).trim();
            }

            String txtDel = "-G " + comboTxt.toUpperCase() + " ";

            if (txtContent.contains(txtDel)) {

                txtContent = txtContent.replace(txtDel, "");
                annotationGroupTxt.setText(txtContent);
            }

        }
    }//GEN-LAST:event_del7ActionPerformed

    private void jLabel6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseClicked
        App.helpWebpage(" https://software.broadinstitute.org/gatk/documentation/article?id=10836");
    }//GEN-LAST:event_jLabel6MouseClicked

    private void g1CheckItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_g1CheckItemStateChanged
        if (g1Check.isSelected()) {
            annotationGroupCombo.setEditable(true);
            add7.setEnabled(true);
            del7.setEnabled(true);
        } else {
            annotationGroupCombo.setEditable(false);
            add7.setEnabled(false);
            del7.setEnabled(false);
        }
    }//GEN-LAST:event_g1CheckItemStateChanged

    private void germlineCheckItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_germlineCheckItemStateChanged
        germlineResTxt.setText("");
        if (germlineCheck.isSelected())
            germlineResBtn.setEnabled(true);
        else
            germlineResBtn.setEnabled(false);
    }//GEN-LAST:event_germlineCheckItemStateChanged

    private void ponCheckItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_ponCheckItemStateChanged
        ponTxt.setText("");
        if (ponCheck.isSelected()) {
            ponBtn.setEnabled(true);
            generatePON.setSelected(false);
        } else
            ponBtn.setEnabled(false);
    }//GEN-LAST:event_ponCheckItemStateChanged

    private void allelesCheckItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_allelesCheckItemStateChanged
        alleleTxt.setText("");
        if (allelesCheck.isSelected())
            alleleBtn.setEnabled(true);
        else
            alleleBtn.setEnabled(false);
    }//GEN-LAST:event_allelesCheckItemStateChanged

    private void f1r2CheckItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_f1r2CheckItemStateChanged
        f1r2.setText("");
        if (f1r2Check.isSelected())
            f1r2Btn.setEnabled(true);
        else
            f1r2Btn.setEnabled(false);
    }//GEN-LAST:event_f1r2CheckItemStateChanged

    private void intervalCheckItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_intervalCheckItemStateChanged
        intervalT.setText("");

        intervalText.setText("");

        if (intervalCheck.isSelected()) {
            intervalBtn.setEnabled(true);
            add6.setEnabled(true);
            del6.setEnabled(true);
            intervalT.setEditable(true);

        } else {
            intervalT.setEditable(false);

            intervalBtn.setEnabled(false);
            add6.setEnabled(false);
            del6.setEnabled(false);
        }
    }//GEN-LAST:event_intervalCheckItemStateChanged

    private void splitNcigarItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_splitNcigarItemStateChanged
        if (splitNcigar.isSelected())
            splitNcigar.setText("YES");
        else
            splitNcigar.setText("NO");
    }//GEN-LAST:event_splitNcigarItemStateChanged

    private void selectVariantsOpItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_selectVariantsOpItemStateChanged
        if (selectVariantsOp.isSelected()) {
            selectVariantsOp.setText("YES");
        } else {
            selectVariantsOp.setText("NO");
        }

    }//GEN-LAST:event_selectVariantsOpItemStateChanged

    private void jLabel35MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel35MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel35MouseClicked

    private void variantFilterAItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_variantFilterAItemStateChanged
        if (variantFilterA.isSelected())
            variantFilterA.setText("YES");
        else {

            variantFilterA.setText("NO");

        }
    }//GEN-LAST:event_variantFilterAItemStateChanged

    private void add8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add8ActionPerformed
        String txtContent = variantFilterTxtA.getText();
        String selectedExpr = (String) snpSiftCombobox.getSelectedItem();

//        if (selectedExpr != null && !selectedExpr.isBlank()) {
//            String txtAdd = selectedExpr.toUpperCase() + " ";
//            if (!txtContent.contains(txtAdd)) {
//                variantFilterTxtA.append(txtAdd);
//            }
//        }
//CorrectionDone
        if (selectedExpr == null || selectedExpr.isBlank()) {
            return;
        }

        // Skip divider headers inside the combobox if clicked
        if (selectedExpr.startsWith("---")) {
            return;
        }

        String exprToAdd = selectedExpr.trim() + " ";

        // Prevent duplicate entries
        if (txtContent.contains(selectedExpr.trim())) {
            return;
        }

        // Check if both tags exist in the text area
        if (txtContent.contains("[SNPEFF]") && txtContent.contains("[VEP]")) {

            // Auto-route SnpEff specific expressions to [SNPEFF] section
            if (selectedExpr.startsWith("ANN[*]")) {
                insertUnderTag("[SNPEFF]", exprToAdd);
            } // Auto-route VEP specific expressions to [VEP] section
            else if (selectedExpr.startsWith("CSQ[*]")) {
                insertUnderTag("[VEP]", exprToAdd);
            } // For generic options (QUAL > 30, FILTER = 'PASS', etc.), insert at caret or end
            else {
                insertAtCaretOrEnd(exprToAdd);
            }
        } else {
            // Single option mode (no tags present)
            variantFilterTxtA.append(exprToAdd);
        }
    }//GEN-LAST:event_add8ActionPerformed

    private void insertUnderTag(String tag, String exprToAdd) {
    String text = variantFilterTxtA.getText();
    int tagIndex = text.indexOf(tag);

    if (tagIndex != -1) {
        // Find end of line after the tag
        int lineEnd = text.indexOf("\n", tagIndex);
        if (lineEnd == -1) {
            lineEnd = text.length();
        }

        StringBuilder sb = new StringBuilder(text);
        // Insert expression on a new line right after the tag header
        sb.insert(lineEnd, exprToAdd);
        variantFilterTxtA.setText(sb.toString());
    } else {
        variantFilterTxtA.append(  exprToAdd);
    }
}
private void insertAtCaretOrEnd(String exprToAdd) {
    int caretPos = variantFilterTxtA.getCaretPosition();
    String text = variantFilterTxtA.getText();

    if (caretPos >= 0 && caretPos <= text.length()) {
        StringBuilder sb = new StringBuilder(text);
        sb.insert(caretPos, exprToAdd);
        variantFilterTxtA.setText(sb.toString());
        variantFilterTxtA.setCaretPosition(caretPos + exprToAdd.length());
    } else {
        variantFilterTxtA.append(exprToAdd);
    }
}    
    
    private void del8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_del8ActionPerformed
        String txtContent = variantFilterTxtA.getText();
        String selectedExpr = (String) snpSiftCombobox.getSelectedItem();

//        if (selectedExpr != null && !selectedExpr.isBlank()) {
//            String txtDel = selectedExpr.toUpperCase() + " ";
//            if (txtContent.contains(txtDel)) {
//                txtContent = txtContent.replace(txtDel, "");
//                variantFilterTxtA.setText(txtContent);
//            }
//        }
//CorrectionDone
if (selectedExpr == null || selectedExpr.isBlank()) {
        return;
    }

    String targetTrimmed = selectedExpr.trim();

    // Search and remove the expression regardless of extra spaces around it
    if (txtContent.contains(targetTrimmed)) {
        // Removes target expression and trailing space if present
        txtContent = txtContent.replace(targetTrimmed + " ", "")
                               .replace(targetTrimmed, "");
        variantFilterTxtA.setText(txtContent);
    }

    }//GEN-LAST:event_del8ActionPerformed

    private void jLabel28MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel28MouseClicked

        //WES-Setting: NIGAB PC
        refTxt.setText("/media/nigab/sda1/Iffat/Reference/star_index/GRCh38.p14.genome.fa");
        listKnownSites.addElement("/media/nigab/sda1/Iffat/VCF/resources_broad_hg38_v0_1000G_phase1.snps.high_confidence.hg38.vcf.gz");
        listKnownSites.addElement("/media/nigab/sda1/Iffat/VCF/resources_broad_hg38_v0_hapmap_3.3.hg38.vcf.gz");
        listKnownSites.addElement("/media/nigab/sda1/Iffat/VCF/resources_broad_hg38_v0_Mills_and_1000G_gold_standard.indels.hg38.vcf.gz");
        intervalT1.setText("/media/nigab/sda1/Iffat/VCF/resources_broad_hg38_v0_wgs_calling_regions.hg38.interval_list");

// For WES-University PC
//        refTxt.setText("/media/dr/9d1c8592-358c-4c9b-bfe6-d7c6e2a62be0/home/computer/Iffat/Genomes_indexed/GRCh38.p13.genome.fa");
//        listKnownSites.addElement("/home/dr/GATK_DAT/1000G_phase1.snps.high_confidence.hg38.vcf.gz");
//        listKnownSites.addElement("/home/dr/GATK_DAT/Mills_and_1000G_gold_standard.indels.hg38.vcf.gz");
//        listKnownSites.addElement("/home/dr/GATK_DAT/hapmap_3.3.hg38.vcf.gz");
//       // intervalT1.setText("/media/dr/9d1c8592-358c-4c9b-bfe6-d7c6e2a62be0/home/computer/Iffat/resources_broad_hg38_v0_wgs_calling_regions.hg38.interval_list");
        //
//        refTxt.setText("/media/dr/9d1c8592-358c-4c9b-bfe6-d7c6e2a62be0/home/computer/Iffat/Genomes_indexed/GRCh38.p13.genome.fa");
//        listKnownSites.addElement("/home/dr/GATK_DAT/1000G_phase1.snps.high_confidence.hg38.vcf.gz");
//        listKnownSites.addElement("/home/dr/GATK_DAT/Mills_and_1000G_gold_standard.indels.hg38.vcf.gz");
//        listKnownSites.addElement("/home/dr/GATK_DAT/hapmap_3.3.hg38.vcf.gz");
//        germlineResTxt.setText("/media/dr/9d1c8592-358c-4c9b-bfe6-d7c6e2a62be0/home/computer/Iffat/VCF/af-only-gnomad.hg38.vcf.gz");
//        ponTxt.setText("/media/dr/9d1c8592-358c-4c9b-bfe6-d7c6e2a62be0/home/computer/Iffat/VCF/1000g_pon.hg38.vcf.gz");
        variantFilterA.setSelected(true);

        bwaM.setSelected(true);
        thread.setText("18");
        // For WES Variant Calling
        selectVariantTxt.setText("--select-type-to-include SNP --select-type-to-include INDEL --select-type-to-include MNP --select-type-to-include MIXED    --select 'AF[0] >= 0.01'");
        variantFilterTxtB.setText("--filter-name \"WES_HardFilter\" --filter-expression \"QD < 2.0 || FS > 200.0 || MQ < 40.0\" ");
        variantFilterTxtA.setText("[SNPEFF]  !(ANN[*].EFFECT has 'intergenic_region') & !(ANN[*].EFFECT has 'intron_variant') & !(ANN[*].EFFECT has 'synonymous_variant') & (exists AF) & (AF[*] >= 0.01) \n "
                + "[VEP] !(CSQ[*] =~ 'intergenic_region|intron_variant|synonymous_variant') && (exists AF) && (AF[*] >= 0.01) ");
        // For RNA-Seq Variant Calling
        //selectVariantTxt.setText("--select-type-to-include SNP --select-type-to-include INDEL --select-type-to-include MNP --select-type-to-include MIXED    --select 'AF[0] >= 0.01'");
        // variantFilterTxtB.setText("--filter-name \"RNASeq_HardFilter\" --filter-expression \"QD < 2.0 || FS > 30.0 || SOR > 3.0 || MQ < 40.0 || DP < 10\" ");
        //variantFilterTxtA.setText("'!(ANN[*].EFFECT has \"intergenic_region\") & !(ANN[*].EFFECT has \"intron_variant\") ");

        //  variantFilterTxtA.setText("\"((ANN[*].EFFECT has 'missense_variant') | (ANN[*].EFFECT has 'frameshift_variant') | (ANN[*].EFFECT has 'stop_gained')) & isVariant & (VT = 'SNP')\"");
//           refTxt.setText("/home/iffy/TestFiles/MouseExample/INPUTS/mm9.fa");
//        listKnownSites.addElement("/home/iffy/TestFiles/MouseExample/INPUTS/vcf/00-common_all.vcf.gz");
//        selectVariantTxt.setText("--select-type-to-include SNP --select-type-to-include INDEL");
//        variantFilterTxtB.setText("--filter-name AlleleFrequence --filter-expression \" AF < 0.01\" ");
//        variantFilterTxtA.setText("\"((ANN[*].EFFECT has 'missense_variant') | (ANN[*].EFFECT has 'frameshift_variant') | (ANN[*].EFFECT has 'stop_gained')) & isVariant & (VT = 'SNP')\"");

    }//GEN-LAST:event_jLabel28MouseClicked

    private void fastqcResultComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fastqcResultComboActionPerformed
        if (fastqcResultCombo.isEnabled() == true) {
            try {

                //File htmlFile = new File(FilenameUtils.removeExtension(allFiles[resultsCombo.getSelectedIndex()].getAbsolutePath()) + ("_fastqc.html"));
                File htmlFile = new File(output_Dir.getText() + fastqcResultCombo.getSelectedItem());

                Desktop.getDesktop().browse(htmlFile.toURI());
            } catch (IOException ex) {
                App.LOGGER.error("\r\nERROR in FASTQC Analysis: " + ex);
                ex.printStackTrace();
                App.textArea.append("\r\nERROR in FASTQC Analysis: " + ex);
            }
        }
    }//GEN-LAST:event_fastqcResultComboActionPerformed

    private void fastqcTrimComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fastqcTrimComboActionPerformed
        if (fastqcTrimCombo.getSelectedIndex() == 0) {
            trimPanel.setVisible(false);
            nextBtn.setText("Proceed to Run Pipeline");

        } else {
            trimPanel.setVisible(true);
            nextBtn.setText("Continue Trimming & QC Refinement");
        }
    }//GEN-LAST:event_fastqcTrimComboActionPerformed

    private void jLabel104MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel104MouseClicked

        try {
            Desktop.getDesktop().open(new File(System.getProperty("user.dir").concat("/bin/FastQC_Manual.pdf")));
        } catch (IOException ex) {
            App.LOGGER.error("\r\nERROR loading FASTQC mannual file: " + ex);
            App.textArea.append("\r\nERROR loading FASTQC mannual file: " + ex);
        }
    }//GEN-LAST:event_jLabel104MouseClicked

    private void jLabel104MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel104MouseEntered
        //    jLabel14.setIcon(App.icons[1]);
    }//GEN-LAST:event_jLabel104MouseEntered

    private void jLabel104MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel104MouseExited
        //    jLabel14.setIcon(App.icons[0]);
    }//GEN-LAST:event_jLabel104MouseExited

    private void slidingCItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_slidingCItemStateChanged
        if (slidingC.isSelected()) {
            sliding.setEnabled(true);
        } else {
            sliding.setEnabled(false);
        }
    }//GEN-LAST:event_slidingCItemStateChanged

    private void browseGTFBtn2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseGTFBtn2ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(App.Trimmomatic_PATH));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Fasta Files (*.fa)", "fa"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {

                adapterfile.setText(fileChooser.getSelectedFile().toString());

            }
        }
    }//GEN-LAST:event_browseGTFBtn2ActionPerformed

    private void headcropCItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_headcropCItemStateChanged
        if (headcropC.isSelected()) {
            headcrop.setEnabled(true);
        } else {
            headcrop.setEnabled(false);
        }
    }//GEN-LAST:event_headcropCItemStateChanged

    private void maxinfoCItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_maxinfoCItemStateChanged
        if (maxinfoC.isSelected()) {
            maxinfo.setEnabled(true);
        } else {
            maxinfo.setEnabled(false);
        }
    }//GEN-LAST:event_maxinfoCItemStateChanged

    private void trailingCItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_trailingCItemStateChanged
        if (trailingC.isSelected()) {
            trailing.setEnabled(true);
        } else {
            trailing.setEnabled(false);
        }
    }//GEN-LAST:event_trailingCItemStateChanged

    private void leadingCItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_leadingCItemStateChanged
        if (leadingC.isSelected()) {
            leading.setEnabled(true);
        } else {
            leading.setEnabled(false);
        }
    }//GEN-LAST:event_leadingCItemStateChanged

    private void minlenCItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_minlenCItemStateChanged
        if (minlenC.isSelected()) {
            minlen.setEnabled(true);
        } else {
            minlen.setEnabled(false);
        }
    }//GEN-LAST:event_minlenCItemStateChanged

    private void cropCItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cropCItemStateChanged
        if (cropC.isSelected()) {
            crop.setEnabled(true);
        } else {
            crop.setEnabled(false);
        }
    }//GEN-LAST:event_cropCItemStateChanged

    private void fastpResultComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fastpResultComboActionPerformed
        if (fastpResultCombo.isEnabled() == true) {
            try {

                File htmlFile = new File(output_Dir.getText() + fastpResultCombo.getSelectedItem());
                Desktop.getDesktop().browse(htmlFile.toURI());

            } catch (IOException ex) {
                App.LOGGER.error("\r\nERROR in FASTP Analysis: " + ex);
                ex.printStackTrace();
                App.textArea.append("\r\nERROR in FASTP Analysis: " + ex);
            }
        }
    }//GEN-LAST:event_fastpResultComboActionPerformed

    private void fastpTrimComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fastpTrimComboActionPerformed
        if (fastpTrimCombo.getSelectedIndex() == 0) {
            nextBtn.setText("Proceed to Run Pipeline");
            fastPparameter.setVisible(false);
        } else {
            nextBtn.setText("Continue Trimming & QC Refinement");
            fastPparameter.setVisible(true);
        }
    }//GEN-LAST:event_fastpTrimComboActionPerformed

    private void jLabel79MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel79MouseClicked
        App.helpWebpage("https://github.com/OpenGene/fastp/blob/master/README.md");
    }//GEN-LAST:event_jLabel79MouseClicked

    private void noneRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noneRadioActionPerformed
        nextBtn.setText("Next");
    }//GEN-LAST:event_noneRadioActionPerformed

    private void fastqcRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fastqcRadioActionPerformed
        nextBtn.setText("Proceed to Quality Check");
    }//GEN-LAST:event_fastqcRadioActionPerformed

    private void fastpRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fastpRadioActionPerformed
        nextBtn.setText("Proceed to Quality Check");
    }//GEN-LAST:event_fastpRadioActionPerformed

    private void back1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_back1ActionPerformed
        QualityControlPanel.setVisible(false);
        inputPanel.setVisible(true);
        nextBtn.setText("Proceed to Quality Check");
    }//GEN-LAST:event_back1ActionPerformed

    private void knownSitesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_knownSitesKeyPressed
        if (evt.getKeyChar() == KeyEvent.VK_DELETE) {
            listKnownSites.remove(knownSites.getSelectedIndex());

        }
    }//GEN-LAST:event_knownSitesKeyPressed

    private void jointModeHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jointModeHActionPerformed
        genomicDBPanel.setVisible(true);
    }//GEN-LAST:event_jointModeHActionPerformed

    private void singleModeHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_singleModeHActionPerformed
        genomicDBPanel.setVisible(false);
    }//GEN-LAST:event_singleModeHActionPerformed

    private void del9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_del9ActionPerformed
        String txtContent = intervalText1.getText();
        if (!intervalT.getText().isBlank()) {
            String txtDel = "-L " + intervalT1.getText().toLowerCase() + " ";
            if (!txtContent.contains(txtDel)) {
                txtContent = txtContent.replace(txtDel, "");
                intervalText1.append(txtDel);
            }

        }
    }//GEN-LAST:event_del9ActionPerformed

    private void add9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add9ActionPerformed
        String txtContent = intervalText1.getText();
        if (!intervalT1.getText().isBlank()) {
            String txtAdd = "-L " + intervalT1.getText().toLowerCase() + " ";
            if (!txtContent.contains(txtAdd)) {
                intervalText1.append(txtAdd);
            }
        }
    }//GEN-LAST:event_add9ActionPerformed

    private void intervalBtn2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_intervalBtn2ActionPerformed

        intervalT1.setText("");
        intervalBtn2.setEnabled(true);

        intervalT2.setText("");
        intervalText1.setText("");
        add9.setEnabled(false);
        del9.setEnabled(false);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("BED Files (*.bed)", "bed"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Interval List (*.interval_list)", "interval_list"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("VCF Files (*.vcf)", "vcf"));
        fileChooser.setAcceptAllFileFilterUsed(true);

        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {

                intervalT1.setText(fileChooser.getSelectedFile().toString());

            }
        }
    }//GEN-LAST:event_intervalBtn2ActionPerformed

    private void createIntervalRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createIntervalRActionPerformed
        intervalT1.setText("");
        intervalBtn2.setEnabled(false);

        intervalT2.setText("");
        intervalText1.setText("");
        add9.setEnabled(true);
        del9.setEnabled(true);

    }//GEN-LAST:event_createIntervalRActionPerformed

    private void excludeFilteredItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_excludeFilteredItemStateChanged
        if (excludeFiltered.isSelected()) {
            selectVariantTxt.append(" --exclude-filtered ");
        }
    }//GEN-LAST:event_excludeFilteredItemStateChanged

    private void restrictAllelesItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_restrictAllelesItemStateChanged
        if (restrictAlleles.isSelected()) {
            restrictAlleleCombo.setEnabled(true);
            add10.setEnabled(true);
            del10.setEnabled(true);
        } else {
            restrictAlleleCombo.setEnabled(false);
            add10.setEnabled(false);
            del10.setEnabled(false);
        }
        if (!jCheckBox5.isSelected() && !jCheckBox6.isSelected()
                && !jCheckBox7.isSelected() && !jCheckBox8.isSelected()) {
            selectVariantTxt.setText("");
        }
    }//GEN-LAST:event_restrictAllelesItemStateChanged

    private void add10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add10ActionPerformed

        String txtContent = selectVariantTxt.getText();
        String txtAdd = "--restrict-alleles-to " + restrictAlleleCombo.getSelectedItem().toString() + " ";
        if (!txtContent.contains(txtAdd)) {
            selectVariantTxt.append(txtAdd);
        }

    }//GEN-LAST:event_add10ActionPerformed

    private void del10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_del10ActionPerformed
        String txtContent = selectVariantTxt.getText();
        String txtDel = "--restrict-alleles-to " + restrictAlleleCombo.getSelectedItem().toString() + " ";
        if (txtContent.contains(txtDel)) {
            txtContent = txtContent.replace(txtDel, "");
            selectVariantTxt.setText(txtContent);
        }

    }//GEN-LAST:event_del10ActionPerformed

    private void snpEffRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_snpEffRadioActionPerformed
        jLabel35.setText("Filter String for snpEff:");
        snpSiftCombobox.setModel(new DefaultComboBoxModel<>(snpSiftFilters));
        variantFilterTxtA.setText("!(ANN[*].EFFECT has 'intergenic_region') & !(ANN[*].EFFECT has 'intron_variant') & !(ANN[*].EFFECT has 'synonymous_variant') & (exists AF) & (AF[*] >= 0.01) ");
    }//GEN-LAST:event_snpEffRadioActionPerformed

    private void vepRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vepRadioActionPerformed
        jLabel35.setText("Filter String for VEP:");

        snpSiftCombobox.setModel(new DefaultComboBoxModel<>(snpSiftFiltersVEP));
        variantFilterTxtA.setText("!(CSQ[*] =~ 'intergenic_region|intron_variant|synonymous_variant') && (exists AF) && (AF[*] >= 0.01)");
    }//GEN-LAST:event_vepRadioActionPerformed

    private void snpEffvepRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_snpEffvepRadioActionPerformed
        jLabel35.setText("Filter String: Do not delete [SNPEFF] or [VEP] tags. Write/select your expressions directly below or beside each tag header.");
        snpSiftCombobox.setModel(new DefaultComboBoxModel<>(combinedFilters));
        variantFilterTxtA.setText("");
        variantFilterTxtA.append("[SNPEFF]\n\n");
        variantFilterTxtA.append("!(ANN[*].EFFECT has 'intergenic_region') & !(ANN[*].EFFECT has 'intron_variant') & !(ANN[*].EFFECT has 'synonymous_variant') & (exists AF) & (AF[*] >= 0.01)\n");
        
        variantFilterTxtA.append("[VEP]\n\n");
      variantFilterTxtA.append("!(CSQ[*] =~ 'intergenic_region|intron_variant|synonymous_variant') && (exists AF) && (AF[*] >= 0.01)");
  

    }//GEN-LAST:event_snpEffvepRadioActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel FilterPanel;
    private javax.swing.JLabel Label1;
    private javax.swing.JLabel Label2;
    private javax.swing.JLabel Label3;
    private javax.swing.JLabel Label4;
    private javax.swing.JLabel Label5;
    private javax.swing.JLabel Label6;
    private javax.swing.JTextField Linterval;
    private javax.swing.JPanel MainPanel;
    private javax.swing.JPanel QualityControlPanel;
    private javax.swing.JTextField adapterfile;
    private javax.swing.JButton add1;
    private javax.swing.JButton add10;
    private javax.swing.JButton add2;
    private javax.swing.JButton add3;
    private javax.swing.JButton add4;
    private javax.swing.JButton add5;
    private javax.swing.JButton add6;
    private javax.swing.JButton add7;
    private javax.swing.JButton add8;
    private javax.swing.JButton add9;
    private javax.swing.JPanel addReplaceRGPanel;
    private javax.swing.JButton alleleBtn;
    private javax.swing.JTextField alleleTxt;
    private javax.swing.JCheckBox allelesCheck;
    private javax.swing.JPanel analysisPanel;
    private javax.swing.JComboBox<String> annotationGroupCombo;
    private javax.swing.JTextArea annotationGroupTxt;
    private javax.swing.JButton back1;
    private javax.swing.JButton bamoutB;
    private javax.swing.JCheckBox bamoutCheck;
    private javax.swing.JTextField bamoutTxt;
    private javax.swing.JButton browseGTFBtn2;
    private javax.swing.JCheckBox bwaM;
    private javax.swing.JPanel bwaPanel;
    private javax.swing.JPanel byBam;
    private javax.swing.JPanel byFastq;
    private javax.swing.JRadioButton createIntervalR;
    private javax.swing.JTextField crop;
    private javax.swing.JCheckBox cropC;
    private javax.swing.JButton del1;
    private javax.swing.JButton del10;
    private javax.swing.JButton del2;
    private javax.swing.JButton del3;
    private javax.swing.JButton del4;
    private javax.swing.JButton del5;
    private javax.swing.JButton del6;
    private javax.swing.JButton del7;
    private javax.swing.JButton del8;
    private javax.swing.JButton del9;
    private javax.swing.JComboBox<String> ercCombo;
    private javax.swing.JCheckBox excludeFiltered;
    private javax.swing.JTextField f1r2;
    private javax.swing.JButton f1r2Btn;
    private javax.swing.JCheckBox f1r2Check;
    private javax.swing.JPanel fastPparameter;
    private javax.swing.JPanel fastpPanel;
    private javax.swing.JRadioButton fastpRadio;
    private javax.swing.JComboBox<String> fastpResultCombo;
    private javax.swing.JComboBox<String> fastpTrimCombo;
    private javax.swing.JTextArea fastpTxtArea;
    private javax.swing.JPanel fastqcPanel;
    private javax.swing.JRadioButton fastqcRadio;
    private javax.swing.JComboBox<String> fastqcResultCombo;
    private javax.swing.JTable fastqcTable;
    private javax.swing.JComboBox<String> fastqcTrimCombo;
    private javax.swing.JTextField filterExprB;
    private javax.swing.JTextField filterNameB;
    private javax.swing.JCheckBox g1Check;
    private javax.swing.JCheckBox generatePON;
    private javax.swing.JPanel genomicDBPanel;
    private javax.swing.JCheckBox germline;
    private javax.swing.JCheckBox germlineCheck;
    private javax.swing.JPanel germlinePanel;
    private javax.swing.JButton germlineResBtn;
    private javax.swing.JTextField germlineResTxt;
    private javax.swing.ButtonGroup haploIntervalBG;
    private javax.swing.ButtonGroup haplotypeGroup;
    private javax.swing.JTextField headcrop;
    private javax.swing.JCheckBox headcropC;
    private javax.swing.JTextField illumniaclip;
    private javax.swing.JList<String> inputBamTxt;
    private javax.swing.JComboBox<String> inputOptions;
    private javax.swing.JPanel inputPanel;
    private javax.swing.JButton inputbamBtn;
    private javax.swing.JButton intervalBtn;
    private javax.swing.JButton intervalBtn1;
    private javax.swing.JButton intervalBtn2;
    private javax.swing.JCheckBox intervalCheck;
    private javax.swing.JTextField intervalT;
    private javax.swing.JTextField intervalT1;
    private javax.swing.JTextField intervalT2;
    private javax.swing.JTextArea intervalText;
    private javax.swing.JTextArea intervalText1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JCheckBox jCheckBox7;
    private javax.swing.JCheckBox jCheckBox8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel100;
    private javax.swing.JLabel jLabel101;
    private javax.swing.JLabel jLabel102;
    private javax.swing.JLabel jLabel103;
    private javax.swing.JLabel jLabel104;
    private javax.swing.JLabel jLabel105;
    private javax.swing.JLabel jLabel106;
    private javax.swing.JLabel jLabel107;
    private javax.swing.JLabel jLabel108;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
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
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JLabel jLabel79;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel80;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel92;
    private javax.swing.JLabel jLabel93;
    private javax.swing.JLabel jLabel94;
    private javax.swing.JLabel jLabel95;
    private javax.swing.JLabel jLabel96;
    private javax.swing.JLabel jLabel97;
    private javax.swing.JLabel jLabel98;
    private javax.swing.JLabel jLabel99;
    private javax.swing.JLabel jLabelHelp;
    private javax.swing.JList<String> jListNormal;
    private javax.swing.JList<String> jListTumor;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane1Tree;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JTree jTree1;
    private javax.swing.JRadioButton jointModeH;
    private javax.swing.JButton knownSNPBtn;
    private javax.swing.JList<String> knownSites;
    private javax.swing.JTextField leading;
    private javax.swing.JCheckBox leadingC;
    private javax.swing.JCheckBox logC;
    private javax.swing.JLabel mainLabelWarning;
    private javax.swing.JTextField maxinfo;
    private javax.swing.JCheckBox maxinfoC;
    private javax.swing.JTextField minlen;
    private javax.swing.JCheckBox minlenC;
    private javax.swing.JPanel mutect2Panel;
    private javax.swing.JComboBox<String> mutectCombo;
    private javax.swing.JButton nextBtn;
    private javax.swing.JRadioButton noneRadio;
    private javax.swing.JButton norTumBtn;
    private javax.swing.JPanel normalPanel;
    private javax.swing.JPanel otherPanel;
    private javax.swing.JButton outputBtn;
    private javax.swing.JLabel outputLabel;
    private javax.swing.JTextField output_Dir;
    private javax.swing.JRadioButton pairRadioButton;
    private javax.swing.JComboBox<String> phredEncodingCombo;
    private javax.swing.JButton ponBtn;
    private javax.swing.JCheckBox ponCheck;
    private javax.swing.JLabel ponLabel2;
    private javax.swing.JLabel ponLabel3;
    private javax.swing.JLabel ponLabel4;
    private javax.swing.JTextField ponTxt;
    private javax.swing.ButtonGroup qualityCheckBG;
    private javax.swing.ButtonGroup readBG;
    private javax.swing.JList<String> readFilesInPair1;
    private javax.swing.JList<String> readFilesInPair2;
    private javax.swing.JList<String> readFilesInSingle;
    private javax.swing.JButton refBtn;
    private javax.swing.JTextField refTxt;
    private javax.swing.JComboBox<String> restrictAlleleCombo;
    private javax.swing.JCheckBox restrictAlleles;
    private javax.swing.JTextField select;
    private javax.swing.JTextField selectGenotype;
    private javax.swing.JComboBox<String> selectTypeCombo;
    private javax.swing.JTextArea selectVariantTxt;
    private javax.swing.JCheckBox selectVariantsOp;
    private javax.swing.JRadioButton singleModeH;
    private javax.swing.JRadioButton singleRadioButton;
    private javax.swing.JTextField sliding;
    private javax.swing.JCheckBox slidingC;
    private javax.swing.JRadioButton snpEffRadio;
    private javax.swing.JTextField snpEff_DB;
    private javax.swing.JRadioButton snpEffvepRadio;
    private javax.swing.JComboBox<String> snpSiftCombobox;
    private javax.swing.JCheckBox somatic;
    private javax.swing.JCheckBox splitNcigar;
    private javax.swing.JTextField thread;
    private javax.swing.JTextField thread1;
    private javax.swing.JCheckBox threadC;
    private javax.swing.JTextField trailing;
    private javax.swing.JCheckBox trailingC;
    private javax.swing.JPanel trimPanel;
    private javax.swing.JButton tumNorBtn;
    private javax.swing.JPanel upPanel;
    private javax.swing.JRadioButton uploadIntervalfileR;
    private javax.swing.ButtonGroup variantAnnotationBG;
    private javax.swing.JCheckBox variantFilterA;
    private javax.swing.JTextArea variantFilterTxtA;
    private javax.swing.JTextArea variantFilterTxtB;
    private javax.swing.JTextField vepAssembly;
    private javax.swing.JRadioButton vepRadio;
    private javax.swing.JTextField vepSpecies;
    private javax.swing.JPanel visualPanel;
    private javax.swing.JLabel warningLabel;
    // End of variables declaration//GEN-END:variables

    class ListItemTransferHandler extends TransferHandler {

        private int index = -1; // To track dragged item index
        private JList<?> sourceList;  // To track the source list

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.stringFlavor)
                    && support.getComponent() == sourceList;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            //JList<?> list = (JList<?>) c;
            sourceList = (JList<?>) c;
            index = sourceList.getSelectedIndex();
            String value = sourceList.getSelectedValue().toString();
            return new StringSelection(value);
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            JList<?> targetList = (JList<?>) support.getComponent();

            // Prevent data import if target is not the source list
            if (targetList != sourceList) {
                return false;
            }

            JList.DropLocation dropLocation = (JList.DropLocation) support.getDropLocation();
            int dropIndex = dropLocation.getIndex();
            DefaultListModel model = (DefaultListModel) targetList.getModel();

            if (index != -1 && dropIndex != index) {
                Object draggedItem = model.getElementAt(index);
                model.remove(index);

                // Adjust the drop index if necessary
                if (dropIndex > index) {
                    dropIndex--;
                }

                model.add(dropIndex, draggedItem);
                targetList.setSelectedIndex(dropIndex);
                return true;
            }
            return false;
        }
    }

}//END CLASS
