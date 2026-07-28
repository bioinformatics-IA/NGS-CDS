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
import java.io.InputStreamReader;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Collections;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

// Libraries for table
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 *
 * @author iffy
 */
public class RNASeqPipeLineInternalFrame extends javax.swing.JInternalFrame {

    /*INPUT VARIABLES:*/
    private File[] allFiles;

    private DefaultListModel listModelfasta, listModelfasta1, listModelreadSingle, listModelreadPair1, listModelreadPair2, interactionListModel;
    private DefaultTableModel dm, sampleTableModel;
    private JTableHeader headerQC;

    private List<String> filesForCombo;
    private List<String> filesForComboP = new ArrayList<>();

    private List<String> sfileNames, sfilePaths, sfileExtensions;
    private List<String> p1fileNames, p1filePaths, p1fileExtensions, p1p2fileNames;
    private List<String> p2fileNames, p2filePaths, p2fileExtensions;

    private Map<String, List<String>> singlePathMap;
    private Map<String, Map<String, List<String>>> pairPathMap;
    public String selectedKey;
    public InputPathDialog inputDialog;
    public int fastCounter;
    public Frame parentFrame;

    private String read, mode, fileMode;// read: SINGLEREAD/PAIRREAD, mode: MULTIPLEPERRUN/SINGLEPERRUN, fileMode= exFAT/NTFS
    private Vector readCombo;
    private String s;

    private DefaultListModel deFactorListModel, model1; //check this
    private String analysisP;
    private boolean pipelineError = false, tableFlag = true, rcodeError = false;
    private boolean checkFastq = true;//For checking Fastq
    Map<Integer, String> tooltips; //For FastQC Table

    /**
     * Creates new form RNASeqAnalysisInternalFrame
     */
    public RNASeqPipeLineInternalFrame() {

        listModelfasta = new DefaultListModel();
        listModelfasta1 = new DefaultListModel();

        listModelreadSingle = new DefaultListModel();
        listModelreadPair1 = new DefaultListModel();
        listModelreadPair2 = new DefaultListModel();

        //Declaring Map
        singlePathMap = new HashMap<>();
        pairPathMap = new HashMap<>();
        fastCounter = 0;
        parentFrame = JOptionPane.getFrameForComponent(this);

        interactionListModel = new DefaultListModel();

        deFactorListModel = new DefaultListModel();
        model1 = new DefaultListModel();
        tooltips = new HashMap<>();

        //FastQC Table Model
        String[] tblHead = {"Sample Name", "Basic Statistics", "Per base sequence quality", "Per sequence quality scores", "Per base sequence content", "Per sequence GC content", "Per base N content", "Sequence Length Distribution", "Sequence Duplication Levels", "Overrepresented sequences", "Adapter Content"};
        dm = new DefaultTableModel(tblHead, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column <= 11 ? false : true;
            }
        };

        // Sample Data Table Model
        // With the first column uneditable
        String[] columnNames = {"SAMPLENAME"};
        sampleTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column == 0) {// Make the first column (index 0) always uneditable
                    return false;
                }
                return tableFlag;// Make other columns editable if true
            }
        };

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

        // Set up lowercase cell editor for all cells
        setLowerCaseCellEditor(sampleTable);

        //INPUT FILES SETTING
        sfileNames = new ArrayList<>();
        sfilePaths = new ArrayList<>();
        sfileExtensions = new ArrayList<>();

        p1fileNames = new ArrayList<>();
        p1filePaths = new ArrayList<>();
        p1fileExtensions = new ArrayList<>();

        p2fileNames = new ArrayList<>();
        p2filePaths = new ArrayList<>();
        p2fileExtensions = new ArrayList<>();

        //END INPUT FILES SETTING
        helpLabel.setIcon(App.icons[0]);
//STAR  DEFAULTS
        starPanel.setVisible(false);
        outFileNamePrefix.setEnabled(false);
        mode = "SINGLE";
        read = "SINGLE";

        multipleFeaturePanel.setVisible(true);

        jButton6.setEnabled(true);
        jButton7.setEnabled(false);
        jButton8.setEnabled(false);
// BUTTON GROUPS
        inputReadsBG.add(singleRadioButton);
        inputReadsBG.add(pairRadioButton);

        sampleTableRadios.add(sampleRadio1);
        sampleTableRadios.add(sampleRadio2);

        featureCountsBG.add(eachRadio);
        featureCountsBG.add(combinedRadio);

        buttonGroup3.add(singleFactorRadio);//FOR DNA SEQ PANEL
        buttonGroup3.add(multiFactorRadio);

        buttonGroup4.add(jRadioContrast);
        buttonGroup4.add(jRadioList);

        expressionAnalysisBG.add(deseqRadio);
        analysisP = "deSeq2";
        expressionAnalysisBG.add(edgeRadio);
        expressionAnalysisBG.add(bothRadio);

        QCbuttonGroup.add(fastpRadio);
        QCbuttonGroup.add(fastqcRadio);
        QCbuttonGroup.add(noneRadio);

        //SplitPane
//  jSplitPane1.setResizeWeight(.5d);//.setDividerLocation(0.5);
        ////////////////////
//SETTING ALL PANELS
////////////////////
//1- INPUT PANEL
        inputPanel.setVisible(true);
//2- DOWN PANEL
        downPanel.setVisible(false);
//2.1- QUALITY CONTROL
        QualityControlPanel.setVisible(false);
        fastpPanel.setVisible(false);
        fastPparameter.setVisible(false);
        fastqcPanel.setVisible(false);
        trimPanel.setVisible(false);
//2.2- SAMPLE COLLECTION
        sampleDataPanel.setVisible(true);
//2.3- ALIGNMENT
        starPanel.setVisible(false);
//2.4- EXPRESSION ANALYSIS
        analysisModePanel.setVisible(false);
        resDEPanel.setVisible(false);//DESeq2 Matrix & Results
        complexDesignPanel.setVisible(false);
        interactionTermPanel.setVisible(false);
        edgeRDesignPanel.setVisible(false);
        resEDPanel.setVisible(false);// edgeR Results
//2.5- GENE ENRICHMENT
        geneEnrichmentPanel.setVisible(false);

        //// END SETTING ALL PANELS
        contrastFactorCombo.setEnabled(false);

        warningLabel.setText(null);

    }//Constructor
//##############################################################################

    public void setLowerCaseCellEditor(JTable table) {
        JTextField textField = new JTextField();

        // Add a focus listener to convert text to lowercase
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String text = textField.getText();
                textField.setText(text.toLowerCase());
            }
        });

        // Use the configured text field as the cell editor
        DefaultCellEditor lowerCaseEditor = new DefaultCellEditor(textField) {
            @Override
            public Object getCellEditorValue() {
                // Ensure the value returned is always lowercase
                return textField.getText().toLowerCase();
            }
        };

        // Apply the editor to all columns
        table.setDefaultEditor(Object.class, lowerCaseEditor);
    }

    public void resetSettings() {

// RESET INPUT ARRAYS
        sfileNames.clear();
        sfilePaths.clear();
        sfileExtensions.clear();

        p1fileNames.clear();
        p1filePaths.clear();
        p1fileExtensions.clear();

        p2fileNames.clear();
        p2filePaths.clear();
        p2fileExtensions.clear();

//RESET INPUT LIST MODELS
        listModelreadSingle.clear();
        listModelreadPair1.clear();
        listModelreadPair2.clear();
// FAST QC RESET
        fastqcResultCombo.setEnabled(false);
        fastqcResultCombo.removeAllItems();
        filesForCombo.clear();
        dm.setRowCount(0);

    }

    public void RNASeq1() {

        output_Dir.setText("/mnt/ntfs/Output_RNASeq1/");
        genomeDir.setText("/media/iffy/Ventoy/Reference/star_index/");
        listModelfasta1.addElement("/media/iffy/Ventoy/Reference/star_index/GRCh38.p14.genome.fa");
        sjdbGTFfile.setText("/media/iffy/Ventoy/Reference/star_index/gencode.v48.annotation.gtf");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972486_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972434_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972487_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972431_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972494_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972496_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972505_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972480_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972461_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972440_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972450_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972426_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972446_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972414_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972419_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972412_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972428_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972427_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972484_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972475_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972443_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972410_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972478_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972490_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972433_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972506_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972465_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972477_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972476_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972442_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972454_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972416_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972421_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972408_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972474_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972479_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972482_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972435_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972504_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972432_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972413_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972409_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972462_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972473_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972467_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972458_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972415_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972471_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972502_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972472_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972448_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972464_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972449_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972488_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972445_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972436_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972483_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972430_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972417_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972453_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972501_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972420_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972451_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972447_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972457_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972411_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972459_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972422_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972452_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972444_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972456_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972429_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972441_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972466_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972407_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972493_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972499_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972469_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972485_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972438_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972489_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972460_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972495_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972439_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972481_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972497_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972498_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972437_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972492_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972425_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972468_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972500_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972418_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972423_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972503_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972463_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972424_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972470_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972491_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972455_1.fastq.gz\"");

        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972486_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972434_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972487_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972431_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972494_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972496_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972505_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972480_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972461_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972440_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972450_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972426_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972446_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972414_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972419_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972412_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972428_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972427_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972484_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972475_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972443_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972410_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972478_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972490_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972433_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972506_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972465_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972477_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972476_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972442_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972454_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972416_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972421_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972408_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972474_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972479_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972482_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972435_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972504_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972432_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972413_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972409_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972462_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972473_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972467_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972458_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972415_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972471_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972502_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972472_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972448_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972464_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972449_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972488_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972445_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972436_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972483_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972430_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972417_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972453_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972501_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972420_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972451_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972447_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972457_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972411_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972459_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972422_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972452_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972444_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972456_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972429_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972441_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972466_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972407_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972493_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972499_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972469_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972485_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972438_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972489_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972460_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972495_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972439_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972481_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972497_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972498_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972437_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972492_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972425_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972468_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972500_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972418_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972423_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972503_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972463_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972424_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972470_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972491_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/RNASeqData1/SRR20972455_2.fastq.gz\"");
    }

    public void RNASeqTB() {
        output_Dir.setText("/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/");
        genomeDir.setText("/media/nigab/sda1/Iffat/Reference/star_index/");
        listModelfasta1.addElement("/media/nigab/sda1/Iffat/Reference/star_index/GRCh38.p14.genome.fa");
        sjdbGTFfile.setText("/media/nigab/sda1/Iffat/Reference/star_index/gencode.v48.annotation.gtf");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476081_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475984_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476068_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476046_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476091_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476016_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476014_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476036_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476065_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476030_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476029_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476041_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476018_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475991_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476060_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476032_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476045_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475985_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476064_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475990_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476066_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476050_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476067_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476020_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476039_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476094_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476092_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476084_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476080_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476082_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476042_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476089_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475997_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476074_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476086_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476088_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476077_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476003_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476069_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475996_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476043_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476059_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476002_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476058_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475983_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476019_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476015_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476017_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476007_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476057_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476005_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476098_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476053_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476073_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476023_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475999_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476037_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476079_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476035_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475992_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476072_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476025_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476013_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476093_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476047_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476071_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475974_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476085_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476055_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476056_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476027_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475986_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476078_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476076_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476054_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476070_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476052_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475988_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476040_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476062_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476022_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476090_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476083_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476026_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476034_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475987_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476031_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476033_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476051_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476024_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476028_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475989_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476087_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476008_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476011_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476000_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476012_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475970_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475982_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475972_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475980_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475976_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476006_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475981_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476038_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475973_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476048_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476001_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476010_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476044_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475994_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475978_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475969_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476063_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476009_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475979_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476097_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476075_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476096_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476095_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476061_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475971_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475977_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476021_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475975_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475995_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475998_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476004_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476049_1.fastq.gz\"");
        listModelreadPair1.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475993_1.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476081_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475984_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476068_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476046_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476091_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476016_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476014_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476036_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476065_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476030_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476029_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476041_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476018_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475991_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476060_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476032_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476045_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475985_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476064_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475990_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476066_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476050_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476067_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476020_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476039_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476094_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476092_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476084_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476080_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476082_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476042_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476089_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475997_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476074_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476086_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476088_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476077_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476003_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476069_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475996_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476043_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476059_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476002_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476058_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475983_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476019_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476015_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476017_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476007_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476057_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476005_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476098_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476053_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476073_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476023_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475999_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476037_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476079_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476035_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475992_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476072_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476025_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476013_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476093_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476047_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476071_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475974_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476085_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476055_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476056_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476027_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475986_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476078_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476076_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476054_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476070_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476052_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475988_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476040_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476062_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476022_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476090_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476083_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476026_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476034_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475987_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476031_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476033_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476051_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476024_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476028_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28475989_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/Zainab_Project/fastqFiles/SRR28476087_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476008_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476011_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476000_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476012_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475970_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475982_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475972_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475980_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475976_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476006_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475981_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476038_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475973_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476048_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476001_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476010_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476044_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475994_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475978_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475969_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476063_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476009_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475979_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476097_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476075_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476096_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476095_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476061_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475971_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475977_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476021_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475975_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475995_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475998_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476004_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28476049_2.fastq.gz\"");
        listModelreadPair2.addElement("\"/media/nigab/sdc1/Wajya/Iffat/Cohort2/RNASeq_Cohort2/FastP0/SRR28475993_2.fastq.gz\"");
    }

    public void CL_Data(){
        listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164495_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164508_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164516_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164520_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164504_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164496_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164513_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164501_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164502_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164514_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164512_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164515_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164518_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164509_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164524_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164521_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164503_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164497_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164500_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164499_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164505_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164519_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164517_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164507_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164511_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164523_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164498_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164522_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164506_1.fastq.gz\"");
listModelreadPair1.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164510_1.fastq.gz\"");

listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164495_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164508_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164516_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164520_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164504_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164496_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164513_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164501_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164502_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164514_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164512_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164515_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164518_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164509_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164524_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164521_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164503_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164497_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164500_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164499_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164505_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164519_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164517_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164507_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164511_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164523_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164498_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164522_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164506_2.fastq.gz\"");
listModelreadPair2.addElement("\"/media/nigab/sda1/Iffat/CL_fastq/SRR21164510_2.fastq.gz\"");


    }
    
    public void exampleInputData() {

         RNASeq1();
        // CL_Data();
      //  RNASeqTB();
// For NIGAB PC:
//        output_Dir.setText("/media/nigab/sdc1/Wajya/Iffat/Output_RNASeq1/");
//        genomeDir.setText("/media/nigab/sda1/Iffat/Reference/star_index/");
//        listModelfasta1.addElement("/media/nigab/sda1/Iffat/Reference/star_index/GRCh38.p14.genome.fa");
//        sjdbGTFfile.setText("/media/nigab/sda1/Iffat/Reference/star_index/gencode.v48.annotation.gtf");

    

    ////FOR UNI
//        output_Dir.setText("/media/nigab/sda1/Iffat/Output_RNASeq/");
//        genomeDir.setText("/media/nigab/sda1/Iffat/Reference/star_index");
//        listModelfasta1.addElement("/media/nigab/sda1/Iffat/Reference/star_index/GRCh38.p14.genome.fa");
//        sjdbGTFfile.setText("/media/nigab/sda1/Iffat/Reference/star_index/gencode.v48.annotation.gtf");
        


//PROJECT
    
        
//        output_Dir.setText("/home/iffy/TestFiles//Output_Dir/");
//        genomeDir.setText("/media/dr/9d1c8592-358c-4c9b-bfe6-d7c6e2a62be0/home/computer/Iffat/Genomes_indexed");
//        listModelfasta1.addElement("/media/dr/9d1c8592-358c-4c9b-bfe6-d7c6e2a62be0/home/computer/Iffat/Genomes_indexed/GRCh38.p13.genome.fa");
//        sjdbGTFfile.setText("/media/dr/9d1c8592-358c-4c9b-bfe6-d7c6e2a62be0/home/computer/Iffat/Genomes_indexed/gencode.v38.chr_patch_hapl_scaff.annotation.gtf");
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//For picture
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475969_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475969_2.fastq.gz\"");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475970_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475970_2.fastq.gz\"");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475971_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475971_2.fastq.gz\"");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475972_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475972_2.fastq.gz\"");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475973_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475973_2.fastq.gz\"");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475975_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475975_2.fastq.gz\"");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475976_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475976_2.fastq.gz\"");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475978_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475978_2.fastq.gz\"");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475979_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475979_2.fastq.gz\"");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475980_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475980_2.fastq.gz\"");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475981_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475981_2.fastq.gz\"");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475982_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475982_2.fastq.gz\"");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475993_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475993_2.fastq.gz\"");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475994_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475994_2.fastq.gz\"");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475995_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475995_2.fastq.gz\"");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475998_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28475998_2.fastq.gz\"");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28476000_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28476000_2.fastq.gz\"");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28476001_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28476001_2.fastq.gz\"");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28476004_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/InputFiles/trimmed/SRR28476004_2.fastq.gz\"");
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//        listModelreadPair1.addElement("\"/media/dr/9d1c8592-358c-4c9b-bfe6-d7c6e2a62be0/home/computer/Mam_iffat/InputFiles/SRR9697593_1.fastq.gz\"");
//        listModelreadPair2.addElement("\"/media/dr/9d1c8592-358c-4c9b-bfe6-d7c6e2a62be0/home/computer/Mam_iffat/InputFiles/SRR9697593_2.fastq.gz\"");
//MOUSEEXAMPLE
//        output_Dir.setText("/home/iffy/TestFiles/FINALOUTPUTS/");
//        genomeDir.setText("/home/iffy/TestFiles/MouseExample/GENOME");
//        listModelfasta1.addElement("/home/iffy/TestFiles/MouseExample/INPUTS/GenomeFasta/mm10.fa");
//        sjdbGTFfile.setText("/home/iffy/TestFiles/MouseExample/INPUTS/ref-transcripts.gtf");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/MouseExample/INPUTS/Fastq/SRR8697626_1.fq.gz\"");
//        listModelreadPair1.addElement("\"/home/iffy/TestFiles/MouseExample/INPUTS/Fastq/SRR8697627_1.fq.gz\"");
//
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/MouseExample/INPUTS/Fastq/SRR8697626_2.fq.gz\"");
//        listModelreadPair2.addElement("\"/home/iffy/TestFiles/MouseExample/INPUTS/Fastq/SRR8697627_2.fq.gz\"");
        //  listModelreadSingle.addElement("\"/home/iffy/TestFiles/MouseExample/INPUTS/Mmusculus_3.fq.gz\"");
//        listModelreadSingle.addElement("\"/home/iffy/TestFiles/MouseExample/INPUTS/Mmusculus_4.fq.gz\"");
//        listModelreadSingle.addElement("\"/home/iffy/TestFiles/MouseExample/INPUTS/mt_1.fq.gz\"");
//        listModelreadSingle.addElement("\"/home/iffy/TestFiles/MouseExample/INPUTS/mt_2.fq.gz\"");
//        listModelreadSingle.addElement("\"/home/iffy/TestFiles/MouseExample/INPUTS/mt_3.fq.gz\"");
//        listModelreadSingle.addElement("\"/home/iffy/TestFiles/MouseExample/INPUTS/mt_4.fq.gz\"");
        //       adapterfile.setText("/home/iffy/NetBeansProjects/NGSGradle/app/bin/Trimmomatic-0.39/adapters/TruSeq2-SE.fa");
