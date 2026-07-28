/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JInternalFrame.java to edit this template
 */
package NGSGradle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashSet;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 *
 * @author iffy
 */
public class DEGenesAnalysis extends javax.swing.JInternalFrame {

    /*INPUT VARIABLES:*/
    private File[] allFiles;
    private DefaultTableModel sampleTableModel;
    private DefaultListModel deFactorListModel, model1, interactionListModel; //check this 
    private String analysisP;
    private boolean tableFlag = true, rcodeError = false;

    /**
     * Creates new form RNASeqAnalysisInternalFrame
     */
    public DEGenesAnalysis() {

        deFactorListModel = new DefaultListModel(); //ForDeseq
        model1 = new DefaultListModel(); //For edgeR
        interactionListModel = new DefaultListModel();
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
        // Set up lowercase cell editor for all cells
        setLowerCaseCellEditor(sampleTable);

        // Force focus on editor when double-clicked
        sampleTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Detect double-click
                    int row = sampleTable.rowAtPoint(e.getPoint());
                    int col = sampleTable.columnAtPoint(e.getPoint());
                    if (sampleTable.isCellEditable(row, col)) {
                        sampleTable.editCellAt(row, col);
                        Component editor = sampleTable.getEditorComponent();
                        if (editor != null) {
                            editor.requestFocus(); // Request focus for the editor
                        }
                    }
                }
            }
        });

        helpLabel.setIcon(App.icons[0]);

// BUTTON GROUPS
        inputOptionsGroup.add(countsFolder);
        inputOptionsGroup.add(countsFile);

        sampleTableRadios.add(sampleRadio1);
        sampleTableRadios.add(sampleRadio2);

        buttonGroup3.add(singleFactorRadio);//FOR DNA SEQ PANEL
        buttonGroup3.add(multiFactorRadio);

        buttonGroup4.add(jRadioContrast);
        buttonGroup4.add(jRadioList);

        expressionAnalysisBG.add(deseqRadio);
        analysisP = "deSeq2";
        expressionAnalysisBG.add(edgeRadio);
        expressionAnalysisBG.add(bothRadio);

////////////////////
//SETTING ALL PANELS
////////////////////
        inputPanel.setVisible(true);
        downPanel.setVisible(false);

        sampleDataPanel.setVisible(true);
        analysisModePanel.setVisible(false);
        resDEPanel.setVisible(false);//DESeq2 Matrix & Results
        complexDesignPanel.setVisible(false);
        interactionTermPanel.setVisible(false);
        edgeRDesignPanel.setVisible(false);
        resEDPanel.setVisible(false);// edgeR Results

        geneEnrichmentPanel.setVisible(false); //GSEA Panel

//// END SETTING ALL PANELS
        contrastFactorCombo.setEnabled(false);

        warningLabel.setText(null);

    }//Constructor
//##############################################################################

    public void exampleInputData() {
        output_Dir.setText("/home/iffy/TestFiles/FINALOUTPUTS/");
    }

    public void display(String title, String value) {

        System.out.println("===========================================");
        System.out.println("=============== " + title.toUpperCase() + " ===================");
        System.out.println("===========================================");
        System.out.println(value);
    }

