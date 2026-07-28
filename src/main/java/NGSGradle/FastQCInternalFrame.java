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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author iffy
 */
public class FastQCInternalFrame extends javax.swing.JInternalFrame {

    /**
     * Creates new form FastQCInternalFrame
     */
    JFileChooser fileChooser;
    private File[] allFiles;
    private List<String> fileNameWithExtension = new ArrayList<>();
    private List<String> fileNameWithoutExtension = new ArrayList<>();

    private DefaultListModel listModelfasta;
    private DefaultTableModel dm;
    private boolean flag;
    Map<Integer, String> tooltips; //For FastQC Table
    private JTableHeader headerQC;

    public FastQCInternalFrame() {

        //FastQC Table Model
        String[] tblHead = {"Sample Name", "Basic Statistics", "Per base sequence quality", "Per sequence quality scores", "Per base sequence content", "Per sequence GC content", "Per base N content", "Sequence Length Distribution", "Sequence Duplication Levels", "Overrepresented sequences", "Adapter Content"};
        dm = new DefaultTableModel(tblHead, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column <= 11 ? false : true;
            }
        };

        listModelfasta = new DefaultListModel();
         tooltips = new HashMap<>();
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
        //END FASTQC

        jLabel14.setIcon(App.icons[0]);

        fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setAcceptAllFileFilterUsed(true);
        fastqcResultCombo.setEnabled(false);
        flag = false;

    }

    ////////////////////FUNCTION//////////
    String[] readData(String filename) {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            List<String> cmdList = new ArrayList<String>();
            cmdList.add("sh");
            cmdList.add(System.getProperty("user.dir").concat("/bin/fastqc.sh"));
            cmdList.add("Step2");//1
  
            cmdList.add(outputTxt.getText() + filename);//2

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
            System.out.println(samplerow);
            return samplerow;
        } catch (IOException ex) {
            App.LOGGER.error("Error loading FastQC: " + ex);

        }
        return null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        FastQCPanel = new javax.swing.JPanel(){

            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(NGSGradle.App.bgLoginMain, 0, 0, null);
            }
        }

        ;
        browseBtn = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        analyseBtn = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        fastqcResultCombo = new javax.swing.JComboBox<>();
        jLabel14 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        outputTxt = new javax.swing.JTextField();
        browseOBtn = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        fastqTxt = new javax.swing.JList<>(listModelfasta);
        jScrollPane3 = new javax.swing.JScrollPane();
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
        jLabel4 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        warningLabel = new javax.swing.JLabel();

        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("FASTQC");

        FastQCPanel.setBackground(new java.awt.Color(204, 204, 204));
        FastQCPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "FASTQC: A quality control tool for high throughput sequence data", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N

        browseBtn.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        browseBtn.setText("...");
        browseBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                browseBtnMouseClicked(evt);
            }
        });
        browseBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseBtnActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Upload Sequence file(s) for analysis:");

        analyseBtn.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        analyseBtn.setText("Perform Analysis");
        analyseBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                analyseBtnActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("View Html Results:");

        fastqcResultCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "" }));
        fastqcResultCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fastqcResultComboActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Liberation Sans", 1, 15)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setText("HELP?");
        jLabel14.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel14MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel14MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel14MouseExited(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Set Output Directory:");

        outputTxt.setEditable(false);

        browseOBtn.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        browseOBtn.setText("...");
        browseOBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseOBtnActionPerformed(evt);
            }
        });

        fastqTxt.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        fastqTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                fastqTxtKeyPressed(evt);
            }
        });
        jScrollPane2.setViewportView(fastqTxt);

        fastqcTable.setFont(new java.awt.Font("Liberation Mono", 1, 14)); // NOI18N
        fastqcTable.setModel(dm);
        jScrollPane3.setViewportView(fastqcTable);

        jLabel4.setFont(new java.awt.Font("Liberation Sans", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Results Summary:");

        warningLabel.setFont(new java.awt.Font("Liberation Sans", 3, 18)); // NOI18N
        warningLabel.setForeground(new java.awt.Color(255, 0, 0));
        warningLabel.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                warningLabelPropertyChange(evt);
            }
        });

        javax.swing.GroupLayout FastQCPanelLayout = new javax.swing.GroupLayout(FastQCPanel);
        FastQCPanel.setLayout(FastQCPanelLayout);
        FastQCPanelLayout.setHorizontalGroup(
            FastQCPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FastQCPanelLayout.createSequentialGroup()
                .addGroup(FastQCPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(FastQCPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(FastQCPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(FastQCPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(outputTxt)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 793, Short.MAX_VALUE))
                        .addGroup(FastQCPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(FastQCPanelLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(FastQCPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(FastQCPanelLayout.createSequentialGroup()
                                        .addGap(1, 1, 1)
                                        .addComponent(browseBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(browseOBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, FastQCPanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel14)
                                .addGap(86, 86, 86))))
                    .addGroup(FastQCPanelLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(FastQCPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(FastQCPanelLayout.createSequentialGroup()
                                .addGroup(FastQCPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fastqcResultCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(FastQCPanelLayout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 1402, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 75, Short.MAX_VALUE))
                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 1492, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(FastQCPanelLayout.createSequentialGroup()
                .addGap(555, 555, 555)
                .addComponent(analyseBtn)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(FastQCPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(FastQCPanelLayout.createSequentialGroup()
                    .addGap(146, 146, 146)
                    .addComponent(warningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 826, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(527, Short.MAX_VALUE)))
        );
        FastQCPanelLayout.setVerticalGroup(
            FastQCPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FastQCPanelLayout.createSequentialGroup()
                .addGroup(FastQCPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(FastQCPanelLayout.createSequentialGroup()
                        .addGap(136, 136, 136)
                        .addComponent(jLabel1))
                    .addGroup(FastQCPanelLayout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addGroup(FastQCPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(FastQCPanelLayout.createSequentialGroup()
                                .addComponent(browseOBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(browseBtn))
                            .addGroup(FastQCPanelLayout.createSequentialGroup()
                                .addGroup(FastQCPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(outputTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jLabel14))
                .addGap(18, 18, 18)
                .addComponent(analyseBtn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(FastQCPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(fastqcResultCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 321, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(FastQCPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(FastQCPanelLayout.createSequentialGroup()
                    .addGap(14, 14, 14)
                    .addComponent(warningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(690, Short.MAX_VALUE)))
        );

        jScrollPane1.setViewportView(FastQCPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1317, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 764, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void browseBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseBtnActionPerformed

        //To reset if browse button is clicked again
        if (flag == true) {
            fastqcResultCombo.setEnabled(false);
            fastqcResultCombo.removeAllItems();
            listModelfasta.clear();
            dm.setRowCount(0);
            //   dm.fireTableDataChanged();
        }

        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("FastQ Files (*.fastq)", "fastq"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Casava FastQ Files (*.fastq)", "fastq"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("FastaQ GZIP Files (*.fastq.gz)", "gz"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("SAM Files (*.sam)", "sam"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("BAM Files (*.bam)", "bam"));

        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            if (fileChooser.getSelectedFile() != null) {
                allFiles = fileChooser.getSelectedFiles();

                String path = "";
                fileNameWithExtension.clear();
                fileNameWithoutExtension.clear();
                //Adding path to the List, Extracting the file names
                for (int i = 0; i < allFiles.length; i++) {

                    listModelfasta.addElement(" \"" + allFiles[i].getAbsolutePath() + "\"");
                    fileNameWithExtension.add(i, allFiles[i].getName());
                    if ((allFiles[i].getName().endsWith(".fastq.gz")) || (allFiles[i].getName().endsWith(".fq.gz"))) {

                        fileNameWithoutExtension.add(FilenameUtils.removeExtension(FilenameUtils.removeExtension(allFiles[i].getName())) + "_fastqc.html");
                    } else {
                        fileNameWithoutExtension.add(FilenameUtils.removeExtension(allFiles[i].getName()) + "_fastqc.html");
                    }

                }//end for
            }//if file chooser
        }//if
        flag = true;//clicked browse button again
    }//GEN-LAST:event_browseBtnActionPerformed

    private void analyseBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_analyseBtnActionPerformed

        if ((outputTxt.getText().isBlank()) || (listModelfasta.isEmpty())) {
            warningLabel.setText("**ERROR: Output Directory or Sequence Files can't be left blank");
        } else {

            //On Perform Analysis Button Clicked        
            browseOBtn.setEnabled(false);
            browseBtn.setEnabled(false);
            fastqcResultCombo.setEnabled(false);
            analyseBtn.setEnabled(false);

            warningLabel.setText("");
            App.bar.setIndeterminate(true);
            App.sP.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Running FastQC:", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Liberation Sans", 1, 15), new java.awt.Color(255, 255, 255)));
            SwingWorker<?, ?> worker = new SwingWorker<Integer, String>() {
                private int status;

                @Override
                protected Integer doInBackground() {
                    try {
                        //FASTQC Windows :Dir_with_FastQC>java -Xmx250m -classpath .;./sam-1.103.jar;./jbzip2-0.9.jar uk.ac.babraham.FastQC.FastQCApplication ju.fq
                        ProcessBuilder builder = new ProcessBuilder();
                        List<String> cmdList = new ArrayList<String>();
                        cmdList.add("sh");
                        cmdList.add(System.getProperty("user.dir").concat("/bin/fastqc.sh"));
                        cmdList.add("Step1");//1
                        cmdList.add(App.FastQC_PATH); //$2
                        String myArray = "";

                        for (int i = 0; i < fastqTxt.getModel().getSize(); i++) {
                            //Reading Complete file paths
                            myArray += (fastqTxt.getModel().getElementAt(i) + " ");

                        }

                        myArray = StringUtils.chop(myArray).replaceAll("\"", "");
                        myArray = myArray.trim();
                        cmdList.add(myArray); //3
                        cmdList.add(outputTxt.getText());//4
                        builder.command(cmdList);
                        builder.redirectErrorStream(true);
                        Process process = builder.start();
                        InputStream is = process.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        String line = null;
                        while ((line = br.readLine()) != null && !isCancelled()) {
                            App.LOGGER.info(line + "\r\n");
                            publish(line);

                        }

                        //Adding file names to the combobox
                        fastqcResultCombo.setModel(new DefaultComboBoxModel<String>(fileNameWithoutExtension.toArray(new String[0])));
                        fastqcResultCombo.setEnabled(true);
                        flag = true;
                        if (!isCancelled()) {
                            status = process.waitFor();
                        }

                        process.destroy();

//FASTQC Analysis DONE
//READING DATA from Fastqc files for Table
                        dm.setRowCount(0);
                        for (int i = 0; i < allFiles.length; i++) {

                            if ((allFiles[i].getName().endsWith(".fastq.gz")) || (allFiles[i].getName().endsWith(".fq.gz"))) {

                                dm.addRow(readData(FilenameUtils.removeExtension(FilenameUtils.removeExtension(allFiles[i].getName())) + "_fastqc.zip"));
                            } else {
                                dm.addRow(readData(FilenameUtils.removeExtension(allFiles[i].getName()) + "_fastqc.zip"));

                            }
                          
                        }//end for
                        dm.fireTableDataChanged();

                    } catch (IOException | InterruptedException ex) {
                        ex.printStackTrace(new PrintWriter(App.stack));
                        App.LOGGER.error("\r\n ERROR in FastQC analysis: " + App.stack);
                        App.textArea.append("\r\n ERROR in FastQC analysis: " + App.stack);

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

                    //On Perform Analysis Button Press        
                    browseOBtn.setEnabled(true);
                    browseBtn.setEnabled(true);
                    fastqcResultCombo.setEnabled(true);
                    analyseBtn.setEnabled(true);

                    //         dm.fireTableDataChanged();
                }

            };

            worker.execute();
        }
        //   fastqcTable.
        /*
        try {
            //FASTQC Windows :Dir_with_FastQC>java -Xmx250m -classpath .;./sam-1.103.jar;./jbzip2-0.9.jar uk.ac.babraham.FastQC.FastQCApplication ju.fq
            ProcessBuilder builder = new ProcessBuilder();

            List<String> cmdList = new ArrayList<String>();
            // adding command and args to the list
            cmdList.add("sh");
            cmdList.add(System.getProperty("user.dir").concat("/bin/fastqc.sh"));
            cmdList.add(App.FastQC_PATH); //$1

            cmdList.add(fastqTxt.getText());
            cmdList.add(outputTxt.getText());

            App.textArea.append("\r\n" + cmdList);

            if (System.getProperty("os.name").startsWith("Windows")) {

                //  builder2.command("cmd.exe", "/c", "java -Xmx250m -classpath .;./sam-1.103.jar;./jbzip2-0.9.jar uk.ac.babraham.FastQC.FastQCApplication" + fastqTxt.getText())
                //        .directory(new File(path));
            } else {
                //

                // builder2.command("sh", "-c", "fastqc", fastqTxt.getText()).directory(new File(path));
                builder.command(cmdList);
            }
            builder.redirectErrorStream(true);

            Process process = builder.start();

            InputStream is = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                App.LOGGER.info(line + "\r\n");
                App.textArea.append(line + "\r\n");

            }

            //RESULTS
            // fileNameWithoutExtension.forEach(x -> x.concat("_fastqc.html"));
            // System.out.println("IN ANALYSIS: "+fileNameWithoutExtension);
            resultsCombo.setModel(new DefaultComboBoxModel<String>(fileNameWithoutExtension.toArray(new String[0])));
            resultsCombo.setEnabled(true);
            flag = true;
            int r = process.waitFor(); // Let the process finish.
            if (r == 0) { // No error
                // run cmd2.
            }
            process.destroy();
        } catch (IOException | InterruptedException ex) {
            App.LOGGER.error("ERROR in FastQC Analysis: " + ex);
            ex.printStackTrace();
            App.textArea.append("ERROR in FastQC Analysis: " + ex);
        }
         */
    }//GEN-LAST:event_analyseBtnActionPerformed

    private void fastqcResultComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fastqcResultComboActionPerformed
        if (fastqcResultCombo.isEnabled() == true) {
            try {

                //File htmlFile = new File(FilenameUtils.removeExtension(allFiles[resultsCombo.getSelectedIndex()].getAbsolutePath()) + ("_fastqc.html"));
                File htmlFile = new File(outputTxt.getText() + fastqcResultCombo.getSelectedItem());

                Desktop.getDesktop().browse(htmlFile.toURI());
            } catch (IOException ex) {
                App.LOGGER.error("\r\nERROR in FASTQC Analysis: " + ex);
                ex.printStackTrace();
                App.textArea.append("\r\nERROR in FASTQC Analysis: " + ex);
            }
        }

    }//GEN-LAST:event_fastqcResultComboActionPerformed

    private void jLabel14MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel14MouseClicked

        try {
            Desktop.getDesktop().open(new File(System.getProperty("user.dir").concat("/bin/FastQC_Manual.pdf")));
        } catch (IOException ex) {
            App.LOGGER.error("\r\nERROR loading FASTQC mannual file: " + ex);
            App.textArea.append("\r\nERROR loading FASTQC mannual file: " + ex);
        }
    }//GEN-LAST:event_jLabel14MouseClicked

    private void browseOBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseOBtnActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new java.io.File("."));
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {

            String path = fileChooser.getSelectedFile().toString();

            outputTxt.setText(path + "/");

        }
    }//GEN-LAST:event_browseOBtnActionPerformed

    private void jLabel14MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel14MouseEntered
        jLabel14.setIcon(App.icons[1]);
    }//GEN-LAST:event_jLabel14MouseEntered

    private void jLabel14MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel14MouseExited
        jLabel14.setIcon(App.icons[0]);
    }//GEN-LAST:event_jLabel14MouseExited

    private void fastqTxtKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fastqTxtKeyPressed
        if (evt.getKeyChar() == KeyEvent.VK_DELETE) {
            listModelfasta.remove(fastqTxt.getSelectedIndex());

            fastqcResultCombo.setEnabled(false);
            fastqcResultCombo.removeAllItems();
            dm.setRowCount(0);
            // dm.fireTableDataChanged();

        }
    }//GEN-LAST:event_fastqTxtKeyPressed

    private void browseBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_browseBtnMouseClicked

    }//GEN-LAST:event_browseBtnMouseClicked

    private void warningLabelPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_warningLabelPropertyChange
        jScrollPane2.getViewport().setViewPosition(new Point(0, 0));
    }//GEN-LAST:event_warningLabelPropertyChange


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel FastQCPanel;
    private javax.swing.JButton analyseBtn;
    private javax.swing.JButton browseBtn;
    private javax.swing.JButton browseOBtn;
    private javax.swing.JList<String> fastqTxt;
    private javax.swing.JComboBox<String> fastqcResultCombo;
    private javax.swing.JTable fastqcTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTextField outputTxt;
    private javax.swing.JLabel warningLabel;
    // End of variables declaration//GEN-END:variables
}