//
//
//        //HUMAN EXAMPLE
//        listModelreadSingle.addElement("\"/home/iffy/TestFiles/HUMANDATA/SRR20701748_1.fastq.gz\"");
//        listModelreadSingle.addElement("\"/home/iffy/TestFiles/HUMANDATA/SRR20701748_2.fastq.gz\"");
//
//      //  listModelreadPair1.addElement("\"/home/iffy/TestFiles/HUMANDATA/SRR20701748_1.fastq.gz\"");
//      //  listModelreadPair2.addElement("\"/home/iffy/TestFiles/HUMANDATA/SRR20701748_2.fastq.gz\"");
//
//
//        adapterfile.setText("/home/iffy/NetBeansProjects/NGSGradle/app/bin/Trimmomatic-0.39/adapters/TruSeq2-SE.fa");
//        //adapterfile.setText("/home/iffy/NetBeansProjects/NGSGradle/app/bin/Trimmomatic-0.39/adapters/TruSeq2-PE.fa");
//
//        genomeDir.setText("/home/iffy/TestFiles/Genomes");
//        listModelfasta1.addElement("/home/iffy/TestFiles/GenomeFasta/GRCh38.p13.genome.fa");
//        sjdbGTFfile.setText("/home/iffy/TestFiles/GenomeFasta/gencode.v39.chr_patch_hapl_scaff.annotation.gtf");
//
    }

    public void inputFilesData() {
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

        sampleTableModel.setRowCount(0);

//1-READ INPUT FILES (Name, Extension,Path)
        if (singleRadioButton.isSelected()) {
            for (int i = 0; i < readFilesInSingle.getModel().getSize(); i++) {
                sfilePaths.add(i, readFilesInSingle.getModel().getElementAt(i).replaceAll("\"", "").trim());

                if ((sfilePaths.get(i).endsWith(".gz")) || (sfilePaths.get(i).endsWith(".bz2"))) {

                    sfileNames.add(i, FilenameUtils.getBaseName(FilenameUtils.removeExtension(sfilePaths.get(i))));
                    sfileExtensions.add(i, sfilePaths.get(i).substring(sfilePaths.get(i).lastIndexOf(sfileNames.get(i)) + sfileNames.get(i).length() + 1));
                    fileMode = "zipped";
                } else {

                    sfileNames.add(i, FilenameUtils.getBaseName(sfilePaths.get(i)));
                    sfileExtensions.add(i, sfilePaths.get(i).substring(sfilePaths.get(i).lastIndexOf(sfileNames.get(i)) + sfileNames.get(i).length() + 1));
                    fileMode = "unzipped";
                }

            }//endFor

//2-Fill the data in Sample Table
            if (fastqcRadio.isSelected()) { //WITH TRIM
                for (int i = 0; i < sfileNames.size(); i++) {
                    sampleTableModel.addRow(new Object[]{sfileNames.get(i)});
                }
            }

        } else if (pairRadioButton.isSelected()) {

            //PAIR 1
            for (int i = 0; i < readFilesInPair1.getModel().getSize(); i++) {

                p1filePaths.add(i, readFilesInPair1.getModel().getElementAt(i).replaceAll("\"", "").trim());

                if ((p1filePaths.get(i).endsWith(".gz")) || (p1filePaths.get(i).endsWith(".bz2"))) {

                    p1fileNames.add(i, FilenameUtils.getBaseName(FilenameUtils.removeExtension(p1filePaths.get(i))));
                    p1fileExtensions.add(i, p1filePaths.get(i).substring(p1filePaths.get(i).lastIndexOf(p1fileNames.get(i)) + p1fileNames.get(i).length() + 1));
                    fileMode = "zipped";
                } else {

                    p1fileNames.add(i, FilenameUtils.getBaseName(p1filePaths.get(i)));
                    p1fileExtensions.add(i, p1filePaths.get(i).substring(p1filePaths.get(i).lastIndexOf(p1fileNames.get(i)) + p1fileNames.get(i).length() + 1));
                    fileMode = "unzipped";
                }

            }//for

            //PAIR 2
            for (int i = 0; i < readFilesInPair2.getModel().getSize(); i++) {
                p2filePaths.add(i, readFilesInPair2.getModel().getElementAt(i).replaceAll("\"", "").trim());

                if ((p2filePaths.get(i).endsWith(".gz")) || (p2filePaths.get(i).endsWith(".bz2"))) {

                    p2fileNames.add(i, FilenameUtils.getBaseName(FilenameUtils.removeExtension(p2filePaths.get(i))));
                    p2fileExtensions.add(i, p2filePaths.get(i).substring(p2filePaths.get(i).lastIndexOf(p2fileNames.get(i)) + p2fileNames.get(i).length() + 1));

                } else {

                    p2fileNames.add(i, FilenameUtils.getBaseName(p2filePaths.get(i)));
                    p2fileExtensions.add(i, p2filePaths.get(i).substring(p2filePaths.get(i).lastIndexOf(p2fileNames.get(i)) + p2fileNames.get(i).length() + 1));
                }

            }//for

            if (fastqcRadio.isSelected()) { //WITH TRIM
                //Fill the data in Sample Table
                for (int i = 0; i < p1fileNames.size(); i++) {
                    sampleTableModel.addRow(new Object[]{p1fileNames.get(i)});
                    sampleTableModel.addRow(new Object[]{p2fileNames.get(i)});

                }
            }
        }//endif

//END READ INPUT FILES
    }

    public void inputFilesDataS(Map<String, List<String>> fileMap, String key) {

//RESETING ALL ARRAYS
        sfileNames = new ArrayList<>();
        sfilePaths = new ArrayList<>();
        sfileExtensions = new ArrayList<>();
        sampleTableModel.setRowCount(0);

//READ INPUT FILES
        List<String> filePaths = fileMap.getOrDefault(key, List.of());
        int i = 0;
        for (String fullPath : filePaths) {
            sfilePaths.add(i, fullPath.replaceAll("\"", "").trim());

            if ((sfilePaths.get(i).endsWith(".gz")) || (sfilePaths.get(i).endsWith(".bz2"))) {

                sfileNames.add(i, FilenameUtils.getBaseName(FilenameUtils.removeExtension(sfilePaths.get(i))));
                sfileExtensions.add(i, sfilePaths.get(i).substring(sfilePaths.get(i).lastIndexOf(sfileNames.get(i)) + sfileNames.get(i).length() + 1));
                fileMode = "zipped";
            } else {

                sfileNames.add(i, FilenameUtils.getBaseName(sfilePaths.get(i)));
                sfileExtensions.add(i, sfilePaths.get(i).substring(sfilePaths.get(i).lastIndexOf(sfileNames.get(i)) + sfileNames.get(i).length() + 1));
                fileMode = "unzipped";
            }
            i++;
        }//endFor

        //2-Fill the data in Sample Table
        for (i = 0; i < sfileNames.size(); i++) {
            sampleTableModel.addRow(new Object[]{sfileNames.get(i)});
        }

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
        sampleTableModel.setRowCount(0);

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
                fileMode = "zipped";
            } else {
                String baseName = FilenameUtils.getBaseName(p1filePaths.get(i));
                p1p2fileNames.add(baseName.replaceAll("_[12]$", ""));
                p1fileNames.add(i, baseName);
                p1fileExtensions.add(i, p1filePaths.get(i).substring(p1filePaths.get(i).lastIndexOf(p1fileNames.get(i)) + p1fileNames.get(i).length() + 1));
                fileMode = "unzipped";
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
        //Fill the data in Sample Table
        for (i = 0; i < p1fileNames.size(); i++) {
            sampleTableModel.addRow(new Object[]{p1p2fileNames.get(i)});

        }

//END READ INPUT FILES
    }

    public boolean checkDown() {

        //Check STAR
        if (starThread.getText().isBlank()
                || runMode2.getText().isBlank()
                || outFileNamePrefix.getText().isBlank()
                || sjdbOverhang2.getText().isBlank()
                || jTextField4.getText().isBlank()
                || jTextField5.getText().isBlank()
                || outBAMsortingBinsN.getText().isBlank()
                || outSAMmapqUnique.getText().isBlank()
                || genomeDir.getText().isBlank()
                || sjdbGTFfile.getText().isBlank()
                || listModelfasta1.isEmpty()
                || countThresh.getText().isBlank()) {
            warningLabel.setText("**ERROR: Fill all fields");
            return false;
        } //Check Analysis
        if (!tableLockBtn.isSelected()) {
            warningLabel.setText("**ERROR: Lock the table");
            return false;
        } //Check deseq results
        if (singleFactorRadio.isSelected() == false && multiFactorRadio.isSelected() == false) {
            warningLabel.setText("**ERROR: Select any analysis mode");
            return false;

        }
        if (multiFactorRadio.isSelected() == true) {

            if (interactionTermCombo.getSelectedIndex() == 1) {
                if (interactionList.isSelectionEmpty()) {
                    warningLabel.setText("**ERROR: Please select one or more interaction terms.");
                    return false;
                }
            }

        }
        if (analysisP == "deSeq2" || analysisP == "both") {

            if (jRadioContrast.isSelected()) {
                if (combination1.getSelectedIndex() == combination2.getSelectedIndex()) {
                    warningLabel.setText("**ERROR: Both factor levels should be different");
                    return false;
                }

            } else if (jRadioList.isSelected()) {

                if (!((deFactorList.getSelectedIndices().length > 0) && (deFactorList.getSelectedIndices().length <= 2))) {
                    warningLabel.setText("Select at least one or maximum two terms from the list");
                    return false;
                }
            }

        }
        if (analysisP == "edgeR" || analysisP == "both") {

            if (edgeRTestCombo.getSelectedIndex() == 0) {
                warningLabel.setText("Select any test type.");
                return false;
            } else if (edgeRTestCombo.getSelectedIndex() == 1) {
                if (edgeRTestList.getSelectedIndices().length != 2) {
                    warningLabel.setText("Must select two coefficents");
                    return false;
                }

            } else if ((edgeRTestCombo.getSelectedIndex() == 2) || (edgeRTestCombo.getSelectedIndex() == 4) || (edgeRTestCombo.getSelectedIndex() == 5)) {
                if (edgeRTestList.getSelectedIndices().length < 1) {
                    warningLabel.setText("Select any coefficent(s)");
                    return false;
                }

            } else if (edgeRTestCombo.getSelectedIndex() == 3) {
                // Regular expression for a comma-separated list of number
                String pattern = "^[-]?\\\\d+(\\\\.\\\\d+)?(,[-]?\\\\d+(\\\\.\\\\d+)?)*$";
                if (contrastValue.getText().isBlank()) {
                    warningLabel.setText("**ERROR: Fill all fields");
                    return false;
                } else if (!(contrastValue.getText().trim().matches(pattern))) {
                    warningLabel.setText("**ERROR: Contrast pattern isn't correct  ");
                    return false;
                }

            }

        }
        if (jTextField8.getText().isBlank()
                || jTextField9.getText().isBlank()
                || jTextField6.getText().isBlank()
                || jTextField7.getText().isBlank()
                || jTextField12.getText().isBlank()
                || jTextField11.getText().isBlank()
                || jTextField13.getText().isBlank()
                || jTextField15.getText().isBlank()
                || jTextField16.getText().isBlank()
                || jTextField14.getText().isBlank()
                || jTextField17.getText().isBlank()) {
            warningLabel.setText("**ERROR: Fill all fields");
            return false;
        }

        return true;

    }

    //#########################################################################
    // TOOL METHODS
    public void runFastP(String selectedKey) {
        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

            @Override
            protected Integer doInBackground() {
                try {
                    pipelineError = false;
                    ProcessBuilder builder = new ProcessBuilder();
                    List<String> cmdList = new ArrayList<String>();
                    cmdList.add("sh");
                    cmdList.add(System.getProperty("user.dir").concat("/bin/fastp.sh"));
                    cmdList.add(App.FastP_PATH); //$1
                    cmdList.add(App.MULTIQC_PATH); //2

                    String myArray = "";
                    filesForComboP = new ArrayList<>();
                    if (singleRadioButton.isSelected()) {
                        cmdList.add("SINGLE");//3

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
                        cmdList.add(myArray); //4
                    } else if (pairRadioButton.isSelected()) {
                        cmdList.add("PAIR");//3

                        Map<String, List<String>> innerMap = pairPathMap.getOrDefault(selectedKey, Map.of());

                        List<String> r1List = innerMap.getOrDefault("R1", List.of());
                        List<String> r2List = innerMap.getOrDefault("R2", List.of());

                        for (int i = 0; i < r1List.size(); i++) {
                            myArray += (r1List.get(i).trim() + "#" + r2List.get(i).trim() + ",");
                            filesForComboP.add("FastP" + fastCounter + "/" + p1p2fileNames.get(i) + ".html");

                        }//for
                        myArray = StringUtils.chop(myArray).replaceAll("\"", "");
                        myArray = myArray.trim();
                        cmdList.add(myArray); //4

//                        if (readFilesInPair1.getModel().getSize() == readFilesInPair2.getModel().getSize()) {
//                            for (int i = 0; i < readFilesInPair1.getModel().getSize(); i++) {
//                                myArray += (readFilesInPair1.getModel().getElementAt(i) + "#" + readFilesInPair2.getModel().getElementAt(i) + ",");
//                                filesForComboP.add(p1fileNames.get(i) + ".html");
//                                //filesForComboP.add(p1fileNames.get(i) + ".json");
//                            }//for
//                            myArray = StringUtils.chop(myArray).replaceAll("\"", "");
//                            myArray = myArray.trim();
//                            cmdList.add(myArray); //3
//                        }
                    }//endif

                    cmdList.add(output_Dir.getText());//5
                    cmdList.add(fastCounter + "");//6

                    if (fastpTrimCombo.getSelectedIndex() == 1) //7
                    {
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
//                    //Changing input lists path to fastp paths
//                    if (singleRadioButton.isSelected()) {
//                        listModelreadSingle.clear();
//                        for (int i = 0; i < sfileNames.size(); i++) {
//                            listModelreadSingle.addElement(output_Dir.getText() + sfileNames.get(i) + "." + sfileExtensions.get(i));
//
//                        }//for
//
//                    } else if (pairRadioButton.isSelected()) {
//                        listModelreadPair1.clear();
//                        listModelreadPair2.clear();
//                        for (int i = 0; i < p1fileNames.size(); i++) {
//
//                            listModelreadPair1.addElement(output_Dir.getText() + p1fileNames.get(i) + "." + p1fileExtensions.get(i));
//
//                        }//for
//                        for (int i = 0; i < p2fileNames.size(); i++) {
//                            listModelreadPair2.addElement(output_Dir.getText() + p2fileNames.get(i) + "." + p2fileExtensions.get(i));
//
//                        }//for
//
//                    }//if

                }//error
            }//end Done

        };

        worker.execute();

    }//end runFastqc

    public void runFastqc(String selectedKey) {
        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress: Running FastQC", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
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
//FASTQC Windows :Dir_with_FastQC>java -Xmx250m -classpath .;./sam-1.103.jar;./jbzip2-0.9.jar uk.ac.babraham.FastQC.FastQCApplication ju.fq
                    ProcessBuilder builder = new ProcessBuilder();
                    ++fastCounter;
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
                    runFastqc("FastQC" + fastCounter);

                }
            }//DONE

        };

        worker.execute();

    }//end Trimmomatic

    public void runSTAR() {

        warningLabel.setText(null);
        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

            @Override
            protected Integer doInBackground() {
                Process p = null;
                try {

                    pipelineError = false;
                    List<String> cmdList = new ArrayList<String>();

                    cmdList.add("sh");
                    cmdList.add(System.getProperty("user.dir").concat("/bin/STAR_RNA.sh"));
                    //PATHS:
                    cmdList.add(App.STAR_PATH); //$1
                    cmdList.add(App.PICARD_PATH);//2
                    cmdList.add(App.GATK_PATH);//3
                    cmdList.add(App.SUBREAD_PATH);//4

                    //REF GENOME YES
                    cmdList.add(starThread.getText());//5
                    cmdList.add(runMode2.getText());//6
                    cmdList.add(genomeDir.getText());//7

                    if (runModeCombo.getSelectedIndex() == 1)//MULTIPLE
                    {

                        //SINGLE READ -- COMMA SEPARATED  8
                        if (singleRadioButton.isSelected()) {

                            String myArray = "";

                            List<String> paths = singlePathMap.getOrDefault(selectedKey, List.of());

                            for (int i = 0; i < paths.size(); i++) {  //readFilesInSingle.getModel().getSize()

                                myArray += (paths.get(i) + ",").trim();//readFilesInSingle.getModel().getElementAt(i)
                            }
                            myArray = StringUtils.chop(myArray).replaceAll("\"", "");
                            myArray = "\"" + myArray + "\"";

                            cmdList.add(myArray); //--readFilesIn 8

                        } else if (pairRadioButton.isSelected()) //PAIR1 COMMA SEPARATED SPACE PAIR2 COMMA SEPARATED
                        {
                            String myArray = "";
                            Map<String, List<String>> innerMap = pairPathMap.getOrDefault(selectedKey, Map.of());
                            List<String> r1List = innerMap.getOrDefault("R1", List.of());
                            List<String> r2List = innerMap.getOrDefault("R2", List.of());

                            for (int i = 0; i < r1List.size(); i++) {//readFilesInPair1.getModel().getSize()
                                myArray += (r1List.get(i) + ",").trim();//readFilesInPair1.getModel().getElementAt(i) 
                            }

                            myArray = StringUtils.chop(myArray).replaceAll("\"", "");
                            myArray = "\"" + myArray + "\" ";

                            String myArray2 = "";
                            for (int i = 0; i < r2List.size(); i++) {//readFilesInPair2.getModel().getSize()
                                myArray2 += (r2List.get(i) + ",").trim();//readFilesInPair2.getModel().getElementAt(i)
                            }
                            myArray2 = StringUtils.chop(myArray2).replaceAll("\"", "");
                            myArray2 = "\"" + myArray2 + "\"";

                            myArray = myArray + myArray2;
                            cmdList.add(myArray); //--readFilesIn 8

                        }

                    }//end outer if
                    else if (runModeCombo.getSelectedIndex() == 0) //SINGLE PER RUN
                    {

                        //ReadFilesIn 8     Separated by SPACE
                        if (singleRadioButton.isSelected()) {

                            String myArray = "";
                            List<String> paths = singlePathMap.getOrDefault(selectedKey, List.of());
                            for (int i = 0; i < paths.size(); i++) {

                                myArray += paths.get(i).trim() + " ";
                            }

                            cmdList.add(myArray); //--readFilesIn 8

                        } else if (pairRadioButton.isSelected()) {
                            String myArray1, myArray2, myArray = "";
                            Map<String, List<String>> innerMap = pairPathMap.getOrDefault(selectedKey, Map.of());
                            List<String> r1List = innerMap.getOrDefault("R1", List.of());
                            List<String> r2List = innerMap.getOrDefault("R2", List.of());

                            for (int i = 0; i < r1List.size(); i++) {
                                myArray1 = r1List.get(i).trim();
                                myArray2 = r2List.get(i).trim();
                                myArray1 = myArray1 + "," + myArray2;

                                myArray += myArray1 + " ";
                            }
                            myArray = StringUtils.chop(myArray);

                            cmdList.add(myArray); //--readFilesIn 8

                        }

                    }

                    cmdList.add(readFilesCommand.getSelectedItem().toString());//9
                    cmdList.add(sjdbOverhang2.getText()); //10

                    cmdList.add(outSAMtype2.getSelectedItem().toString()); //11
                    cmdList.add(twopassMode.getSelectedItem().toString());//12
                    cmdList.add(outFileNamePrefix.getText());//13 Sample name (only in case of MULTIPLE)
                    cmdList.add(jTextField4.getText());//14
                    cmdList.add(jTextField5.getText());//15
                    cmdList.add(quantMode.getSelectedItem().toString());//16
                    cmdList.add(sjdbGTFfile.getText());//17
                    cmdList.add(output_Dir.getText());//18

                    cmdList.add(read);//19 SINGLE/PAIR
                    cmdList.add(mode);//20 SINGLE/MULTIPLE
                    if (runModeCombo.getSelectedIndex() == 0) {
                        if (eachRadio.isSelected()) {
                            cmdList.add("EACH");//21
                        } else if (combinedRadio.isSelected()) {
                            cmdList.add("COMBINED");//21
                        }
                    } else {
                        cmdList.add("");//21
                    }
                    cmdList.add(fileMode);//22
                    cmdList.add(outBAMsortingBinsN.getText());//23
                    cmdList.add(outSAMmapqUnique.getText());//24
                    cmdList.add(outSAMunmapped.getSelectedItem().toString());//25
                    //display("STAR commands", cmdList.toString());
                    ProcessBuilder pb = new ProcessBuilder(cmdList);
                    pb.redirectErrorStream(true);
                    try {
                        p = pb.start();
                    } catch (Exception e) {
                        App.LOGGER.info("\r\n" + e.toString() + ":" + e.getMessage());
                        publish("\r\n" + e.toString() + ":" + e.getMessage());
                        pipelineError = true;
                        return null;

                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        App.LOGGER.info("\r\n" + line);
                        publish("\r\n" + line);

                        if (line.toLowerCase().contains("killed")
                                || line.toLowerCase().contains("aborted")
                                || line.toLowerCase().contains("error")
                                || line.toLowerCase().contains("exception")) {
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
                    if (combinedRadio.isSelected()) {

                        combinedFeatures(output_Dir.getText());
                    }
                          runBAMCharts(System.getProperty("user.dir").concat("/bin/bamCharts.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"));

                }//error
            }//done

        };

        worker.execute();

    }//end STAR

    ///////////////////////////////////////////////////////////////////////////////
    // R METHODS
 public void runDESeqMatrix(String rFilePath, String workDir, String rLib) {
        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));

        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

            @Override
            protected Integer doInBackground() {
                Process p;
                try {
                    rcodeError = false;

                    List<String> cmdList = new ArrayList<String>();

                    cmdList.add("Rscript");
                    cmdList.add(rFilePath); //R Script Path
                    cmdList.add(workDir);  //Working Directory 1
                    cmdList.add(rLib);//2

                    if (filteringCombo.getSelectedIndex() == 0) // None,   
                    {
                        cmdList.add("none");//3
                        cmdList.add(""); //4
                        cmdList.add(""); //5
                    } else if (filteringCombo.getSelectedIndex() == 1) { //Total Count ≥ X,
                        cmdList.add("countonly");//3
                        cmdList.add(countThresh.getText()); //4
                        cmdList.add(""); //5
                    } else if (filteringCombo.getSelectedIndex() == 2) {// Present in ≥ Y% Samples,
                        cmdList.add("percentageonly");//3
                        cmdList.add(""); //4
                        cmdList.add(countThresh1.getText()); //5
                    } else if (filteringCombo.getSelectedIndex() == 3) {// Both
                        cmdList.add("both");
                        cmdList.add(countThresh.getText()); //3
                        cmdList.add(countThresh1.getText()); //4
                    }

                    if (singleFactorRadio.isSelected()) { //6
                        cmdList.add("SINGLE");
                    } else if (multiFactorRadio.isSelected()) {
                        cmdList.add("MULTIPLE");

                        if (interactionTermCombo.getSelectedIndex() == 1) { //7,8
                            //YES
                            cmdList.add(interactionTermCombo.getSelectedItem().toString());//7 
                            cmdList.add(String.join(",", interactionList.getSelectedValuesList())); //8
                            // cmdList.add("");//interaction2

                        } else if (interactionTermCombo.getSelectedIndex() == 0) {
                            //NO
                            cmdList.add(interactionTermCombo.getSelectedItem().toString());//7

                        }
                    }

                    ProcessBuilder pb = new ProcessBuilder(cmdList);
                    pb.redirectErrorStream(true);
                    p = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                    String line;
                    int i = 0;

                    while ((line = reader.readLine()) != null) {
                        App.LOGGER.info("\r\n" + line);

                        if (line.contains("Coordinate system already present.")
                                || line.contains("using 'apeglm' for LFC shrinkage.")
                                || line.contains(" Zhu, A.,")
                                || line.contains("sequence count data:")
                                || line.contains(" Bioinformatics. https://doi.org/10.1093/bioinformatics/bty895")
                                || line.contains("replace the existing one.")) {
                            continue;
                        }

                        publish("\r\n" + line);

//                        if (line.toLowerCase().contains("error:") || line.toLowerCase().contains("error in")) {
//
//                            rcodeError = true;
//                            if (line.contains("Error in checkFullRank(modelMatrix) :")) {
//                                publish("\r\n" + "To resolve this, please review the Cross Table of the Sample Data Frame and also avoid redundant interaction terms.\r\n Ensure there are no 0 values for any conditions.\r\n If you find any 0 values, adjust the sample data so that all conditions have non-zero counts, allowing DESeq2 to perform the analysis correctly.");
//                            }
//
//                            break;
//                        }
                    }//while

                    while ((line = stdErr.readLine()) != null) {
                        App.LOGGER.error("\r\n" + line);

                        // Now your error detection works reliably
                        if (line.toLowerCase().contains("error:") || line.toLowerCase().contains("error in")) {
                            rcodeError = true;

                            if (line.contains("Error in checkFullRank(modelMatrix) :")) {
                                publish("\r\nTo resolve this, please review the Cross Table of the Sample Data Frame and also avoid redundant interaction terms.\r\nEnsure there are no 0 values for any conditions.\r\nIf you find any 0 values, adjust the sample data so that all conditions have non-zero counts, allowing DESeq2 to perform the analysis correctly.");
                            }
                            break;
                        }

                        publish("\r\n" + line);  // print full error lines too
                    }

                    //////////////////////////////////////
                    if (!isCancelled()) {
                        status = p.waitFor();
                    }

                    p.destroy();

                } catch (IOException | InterruptedException ex) {

                    ex.printStackTrace(new PrintWriter(App.stack));
                    App.LOGGER.error("\r\n ERROR in R analysis: " + App.stack);
                    App.textArea.append("\r\n ERROR in R analysis: " + App.stack);
                    rcodeError = true;

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
                if (!rcodeError) {

                    runDESeqResult(System.getProperty("user.dir").concat("/bin/DESeqResults.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"));

                }//endif

            }

        };

        worker.execute();

    }

    public void runBAMCharts(String rFilePath, String workDir, String rLib) {
        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress: Creating BAM files graphs...Please Wait...", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));

        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

            @Override
            protected Integer doInBackground() {
                Process p;
                try {
                    rcodeError = false;

                    List<String> cmdList = new ArrayList<String>();

                    cmdList.add("Rscript");
                    cmdList.add(rFilePath); //R Script Path
                    cmdList.add(workDir);  //Working Directory 1
                    cmdList.add(rLib);//2

                    ProcessBuilder pb = new ProcessBuilder(cmdList);
                    pb.redirectErrorStream(true);
                    p = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    BufferedReader stdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                    String line;
                    int i = 0;

                    while ((line = reader.readLine()) != null) {
                        App.LOGGER.info("\r\n" + line);

                        if (line.toLowerCase().contains("error")) {
                            rcodeError = true;
                            continue;
                        }

                        publish("\r\n" + line);

                    }//while

                    while ((line = stdErr.readLine()) != null) {
                        App.LOGGER.error("\r\n" + line);

                        // Now your error detection works reliably
                        if (line.toLowerCase().contains("error:") || line.toLowerCase().contains("error in")) {
                            rcodeError = true;

                            if (line.contains("Error in checkFullRank(modelMatrix) :")) {
                                publish("\r\nTo resolve this, please review the Cross Table of the Sample Data Frame and also avoid redundant interaction terms.\r\nEnsure there are no 0 values for any conditions.\r\nIf you find any 0 values, adjust the sample data so that all conditions have non-zero counts, allowing DESeq2 to perform the analysis correctly.");
                            }
                            break;
                        }

                        publish("\r\n" + line);  // print full error lines too
                    }

                    //////////////////////////////////////
                    if (!isCancelled()) {
                        status = p.waitFor();
                    }

                    p.destroy();

                } catch (IOException | InterruptedException ex) {

                    ex.printStackTrace(new PrintWriter(App.stack));
                    App.LOGGER.error("\r\n ERROR in R analysis: " + App.stack);
                    App.textArea.append("\r\n ERROR in R analysis: " + App.stack);
                    rcodeError = true;

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
                if (rcodeError == false) {

                    runDESeqResult(System.getProperty("user.dir").concat("/bin/DESeqResults.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"));

                }//endif

            }

        };

        worker.execute();

    }

    public void runDESeqResult(String rFilePath, String workDir, String rLib) {
        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));

        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

            @Override
            protected Integer doInBackground() {
                Process p;
                try {
                    rcodeError = false;

                    List<String> cmdList = new ArrayList<String>();

                    cmdList.add("Rscript");
                    cmdList.add(rFilePath); //R Script Path
                    cmdList.add(workDir);  //Working Directory 1
                    cmdList.add(rLib);//2
                    cmdList.add(mannualContrast.getText()); //alpha 3
                    cmdList.add(jTextField2.getText()); //padj 4
                    cmdList.add(jTextField1.getText()); //log2fold 5

                    if (jRadioContrast.isSelected()) {
                        cmdList.add(contrastFactorCombo.getSelectedItem().toString()); //selectedfactor 6
                        cmdList.add(combination1.getSelectedItem().toString()); //value1 7
                        cmdList.add(combination2.getSelectedItem().toString()); //value2 8
                    } else if (jRadioList.isSelected()) {
                        cmdList.add("LIST"); //selectedfactor 6

                        cmdList.add(deFactorList.getSelectedValuesList().toString().trim().replaceAll(" ", ".")); //value1
                        cmdList.add(""); //value2 8
                    }

                    ProcessBuilder pb = new ProcessBuilder(cmdList);
                    pb.redirectErrorStream(true);
                    p = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    int i = 0;

                    while ((line = reader.readLine()) != null) {
                        App.LOGGER.info("\r\n" + line);
                        publish("\r\n" + line);
                        if (line.startsWith("[1] \"Intercept,")) {
                            line = line.replaceAll("\"", "");
                            String[] parts = line.substring(3).split(",");
                            for (String str : parts) {
                                publish(str);
                            }

                        }//endif

                        if (line.toLowerCase().contains("error:") || line.toLowerCase().contains("error in")) {

                            rcodeError = true;

                            if (line.contains("Error in checkContrast(contrast, resNames) :")) {
                                publish("\r\n" + "To resolve this, please review the Cross Table of the Sample Data Frame.\r\n Ensure there are no 0 values for any conditions.\r\n If you find any 0 values, adjust the sample data so that all conditions have non-zero counts, allowing DESeq2 to perform the analysis correctly.");
                            }

                            break;
                        }

                    }//while

                    //////////////////////////////////////
                    if (!isCancelled()) {
                        Thread.sleep(1000); // Sleep for 1 second
                        status = p.waitFor();
                        Thread.sleep(1000); // Sleep for 1 second
                    }

                    p.destroy();

                } catch (IOException | InterruptedException ex) {

                    ex.printStackTrace(new PrintWriter(App.stack));
                    App.LOGGER.error("\r\n ERROR in R analysis: " + App.stack);
                    App.textArea.append("\r\n ERROR in R analysis: " + App.stack);
                    rcodeError = true;

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
                if (!rcodeError) {

                    if (analysisP.equalsIgnoreCase("both")) {
                        runEdgeRMatrix(System.getProperty("user.dir").concat("/bin/edgeRMatrix.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"));
                    } else {
                        runGSEAnalysis(analysisP);
                    }

                }//endif

            }

        };

        worker.execute();

    }

    public void runEdgeRMatrix(String rFilePath, String workDir, String rLib) {
        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));

        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

            @Override
            protected Integer doInBackground() {
                Process p;
                try {
                    rcodeError = false;

                    List<String> cmdList = new ArrayList<String>();

                    cmdList.add("Rscript");
                    cmdList.add(rFilePath); //R Script Path
                    cmdList.add(workDir);  //Working Directory 1
                    cmdList.add(rLib);//2
                    if (singleFactorRadio.isSelected()) { //3
                        cmdList.add("SINGLE");
                    } else if (multiFactorRadio.isSelected()) {
                        cmdList.add("MULTIPLE");
                    }
                    if (interceptOption.getSelectedIndex() == 1) {
                        cmdList.add("YES");
                    } else {
                        cmdList.add("NO");
                    }

                    ProcessBuilder pb = new ProcessBuilder(cmdList);
                    pb.redirectErrorStream(true);
                    p = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    int i = 0;

                    while ((line = reader.readLine()) != null) {
                        App.LOGGER.info("\r\n" + line);
                        publish("\r\n" + line);
                        if (line.startsWith("[1] \"Intercept,")) {
                            line = line.replaceAll("\"", "");
                            String[] parts = line.substring(3).split(",");
                            for (String str : parts) {
                                publish(str);
                            }

                        }//endif

                        if (line.toLowerCase().contains("error:") || line.toLowerCase().contains("error in")) {

                            //publish("NOTE: Check results.csv. Provided condition (padj < " + jTextField2.getText() + " and logFoldChange > " + jTextField1.getText() + ") is out of range");
                            rcodeError = true;
                            break;
                        }

                    }//while

                    //////////////////////////////////////
                    if (!isCancelled()) {
                        Thread.sleep(1000); // Sleep for 1 second
                        status = p.waitFor();
                        Thread.sleep(1000); // Sleep for 1 second
                    }

                    p.destroy();

                } catch (IOException | InterruptedException ex) {

                    ex.printStackTrace(new PrintWriter(App.stack));
                    App.LOGGER.error("\r\n ERROR in R analysis: " + App.stack);
                    App.textArea.append("\r\n ERROR in R analysis: " + App.stack);
                    rcodeError = true;

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
                if (!rcodeError) {

                    runEdgeRResult(System.getProperty("user.dir").concat("/bin/edgeRResults.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"));

                }//endif

            }

        };

        worker.execute();

    }

    public void runEdgeRResult(String rFilePath, String workDir, String rLib) {
        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));

        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

            @Override
            protected Integer doInBackground() {
                Process p;
                try {
                    rcodeError = false;

                    List<String> cmdList = new ArrayList<String>();

                    cmdList.add("Rscript");
                    cmdList.add(rFilePath); //R Script Path
                    cmdList.add(workDir);  //Working Directory 1
                    cmdList.add(rLib);//2
                    cmdList.add(jTextField22.getText());//pvalue3
                    cmdList.add(jTextField21.getText()); //logfc4
                    cmdList.add(edgeRTestCombo.getSelectedItem().toString());  //test type 5

                    if (interceptOption.getSelectedIndex() == 1) { //6
                        cmdList.add("YES");
                    } else {
                        cmdList.add("NO");
                    }

                    int index[] = edgeRTestList.getSelectedIndices();
                    List<String> coefValues = edgeRTestList.getSelectedValuesList();

                    if (edgeRTestCombo.getSelectedIndex() == 1)//exact
                    {
                        cmdList.add(coefValues.get(0));//7
                        cmdList.add(coefValues.get(1));//8

                    } else if ((edgeRTestCombo.getSelectedIndex() == 2)
                            || (edgeRTestCombo.getSelectedIndex() == 4)
                            || (edgeRTestCombo.getSelectedIndex() == 6))//glm coef,lrt
                    {

                        if (index.length == 1) {

                            cmdList.add(coefValues.get(0));//7
                            cmdList.add("");//8

                        } else if (index.length == 2) {

                            cmdList.add(coefValues.get(0));//7
                            cmdList.add(coefValues.get(1));//8
                        }

                    } else if ((edgeRTestCombo.getSelectedIndex() == 3) || (edgeRTestCombo.getSelectedIndex() == 5))//glm contrast
                    {

                        cmdList.add(contrastValue.getText());//7

                        cmdList.add("");//8

                    }

                    ProcessBuilder pb = new ProcessBuilder(cmdList);
                    pb.redirectErrorStream(true);
                    p = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    int i = 0;

                    while ((line = reader.readLine()) != null) {
                        App.LOGGER.info("\r\n" + line);
                        publish("\r\n" + line);
                        if (line.startsWith("[1] \"Intercept,")) {
                            line = line.replaceAll("\"", "");
                            String[] parts = line.substring(3).split(",");
                            for (String str : parts) {
                                publish(str);
                            }

                        }//endif

                        if (line.toLowerCase().contains("error:") || line.toLowerCase().contains("error in")) {

                            //publish("NOTE: Check results.csv. Provided condition (padj < " + jTextField2.getText() + " and logFoldChange > " + jTextField1.getText() + ") is out of range");
                            rcodeError = true;
                            break;
                        }

                    }//while

                    //////////////////////////////////////
                    if (!isCancelled()) {
                        Thread.sleep(1000); // Sleep for 1 second
                        status = p.waitFor();
                        Thread.sleep(1000); // Sleep for 1 second
                    }

                    p.destroy();

                } catch (IOException | InterruptedException ex) {

                    ex.printStackTrace(new PrintWriter(App.stack));
                    App.LOGGER.error("\r\n ERROR in R analysis: " + App.stack);
                    App.textArea.append("\r\n ERROR in R analysis: " + App.stack);
                    rcodeError = true;

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
                if (!rcodeError) {

                    runGSEAnalysis(analysisP);

                }//endif

            }

        };

        worker.execute();

    }

    public void runGSEAnalysis(String analysis) {
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
                    cmdList.add(System.getProperty("user.dir").concat("/bin/gene_enrichment.R")); //R Script Path
                    cmdList.add(output_Dir.getText());  //Working Directory 1
                    cmdList.add(System.getProperty("user.dir").concat("/R_Libraries"));//2
                    cmdList.add(jComboBox2.getSelectedItem().toString());//3
                    cmdList.add(jTextField8.getText());//4 organism
                    cmdList.add(jComboBox3.getSelectedItem().toString());
                    cmdList.add((jComboBox3.getSelectedIndex() + 1) + "");
                    cmdList.add(jComboBox4.getSelectedItem().toString());
                    cmdList.add(jComboBox5.getSelectedItem().toString());
                    cmdList.add(jTextField6.getText());
                    cmdList.add(jTextField7.getText());
                    cmdList.add(jComboBox7.getSelectedItem().toString());
                    cmdList.add(jComboBox6.getSelectedItem().toString());
                    cmdList.add(jTextField12.getText());
                    cmdList.add(jComboBox8.getSelectedItem().toString());
                    cmdList.add(jTextField11.getText());//15 eps

                    cmdList.add(jTextField9.getText());
                    cmdList.add(jComboBox9.getSelectedItem().toString());
                    cmdList.add(jTextField13.getText());

                    cmdList.add(jComboBox10.getSelectedItem().toString());
                    cmdList.add(jTextField14.getText());

                    cmdList.add(jTextField15.getText());
                    cmdList.add(jTextField16.getText());
                    cmdList.add(jTextField17.getText());//23
                    if (analysis.equals("deSeq2")) {
                        cmdList.add("deSeq2");//24
                    } else if (analysis.equals("edgeR")) {
                        cmdList.add("edgeR");
                        //     cmdList.add(jTextField18.getText());//25
                    } else if (analysis.equals("both")) {
                        cmdList.add("both");//24
                        //     cmdList.add(jTextField18.getText());//25
                    }

                    ProcessBuilder pb = new ProcessBuilder(cmdList);
                    pb.redirectErrorStream(true);
                    p = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;

                    while ((line = reader.readLine()) != null) {
                        App.LOGGER.info("\r\n" + line);
                        publish("\r\n" + line);

                        if ((line.contains("Execution halted")) || (line.contains("ERROR: Failed"))) {
                            rcodeError = true;
                            break;
                        }

                    }//while

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
                App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
                App.bar.setIndeterminate(false);

                if (!rcodeError) {
                    if (deseqRadio.isSelected()) {
                        runShiny(System.getProperty("user.dir").concat("/bin/shinyGraphs.R"), 
                                output_Dir.getText().concat("DeSeqResults") + "," + output_Dir.getText().concat("DeSeqResults/GeneEnrich_Results"), 
                                System.getProperty("user.dir").concat("/R_Libraries"), "DESEQ2,GSEA");
                    } else if (edgeRadio.isSelected()) {
                        runShiny(System.getProperty("user.dir").concat("/bin/shinyGraphs.R"), output_Dir.getText().concat("EdgeRResults") + "," + output_Dir.getText().concat("EdgeRResults/GeneEnrich_Results"), System.getProperty("user.dir").concat("/R_Libraries"), "EDGER,GSEA");
                    } else if (bothRadio.isSelected()) {

                        runShiny(System.getProperty("user.dir").concat("/bin/shinyGraphs.R"), output_Dir.getText().concat("DeSeqResults") + "," + output_Dir.getText().concat("EdgeRResults") + "," + output_Dir.getText().concat("DeSeqResults/GeneEnrich_Results") + "," + output_Dir.getText().concat("EdgeRResults/GeneEnrich_Results"), System.getProperty("user.dir").concat("/R_Libraries"), "DESEQ2,EDGER,GSEA_DESEQ2,GSEA_EDGER");

                    }
                }//error if

            }

        };

        worker.execute();

    }

    public void runShiny(String rFilePath, String graphsDir, String rLib, String mode) {
        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));

        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

            @Override
            protected Integer doInBackground() {
                Process p;
                try {
                    rcodeError = false;

                    List<String> cmdList = new ArrayList<String>();

                    cmdList.add("Rscript");
                    cmdList.add(rFilePath); //R Script Path
                    cmdList.add(graphsDir);  //Working Directory 1
                    cmdList.add(rLib);//2
                    cmdList.add(mode);//3  deSeq2,edgeR,GSEA

                    System.out.println("GraphDir is:" + graphsDir);

                    System.out.println("MODE is:" + mode);

                    ProcessBuilder pb = new ProcessBuilder(cmdList);
                    pb.redirectErrorStream(true);
                    p = pb.start();

                    Thread.sleep(3000); // Give Shiny time to start

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    boolean openedBrowser = false;

//                    while ((line = reader.readLine()) != null) {
//                        App.LOGGER.info("\r\n" + line);
//                        publish("\r\n" + line);
                      //  if (!openedBrowser && line.contains("Listening on")) {
                            
                  if (!openedBrowser ) {
                try {
                                // Shiny is ready — open the browser
                                Desktop.getDesktop().browse(new URI("http://127.0.0.1:8080"));
                                openedBrowser = true;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        // Optional: break out once Shiny is running
                        if (openedBrowser) {
                            App.LOGGER.info("Shiny app is running — exiting reader loop.");
                            //break; // stop reading output (let Shiny continue running in background)
                        }

             //       }//while

                     //////////////////////////////////////
//                    if (!isCancelled()) {
//                        status = p.waitFor();
//                    }
//
//                    p.destroy();

                } catch (IOException | InterruptedException ex) {

                    ex.printStackTrace(new PrintWriter(App.stack));
                    App.LOGGER.error("\r\n ERROR in R analysis: " + App.stack);
                    App.textArea.append("\r\n ERROR in R analysis: " + App.stack);
                    rcodeError = true;

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

            }

        };

        worker.execute();

    }

    /////////////////////////////////////////////////////////////////////
    // END TOOL METHODS ########################################################
    // Save table data to CSV file, overwriting if file already exists
    public void saveTableToTSV(String filePath) {
        try (FileWriter csvWriter = new FileWriter(filePath, false)) { // 'false' ensures overwrite mode
            // Write column headers
            for (int i = 0; i < sampleTableModel.getColumnCount(); i++) {
                if (i == sampleTableModel.getColumnCount() - 1) //Last Column
                {
                    csvWriter.append(sampleTableModel.getColumnName(i));
                } else {
                    csvWriter.append(sampleTableModel.getColumnName(i)).append("\t");
                }

            }
            csvWriter.append("\n");

            // Write rows
            for (int i = 0; i < sampleTableModel.getRowCount(); i++) {
                int count = sampleTableModel.getColumnCount() - 1;
                for (int j = 0; j < sampleTableModel.getColumnCount(); j++) {
                    Object cellValue = sampleTableModel.getValueAt(i, j);
                    String value = (cellValue != null ? cellValue.toString().trim() : "");
                    value = value.replaceAll("\\s+", ".");

                    if (j == count) {
                        csvWriter.append(value);
                    } else {
                        csvWriter.append(value).append("\t");
                    }
                }
                csvWriter.append("\n");
            }
            App.LOGGER.info("\r\n" + "TSV file saved successfully: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkFileExists(String directoryPath, String fileName) {
        Path filePath = Paths.get(directoryPath, fileName);
        return Files.exists(filePath);
    }

    public void display(String title, String value) {

        System.out.println("===========================================");
        System.out.println("=============== " + title.toUpperCase() + " ===================");
        System.out.println("===========================================");
        System.out.println(value);
    }

    /**
     * MEMBER FUNCTIONS
     */
    ////////////////////FUNCTION//////////
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

//READCOUNTS COMBO
    public void populateSTARCount(String path) {

//SETTING READ COUNTS
        readCombo = new Vector();
        readCombo.addElement("Select...");

        File folder = new File(path);

        File[] listOfFiles = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                //System.out.println("\nSTAR PATH NAME: " + pathname.getName());
                return pathname.getName().endsWith(".ReadsPerGene.out.tab");
            }
        });
//  for (File file : listOfFiles) {
//System.out.println("\nSTAR GeneCount: " + file.getName());
//  }

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                readCombo.addElement(listOfFiles[i].getName());
            }
        }
