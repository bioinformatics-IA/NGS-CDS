/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package NGSGradle;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

public class HeaderRenderer extends DefaultTableCellRenderer {
    public HeaderRenderer() {
        setHorizontalAlignment(SwingConstants.CENTER); // Center align the text
        setVerticalAlignment(SwingConstants.CENTER); // Align text vertically
        setBorder(BorderFactory.createEtchedBorder()); // Add border
        setFont(new Font("Arial", Font.BOLD, 12)); // Increase font size for better visibility
        setPreferredSize(new Dimension(getPreferredSize().width, 60)); // Increase header height
  
    
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        // Wrap text in HTML for multi-line
        if (value != null) {
            label.setText("<html>" + value.toString().replace(" ", "<br>") + "</html>");
        }
        
        return label;
    }
}
