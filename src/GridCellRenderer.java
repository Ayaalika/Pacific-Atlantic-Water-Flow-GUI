import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class GridCellRenderer extends DefaultTableCellRenderer {
    private final Set<Point> pathSet = new HashSet<>();

    public void setPath(java.util.List<Cell> p) {
        pathSet.clear();
        for (Cell c : p) pathSet.add(new Point(c.j, c.i));
    }

    public void clearPath() { pathSet.clear(); }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        setHorizontalAlignment(CENTER);
        setBackground(Color.WHITE);

        Object val = table.getModel().getValueAt(row, column);
        if (val instanceof Number) setText(String.valueOf(((Number) val).intValue()));

        PacificAtlanticGUI frame = (PacificAtlanticGUI) SwingUtilities.getWindowAncestor(table);
        if (frame != null && frame.G != null) {
            Cell cell = frame.G[row][column];
            if (cell.isDesert) setBackground(new Color(0xF0E68C));
            else if (cell.isRiver) setBackground(new Color(0x87CEFA));
            else if (pathSet.contains(new Point(column, row))) setBackground(new Color(0x008080));
            else setBackground(new Color(0xFFFFFF));
        }
        return this;
    }
}