//  starReadCount.setModel(new javax.swing.DefaultComboBoxModel(readCombo));

//SETTING featureCounts
        readCombo = new Vector();
        readCombo.addElement("Select...");
        // folder = new File(path);
        listOfFiles = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                //  System.out.println("\nFEATURECOUNTS PATH NAME: " + pathname.getName());
                return pathname.getName().endsWith(".featureCounts");
            }
        });

//  for (File file : listOfFiles) {
//System.out.println("\nFeatureCounts: " + file.getName());
//  }
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                readCombo.addElement(listOfFiles[i].getName());
            }
        }
//  featureCounts.setModel(new javax.swing.DefaultComboBoxModel(readCombo));

//SETTING featureCountsSummary
        readCombo = new Vector();
        readCombo.addElement("Select...");
        folder = new File(path);
        listOfFiles = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
// System.out.println("\nSUMMARY PATH NAME: " + pathname.getName());
                return pathname.getName().endsWith(".featureCounts.summary");
            }
        });
//
//  for (File file : listOfFiles) {
//System.out.println("FeatureSummary: " + file.getName());
//  }

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                readCombo.addElement(listOfFiles[i].getName());
            }
        }
//  featureCountsSummary.setModel(new javax.swing.DefaultComboBoxModel(readCombo));

    }

    //*************************************************************************
    //COMINDED FEATURES
    //*************************************************************************
    public void combinedFeatures(String path) {
        s = "";
        File file;

        if (singleRadioButton.isSelected()) {

            for (int i = 0; i < sfileNames.size(); i++) {
                file = new File(path + sfileNames.get(i) + ".star.Aligned.sortedByCoord.out.bam");

                if (file.isFile()) {
                    s += file.getAbsolutePath() + " ";

                }
            }//for
        } else if (pairRadioButton.isSelected()) {
            for (int i = 0; i < p1fileNames.size(); i++) {
                file = new File(path + p1p2fileNames.get(i) + ".star.Aligned.sortedByCoord.out.bam");

                if (file.isFile()) {
                    s += file.getAbsolutePath() + " ";

                }

            }//for

        }
        s = s.trim(); //Space Separated List of Paths
        //  System.out.println("Files: " + s);
        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress: Generating featureCounts...", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));

        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

            @Override
            protected Integer doInBackground() {
                Process p;
                try {

                    pipelineError = false;

                    List<String> cmdList = new ArrayList<String>();

                    cmdList.add("sh");
                    cmdList.add(System.getProperty("user.dir").concat("/bin/feature.sh"));
//PATHS:

                    cmdList.add(App.SUBREAD_PATH);//1

                    if (singleRadioButton.isSelected()) {
                        cmdList.add("SINGLE"); //2
                    } else if (pairRadioButton.isSelected()) {
                        cmdList.add("PAIR");//2
                    }

                    cmdList.add(starThread.getText());//3T
                    cmdList.add("0");//4 s
                    cmdList.add(sjdbGTFfile.getText());//5 a
                    cmdList.add(output_Dir.getText());//6 O

                    cmdList.add("Combined");//7
//publish("Combined strings"+s);
                    cmdList.add(s);//8

                    //  publish("Combined Command: " + cmdList);
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

                    File readcountsFile = new File(output_Dir.getText() + "readcounts.RDS");
                    File dfFile = new File(output_Dir.getText() + "df.RDS");

                    if (readcountsFile.exists() && dfFile.exists()) {
                        App.textArea.append("Count Files (readcounts.RDS,df.RDS )  already exist. Skipping count script execution.");

                        if (deseqRadio.isSelected()) {
                            runDESeqMatrix(System.getProperty("user.dir").concat("/bin/DESeqMatrix.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"));
                        } else if (edgeRadio.isSelected()) {
                            runEdgeRMatrix(System.getProperty("user.dir").concat("/bin/edgeRMatrix.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"));
                        } else if (bothRadio.isSelected()) {

                            runDESeqMatrix(System.getProperty("user.dir").concat("/bin/DESeqMatrix.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"));

                        }

                    } else {

                        runRCOUNT(System.getProperty("user.dir").concat("/bin/counts.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"));//, "COMBINED"
                    }
                }
            }

        };

        worker.execute();

    }

    public void runRCOUNT(String rFilePath, String workDir, String rLib) {

        App.bar.setIndeterminate(true);
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress: Preparing Counts...", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));

        SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
            private int status;

            @Override
            protected Integer doInBackground() {
                Process p;
                try {
                    rcodeError = false;
                    List<String> cmdList = new ArrayList<String>();

                    cmdList.add("Rscript");
                    cmdList.add(rFilePath); //R Script Path
                    cmdList.add(workDir);  //Working Directory 1
                    cmdList.add(rLib); //2

                    ProcessBuilder pb = new ProcessBuilder(cmdList);
                    pb.redirectErrorStream(true);
                    p = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        App.LOGGER.info("\r\n" + line);
                        publish("\r\n" + line);
                        if (line.contains("Execution halted")) {
                            rcodeError = true;
                            break;
                        }

                    }
                    if (!isCancelled()) {
                        status = p.waitFor();
                    }

                    p.destroy();

                } catch (IOException | InterruptedException ex) {

                    rcodeError = true;

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
                App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));

                App.bar.setIndeterminate(false);
                if (!rcodeError) {

                    Map<String, List<String>> factorLevels;
                    try {
                        factorLevels = readFactorLevelsFromJson(output_Dir.getText() + "factor_levels.json");
                        List<String> contrasts = buildContrastsFromLevels(factorLevels);

                        deFactorListModel.clear();
                        for (String contrast : contrasts) {

                            deFactorListModel.addElement(contrast);
                        }

                    } catch (Exception ex) {
                        rcodeError = true;
                        ex.printStackTrace(new PrintWriter(App.stack));
                        App.LOGGER.error("\r\n ERROR in R analysis: " + App.stack);
                        App.textArea.append("\r\n ERROR in R analysis: " + App.stack);
                    }
                    if (!rcodeError) {
                        if (deseqRadio.isSelected()) {
                            runDESeqMatrix(System.getProperty("user.dir").concat("/bin/DESeqMatrix.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"));
                        } else if (edgeRadio.isSelected()) {
                            runEdgeRMatrix(System.getProperty("user.dir").concat("/bin/edgeRMatrix.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"));
                        } else if (bothRadio.isSelected()) {

                            runDESeqMatrix(System.getProperty("user.dir").concat("/bin/DESeqMatrix.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"));

                        }
                    }
                }//if
            }//DONE

        };

        worker.execute();

    }//rCount

    public Map<String, List<String>> readFactorLevelsFromJson(String jsonFilePath) throws Exception {
       
        System.out.println("JSON FILE PATH: " + jsonFilePath);
        // READ AND PRINT COMPLETE JSON FILE
    BufferedReader br = new BufferedReader(new FileReader(jsonFilePath));

    String line;

    System.out.println("===== JSON CONTENT START =====");

    while ((line = br.readLine()) != null) {

        System.out.println(line);

    }

    System.out.println("===== JSON CONTENT END =====");

    br.close();
    

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, List<String>>>() {
        }.getType();
        FileReader reader = new FileReader(jsonFilePath);
        Map<String, List<String>> factorLevels = gson.fromJson(reader, type);
        reader.close();
        return factorLevels;
    }

    public List<String> buildContrastsFromLevels(Map<String, List<String>> factorLevels) {
        List<String> contrastList = new ArrayList<>();
        contrastList.add("Intercept"); // Add intercept first

        for (Map.Entry<String, List<String>> entry : factorLevels.entrySet()) {
            String factor = entry.getKey();
            List<String> levels = entry.getValue();

            if (levels.size() > 1) {
                // DESeq2-style: levelB_vs_levelA → B - A
                String contrast = factor + "_" + levels.get(1) + "_vs_" + levels.get(0);
                contrastList.add(contrast);
            }
        }
        return contrastList;
    }

    public void resetPanel2() {

        listModelfasta.clear();
        listModelfasta1.clear();

        starThread.setText("1");
        readFilesCommand.setSelectedIndex(0);

        outSAMtype2.setSelectedIndex(0);
        twopassMode.setSelectedIndex(0);
        outFileNamePrefix.setText("Sample");
        genomeDir.setText(null);

        sjdbOverhang2.setText("100");
        listModelfasta1.clear();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        inputReadsBG = new javax.swing.ButtonGroup();
        featureCountsBG = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        jLabel63 = new javax.swing.JLabel();
        buttonGroup5 = new javax.swing.ButtonGroup();
        QCbuttonGroup = new javax.swing.ButtonGroup();
        expressionAnalysisBG = new javax.swing.ButtonGroup();
        sampleTableRadios = new javax.swing.ButtonGroup();
        jScrollPane2 = new javax.swing.JScrollPane();
        MainPanel = new javax.swing.JPanel();
        upPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgInput, 0, 0, null);
            }
        }

        ;
        nextBtn = new javax.swing.JButton();
        warningLabel = new javax.swing.JLabel();
        helpLabel = new javax.swing.JLabel();
        inputPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgInput, 0, 0, null);
            }
        }

        ;
        jLabel32 = new javax.swing.JLabel();
        output_Dir = new javax.swing.JTextField();
        singleRadioButton = new javax.swing.JRadioButton();
        pairRadioButton = new javax.swing.JRadioButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        readFilesInPair1 = new javax.swing.JList<>(listModelreadPair1);
        jScrollPane4 = new javax.swing.JScrollPane();
        readFilesInSingle = new javax.swing.JList<>(listModelreadSingle);
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        readFilesInPair2 = new javax.swing.JList<>(listModelreadPair2);
        jButton8 = new javax.swing.JButton();
        outputBtn = new javax.swing.JButton();
        jLabel98 = new javax.swing.JLabel();
        jLabel103 = new javax.swing.JLabel();
        jLabel104 = new javax.swing.JLabel();
        deseqRadio = new javax.swing.JRadioButton();
        edgeRadio = new javax.swing.JRadioButton();
        bothRadio = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel90 = new javax.swing.JLabel();
        fastpRadio = new javax.swing.JRadioButton();
        fastqcRadio = new javax.swing.JRadioButton();
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
        jScrollPane8 = new javax.swing.JScrollPane();
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
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
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
        jLabel97 = new javax.swing.JLabel();
        jLabel99 = new javax.swing.JLabel();
        jLabel100 = new javax.swing.JLabel();
        jLabel101 = new javax.swing.JLabel();
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
        thread = new javax.swing.JTextField();
        slidingC = new javax.swing.JCheckBox();
        jLabel102 = new javax.swing.JLabel();
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
        jScrollPane11 = new javax.swing.JScrollPane();
        fastpTxtArea = new javax.swing.JTextArea();
        jLabel50 = new javax.swing.JLabel();
        jLabel79 = new javax.swing.JLabel();
        jLabel80 = new javax.swing.JLabel();
        downPanel = new javax.swing.JPanel();
        back2 = new javax.swing.JButton();
        sampleDataPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgInput, 0, 0, null);
            }
        }

        ;
        addColumnButton = new javax.swing.JButton();
        deleteColumnButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        sampleTable = new javax.swing.JTable(sampleTableModel);
        jLabel2 = new javax.swing.JLabel();
        tableLockBtn = new javax.swing.JToggleButton();
        sampleRadio1 = new javax.swing.JRadioButton();
        sampleRadio2 = new javax.swing.JRadioButton();
        sampleFilePathTxt = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        starPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgInput, 0, 0, null);
            }
        }

        ;
        jLabel35 = new javax.swing.JLabel();
        starThread = new javax.swing.JTextField();
        jLabel52 = new javax.swing.JLabel();
        genomeDir = new javax.swing.JTextField();
        genomeDirBtn2 = new javax.swing.JButton();
        jLabel37 = new javax.swing.JLabel();
        runMode2 = new javax.swing.JTextField();
        jLabel44 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        sjdbOverhang2 = new javax.swing.JTextField();
        readFilesCommand = new javax.swing.JComboBox<>();
        jLabel47 = new javax.swing.JLabel();
        outFileNamePrefix = new javax.swing.JTextField();
        jLabel48 = new javax.swing.JLabel();
        outSAMtype2 = new javax.swing.JComboBox<>();
        jLabel49 = new javax.swing.JLabel();
        twopassMode = new javax.swing.JComboBox<>();
        jLabel55 = new javax.swing.JLabel();
        refGenomeBtn = new javax.swing.JButton();
        jScrollPane7 = new javax.swing.JScrollPane();
        refGenomeList = new javax.swing.JList<>(listModelfasta1);
        jTextField5 = new javax.swing.JTextField();
        jLabel59 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel60 = new javax.swing.JLabel();
        jLabel61 = new javax.swing.JLabel();
        quantMode = new javax.swing.JComboBox<>();
        jLabel62 = new javax.swing.JLabel();
        sjdbGTFfile = new javax.swing.JTextField();
        browseGTFBtn1 = new javax.swing.JButton();
        multipleFeaturePanel = new javax.swing.JPanel();
        eachRadio = new javax.swing.JRadioButton();
        combinedRadio = new javax.swing.JRadioButton();
        jLabel91 = new javax.swing.JLabel();
        outBAMsortingBinsN = new javax.swing.JTextField();
        jLabel92 = new javax.swing.JLabel();
        outSAMmapqUnique = new javax.swing.JTextField();
        runModeCombo = new javax.swing.JComboBox<>();
        jLabel42 = new javax.swing.JLabel();
        jLabel89 = new javax.swing.JLabel();
        outSAMunmapped = new javax.swing.JComboBox<>();
        analysisModePanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgInput, 0, 0, null);
            }
        }

        ;
        singleFactorRadio = new javax.swing.JRadioButton();
        multiFactorRadio = new javax.swing.JRadioButton();
        complexDesignPanel = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        interactionTermCombo = new javax.swing.JComboBox<>();
        interactionTermPanel = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        interactionList = new javax.swing.JList<>(interactionListModel);
        edgeRDesignPanel = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        interceptOption = new javax.swing.JComboBox<>();
        jLabel16 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jLabel81 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        countThresh1 = new javax.swing.JTextField();
        countThresh = new javax.swing.JTextField();
        filteringCombo = new javax.swing.JComboBox<>();
        jLabel83 = new javax.swing.JLabel();
        resDEPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgInput, 0, 0, null);
            }
        }

        ;
        jLabel10 = new javax.swing.JLabel();
        contrastFactorCombo = new javax.swing.JComboBox<>();
        combination1 = new javax.swing.JComboBox<>();
        jLabel17 = new javax.swing.JLabel();
        combination2 = new javax.swing.JComboBox<>();
        jLabel18 = new javax.swing.JLabel();
        reRunResults = new javax.swing.JButton();
        jLabel19 = new javax.swing.JLabel();
        jRadioContrast = new javax.swing.JRadioButton();
        mannualContrast = new javax.swing.JTextField();
        jRadioList = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane9 = new javax.swing.JScrollPane();
        deFactorList = new javax.swing.JList<>(deFactorListModel);
        jTextField2 = new javax.swing.JTextField();
        jTextField1 = new javax.swing.JTextField();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel84 = new javax.swing.JLabel();
        jLabel85 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jTextField10 = new javax.swing.JTextField();
        jLabel93 = new javax.swing.JLabel();
        jLabel95 = new javax.swing.JLabel();
        resEDPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgInput, 0, 0, null);
            }
        }

        ;
        jLabel82 = new javax.swing.JLabel();
        jLabel86 = new javax.swing.JLabel();
        contrastValue = new javax.swing.JTextField();
        jTextField21 = new javax.swing.JTextField();
        jLabel87 = new javax.swing.JLabel();
        edgeRTestCombo = new javax.swing.JComboBox<>();
        jLabel88 = new javax.swing.JLabel();
        jScrollPane10 = new javax.swing.JScrollPane();
        edgeRTestList = new javax.swing.JList<>(model1);
        jSeparator2 = new javax.swing.JSeparator();
        jButton25 = new javax.swing.JButton();
        testInfoLabel = new javax.swing.JLabel();
        selectInfoLabel = new javax.swing.JLabel();
        jTextField22 = new javax.swing.JTextField();
        jLabel94 = new javax.swing.JLabel();
        testInfoLabel1 = new javax.swing.JLabel();
        testInfoLabel2 = new javax.swing.JLabel();
        geneEnrichmentPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgInput, 0, 0, null);
            }
        }

        ;
        jLabel13 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jTextField7 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox<>();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jComboBox4 = new javax.swing.JComboBox<>();
        jLabel31 = new javax.swing.JLabel();
        jComboBox5 = new javax.swing.JComboBox<>();
        jLabel33 = new javax.swing.JLabel();
        jComboBox6 = new javax.swing.JComboBox<>();
        jLabel43 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jComboBox7 = new javax.swing.JComboBox<>();
        jLabel56 = new javax.swing.JLabel();
        jTextField12 = new javax.swing.JTextField();
        jLabel57 = new javax.swing.JLabel();
        jComboBox8 = new javax.swing.JComboBox<>();
        jButton11 = new javax.swing.JButton();
        jLabel39 = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        jTextField11 = new javax.swing.JTextField();
        jLabel64 = new javax.swing.JLabel();
        jComboBox9 = new javax.swing.JComboBox<>();
        jLabel65 = new javax.swing.JLabel();
        jLabel67 = new javax.swing.JLabel();
        jTextField13 = new javax.swing.JTextField();
        jComboBox10 = new javax.swing.JComboBox<>();
        jLabel66 = new javax.swing.JLabel();
        jLabel68 = new javax.swing.JLabel();
        jTextField14 = new javax.swing.JTextField();
        jLabel69 = new javax.swing.JLabel();
        jLabel70 = new javax.swing.JLabel();
        jLabel71 = new javax.swing.JLabel();
        jLabel72 = new javax.swing.JLabel();
        jTextField15 = new javax.swing.JTextField();
        jTextField16 = new javax.swing.JTextField();
        jTextField17 = new javax.swing.JTextField();
        jLabel73 = new javax.swing.JLabel();
        jLabel74 = new javax.swing.JLabel();
        jLabel75 = new javax.swing.JLabel();
        jLabel76 = new javax.swing.JLabel();

        jLabel63.setText("jLabel63");

        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("RNA SEQ DATA ANALYSIS PIPELINE");

        MainPanel.setBackground(new java.awt.Color(0, 0, 0));
        MainPanel.setPreferredSize(new java.awt.Dimension(1780, 5578));
        MainPanel.setLayout(new org.jdesktop.swingx.VerticalLayout());

        upPanel.setOpaque(false);

        nextBtn.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        nextBtn.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/next1.png"))); // NOI18N
        nextBtn.setText("Proceed to Quality Check");
        nextBtn.setBorderPainted(false);
        nextBtn.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        nextBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextBtnActionPerformed(evt);
            }
        });

        warningLabel.setFont(new java.awt.Font("Liberation Sans", 3, 12)); // NOI18N
        warningLabel.setForeground(new java.awt.Color(255, 0, 0));
        warningLabel.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                warningLabelPropertyChange(evt);
            }
        });

        helpLabel.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        helpLabel.setForeground(new java.awt.Color(255, 255, 255));
        helpLabel.setText("Help?");
        helpLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                helpLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                helpLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                helpLabelMouseExited(evt);
            }
        });

        javax.swing.GroupLayout upPanelLayout = new javax.swing.GroupLayout(upPanel);
        upPanel.setLayout(upPanelLayout);
        upPanelLayout.setHorizontalGroup(
            upPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, upPanelLayout.createSequentialGroup()
                .addGroup(upPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(upPanelLayout.createSequentialGroup()
                        .addGap(61, 61, 61)
                        .addComponent(warningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 1006, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(upPanelLayout.createSequentialGroup()
                        .addGap(354, 354, 354)
                        .addComponent(nextBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 464, Short.MAX_VALUE)
                .addComponent(helpLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(126, 126, 126))
        );
        upPanelLayout.setVerticalGroup(
            upPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(upPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(helpLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(upPanelLayout.createSequentialGroup()
                        .addComponent(warningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nextBtn)))
                .addContainerGap(9, Short.MAX_VALUE))
        );

        MainPanel.add(upPanel);

        inputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "INPUT OPTIONS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 14), new java.awt.Color(255, 255, 255))); // NOI18N
        inputPanel.setOpaque(false);

        jLabel32.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(255, 255, 255));
        jLabel32.setText("Set Output Directory:");

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
        jScrollPane4.setViewportView(readFilesInSingle);

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
        jScrollPane6.setViewportView(readFilesInPair2);

        jButton8.setText("...");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        outputBtn.setText("...");
        outputBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputBtnActionPerformed(evt);
            }
        });

        jLabel98.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel98.setForeground(new java.awt.Color(255, 255, 255));
        jLabel98.setText("Enter all the neccesary input files. NOTE: Path or Filename should not contain any spaces");

        jLabel103.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel103.setForeground(new java.awt.Color(255, 255, 255));
        jLabel103.setText("Choose Quality Control Method:");

        jLabel104.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel104.setForeground(new java.awt.Color(255, 255, 255));
        jLabel104.setText("Choose the Differential Expression analysis method:");

        deseqRadio.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        deseqRadio.setForeground(new java.awt.Color(255, 255, 255));
        deseqRadio.setSelected(true);
        deseqRadio.setText("DESeq2");
        deseqRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deseqRadioActionPerformed(evt);
            }
        });

        edgeRadio.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        edgeRadio.setForeground(new java.awt.Color(255, 255, 255));
        edgeRadio.setText("EdgeR");
        edgeRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                edgeRadioActionPerformed(evt);
            }
        });

        bothRadio.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        bothRadio.setForeground(new java.awt.Color(255, 255, 255));
        bothRadio.setText("Both");
        bothRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bothRadioActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("***EXAMPLE DATA");
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });

        jLabel90.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel90.setForeground(new java.awt.Color(255, 255, 255));
        jLabel90.setText("NOTE: User can upload input files in unzipped (*.fq , *.fa) or zipped (*.gz,*.bz2) format");

        fastpRadio.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        fastpRadio.setForeground(new java.awt.Color(255, 255, 255));
        fastpRadio.setText("FastP");
        fastpRadio.setToolTipText("Use this option for an all-in-one solution that performs quality control, trimming, adapter removal, and generates reports in HTML/JSON formats.");
        fastpRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fastpRadioActionPerformed(evt);
            }
        });

        fastqcRadio.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        fastqcRadio.setForeground(new java.awt.Color(255, 255, 255));
        fastqcRadio.setSelected(true);
        fastqcRadio.setText("FastQC");
        fastqcRadio.setToolTipText("Use FastQC to assess the quality of raw sequencing data without applying any trimming or filtering. \nOR\nUse FastQC in combination with a trimming tool (e.g., Trimmomatic) to perform quality control and clean up the data.");
        fastqcRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fastqcRadioActionPerformed(evt);
            }
        });

        noneRadio.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        noneRadio.setForeground(new java.awt.Color(255, 255, 255));
        noneRadio.setText("None");
        noneRadio.setToolTipText("Use this option for an all-in-one solution that performs quality control, trimming, adapter removal, and generates reports in HTML/JSON formats.");
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
                .addGap(40, 40, 40)
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(inputPanelLayout.createSequentialGroup()
                        .addComponent(singleRadioButton)
                        .addGap(18, 18, 18)
                        .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(inputPanelLayout.createSequentialGroup()
                                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton6))
                            .addGroup(inputPanelLayout.createSequentialGroup()
                                .addComponent(output_Dir, javax.swing.GroupLayout.PREFERRED_SIZE, 956, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(outputBtn))
                            .addComponent(jLabel90, javax.swing.GroupLayout.PREFERRED_SIZE, 624, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(529, Short.MAX_VALUE))
                    .addGroup(inputPanelLayout.createSequentialGroup()
                        .addComponent(pairRadioButton)
                        .addGap(18, 18, 18)
                        .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(inputPanelLayout.createSequentialGroup()
                                .addComponent(fastqcRadio)
                                .addGap(36, 36, 36)
                                .addComponent(fastpRadio)
                                .addGap(18, 18, 18)
                                .addComponent(noneRadio))
                            .addGroup(inputPanelLayout.createSequentialGroup()
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 449, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton7)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 458, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton8)))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(inputPanelLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel103)
                    .addComponent(jLabel32)
                    .addGroup(inputPanelLayout.createSequentialGroup()
                        .addComponent(jLabel104)
                        .addGap(50, 50, 50)
                        .addComponent(deseqRadio)
                        .addGap(18, 18, 18)
                        .addComponent(edgeRadio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bothRadio))
                    .addGroup(inputPanelLayout.createSequentialGroup()
                        .addComponent(jLabel98)
                        .addGap(27, 27, 27)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        inputPanelLayout.setVerticalGroup(
            inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel98)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel32)
                    .addComponent(output_Dir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(outputBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel90, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(singleRadioButton)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton6))
                .addGap(18, 18, 18)
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pairRadioButton)
                    .addComponent(jButton7)
                    .addComponent(jButton8)
                    .addComponent(jScrollPane5)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23)
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(fastqcRadio)
                        .addComponent(fastpRadio)
                        .addComponent(noneRadio))
                    .addComponent(jLabel103, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel104)
                    .addComponent(deseqRadio)
                    .addComponent(edgeRadio)
                    .addComponent(bothRadio))
                .addContainerGap(69, Short.MAX_VALUE))
        );

        MainPanel.add(inputPanel);

        QualityControlPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Quality Control", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255))); // NOI18N
        QualityControlPanel.setOpaque(false);

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
        jScrollPane8.setViewportView(fastqcTable);

        jLabel5.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("FastQC Results Summary:");

        jLabel4.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Html Results:");

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

        jLabel97.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel97.setForeground(new java.awt.Color(255, 255, 255));
        jLabel97.setText("HELP?");
        jLabel97.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel97MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel97MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel97MouseExited(evt);
            }
        });

        jLabel99.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        jLabel99.setForeground(new java.awt.Color(255, 255, 255));
        jLabel99.setText("Trimmomatic Parameters:");

        jLabel100.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel100.setForeground(new java.awt.Color(255, 255, 255));
        jLabel100.setText("phred Encoding:");

        jLabel101.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel101.setForeground(new java.awt.Color(255, 255, 255));
        jLabel101.setText("Adapter Fasta File:");

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

        thread.setEnabled(false);

        slidingC.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        slidingC.setForeground(new java.awt.Color(255, 255, 255));
        slidingC.setSelected(true);
        slidingC.setText("SLIDINGWINDOW:");
        slidingC.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                slidingCItemStateChanged(evt);
            }
        });

        jLabel102.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel102.setForeground(new java.awt.Color(255, 255, 255));
        jLabel102.setText("ILLUMNIACLIP:");

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
                    .addComponent(jLabel99)
                    .addGroup(trimPanelLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel100)
                            .addComponent(jLabel101)
                            .addComponent(jLabel102)
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
                                                .addComponent(thread))
                                            .addGroup(trimPanelLayout.createSequentialGroup()
                                                .addComponent(adapterfile, javax.swing.GroupLayout.PREFERRED_SIZE, 395, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(browseGTFBtn2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGap(18, 18, 18)
                                        .addComponent(logC)))
                                .addGap(89, 89, 89)
                                .addComponent(jLabel97, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(303, Short.MAX_VALUE))
        );
        trimPanelLayout.setVerticalGroup(
            trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(trimPanelLayout.createSequentialGroup()
                .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(trimPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel99)
                        .addGap(11, 11, 11)
                        .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel100)
                            .addComponent(phredEncodingCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(threadC)
                            .addComponent(thread, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(logC))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel101)
                            .addComponent(adapterfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(browseGTFBtn2)))
                    .addGroup(trimPanelLayout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jLabel97, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(trimPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel102)
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
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 1299, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(fastqcPanelLayout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fastqcResultCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel53, javax.swing.GroupLayout.PREFERRED_SIZE, 503, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(437, Short.MAX_VALUE))
        );
        fastqcPanelLayout.setVerticalGroup(
            fastqcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fastqcPanelLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel53, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(fastqcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fastqcResultCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 309, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addGroup(fastqcPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel54, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fastqcTrimCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(trimPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(61, Short.MAX_VALUE))
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
        jScrollPane11.setViewportView(fastpTxtArea);

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
                    .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 1000, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel50, javax.swing.GroupLayout.PREFERRED_SIZE, 691, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(fastPparameterLayout.createSequentialGroup()
                        .addComponent(jLabel80, javax.swing.GroupLayout.PREFERRED_SIZE, 349, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel79, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(48, Short.MAX_VALUE))
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
                .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addContainerGap(965, Short.MAX_VALUE))
            .addGroup(fastpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(fastpPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(fastPparameter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(673, Short.MAX_VALUE)))
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

        downPanel.setBackground(new java.awt.Color(0, 0, 0));
        downPanel.setOpaque(false);
        downPanel.setLayout(new org.jdesktop.swingx.VerticalLayout());

        back2.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        back2.setText("BACK");
        back2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                back2ActionPerformed(evt);
            }
        });
        downPanel.add(back2);

        sampleDataPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Prepare Sample Meta Data", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N
        sampleDataPanel.setOpaque(false);

        addColumnButton.setText("Add Column");
        addColumnButton.setEnabled(false);
        addColumnButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addColumnButtonActionPerformed(evt);
            }
        });

        deleteColumnButton.setText("Delete Column");
        deleteColumnButton.setEnabled(false);
        deleteColumnButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteColumnButtonActionPerformed(evt);
            }
        });

        sampleTable.setFont(new java.awt.Font("Linux Libertine Mono O", 0, 15)); // NOI18N
        sampleTable.setModel(sampleTableModel);
        sampleTable.setRowHeight(25);
        sampleTable.setRowMargin(1);
        sampleTable.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                sampleTableFocusLost(evt);
            }
        });
        jScrollPane3.setViewportView(sampleTable);

        jLabel2.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Done with editing? ");

        tableLockBtn.setText("Lock Table");
        tableLockBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tableLockBtnActionPerformed(evt);
            }
        });

        sampleRadio1.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        sampleRadio1.setForeground(new java.awt.Color(255, 255, 255));
        sampleRadio1.setSelected(true);
        sampleRadio1.setText("Upload a Sample data CSV/TSV File:");
        sampleRadio1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sampleRadio1ActionPerformed(evt);
            }
        });

        sampleRadio2.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        sampleRadio2.setForeground(new java.awt.Color(255, 255, 255));
        sampleRadio2.setText("Manual Data Entry:");
        sampleRadio2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sampleRadio2ActionPerformed(evt);
            }
        });

        sampleFilePathTxt.setEditable(false);

        jButton1.setText("...");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Please choose one of the following options to provide your sample meta data:");

        jLabel7.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Ensure the file has 'SampleName' as the first column (SampleName, condition1, condition2). ");

        jLabel8.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Enter data directly into the table. You can add or delete columns as needed to customize your input.");

        jLabel22.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(255, 255, 255));
        jLabel22.setText("Press the Lock Table button to proceed with further analysis.");

        javax.swing.GroupLayout sampleDataPanelLayout = new javax.swing.GroupLayout(sampleDataPanel);
        sampleDataPanel.setLayout(sampleDataPanelLayout);
        sampleDataPanelLayout.setHorizontalGroup(
            sampleDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sampleDataPanelLayout.createSequentialGroup()
                .addGroup(sampleDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sampleDataPanelLayout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addGroup(sampleDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(sampleDataPanelLayout.createSequentialGroup()
                                .addGroup(sampleDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(sampleRadio1)
                                    .addGroup(sampleDataPanelLayout.createSequentialGroup()
                                        .addComponent(addColumnButton, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(deleteColumnButton)))
                                .addGap(308, 308, 308)
                                .addGroup(sampleDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(sampleDataPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(tableLockBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(sampleRadio2)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addGroup(sampleDataPanelLayout.createSequentialGroup()
                                .addComponent(sampleFilePathTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 854, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1))
                            .addComponent(jLabel8)))
                    .addGroup(sampleDataPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 1167, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(186, Short.MAX_VALUE))
        );
        sampleDataPanelLayout.setVerticalGroup(
            sampleDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sampleDataPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sampleRadio1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addGroup(sampleDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sampleFilePathTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addGap(18, 18, 18)
                .addComponent(sampleRadio2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(sampleDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addColumnButton, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteColumnButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(tableLockBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel22)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        downPanel.add(sampleDataPanel);

        starPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Provide following information to perform two pass mapping", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N
        starPanel.setOpaque(false);

        jLabel35.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(255, 255, 255));
        jLabel35.setText("No. of Threads:");

        starThread.setText("1");

        jLabel52.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel52.setForeground(new java.awt.Color(255, 255, 255));
        jLabel52.setText("Genome Directory:");

        genomeDir.setEditable(false);

        genomeDirBtn2.setText("...");
        genomeDirBtn2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genomeDirBtn2ActionPerformed(evt);
            }
        });

        jLabel37.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel37.setForeground(new java.awt.Color(255, 255, 255));
        jLabel37.setText("Run Mode:");

        runMode2.setEditable(false);
        runMode2.setText("alignReads");

        jLabel44.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel44.setForeground(new java.awt.Color(255, 255, 255));
        jLabel44.setText("readFilesCommand:");

        jLabel46.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel46.setForeground(new java.awt.Color(255, 255, 255));
        jLabel46.setText("sjdbOverhang:");

        sjdbOverhang2.setText("100");

        readFilesCommand.setEditable(true);
        readFilesCommand.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "gunzip -c", "zcat", "bunzip2 -c", "-" }));

        jLabel47.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(255, 255, 255));
        jLabel47.setText("outFileNamePrefix:");

        outFileNamePrefix.setText("Combined_Sample");

        jLabel48.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel48.setForeground(new java.awt.Color(255, 255, 255));
        jLabel48.setText("outSAMtype:");

        outSAMtype2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "BAM SortedByCoordinate", "BAM Unsorted", "BAM Unsorted SortedByCoordinate", "SAM SortedByCoordinate", "SAM Unsorted", "SAM Unsorted SortedByCoordinate", "None" }));

        jLabel49.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel49.setForeground(new java.awt.Color(255, 255, 255));
        jLabel49.setText("twopassMode:");

        twopassMode.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Basic", "None" }));

        jLabel55.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel55.setForeground(new java.awt.Color(255, 255, 255));
        jLabel55.setText("Reference Genome:");

        refGenomeBtn.setText("...");
        refGenomeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refGenomeBtnActionPerformed(evt);
            }
        });

        refGenomeList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        refGenomeList.setVisibleRowCount(10);
        refGenomeList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                refGenomeListKeyPressed(evt);
            }
        });
        jScrollPane7.setViewportView(refGenomeList);

        jTextField5.setText("1000000");
        jTextField5.setToolTipText("number of threads to run STAR default: 1");

        jLabel59.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel59.setForeground(new java.awt.Color(255, 255, 255));
        jLabel59.setText("limitOutSJcollapsed:");

        jTextField4.setText("0");
        jTextField4.setToolTipText("number of threads to run STAR default: 1");

        jLabel60.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel60.setForeground(new java.awt.Color(255, 255, 255));
        jLabel60.setText("limitBAMsortRAM:");

        jLabel61.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel61.setForeground(new java.awt.Color(255, 255, 255));
        jLabel61.setText("quantMode:");

        quantMode.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "GeneCounts", "TranscriptomeSAM", "TranscriptomeSAM GeneCounts", "-" }));

        jLabel62.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel62.setForeground(new java.awt.Color(255, 255, 255));
        jLabel62.setText("sjdbGTFfile:");

        sjdbGTFfile.setEditable(false);

        browseGTFBtn1.setText("...");
        browseGTFBtn1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseGTFBtn1ActionPerformed(evt);
            }
        });

        multipleFeaturePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "featureCounts Setting for Multiple Samples", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N
        multipleFeaturePanel.setOpaque(false);

        eachRadio.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        eachRadio.setForeground(new java.awt.Color(255, 255, 255));
        eachRadio.setText("Generate seperate featureCounts file(s) for each sample");

        combinedRadio.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        combinedRadio.setForeground(new java.awt.Color(255, 255, 255));
        combinedRadio.setSelected(true);
        combinedRadio.setText("Generate a combined featureCounts file for all samples");

        javax.swing.GroupLayout multipleFeaturePanelLayout = new javax.swing.GroupLayout(multipleFeaturePanel);
        multipleFeaturePanel.setLayout(multipleFeaturePanelLayout);
        multipleFeaturePanelLayout.setHorizontalGroup(
            multipleFeaturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(multipleFeaturePanelLayout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addGroup(multipleFeaturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(combinedRadio)
                    .addComponent(eachRadio))
                .addContainerGap(35, Short.MAX_VALUE))
        );
        multipleFeaturePanelLayout.setVerticalGroup(
            multipleFeaturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(multipleFeaturePanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(eachRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(combinedRadio)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jLabel91.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel91.setForeground(new java.awt.Color(255, 255, 255));
        jLabel91.setText("outBAMsortingBinsN:");

        outBAMsortingBinsN.setText("50");
        outBAMsortingBinsN.setToolTipText("number of threads to run STAR default: 1");

        jLabel92.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel92.setForeground(new java.awt.Color(255, 255, 255));
        jLabel92.setText("outSAMmapqUnique:");

        outSAMmapqUnique.setText("255");
        outSAMmapqUnique.setToolTipText("number of threads to run STAR default: 1");

        runModeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Single Sample Per Run (Generate seperate bam file for each Sample) (Recommended)", "Multiple Reads as Single Sample in Single Run (Generate one bam file for all reads)" }));
        runModeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runModeComboActionPerformed(evt);
            }
        });

        jLabel42.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel42.setForeground(new java.awt.Color(255, 255, 255));
        jLabel42.setText("Select Sample Mapping:");

        jLabel89.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel89.setForeground(new java.awt.Color(255, 255, 255));
        jLabel89.setText("outSAMunmapped:");

        outSAMunmapped.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "None", "Within", "Separate" }));

        javax.swing.GroupLayout starPanelLayout = new javax.swing.GroupLayout(starPanel);
        starPanel.setLayout(starPanelLayout);
        starPanelLayout.setHorizontalGroup(
            starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(starPanelLayout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel37)
                    .addComponent(jLabel35)
                    .addComponent(jLabel44)
                    .addComponent(jLabel49)
                    .addComponent(jLabel47)
                    .addComponent(jLabel42)
                    .addComponent(jLabel46)
                    .addComponent(jLabel48)
                    .addComponent(jLabel60)
                    .addComponent(jLabel59)
                    .addComponent(jLabel61)
                    .addComponent(jLabel91)
                    .addComponent(jLabel92)
                    .addComponent(jLabel89))
                .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(starPanelLayout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(twopassMode, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(outFileNamePrefix, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(outBAMsortingBinsN, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(outSAMmapqUnique, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(sjdbOverhang2, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(outSAMtype2, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(quantMode, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(outSAMunmapped, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(starPanelLayout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(runModeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 619, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(starPanelLayout.createSequentialGroup()
                                .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(starThread, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(runMode2, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(readFilesCommand, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(55, 55, 55)
                                .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel52)
                                    .addComponent(jLabel55)
                                    .addComponent(jLabel62))
                                .addGap(18, 18, 18)
                                .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(multipleFeaturePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(starPanelLayout.createSequentialGroup()
                                        .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(jScrollPane7)
                                            .addComponent(genomeDir)
                                            .addComponent(sjdbGTFfile, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 496, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(refGenomeBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(genomeDirBtn2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(browseGTFBtn1))))))))
                .addGap(0, 153, Short.MAX_VALUE))
        );
        starPanelLayout.setVerticalGroup(
            starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(starPanelLayout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel42)
                    .addComponent(runModeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(68, 68, 68)
                .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(starPanelLayout.createSequentialGroup()
                        .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel35)
                            .addComponent(starThread, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel37)
                            .addComponent(runMode2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel44)
                            .addComponent(readFilesCommand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(9, 9, 9)
                        .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel49)
                            .addComponent(twopassMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel47)
                            .addComponent(outFileNamePrefix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel46)
                            .addComponent(sjdbOverhang2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(9, 9, 9)
                        .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(outSAMtype2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel48))
                        .addGap(9, 9, 9)
                        .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(outSAMunmapped, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel89))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel60)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(starPanelLayout.createSequentialGroup()
                        .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel52)
                            .addComponent(genomeDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(genomeDirBtn2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(starPanelLayout.createSequentialGroup()
                                .addComponent(refGenomeBtn)
                                .addGap(152, 152, 152)
                                .addComponent(browseGTFBtn1))
                            .addGroup(starPanelLayout.createSequentialGroup()
                                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(sjdbGTFfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel62)))
                            .addComponent(jLabel55))))
                .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(starPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel59))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel61)
                            .addComponent(quantMode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(outBAMsortingBinsN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel91)))
                    .addGroup(starPanelLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(multipleFeaturePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(starPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(outSAMmapqUnique, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel92))
                .addContainerGap(37, Short.MAX_VALUE))
        );

        downPanel.add(starPanel);

        analysisModePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Analysis Mode:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255))); // NOI18N
        analysisModePanel.setOpaque(false);

        singleFactorRadio.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        singleFactorRadio.setForeground(new java.awt.Color(255, 255, 255));
        singleFactorRadio.setSelected(true);
        singleFactorRadio.setText("Single Factor(Simple)");
        singleFactorRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                singleFactorRadioActionPerformed(evt);
            }
        });

        multiFactorRadio.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        multiFactorRadio.setForeground(new java.awt.Color(255, 255, 255));
        multiFactorRadio.setText("Multiple Factors(Complex)");
        multiFactorRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                multiFactorRadioActionPerformed(evt);
            }
        });

        complexDesignPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Complex Design", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255))); // NOI18N
        complexDesignPanel.setOpaque(false);

        jLabel21.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(255, 255, 255));
        jLabel21.setText("Create Design  formula with interaction term?");

        interactionTermCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NO", "YES" }));
        interactionTermCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                interactionTermComboItemStateChanged(evt);
            }
        });

        interactionTermPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Select Interaction Term(s)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255))); // NOI18N
        interactionTermPanel.setOpaque(false);

        jLabel25.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(255, 255, 255));
        jLabel25.setText("Select Interaction terms to be used:");

        jScrollPane1.setViewportView(interactionList);

        javax.swing.GroupLayout interactionTermPanelLayout = new javax.swing.GroupLayout(interactionTermPanel);
        interactionTermPanel.setLayout(interactionTermPanelLayout);
        interactionTermPanelLayout.setHorizontalGroup(
            interactionTermPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(interactionTermPanelLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(interactionTermPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(interactionTermPanelLayout.createSequentialGroup()
                        .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE))
                .addContainerGap())
        );
        interactionTermPanelLayout.setVerticalGroup(
            interactionTermPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(interactionTermPanelLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabel25)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout complexDesignPanelLayout = new javax.swing.GroupLayout(complexDesignPanel);
        complexDesignPanel.setLayout(complexDesignPanelLayout);
        complexDesignPanelLayout.setHorizontalGroup(
            complexDesignPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(complexDesignPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(complexDesignPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(complexDesignPanelLayout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addGap(43, 43, 43)
                        .addComponent(interactionTermCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(interactionTermPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        complexDesignPanelLayout.setVerticalGroup(
            complexDesignPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, complexDesignPanelLayout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(complexDesignPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(interactionTermCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(interactionTermPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        edgeRDesignPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "edgeR Design", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255))); // NOI18N
        edgeRDesignPanel.setOpaque(false);

        jLabel27.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(255, 255, 255));
        jLabel27.setText("Create Design Formula:");

        interceptOption.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "No Intercept (with 0+)", "With Intercept (without 0+)" }));

        javax.swing.GroupLayout edgeRDesignPanelLayout = new javax.swing.GroupLayout(edgeRDesignPanel);
        edgeRDesignPanel.setLayout(edgeRDesignPanelLayout);
        edgeRDesignPanelLayout.setHorizontalGroup(
            edgeRDesignPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(edgeRDesignPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel27)
                .addGap(33, 33, 33)
                .addComponent(interceptOption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        edgeRDesignPanelLayout.setVerticalGroup(
            edgeRDesignPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, edgeRDesignPanelLayout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(edgeRDesignPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(interceptOption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(41, Short.MAX_VALUE))
        );

        jLabel16.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("Please select the appropriate analysis type:");

        jLabel34.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(255, 255, 255));
        jLabel34.setText("For DESeq2, this refers to sample metadata containing one condition column");

        jLabel36.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel36.setForeground(new java.awt.Color(255, 255, 255));
        jLabel36.setText("(excluding the SampleName column).");

        jLabel38.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel38.setForeground(new java.awt.Color(255, 255, 255));
        jLabel38.setText("For edgeR, this refers to a pairwise comparison between two groups.");

        jLabel40.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(255, 255, 255));
        jLabel40.setText("For DESeq2, this applies to sample metadata with more than one condition column.");

        jLabel41.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel41.setForeground(new java.awt.Color(255, 255, 255));
        jLabel41.setText("For edgeR, this involves comparisons with more than two groups or factors.");

        jLabel81.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel81.setForeground(new java.awt.Color(255, 255, 255));
        jLabel81.setText("Percentage Threshold (Y):");

        jLabel11.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Counts Threshold (X):");

        countThresh1.setText("0.9");

        countThresh.setText("10");

        filteringCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "None", "Total Count ≥ X", "Present in ≥ Y% Samples", "Both" }));
        filteringCombo.setSelectedIndex(3);
        filteringCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                filteringComboItemStateChanged(evt);
            }
        });

        jLabel83.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel83.setForeground(new java.awt.Color(255, 255, 255));
        jLabel83.setText("Choose a filtering method:");

        javax.swing.GroupLayout analysisModePanelLayout = new javax.swing.GroupLayout(analysisModePanel);
        analysisModePanel.setLayout(analysisModePanelLayout);
        analysisModePanelLayout.setHorizontalGroup(
            analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(analysisModePanelLayout.createSequentialGroup()
                .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(analysisModePanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jLabel16))
                    .addGroup(analysisModePanelLayout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(multiFactorRadio)
                            .addComponent(singleFactorRadio)
                            .addGroup(analysisModePanelLayout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel36)
                                    .addComponent(jLabel34)
                                    .addComponent(jLabel38)
                                    .addComponent(jLabel40)
                                    .addComponent(jLabel41)
                                    .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, analysisModePanelLayout.createSequentialGroup()
                                            .addComponent(jLabel83)
                                            .addGap(18, 18, 18)
                                            .addComponent(filteringCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGroup(analysisModePanelLayout.createSequentialGroup()
                                            .addComponent(jLabel11)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(countThresh, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(analysisModePanelLayout.createSequentialGroup()
                                            .addComponent(jLabel81)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(countThresh1, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                        .addGap(59, 59, 59)
                        .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(complexDesignPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(edgeRDesignPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(186, Short.MAX_VALUE))
        );
        analysisModePanelLayout.setVerticalGroup(
            analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(analysisModePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(analysisModePanelLayout.createSequentialGroup()
                        .addComponent(singleFactorRadio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel34)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel36)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel38)
                        .addGap(33, 33, 33)
                        .addComponent(multiFactorRadio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel40)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel41)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(filteringCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel83))
                        .addGap(30, 30, 30)
                        .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(countThresh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel81)
                            .addComponent(countThresh1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(complexDesignPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(edgeRDesignPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        downPanel.add(analysisModePanel);

        resDEPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "DESeq2 Analysis Parameters:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255))); // NOI18N
        resDEPanel.setOpaque(false);

        jLabel10.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("log2FoldChange greater than:");

        contrastFactorCombo.setEnabled(false);
        contrastFactorCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contrastFactorComboActionPerformed(evt);
            }
        });

        combination1.setEnabled(false);

        jLabel17.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setText("Select Factor:");

        combination2.setEnabled(false);

        jLabel18.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(255, 255, 255));
        jLabel18.setText("Select Factor Level:");

        reRunResults.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        reRunResults.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/next1.png"))); // NOI18N
        reRunResults.setText("Rerun DeSeq Results");
        reRunResults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reRunResultsActionPerformed(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(255, 255, 255));
        jLabel19.setText("VS");

        jRadioContrast.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jRadioContrast.setForeground(new java.awt.Color(255, 255, 255));
        jRadioContrast.setText("By Contrast");
        jRadioContrast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioContrastActionPerformed(evt);
            }
        });

        mannualContrast.setToolTipText("Write your desired contrast and press Enter");
        mannualContrast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mannualContrastActionPerformed(evt);
            }
        });

        jRadioList.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jRadioList.setForeground(new java.awt.Color(255, 255, 255));
        jRadioList.setSelected(true);
        jRadioList.setText("By List");
        jRadioList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioListActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Predefined DESeq2 Contrast Combinations:");

        jLabel28.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(255, 255, 255));
        jLabel28.setText("Select from resultNames list:");

        jLabel9.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("padj  less than:");

        jScrollPane9.setViewportView(deFactorList);

        jTextField2.setText("0.05");

        jTextField1.setText("2");

        jLabel84.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        jLabel84.setForeground(new java.awt.Color(255, 255, 255));
        jLabel84.setText("Search By:");

        jLabel85.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel85.setForeground(new java.awt.Color(255, 255, 255));
        jLabel85.setText("AND");

        jLabel24.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setText("In case of any error in padj and logfoldchain you can rerun the analysis after changing values:");

        jButton2.setText("View Graphs");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jTextField10.setText("0.1");

        jLabel93.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel93.setForeground(new java.awt.Color(255, 255, 255));
        jLabel93.setText("alpha p value cutoff:");

        jLabel95.setFont(new java.awt.Font("Liberation Sans", 3, 15)); // NOI18N
        jLabel95.setForeground(new java.awt.Color(255, 255, 255));
        jLabel95.setText("If your desired contrast is not listed below, you can add it manually");

        javax.swing.GroupLayout resDEPanelLayout = new javax.swing.GroupLayout(resDEPanel);
        resDEPanel.setLayout(resDEPanelLayout);
        resDEPanelLayout.setHorizontalGroup(
            resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resDEPanelLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSeparator4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 1060, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(resDEPanelLayout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(resDEPanelLayout.createSequentialGroup()
                                        .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(resDEPanelLayout.createSequentialGroup()
                                                .addGap(67, 67, 67)
                                                .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addComponent(jLabel18)
                                                    .addComponent(jLabel17)))
                                            .addComponent(jLabel28)
                                            .addGroup(resDEPanelLayout.createSequentialGroup()
                                                .addGap(51, 51, 51)
                                                .addComponent(jRadioList)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(resDEPanelLayout.createSequentialGroup()
                                                .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(contrastFactorCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(combination1, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(18, 18, 18)
                                                .addComponent(jLabel19)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(combination2, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 624, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel3)
                                            .addGroup(resDEPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel95)
                                                .addGap(18, 18, 18)
                                                .addComponent(mannualContrast, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(resDEPanelLayout.createSequentialGroup()
                                        .addGap(74, 74, 74)
                                        .addComponent(jLabel24)
                                        .addGap(18, 18, 18)
                                        .addComponent(reRunResults))
                                    .addGroup(resDEPanelLayout.createSequentialGroup()
                                        .addGap(50, 50, 50)
                                        .addComponent(jRadioContrast))
                                    .addComponent(jLabel84)
                                    .addGroup(resDEPanelLayout.createSequentialGroup()
                                        .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(jLabel9)
                                            .addComponent(jLabel93))
                                        .addGap(47, 47, 47)
                                        .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(jTextField2, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jTextField10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(33, 33, 33)
                                        .addComponent(jLabel85)
                                        .addGap(43, 43, 43)
                                        .addComponent(jLabel10)
                                        .addGap(47, 47, 47)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(14, 14, 14))))
                    .addGroup(resDEPanelLayout.createSequentialGroup()
                        .addGap(651, 651, 651)
                        .addComponent(jButton2)))
                .addContainerGap(118, Short.MAX_VALUE))
        );
        resDEPanelLayout.setVerticalGroup(
            resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resDEPanelLayout.createSequentialGroup()
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel84)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioContrast)
                .addGap(8, 8, 8)
                .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(contrastFactorCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(combination1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19)
                    .addComponent(combination2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(resDEPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel95)
                            .addComponent(mannualContrast, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(resDEPanelLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jRadioList)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel28)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel93)
                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19)
                .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel10)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel85))
                    .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel9)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(29, 29, 29)
                .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(reRunResults))
                .addGap(50, 50, 50))
        );

        downPanel.add(resDEPanel);

        resEDPanel.setBackground(new java.awt.Color(0, 0, 0));
        resEDPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "edgeR Results", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255))); // NOI18N
        resEDPanel.setOpaque(false);

        jLabel82.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel82.setForeground(new java.awt.Color(255, 255, 255));
        jLabel82.setText("Select test:");

        jLabel86.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel86.setForeground(new java.awt.Color(255, 255, 255));
        jLabel86.setText("Contrast:");

        contrastValue.setText("0,-1,1");
        contrastValue.setToolTipText("Enter comma seprated values");

        jTextField21.setText("2");

        jLabel87.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel87.setForeground(new java.awt.Color(255, 255, 255));
        jLabel87.setText("log2FoldChange Threshold:");

        edgeRTestCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select", "exactTest", "glmQLFit+glmQLFTest(coef)", "glmQLFit+glmQLFTest(contrast)", "glmFit+glmLRT(coef)", "glmFit+glmLRT(contrast)", "glmTreat" }));
        edgeRTestCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                edgeRTestComboItemStateChanged(evt);
            }
        });

        jLabel88.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel88.setForeground(new java.awt.Color(255, 255, 255));
        jLabel88.setText("Select 1 or 2 coefficent(s): ");

        edgeRTestList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                edgeRTestListValueChanged(evt);
            }
        });
        jScrollPane10.setViewportView(edgeRTestList);

        jButton25.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButton25.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/bill2.png"))); // NOI18N
        jButton25.setText("Genrate Results");
        jButton25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton25ActionPerformed(evt);
            }
        });

        testInfoLabel.setFont(new java.awt.Font("Liberation Sans", 2, 15)); // NOI18N
        testInfoLabel.setForeground(new java.awt.Color(255, 255, 255));
        testInfoLabel.setText(".");

        selectInfoLabel.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        selectInfoLabel.setForeground(new java.awt.Color(255, 255, 255));
        selectInfoLabel.setText("Select any coefficents");

        jTextField22.setText("0.05");

        jLabel94.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel94.setForeground(new java.awt.Color(255, 255, 255));
        jLabel94.setText("PValue Threshold:");

        testInfoLabel1.setFont(new java.awt.Font("Liberation Sans", 2, 15)); // NOI18N
        testInfoLabel1.setForeground(new java.awt.Color(255, 255, 255));

        testInfoLabel2.setFont(new java.awt.Font("Liberation Sans", 2, 15)); // NOI18N
        testInfoLabel2.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout resEDPanelLayout = new javax.swing.GroupLayout(resEDPanel);
        resEDPanel.setLayout(resEDPanelLayout);
        resEDPanelLayout.setHorizontalGroup(
            resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(testInfoLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 939, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(resEDPanelLayout.createSequentialGroup()
                .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(resEDPanelLayout.createSequentialGroup()
                        .addGap(236, 236, 236)
                        .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(testInfoLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 947, Short.MAX_VALUE)
                            .addComponent(testInfoLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(resEDPanelLayout.createSequentialGroup()
                        .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(resEDPanelLayout.createSequentialGroup()
                                .addGap(115, 115, 115)
                                .addComponent(jLabel82))
                            .addGroup(resEDPanelLayout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addComponent(jLabel88)))
                        .addGap(33, 33, 33)
                        .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane10)
                            .addComponent(edgeRTestCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(selectInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 662, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(resEDPanelLayout.createSequentialGroup()
                                .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel94)
                                    .addComponent(jLabel87)
                                    .addComponent(jLabel86))
                                .addGap(41, 41, 41)
                                .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton25)
                                    .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(contrastValue, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
                                        .addComponent(jTextField22)
                                        .addComponent(jTextField21)))))))
                .addContainerGap(361, Short.MAX_VALUE))
            .addGroup(resEDPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator2))
        );
        resEDPanelLayout.setVerticalGroup(
            resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resEDPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel82)
                    .addComponent(edgeRTestCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(testInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(testInfoLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(testInfoLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(resEDPanelLayout.createSequentialGroup()
                        .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, resEDPanelLayout.createSequentialGroup()
                                .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(resEDPanelLayout.createSequentialGroup()
                                        .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel86)
                                            .addComponent(contrastValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel94))
                                    .addGroup(resEDPanelLayout.createSequentialGroup()
                                        .addGap(36, 36, 36)
                                        .addComponent(jTextField22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(12, 12, 12)
                                .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jTextField21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel87))
                                .addGap(18, 18, 18)
                                .addComponent(jButton25))
                            .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel88))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        downPanel.add(resEDPanel);

        geneEnrichmentPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Gene Enrichment Analysis", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255))); // NOI18N
        geneEnrichmentPanel.setOpaque(false);

        jLabel13.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("pvalueCutoff:");

        jTextField6.setText("0.05");

        jTextField7.setText("0.2");

        jLabel14.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setText("qvalueCutoff:");

        jLabel15.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setText("Annotation DB Organism:");

        jTextField8.setText("org.Hs.eg.db");

        jLabel23.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(255, 255, 255));
        jLabel23.setText("KEGG Organism:");

        jTextField9.setText("hsa");

        jLabel26.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(255, 255, 255));
        jLabel26.setText("From Type:");

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ENSEMBL", "SYMBOL", "ENTREZID", "UNIPROT" }));

        jLabel12.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("GO Classification (groupGO) Parameters:");

        jLabel20.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(255, 255, 255));
        jLabel20.setText("Sub ontologies:");

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "MF", "BP", "CC" }));

        jLabel29.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(255, 255, 255));
        jLabel29.setText("GO Overrepresentation (enrichGO) Parameters:");

        jLabel30.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(255, 255, 255));
        jLabel30.setText("Sub ontologies:");

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ALL", "MF", "BP", "CC" }));

        jLabel31.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(255, 255, 255));
        jLabel31.setText("pAdjustMethod:");

        jComboBox5.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "holm", "hochberg", "hommel", "bonferroni", "BH", "BY", "fdr", "none" }));
        jComboBox5.setSelectedIndex(4);

        jLabel33.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel33.setForeground(new java.awt.Color(255, 255, 255));
        jLabel33.setText("pAdjustMethod:");

        jComboBox6.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "holm", "hochberg", "hommel", "bonferroni", "BH", "BY", "fdr", "none" }));
        jComboBox6.setSelectedIndex(7);

        jLabel43.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel43.setForeground(new java.awt.Color(255, 255, 255));
        jLabel43.setText("Gene Set Enrichment Analysis GO (gseGO) Parameters:");

        jLabel45.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel45.setForeground(new java.awt.Color(255, 255, 255));
        jLabel45.setText("Sub ontologies:");

        jComboBox7.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ALL", "MF", "BP", "CC" }));

        jLabel56.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel56.setForeground(new java.awt.Color(255, 255, 255));
        jLabel56.setText("pvalueCutoff:");

        jTextField12.setText("0.05");

        jLabel57.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel57.setForeground(new java.awt.Color(255, 255, 255));
        jLabel57.setText("by:");

        jComboBox8.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "fgsea", "DOSE" }));

        jButton11.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButton11.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/graph4.png"))); // NOI18N
        jButton11.setText("RUN GSEA");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jLabel39.setFont(new java.awt.Font("Liberation Sans", 3, 15)); // NOI18N
        jLabel39.setForeground(new java.awt.Color(255, 255, 255));
        jLabel39.setText("Supported Organisms");
        jLabel39.setToolTipText("https://www.genome.jp/kegg/catalog/org_list.html");
        jLabel39.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel39MouseClicked(evt);
            }
        });

        jLabel58.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel58.setForeground(new java.awt.Color(255, 255, 255));
        jLabel58.setText("eps:");

        jTextField11.setText("1e-10");

        jLabel64.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel64.setForeground(new java.awt.Color(255, 255, 255));
        jLabel64.setText("pAdjustMethod:");

        jComboBox9.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "holm", "hochberg", "hommel", "bonferroni", "BH", "BY", "fdr", "none" }));
        jComboBox9.setSelectedIndex(4);

        jLabel65.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel65.setForeground(new java.awt.Color(255, 255, 255));
        jLabel65.setText("KEGG pathway over-representation analysis:");

        jLabel67.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel67.setForeground(new java.awt.Color(255, 255, 255));
        jLabel67.setText("pvalueCutoff:");

        jTextField13.setText("0.05");

        jComboBox10.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "holm", "hochberg", "hommel", "bonferroni", "BH", "BY", "fdr", "none" }));
        jComboBox10.setSelectedIndex(4);

        jLabel66.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel66.setForeground(new java.awt.Color(255, 255, 255));
        jLabel66.setText("KEGG pathway gene set enrichment analysis");

        jLabel68.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel68.setForeground(new java.awt.Color(255, 255, 255));
        jLabel68.setText("pvalueCutoff:");

        jTextField14.setText("0.05");

        jLabel69.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel69.setForeground(new java.awt.Color(255, 255, 255));
        jLabel69.setText("pAdjustMethod:");

        jLabel70.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel70.setForeground(new java.awt.Color(255, 255, 255));
        jLabel70.setText("KEGG module over-representation analysis:");

        jLabel71.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel71.setForeground(new java.awt.Color(255, 255, 255));
        jLabel71.setText("pValueCutoff:");

        jLabel72.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel72.setForeground(new java.awt.Color(255, 255, 255));
        jLabel72.setText("qValueCutoff:");

        jTextField15.setText("1");

        jTextField16.setText("1");

        jTextField17.setText("1");

        jLabel73.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel73.setForeground(new java.awt.Color(255, 255, 255));
        jLabel73.setText("KEGG module over-representation analysis:");

        jLabel74.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel74.setForeground(new java.awt.Color(255, 255, 255));
        jLabel74.setText("pValueCutoff:");

        jLabel75.setFont(new java.awt.Font("Liberation Sans", 2, 15)); // NOI18N
        jLabel75.setForeground(new java.awt.Color(255, 255, 255));
        jLabel75.setText("e.g  org.Mm.eg.db,  org.Hs.eg.db");

        jLabel76.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel76.setForeground(new java.awt.Color(255, 255, 255));
        jLabel76.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/updateProduct1.png"))); // NOI18N
        jLabel76.setText("Restore Defaults...");
        jLabel76.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel76MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout geneEnrichmentPanelLayout = new javax.swing.GroupLayout(geneEnrichmentPanel);
        geneEnrichmentPanel.setLayout(geneEnrichmentPanelLayout);
        geneEnrichmentPanelLayout.setHorizontalGroup(
            geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                        .addGap(55, 55, 55)
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                                .addComponent(jLabel23)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                                .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel15)
                                    .addComponent(jLabel26))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jTextField8)
                                    .addComponent(jComboBox2, 0, 145, Short.MAX_VALUE)))
                            .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                                .addComponent(jLabel20)
                                .addGap(18, 18, 18)
                                .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                                .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, geneEnrichmentPanelLayout.createSequentialGroup()
                                            .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(jLabel13)
                                                .addComponent(jLabel14))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                                        .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                                            .addComponent(jLabel31)
                                            .addGap(12, 12, 12)))
                                    .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel30)
                                        .addGap(12, 12, 12)))
                                .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jComboBox4, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jComboBox5, 0, 145, Short.MAX_VALUE)
                                    .addComponent(jTextField7)
                                    .addComponent(jTextField6)))))
                    .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12)
                            .addComponent(jLabel29)
                            .addComponent(jLabel65)
                            .addComponent(jLabel66)
                            .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                                .addGap(48, 48, 48)
                                .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel64)
                                    .addComponent(jLabel67)
                                    .addComponent(jLabel68)
                                    .addComponent(jLabel69))
                                .addGap(12, 12, 12)
                                .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jComboBox9, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jComboBox10, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jTextField14, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                                .addGap(128, 128, 128)
                                .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel70)
                                    .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                                            .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, geneEnrichmentPanelLayout.createSequentialGroup()
                                                        .addComponent(jLabel56)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                                                    .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                                                        .addComponent(jLabel33)
                                                        .addGap(12, 12, 12)))
                                                .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                                                    .addComponent(jLabel45)
                                                    .addGap(12, 12, 12)))
                                            .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(jComboBox6, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jComboBox7, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                                            .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(jLabel58)
                                                .addComponent(jLabel57))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(jComboBox8, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addComponent(jLabel43)
                                        .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                                            .addComponent(jLabel71)
                                            .addGap(18, 18, 18)
                                            .addComponent(jTextField15, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                                            .addComponent(jLabel72)
                                            .addGap(18, 18, 18)
                                            .addComponent(jTextField16, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                                            .addComponent(jLabel74)
                                            .addGap(18, 18, 18)
                                            .addComponent(jTextField17, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(jLabel73)))
                            .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel75)
                                    .addComponent(jLabel39))
                                .addGap(65, 65, 65)
                                .addComponent(jLabel76, javax.swing.GroupLayout.DEFAULT_SIZE, 772, Short.MAX_VALUE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                        .addGap(344, 344, 344)
                        .addComponent(jButton11)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        geneEnrichmentPanelLayout.setVerticalGroup(
            geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                        .addGap(17, 17, 17)
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel26)
                            .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton11)))
                .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel15)
                            .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel75))
                        .addGap(15, 15, 15)
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel23)
                            .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel39))
                        .addGap(35, 35, 35)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel20)
                            .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jLabel76)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                        .addComponent(jLabel29)
                        .addGap(18, 18, 18)
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel30)
                            .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel31)
                            .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14)
                            .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                        .addComponent(jLabel43)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel45)
                            .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel33)
                            .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel56)
                            .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel57)
                            .addComponent(jComboBox8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel58)
                            .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(12, 12, 12)
                .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel65)
                    .addComponent(jLabel70))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel64)
                            .addComponent(jComboBox9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel67)
                            .addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel71))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel72))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel66)
                    .addComponent(jLabel73))
                .addGap(18, 18, 18)
                .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(geneEnrichmentPanelLayout.createSequentialGroup()
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel69)
                            .addComponent(jComboBox10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel68)
                            .addComponent(jTextField14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(geneEnrichmentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextField17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel74)))
                .addGap(33, 33, 33))
        );

        downPanel.add(geneEnrichmentPanel);

        MainPanel.add(downPanel);

        jScrollPane2.setViewportView(MainPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1610, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1502, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void helpLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpLabelMouseClicked
        App.helpWebpage("https://gatk.broadinstitute.org/hc/en-us/articles/360035531192-RNAseq-short-variant-discovery-SNPs-Indels-");
    }//GEN-LAST:event_helpLabelMouseClicked

    private void helpLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpLabelMouseEntered
        helpLabel.setIcon(App.icons[1]);
    }//GEN-LAST:event_helpLabelMouseEntered

    private void helpLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpLabelMouseExited
        helpLabel.setIcon(App.icons[0]);
    }//GEN-LAST:event_helpLabelMouseExited

    private void outputBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputBtnActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new java.io.File("."));
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            String[] e = fileChooser.getSelectedFile().list();
            if (e.length > 0)//not empty
            {

                int x = JOptionPane.showConfirmDialog(null, "Selected folder is not empty. Either "
                        + "select an empty folder or press OK to continue to overwrite files?", "WARNING", JOptionPane.OK_CANCEL_OPTION);
                if (x == JOptionPane.OK_OPTION) {
                    String path = fileChooser.getSelectedFile().toString();

                    output_Dir.setText(path + "/");
                }

            } else {

                String path = fileChooser.getSelectedFile().toString();

                output_Dir.setText(path + "/");
            }
        }
    }//GEN-LAST:event_outputBtnActionPerformed

    private void singleRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_singleRadioButtonActionPerformed
        read = "SINGLE";
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
        read = "PAIR";
        //Single read OFF
        jButton6.setEnabled(false);
        listModelreadSingle.clear();
        //fileNameWithoutExtension.clear();
        //Pair read ON
        //listModelreadPair1.clear();listModelreadPair2.clear();
        jButton7.setEnabled(true);
        jButton8.setEnabled(true);
    }//GEN-LAST:event_pairRadioButtonActionPerformed

    private void readFilesInSingleKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_readFilesInSingleKeyPressed
        if (evt.getKeyChar() == KeyEvent.VK_DELETE) {
            listModelreadSingle.remove(readFilesInSingle.getSelectedIndex());

        }
    }//GEN-LAST:event_readFilesInSingleKeyPressed

    private void readFilesInPair1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_readFilesInPair1KeyPressed
        if (evt.getKeyChar() == KeyEvent.VK_DELETE) {
            listModelreadPair1.remove(readFilesInPair1.getSelectedIndex());

        }
    }//GEN-LAST:event_readFilesInPair1KeyPressed

    private void readFilesInPair2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_readFilesInPair2KeyPressed
        if (evt.getKeyChar() == KeyEvent.VK_DELETE) {
            listModelreadPair2.remove(readFilesInPair2.getSelectedIndex());

        }
    }//GEN-LAST:event_readFilesInPair2KeyPressed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setAcceptAllFileFilterUsed(false);
        // Add custom file filters
        fileChooser.addChoosableFileFilter(createFileFilter(
                "BZIP2 Fastq Files (*.fq.bz2, *.fastq.bz2)", new String[]{".fq.bz2", ".fastq.bz2"}));
        fileChooser.addChoosableFileFilter(createFileFilter(
                "GZIP Fastq Files (*.fq.gz, *.fastq.gz)", new String[]{".fq.gz", ".fastq.gz"}));
        fileChooser.addChoosableFileFilter(createFileFilter(
                "Fastq Files (*.fq, *.fastq)", new String[]{".fq", ".fastq"}));
        fileChooser.addChoosableFileFilter(createFileFilter(
                "Fasta Files (*.fa, *.fasta)", new String[]{".fa", ".fasta"}));

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

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setAcceptAllFileFilterUsed(false);

        // Add custom file filters
        fileChooser.addChoosableFileFilter(createFileFilter(
                "BZIP2 Fastq Files (*.fq.bz2, *.fastq.bz2)", new String[]{".fq.bz2", ".fastq.bz2"}));
        fileChooser.addChoosableFileFilter(createFileFilter(
                "GZIP Fastq Files (*.fq.gz, *.fastq.gz)", new String[]{".fq.gz", ".fastq.gz"}));
        fileChooser.addChoosableFileFilter(createFileFilter(
                "Fastq Files (*.fq, *.fastq)", new String[]{".fq", ".fastq"}));
        fileChooser.addChoosableFileFilter(createFileFilter(
                "Fasta Files (*.fa, *.fasta)", new String[]{".fa", ".fasta"}));

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

