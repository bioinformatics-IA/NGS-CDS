/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JInternalFrame.java to edit this template
 */
package NGSGradle;

import java.io.File;
import javax.swing.JFileChooser;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import static javax.swing.TransferHandler.MOVE;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class GATKFrame extends javax.swing.JInternalFrame {

    JFileChooser fileChooser;
    File allFile;
    private File[] allFiles;
    private DefaultListModel listModel, listNormal,listTumor, listModelHaplo;
    private Vector<String> items;
    private DefaultComboBoxModel<String> model;
    

    private ArrayList<JLabel> ln;
    private ArrayList<JTextField> tn;

    private String somaticOP = "TUMORMIT";
    private boolean error = false; //No error by default      

    String toolName;

    public GATKFrame() {

        listModel = new DefaultListModel();
        listNormal = new DefaultListModel();
        listTumor = new DefaultListModel();
        
        listModelHaplo = new DefaultListModel();
        
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

       
        ln = new ArrayList<>();
        tn = new ArrayList<>();

        //Icons:
        jLabel5.setIcon(App.icons[0]);
        jLabel10.setIcon(App.icons[0]);
        jLabel15.setIcon(App.icons[0]);
        jLabel30.setIcon(App.icons[0]);
        jLabel35.setIcon(App.icons[0]);
        jLabel18.setIcon(App.icons[0]);

        //PANELS:
        AnalyzeCovariatesPanel.setVisible(false);
        HaplotypeCallerPanel.setVisible(false);
        VariantFiltrationPanel.setVisible(false);
        ApplyBQSRPanel.setVisible(false);
        BaseRecalibratorPanel.setVisible(false);
        IndexFeatureFilePanel.setVisible(false);
        mutect2Panel.setVisible(false);

        //Radio Buttons 
        buttonGroup1.add(radioPlot1);
        buttonGroup1.add(radioPlot2);
        buttonGroup1.add(radioPlot3);
        //AnalyzeCovariates Components
        input2Txt.setEnabled(false);
        output2Txt.setEnabled(false);
        jButton12.setEnabled(false);
        jButton13.setEnabled(false);
        plot1Btn.setEnabled(false);

        input2Txt1.setEnabled(false);
        input2Txt2.setEnabled(false);
        output2Txt1.setEnabled(false);
        jButton22.setEnabled(false);
        jButton23.setEnabled(false);
        jButton21.setEnabled(false);
        plot2Btn.setEnabled(false);

        input2Txt4.setEnabled(false);
        input2Txt5.setEnabled(false);
        input2Txt3.setEnabled(false);
        output2Txt2.setEnabled(false);
        jButton24.setEnabled(false);
        jButton27.setEnabled(false);
        jButton25.setEnabled(false);
        jButton26.setEnabled(false);
        plot3Btn.setEnabled(false);

        //mutect2

        germlineResTxt.addMouseListener(mouseListener);
        ponTxt.addMouseListener(mouseListener);
        alleleTxt.addMouseListener(mouseListener);
        f1r2.addMouseListener(mouseListener);

    }

    ///////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////
//    public void addNormalComponents() {
//
//        int numEntries = listNormal.size();
//        jPanelNormal.removeAll();
//
//        // Clear the existing components
//        ln.clear();
//        tn.clear();
//
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//        gbc.insets = new Insets(5, 5, 5, 5);
//        gbc.anchor = GridBagConstraints.WEST;
//
//        // Add new components based on the number of entries in the JList
//        for (int i = 0; i < numEntries; i++) {
//
//            JLabel label1 = new JLabel("Sample Name " + (i + 1) + ":");
//            JTextField textField1 = new JTextField(25);
//            //Reading just file name from the list
//            Path path = Paths.get(jListNormal.getModel().getElementAt(i));
//            textField1.setText(StringUtils.chop(path.getFileName().toString().replaceAll("\\.(?![^.]+$)", "")));
//
//            label1.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
//            label1.setForeground(new java.awt.Color(255, 255, 255));
//
//            ln.add(label1);
//            tn.add(textField1);
//
//            jPanelNormal.add(label1, gbc);
//            gbc.gridx++;
//            jPanelNormal.add(textField1, gbc);
//            gbc.gridx++;
//
//            gbc.gridy++;
//
//            gbc.gridx = 0;
//
//        }//for
//
//        // Repaint the UI to reflect the changes
//        jPanelNormal.revalidate();
//        jPanelNormal.repaint();
//        /////////////////////////////////////////////////////////
//
//    }
//
//    public void removeNormalComponents() {
//        jPanelNormal.removeAll();
//
//        // Clear the existing components
//        ln.clear();
//        tn.clear();
//
//        // Repaint the UI to reflect the changes
//        jPanelNormal.revalidate();
//        jPanelNormal.repaint();
//
//    }
//
//    public String retrieveNSampleData(int i) {
//
//        JTextField textField = tn.get(i);
//        String value = textField.getText();
//        return value;
//    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        splitPane = new javax.swing.JSplitPane();
        mainPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgLoginMain, 0, 0, null);
            }
        }

        ;
        BaseRecalibratorPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgLoginMain, 0, 0, null);
            }
        }

        ;
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        inputTxt = new javax.swing.JTextField();
        knownsitesTxt = new javax.swing.JTextField();
        refTxt = new javax.swing.JTextField();
        outputTxt = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        knownsitesTxt1 = new javax.swing.JTextField();
        jButton15 = new javax.swing.JButton();
        ApplyBQSRPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgLoginMain, 0, 0, null);
            }
        }

        ;
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        ref1Txt = new javax.swing.JTextField();
        bqsrTxt = new javax.swing.JTextField();
        output1Txt = new javax.swing.JTextField();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jButton11 = new javax.swing.JButton();
        input1Txt = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        AnalyzeCovariatesPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgLoginMain, 0, 0, null);
            }
        }

        ;
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        input2Txt = new javax.swing.JTextField();
        output2Txt = new javax.swing.JTextField();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        plot1Btn = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        radioPlot1 = new javax.swing.JRadioButton();
        radioPlot2 = new javax.swing.JRadioButton();
        radioPlot3 = new javax.swing.JRadioButton();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        output2Txt1 = new javax.swing.JTextField();
        input2Txt1 = new javax.swing.JTextField();
        plot2Btn = new javax.swing.JButton();
        jButton21 = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        jLabel23 = new javax.swing.JLabel();
        input2Txt2 = new javax.swing.JTextField();
        jButton23 = new javax.swing.JButton();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        plot3Btn = new javax.swing.JButton();
        output2Txt2 = new javax.swing.JTextField();
        input2Txt3 = new javax.swing.JTextField();
        input2Txt4 = new javax.swing.JTextField();
        jButton24 = new javax.swing.JButton();
        jButton25 = new javax.swing.JButton();
        jButton26 = new javax.swing.JButton();
        jLabel27 = new javax.swing.JLabel();
        input2Txt5 = new javax.swing.JTextField();
        jButton27 = new javax.swing.JButton();
        HaplotypeCallerPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgLoginMain, 0, 0, null);
            }
        }

        ;
        jLabel28 = new javax.swing.JLabel();
        input2Txt6 = new javax.swing.JTextField();
        jButton28 = new javax.swing.JButton();
        plot1Btn1 = new javax.swing.JButton();
        jLabel30 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jButton37 = new javax.swing.JButton();
        jLabel39 = new javax.swing.JLabel();
        jButton38 = new javax.swing.JButton();
        outputTxt13 = new javax.swing.JTextField();
        jScrollPane5 = new javax.swing.JScrollPane();
        inputBamTxt1 = new javax.swing.JList<>(listModelHaplo);
        jLabel19 = new javax.swing.JLabel();
        jointModeH = new javax.swing.JRadioButton();
        annotationGroupCombo = new javax.swing.JComboBox<>(items);
        jScrollPane11 = new javax.swing.JScrollPane();
        annotationGroupTxt = new javax.swing.JTextArea();
        add7 = new javax.swing.JButton();
        jLabel48 = new javax.swing.JLabel();
        del7 = new javax.swing.JButton();
        bamoutTxt = new javax.swing.JTextField();
        jLabel49 = new javax.swing.JLabel();
        bamoutB = new javax.swing.JButton();
        jLabel50 = new javax.swing.JLabel();
        ercCombo = new javax.swing.JComboBox<>();
        jLabel51 = new javax.swing.JLabel();
        g1Check = new javax.swing.JCheckBox();
        jLabel52 = new javax.swing.JLabel();
        bamoutCheck = new javax.swing.JCheckBox();
        jLabel53 = new javax.swing.JLabel();
        singleModeH = new javax.swing.JRadioButton();
        VariantFiltrationPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgLoginMain, 0, 0, null);
            }
        }

        ;
        jLabel33 = new javax.swing.JLabel();
        input2Txt8 = new javax.swing.JTextField();
        jButton29 = new javax.swing.JButton();
        plot1Btn2 = new javax.swing.JButton();
        jLabel35 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        input2Txt13 = new javax.swing.JTextField();
        jButton39 = new javax.swing.JButton();
        jLabel41 = new javax.swing.JLabel();
        jButton40 = new javax.swing.JButton();
        outputTxt14 = new javax.swing.JTextField();
        filterName = new javax.swing.JTextField();
        filterExpr = new javax.swing.JTextField();
        add5 = new javax.swing.JButton();
        del5 = new javax.swing.JButton();
        jScrollPane10 = new javax.swing.JScrollPane();
        variantFilterTxt = new javax.swing.JTextArea();
        jLabel29 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        SelectVariantsPanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgLoginMain, 0, 0, null);
            }
        }

        ;
        jLabel36 = new javax.swing.JLabel();
        input2Txt9 = new javax.swing.JTextField();
        jButton30 = new javax.swing.JButton();
        plot1Btn3 = new javax.swing.JButton();
        jLabel37 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        input2Txt14 = new javax.swing.JTextField();
        jButton41 = new javax.swing.JButton();
        jLabel57 = new javax.swing.JLabel();
        jButton42 = new javax.swing.JButton();
        outputTxt15 = new javax.swing.JTextField();
        del3 = new javax.swing.JButton();
        add4 = new javax.swing.JButton();
        del4 = new javax.swing.JButton();
        add2 = new javax.swing.JButton();
        del2 = new javax.swing.JButton();
        jScrollPane8 = new javax.swing.JScrollPane();
        selectVariantTxt = new javax.swing.JTextArea();
        jLabel61 = new javax.swing.JLabel();
        intervalBtn = new javax.swing.JButton();
        jLabel62 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        select = new javax.swing.JTextField();
        selectGenotype = new javax.swing.JTextField();
        Linterval = new javax.swing.JTextField();
        jCheckBox5 = new javax.swing.JCheckBox();
        jCheckBox6 = new javax.swing.JCheckBox();
        jCheckBox7 = new javax.swing.JCheckBox();
        jCheckBox8 = new javax.swing.JCheckBox();
        selectTypeCombo = new javax.swing.JComboBox<>();
        del1 = new javax.swing.JButton();
        add1 = new javax.swing.JButton();
        add3 = new javax.swing.JButton();
        IndexFeatureFilePanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgLoginMain, 0, 0, null);
            }
        }

        ;
        jLabel12 = new javax.swing.JLabel();
        inputTxt1 = new javax.swing.JTextField();
        jButton14 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        mutect2Panel = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        jLabel92 = new javax.swing.JLabel();
        jLabel95 = new javax.swing.JLabel();
        refTxt1 = new javax.swing.JTextField();
        output_Dir = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        inputBamTxt = new javax.swing.JList<>(listModel);
        outputBtn = new javax.swing.JButton();
        inputbamBtn = new javax.swing.JButton();
        refBtn = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        mutectCombo = new javax.swing.JComboBox<>();
        normalPanel = new javax.swing.JPanel();
        jLabel58 = new javax.swing.JLabel();
        tumNorBtn = new javax.swing.JButton();
        norTumBtn = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        jListNormal = new javax.swing.JList<>(listNormal);
        jScrollPane14 = new javax.swing.JScrollPane();
        jListTumor = new javax.swing.JList<>(listTumor);
        jLabel59 = new javax.swing.JLabel();
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
        jScrollPane15 = new javax.swing.JScrollPane();
        intervalText = new javax.swing.JTextArea();
        intervalT = new javax.swing.JTextField();
        add9 = new javax.swing.JButton();
        del9 = new javax.swing.JButton();
        jLabel70 = new javax.swing.JLabel();
        jLabel71 = new javax.swing.JLabel();
        jLabel72 = new javax.swing.JLabel();
        ponLabel8 = new javax.swing.JLabel();
        generatePON = new javax.swing.JCheckBox();
        ponLabel9 = new javax.swing.JLabel();
        ponLabel10 = new javax.swing.JLabel();
        jLabel73 = new javax.swing.JLabel();
        germlineCheck = new javax.swing.JCheckBox();
        ponCheck = new javax.swing.JCheckBox();
        allelesCheck = new javax.swing.JCheckBox();
        f1r2Check = new javax.swing.JCheckBox();
        intervalCheck = new javax.swing.JCheckBox();
        warningLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTree = new javax.swing.JTree();

        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("GATK");

        BaseRecalibratorPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "BaseRecalibrator: Generates recalibration table for Base Quality Score Recalibration (BQSR)", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N
        BaseRecalibratorPanel.setOpaque(false);
        BaseRecalibratorPanel.setPreferredSize(new java.awt.Dimension(1200, 500));

        jLabel1.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("INPUT SAM/BAM/CRAM:");

        jLabel2.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Reference File:");

        jLabel3.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Known Sites:");

        jLabel4.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Output:");

        jButton1.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButton1.setText("Browse");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButton2.setText("Browse");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButton3.setText("Browse");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButton4.setText("Browse");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButton5.setText("Perform Analysis");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("HELP?");
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel5MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel5MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel5MouseExited(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setText("Known Sites (Optional):");

        jButton15.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButton15.setText("Browse");
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout BaseRecalibratorPanelLayout = new javax.swing.GroupLayout(BaseRecalibratorPanel);
        BaseRecalibratorPanel.setLayout(BaseRecalibratorPanelLayout);
        BaseRecalibratorPanelLayout.setHorizontalGroup(
            BaseRecalibratorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, BaseRecalibratorPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel5)
                .addGap(100, 100, 100))
            .addGroup(BaseRecalibratorPanelLayout.createSequentialGroup()
                .addGroup(BaseRecalibratorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(BaseRecalibratorPanelLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(BaseRecalibratorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1)
                            .addComponent(jLabel4)
                            .addComponent(jLabel17))
                        .addGap(18, 18, 18)
                        .addGroup(BaseRecalibratorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(inputTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
                            .addComponent(refTxt)
                            .addComponent(knownsitesTxt)
                            .addComponent(outputTxt)
                            .addComponent(knownsitesTxt1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(BaseRecalibratorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton2)
                            .addComponent(jButton3)
                            .addComponent(jButton1)
                            .addComponent(jButton4)
                            .addComponent(jButton15)))
                    .addGroup(BaseRecalibratorPanelLayout.createSequentialGroup()
                        .addGap(438, 438, 438)
                        .addComponent(jButton5)))
                .addContainerGap(369, Short.MAX_VALUE))
        );
        BaseRecalibratorPanelLayout.setVerticalGroup(
            BaseRecalibratorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(BaseRecalibratorPanelLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(BaseRecalibratorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(BaseRecalibratorPanelLayout.createSequentialGroup()
                        .addGroup(BaseRecalibratorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(inputTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(23, 23, 23)
                        .addGroup(BaseRecalibratorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(refTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(28, 28, 28)
                        .addGroup(BaseRecalibratorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(knownsitesTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(28, 28, 28)
                        .addGroup(BaseRecalibratorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel17)
                            .addComponent(knownsitesTxt1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(BaseRecalibratorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(outputTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(BaseRecalibratorPanelLayout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addGap(23, 23, 23)
                        .addComponent(jButton1)
                        .addGap(28, 28, 28)
                        .addComponent(jButton3)
                        .addGap(28, 28, 28)
                        .addComponent(jButton15)
                        .addGap(18, 18, 18)
                        .addComponent(jButton4)))
                .addGap(85, 85, 85)
                .addComponent(jButton5)
                .addContainerGap(97, Short.MAX_VALUE))
        );

        ApplyBQSRPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ApplyBQSR:  Apply base quality score recalibration", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 24), new java.awt.Color(255, 255, 255))); // NOI18N
        ApplyBQSRPanel.setOpaque(false);
        ApplyBQSRPanel.setPreferredSize(new java.awt.Dimension(1200, 500));

        jLabel6.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("REFERENCE FILE:");

        jLabel8.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("BQSR FILE:");

        jLabel9.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("OUTPUT FILE:");

        jButton7.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButton7.setText("Browse");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButton8.setText("Browse");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButton9.setText("Browse");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton10.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButton10.setText("Perform Analysis");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("HELP?");
        jLabel10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel10MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel10MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel10MouseExited(evt);
            }
        });

        jButton11.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButton11.setText("Browse");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("INPUT FILE:");

        javax.swing.GroupLayout ApplyBQSRPanelLayout = new javax.swing.GroupLayout(ApplyBQSRPanel);
        ApplyBQSRPanel.setLayout(ApplyBQSRPanelLayout);
        ApplyBQSRPanelLayout.setHorizontalGroup(
            ApplyBQSRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ApplyBQSRPanelLayout.createSequentialGroup()
                .addContainerGap(1046, Short.MAX_VALUE)
                .addComponent(jLabel10)
                .addGap(99, 99, 99))
            .addGroup(ApplyBQSRPanelLayout.createSequentialGroup()
                .addGap(67, 67, 67)
                .addGroup(ApplyBQSRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ApplyBQSRPanelLayout.createSequentialGroup()
                        .addGroup(ApplyBQSRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7))
                        .addGap(18, 18, 18)
                        .addGroup(ApplyBQSRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(ref1Txt)
                            .addComponent(input1Txt)
                            .addComponent(bqsrTxt)
                            .addComponent(output1Txt, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(ApplyBQSRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton9)
                            .addComponent(jButton7)
                            .addComponent(jButton8)
                            .addComponent(jButton11)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ApplyBQSRPanelLayout.createSequentialGroup()
                        .addComponent(jButton10)
                        .addGap(296, 296, 296)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        ApplyBQSRPanelLayout.setVerticalGroup(
            ApplyBQSRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ApplyBQSRPanelLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel10)
                .addGap(26, 26, 26)
                .addGroup(ApplyBQSRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ApplyBQSRPanelLayout.createSequentialGroup()
                        .addGroup(ApplyBQSRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(ref1Txt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(ApplyBQSRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(input1Txt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(ApplyBQSRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(bqsrTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(20, 20, 20)
                        .addGroup(ApplyBQSRPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(output1Txt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(ApplyBQSRPanelLayout.createSequentialGroup()
                        .addComponent(jButton9)
                        .addGap(18, 18, 18)
                        .addComponent(jButton11)
                        .addGap(18, 18, 18)
                        .addComponent(jButton7)
                        .addGap(20, 20, 20)
                        .addComponent(jButton8)))
                .addGap(70, 70, 70)
                .addComponent(jButton10)
                .addContainerGap(161, Short.MAX_VALUE))
        );

        AnalyzeCovariatesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "AnalyzeCovariates: Evaluate and compare base quality score recalibration (BQSR) tables", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 24), new java.awt.Color(255, 255, 255))); // NOI18N
        AnalyzeCovariatesPanel.setOpaque(false);

        jLabel11.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("INPUT FILE:");

        jLabel13.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("OUTPUT:");

        jButton12.setText("Browse");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jButton13.setText("Browse");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        plot1Btn.setText("Analyze");
        plot1Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plot1BtnActionPerformed(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setText("HELP?");
        jLabel15.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel15MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel15MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel15MouseExited(evt);
            }
        });

        radioPlot1.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        radioPlot1.setForeground(new java.awt.Color(255, 255, 255));
        radioPlot1.setText("Plot a single recalibration table");
        radioPlot1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioPlot1ActionPerformed(evt);
            }
        });

        radioPlot2.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        radioPlot2.setForeground(new java.awt.Color(255, 255, 255));
        radioPlot2.setText("Plot \"before\" (first pass) and \"after\" (second pass) recalibration tables to compare them");
        radioPlot2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioPlot2ActionPerformed(evt);
            }
        });

        radioPlot3.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        radioPlot3.setForeground(new java.awt.Color(255, 255, 255));
        radioPlot3.setText("Plot up to three recalibration tables for comparison");
        radioPlot3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioPlot3ActionPerformed(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(255, 255, 255));
        jLabel21.setText("Original (before):");

        jLabel22.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(255, 255, 255));
        jLabel22.setText("OUTPUT:");

        plot2Btn.setText("Analyze");
        plot2Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plot2BtnActionPerformed(evt);
            }
        });

        jButton21.setText("Browse");
        jButton21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton21ActionPerformed(evt);
            }
        });

        jButton22.setText("Browse");
        jButton22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton22ActionPerformed(evt);
            }
        });

        jLabel23.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel23.setForeground(new java.awt.Color(255, 255, 255));
        jLabel23.setText("Recalibrated (after):");

        jButton23.setText("Browse");
        jButton23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton23ActionPerformed(evt);
            }
        });

        jLabel24.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setText("INPUT:");

        jLabel25.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(255, 255, 255));
        jLabel25.setText("Recalibrated (after):");

        jLabel26.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel26.setForeground(new java.awt.Color(255, 255, 255));
        jLabel26.setText("OUTPUT:");

        plot3Btn.setText("Analyze");
        plot3Btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plot3BtnActionPerformed(evt);
            }
        });

        jButton24.setText("Browse");
        jButton24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton24ActionPerformed(evt);
            }
        });

        jButton25.setText("Browse");
        jButton25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton25ActionPerformed(evt);
            }
        });

        jButton26.setText("Browse");
        jButton26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton26ActionPerformed(evt);
            }
        });

        jLabel27.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel27.setForeground(new java.awt.Color(255, 255, 255));
        jLabel27.setText("Original (before):");

        jButton27.setText("Browse");
        jButton27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton27ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout AnalyzeCovariatesPanelLayout = new javax.swing.GroupLayout(AnalyzeCovariatesPanel);
        AnalyzeCovariatesPanel.setLayout(AnalyzeCovariatesPanelLayout);
        AnalyzeCovariatesPanelLayout.setHorizontalGroup(
            AnalyzeCovariatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, AnalyzeCovariatesPanelLayout.createSequentialGroup()
                .addContainerGap(978, Short.MAX_VALUE)
                .addComponent(jLabel15)
                .addGap(54, 54, 54))
            .addComponent(jSeparator1)
            .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator2)
                .addContainerGap())
            .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                .addGroup(AnalyzeCovariatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                        .addGap(246, 246, 246)
                        .addGroup(AnalyzeCovariatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(AnalyzeCovariatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                                    .addComponent(jLabel13)
                                    .addGap(18, 18, 18)
                                    .addComponent(output2Txt, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(jButton13))
                                .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                                    .addComponent(jLabel11)
                                    .addGap(18, 18, 18)
                                    .addComponent(input2Txt, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(jButton12)))
                            .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                                .addGap(215, 215, 215)
                                .addComponent(plot1Btn))))
                    .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(radioPlot2))
                    .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(radioPlot1))
                    .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(radioPlot3))
                    .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                        .addGap(239, 239, 239)
                        .addGroup(AnalyzeCovariatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                                .addComponent(jLabel22)
                                .addGap(18, 18, 18)
                                .addComponent(output2Txt1, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton21))
                            .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                                .addComponent(jLabel21)
                                .addGap(18, 18, 18)
                                .addComponent(input2Txt1, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton22))
                            .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                                .addComponent(jLabel23)
                                .addGap(18, 18, 18)
                                .addComponent(input2Txt2, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton23))))
                    .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                        .addGap(454, 454, 454)
                        .addComponent(plot2Btn))
                    .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                        .addGap(237, 237, 237)
                        .addGroup(AnalyzeCovariatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(AnalyzeCovariatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                                    .addComponent(jLabel26)
                                    .addGap(18, 18, 18)
                                    .addComponent(output2Txt2, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(jButton26))
                                .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                                    .addComponent(jLabel24)
                                    .addGap(18, 18, 18)
                                    .addComponent(input2Txt4, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(jButton24))
                                .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                                    .addComponent(jLabel25)
                                    .addGap(18, 18, 18)
                                    .addComponent(input2Txt3, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(jButton25))
                                .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                                    .addComponent(jLabel27)
                                    .addGap(18, 18, 18)
                                    .addComponent(input2Txt5, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(jButton27)))
                            .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                                .addGap(215, 215, 215)
                                .addComponent(plot3Btn)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        AnalyzeCovariatesPanelLayout.setVerticalGroup(
            AnalyzeCovariatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AnalyzeCovariatesPanelLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel15)
                .addGap(1, 1, 1)
                .addComponent(radioPlot1)
                .addGap(22, 22, 22)
                .addGroup(AnalyzeCovariatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(input2Txt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton12))
                .addGap(18, 18, 18)
                .addGroup(AnalyzeCovariatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(output2Txt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton13))
                .addGap(62, 62, 62)
                .addComponent(plot1Btn)
                .addGap(31, 31, 31)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(radioPlot2)
                .addGap(43, 43, 43)
                .addGroup(AnalyzeCovariatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(input2Txt1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton22))
                .addGap(18, 18, 18)
                .addGroup(AnalyzeCovariatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(input2Txt2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton23))
                .addGap(26, 26, 26)
                .addGroup(AnalyzeCovariatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(output2Txt1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton21))
                .addGap(40, 40, 40)
                .addComponent(plot2Btn)
                .addGap(18, 18, 18)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(radioPlot3)
                .addGap(18, 18, 18)
                .addGroup(AnalyzeCovariatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(input2Txt4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton24))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addGroup(AnalyzeCovariatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(input2Txt5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton27))
                .addGap(18, 18, 18)
                .addGroup(AnalyzeCovariatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(input2Txt3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton25))
                .addGap(26, 26, 26)
                .addGroup(AnalyzeCovariatesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(output2Txt2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton26))
                .addGap(40, 40, 40)
                .addComponent(plot3Btn)
                .addGap(20, 20, 20))
        );

        HaplotypeCallerPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "HaplotypeCaller: Call germline SNPs and indels via local re-assembly of haplotypes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 24), new java.awt.Color(255, 255, 255))); // NOI18N
        HaplotypeCallerPanel.setOpaque(false);
        HaplotypeCallerPanel.setPreferredSize(new java.awt.Dimension(1200, 500));

        jLabel28.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(255, 255, 255));
        jLabel28.setText("Reference File:");

        jButton28.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButton28.setText("Browse");
        jButton28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton28ActionPerformed(evt);
            }
        });

        plot1Btn1.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        plot1Btn1.setText("Analyze");
        plot1Btn1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plot1Btn1ActionPerformed(evt);
            }
        });

        jLabel30.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel30.setForeground(new java.awt.Color(255, 255, 255));
        jLabel30.setText("HELP?");
        jLabel30.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel30MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel30MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel30MouseExited(evt);
            }
        });

        jLabel38.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel38.setForeground(new java.awt.Color(255, 255, 255));
        jLabel38.setText("INPUT FILE(s):");

        jButton37.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButton37.setText("Browse");
        jButton37.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton37ActionPerformed(evt);
            }
        });

        jLabel39.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel39.setForeground(new java.awt.Color(255, 255, 255));
        jLabel39.setText("OUTPUT DIRECTORY:");

        jButton38.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButton38.setText("Browse");
        jButton38.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton38ActionPerformed(evt);
            }
        });

        inputBamTxt1.setBorder(javax.swing.BorderFactory.createTitledBorder("Upload all bam(s) files"));
        inputBamTxt1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        inputBamTxt1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                inputBamTxt1KeyPressed(evt);
            }
        });
        jScrollPane5.setViewportView(inputBamTxt1);

        jLabel19.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(255, 255, 255));
        jLabel19.setText("Annotation Groups (-G):");

        jointModeH.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jointModeH.setForeground(new java.awt.Color(255, 255, 255));
        jointModeH.setText("Joint Genotyping Mode");
        jointModeH.setToolTipText("Combines multiple GVCFs using GenotypeGVCFs for accurate variant calling across all samples.");

        annotationGroupCombo.setEditable(true);

        annotationGroupTxt.setEditable(false);
        annotationGroupTxt.setColumns(20);
        annotationGroupTxt.setLineWrap(true);
        annotationGroupTxt.setRows(5);
        jScrollPane11.setViewportView(annotationGroupTxt);

        add7.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        add7.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/plusSs.png"))); // NOI18N
        add7.setEnabled(false);
        add7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add7ActionPerformed(evt);
            }
        });

        jLabel48.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel48.setForeground(new java.awt.Color(255, 255, 255));
        jLabel48.setText("Select Emit Ref Confidence (-ERC):");

        del7.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        del7.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/removeSs.png"))); // NOI18N
        del7.setEnabled(false);
        del7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del7ActionPerformed(evt);
            }
        });

        bamoutTxt.setEditable(false);

        jLabel49.setFont(new java.awt.Font("Liberation Sans", 3, 15)); // NOI18N
        jLabel49.setForeground(new java.awt.Color(255, 255, 255));
        jLabel49.setText("GATK Annotation Group Reference");
        jLabel49.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel49MouseClicked(evt);
            }
        });

        bamoutB.setText("...");
        bamoutB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bamoutBActionPerformed(evt);
            }
        });

        jLabel50.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel50.setForeground(new java.awt.Color(255, 255, 255));
        jLabel50.setText("Selected Annotation Groups:");

        ercCombo.setEditable(true);
        ercCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "GVCF", "BP_RESOLUTION", "NONE" }));

        jLabel51.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel51.setForeground(new java.awt.Color(255, 255, 255));
        jLabel51.setText("Please type the name of an annotation group into the text field and click the (+) button to include it in the selected Annotation Group below. or remove it using (-)");

        g1Check.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        g1Check.setForeground(new java.awt.Color(255, 255, 255));
        g1Check.setText("Add Annotation Groups (-G):");
        g1Check.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                g1CheckItemStateChanged(evt);
            }
        });

        jLabel52.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel52.setForeground(new java.awt.Color(255, 255, 255));
        jLabel52.setText("If you want to remove an annotation group from the list, type the name of annotation group into the text field and click (-) button.");

        bamoutCheck.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        bamoutCheck.setForeground(new java.awt.Color(255, 255, 255));
        bamoutCheck.setText("-bamout");
        bamoutCheck.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                bamoutCheckItemStateChanged(evt);
            }
        });

        jLabel53.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel53.setForeground(new java.awt.Color(255, 255, 255));
        jLabel53.setText("If you are unsure of the valid annotation groups (e.g., Standard, AS_Standard, etc.), please click GATK Annotation Group Reference");

        singleModeH.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        singleModeH.setForeground(new java.awt.Color(255, 255, 255));
        singleModeH.setSelected(true);
        singleModeH.setText("Single Sample Mode");
        singleModeH.setToolTipText(" Calls variants for each sample independently.");

        javax.swing.GroupLayout HaplotypeCallerPanelLayout = new javax.swing.GroupLayout(HaplotypeCallerPanel);
        HaplotypeCallerPanel.setLayout(HaplotypeCallerPanelLayout);
        HaplotypeCallerPanelLayout.setHorizontalGroup(
            HaplotypeCallerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, HaplotypeCallerPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel30)
                .addGap(54, 54, 54))
            .addGroup(HaplotypeCallerPanelLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(HaplotypeCallerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(HaplotypeCallerPanelLayout.createSequentialGroup()
                        .addGroup(HaplotypeCallerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel19)
                            .addComponent(jLabel51)
                            .addComponent(jLabel52)
                            .addComponent(jLabel53)
                            .addGroup(HaplotypeCallerPanelLayout.createSequentialGroup()
                                .addComponent(g1Check)
                                .addGap(207, 207, 207)
                                .addComponent(jLabel49))
                            .addComponent(jLabel50)
                            .addComponent(singleModeH)
                            .addComponent(jointModeH)
                            .addComponent(jLabel48)
                            .addComponent(ercCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(HaplotypeCallerPanelLayout.createSequentialGroup()
                                .addComponent(bamoutTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bamoutB))
                            .addComponent(bamoutCheck)
                            .addGroup(HaplotypeCallerPanelLayout.createSequentialGroup()
                                .addGroup(HaplotypeCallerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jScrollPane11, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
                                    .addGroup(HaplotypeCallerPanelLayout.createSequentialGroup()
                                        .addComponent(annotationGroupCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(add7, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(del7, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(329, 329, 329)
                                .addComponent(plot1Btn1, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(270, 270, 270)))
                        .addContainerGap())
                    .addGroup(HaplotypeCallerPanelLayout.createSequentialGroup()
                        .addGroup(HaplotypeCallerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel28)
                            .addComponent(jLabel38)
                            .addComponent(jLabel39))
                        .addGap(18, 18, 18)
                        .addGroup(HaplotypeCallerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(input2Txt6)
                            .addComponent(jScrollPane5)
                            .addComponent(outputTxt13))
                        .addGap(18, 18, 18)
                        .addGroup(HaplotypeCallerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton38)
                            .addComponent(jButton28)
                            .addComponent(jButton37))
                        .addGap(231, 231, 231))))
        );
        HaplotypeCallerPanelLayout.setVerticalGroup(
            HaplotypeCallerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(HaplotypeCallerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel30)
                .addGap(27, 27, 27)
                .addGroup(HaplotypeCallerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(input2Txt6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton28))
                .addGap(22, 22, 22)
                .addGroup(HaplotypeCallerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(HaplotypeCallerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel38)
                        .addComponent(jButton37))
                    .addGroup(HaplotypeCallerPanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(19, 19, 19)
                        .addGroup(HaplotypeCallerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton38)
                            .addComponent(outputTxt13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel39))))
                .addGap(34, 34, 34)
                .addComponent(singleModeH)
                .addGap(18, 18, 18)
                .addComponent(jointModeH)
                .addGap(31, 31, 31)
                .addComponent(jLabel48)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ercCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel51)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel52)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel53)
                .addGap(18, 18, 18)
                .addGroup(HaplotypeCallerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(g1Check)
                    .addComponent(jLabel49))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(HaplotypeCallerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(add7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(del7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(annotationGroupCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addComponent(jLabel50)
                .addGroup(HaplotypeCallerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(HaplotypeCallerPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(HaplotypeCallerPanelLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(plot1Btn1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bamoutCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(HaplotypeCallerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bamoutTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bamoutB))
                .addGap(58, 58, 58))
        );

        VariantFiltrationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "VariantFiltration: Filter variant calls based on INFO and/or FORMAT annotations", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 24), new java.awt.Color(255, 255, 255))); // NOI18N
        VariantFiltrationPanel.setOpaque(false);
        VariantFiltrationPanel.setPreferredSize(new java.awt.Dimension(1200, 500));

        jLabel33.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel33.setForeground(new java.awt.Color(255, 255, 255));
        jLabel33.setText("Reference File:");

        jButton29.setText("Browse");
        jButton29.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton29ActionPerformed(evt);
            }
        });

        plot1Btn2.setText("Analyze");
        plot1Btn2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plot1Btn2ActionPerformed(evt);
            }
        });

        jLabel35.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(255, 255, 255));
        jLabel35.setText("HELP?");
        jLabel35.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel35MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel35MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel35MouseExited(evt);
            }
        });

        jLabel40.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(255, 255, 255));
        jLabel40.setText("INPUT FILE:");

        jButton39.setText("Browse");
        jButton39.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton39ActionPerformed(evt);
            }
        });

        jLabel41.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel41.setForeground(new java.awt.Color(255, 255, 255));
        jLabel41.setText("OUTPUT:");

        jButton40.setText("Browse");
        jButton40.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton40ActionPerformed(evt);
            }
        });

        add5.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        add5.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/plusSs.png"))); // NOI18N
        add5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add5ActionPerformed(evt);
            }
        });

        del5.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        del5.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/removeSs.png"))); // NOI18N
        del5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del5ActionPerformed(evt);
            }
        });

        variantFilterTxt.setEditable(false);
        variantFilterTxt.setColumns(20);
        variantFilterTxt.setLineWrap(true);
        variantFilterTxt.setRows(5);
        jScrollPane10.setViewportView(variantFilterTxt);

        jLabel29.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel29.setForeground(new java.awt.Color(255, 255, 255));
        jLabel29.setText("--filter-expression");
        jLabel29.setToolTipText("One or more expressions used with INFO fields to filter");

        jLabel54.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel54.setForeground(new java.awt.Color(255, 255, 255));
        jLabel54.setText("Build filter string for VariantFilteration by adding(+) or removing(-) below arguments");

        jLabel55.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel55.setForeground(new java.awt.Color(255, 255, 255));
        jLabel55.setText("Filter String:");
        jLabel55.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel55MouseClicked(evt);
            }
        });

        jLabel31.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel31.setForeground(new java.awt.Color(255, 255, 255));
        jLabel31.setText("--filter-name");

        javax.swing.GroupLayout VariantFiltrationPanelLayout = new javax.swing.GroupLayout(VariantFiltrationPanel);
        VariantFiltrationPanel.setLayout(VariantFiltrationPanelLayout);
        VariantFiltrationPanelLayout.setHorizontalGroup(
            VariantFiltrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, VariantFiltrationPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel35)
                .addGap(54, 54, 54))
            .addGroup(VariantFiltrationPanelLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(VariantFiltrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(VariantFiltrationPanelLayout.createSequentialGroup()
                        .addGroup(VariantFiltrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(VariantFiltrationPanelLayout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addGroup(VariantFiltrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 937, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(VariantFiltrationPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel31)
                                        .addGap(18, 18, 18)
                                        .addComponent(filterName, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(155, 155, 155)
                                        .addComponent(jLabel29)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(filterExpr, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(add5, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(del5, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel55)))
                            .addComponent(jLabel54))
                        .addContainerGap(216, Short.MAX_VALUE))
                    .addGroup(VariantFiltrationPanelLayout.createSequentialGroup()
                        .addGroup(VariantFiltrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(VariantFiltrationPanelLayout.createSequentialGroup()
                                .addComponent(jLabel33)
                                .addGap(18, 18, 18)
                                .addComponent(input2Txt8))
                            .addGroup(VariantFiltrationPanelLayout.createSequentialGroup()
                                .addGroup(VariantFiltrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel40)
                                    .addComponent(jLabel41))
                                .addGap(47, 47, 47)
                                .addGroup(VariantFiltrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(input2Txt13)
                                    .addComponent(outputTxt14))))
                        .addGap(18, 18, 18)
                        .addGroup(VariantFiltrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(VariantFiltrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jButton29)
                                .addComponent(jButton39))
                            .addComponent(jButton40))
                        .addGap(235, 235, 235))))
            .addGroup(VariantFiltrationPanelLayout.createSequentialGroup()
                .addGap(395, 395, 395)
                .addComponent(plot1Btn2, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        VariantFiltrationPanelLayout.setVerticalGroup(
            VariantFiltrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(VariantFiltrationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel35)
                .addGap(27, 27, 27)
                .addGroup(VariantFiltrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(VariantFiltrationPanelLayout.createSequentialGroup()
                        .addGroup(VariantFiltrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel33)
                            .addComponent(input2Txt8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton29))
                        .addGroup(VariantFiltrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(VariantFiltrationPanelLayout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(jButton39))
                            .addGroup(VariantFiltrationPanelLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(VariantFiltrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel40)
                                    .addComponent(input2Txt13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel41))
                    .addGroup(VariantFiltrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(outputTxt14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton40)))
                .addGap(33, 33, 33)
                .addComponent(jLabel54)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(VariantFiltrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(VariantFiltrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(filterExpr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel29))
                    .addComponent(add5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(VariantFiltrationPanelLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(VariantFiltrationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel31)
                            .addComponent(filterName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(del5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel55)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(plot1Btn2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(61, 61, 61))
        );

        SelectVariantsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "SelectVariants: Select a subset of variants from a VCF file", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 24), new java.awt.Color(255, 255, 255))); // NOI18N
        SelectVariantsPanel.setOpaque(false);
        SelectVariantsPanel.setPreferredSize(new java.awt.Dimension(1200, 500));

        jLabel36.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel36.setForeground(new java.awt.Color(255, 255, 255));
        jLabel36.setText("Reference File:");

        jButton30.setText("Browse");
        jButton30.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton30ActionPerformed(evt);
            }
        });

        plot1Btn3.setText("Analyze");
        plot1Btn3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plot1Btn3ActionPerformed(evt);
            }
        });

        jLabel37.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel37.setForeground(new java.awt.Color(255, 255, 255));
        jLabel37.setText("HELP?");
        jLabel37.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel37MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel37MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel37MouseExited(evt);
            }
        });

        jLabel56.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel56.setForeground(new java.awt.Color(255, 255, 255));
        jLabel56.setText("INPUT FILE:");

        jButton41.setText("Browse");
        jButton41.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton41ActionPerformed(evt);
            }
        });

        jLabel57.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel57.setForeground(new java.awt.Color(255, 255, 255));
        jLabel57.setText("OUTPUT:");

        jButton42.setText("Browse");
        jButton42.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton42ActionPerformed(evt);
            }
        });

        del3.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        del3.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/removeSs.png"))); // NOI18N
        del3.setEnabled(false);
        del3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del3ActionPerformed(evt);
            }
        });

        add4.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        add4.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/plusSs.png"))); // NOI18N
        add4.setEnabled(false);
        add4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add4ActionPerformed(evt);
            }
        });

        del4.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        del4.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/removeSs.png"))); // NOI18N
        del4.setEnabled(false);
        del4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del4ActionPerformed(evt);
            }
        });

        add2.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        add2.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/plusSs.png"))); // NOI18N
        add2.setEnabled(false);
        add2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add2ActionPerformed(evt);
            }
        });

        del2.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        del2.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/removeSs.png"))); // NOI18N
        del2.setEnabled(false);
        del2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del2ActionPerformed(evt);
            }
        });

        selectVariantTxt.setEditable(false);
        selectVariantTxt.setColumns(20);
        selectVariantTxt.setLineWrap(true);
        selectVariantTxt.setRows(5);
        jScrollPane8.setViewportView(selectVariantTxt);

        jLabel61.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        jLabel61.setForeground(new java.awt.Color(255, 255, 255));
        jLabel61.setText("SelectVariants Options:");

        intervalBtn.setText("...");
        intervalBtn.setToolTipText("To create an interval list:\n1- Type an interval (e.g., chr1:1000-2000) or browse for a file (...)\n2- Click  add (+) to include it in the list or del (-) to remove it from the list.");
        intervalBtn.setEnabled(false);
        intervalBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                intervalBtnActionPerformed(evt);
            }
        });

        jLabel62.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel62.setForeground(new java.awt.Color(255, 255, 255));
        jLabel62.setText("Build filter string for SelectVariants by adding(+) or removing(-) below arguments");

        jLabel63.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel63.setForeground(new java.awt.Color(255, 255, 255));
        jLabel63.setText("Filter String:");

        select.setEditable(false);

        selectGenotype.setEditable(false);

        Linterval.setEditable(false);

        jCheckBox5.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jCheckBox5.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBox5.setSelected(true);
        jCheckBox5.setText("Variant Type (--select-type-to-include)");
        jCheckBox5.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox5ItemStateChanged(evt);
            }
        });

        jCheckBox6.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jCheckBox6.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBox6.setText("Selection Expression (-select)");
        jCheckBox6.setToolTipText("A filtering expression in terms of either INFO fields or the VariantContext object. If the expression evaluates to true for a variant, it will be kept in the output vcf.");
        jCheckBox6.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox6ItemStateChanged(evt);
            }
        });

        jCheckBox7.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jCheckBox7.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBox7.setText("Genotype Condition (-select-genotype)");
        jCheckBox7.setToolTipText("A filtering expression in terms of FORMAT fields. If the expression evaluates to true for a variant, it will be kept in the output vcf.");
        jCheckBox7.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox7ItemStateChanged(evt);
            }
        });

        jCheckBox8.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jCheckBox8.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBox8.setText("Genomic Interval (-L)");
        jCheckBox8.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBox8ItemStateChanged(evt);
            }
        });

        selectTypeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NO_VARIATION", "SNP", "MNP", "INDEL", "SYMBOLIC", "MIXED" }));
        selectTypeCombo.setSelectedIndex(1);

        del1.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        del1.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/removeSs.png"))); // NOI18N
        del1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del1ActionPerformed(evt);
            }
        });

        add1.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        add1.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/plusSs.png"))); // NOI18N
        add1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add1ActionPerformed(evt);
            }
        });

        add3.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        add3.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/plusSs.png"))); // NOI18N
        add3.setEnabled(false);
        add3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout SelectVariantsPanelLayout = new javax.swing.GroupLayout(SelectVariantsPanel);
        SelectVariantsPanel.setLayout(SelectVariantsPanelLayout);
        SelectVariantsPanelLayout.setHorizontalGroup(
            SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SelectVariantsPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel37)
                .addGap(54, 54, 54))
            .addGroup(SelectVariantsPanelLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SelectVariantsPanelLayout.createSequentialGroup()
                        .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel61)
                            .addComponent(jLabel62)
                            .addGroup(SelectVariantsPanelLayout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(SelectVariantsPanelLayout.createSequentialGroup()
                                        .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jCheckBox5)
                                            .addComponent(jCheckBox6)
                                            .addComponent(jCheckBox7)
                                            .addComponent(jCheckBox8))
                                        .addGap(12, 12, 12)
                                        .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                            .addComponent(selectTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(select, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(selectGenotype, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(Linterval, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(SelectVariantsPanelLayout.createSequentialGroup()
                                                .addComponent(add1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(del1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(SelectVariantsPanelLayout.createSequentialGroup()
                                                .addComponent(add2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(del2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(SelectVariantsPanelLayout.createSequentialGroup()
                                                .addComponent(add3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(del3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addGroup(SelectVariantsPanelLayout.createSequentialGroup()
                                                .addComponent(intervalBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(add4, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(del4, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 937, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel63))))
                        .addContainerGap(216, Short.MAX_VALUE))
                    .addGroup(SelectVariantsPanelLayout.createSequentialGroup()
                        .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(SelectVariantsPanelLayout.createSequentialGroup()
                                .addComponent(jLabel36)
                                .addGap(18, 18, 18)
                                .addComponent(input2Txt9))
                            .addGroup(SelectVariantsPanelLayout.createSequentialGroup()
                                .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel56)
                                    .addComponent(jLabel57))
                                .addGap(47, 47, 47)
                                .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(input2Txt14)
                                    .addComponent(outputTxt15))))
                        .addGap(18, 18, 18)
                        .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jButton30)
                                .addComponent(jButton41))
                            .addComponent(jButton42))
                        .addGap(235, 235, 235))))
            .addGroup(SelectVariantsPanelLayout.createSequentialGroup()
                .addGap(433, 433, 433)
                .addComponent(plot1Btn3, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        SelectVariantsPanelLayout.setVerticalGroup(
            SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SelectVariantsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel37)
                .addGap(27, 27, 27)
                .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(SelectVariantsPanelLayout.createSequentialGroup()
                        .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel36)
                            .addComponent(input2Txt9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton30))
                        .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(SelectVariantsPanelLayout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(jButton41))
                            .addGroup(SelectVariantsPanelLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel56)
                                    .addComponent(input2Txt14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel57))
                    .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(outputTxt15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton42)))
                .addGap(32, 32, 32)
                .addComponent(jLabel61)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel62)
                .addGap(15, 15, 15)
                .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox5)
                    .addComponent(add1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(del1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(SelectVariantsPanelLayout.createSequentialGroup()
                        .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(SelectVariantsPanelLayout.createSequentialGroup()
                                .addGap(4, 4, 4)
                                .addComponent(jCheckBox6))
                            .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(del2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(select, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(add2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jCheckBox7)
                                .addComponent(selectGenotype, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(add3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(del3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jCheckBox8)
                        .addGroup(SelectVariantsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(Linterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(intervalBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(add4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(del4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel63)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(43, 43, 43)
                .addComponent(plot1Btn3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        IndexFeatureFilePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "IndexFeatureFile: Creates an index for a feature file, e.g. VCF or BED file.", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N
        IndexFeatureFilePanel.setOpaque(false);
        IndexFeatureFilePanel.setPreferredSize(new java.awt.Dimension(1200, 500));

        jLabel12.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText("INPUT VCF or BED File:");

        jButton14.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButton14.setText("Browse");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        jButton17.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jButton17.setText("Perform Analysis");
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton17ActionPerformed(evt);
            }
        });

        jLabel18.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(255, 255, 255));
        jLabel18.setText("HELP?");
        jLabel18.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel18MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel18MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel18MouseExited(evt);
            }
        });

        javax.swing.GroupLayout IndexFeatureFilePanelLayout = new javax.swing.GroupLayout(IndexFeatureFilePanel);
        IndexFeatureFilePanel.setLayout(IndexFeatureFilePanelLayout);
        IndexFeatureFilePanelLayout.setHorizontalGroup(
            IndexFeatureFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, IndexFeatureFilePanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel18)
                .addGap(100, 100, 100))
            .addGroup(IndexFeatureFilePanelLayout.createSequentialGroup()
                .addGroup(IndexFeatureFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(IndexFeatureFilePanelLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel12)
                        .addGap(18, 18, 18)
                        .addComponent(inputTxt1, javax.swing.GroupLayout.PREFERRED_SIZE, 680, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton14))
                    .addGroup(IndexFeatureFilePanelLayout.createSequentialGroup()
                        .addGap(391, 391, 391)
                        .addComponent(jButton17)))
                .addContainerGap(217, Short.MAX_VALUE))
        );
        IndexFeatureFilePanelLayout.setVerticalGroup(
            IndexFeatureFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(IndexFeatureFilePanelLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(IndexFeatureFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(inputTxt1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton14))
                .addGap(57, 57, 57)
                .addComponent(jButton17)
                .addContainerGap(318, Short.MAX_VALUE))
        );

        mutect2Panel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Mutect2 Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255))); // NOI18N
        mutect2Panel.setOpaque(false);

        jLabel47.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(255, 255, 255));
        jLabel47.setText("Set Output Directory:");

        jLabel92.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel92.setForeground(new java.awt.Color(255, 255, 255));
        jLabel92.setText("Input BAM File:");

        jLabel95.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel95.setForeground(new java.awt.Color(255, 255, 255));
        jLabel95.setText("Reference Genome(fasta):");

        refTxt1.setEditable(false);

        output_Dir.setEditable(false);

        inputBamTxt.setBorder(javax.swing.BorderFactory.createTitledBorder("Upload all bam(s) files"));
        inputBamTxt.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        inputBamTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                inputBamTxtKeyPressed(evt);
            }
        });
        jScrollPane4.setViewportView(inputBamTxt);

        outputBtn.setText("...");
        outputBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputBtnActionPerformed(evt);
            }
        });

        inputbamBtn.setText("...");
        inputbamBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputbamBtnActionPerformed(evt);
            }
        });

        refBtn.setText("...");
        refBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refBtnActionPerformed(evt);
            }
        });

        jButton6.setText("Perform Analysis");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel16.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("ExampleData");
        jLabel16.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel16MouseClicked(evt);
            }
        });

        jLabel32.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(255, 255, 255));
        jLabel32.setText("Select any option:");

        mutectCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "SELECT", "Tumor with matched normal (Single)", "Tumor with matched normal (Multiple)", "Tumor-only mode", "Mitochondrial mode", "Force-calling mode" }));
        mutectCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                mutectComboItemStateChanged(evt);
            }
        });

        normalPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Tumor-Normal Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255))); // NOI18N
        normalPanel.setOpaque(false);

        jLabel58.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel58.setForeground(new java.awt.Color(255, 255, 255));
        jLabel58.setText("Normal Sample(s):");

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

        jLabel59.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel59.setForeground(new java.awt.Color(255, 255, 255));
        jLabel59.setText("Tumor Sample(s):");

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
                        .addComponent(jLabel59)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 443, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(normalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(normalPanelLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jLabel58))
                            .addGroup(normalPanelLayout.createSequentialGroup()
                                .addGap(33, 33, 33)
                                .addGroup(normalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(norTumBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                    .addComponent(tumNorBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(28, 28, 28)
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
                .addContainerGap(16, Short.MAX_VALUE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addComponent(Label6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(normalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, normalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(normalPanelLayout.createSequentialGroup()
                            .addGroup(normalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel58)
                                .addGroup(normalPanelLayout.createSequentialGroup()
                                    .addGap(9, 9, 9)
                                    .addComponent(jLabel59)))
                            .addGap(22, 22, 22)
                            .addComponent(tumNorBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(43, 43, 43)
                            .addComponent(norTumBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(25, 25, 25))))
                .addGap(22, 22, 22))
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
        jScrollPane15.setViewportView(intervalText);

        intervalT.setEditable(false);

        add9.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        add9.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/plusSs.png"))); // NOI18N
        add9.setEnabled(false);
        add9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                add9ActionPerformed(evt);
            }
        });

        del9.setFont(new java.awt.Font("Liberation Sans", 1, 18)); // NOI18N
        del9.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/removeSs.png"))); // NOI18N
        del9.setEnabled(false);
        del9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                del9ActionPerformed(evt);
            }
        });

        jLabel70.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel70.setForeground(new java.awt.Color(255, 255, 255));
        jLabel70.setText("To create an interval list:");

        jLabel71.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel71.setForeground(new java.awt.Color(255, 255, 255));
        jLabel71.setText("2- Click  add (+) to include it in the interval list or del (-) to remove it from the list.");

        jLabel72.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel72.setForeground(new java.awt.Color(255, 255, 255));
        jLabel72.setText("1- Type an interval (e.g., chr1:1000-2000) or browse for a file (...)");

        ponLabel8.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        ponLabel8.setForeground(new java.awt.Color(255, 255, 255));
        ponLabel8.setText("The generated PoN file will be used automatically in the pipeline.");

        generatePON.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        generatePON.setForeground(new java.awt.Color(255, 255, 255));
        generatePON.setText("Generate PON");
        generatePON.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generatePONActionPerformed(evt);
            }
        });

        ponLabel9.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        ponLabel9.setForeground(new java.awt.Color(255, 255, 255));
        ponLabel9.setText("OR");

        ponLabel10.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        ponLabel10.setForeground(new java.awt.Color(255, 255, 255));
        ponLabel10.setText("Check this 'Generate PoN' t box to genrate one from your selected normal samples:");

        jLabel73.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel73.setForeground(new java.awt.Color(255, 255, 255));
        jLabel73.setText("Interval list:");

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
                        .addComponent(add9, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(del9, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(ponLabel8)
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
                        .addComponent(ponLabel9))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(ponLabel10))
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
                        .addComponent(jLabel70))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel72, javax.swing.GroupLayout.PREFERRED_SIZE, 487, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel71))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, 554, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(otherPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel73))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(ponLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ponLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(otherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ponLabel8)
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
                .addComponent(jLabel70)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel72)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel71)
                .addGap(23, 23, 23)
                .addGroup(otherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(otherPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(intervalT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(intervalBtn))
                    .addComponent(del9, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(add9, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel73)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50))
        );

        javax.swing.GroupLayout mutect2PanelLayout = new javax.swing.GroupLayout(mutect2Panel);
        mutect2Panel.setLayout(mutect2PanelLayout);
        mutect2PanelLayout.setHorizontalGroup(
            mutect2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mutect2PanelLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(mutect2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(otherPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(mutect2PanelLayout.createSequentialGroup()
                        .addGroup(mutect2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(normalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(mutect2PanelLayout.createSequentialGroup()
                                .addGroup(mutect2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(mutect2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(mutect2PanelLayout.createSequentialGroup()
                                            .addComponent(jLabel92, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(83, 83, 83)
                                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 721, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(18, 18, 18)
                                            .addComponent(inputbamBtn))
                                        .addGroup(mutect2PanelLayout.createSequentialGroup()
                                            .addGroup(mutect2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel47)
                                                .addComponent(jLabel32))
                                            .addGap(87, 87, 87)
                                            .addGroup(mutect2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(mutectCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 559, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGroup(mutect2PanelLayout.createSequentialGroup()
                                                    .addComponent(output_Dir, javax.swing.GroupLayout.PREFERRED_SIZE, 721, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(outputBtn)))))
                                    .addGroup(mutect2PanelLayout.createSequentialGroup()
                                        .addComponent(jLabel95)
                                        .addGap(51, 51, 51)
                                        .addComponent(refTxt1, javax.swing.GroupLayout.PREFERRED_SIZE, 721, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(refBtn)
                                        .addGap(4, 4, 4)))
                                .addGap(59, 59, 59)
                                .addComponent(jLabel16)))
                        .addGap(0, 14, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mutect2PanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(480, 480, 480))
        );
        mutect2PanelLayout.setVerticalGroup(
            mutect2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mutect2PanelLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(mutect2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel32)
                    .addComponent(mutectCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mutect2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16)
                    .addGroup(mutect2PanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(mutect2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mutect2PanelLayout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(jLabel47))
                            .addComponent(output_Dir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(outputBtn))
                        .addGap(12, 12, 12)
                        .addGroup(mutect2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel92)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(inputbamBtn))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(mutect2PanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mutect2PanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel95))
                    .addComponent(refTxt1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(refBtn))
                .addGap(26, 26, 26)
                .addComponent(normalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(otherPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );

        warningLabel.setFont(new java.awt.Font("Liberation Sans", 3, 18)); // NOI18N
        warningLabel.setForeground(new java.awt.Color(255, 51, 0));

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addComponent(BaseRecalibratorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(HaplotypeCallerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(VariantFiltrationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(671, Short.MAX_VALUE))
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                    .addGap(91, 91, 91)
                    .addComponent(ApplyBQSRPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(639, Short.MAX_VALUE)))
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                    .addGap(101, 101, 101)
                    .addComponent(AnalyzeCovariatesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(742, Short.MAX_VALUE)))
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                    .addGap(53, 53, 53)
                    .addComponent(IndexFeatureFilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(677, Short.MAX_VALUE)))
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                    .addGap(32, 32, 32)
                    .addComponent(mutect2Panel, javax.swing.GroupLayout.PREFERRED_SIZE, 1310, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(588, Short.MAX_VALUE)))
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                    .addContainerGap(36, Short.MAX_VALUE)
                    .addComponent(warningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 1265, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(629, Short.MAX_VALUE)))
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(SelectVariantsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(913, Short.MAX_VALUE)))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(87, 87, 87)
                .addComponent(BaseRecalibratorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(HaplotypeCallerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(55, 55, 55)
                .addComponent(VariantFiltrationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                    .addGap(124, 124, 124)
                    .addComponent(ApplyBQSRPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(1030, Short.MAX_VALUE)))
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                    .addGap(134, 134, 134)
                    .addComponent(AnalyzeCovariatesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(624, Short.MAX_VALUE)))
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                    .addGap(97, 97, 97)
                    .addComponent(IndexFeatureFilePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(1057, Short.MAX_VALUE)))
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                    .addGap(56, 56, 56)
                    .addComponent(mutect2Panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(633, Short.MAX_VALUE)))
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                    .addGap(17, 17, 17)
                    .addComponent(warningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(1603, Short.MAX_VALUE)))
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(SelectVariantsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(1181, Short.MAX_VALUE)))
        );

        splitPane.setRightComponent(mainPanel);

        jTree.setFont(new java.awt.Font("Noto Sans CJK SC", 1, 14)); // NOI18N
        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("GATK");
        javax.swing.tree.DefaultMutableTreeNode treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("BaseRecalibrator");
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("ApplyBQSR");
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("AnalyzeCovariates");
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("HaplotypeCaller");
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Mutect2");
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("VariantFiltration");
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("IndexFeatureFile");
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("SelectVariants");
        treeNode1.add(treeNode2);
        jTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTreeValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jTree);

        splitPane.setLeftComponent(jScrollPane2);

        jScrollPane1.setViewportView(splitPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1243, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("BAM Files (*.bam)", "bam"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("SAM Files (*.sam)", "sam"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("CRAM Files (*.cram)", "cram"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFile = fileChooser.getSelectedFile();

                String path = allFile.getAbsolutePath();

                inputTxt.setText(path);

            }
            outputTxt.setText(FilenameUtils.getFullPath(allFile.getAbsolutePath()) + FilenameUtils.removeExtension(allFile.getName()).concat(".table"));
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("FASTA Files (*.fa)", "fa"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("FASTA Files (*.fasta)", "fasta"));

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFile = fileChooser.getSelectedFile();

                String path = allFile.getAbsolutePath();

                refTxt.setText(path);

            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed

        outputTxt.setText(App.outputFile(new FileNameExtensionFilter("Recalibration Table (*.table)", "table"), ".table"));


    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("VCF Files (*.vcf)", "vcf"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("VCF Files (*.vcf.gz)", "gz"));

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFile = fileChooser.getSelectedFile();

                String path = allFile.getAbsolutePath();

                knownsitesTxt.setText(path);

            }
        }

    }//GEN-LAST:event_jButton3ActionPerformed

    private void jLabel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseClicked

        App.helpWebpage("https://gatk.broadinstitute.org/hc/en-us/articles/30332011267355-BaseRecalibrator");

    }//GEN-LAST:event_jLabel5MouseClicked

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        if (toolName.equals("BaseRecalibrator")) {
            App.bar.setIndeterminate(true);
            App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
            SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
                private int status;

                @Override
                protected Integer doInBackground() {
                    Process p;
                    try {
                        List<String> cmdList = new ArrayList<String>();
                        // adding command and args to the list
                        cmdList.add("sh");
                        cmdList.add(System.getProperty("user.dir").concat("/bin/GATK.sh"));
                        cmdList.add(App.GATK_PATH);
                        cmdList.add(toolName);

                        cmdList.add(inputTxt.getText());
                        cmdList.add(refTxt.getText());
                        cmdList.add(knownsitesTxt.getText());
                        cmdList.add(knownsitesTxt1.getText());
                        cmdList.add(outputTxt.getText());

                        publish("\r\nCommand Executed: " + cmdList);

                        ProcessBuilder pb = new ProcessBuilder(cmdList);
                        pb.redirectErrorStream(true);
                        p = pb.start();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(
                                p.getInputStream()));
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
                        App.LOGGER.error("\r\n ERROR in BaseRecalibrator analysis: " + App.stack);
                        App.textArea.append("\r\n ERROR in BaseRecalibrator analysis: " + App.stack);

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
        }//end-if
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Recalibration Table BQSR (*.table)", "table"));

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFile = fileChooser.getSelectedFile();

                String path = allFile.getAbsolutePath();

                bqsrTxt.setText(path);

            }
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed

        output1Txt.setText(App.outputFile(new FileNameExtensionFilter("BAM Files (*.bam)", "bam"), ".bam"));


    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("FASTA Files (*.fa)", "fa"));

        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("FASTA Files (*.fasta)", "fasta"));

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFile = fileChooser.getSelectedFile();

                String path = allFile.getAbsolutePath();

                ref1Txt.setText(path);

            }
        }
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        if (toolName.equals("ApplyBQSR")) {
            App.bar.setIndeterminate(true);
            App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
            SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
                private int status;

                @Override
                protected Integer doInBackground() {

                    Process p;
                    try {
                        List<String> cmdList = new ArrayList<String>();
                        // adding command and args to the list
                        cmdList.add("sh");
                        cmdList.add(System.getProperty("user.dir").concat("/bin/GATK.sh"));
                        cmdList.add(App.GATK_PATH);//$1 
                        cmdList.add(toolName);

                        cmdList.add(ref1Txt.getText());
                        cmdList.add(input1Txt.getText());
                        cmdList.add(bqsrTxt.getText());
                        cmdList.add(output1Txt.getText());

                        publish("\r\nCommand Executed: " + cmdList);
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
                        App.LOGGER.error("\r\n ERROR in ApplyBQSR analysis: " + App.stack);
                        App.textArea.append("\r\n ERROR in ApplyBQSR analysis: " + App.stack);

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
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jLabel10MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10MouseClicked
        App.helpWebpage("https://gatk.broadinstitute.org/hc/en-us/articles/30331973217435-ApplyBQSR");

    }//GEN-LAST:event_jLabel10MouseClicked

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Recalibration Table (*.table)", "table"));

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFile = fileChooser.getSelectedFile();

                String path = allFile.getAbsolutePath();

                input2Txt.setText(path);

            }
        }
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        output2Txt.setText(App.outputFile(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"), ".pdf"));

    }//GEN-LAST:event_jButton13ActionPerformed

    private void plot1BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plot1BtnActionPerformed
        if (toolName.equals("AnalyzeCovariates")) {
            App.bar.setIndeterminate(true);
            App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
            SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
                private int status;

                @Override
                protected Integer doInBackground() {

                    Process p;
                    try {
                        List<String> cmdList = new ArrayList<String>();
                        // adding command and args to the list
                        cmdList.add("sh");
                        cmdList.add(System.getProperty("user.dir").concat("/bin/GATK.sh"));
                        cmdList.add(App.GATK_PATH);
                        cmdList.add(toolName); //$1

                        cmdList.add(input2Txt.getText());
                        cmdList.add(output2Txt.getText());
                        publish("\r\nCommand Executed: " + cmdList);

                        ProcessBuilder pb = new ProcessBuilder(cmdList);
                        pb.redirectErrorStream(true);
                        p = pb.start();

                        p.waitFor();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(
                                p.getInputStream()));
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
                        App.LOGGER.error("\r\n ERROR in AnalyzeCovariates analysis: " + App.stack);
                        App.textArea.append("\r\n ERROR in AnalyzeCovariates analysis: " + App.stack);

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
    }//GEN-LAST:event_plot1BtnActionPerformed

    private void jLabel15MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MouseClicked
        App.helpWebpage("https://gatk.broadinstitute.org/hc/en-us/articles/5358816130587-AnalyzeCovariates");

    }//GEN-LAST:event_jLabel15MouseClicked

    private void jTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTreeValueChanged
        String node = evt.getNewLeadSelectionPath().getLastPathComponent().toString();
        toolName = node;
        if (node.equals("BaseRecalibrator")) {

            AnalyzeCovariatesPanel.setVisible(false);

            ApplyBQSRPanel.setVisible(false);
            HaplotypeCallerPanel.setVisible(false);
            BaseRecalibratorPanel.setVisible(true);
            VariantFiltrationPanel.setVisible(false);
            mutect2Panel.setVisible(false);

        } else if (node.equals("ApplyBQSR")) {

            AnalyzeCovariatesPanel.setVisible(false);

            ApplyBQSRPanel.setVisible(true);
            BaseRecalibratorPanel.setVisible(false);
            HaplotypeCallerPanel.setVisible(false);
            VariantFiltrationPanel.setVisible(false);
            mutect2Panel.setVisible(false);

        } else if (node.equals("AnalyzeCovariates")) {

            AnalyzeCovariatesPanel.setVisible(true);
            HaplotypeCallerPanel.setVisible(false);
            ApplyBQSRPanel.setVisible(false);
            BaseRecalibratorPanel.setVisible(false);
            VariantFiltrationPanel.setVisible(false);
            mutect2Panel.setVisible(false);

        } else if (node.equals("HaplotypeCaller")) {

            AnalyzeCovariatesPanel.setVisible(false);

            ApplyBQSRPanel.setVisible(false);
            BaseRecalibratorPanel.setVisible(false);
            HaplotypeCallerPanel.setVisible(true);
            VariantFiltrationPanel.setVisible(false);
            mutect2Panel.setVisible(false);
        } else if (node.equals("VariantFiltration")) {

            AnalyzeCovariatesPanel.setVisible(false);

            ApplyBQSRPanel.setVisible(false);
            BaseRecalibratorPanel.setVisible(false);
            HaplotypeCallerPanel.setVisible(false);
            VariantFiltrationPanel.setVisible(true);
            mutect2Panel.setVisible(false);
        } else if (node.equals("Mutect2")) {
            AnalyzeCovariatesPanel.setVisible(false);

            ApplyBQSRPanel.setVisible(false);
            BaseRecalibratorPanel.setVisible(false);
            HaplotypeCallerPanel.setVisible(false);
            VariantFiltrationPanel.setVisible(false);
            mutect2Panel.setVisible(true);

        } else {
            AnalyzeCovariatesPanel.setVisible(false);

            ApplyBQSRPanel.setVisible(false);
            BaseRecalibratorPanel.setVisible(false);
            HaplotypeCallerPanel.setVisible(false);
            VariantFiltrationPanel.setVisible(false);
            mutect2Panel.setVisible(false);
        }


    }//GEN-LAST:event_jTreeValueChanged

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("BAM Files (*.bam)", "bam"));

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFile = fileChooser.getSelectedFile();

                String path = allFile.getAbsolutePath();

                input1Txt.setText(path);

            }
        }
    }//GEN-LAST:event_jButton11ActionPerformed

    private void plot2BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plot2BtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_plot2BtnActionPerformed

    private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
        output2Txt1.setText(App.outputFile(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"), ".pdf"));

    }//GEN-LAST:event_jButton21ActionPerformed

    private void jButton22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton22ActionPerformed
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Recalibration Table (*.table)", "table"));

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFile = fileChooser.getSelectedFile();

                String path = allFile.getAbsolutePath();

                input2Txt1.setText(path);

            }
        }
    }//GEN-LAST:event_jButton22ActionPerformed

    private void jButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton23ActionPerformed
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Recalibration Table (*.table)", "table"));

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFile = fileChooser.getSelectedFile();

                String path = allFile.getAbsolutePath();

                input2Txt2.setText(path);

            }
        }
    }//GEN-LAST:event_jButton23ActionPerformed

    private void plot3BtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plot3BtnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_plot3BtnActionPerformed

    private void jButton24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton24ActionPerformed
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Recalibration Table (*.table)", "table"));

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFile = fileChooser.getSelectedFile();

                String path = allFile.getAbsolutePath();

                input2Txt4.setText(path);

            }
        }
    }//GEN-LAST:event_jButton24ActionPerformed

    private void jButton25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton25ActionPerformed
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Recalibration Table (*.table)", "table"));

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFile = fileChooser.getSelectedFile();

                String path = allFile.getAbsolutePath();

                input2Txt3.setText(path);

            }
        }
    }//GEN-LAST:event_jButton25ActionPerformed

    private void jButton26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton26ActionPerformed
        output2Txt2.setText(App.outputFile(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"), ".pdf"));

    }//GEN-LAST:event_jButton26ActionPerformed

    private void jButton27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton27ActionPerformed
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Recalibration Table (*.table)", "table"));

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFile = fileChooser.getSelectedFile();

                String path = allFile.getAbsolutePath();

                input2Txt5.setText(path);

            }
        }
    }//GEN-LAST:event_jButton27ActionPerformed

    private void radioPlot1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioPlot1ActionPerformed
        input2Txt.setEnabled(true);
        output2Txt.setEnabled(true);
        jButton12.setEnabled(true);
        jButton13.setEnabled(true);
        plot1Btn.setEnabled(true);

        input2Txt1.setEnabled(false);
        input2Txt2.setEnabled(false);
        output2Txt1.setEnabled(false);
        jButton22.setEnabled(false);
        jButton23.setEnabled(false);
        jButton21.setEnabled(false);
        plot2Btn.setEnabled(false);

        input2Txt4.setEnabled(false);
        input2Txt5.setEnabled(false);
        input2Txt3.setEnabled(false);
        output2Txt2.setEnabled(false);
        jButton24.setEnabled(false);
        jButton27.setEnabled(false);
        jButton25.setEnabled(false);
        jButton26.setEnabled(false);
        plot3Btn.setEnabled(false);
    }//GEN-LAST:event_radioPlot1ActionPerformed

    private void radioPlot2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioPlot2ActionPerformed
        input2Txt.setEnabled(false);
        output2Txt.setEnabled(false);
        jButton12.setEnabled(false);
        jButton13.setEnabled(false);
        plot1Btn.setEnabled(false);

        input2Txt1.setEnabled(true);
        input2Txt2.setEnabled(true);
        output2Txt1.setEnabled(true);
        jButton22.setEnabled(true);
        jButton23.setEnabled(true);
        jButton21.setEnabled(true);
        plot2Btn.setEnabled(true);

        input2Txt4.setEnabled(false);
        input2Txt5.setEnabled(false);
        input2Txt3.setEnabled(false);
        output2Txt2.setEnabled(false);
        jButton24.setEnabled(false);
        jButton27.setEnabled(false);
        jButton25.setEnabled(false);
        jButton26.setEnabled(false);
        plot3Btn.setEnabled(false);
    }//GEN-LAST:event_radioPlot2ActionPerformed

    private void radioPlot3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioPlot3ActionPerformed
        input2Txt.setEnabled(false);
        output2Txt.setEnabled(false);
        jButton12.setEnabled(false);
        jButton13.setEnabled(false);
        plot1Btn.setEnabled(false);

        input2Txt1.setEnabled(false);
        input2Txt2.setEnabled(false);
        output2Txt1.setEnabled(false);
        jButton22.setEnabled(false);
        jButton23.setEnabled(false);
        jButton21.setEnabled(false);
        plot2Btn.setEnabled(false);

        input2Txt4.setEnabled(true);
        input2Txt5.setEnabled(true);
        input2Txt3.setEnabled(true);
        output2Txt2.setEnabled(true);
        jButton24.setEnabled(true);
        jButton27.setEnabled(true);
        jButton25.setEnabled(true);
        jButton26.setEnabled(true);
        plot3Btn.setEnabled(true);
    }//GEN-LAST:event_radioPlot3ActionPerformed

    private void jButton28ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton28ActionPerformed
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("FASTA Files (*.fasta)", "fasta"));

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFile = fileChooser.getSelectedFile();

                String path = allFile.getAbsolutePath();

                input2Txt6.setText(path);

            }
        }
    }//GEN-LAST:event_jButton28ActionPerformed

    private void plot1Btn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plot1Btn1ActionPerformed
        if (toolName.equals("HaplotypeCaller")) {

            App.bar.setIndeterminate(true);
            App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
            SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
                private int status;

                @Override
                protected Integer doInBackground() {

                    Process p;
                    try {
                        List<String> cmdList = new ArrayList<String>();
                        // adding command and args to the list
                        cmdList.add("sh");
                        cmdList.add(System.getProperty("user.dir").concat("/bin/GATK.sh"));
                        cmdList.add(App.GATK_PATH);//1
                        cmdList.add(toolName); //$2

                        cmdList.add(input2Txt6.getText()); //ref

                        String bamfiles = "";
                        for (int i = 0; i < inputBamTxt1.getModel().getSize(); i++) {

                            bamfiles += (inputBamTxt1.getModel().getElementAt(i) + ",").trim();
                        }
                        bamfiles = StringUtils.chop(bamfiles).replaceAll("\"", "");

                        cmdList.add(bamfiles);//4 Bam file name

                        cmdList.add(outputTxt13.getText()); //5
                        if (singleModeH.isSelected()) {
                            cmdList.add("SINGLEMODE");//6 
                        } else if (jointModeH.isSelected()) {
                            cmdList.add("JOINTMODE");//6 
                        }
                        cmdList.add(ercCombo.getSelectedItem().toString()); //7 -ERC

                        if (g1Check.isSelected()) {
                            cmdList.add(annotationGroupTxt.getText()); //8 -G
                        }
                        if (bamoutCheck.isSelected()) {
                            cmdList.add(bamoutTxt.getText()); //9 -bamout
                        }

                        publish("\r\nCommand Executed: " + cmdList);

                        ProcessBuilder pb = new ProcessBuilder(cmdList);
                        pb.redirectErrorStream(true);
                        p = pb.start();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(
                                p.getInputStream()));
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
                        App.LOGGER.error("\r\n ERROR in HaplotypeCaller analysis: " + App.stack);
                        App.textArea.append("\r\n ERROR in HaplotypeCaller analysis: " + App.stack);

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
    }//GEN-LAST:event_plot1Btn1ActionPerformed

    private void jLabel30MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel30MouseClicked
        App.helpWebpage("https://gatk.broadinstitute.org/hc/en-us/articles/30332006386459-HaplotypeCaller");

    }//GEN-LAST:event_jLabel30MouseClicked

    private void jButton37ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton37ActionPerformed
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
                    listModelHaplo.addElement(" \"" + allFiles[i].getAbsolutePath() + "\"");
                }

            }
        }

    }//GEN-LAST:event_jButton37ActionPerformed

    private void jButton38ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton38ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new java.io.File("."));
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {

            String path = fileChooser.getSelectedFile().toString();

            outputTxt13.setText(path + "/");

        }
    }//GEN-LAST:event_jButton38ActionPerformed

    private void jButton29ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton29ActionPerformed
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("FASTA Files (*.fa)", "fa"));

        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("FASTA Files (*.fasta)", "fasta"));

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFile = fileChooser.getSelectedFile();

                String path = allFile.getAbsolutePath();

                input2Txt8.setText(path);

            }
        }
    }//GEN-LAST:event_jButton29ActionPerformed

    private void plot1Btn2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plot1Btn2ActionPerformed
        if (toolName.equals("VariantFiltration")) {
            App.bar.setIndeterminate(true);
            App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
            SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
                private int status;

                @Override
                protected Integer doInBackground() {

                    Process p;
                    try {
                        List<String> cmdList = new ArrayList<String>();
                        // adding command and args to the list
                        cmdList.add("sh");
                        cmdList.add(System.getProperty("user.dir").concat("/bin/GATK.sh"));
                        cmdList.add(App.GATK_PATH);
                        cmdList.add(toolName); //$2

                        cmdList.add(input2Txt8.getText());
                        cmdList.add(input2Txt13.getText());
                        cmdList.add(outputTxt14.getText());
                        cmdList.add( variantFilterTxt.getText().trim());
                      

                        publish("\r\nCommand Executed: " + cmdList);

                        ProcessBuilder pb = new ProcessBuilder(cmdList);
                        pb.redirectErrorStream(true);
                        p = pb.start();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(
                                p.getInputStream()));
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
                        App.LOGGER.error("\r\n ERROR in VariantFiltration analysis: " + App.stack);
                        App.textArea.append("\r\n ERROR in VariantFiltration analysis: " + App.stack);

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
    }//GEN-LAST:event_plot1Btn2ActionPerformed

    private void jLabel35MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel35MouseClicked
        App.helpWebpage("https://gatk.broadinstitute.org/hc/en-us/articles/30332042223387-VariantFiltration");

    }//GEN-LAST:event_jLabel35MouseClicked

    private void jButton39ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton39ActionPerformed
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("VCF Files (*.vcf.gz)", "gz"));

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFile = fileChooser.getSelectedFile();

                String path = allFile.getAbsolutePath();

                input2Txt13.setText(path);

            }
        }
    }//GEN-LAST:event_jButton39ActionPerformed

    private void jButton40ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton40ActionPerformed

        outputTxt14.setText(App.outputFile(new FileNameExtensionFilter("VCF Files (*.vcf.gz)", "gz"), ".vcf.gz"));

    }//GEN-LAST:event_jButton40ActionPerformed

    private void jLabel5MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseEntered
        jLabel5.setIcon(App.icons[1]);
    }//GEN-LAST:event_jLabel5MouseEntered

    private void jLabel5MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseExited
        jLabel5.setIcon(App.icons[0]);
    }//GEN-LAST:event_jLabel5MouseExited

    private void jLabel10MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10MouseEntered
        jLabel10.setIcon(App.icons[1]);
    }//GEN-LAST:event_jLabel10MouseEntered

    private void jLabel10MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel10MouseExited
        jLabel10.setIcon(App.icons[0]);
    }//GEN-LAST:event_jLabel10MouseExited

    private void jLabel15MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MouseEntered
        jLabel15.setIcon(App.icons[1]);
    }//GEN-LAST:event_jLabel15MouseEntered

    private void jLabel15MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel15MouseExited
        jLabel15.setIcon(App.icons[0]);
    }//GEN-LAST:event_jLabel15MouseExited

    private void jLabel30MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel30MouseEntered
        jLabel30.setIcon(App.icons[1]);
    }//GEN-LAST:event_jLabel30MouseEntered

    private void jLabel30MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel30MouseExited
        jLabel30.setIcon(App.icons[0]);
    }//GEN-LAST:event_jLabel30MouseExited

    private void jLabel35MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel35MouseEntered
        jLabel35.setIcon(App.icons[1]);
    }//GEN-LAST:event_jLabel35MouseEntered

    private void jLabel35MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel35MouseExited
        jLabel35.setIcon(App.icons[0]);
    }//GEN-LAST:event_jLabel35MouseExited

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("BED Files (*.bed)", "bed"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("VCF Files (*.vcf)", "vcf"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("VCF.GZ Files (*.gz)", "gz"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFile = fileChooser.getSelectedFile();

                String path = allFile.getAbsolutePath();

                inputTxt1.setText(path);

            }
        }
    }//GEN-LAST:event_jButton14ActionPerformed

    private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
        if (toolName.equals("IndexFeatureFile")) {
            App.bar.setIndeterminate(true);
            App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
            SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
                private int status;

                @Override
                protected Integer doInBackground() {
                    Process p;
                    try {
                        List<String> cmdList = new ArrayList<String>();
                        // adding command and args to the list
                        cmdList.add("sh");
                        cmdList.add(System.getProperty("user.dir").concat("/bin/GATK.sh"));
                        cmdList.add(App.GATK_PATH);
                        cmdList.add(toolName);

                        cmdList.add(inputTxt1.getText());

                        publish("\r\nCommand Executed: " + cmdList);

                        ProcessBuilder pb = new ProcessBuilder(cmdList);
                        pb.redirectErrorStream(true);
                        p = pb.start();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(
                                p.getInputStream()));
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
                        App.LOGGER.error("\r\n ERROR in IndexFeatureFile analysis: " + App.stack);
                        App.textArea.append("\r\n ERROR in IndexFeatureFile analysis: " + App.stack);

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
        }//end-if

    }//GEN-LAST:event_jButton17ActionPerformed

    private void jLabel18MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel18MouseClicked
        App.helpWebpage("https://gatk.broadinstitute.org/hc/en-us/articles/30332014273051-IndexFeatureFile");

    }//GEN-LAST:event_jLabel18MouseClicked

    private void jLabel18MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel18MouseEntered
        jLabel18.setIcon(App.icons[1]);        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel18MouseEntered

    private void jLabel18MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel18MouseExited
        jLabel18.setIcon(App.icons[0]);  // TODO add your handling code here:
    }//GEN-LAST:event_jLabel18MouseExited

    private void inputBamTxtKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_inputBamTxtKeyPressed
        if (evt.getKeyChar() == KeyEvent.VK_DELETE) {
            listModel.remove(inputBamTxt.getSelectedIndex());

        }
    }//GEN-LAST:event_inputBamTxtKeyPressed

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

    private void refBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refBtnActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Fasta Files (*.fa)", "fa"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Fasta Files (*.fasta)", "fasta"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {

                refTxt1.setText(fileChooser.getSelectedFile().toString());

            }
        }
    }//GEN-LAST:event_refBtnActionPerformed