// CHECKING METHODS
    public boolean checkInput() {
        if (output_Dir.getText().isBlank()) {
            warningLabel.setText("**ERROR: The output directory field is empty. Please provide a valid directory path where the output files will be stored.");
            return false;
        } else if (countsFolder.isSelected()) {
            if (countsFolderTxt.getText().isBlank()) {
                warningLabel.setText("**ERROR: The featurecounts directory field is empty. Please provide a valid directory path where the featurecounts files are present.");
                return false;
            }

        } else if (countsFile.isSelected()) {
            if (countsFileTxt.getText().isBlank()) {
                warningLabel.setText("**ERROR: No counts file has been uploaded. Please upload a valid counts file in CSV or TSV format.");
                return false;
            }

        }

        return true;//No error
    }

    public boolean checkDown() {

        //Check Sample Data Table
        if (!sampleRadio1.isSelected() && !sampleRadio2.isSelected()) {
            warningLabel.setText("**ERROR: Select sample table option");
            return false;

        } else if (sampleRadio1.isSelected()) {
            if (sampleFilePathTxt.getText().isBlank()) {
                warningLabel.setText("**ERROR: Upload a valid Sample csv or tsv file.");
                return false;
            }
        } else if (sampleTableModel.getColumnCount() < 2 || sampleTableModel.getRowCount() <= 1) {
            warningLabel.setText("**ERROR: There isn't enough data in the Sample Table");
            return false;

        } else if (!tableLockBtn.isSelected()) {
            warningLabel.setText("**ERROR: Lock the Sample Table 1st");
            return false;

        } else if (countThresh.getText().isBlank()) {
            warningLabel.setText("**ERROR: Provide the counts threshhold");
            return false;
        } //Check deseq results
        else if (singleFactorRadio.isSelected() == false && multiFactorRadio.isSelected() == false) {
            warningLabel.setText("**ERROR: Select any analysis mode");
            return false;

        } else if (multiFactorRadio.isSelected() == true) {

            if (interactionTermCombo.getSelectedIndex() == 1) {
                if (interactionList.isSelectionEmpty()) {
                    warningLabel.setText("**ERROR: Please select one or more interaction terms.");
                    return false;
                }
            }

        } else if (analysisP == "deSeq2") {

            if (jRadioContrast.isSelected()) {
                if (combination1.getSelectedIndex() == combination2.getSelectedIndex()) {
                    warningLabel.setText("**ERROR: Both factor levels should be different for contrast");
                    return false;
                }

            } else if (jRadioList.isSelected()) {

                if (!((deFactorList.getSelectedIndices().length > 0) && (deFactorList.getSelectedIndices().length <= 2))) {
                    warningLabel.setText("Select at least one or maximum two terms from the list");
                    return false;
                }
            }

        } else if (analysisP == "edgeR") {

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

        } //gsea
        else if (jTextField8.getText().isBlank()
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
    public void runRCOUNT(String rFilePath, String workDir, String rLib) {

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
                    cmdList.add(rLib); //2
                    if (countsFolder.isSelected()) {
                        cmdList.add("FOLDER");//3
                        cmdList.add(countsFolderTxt.getText());//4
                    } else if (countsFile.isSelected()) {
                        cmdList.add("FILE");//3
                        cmdList.add(countsFileTxt.getText());//4
                    }
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
                    //Saving Combined.tsv samplenames col in sampletable row
                    try ( BufferedReader br = new BufferedReader(new FileReader(output_Dir.getText().concat("CombinedCounts.tsv")))) {
                        // Read the first line (header)
                        String headerLine = br.readLine();
                        List<String> columnNames = new ArrayList<>();

                        if (headerLine != null) {
                            // Split the header line by tab (\t) character
                            String[] columns = headerLine.split("\t");

                            // Add all columns except the first one
                            for (int i = 1; i < columns.length; i++) {
                                columnNames.add(columns[i]);
                            }
                        }

                        for (String columnName : columnNames) {
                            // Add only the first column to the model
                            sampleTableModel.addRow(new Object[]{columnName});
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //
                }//if
            }//DONE

        };

        worker.execute();

    }

    public void runRDF(String rFilePath, String workDir, String rLib) {

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

//                    Map<String, List<String>> factorLevels;
//                    try {
//                        factorLevels = readFactorLevelsFromJson(output_Dir.getText() + "factor_levels.json");
//                        List<String> contrasts = buildContrastsFromLevels(factorLevels);
//
//                        deFactorListModel.clear();
//                        for (String contrast : contrasts) {
//
//                            deFactorListModel.addElement(contrast);
//                        }
//
//                    } catch (Exception ex) {
//                        rcodeError = true;
//                        ex.printStackTrace(new PrintWriter(App.stack));
//                        App.LOGGER.error("\r\n ERROR in R analysis: " + App.stack);
//                        App.textArea.append("\r\n ERROR in R analysis: " + App.stack);
//                    }
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

    }

    public Map<String, List<String>> readFactorLevelsFromJson(String jsonFilePath) throws Exception {
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

//                        if (line.startsWith("[1] \"Intercept,")) {
//                            line = line.replaceAll("\"", "");
//                            String[] parts = line.substring(3).split(",");
//                            for (String str : parts) {
//                                publish(str);
//                            }
//
//                        }//endif
                        if (line.toLowerCase().contains("error:") || line.toLowerCase().contains("error in")) {

                            rcodeError = true;
                            if (line.contains("Error in checkFullRank(modelMatrix) :")) {
                                publish("\r\n" + "To resolve this, please review the Cross Table of the Sample Data Frame and also avoid redundant interaction terms.\r\n Ensure there are no 0 values for any conditions.\r\n If you find any 0 values, adjust the sample data so that all conditions have non-zero counts, allowing DESeq2 to perform the analysis correctly.");
                            }

                            break;
                        }

                    }//while

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
                    cmdList.add(jTextField3.getText()); //alpha 3
                    cmdList.add(jTextField2.getText()); //padj 4
                    cmdList.add(jTextField1.getText()); //log2fold 5

                    if (jRadioContrast.isSelected()) {
                        cmdList.add(contrastFactorCombo.getSelectedItem().toString()); //selectedfactor 6
                        cmdList.add(combination1.getSelectedItem().toString()); //value1 7 
                        cmdList.add(combination2.getSelectedItem().toString()); //value2 8
                    } else if (jRadioList.isSelected()) {
                        cmdList.add("LIST"); //selectedfactor 6

                        cmdList.add(deFactorList.getSelectedValuesList().toString().replaceAll(" ", "")); //value1
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
                rcodeError = false;
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
                  //      cmdList.add(edgerFileTxt.getText());//25
                    } else if (analysis.equals("both")) {
                        cmdList.add("both");//24
                  //      cmdList.add(edgerFileTxt.getText());//25
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
                    if (deseqRadio.isSelected()) {
                        runShiny(System.getProperty("user.dir").concat("/bin/shinyGraphs.R"), output_Dir.getText().concat("DeSeqResults") + "," + output_Dir.getText().concat("DeSeqResults/GeneEnrich_Results"), System.getProperty("user.dir").concat("/R_Libraries"), "DESEQ2,GSEA");
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
        App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "In Progress: Preparing PDF... ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));

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

                    ProcessBuilder pb = new ProcessBuilder(cmdList);
                    pb.redirectErrorStream(true);
                    p = pb.start();

                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        App.LOGGER.info("\r\n" + line);
                        publish("\r\n" + line);
                    }//while

    //                Thread.sleep(5000); // 5 seconds

                    // Open browser to view the Shiny app
      //              URI uri = new URI("http://127.0.0.1:8080");
        //            Desktop.getDesktop().browse(uri);

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

            }

        };

        worker.execute();

    }

    // END TOOL METHODS ########################################################