// Method to create a FileFilter for "_1.extension" files
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

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setAcceptAllFileFilterUsed(true);
        // Add custom file filters
        fileChooser.addChoosableFileFilter(createFileFilter(
                "BZIP2 Fastq Files (*.fq.bz2, *.fastq.bz2)", new String[]{"fq.bz2", "fastq.bz2"}));
        fileChooser.addChoosableFileFilter(createFileFilter(
                "GZIP Fastq Files (*.fq.gz, *.fastq.gz)", new String[]{"fq.gz", "fastq.gz"}));
        fileChooser.addChoosableFileFilter(createFileFilter(
                "Fastq Files (*.fq, *.fastq)", new String[]{"fq", "fastq"}));
        fileChooser.addChoosableFileFilter(createFileFilter(
                "Fasta Files (*.fa, *.fasta)", new String[]{"fa", "fasta"}));

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

    private void runModeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runModeComboActionPerformed
        if (runModeCombo.getSelectedIndex() == 0) {
            mode = "SINGLE";
            outFileNamePrefix.setEnabled(false);
            multipleFeaturePanel.setVisible(true);
        } else if (runModeCombo.getSelectedIndex() == 1) {
            mode = "MULTIPLE";
            outFileNamePrefix.setEnabled(true);
            multipleFeaturePanel.setVisible(false);
        }

    }//GEN-LAST:event_runModeComboActionPerformed

    private boolean isValidGzip(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath); GZIPInputStream gis = new GZIPInputStream(fis)) {
            byte[] buffer = new byte[2]; // Just read a few bytes to check validity
            gis.read(buffer);
            return true; // No error = valid gzip
        } catch (IOException e) {
            return false; // Exception = not a valid gzip file
        }
    }

    private boolean isValidBzip2(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath); BufferedInputStream bis = new BufferedInputStream(fis); BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bis)) {
            byte[] buffer = new byte[2];
            bzIn.read(buffer); // Read a few bytes to validate
            return true;
        } catch (IOException e) {
            return false;
        }
    }

