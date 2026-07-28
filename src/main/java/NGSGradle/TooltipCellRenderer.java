/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package NGSGradle;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Map;

public class TooltipCellRenderer extends DefaultTableCellRenderer {
    private final Map<Integer, String> tooltips; // Store tooltips

    public TooltipCellRenderer(Map<Integer, String> tooltips) {
        this.tooltips = tooltips;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (tooltips.containsKey(column) && value != null && !value.toString().equalsIgnoreCase("PASS")) {
            label.setToolTipText(tooltips.get(column)); // Set tooltip for failing cells
        } else {
            label.setToolTipText(null); // No tooltip for "PASS"
        }

        return label;
    }
}