// Method to set up a lowercase cell editor
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

    // Save table data to CSV file, overwriting if file already exists
    public void saveTableToTSV(String filePath) {
        try ( FileWriter csvWriter = new FileWriter(filePath, false)) { // 'false' ensures overwrite mode
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

    public void generateInteractionEffects(Map<String, Set<String>> factorLevels) {
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

    public void predictColumnNames() {

// Clear previous entries in model1
        model1.clear();

        TableModel model = sampleTable.getModel();
        Set<String> uniqueCombinations = new HashSet<>();

        if ((model.getColumnCount() == 2) || (singleFactorRadio.isSelected())) {
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

    // Recursive method to generate combinations of interaction terms
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
    // Add new columns from file headers if they don't already exist

    public void addNewColumns(String[] headers) {
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

    public boolean columnExists(String columnName) {
        for (int i = 0; i < sampleTableModel.getColumnCount(); i++) {
            if (sampleTableModel.getColumnName(i).equalsIgnoreCase(columnName)) {
                return true;
            }
        }
        return false;
    }

    // Utility method to clean SampleName (remove extensions and suffixes)
    public String cleanName(String name) {
        // Remove file extensions: .fq, .fq.gz, .fq.bz2, .fastq, .fastq.gz, .fastq.bz2
        name = name.replaceAll("\\.(fq|fq\\.gz|fq\\.bz2|fastq|fastq\\.gz|fastq\\.bz2)$", "");

        // Remove suffixes like _trim, _p, _P
        name = name.replaceAll("(_trim|_p|_P)$", "");
        return name;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
                                                                                                                                                                                                                                                      */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        jLabel63 = new javax.swing.JLabel();
        buttonGroup5 = new javax.swing.ButtonGroup();
        QCbuttonGroup = new javax.swing.ButtonGroup();
        expressionAnalysisBG = new javax.swing.ButtonGroup();
        sampleTableRadios = new javax.swing.ButtonGroup();
        inputOptionsGroup = new javax.swing.ButtonGroup();
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
        inputButton = new javax.swing.JButton();
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
        outputBtn = new javax.swing.JButton();
        jLabel98 = new javax.swing.JLabel();
        jLabel103 = new javax.swing.JLabel();
        jLabel104 = new javax.swing.JLabel();
        deseqRadio = new javax.swing.JRadioButton();
        edgeRadio = new javax.swing.JRadioButton();
        bothRadio = new javax.swing.JRadioButton();
        countsFolder = new javax.swing.JRadioButton();
        countsFile = new javax.swing.JRadioButton();
        countsFolderTxt = new javax.swing.JTextField();
        countsFolderBtn = new javax.swing.JButton();
        countsFileTxt = new javax.swing.JTextField();
        countsFileBtn = new javax.swing.JButton();
        jLabel99 = new javax.swing.JLabel();
        downPanel = new javax.swing.JPanel();
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
        jLabel4 = new javax.swing.JLabel();
        analysisModePanel = new javax.swing.JPanel()
        {

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgInput, 0, 0, null);
            }
        }

        ;
        jLabel16 = new javax.swing.JLabel();
        singleFactorRadio = new javax.swing.JRadioButton();
        multiFactorRadio = new javax.swing.JRadioButton();
        complexDesignPanel = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        interactionTermCombo = new javax.swing.JComboBox<>();
        interactionTermPanel = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        interactionList = new javax.swing.JList<>(interactionListModel);
        jLabel11 = new javax.swing.JLabel();
        countThresh = new javax.swing.JTextField();
        edgeRDesignPanel = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        interceptOption = new javax.swing.JComboBox<>();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        countThresh1 = new javax.swing.JTextField();
        filteringCombo = new javax.swing.JComboBox<>();
        jLabel40 = new javax.swing.JLabel();
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
        jTextField3 = new javax.swing.JTextField();
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
        jLabel89 = new javax.swing.JLabel();
        jLabel90 = new javax.swing.JLabel();
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
        selectInfoLabel = new javax.swing.JLabel();
        testInfoLabel = new javax.swing.JLabel();
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

        inputButton.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        inputButton.setIcon(new javax.swing.ImageIcon(System.getProperty("user.dir").concat("/images/next1.png"))); // NOI18N
        inputButton.setText("NEXT");
        inputButton.setBorderPainted(false);
        inputButton.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        inputButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputButtonActionPerformed(evt);
            }
        });

        warningLabel.setFont(new java.awt.Font("Liberation Sans", 3, 18)); // NOI18N
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
            .addGroup(upPanelLayout.createSequentialGroup()
                .addGroup(upPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(upPanelLayout.createSequentialGroup()
                        .addGap(353, 353, 353)
                        .addComponent(inputButton, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(warningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 1313, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 338, Short.MAX_VALUE)
                .addComponent(helpLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        upPanelLayout.setVerticalGroup(
            upPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(upPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(upPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(upPanelLayout.createSequentialGroup()
                        .addComponent(warningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(inputButton, javax.swing.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, upPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(helpLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        MainPanel.add(upPanel);

        inputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "INPUT OPTIONS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N
        inputPanel.setOpaque(false);

        jLabel32.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel32.setForeground(new java.awt.Color(255, 255, 255));
        jLabel32.setText("Set Output Directory:");

        output_Dir.setEditable(false);

        outputBtn.setText("...");
        outputBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputBtnActionPerformed(evt);
            }
        });

        jLabel98.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel98.setForeground(new java.awt.Color(255, 255, 255));
        jLabel98.setText("Enter all the neccesary input files. NOTE: Path or Filename should not contain any spaces");

        jLabel103.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel103.setForeground(new java.awt.Color(255, 255, 255));
        jLabel103.setText("Please choose one of the following options to proceed with the analysis:");

        jLabel104.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel104.setForeground(new java.awt.Color(255, 255, 255));
        jLabel104.setText("Choose the Differential Expression analysis method:");

        deseqRadio.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        deseqRadio.setForeground(new java.awt.Color(255, 255, 255));
        deseqRadio.setSelected(true);
        deseqRadio.setText("DESeq2");
        deseqRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deseqRadioActionPerformed(evt);
            }
        });

        edgeRadio.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        edgeRadio.setForeground(new java.awt.Color(255, 255, 255));
        edgeRadio.setText("EdgeR");
        edgeRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                edgeRadioActionPerformed(evt);
            }
        });

        bothRadio.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        bothRadio.setForeground(new java.awt.Color(255, 255, 255));
        bothRadio.setText("Both");
        bothRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bothRadioActionPerformed(evt);
            }
        });

        countsFolder.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        countsFolder.setForeground(new java.awt.Color(255, 255, 255));
        countsFolder.setSelected(true);
        countsFolder.setText("Provide the directory path for the FeatureCounts folder:");
        countsFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                countsFolderActionPerformed(evt);
            }
        });

        countsFile.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        countsFile.setForeground(new java.awt.Color(255, 255, 255));
        countsFile.setText("Upload a counts file (CSV or TSV format):");
        countsFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                countsFileActionPerformed(evt);
            }
        });

        countsFolderTxt.setEditable(false);

        countsFolderBtn.setText("...");
        countsFolderBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                countsFolderBtnActionPerformed(evt);
            }
        });

        countsFileTxt.setEditable(false);

        countsFileBtn.setText("...");
        countsFileBtn.setEnabled(false);
        countsFileBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                countsFileBtnActionPerformed(evt);
            }
        });

        jLabel99.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel99.setForeground(new java.awt.Color(255, 255, 255));
        jLabel99.setText("ExampleData");
        jLabel99.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel99MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout inputPanelLayout = new javax.swing.GroupLayout(inputPanel);
        inputPanel.setLayout(inputPanelLayout);
        inputPanelLayout.setHorizontalGroup(
            inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputPanelLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(inputPanelLayout.createSequentialGroup()
                        .addComponent(jLabel32)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(output_Dir, javax.swing.GroupLayout.PREFERRED_SIZE, 956, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(outputBtn))
                    .addGroup(inputPanelLayout.createSequentialGroup()
                        .addComponent(jLabel98)
                        .addGap(267, 267, 267)
                        .addComponent(jLabel99))
                    .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel103)
                        .addComponent(countsFolder))
                    .addGroup(inputPanelLayout.createSequentialGroup()
                        .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, inputPanelLayout.createSequentialGroup()
                                .addComponent(jLabel104)
                                .addGap(50, 50, 50)
                                .addComponent(deseqRadio)
                                .addGap(18, 18, 18)
                                .addComponent(edgeRadio)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(bothRadio))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, inputPanelLayout.createSequentialGroup()
                                .addGap(91, 91, 91)
                                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(inputPanelLayout.createSequentialGroup()
                                        .addComponent(countsFile)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(countsFolderTxt)
                                    .addComponent(countsFileTxt))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(countsFolderBtn)
                            .addComponent(countsFileBtn))))
                .addContainerGap(582, Short.MAX_VALUE))
        );
        inputPanelLayout.setVerticalGroup(
            inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputPanelLayout.createSequentialGroup()
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(inputPanelLayout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jLabel98))
                    .addGroup(inputPanelLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel99)))
                .addGap(18, 18, 18)
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel32)
                    .addComponent(output_Dir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(outputBtn))
                .addGap(41, 41, 41)
                .addComponent(jLabel103)
                .addGap(18, 18, 18)
                .addComponent(countsFolder)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(countsFolderTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(countsFolderBtn))
                .addGap(18, 18, 18)
                .addComponent(countsFile)
                .addGap(18, 18, 18)
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(countsFileTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(countsFileBtn))
                .addGap(37, 37, 37)
                .addGroup(inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel104)
                    .addComponent(deseqRadio)
                    .addComponent(edgeRadio)
                    .addComponent(bothRadio))
                .addContainerGap(67, Short.MAX_VALUE))
        );

        MainPanel.add(inputPanel);

        downPanel.setBackground(new java.awt.Color(0, 0, 0));
        downPanel.setOpaque(false);
        downPanel.setLayout(new org.jdesktop.swingx.VerticalLayout());

        sampleDataPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Prepare Sample Meta Data", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N
        sampleDataPanel.setOpaque(false);

        addColumnButton.setText("Add Column");
        addColumnButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addColumnButtonActionPerformed(evt);
            }
        });

        deleteColumnButton.setText("Delete Column");
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
        sampleRadio1.setText("Upload a Sample data CSV/TSV File:");
        sampleRadio1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sampleRadio1ActionPerformed(evt);
            }
        });

        sampleRadio2.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        sampleRadio2.setForeground(new java.awt.Color(255, 255, 255));
        sampleRadio2.setSelected(true);
        sampleRadio2.setText("Manual Data Entry:");
        sampleRadio2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sampleRadio2ActionPerformed(evt);
            }
        });

        sampleFilePathTxt.setEditable(false);

        jButton1.setText("...");
        jButton1.setEnabled(false);
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

        jLabel4.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Press the Lock Table button to proceed with further analysis.");

        javax.swing.GroupLayout sampleDataPanelLayout = new javax.swing.GroupLayout(sampleDataPanel);
        sampleDataPanel.setLayout(sampleDataPanelLayout);
        sampleDataPanelLayout.setHorizontalGroup(
            sampleDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sampleDataPanelLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(sampleDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sampleDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(sampleRadio1)
                        .addGroup(sampleDataPanelLayout.createSequentialGroup()
                            .addComponent(addColumnButton, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(deleteColumnButton)))
                    .addComponent(sampleRadio2)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addGroup(sampleDataPanelLayout.createSequentialGroup()
                        .addComponent(sampleFilePathTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 854, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(sampleDataPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(sampleDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(sampleDataPanelLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tableLockBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel4))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(sampleDataPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 1167, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(sampleDataPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addColumnButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(deleteColumnButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tableLockBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        downPanel.add(sampleDataPanel);

        analysisModePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Analysis Mode:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255))); // NOI18N
        analysisModePanel.setOpaque(false);

        jLabel16.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("Please select the appropriate analysis type:");

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

        jLabel22.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(255, 255, 255));
        jLabel22.setText("Select Interaction terms to be used:");

        jScrollPane1.setViewportView(interactionList);

        javax.swing.GroupLayout interactionTermPanelLayout = new javax.swing.GroupLayout(interactionTermPanel);
        interactionTermPanel.setLayout(interactionTermPanelLayout);
        interactionTermPanelLayout.setHorizontalGroup(
            interactionTermPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(interactionTermPanelLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(interactionTermPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(interactionTermPanelLayout.createSequentialGroup()
                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE))
                .addContainerGap())
        );
        interactionTermPanelLayout.setVerticalGroup(
            interactionTermPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(interactionTermPanelLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout complexDesignPanelLayout = new javax.swing.GroupLayout(complexDesignPanel);
        complexDesignPanel.setLayout(complexDesignPanelLayout);
        complexDesignPanelLayout.setHorizontalGroup(
            complexDesignPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(complexDesignPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(complexDesignPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(interactionTermPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(complexDesignPanelLayout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addGap(43, 43, 43)
                        .addComponent(interactionTermCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        complexDesignPanelLayout.setVerticalGroup(
            complexDesignPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, complexDesignPanelLayout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(complexDesignPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(interactionTermCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(interactionTermPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel11.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Counts Threshold (X):");

        countThresh.setText("10");

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
                .addContainerGap(311, Short.MAX_VALUE))
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

        jLabel34.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel34.setForeground(new java.awt.Color(255, 255, 255));
        jLabel34.setText("For DESeq2, this refers to sample metadata containing one condition column");

        jLabel35.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel35.setForeground(new java.awt.Color(255, 255, 255));
        jLabel35.setText("For edgeR, this refers to a pairwise comparison between two groups.");

        jLabel37.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel37.setForeground(new java.awt.Color(255, 255, 255));
        jLabel37.setText("For DESeq2, this applies to sample metadata with more than one condition column.");

        jLabel38.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel38.setForeground(new java.awt.Color(255, 255, 255));
        jLabel38.setText("For edgeR, this involves comparisons with more than two groups or factors.");

        jLabel36.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel36.setForeground(new java.awt.Color(255, 255, 255));
        jLabel36.setText("(excluding the SampleName column).");

        jLabel25.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel25.setForeground(new java.awt.Color(255, 255, 255));
        jLabel25.setText("Percentage Threshold (Y):");

        countThresh1.setText("0.9");

        filteringCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "None", "Total Count ≥ X", "Present in ≥ Y% Samples", "Both" }));
        filteringCombo.setSelectedIndex(3);
        filteringCombo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                filteringComboItemStateChanged(evt);
            }
        });

        jLabel40.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel40.setForeground(new java.awt.Color(255, 255, 255));
        jLabel40.setText("Choose a filtering method:");

        javax.swing.GroupLayout analysisModePanelLayout = new javax.swing.GroupLayout(analysisModePanel);
        analysisModePanel.setLayout(analysisModePanelLayout);
        analysisModePanelLayout.setHorizontalGroup(
            analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(analysisModePanelLayout.createSequentialGroup()
                .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(analysisModePanelLayout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16)
                            .addComponent(singleFactorRadio)))
                    .addGroup(analysisModePanelLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(multiFactorRadio)
                            .addGroup(analysisModePanelLayout.createSequentialGroup()
                                .addGap(47, 47, 47)
                                .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel38)
                                    .addComponent(jLabel37)))
                            .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, analysisModePanelLayout.createSequentialGroup()
                                    .addComponent(jLabel40)
                                    .addGap(18, 18, 18)
                                    .addComponent(filteringCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(analysisModePanelLayout.createSequentialGroup()
                                    .addComponent(jLabel11)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(countThresh, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(analysisModePanelLayout.createSequentialGroup()
                                    .addComponent(jLabel25)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(countThresh1, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(analysisModePanelLayout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel36)
                            .addComponent(jLabel35)
                            .addComponent(jLabel34))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 195, Short.MAX_VALUE)
                .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(complexDesignPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(edgeRDesignPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(196, Short.MAX_VALUE))
        );
        analysisModePanelLayout.setVerticalGroup(
            analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(analysisModePanelLayout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(analysisModePanelLayout.createSequentialGroup()
                        .addComponent(complexDesignPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(edgeRDesignPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(analysisModePanelLayout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(singleFactorRadio)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel34)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel36)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel35)
                        .addGap(46, 46, 46)
                        .addComponent(multiFactorRadio)
                        .addGap(2, 2, 2)
                        .addComponent(jLabel37)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel38)
                        .addGap(63, 63, 63)
                        .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(filteringCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel40))
                        .addGap(30, 30, 30)
                        .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(countThresh, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(analysisModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel25)
                            .addComponent(countThresh1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
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

        jTextField3.setText("0.1");

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
        jLabel3.setText("alpha p value cutoff:");

        jLabel28.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel28.setForeground(new java.awt.Color(255, 255, 255));
        jLabel28.setText("Select from resultNames list:");

        jLabel9.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("padj  less than:");

        deFactorList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        jScrollPane9.setViewportView(deFactorList);

        jTextField2.setText("0.05");

        jTextField1.setText("2");

        jLabel84.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel84.setForeground(new java.awt.Color(255, 255, 255));
        jLabel84.setText("Choose Differential Expression Comparison Method");

        jLabel85.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel85.setForeground(new java.awt.Color(255, 255, 255));
        jLabel85.setText("AND");

        jLabel24.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel24.setForeground(new java.awt.Color(255, 255, 255));
        jLabel24.setText("In case of any error in padj and logfoldchain you can rerun the analysis after changing values:");

        jLabel89.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel89.setForeground(new java.awt.Color(255, 255, 255));
        jLabel89.setText("Select either \"Contrast\" or \"List\" to define your comparison strategy:");

        jLabel90.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel90.setForeground(new java.awt.Color(255, 255, 255));
        jLabel90.setText("Choose a factor (column) and two levels — the first will be used as the reference.");

        javax.swing.GroupLayout resDEPanelLayout = new javax.swing.GroupLayout(resDEPanel);
        resDEPanel.setLayout(resDEPanelLayout);
        resDEPanelLayout.setHorizontalGroup(
            resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resDEPanelLayout.createSequentialGroup()
                .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                                .addGap(18, 18, 18)
                                .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(resDEPanelLayout.createSequentialGroup()
                                        .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(contrastFactorCombo, 0, 294, Short.MAX_VALUE)
                                            .addComponent(combination1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel19)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(combination2, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 605, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(resDEPanelLayout.createSequentialGroup()
                                .addGap(74, 74, 74)
                                .addComponent(jLabel24)
                                .addGap(18, 18, 18)
                                .addComponent(reRunResults))
                            .addGroup(resDEPanelLayout.createSequentialGroup()
                                .addGap(50, 50, 50)
                                .addComponent(jRadioContrast)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel90))
                            .addComponent(jLabel84)
                            .addGroup(resDEPanelLayout.createSequentialGroup()
                                .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel3))
                                .addGap(47, 47, 47)
                                .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(jTextField2, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(33, 33, 33)
                                .addComponent(jLabel85)
                                .addGap(43, 43, 43)
                                .addComponent(jLabel10)
                                .addGap(47, 47, 47)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel89))
                        .addGap(0, 787, Short.MAX_VALUE))
                    .addComponent(jSeparator4))
                .addContainerGap())
            .addComponent(jSeparator3)
        );
        resDEPanelLayout.setVerticalGroup(
            resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resDEPanelLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel84)
                .addGap(18, 18, 18)
                .addComponent(jLabel89)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioContrast)
                    .addComponent(jLabel90))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(contrastFactorCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(combination1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19)
                    .addComponent(combination2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(resDEPanelLayout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(resDEPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jRadioList)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel28)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resDEPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
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

        selectInfoLabel.setFont(new java.awt.Font("Liberation Sans", 2, 15)); // NOI18N
        selectInfoLabel.setForeground(new java.awt.Color(255, 255, 255));
        selectInfoLabel.setText("Select any test type");

        testInfoLabel.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        testInfoLabel.setForeground(new java.awt.Color(255, 255, 255));

        jTextField22.setText("0.05");

        jLabel94.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel94.setForeground(new java.awt.Color(255, 255, 255));
        jLabel94.setText("PValue Threshold:");

        testInfoLabel1.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N
        testInfoLabel1.setForeground(new java.awt.Color(255, 255, 255));

        testInfoLabel2.setFont(new java.awt.Font("Liberation Sans", 0, 12)); // NOI18N
        testInfoLabel2.setForeground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout resEDPanelLayout = new javax.swing.GroupLayout(resEDPanel);
        resEDPanel.setLayout(resEDPanelLayout);
        resEDPanelLayout.setHorizontalGroup(
            resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resEDPanelLayout.createSequentialGroup()
                .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(resEDPanelLayout.createSequentialGroup()
                        .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(testInfoLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 751, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, resEDPanelLayout.createSequentialGroup()
                                .addGap(236, 236, 236)
                                .addComponent(testInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 751, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(testInfoLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 751, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 777, Short.MAX_VALUE))
                    .addGroup(resEDPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel82)
                            .addComponent(jLabel88))
                        .addGap(41, 41, 41)
                        .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane10)
                            .addComponent(edgeRTestCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(resEDPanelLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(resEDPanelLayout.createSequentialGroup()
                                        .addGap(180, 180, 180)
                                        .addComponent(jButton25)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(resEDPanelLayout.createSequentialGroup()
                                        .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(resEDPanelLayout.createSequentialGroup()
                                                .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addComponent(jLabel94)
                                                    .addComponent(jLabel87))
                                                .addGap(41, 41, 41)
                                                .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jTextField22)
                                                    .addComponent(jTextField21)))
                                            .addGroup(resEDPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel86)
                                                .addGap(41, 41, 41)
                                                .addComponent(contrastValue, javax.swing.GroupLayout.PREFERRED_SIZE, 302, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGap(711, 711, 711))))
                            .addGroup(resEDPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(selectInfoLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        resEDPanelLayout.setVerticalGroup(
            resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resEDPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel82)
                        .addComponent(edgeRTestCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(selectInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addComponent(testInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(testInfoLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(resEDPanelLayout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(testInfoLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(resEDPanelLayout.createSequentialGroup()
                                .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(resEDPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel86)
                                        .addGap(18, 18, 18)
                                        .addComponent(jLabel94))
                                    .addGroup(resEDPanelLayout.createSequentialGroup()
                                        .addComponent(contrastValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextField22, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(resEDPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jTextField21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel87))
                                .addGap(18, 18, 18)
                                .addComponent(jButton25))
                            .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(resEDPanelLayout.createSequentialGroup()
                        .addGap(65, 65, 65)
                        .addComponent(jLabel88)))
                .addGap(9, 9, 9)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
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
        jComboBox9.setSelectedIndex(7);

        jLabel65.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel65.setForeground(new java.awt.Color(255, 255, 255));
        jLabel65.setText("KEGG pathway over-representation analysis:");

        jLabel67.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel67.setForeground(new java.awt.Color(255, 255, 255));
        jLabel67.setText("pvalueCutoff:");

        jTextField13.setText("0.05");

        jComboBox10.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "holm", "hochberg", "hommel", "bonferroni", "BH", "BY", "fdr", "none" }));
        jComboBox10.setSelectedIndex(7);

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
                                .addComponent(jLabel76, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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

    private void inputButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputButtonActionPerformed

        warningLabel.setText(null);

        if (inputButton.getText().equalsIgnoreCase("NEXT")) {
            if (checkInput()) {
                inputPanel.setVisible(false);


//Prepare Counts and extract SampleNames
                runRCOUNT(System.getProperty("user.dir").concat("/bin/onlyCounts.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"));
                downPanel.setVisible(true);
                inputButton.setText("Run Pipeline");
            }//checkInput 
            else {
                jScrollPane2.getViewport().setViewPosition(new Point(0, 0));
            }

        } else if (inputButton.getText().equalsIgnoreCase("Run Pipeline")) {

            if (checkDown()) {
                inputButton.setEnabled(false);
                // CREATING sample_groups.csv FILE from sampleTable
                saveTableToTSV(output_Dir.getText() + "sample_groups.tsv");

                runRDF(System.getProperty("user.dir").concat("/bin/onlyDF.R"), output_Dir.getText(), System.getProperty("user.dir").concat("/R_Libraries"));

            } else {
                jScrollPane2.getViewport().setViewPosition(new Point(0, 0));

            }
        }//endIf run pipeline
        inputButton.setEnabled(true);

    }//GEN-LAST:event_inputButtonActionPerformed

    private void singleFactorRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_singleFactorRadioActionPerformed
        complexDesignPanel.setVisible(false);

    }//GEN-LAST:event_singleFactorRadioActionPerformed

    private void multiFactorRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multiFactorRadioActionPerformed
        complexDesignPanel.setVisible(true);

    }//GEN-LAST:event_multiFactorRadioActionPerformed

    private void reRunResultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reRunResultsActionPerformed

        rcodeError = false;

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


    }//GEN-LAST:event_reRunResultsActionPerformed

    private void interactionTermComboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_interactionTermComboItemStateChanged
        // interactionList.clearSelection();
        if (interactionTermCombo.getSelectedIndex() == 0) {
            interactionTermPanel.setVisible(false);

        } else if (interactionTermCombo.getSelectedIndex() == 1) {
            interactionTermPanel.setVisible(true);
        }
    }//GEN-LAST:event_interactionTermComboItemStateChanged

    private void jRadioContrastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioContrastActionPerformed
        contrastFactorCombo.setEnabled(true);
        combination1.setEnabled(true);
        combination2.setEnabled(true);

        deFactorList.setEnabled(false);


    }//GEN-LAST:event_jRadioContrastActionPerformed

    private void jRadioListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioListActionPerformed

        deFactorList.setEnabled(true);
        contrastFactorCombo.setEnabled(false);
        combination1.setEnabled(false);
        combination2.setEnabled(false);


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

        //Select, exactTest, glmQLFit+glmQLFTest(coef), glmQLFit+glmQLFTest(contrast), glmFit+glmLRT(coef), glmFit+glmLRT(contrast), glmTreat      
        if (edgeRTestCombo.getSelectedIndex() == 0) {
            warningLabel.setText("Select any test type.");
            rcodeError = true;
        } else if (edgeRTestCombo.getSelectedIndex() == 1) {
            if (edgeRTestList.getSelectedIndices().length != 2) {
                warningLabel.setText("Must select two coefficents");
                rcodeError = true;
            }

        } else if ((edgeRTestCombo.getSelectedIndex() == 2) || (edgeRTestCombo.getSelectedIndex() == 4) || (edgeRTestCombo.getSelectedIndex() == 6)) {
            if (edgeRTestList.getSelectedIndices().length < 1) {
                warningLabel.setText("Select any coefficent(s)");
                rcodeError = true;
            }

        } else if ((edgeRTestCombo.getSelectedIndex() == 3) || (edgeRTestCombo.getSelectedIndex() == 5)) { //contrast
            // Regular expression for a comma-separated list of number
          //  String pattern = "^[-]?\\\\d+(\\\\.\\\\d+)?(,[-]?\\\\d+(\\\\.\\\\d+)?)*$";
           String pattern = "^([a-zA-Z_][a-zA-Z0-9_]*:-?\\d+(\\.\\d+)?)(,\\s*[a-zA-Z_][a-zA-Z0-9_]*:-?\\d+(\\.\\d+)?)*$";

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

    private void addColumnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addColumnButtonActionPerformed
        String newColumn = JOptionPane.showInputDialog(this, "Enter column name:").toUpperCase();

        if (newColumn != null && !newColumn.isBlank()) {
            if (!columnExists(newColumn)) {
                sampleTableModel.addColumn(newColumn);
            } else {
                JOptionPane.showMessageDialog(this, newColumn + " already exists.");
            }
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

        if (interceptOption.getSelectedIndex() == 0 && edgeRTestList.getSelectedIndex() == 0 && edgeRTestCombo.getSelectedIndex() != 1 ) {
            JOptionPane.showMessageDialog(this, "As there is no intercept to be used as reference so 1st column couldn't be selected.1st column will be used as reference in coef");
           // edgeRTestList.clearSelection();
        }

    }//GEN-LAST:event_edgeRTestListValueChanged

    private void tableLockBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tableLockBtnActionPerformed
        if (tableLockBtn.isSelected()) {
            if (sampleTable.getModel().getColumnCount() >= 2 && !hasEmptyCells(sampleTable)) {
                tableLockBtn.setText("Unlock Table");
                addColumnButton.setEnabled(false);
                deleteColumnButton.setEnabled(false);

                tableFlag = false;//Make table uneditable
                sampleTable.repaint();
//Panels

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

//---------------------------------------------------------------
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

                if (singleFactorRadio.isSelected()) {
                    // Use only the first factor column (usually "CONDITION")
                    if (columnCount > 1) {
                        String columnName = sampleTable.getColumnName(1); // assuming CONDITION is at index 1
                        factors.add(columnName);
                    }
                } else {
                    for (int i = 1; i < columnCount; i++) {  // Skip the first column
                        String columnName = sampleTable.getColumnName(i);
                        factors.add(columnName);
                    }
                }

                deFactorListModel.clear();

                // Retrieve unique values (levels) for each 'factor'
                Map<String, Set<String>> factorLevels = new HashMap<>();
                for (String columnName : factors) {
                    //Set<String> levels = new HashSet<>();
                    Set<String> levels = new LinkedHashSet<>();
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

                        // deFactorListModel.addElement(entry.getKey() + "_" + levels.get(1) + "_vs_" + levels.get(0));
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

    

                //Panels Settings
                analysisModePanel.setVisible(true);
                geneEnrichmentPanel.setVisible(true);
                if (analysisP.equalsIgnoreCase("deSeq2")) {
                    resDEPanel.setVisible(true);
                } else if (analysisP.equalsIgnoreCase("edgeR")) {
                    edgeRDesignPanel.setVisible(true);
                    resEDPanel.setVisible(true);
                } else if (analysisP.equalsIgnoreCase("both")) {
                    resDEPanel.setVisible(true);
                    edgeRDesignPanel.setVisible(true);
                    resEDPanel.setVisible(true);
                }
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

            analysisModePanel.setVisible(false);
            geneEnrichmentPanel.setVisible(false);
            if (analysisP.equalsIgnoreCase("deSeq2")) {
                resDEPanel.setVisible(false);
            } else if (analysisP.equalsIgnoreCase("edgeR")) {
                edgeRDesignPanel.setVisible(false);
                resEDPanel.setVisible(false);
            } else if (analysisP.equalsIgnoreCase("both")) {
                resDEPanel.setVisible(false);
                edgeRDesignPanel.setVisible(false);
                resEDPanel.setVisible(false);
            }

            
          
            
        }//IF LOCK SELECTED

    }//GEN-LAST:event_tableLockBtnActionPerformed
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

                    //Reset Table
                    // Reset sampleTableModel: keep only the first column (SampleName)
                    while (sampleTableModel.getColumnCount() > 1) {
                        sampleTableModel.setColumnCount(sampleTableModel.getColumnCount() - 1);
                    }
                    // Read and validate header
                    String[] headers = br.readLine().toUpperCase().split(delimiter);

                    // Validate "SampleName" as the first column
                    if (!headers[0].equalsIgnoreCase("SAMPLENAME")) {
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
//Reading data from file
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] values = line.split(delimiter, -1); // -1 to preserve empty trailing columns
                        String fileSampleName = cleanName(values[0]);

                        if (sampleNameToRowIndex.containsKey(fileSampleName)) {
                            int rowIndex = sampleNameToRowIndex.get(fileSampleName);

                            // Update the table row with the corresponding file data
                            for (int col = 1; col < values.length; col++) {
                                sampleTableModel.setValueAt(values[col].toLowerCase(), rowIndex, col);
                            }
                        }
                    }
                    br.close();

                } //if
                catch (FileNotFoundException ex) {
                    Logger.getLogger(DEGenesAnalysis.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(DEGenesAnalysis.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        br.close();
                    } catch (IOException ex) {
                        Logger.getLogger(DEGenesAnalysis.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        }//end-if


    }//GEN-LAST:event_jButton1ActionPerformed

    private void sampleRadio1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sampleRadio1ActionPerformed
        if (!tableLockBtn.isSelected()) {
            jButton1.setEnabled(true);
            addColumnButton.setEnabled(false);
            deleteColumnButton.setEnabled(false);
        } else {
            warningLabel.setText("Unlock the table 1st");
            sampleRadio1.setSelected(false);
        }

    }//GEN-LAST:event_sampleRadio1ActionPerformed

    private void sampleRadio2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sampleRadio2ActionPerformed
        if (!tableLockBtn.isSelected()) {
            jButton1.setEnabled(false);
            sampleFilePathTxt.setText("");
            addColumnButton.setEnabled(true);
            deleteColumnButton.setEnabled(true);
        } else {
            warningLabel.setText("Unlock the table 1st");
            sampleRadio2.setSelected(false);
        }
    }//GEN-LAST:event_sampleRadio2ActionPerformed

    private void countsFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_countsFolderActionPerformed
        countsFileTxt.setText("");
        countsFileBtn.setEnabled(false);
        countsFolderBtn.setEnabled(true);
    }//GEN-LAST:event_countsFolderActionPerformed

    private void countsFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_countsFileActionPerformed
        countsFolderTxt.setText("");
        countsFolderBtn.setEnabled(false);
        countsFileBtn.setEnabled(true);
    }//GEN-LAST:event_countsFileActionPerformed

    private void countsFolderBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_countsFolderBtnActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new java.io.File("."));
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            String path = fileChooser.getSelectedFile().toString();

            countsFolderTxt.setText(path + "/");

        }
    }//GEN-LAST:event_countsFolderBtnActionPerformed

    private void countsFileBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_countsFileBtnActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("TSV Files (*.tsv)", "tsv"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {

                countsFileTxt.setText(fileChooser.getSelectedFile().toString());

            }
        }

    }//GEN-LAST:event_countsFileBtnActionPerformed

    private void jLabel99MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel99MouseClicked
        output_Dir.setText("/home/iffy/PhD_Data/CASESTUDY/1-Hepatocellular_carcinoma_100/RNASeq_PRJNA867011/TestingDESeq2/");
        countsFolderTxt.setText("/home/iffy/PhD_Data/CASESTUDY/1-Hepatocellular_carcinoma_100/RNASeq_PRJNA867011");


    }//GEN-LAST:event_jLabel99MouseClicked

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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel MainPanel;
    private javax.swing.ButtonGroup QCbuttonGroup;
    private javax.swing.JButton addColumnButton;
    private javax.swing.JPanel analysisModePanel;
    private javax.swing.JRadioButton bothRadio;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.JComboBox<String> combination1;
    private javax.swing.JComboBox<String> combination2;
    private javax.swing.JPanel complexDesignPanel;
    private javax.swing.JComboBox<String> contrastFactorCombo;
    private javax.swing.JTextField contrastValue;
    private javax.swing.JTextField countThresh;
    private javax.swing.JTextField countThresh1;
    private javax.swing.JRadioButton countsFile;
    private javax.swing.JButton countsFileBtn;
    private javax.swing.JTextField countsFileTxt;
    private javax.swing.JRadioButton countsFolder;
    private javax.swing.JButton countsFolderBtn;
    private javax.swing.JTextField countsFolderTxt;
    private javax.swing.JList<String> deFactorList;
    private javax.swing.JButton deleteColumnButton;
    private javax.swing.JRadioButton deseqRadio;
    private javax.swing.JPanel downPanel;
    private javax.swing.JPanel edgeRDesignPanel;
    private javax.swing.JComboBox<String> edgeRTestCombo;
    private javax.swing.JList<String> edgeRTestList;
    private javax.swing.JRadioButton edgeRadio;
    private javax.swing.ButtonGroup expressionAnalysisBG;
    private javax.swing.JComboBox<String> filteringCombo;
    private javax.swing.JPanel geneEnrichmentPanel;
    private javax.swing.JLabel helpLabel;
    private javax.swing.JButton inputButton;
    private javax.swing.ButtonGroup inputOptionsGroup;
    private javax.swing.JPanel inputPanel;
    private javax.swing.JList<String> interactionList;
    private javax.swing.JComboBox<String> interactionTermCombo;
    private javax.swing.JPanel interactionTermPanel;
    private javax.swing.JComboBox<String> interceptOption;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton25;
    private javax.swing.JComboBox<String> jComboBox10;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JComboBox<String> jComboBox5;
    private javax.swing.JComboBox<String> jComboBox6;
    private javax.swing.JComboBox<String> jComboBox7;
    private javax.swing.JComboBox<String> jComboBox8;
    private javax.swing.JComboBox<String> jComboBox9;
    private javax.swing.JLabel jLabel10;
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
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel6;
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
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel82;
    private javax.swing.JLabel jLabel84;
    private javax.swing.JLabel jLabel85;
    private javax.swing.JLabel jLabel86;
    private javax.swing.JLabel jLabel87;
    private javax.swing.JLabel jLabel88;
    private javax.swing.JLabel jLabel89;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabel90;
    private javax.swing.JLabel jLabel94;
    private javax.swing.JLabel jLabel98;
    private javax.swing.JLabel jLabel99;
    private javax.swing.JRadioButton jRadioContrast;
    private javax.swing.JRadioButton jRadioList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JTextField jTextField1;
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
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JRadioButton multiFactorRadio;
    private javax.swing.JButton outputBtn;
    private javax.swing.JTextField output_Dir;
    private javax.swing.JButton reRunResults;
    private javax.swing.JPanel resDEPanel;
    private javax.swing.JPanel resEDPanel;
    private javax.swing.JPanel sampleDataPanel;
    private javax.swing.JTextField sampleFilePathTxt;
    private javax.swing.JRadioButton sampleRadio1;
    private javax.swing.JRadioButton sampleRadio2;
    private javax.swing.JTable sampleTable;
    private javax.swing.ButtonGroup sampleTableRadios;
    private javax.swing.JLabel selectInfoLabel;
    private javax.swing.JRadioButton singleFactorRadio;
    private javax.swing.JToggleButton tableLockBtn;
    private javax.swing.JLabel testInfoLabel;
    private javax.swing.JLabel testInfoLabel1;
    private javax.swing.JLabel testInfoLabel2;
    private javax.swing.JPanel upPanel;
    private javax.swing.JLabel warningLabel;
    // End of variables declaration//GEN-END:variables
}