// Function to decompress Gzip file
    public void decompressGzip(String gzFile) throws IOException {
        String outputFile = gzFile.replace(".gz", "");
        try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(gzFile)); FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
        App.textArea.append("Decompressed: " + outputFile);
    }

    // Function to decompress Bzip2 file
    public void decompressBzip2(String bz2File) throws IOException {
        String outputFile = bz2File.replace(".bz2", "");
        try (FileInputStream fis = new FileInputStream(bz2File); BufferedInputStream bis = new BufferedInputStream(fis); BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(bis); FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bzIn.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
        App.textArea.append("Decompressed: " + outputFile);

    }

    private boolean checkInputFormat() {
        if (singleRadioButton.isSelected()) {
            for (int i = 0; i < readFilesInSingle.getModel().getSize(); i++) {
                String filename = readFilesInSingle.getModel().getElementAt(i).replaceAll("\"", "").trim();

                if (filename.endsWith(".gz")) {
                    if (!isValidGzip(filename)) {
                        return false;
                    }
                } else if (filename.endsWith(".bz2") && !fastpRadio.isSelected()) {

                    if (!isValidBzip2(filename)) {
                        return false;
                    }
                }

            }//endFor

        } else if (pairRadioButton.isSelected()) {

            //PAIR 1
            for (int i = 0; i < readFilesInPair1.getModel().getSize(); i++) {

                String filename = readFilesInPair1.getModel().getElementAt(i).replaceAll("\"", "").trim();

                if (filename.endsWith(".gz")) {
                    if (!isValidGzip(filename)) {
                        return false;
                    }
                } else if (filename.endsWith(".bz2")) {

                    if (!isValidBzip2(filename) && !fastpRadio.isSelected()) {
                        return false;
                    }
                }

            }//for

            //PAIR 2
            for (int i = 0; i < readFilesInPair2.getModel().getSize(); i++) {
                String filename = readFilesInPair2.getModel().getElementAt(i).replaceAll("\"", "").trim();

                if (filename.endsWith(".gz")) {
                    if (!isValidGzip(filename)) {
                        return false;
                    }
                } else if (filename.endsWith(".bz2")) {

                    if (!isValidBzip2(filename) && !fastpRadio.isSelected()) {
                        return false;
                    }
                }

            }//for

        }//endif
        return true;
    }

    private boolean checkInput() {
        if (output_Dir.getText().isBlank()) {
            warningLabel.setText("**ERROR: The output directory field is empty. Please provide a valid directory path where the output files will be stored.");
            return false;
        } else if (singleRadioButton.isSelected()) {

            if (listModelreadSingle.isEmpty()) {
                warningLabel.setText("**ERROR: Input read(s)/Sample(s) list is empty. Please upload or provide at least one read file to proceed.");
                return false;
            }
//            else if (!checkInputFormat()) { //check format
//
//                // Show error message with options
//                int choice = JOptionPane.showOptionDialog(
//                        null,
//                        "ERROR: The input file(s) are NOT a valid GZIP or BZIP2 file!\n"
//                        + "Do you want to unzip it?",
//                        "Invalid File Format",
//                        JOptionPane.YES_NO_OPTION,
//                        JOptionPane.ERROR_MESSAGE,
//                        null,
//                        new Object[]{"Unzip", "Cancel"},
//                        "Unzip"
//                );
//                // If user chooses "Unzip", attempt decompression
//                if (choice == JOptionPane.YES_OPTION) {
//                    try {
//                        warningLabel.setText("");
//                        for (int i = 0; i < readFilesInSingle.getModel().getSize(); i++) {
//                            String filename = readFilesInSingle.getModel().getElementAt(i).replaceAll("\"", "").trim();
//                            if (filename.endsWith(".gz")) {
//                                decompressGzip(filename);
//                            } else if (filename.endsWith(".bz2")) {
//                                decompressBzip2(filename);
//                            }
//                        }//endFor
//                    } catch (IOException e) {
//                        JOptionPane.showMessageDialog(null, "Error decompressing file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//                        return false;
//                    }
//                } else {
//                    warningLabel.setText("NOT valid GZIP or BZIP2 compressed file(s)");
//                    return false;
//                }
//                
//            }//end if-else
            if (fastpRadio.isSelected()) {
                if (containsBz2Files(listModelreadSingle)) {
                    warningLabel.setText("**ERROR: *.bz2 compressed file format is not supported by Fastp. Please upload files in supported formats such as .fastq, .fq, .fastq.gz, or .fq.gz.");
                    return false;

                }
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
//            if (!checkInputFormat()) { //check format
//
//                // Show error message with options
//                int choice = JOptionPane.showOptionDialog(
//                        null,
//                        "ERROR: The input file(s) are NOT a valid GZIP or BZIP2 file!\n"
//                        + "Do you want to unzip it?",
//                        "Invalid File Format",
//                        JOptionPane.YES_NO_OPTION,
//                        JOptionPane.ERROR_MESSAGE,
//                        null,
//                        new Object[]{"Unzip", "Cancel"},
//                        "Unzip"
//                );
//                // If user chooses "Unzip", attempt decompression
//                if (choice == JOptionPane.YES_OPTION) {
//                    try {
//                        warningLabel.setText("");
//                        //PAIR 1
//                        for (int i = 0; i < readFilesInPair1.getModel().getSize(); i++) {
//                            String filename = readFilesInPair1.getModel().getElementAt(i).replaceAll("\"", "").trim();
//                            if (filename.endsWith(".gz")) {
//                                decompressGzip(filename);
//                            } else if (filename.endsWith(".bz2")) {
//                                decompressBzip2(filename);
//                            }
//                        }//for
//
//                        //PAIR 2
//                        for (int i = 0; i < readFilesInPair2.getModel().getSize(); i++) {
//                            String filename = readFilesInPair2.getModel().getElementAt(i).replaceAll("\"", "").trim();
//                            if (filename.endsWith(".gz")) {
//                                decompressGzip(filename);
//                            } else if (filename.endsWith(".bz2")) {
//                                decompressBzip2(filename);
//                            }
//                        }//for
//
//                    } catch (IOException e) {
//                        JOptionPane.showMessageDialog(null, "Error decompressing file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//                        return false;
//                    }
//                } else {
//                    warningLabel.setText("NOT valid GZIP or BZIP2 compressed file(s)");
//                    return false;
//                }
//                
//            }//end if-else

            if (fastpRadio.isSelected()) {
                if (containsBz2Files(listModelreadPair1) || containsBz2Files(listModelreadPair2)) {
                    warningLabel.setText("**ERROR: *.bz2 compressed file format is not supported by Fastp. Please upload files in supported formats such as .fastq, .fq, .fastq.gz, or .fq.gz.");
                    return false;

                }
            }

        }

        return true;//No error
    }

    public boolean containsBz2Files(ListModel<String> listModel) {
        // Iterate through the ListModel
        for (int i = 0; i < listModel.getSize(); i++) {
            String path = listModel.getElementAt(i);
            if (path.toLowerCase().endsWith(".bz2")) {
                return true; // Return true if a .bz2 file is found
            }
        }
        return false; // Return false if no .bz2 files are found
    }
    private void nextBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextBtnActionPerformed

        warningLabel.setText(null);

        //If fastqc+trim --> read input, runfastqc and display results then Runpipline
        //If fastqc-trim -->read input and run Pipeline
        //If fastp --> read input, show results and run pipeline
        if (nextBtn.getText().equalsIgnoreCase("next")) {
            if (checkInput()) {

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
                selectedKey = "DefaultInput";

                inputPanel.setVisible(false);
                downPanel.setVisible(true);
                nextBtn.setText("Run Pipeline");
            }

        } else if (nextBtn.getText().equalsIgnoreCase("Proceed to Quality Check")) {
            if (checkInput()) {

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

                if (singleRadioButton.isSelected()) {
                    inputFilesDataS(singlePathMap, "FastP" + fastCounter);

                } else if (pairRadioButton.isSelected()) {

                    inputFilesDataP(pairPathMap, "FastP" + fastCounter);

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

            QualityControlPanel.setVisible(false);
            downPanel.setVisible(true);
            nextBtn.setText("Run Pipeline");
        } else if (nextBtn.getText().equalsIgnoreCase("Run Pipeline")) {

            if (checkDown()) {
                System.out.println("Selected Key in RunPipeline: " + selectedKey);

                runSTAR();

            } else {
                jScrollPane2.getViewport().setViewPosition(new Point(0, 0));

            }
        }//endIf run pipeline

    }//GEN-LAST:event_nextBtnActionPerformed

    private void singleFactorRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_singleFactorRadioActionPerformed
        complexDesignPanel.setVisible(false);

    }//GEN-LAST:event_singleFactorRadioActionPerformed

    private void multiFactorRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multiFactorRadioActionPerformed
        complexDesignPanel.setVisible(true);

    }//GEN-LAST:event_multiFactorRadioActionPerformed

    private void reRunResultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reRunResultsActionPerformed

        rcodeError = false;

        if (analysisP.equalsIgnoreCase("deSeq2") || analysisP.equalsIgnoreCase("both")) {

            if (jRadioContrast.isSelected()) {
                if (combination1.getSelectedIndex() == combination2.getSelectedIndex()) {
                    warningLabel.setText("**ERROR: Both factor levels should be different");
                    rcodeError = true;
                }

            } else if (jRadioList.isSelected()) {

                if (!((deFactorList.getSelectedIndices().length > 0) && (deFactorList.getSelectedIndices().length <= 2))) {
                    warningLabel.setText("Select at least one or maximum two terms from the list");
                    rcodeError = true;
                }
            }

            if (!rcodeError) {
                runDESeqResult(System.getProperty("user.dir").concat("/bin/DESeqResults.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"));

            }
        }//end if

    }//GEN-LAST:event_reRunResultsActionPerformed

    private void interactionTermComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_interactionTermComboItemStateChanged
        //  interactionList.clearSelection();
        if (interactionTermCombo.getSelectedIndex() == 0) {
            interactionTermPanel.setVisible(false);

        } else if (interactionTermCombo.getSelectedIndex() == 1) {
            interactionTermPanel.setVisible(true);
        }
    }//GEN-LAST:event_interactionTermComboItemStateChanged

    private void jRadioContrastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioContrastActionPerformed
        warningLabel.setText("");

        contrastFactorCombo.setEnabled(true);
        combination1.setEnabled(true);
        combination2.setEnabled(true);

        deFactorList.setEnabled(false);
        reRunResults.setEnabled(true);

    }//GEN-LAST:event_jRadioContrastActionPerformed

    private void jRadioListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioListActionPerformed

        deFactorList.setEnabled(true);
        warningLabel.setText("");
        contrastFactorCombo.setEnabled(false);
        combination1.setEnabled(false);
        combination2.setEnabled(false);
        reRunResults.setEnabled(true);

    }//GEN-LAST:event_jRadioListActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed

        runGSEAnalysis(analysisP);

    }//GEN-LAST:event_jButton11ActionPerformed

    private void jLabel39MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel39MouseClicked
        App.helpWebpage("https://www.genome.jp/kegg/catalog/org_list.html");
    }//GEN-LAST:event_jLabel39MouseClicked

    private void jLabel76MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel76MouseClicked
        jComboBox2.setSelectedIndex(0);
        jTextField8.setText("org.Hs.eg.db");
        jTextField8.setText("hsa");

        jComboBox3.setSelectedIndex(0);
        jComboBox4.setSelectedIndex(0);
        jComboBox5.setSelectedIndex(4);
        jTextField6.setText("0.05");
        jTextField7.setText("0.2");

        jComboBox7.setSelectedIndex(0);
        jComboBox6.setSelectedIndex(7);
        jTextField12.setText("0.05");
        jComboBox8.setSelectedIndex(0);
        jTextField11.setText("1e-10");

        jComboBox9.setSelectedIndex(7);
        jTextField13.setText("0.05");

        jComboBox10.setSelectedIndex(7);
        jTextField14.setText("0.05");

        jTextField15.setText("1");
        jTextField16.setText("1");

        jTextField17.setText("1");

    }//GEN-LAST:event_jLabel76MouseClicked

    private void warningLabelPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_warningLabelPropertyChange
        jScrollPane2.getViewport().setViewPosition(new Point(0, 0));
    }//GEN-LAST:event_warningLabelPropertyChange

    private void jButton25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton25ActionPerformed
        rcodeError = false;

        if (edgeRTestCombo.getSelectedIndex() == 0) {
            warningLabel.setText("Select any test type.");
            rcodeError = true;
        } else if (edgeRTestCombo.getSelectedIndex() == 1) {
            if (edgeRTestList.getSelectedIndices().length != 2) {
                warningLabel.setText("Must select two coefficents");
                rcodeError = true;
            }

        } else if ((edgeRTestCombo.getSelectedIndex() == 2) || (edgeRTestCombo.getSelectedIndex() == 4) || (edgeRTestCombo.getSelectedIndex() == 5)) {
            if (edgeRTestList.getSelectedIndices().length < 1) {
                warningLabel.setText("Select any coefficent(s)");
                rcodeError = true;
            }

        } else if (edgeRTestCombo.getSelectedIndex() == 3) {
            // Regular expression for a comma-separated list of number
            String pattern = "^[-]?\\\\d+(\\\\.\\\\d+)?(,[-]?\\\\d+(\\\\.\\\\d+)?)*$";
            if (contrastValue.getText().isEmpty()) {
                warningLabel.setText("**ERROR: Fill all fields");
                rcodeError = true;
            } else if (!(contrastValue.getText().trim().matches(pattern))) {
                warningLabel.setText("**ERROR: Contrast pattern isn't correct  ");
                rcodeError = true;
            }

        }
        if (!rcodeError) {
            runEdgeRResult(System.getProperty("user.dir").concat("/bin/edgeRResults.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"));//, "COMBINED"

        }