public void fillTumorList() {
        listTumor.removeAllElements();
         
            //Reading just file name from the list
            for (int i = 0; i < inputBamTxt.getModel().getSize(); i++) {
                Path path = Paths.get(inputBamTxt.getModel().getElementAt(i));
                listTumor.addElement(StringUtils.chop(path.getFileName().toString().replaceAll("\\.(?![^.]+$)", "")));
            }
        }
    
    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        //Check Somatic Option Parameters
        warningLabel.setText(null);
     
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
            error = true;
            warningLabel.setText("**ERROR: Select any option from Somatic ");
        
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
            somaticOP = "TUMORO";

            Label1.setText("Execution Modes:");
            Label2.setText("Tumor-Only Mode: If all BAM files are in the Tumor Samples list, they will be processed independently to detect somatic variants with uploaded Panel of Normals (PoN).");
            Label3.setText("Tumor with PoN Mode: If you designate files in both Tumor and Normal lists:");
            Label4.setText("1-The normal samples will be processed first to create a Panel of Normals (PoN).");
            Label5.setText("2-This PoN will then be used to filter artifacts from the tumor samples, improving the accuracy of somatic variant detection.");

        } else if (mutectCombo.getSelectedIndex() == 4) //Mitochondrial mode
        {
            somaticOP = "TUMORMIT";

        } else if (mutectCombo.getSelectedIndex() == 5) //Force-calling mode
        {

            somaticOP = "TUMORFOR";

        }

      
             if (refTxt.getText().isBlank()) {
            warningLabel.setText("**ERROR: The reference genome file (FASTA) is required. Please provide a valid file path in the text field to proceed.");
            error= true;

        }
               if (mutectCombo.getSelectedIndex() == 0) {
                warningLabel.setText("**ERROR: Please select any option for Mutect2");
            error= true;
               } else if (mutectCombo.getSelectedIndex() == 1 || mutectCombo.getSelectedIndex() == 2) {

                if (jListTumor.getModel().getSize() != jListNormal.getModel().getSize()) {
                    warningLabel.setText("**ERROR: Number of Tumor samples & Normal samples must be equal for the selected option. Please check your input.");
                error= true;
                }

                if (germlineResTxt.getText().isBlank()) {
                    warningLabel.setText("**ERROR: Germline resource is mandatory for the selected option. Please provide a valid Germline resource file");
                     error= true;
                }

            } else if (mutectCombo.getSelectedIndex() == 3) {
                if (jListNormal.getModel().getSize() == 0) { //Tumor Only

                    if (germlineResTxt.getText().isBlank()) {
                        warningLabel.setText("**ERROR: Germline resource is mandatory for the selected option. Please provide a valid Germline resource file");
                         error= true;
                    }

                } else if ((jListTumor.getModel().getSize() != 0) && (jListTumor.getModel().getSize() == jListNormal.getModel().getSize())) {
                    //Tumor with PON mode
                    //Run normal 1st and then use PON 
                    if (germlineResTxt.getText().isBlank()) {
                        warningLabel.setText("**ERROR: Germline resource is mandatory for the selected option. Please provide a valid Germline resource file");

                         error= true;
                    }

                } else if (jListTumor.getModel().getSize() != jListNormal.getModel().getSize()) {
                    warningLabel.setText("**ERROR: Tumor-Normal Pairs are not equal");

                     error= true;
                }

            } else if (mutectCombo.getSelectedIndex() == 4) {

                if (intervalText.getText().isBlank()) {
                    warningLabel.setText("**ERROR: Genomic Interval(s) are mandatory for the selected option. Please upload or specify genomic interval data.");
                     error= true;
                }

            } else if (mutectCombo.getSelectedIndex() == 5) {

                if (alleleTxt.getText().isBlank() && f1r2.getText().isBlank()) {
                    warningLabel.setText("**ERROR: Force Calling requires at least one of the following inputs: Allele file or F1R2 file. Both cannot be empty.");
                     error= true;
                }

            }

         
         
        

        if (error == false) {

            warningLabel.setText(null);
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
                        cmdList.add(System.getProperty("user.dir").concat("/bin/GATK.sh"));
                        cmdList.add(App.GATK_PATH);//1
                        cmdList.add(toolName);//2
                        cmdList.add(refTxt1.getText());//3 Refernce File

                        //BAM FILES
                        String bamfiles = "";
                        for (int i = 0; i < inputBamTxt.getModel().getSize(); i++) {

                            bamfiles += (inputBamTxt.getModel().getElementAt(i) + ",").trim();
                        }
                        bamfiles = StringUtils.chop(bamfiles).replaceAll("\"", "");

                        cmdList.add(bamfiles);//4 Bam file name
                        cmdList.add(somaticOP);//5 somatic options

                        cmdList.add(output_Dir.getText()); //6
                        if (mutectCombo.getSelectedIndex() == 1 || mutectCombo.getSelectedIndex() == 2) //Tumor with matched normal (single)
                        {

                            //Tumor-NORMAL BAM FILES
                            String nbamfiles = "";
                            for (int i = 0; i < jListNormal.getModel().getSize(); i++) {

                                nbamfiles += (jListTumor.getModel().getElementAt(i) + "#" + jListNormal.getModel().getElementAt(i) + ",").trim();

                            }
                            nbamfiles = StringUtils.chop(nbamfiles);
                            cmdList.add(nbamfiles);//17 Tumor-Normal Bam file name  

                        } else if (mutectCombo.getSelectedIndex() == 3 || mutectCombo.getSelectedIndex() == 4 || mutectCombo.getSelectedIndex() == 5) //Tumor Only mode
                        {

                            //Tumor BAM FILES
                            String nbamfiles = "";
                            for (int i = 0; i < jListTumor.getModel().getSize(); i++) {

                                nbamfiles += (jListTumor.getModel().getElementAt(i) + ",").trim();

                            }
                            nbamfiles = StringUtils.chop(nbamfiles);
                            cmdList.add(nbamfiles);//17 Tumor Bam file name  

                        }
                        cmdList.add(germlineResTxt.getText());// 18 germline resource
                        cmdList.add(ponTxt.getText());// 19 PON
                        cmdList.add(alleleTxt.getText());//20 allele
                        cmdList.add(f1r2.getText());//21 f1
                        cmdList.add(intervalText.getText());//22

                        
                        //  System.out.println("VariantCommandString"+ cmdList.toString());
                        ProcessBuilder pb = new ProcessBuilder(cmdList);
                        pb.redirectErrorStream(true);
                        p = pb.start();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                        String line;
                        //System.out.println(reader.readLine());
                        while ((line = reader.readLine()) != null) {

                            App.LOGGER.info("\r\n" + line);
                            publish("\r\n" + line);
                            if ((line.endsWith("...... FATAL ERROR, exiting")) || (line.endsWith("Killed"))) {
                                //    error=true;
                                break;
                            }
                            if (line.contains("Exception") || line.contains("USER ERROR")) {
                                break;
                            }
                            if (line.contains("Killed") || line.contains("Aborted") || line.contains("ERROR")) {
                                // error = true;
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

                }//done

            };

            worker.execute();
        }//else


    }//GEN-LAST:event_jButton6ActionPerformed

    private void jLabel16MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel16MouseClicked
        refTxt1.setText("/home/iffy/TestFiles/MouseExample/INPUTS/mm9.fa");//3 Refernce File

        output_Dir.setText("/home/iffy/TestFiles/CheckMutect/"); //6

        germlineResTxt.setText("/home/iffy/TestFiles/MouseExample/INPUTS/vcf/sample1-bcbio-cancer.vcf");// 9 germline resource
        ponTxt.setText("/home/iffy/TestFiles/MouseExample/INPUTS/vcf/sample1-bcbio-cancer.vcf");// 10 PON

     //   genomicInterval.setText(germlineResTxt.getText());
    }//GEN-LAST:event_jLabel16MouseClicked

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("VCF Files (*.vcf)", "vcf"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("VCF Files (*.vcf.gz)", "gz"));

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFile = fileChooser.getSelectedFile();

                String path = allFile.getAbsolutePath();

                knownsitesTxt1.setText(path);

            }
        }
    }//GEN-LAST:event_jButton15ActionPerformed

    private void inputBamTxt1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_inputBamTxt1KeyPressed
        if (evt.getKeyChar() == KeyEvent.VK_DELETE) {
            listModelHaplo.remove(inputBamTxt1.getSelectedIndex());

        }
    }//GEN-LAST:event_inputBamTxt1KeyPressed

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

    private void jLabel49MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel49MouseClicked
        App.helpWebpage(" https://software.broadinstitute.org/gatk/documentation/article?id=10836");
    }//GEN-LAST:event_jLabel49MouseClicked

    private void bamoutBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bamoutBActionPerformed
        bamoutTxt.setText(App.outputFile(new FileNameExtensionFilter("BAM Files (*.bam)", "bam"), ".bam"));
    }//GEN-LAST:event_bamoutBActionPerformed

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

    private void bamoutCheckItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_bamoutCheckItemStateChanged
        bamoutTxt.setText("");        // TODO add your handling code here:
    }//GEN-LAST:event_bamoutCheckItemStateChanged

    private void add5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add5ActionPerformed
        String txtContent = variantFilterTxt.getText();
        if (!filterName.getText().isBlank() && !filterExpr.getText().isBlank()) {
            String txtAdd = "--filter-name " + filterName.getText().toLowerCase() + " " + "--filter-expression " + filterExpr.getText().toUpperCase() + " ";
            if (!txtContent.contains(txtAdd)) {
                variantFilterTxt.append(txtAdd);
            }
        }
    }//GEN-LAST:event_add5ActionPerformed

    private void del5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_del5ActionPerformed
        String txtContent = variantFilterTxt.getText();
        if (!filterName.getText().isBlank() && !filterExpr.getText().isBlank()) {
            String txtDel = "--filter-name " + filterName.getText().toLowerCase() + " " + "--filter-expression " + filterExpr.getText().toUpperCase() + " ";
            if (txtContent.contains(txtDel)) {
                txtContent = txtContent.replace(txtDel, "");
                variantFilterTxt.setText(txtContent);
            }
        }
    }//GEN-LAST:event_del5ActionPerformed

    private void jLabel55MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel55MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel55MouseClicked

    private void jButton30ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton30ActionPerformed
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("FASTA Files (*.fa)", "fa"));

        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("FASTA Files (*.fasta)", "fasta"));

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFile = fileChooser.getSelectedFile();

                String path = allFile.getAbsolutePath();

                input2Txt9.setText(path);

            }
        }
    }//GEN-LAST:event_jButton30ActionPerformed

    private void plot1Btn3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plot1Btn3ActionPerformed
        if (toolName.equals("SelectVariants")) {
            App.bar.setIndeterminate(true);
            App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
            SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
                private int status;

                @Override
                protected Integer doInBackground() {

                    Process p;
                    try {
                        List<String> cmdList = new ArrayList<String>();
                        // adding command and args to the list
                        cmdList.add("sh");
                        cmdList.add(System.getProperty("user.dir").concat("/bin/GATK.sh"));
                        cmdList.add(App.GATK_PATH);
                        cmdList.add(toolName); //$2

                        cmdList.add(input2Txt9.getText());
                        cmdList.add(input2Txt14.getText());
                        cmdList.add(outputTxt15.getText());
                        cmdList.add( selectVariantTxt.getText().trim());
                      

                        publish("\r\nCommand Executed: " + cmdList);

                        ProcessBuilder pb = new ProcessBuilder(cmdList);
                        pb.redirectErrorStream(true);
                        p = pb.start();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(
                                p.getInputStream()));
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
                        App.LOGGER.error("\r\n ERROR in VariantFiltration analysis: " + App.stack);
                        App.textArea.append("\r\n ERROR in VariantFiltration analysis: " + App.stack);

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
        
    }//GEN-LAST:event_plot1Btn3ActionPerformed

    private void jLabel37MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel37MouseClicked
        App.helpWebpage("https://gatk.broadinstitute.org/hc/en-us/articles/30332021749275-SelectVariants");
    }//GEN-LAST:event_jLabel37MouseClicked

    private void jLabel37MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel37MouseEntered
               jLabel35.setIcon(App.icons[1]);
    }//GEN-LAST:event_jLabel37MouseEntered

    private void jLabel37MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel37MouseExited
              jLabel35.setIcon(App.icons[0]);
    }//GEN-LAST:event_jLabel37MouseExited

    private void jButton41ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton41ActionPerformed
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("VCF Files (*.vcf.gz)", "gz"));

        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFile = fileChooser.getSelectedFile();

                String path = allFile.getAbsolutePath();

                input2Txt14.setText(path);

            }
        }
    }//GEN-LAST:event_jButton41ActionPerformed

    private void jButton42ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton42ActionPerformed

        outputTxt15.setText(App.outputFile(new FileNameExtensionFilter("VCF Files (*.vcf.gz)", "gz"), ".vcf.gz"));
    }//GEN-LAST:event_jButton42ActionPerformed

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

    private void del1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_del1ActionPerformed
        String txtContent = selectVariantTxt.getText();
        String txtDel = "--select-type-to-include " + selectTypeCombo.getSelectedItem().toString() + " ";
        if (txtContent.contains(txtDel)) {
            txtContent = txtContent.replace(txtDel, "");
            selectVariantTxt.setText(txtContent);
        }

    }//GEN-LAST:event_del1ActionPerformed

    private void add1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add1ActionPerformed

        String txtContent = selectVariantTxt.getText();
        String txtAdd = "--select-type-to-include " + selectTypeCombo.getSelectedItem().toString() + " ";
        if (!txtContent.contains(txtAdd)) {
            selectVariantTxt.append(txtAdd);
        }
    }//GEN-LAST:event_add1ActionPerformed

    private void add3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add3ActionPerformed
        String txtContent = selectVariantTxt.getText();
        if (!selectGenotype.getText().isBlank()) {
            String txtAdd = "-select-genotype " + selectGenotype.getText().toUpperCase() + " ";
            if (!txtContent.contains(txtAdd)) {
                selectVariantTxt.append(txtAdd);
            }
        }
    }//GEN-LAST:event_add3ActionPerformed

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
            somaticOP = "TUMORO";

            Label1.setText("Execution Modes:");
            Label2.setText("Tumor-Only Mode: If all BAM files are in the Tumor Samples list, they will be processed independently to detect somatic variants with uploaded Panel of Normals (PoN).");
            Label3.setText("Tumor with PoN Mode: If you designate files in both Tumor and Normal lists:");
            Label4.setText("1-The normal samples will be processed first to create a Panel of Normals (PoN).");
            Label5.setText("2-This PoN will then be used to filter artifacts from the tumor samples, improving the accuracy of somatic variant detection.");

        } else if (mutectCombo.getSelectedIndex() == 4) //Mitochondrial mode
        {
            somaticOP = "TUMORMIT";

        } else if (mutectCombo.getSelectedIndex() == 5) //Force-calling mode
        {

            somaticOP = "TUMORFOR";

        }

    }//GEN-LAST:event_mutectComboItemStateChanged

    private void tumNorBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tumNorBtnActionPerformed

        List<String> selectedItems = jListTumor.getSelectedValuesList();

        for (String item : selectedItems) {
            listNormal.addElement(item);
            listTumor.removeElement(item);
        }

    }//GEN-LAST:event_tumNorBtnActionPerformed

    private void norTumBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_norTumBtnActionPerformed

        List<String> selectedItems = jListNormal.getSelectedValuesList();

        for (String item : selectedItems) {
            listTumor.addElement(item);
            listNormal.removeElement(item);
        }
    }//GEN-LAST:event_norTumBtnActionPerformed

    private void jListNormalValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListNormalValueChanged
        if (!evt.getValueIsAdjusting()) {
            jListTumor.clearSelection();
        }
    }//GEN-LAST:event_jListNormalValueChanged

    private void jListTumorValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListTumorValueChanged
        if (!evt.getValueIsAdjusting()) {
            jListNormal.clearSelection();
        }
    }//GEN-LAST:event_jListTumorValueChanged

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

    private void add9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_add9ActionPerformed
        String txtContent = intervalText.getText();
        if (!intervalT.getText().isBlank()) {
            String txtAdd = "-L " + intervalT.getText().toLowerCase() + " ";
            if (!txtContent.contains(txtAdd)) {
                intervalText.append(txtAdd);
            }
        }
    }//GEN-LAST:event_add9ActionPerformed

    private void del9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_del9ActionPerformed
        String txtContent = intervalText.getText();
        if (!intervalT.getText().isBlank()) {
            String txtDel = "-L " + intervalT.getText().toLowerCase() + " ";
            if (!txtContent.contains(txtDel)) {
                txtContent = txtContent.replace(txtDel, "");
                intervalText.append(txtDel);
            }

        }
    }//GEN-LAST:event_del9ActionPerformed

    private void generatePONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generatePONActionPerformed
        if (generatePON.isSelected()) {
            ponTxt.setText("");
            ponBtn.setEnabled(false);
            ponCheck.setSelected(false);
        }
    }//GEN-LAST:event_generatePONActionPerformed

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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AnalyzeCovariatesPanel;
    private javax.swing.JPanel ApplyBQSRPanel;
    private javax.swing.JPanel BaseRecalibratorPanel;
    private javax.swing.JPanel HaplotypeCallerPanel;
    private javax.swing.JPanel IndexFeatureFilePanel;
    private javax.swing.JLabel Label1;
    private javax.swing.JLabel Label2;
    private javax.swing.JLabel Label3;
    private javax.swing.JLabel Label4;
    private javax.swing.JLabel Label5;
    private javax.swing.JLabel Label6;
    private javax.swing.JTextField Linterval;
    private javax.swing.JPanel SelectVariantsPanel;
    private javax.swing.JPanel VariantFiltrationPanel;
    private javax.swing.JButton add1;
    private javax.swing.JButton add2;
    private javax.swing.JButton add3;
    private javax.swing.JButton add4;
    private javax.swing.JButton add5;
    private javax.swing.JButton add6;
    private javax.swing.JButton add7;
    private javax.swing.JButton add8;
    private javax.swing.JButton add9;
    private javax.swing.JButton alleleBtn;
    private javax.swing.JTextField alleleTxt;
    private javax.swing.JCheckBox allelesCheck;
    private javax.swing.JComboBox<String> annotationGroupCombo;
    private javax.swing.JTextArea annotationGroupTxt;
    private javax.swing.JButton bamoutB;
    private javax.swing.JCheckBox bamoutCheck;
    private javax.swing.JTextField bamoutTxt;
    private javax.swing.JTextField bqsrTxt;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton del1;
    private javax.swing.JButton del2;
    private javax.swing.JButton del3;
    private javax.swing.JButton del4;
    private javax.swing.JButton del5;
    private javax.swing.JButton del6;
    private javax.swing.JButton del7;
    private javax.swing.JButton del8;
    private javax.swing.JButton del9;
    private javax.swing.JComboBox<String> ercCombo;
    private javax.swing.JTextField f1r2;
    private javax.swing.JButton f1r2Btn;
    private javax.swing.JCheckBox f1r2Check;
    private javax.swing.JTextField filterExpr;
    private javax.swing.JTextField filterName;
    private javax.swing.JCheckBox g1Check;
    private javax.swing.JCheckBox generatePON;
    private javax.swing.JCheckBox germlineCheck;
    private javax.swing.JButton germlineResBtn;
    private javax.swing.JTextField germlineResTxt;
    private javax.swing.JTextField input1Txt;
    private javax.swing.JTextField input2Txt;
    private javax.swing.JTextField input2Txt1;
    private javax.swing.JTextField input2Txt13;
    private javax.swing.JTextField input2Txt14;
    private javax.swing.JTextField input2Txt2;
    private javax.swing.JTextField input2Txt3;
    private javax.swing.JTextField input2Txt4;
    private javax.swing.JTextField input2Txt5;
    private javax.swing.JTextField input2Txt6;
    private javax.swing.JTextField input2Txt8;
    private javax.swing.JTextField input2Txt9;
    private javax.swing.JList<String> inputBamTxt;
    private javax.swing.JList<String> inputBamTxt1;
    private javax.swing.JTextField inputTxt;
    private javax.swing.JTextField inputTxt1;
    private javax.swing.JButton inputbamBtn;
    private javax.swing.JButton intervalBtn;
    private javax.swing.JCheckBox intervalCheck;
    private javax.swing.JTextField intervalT;
    private javax.swing.JTextArea intervalText;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton27;
    private javax.swing.JButton jButton28;
    private javax.swing.JButton jButton29;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton30;
    private javax.swing.JButton jButton37;
    private javax.swing.JButton jButton38;
    private javax.swing.JButton jButton39;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton40;
    private javax.swing.JButton jButton41;
    private javax.swing.JButton jButton42;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JCheckBox jCheckBox7;
    private javax.swing.JCheckBox jCheckBox8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
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
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
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
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel92;
    private javax.swing.JLabel jLabel95;
    private javax.swing.JList<String> jListNormal;
    private javax.swing.JList<String> jListTumor;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTree jTree;
    private javax.swing.JRadioButton jointModeH;
    private javax.swing.JTextField knownsitesTxt;
    private javax.swing.JTextField knownsitesTxt1;
    private javax.swing.JLabel mainLabelWarning;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel mutect2Panel;
    private javax.swing.JComboBox<String> mutectCombo;
    private javax.swing.JButton norTumBtn;
    private javax.swing.JPanel normalPanel;
    private javax.swing.JPanel otherPanel;
    private javax.swing.JTextField output1Txt;
    private javax.swing.JTextField output2Txt;
    private javax.swing.JTextField output2Txt1;
    private javax.swing.JTextField output2Txt2;
    private javax.swing.JButton outputBtn;
    private javax.swing.JTextField outputTxt;
    private javax.swing.JTextField outputTxt13;
    private javax.swing.JTextField outputTxt14;
    private javax.swing.JTextField outputTxt15;
    private javax.swing.JTextField output_Dir;
    private javax.swing.JButton plot1Btn;
    private javax.swing.JButton plot1Btn1;
    private javax.swing.JButton plot1Btn2;
    private javax.swing.JButton plot1Btn3;
    private javax.swing.JButton plot2Btn;
    private javax.swing.JButton plot3Btn;
    private javax.swing.JButton ponBtn;
    private javax.swing.JCheckBox ponCheck;
    private javax.swing.JLabel ponLabel10;
    private javax.swing.JLabel ponLabel2;
    private javax.swing.JLabel ponLabel3;
    private javax.swing.JLabel ponLabel4;
    private javax.swing.JLabel ponLabel5;
    private javax.swing.JLabel ponLabel6;
    private javax.swing.JLabel ponLabel7;
    private javax.swing.JLabel ponLabel8;
    private javax.swing.JLabel ponLabel9;
    private javax.swing.JTextField ponTxt;
    private javax.swing.JRadioButton radioPlot1;
    private javax.swing.JRadioButton radioPlot2;
    private javax.swing.JRadioButton radioPlot3;
    private javax.swing.JTextField ref1Txt;
    private javax.swing.JButton refBtn;
    private javax.swing.JTextField refTxt;
    private javax.swing.JTextField refTxt1;
    private javax.swing.JTextField select;
    private javax.swing.JTextField selectGenotype;
    private javax.swing.JComboBox<String> selectTypeCombo;
    private javax.swing.JTextArea selectVariantTxt;
    private javax.swing.JRadioButton singleModeH;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JButton tumNorBtn;
    private javax.swing.JTextArea variantFilterTxt;
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

}//END-CLASS