//        runEdgeResults(System.getProperty("user.dir").concat("/bin/edgeRResults.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"));
    }//GEN-LAST:event_jButton25ActionPerformed

    private void edgeRTestComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_edgeRTestComboItemStateChanged

        edgeRTestList.setEnabled(true);
        contrastValue.setEnabled(false);

        if (edgeRTestCombo.getSelectedIndex() == 0) {
            testInfoLabel.setText("");
            testInfoLabel1.setText("");
            testInfoLabel2.setText("");
            selectInfoLabel.setText("Select any test type");

            edgeRTestList.setEnabled(false);
        } else if (edgeRTestCombo.getSelectedIndex() == 1) {//exactTest
            selectInfoLabel.setText("Must select two coefficents & set PValue, LogFC");

            testInfoLabel.setText("Simple Two-Group Comparison");
            testInfoLabel1.setText("");
            testInfoLabel2.setText("");
        } else if (edgeRTestCombo.getSelectedIndex() == 2) {//glmQLFTest (coef)

            selectInfoLabel.setText("Select either one or two coefficents.");

            testInfoLabel.setText("Quasi-Likelihood F-Test for Complex Design with coefficents");
            testInfoLabel1.setText("Tests whether 1+ coefficients are significantly different from 0.");
            testInfoLabel2.setText("Use this only if your design includes an intercept (e.g., ~group). Not recommended for groups comparison");

        } else if (edgeRTestCombo.getSelectedIndex() == 3) {//glmQLFTest (contrast)

            selectInfoLabel.setText("Set contrast vector using +1 (include), -1 (subtract), 0 (ignore) for each group column.");
            testInfoLabel.setText("Quasi-Likelihood F-Test for Complex Design with contrast");
            testInfoLabel1.setText("");
            testInfoLabel2.setText("");
            contrastValue.setEnabled(true);

        } else if (edgeRTestCombo.getSelectedIndex() == 4) {//glmLRT(coef)

            testInfoLabel.setText("Likelihood Ratio Test for Complex Design with coefficents");
            testInfoLabel1.setText("");
            testInfoLabel2.setText("");
            selectInfoLabel.setText("Select either one or two coefficents");

            testInfoLabel1.setText("Tests whether 1+ model coefficients are significantly different from 0");

        } else if (edgeRTestCombo.getSelectedIndex() == 5) {//glmLRT(contrast)
            testInfoLabel.setText("Likelihood Ratio Test for Complex Design with contrast");
            testInfoLabel1.setText("");
            testInfoLabel2.setText("");
            selectInfoLabel.setText("Set contrast vector using +1 (include), -1 (subtract), 0 (ignore) for each group column.");
            contrastValue.setEnabled(true);

        } else if (edgeRTestCombo.getSelectedIndex() == 6) {//glmTreat
            testInfoLabel.setText("Minimum Fold-Change Testing");
            testInfoLabel1.setText("");
            testInfoLabel2.setText("");
            selectInfoLabel.setText("Must select one coefficent & set LogFC");
        }


    }//GEN-LAST:event_edgeRTestComboItemStateChanged

    private void browseGTFBtn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseGTFBtn1ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("GTF Files (*.gtf)", "gtf"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {

                sjdbGTFfile.setText(fileChooser.getSelectedFile().toString());

            }
        }
    }//GEN-LAST:event_browseGTFBtn1ActionPerformed

    private void refGenomeListKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_refGenomeListKeyPressed
        if (evt.getKeyChar() == KeyEvent.VK_DELETE) {

            listModelfasta1.remove(refGenomeList.getSelectedIndex());
        }
    }//GEN-LAST:event_refGenomeListKeyPressed

    private void refGenomeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refGenomeBtnActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Fasta Files (*.fa)", "fa"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setAcceptAllFileFilterUsed(true);

        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {

            if (fileChooser.getSelectedFiles() != null) {
                allFiles = fileChooser.getSelectedFiles();//File[] allfiles

                for (int i = 0; i < allFiles.length; i++) {

                    listModelfasta1.addElement(" \"" + allFiles[i].getAbsolutePath() + "\"");
                }

            }
        }
    }//GEN-LAST:event_refGenomeBtnActionPerformed

    private void genomeDirBtn2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genomeDirBtn2ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new java.io.File("."));
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {

            String path = fileChooser.getSelectedFile().toString();

            genomeDir.setText(path);
        }
    }//GEN-LAST:event_genomeDirBtn2ActionPerformed

    private void jLabel97MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel97MouseClicked

        try {
            Desktop.getDesktop().open(new File(System.getProperty("user.dir").concat("/bin/FastQC_Manual.pdf")));
        } catch (IOException ex) {
            App.LOGGER.error("\r\nERROR loading FASTQC mannual file: " + ex);
            App.textArea.append("\r\nERROR loading FASTQC mannual file: " + ex);
        }
    }//GEN-LAST:event_jLabel97MouseClicked

    private void jLabel97MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel97MouseEntered
        jLabel14.setIcon(App.icons[1]);
    }//GEN-LAST:event_jLabel97MouseEntered

    private void jLabel97MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel97MouseExited
        jLabel14.setIcon(App.icons[0]);
    }//GEN-LAST:event_jLabel97MouseExited

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

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel1MouseClicked
        exampleInputData();        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel1MouseClicked

    private void addColumnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addColumnButtonActionPerformed
        String newColumn = JOptionPane.showInputDialog(this, "Enter column name:").toUpperCase();

        if (newColumn != null && !newColumn.isEmpty()) {
            sampleTableModel.addColumn(newColumn);

        }

    }//GEN-LAST:event_addColumnButtonActionPerformed

    private void deleteColumnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteColumnButtonActionPerformed
        int columnCount = sampleTableModel.getColumnCount();
        if (columnCount > 1) {
            int confirmed = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete the column: " + sampleTableModel.getColumnName(sampleTable.getSelectedColumn()) + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
            if (confirmed == JOptionPane.YES_OPTION) {

                TableColumnModel columnModel = sampleTable.getColumnModel();
                TableColumn column = columnModel.getColumn(columnCount - 1);
                sampleTable.removeColumn(column);
                sampleTableModel.setColumnCount(columnCount - 1);

            }//end if
        } else {
            JOptionPane.showMessageDialog(this, "No column selected for deletion.", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_deleteColumnButtonActionPerformed

    private void contrastFactorComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contrastFactorComboActionPerformed
        combination1.removeAllItems();
        combination2.removeAllItems();

        int selectedColumnIndex = contrastFactorCombo.getSelectedIndex() + 1; // +1 to skip first column

        Set<String> uniqueValues = new HashSet<>();
        for (int i = 0; i < sampleTable.getRowCount(); i++) {
            Object value = sampleTable.getValueAt(i, selectedColumnIndex);
            if (value != null) {
                uniqueValues.add(value.toString());
            }
        }

        for (String value : uniqueValues) {
            combination1.addItem(value);
            combination2.addItem(value);

        }

    }//GEN-LAST:event_contrastFactorComboActionPerformed

    private void generateInteractionEffects(Map<String, Set<String>> factorLevels) {
        List<String> interactionEffects = new ArrayList<>();
        List<String> factors = new ArrayList<>(factorLevels.keySet());

        for (int i = 0; i < factors.size(); i++) {
            String factor1 = factors.get(i);
            List<String> levels1 = new ArrayList<>(factorLevels.get(factor1));
            for (int j = i + 1; j < factors.size(); j++) {
                String factor2 = factors.get(j);
                List<String> levels2 = new ArrayList<>(factorLevels.get(factor2));

                // Assuming first level is reference, generate interaction like `Factor1Level1.Factor2Level1`
                String interaction = factor1 + levels1.get(0) + "." + factor2 + levels2.get(0);
                deFactorListModel.addElement(interaction);
            }
        }

    }

    //Function for generating edgeR list (model1)
    public void predictColumnNames() {

// Clear previous entries in model1
        model1.clear();

        TableModel model = sampleTable.getModel();
        Set<String> uniqueCombinations = new HashSet<>();

        if ((model.getColumnCount() == 2)) // || (singleFactorRadio.isSelected())
        {

            // Only one column for sampleGroups, so no concatenation
            for (int row = 0; row < model.getRowCount(); row++) {
                String value = model.getValueAt(row, 1).toString().toLowerCase();
                uniqueCombinations.add(value);
            }
        } else {
            // Multiple columns for sampleGroups, so concatenate values with a dot separator
            for (int row = 0; row < model.getRowCount(); row++) {
                StringBuilder combination = new StringBuilder();
                for (int col = 1; col < model.getColumnCount(); col++) {
                    combination.append(model.getValueAt(row, col).toString());
                    if (col < model.getColumnCount() - 1) {
                        combination.append(".");
                    }
                }
                uniqueCombinations.add(combination.toString());
            }
        }
        // Add each unique combination to model1
        for (String colName : uniqueCombinations) {
            model1.addElement(colName);
        }

    }

    private void deseqRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deseqRadioActionPerformed
        analysisP = "deSeq2";

    }//GEN-LAST:event_deseqRadioActionPerformed

    private void edgeRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edgeRadioActionPerformed
        analysisP = "edgeR";
    }//GEN-LAST:event_edgeRadioActionPerformed

    private void bothRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bothRadioActionPerformed
        analysisP = "both";
    }//GEN-LAST:event_bothRadioActionPerformed

    private void sampleTableFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sampleTableFocusLost
        // Check if a cell is being edited
        if (sampleTable.isEditing()) {
            // Stop cell editing to save the value
            sampleTable.getCellEditor().stopCellEditing();
        }

    }//GEN-LAST:event_sampleTableFocusLost

    private void edgeRTestListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_edgeRTestListValueChanged

        if (interceptOption.getSelectedIndex() == 0 && edgeRTestList.getSelectedIndex() == 0 && edgeRTestCombo.getSelectedIndex() != 1) {
            JOptionPane.showMessageDialog(this, "As there is no intercept to be used as reference so 1st column couldn't be selected.1st column will be used as reference in coef");
            edgeRTestList.clearSelection();
        }

    }//GEN-LAST:event_edgeRTestListValueChanged

    private void tableLockBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tableLockBtnActionPerformed
        if (tableLockBtn.isSelected()) {

            if (sampleTable.getModel().getColumnCount() >= 2 && !hasEmptyCells(sampleTable)) {
                tableLockBtn.setText("Unlock Table");
                addColumnButton.setEnabled(false);
                deleteColumnButton.setEnabled(false);
                //Saving table
                saveTableToTSV(output_Dir.getText() + "sample_groups.tsv");

                tableFlag = false;//Make table uneditable
                sampleTable.repaint();

//Creating Interaction List
                // Retrieve column names from the JTable
                TableColumnModel columnModel = sampleTable.getColumnModel();
                List<String> columnNames = new ArrayList<>();
                // Start from 1 to skip SampleName
                for (int i = 1; i < columnModel.getColumnCount(); i++) {
                    columnNames.add(columnModel.getColumn(i).getHeaderValue().toString());
                }

// Generate interaction terms dynamically based on column count
                interactionListModel.clear();
                for (int r = 2; r <= columnNames.size(); r++) { // r = interaction term size (2-way, 3-way, etc.)
                    generateCombinations(columnNames, new ArrayList<>(), 0, r, interactionListModel);
                }

//Update Factor ComboBox   accordingly
                contrastFactorCombo.removeAllItems();
                for (int i = 1; i < sampleTable.getColumnCount(); i++) { // Skip first column
                    contrastFactorCombo.addItem(sampleTable.getColumnName(i));
                }

//Check if table has two columns or more: If two use Single Factor else use Multiple
// Get the number of columns
                int columnCount = sampleTable.getColumnCount();
                // Retrieve column names in 'factors'
                List<String> factors = new ArrayList<>();
                for (int i = 1; i < columnCount; i++) {  // Skip the first column
                    String columnName = sampleTable.getColumnName(i);
                    factors.add(columnName);
                }
                deFactorListModel.clear();

                // Retrieve unique values (levels) for each factor
                Map<String, Set<String>> factorLevels = new HashMap<>();
                for (String columnName : factors) {
                    Set<String> levels = new HashSet<>();
                    int columnIndex = sampleTable.getColumnModel().getColumnIndex(columnName);
                    for (int row = 0; row < sampleTable.getRowCount(); row++) {
                        levels.add(sampleTable.getValueAt(row, columnIndex).toString().toLowerCase());
                    }
                    factorLevels.put(columnName, levels);
                }//end for

                //Start adding intercepts
                deFactorListModel.addElement("Intercept");

                // 2. Generate main effect combinations
                for (Map.Entry<String, Set<String>> entry : factorLevels.entrySet()) {
                    List<String> levels = new ArrayList<>(entry.getValue());
                    if (levels.size() > 1) {
                        String base = levels.get(0);
                        for (int i = 1; i < levels.size(); i++) {

                            deFactorListModel.addElement(entry.getKey() + "_" + levels.get(i) + "_vs_" + base);
                        }

                    }
                }

                // 3. Generate interaction terms
                List<String> factorsT = new ArrayList<>(factorLevels.keySet());
                if (factorsT.size() > 1) {
                    generateInteractionEffects(factorLevels);

                }
//Adding edgeR result design list
                predictColumnNames();

                ////
                DefaultListModel<String> model1 = (DefaultListModel<String>) edgeRTestList.getModel();
                StringBuilder defaultContrast = new StringBuilder();

                for (int i = 0; i < model1.size(); i++) {
                    String group = model1.getElementAt(i);
                    defaultContrast.append(group).append(":0");
                    if (i < model1.size() - 1) {
                        defaultContrast.append(",");
                    }
                }

// Set the generated string into your contrast text field
                contrastValue.setText(defaultContrast.toString());

//2.3- ALIGNMENT
                starPanel.setVisible(true);
//2.4- EXPRESSION ANALYSIS
                analysisModePanel.setVisible(true);
                if (analysisP.equals("deSeq2")) {
                    resDEPanel.setVisible(true);
                } else if (analysisP.equals("edgeR")) {
                    edgeRDesignPanel.setVisible(true);
                    resEDPanel.setVisible(true);
                } else if (analysisP.equals("both")) {
                    resDEPanel.setVisible(true);
                    edgeRDesignPanel.setVisible(true);
                    resEDPanel.setVisible(true);
                }

//2.5- GENE ENRICHMENT
                geneEnrichmentPanel.setVisible(true);

            } else {
                tableLockBtn.setSelected(false);
                warningLabel.setText("**ERROR: Ensure the metadata includes 'SampleName', at least one other column, and no empty cells.");
            }
        } else {
            tableLockBtn.setText("Lock Table");
            addColumnButton.setEnabled(true);
            deleteColumnButton.setEnabled(true);
            tableFlag = true;//Make table editable
            sampleTable.repaint();

//2.3- ALIGNMENT
            starPanel.setVisible(false);
//2.4- EXPRESSION ANALYSIS
            analysisModePanel.setVisible(false);
            if (analysisP.equals("deSeq2")) {
                resDEPanel.setVisible(false);
            } else if (analysisP.equals("edgeR")) {
                edgeRDesignPanel.setVisible(false);
                resEDPanel.setVisible(false);
            } else if (analysisP.equals("both")) {
                resDEPanel.setVisible(false);
                edgeRDesignPanel.setVisible(false);
                resEDPanel.setVisible(false);
            }

//2.5- GENE ENRICHMENT
            geneEnrichmentPanel.setVisible(false);

        }
    }//GEN-LAST:event_tableLockBtnActionPerformed

    public void generateCombinations(List<String> columnNames, List<String> current, int index, int size, DefaultListModel<String> listModel) {
        if (current.size() == size) {
            // Join selected column names with ":" to create an interaction term
            listModel.addElement(String.join(":", current));
            return;
        }

        for (int i = index; i < columnNames.size(); i++) {
            current.add(columnNames.get(i));
            generateCombinations(columnNames, current, i + 1, size, listModel);
            current.remove(current.size() - 1);
        }
    }

    public boolean hasEmptyCells(JTable table) {
        TableModel model = table.getModel();
        int rowCount = model.getRowCount();
        int columnCount = model.getColumnCount();

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columnCount; col++) {
                Object value = model.getValueAt(row, col);
                if (value == null || value.toString().trim().isEmpty()) {
                    return true; // Found an empty cell
                }
            }
        }
        return false; // No empty cells
    }
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Csv Files (*.csv)", "csv"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Tsv Files (*.tsv)", "tsv"));
        fileChooser.setAcceptAllFileFilterUsed(true);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {

            if (fileChooser.getSelectedFile() != null) {
                BufferedReader br = null;
                try {
                    File file = fileChooser.getSelectedFile();
                    sampleFilePathTxt.setText(file.toString());
                    //Checking csv vs tsv
                    boolean isCSV = file.getName().endsWith(".csv");
                    String delimiter = isCSV ? "," : "\t";
                    br = new BufferedReader(new FileReader(file));

                    // Reset sampleTableModel: keep only the first column (SampleName)
                    while (sampleTableModel.getColumnCount() > 1) {
                        sampleTableModel.setColumnCount(sampleTableModel.getColumnCount() - 1);
                    }
                    // Read and validate header
                    String[] headers = br.readLine().split(delimiter);

                    // Validate "SampleName" as the first column
                    if (!headers[0].trim().equalsIgnoreCase("SampleName")) {
                        JOptionPane.showMessageDialog(null, "The file must have 'SampleName' as the first column.", "Invalid File", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Add new columns to the sampleTableModel if not already present
                    addNewColumns(headers);

                    // Map for quick access to table rows based on cleaned SampleName
                    Map<String, Integer> sampleNameToRowIndex = new HashMap<>();
                    for (int i = 0; i < sampleTableModel.getRowCount(); i++) {
                        String tableSampleName = cleanName(sampleTableModel.getValueAt(i, 0).toString());
                        sampleNameToRowIndex.put(tableSampleName, i);
                    }

                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] values = line.split(delimiter, -1); // -1 to preserve empty trailing columns
                        String fileSampleName = cleanName(values[0]);

                        if (sampleNameToRowIndex.containsKey(fileSampleName)) {
                            int rowIndex = sampleNameToRowIndex.get(fileSampleName);

                            // Update the table row with the corresponding file data
                            for (int col = 1; col < values.length; col++) {
                                sampleTableModel.setValueAt(values[col], rowIndex, col);
                            }
                        }
                    }
                    br.close();

                } //if
                catch (FileNotFoundException ex) {
                    Logger.getLogger(RNASeqPipeLineInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(RNASeqPipeLineInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        br.close();
                    } catch (IOException ex) {
                        Logger.getLogger(RNASeqPipeLineInternalFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        }//end-if

    }//GEN-LAST:event_jButton1ActionPerformed

    private void sampleRadio1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sampleRadio1ActionPerformed
        jButton1.setEnabled(true);
        addColumnButton.setEnabled(false);
        deleteColumnButton.setEnabled(false);
    }//GEN-LAST:event_sampleRadio1ActionPerformed

    private void sampleRadio2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sampleRadio2ActionPerformed
        jButton1.setEnabled(false);
        sampleFilePathTxt.setText("");
        addColumnButton.setEnabled(true);
        deleteColumnButton.setEnabled(true);

    }//GEN-LAST:event_sampleRadio2ActionPerformed

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

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed

        if (deseqRadio.isSelected()) {
            runShiny(System.getProperty("user.dir").concat("/bin/shinyGraphs.R"), output_Dir.getText().concat("DeSeqResults") + "," + output_Dir.getText().concat("DeSeqResults/GeneEnrich_Results"), System.getProperty("user.dir").concat("/R_Libraries"), "DESEQ2,GSEA");
        } else if (edgeRadio.isSelected()) {
            runShiny(System.getProperty("user.dir").concat("/bin/shinyGraphs.R"), output_Dir.getText().concat("EdgeRResults") + "," + output_Dir.getText().concat("EdgeRResults/GeneEnrich_Results"), System.getProperty("user.dir").concat("/R_Libraries"), "EDGER,GSEA");
        } else if (bothRadio.isSelected()) {

            runShiny(System.getProperty("user.dir").concat("/bin/shinyGraphs.R"), output_Dir.getText().concat("DeSeqResults") + "," + output_Dir.getText().concat("EdgeRResults") + "," + output_Dir.getText().concat("DeSeqResults/GeneEnrich_Results") + "," + output_Dir.getText().concat("EdgeRResults/GeneEnrich_Results"), System.getProperty("user.dir").concat("/R_Libraries"), "DESEQ2,EDGER,GSEA_DESEQ2,GSEA_EDGER");

        }

    }//GEN-LAST:event_jButton2ActionPerformed

    private void fastqcTrimComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fastqcTrimComboActionPerformed
        if (fastqcTrimCombo.getSelectedIndex() == 0) {
            trimPanel.setVisible(false);
            nextBtn.setText("Proceed to Run Pipeline");

        } else {
            trimPanel.setVisible(true);
            nextBtn.setText("Continue Trimming & QC Refinement");
        }
    }//GEN-LAST:event_fastqcTrimComboActionPerformed

    private void fastpTrimComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fastpTrimComboActionPerformed
        if (fastpTrimCombo.getSelectedIndex() == 0) {
            nextBtn.setText("Proceed to Run Pipeline");
            fastPparameter.setVisible(false);
        } else {
            nextBtn.setText("Continue Trimming & QC Refinement");
            fastPparameter.setVisible(true);
        }
    }//GEN-LAST:event_fastpTrimComboActionPerformed

    private void fastpRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fastpRadioActionPerformed

        nextBtn.setText("Proceed to Quality Check");
    }//GEN-LAST:event_fastpRadioActionPerformed

    private void fastqcRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fastqcRadioActionPerformed
        nextBtn.setText("Proceed to Quality Check");
    }//GEN-LAST:event_fastqcRadioActionPerformed

    private void jLabel79MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel79MouseClicked
        App.helpWebpage("https://github.com/OpenGene/fastp/blob/master/README.md");
    }//GEN-LAST:event_jLabel79MouseClicked

    private void noneRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noneRadioActionPerformed
        nextBtn.setText("Next");
    }//GEN-LAST:event_noneRadioActionPerformed

    private void back1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_back1ActionPerformed
        QualityControlPanel.setVisible(false);
        inputPanel.setVisible(true);
        nextBtn.setText("Procees to Quality Check");

    }//GEN-LAST:event_back1ActionPerformed

    private void back2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_back2ActionPerformed
        downPanel.setVisible(false);
        QualityControlPanel.setVisible(true);
    }//GEN-LAST:event_back2ActionPerformed

    private void filteringComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_filteringComboItemStateChanged
        if (filteringCombo.getSelectedIndex() == 0) // None,
        {
            countThresh.setEnabled(false);
            countThresh1.setEnabled(false);

        } else if (filteringCombo.getSelectedIndex() == 1) { //Total Count ≥ X,
            countThresh.setEnabled(true);
            countThresh1.setEnabled(false);
        } else if (filteringCombo.getSelectedIndex() == 2) {// Present in ≥ Y% Samples,
            countThresh.setEnabled(false);
            countThresh1.setEnabled(true);
        } else if (filteringCombo.getSelectedIndex() == 3) {// Both
            countThresh.setEnabled(true);
            countThresh1.setEnabled(true);
        }

    }//GEN-LAST:event_filteringComboItemStateChanged

    private void mannualContrastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mannualContrastActionPerformed
        String text = mannualContrast.getText().trim();
        if (text.isEmpty()) {
            warningLabel.setText("Adding an empty contrast is not allowed.Please enter a valid contrast combination.");
        }
        if (!deFactorListModel.contains(text)) {
            deFactorListModel.addElement(text);
        } else {
            warningLabel.setText("This contrast combination already exists. Please enter a different contrast");
        }
        mannualContrast.setText("");
        warningLabel.setText("");
    }//GEN-LAST:event_mannualContrastActionPerformed
    // Add new columns from file headers if they don't already exist

    private void addNewColumns(String[] headers) {
        for (int i = 1; i < headers.length; i++) { // Start from 1, skip "SampleName"
            if (!columnExists(headers[i])) {
                sampleTableModel.addColumn(headers[i]);
            }
        }

        // Ensure all rows have enough columns
        int totalColumns = sampleTableModel.getColumnCount();
        for (int i = 0; i < sampleTableModel.getRowCount(); i++) {
            while (sampleTableModel.getColumnCount() > sampleTableModel.getColumnCount()) {
                sampleTableModel.addColumn(null);
            }
        }
    }

    private boolean columnExists(String columnName) {
        for (int i = 0; i < sampleTableModel.getColumnCount(); i++) {
            if (sampleTableModel.getColumnName(i).equalsIgnoreCase(columnName)) {
                return true;
            }
        }
        return false;
    }

    // Utility method to clean SampleName (remove extensions and suffixes)
    private String cleanName(String name) {
        // Remove file extensions: .fq, .fq.gz, .fq.bz2, .fastq, .fastq.gz, .fastq.bz2
        name = name.replaceAll("\\.(fq|fq\\.gz|fq\\.bz2|fastq|fastq\\.gz|fastq\\.bz2)$", "");

        // Remove suffixes like _trim, _p, _P
        name = name.replaceAll("(_trim|_p|_P)$", "");
        return name;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel MainPanel;
    private javax.swing.ButtonGroup QCbuttonGroup;
    private javax.swing.JPanel QualityControlPanel;
    private javax.swing.JTextField adapterfile;
    private javax.swing.JButton addColumnButton;
    private javax.swing.JPanel analysisModePanel;
    private javax.swing.JButton back1;
    private javax.swing.JButton back2;
    private javax.swing.JRadioButton bothRadio;
    private javax.swing.JButton browseGTFBtn1;
    private javax.swing.JButton browseGTFBtn2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.JComboBox<String> combination1;
    private javax.swing.JComboBox<String> combination2;
    private javax.swing.JRadioButton combinedRadio;
    private javax.swing.JPanel complexDesignPanel;
    private javax.swing.JComboBox<String> contrastFactorCombo;
    private javax.swing.JTextField contrastValue;
    private javax.swing.JTextField countThresh;
    private javax.swing.JTextField countThresh1;
    private javax.swing.JTextField crop;
    private javax.swing.JCheckBox cropC;
    private javax.swing.JList<String> deFactorList;
    private javax.swing.JButton deleteColumnButton;
    private javax.swing.JRadioButton deseqRadio;
    private javax.swing.JPanel downPanel;
    private javax.swing.JRadioButton eachRadio;
    private javax.swing.JPanel edgeRDesignPanel;
    private javax.swing.JComboBox<String> edgeRTestCombo;
    private javax.swing.JList<String> edgeRTestList;
    private javax.swing.JRadioButton edgeRadio;
    private javax.swing.ButtonGroup expressionAnalysisBG;
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
    private javax.swing.ButtonGroup featureCountsBG;
    private javax.swing.JComboBox<String> filteringCombo;
    private javax.swing.JPanel geneEnrichmentPanel;
    private javax.swing.JTextField genomeDir;
    private javax.swing.JButton genomeDirBtn2;
    private javax.swing.JTextField headcrop;
    private javax.swing.JCheckBox headcropC;
    private javax.swing.JLabel helpLabel;
    private javax.swing.JTextField illumniaclip;
    private javax.swing.JPanel inputPanel;
    private javax.swing.ButtonGroup inputReadsBG;
    private javax.swing.JList<String> interactionList;
    private javax.swing.JComboBox<String> interactionTermCombo;
    private javax.swing.JPanel interactionTermPanel;
    private javax.swing.JComboBox<String> interceptOption;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JComboBox<String> jComboBox10;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JComboBox<String> jComboBox5;
    private javax.swing.JComboBox<String> jComboBox6;
    private javax.swing.JComboBox<String> jComboBox7;
    private javax.swing.JComboBox<String> jComboBox8;
    private javax.swing.JComboBox<String> jComboBox9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel100;
    private javax.swing.JLabel jLabel101;
    private javax.swing.JLabel jLabel102;
    private javax.swing.JLabel jLabel103;
    private javax.swing.JLabel jLabel104;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
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
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JLabel jLabel79;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel80;
    private javax.swing.JLabel jLabel81;
    private javax.swing.JLabel jLabel82;
    private javax.swing.JLabel jLabel83;
    private javax.swing.JLabel jLabel84;
    private javax.swing.JLabel jLabel85;
    private javax.swing.JLabel jLabel86;
    private javax.swing.JLabel jLabel87;
    private javax.swing.JLabel jLabel88;
    private javax.swing.JLabel jLabel89;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel90;
    private javax.swing.JLabel jLabel91;
    private javax.swing.JLabel jLabel92;
    private javax.swing.JLabel jLabel93;
    private javax.swing.JLabel jLabel94;
    private javax.swing.JLabel jLabel95;
    private javax.swing.JLabel jLabel97;
    private javax.swing.JLabel jLabel98;
    private javax.swing.JLabel jLabel99;
    private javax.swing.JRadioButton jRadioContrast;
    private javax.swing.JRadioButton jRadioList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField13;
    private javax.swing.JTextField jTextField14;
    private javax.swing.JTextField jTextField15;
    private javax.swing.JTextField jTextField16;
    private javax.swing.JTextField jTextField17;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField21;
    private javax.swing.JTextField jTextField22;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JTextField leading;
    private javax.swing.JCheckBox leadingC;
    private javax.swing.JCheckBox logC;
    private javax.swing.JTextField mannualContrast;
    private javax.swing.JTextField maxinfo;
    private javax.swing.JCheckBox maxinfoC;
    private javax.swing.JTextField minlen;
    private javax.swing.JCheckBox minlenC;
    private javax.swing.JRadioButton multiFactorRadio;
    private javax.swing.JPanel multipleFeaturePanel;
    private javax.swing.JButton nextBtn;
    private javax.swing.JRadioButton noneRadio;
    private javax.swing.JTextField outBAMsortingBinsN;
    private javax.swing.JTextField outFileNamePrefix;
    private javax.swing.JTextField outSAMmapqUnique;
    private javax.swing.JComboBox<String> outSAMtype2;
    private javax.swing.JComboBox<String> outSAMunmapped;
    private javax.swing.JButton outputBtn;
    private javax.swing.JTextField output_Dir;
    private javax.swing.JRadioButton pairRadioButton;
    private javax.swing.JComboBox<String> phredEncodingCombo;
    private javax.swing.JComboBox<String> quantMode;
    private javax.swing.JButton reRunResults;
    private javax.swing.JComboBox<String> readFilesCommand;
    private javax.swing.JList<String> readFilesInPair1;
    private javax.swing.JList<String> readFilesInPair2;
    private javax.swing.JList<String> readFilesInSingle;
    private javax.swing.JButton refGenomeBtn;
    private javax.swing.JList<String> refGenomeList;
    private javax.swing.JPanel resDEPanel;
    private javax.swing.JPanel resEDPanel;
    private javax.swing.JTextField runMode2;
    private javax.swing.JComboBox<String> runModeCombo;
    private javax.swing.JPanel sampleDataPanel;
    private javax.swing.JTextField sampleFilePathTxt;
    private javax.swing.JRadioButton sampleRadio1;
    private javax.swing.JRadioButton sampleRadio2;
    private javax.swing.JTable sampleTable;
    private javax.swing.ButtonGroup sampleTableRadios;
    private javax.swing.JLabel selectInfoLabel;
    private javax.swing.JRadioButton singleFactorRadio;
    private javax.swing.JRadioButton singleRadioButton;
    private javax.swing.JTextField sjdbGTFfile;
    private javax.swing.JTextField sjdbOverhang2;
    private javax.swing.JTextField sliding;
    private javax.swing.JCheckBox slidingC;
    private javax.swing.JPanel starPanel;
    private javax.swing.JTextField starThread;
    private javax.swing.JToggleButton tableLockBtn;
    private javax.swing.JLabel testInfoLabel;
    private javax.swing.JLabel testInfoLabel1;
    private javax.swing.JLabel testInfoLabel2;
    private javax.swing.JTextField thread;
    private javax.swing.JCheckBox threadC;
    private javax.swing.JTextField trailing;
    private javax.swing.JCheckBox trailingC;
    private javax.swing.JPanel trimPanel;
    private javax.swing.JComboBox<String> twopassMode;
    private javax.swing.JPanel upPanel;
    private javax.swing.JLabel warningLabel;
    // End of variables declaration//GEN-END:variables
}
