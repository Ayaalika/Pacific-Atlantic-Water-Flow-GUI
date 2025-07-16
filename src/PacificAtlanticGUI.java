import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class PacificAtlanticGUI extends JFrame {
    private int n = 5, m = 5;
    public Cell[][] G;
    private List<Cell> path = new ArrayList<>();

    private JSpinner rowsSpinner, colsSpinner;
    private JTable table;
    private GridCellRenderer renderer;
    private JTextArea outputArea;

    private enum Mode { DEFAULT, RIVER, DESERT }
    private Mode currentMode = Mode.DEFAULT;

    public PacificAtlanticGUI() {
        setTitle("Pacific Atlantic Path Finder");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initUI();
    }

    private void setMode(Mode mode) {
        currentMode = mode;
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(0xFFF0F5));
        setContentPane(root);

        Font modernFont = new Font("Segoe UI", Font.PLAIN, 14);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setBackground(new Color(0xFFF0F5));

        top.add(new JLabel("Rows:"));
        rowsSpinner = new JSpinner(new SpinnerNumberModel(n, 1, 100, 1));
        rowsSpinner.setFont(modernFont);
        top.add(rowsSpinner);

        top.add(new JLabel("Cols:"));
        colsSpinner = new JSpinner(new SpinnerNumberModel(m, 1, 100, 1));
        colsSpinner.setFont(modernFont);
        top.add(colsSpinner);

        top.add(modernButton("Apply Size", e -> applySize()));
        top.add(modernButton("Random Fill", e -> randomFill()));
        top.add(modernButton("Find Path", e -> runPathFinding()));

        top.add(modernButton("Set River", e -> setMode(Mode.RIVER)));
        top.add(modernButton("Set Desert", e -> setMode(Mode.DESERT)));
        top.add(modernButton("Default", e -> setMode(Mode.DEFAULT)));

        root.add(top, BorderLayout.NORTH);
        renderer = new GridCellRenderer();
        table = new JTable();
        table.setRowHeight(30);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,0));
        table.setDefaultRenderer(Integer.class, renderer);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col >= 0 && G != null) {
                    switch (currentMode) {
                        case RIVER -> {
                            G[row][col].isRiver = true;
                            G[row][col].isDesert = false;
                            JOptionPane.showMessageDialog(PacificAtlanticGUI.this, "Cell set to River.");
                        }
                        case DESERT -> {
                            G[row][col].isDesert = true;
                            G[row][col].isRiver = false;
                            JOptionPane.showMessageDialog(PacificAtlanticGUI.this, "Cell set to Desert.");
                        }
                        case DEFAULT -> {
                            G[row][col].isRiver = false;
                            G[row][col].isDesert = false;
                            JOptionPane.showMessageDialog(PacificAtlanticGUI.this, "Cell reset to normal.");
                        }
                    }
                    table.repaint();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Color.PINK));
        root.add(scroll, BorderLayout.CENTER);

        outputArea = new JTextArea(6, 40);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(0xFFF5FA));
        outputArea.setForeground(new Color(0x660033));
        outputArea.setBorder(BorderFactory.createTitledBorder("Path Output"));
        JScrollPane outScroll = new JScrollPane(outputArea);
        outScroll.setBorder(BorderFactory.createLineBorder(new Color(0xFFB6C1)));
        root.add(outScroll, BorderLayout.EAST);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(new Color(0xFFF0F5));

        JLabel legend = new JLabel("Legend: Blue=Path, Cyan=River, Yellow=Desert");
        legend.setFont(modernFont);
        legend.setForeground(new Color(0x660033));
        bottom.add(legend, BorderLayout.EAST);

        root.add(bottom, BorderLayout.SOUTH);

        applySize();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JButton modernButton(String text, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(new Color(0xD87093));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        btn.addActionListener(action);
        return btn;
    }

    private void applySize() {
        n = (Integer) rowsSpinner.getValue();
        m = (Integer) colsSpinner.getValue();
        path.clear();
        renderer.clearPath();
        G = new Cell[n][m];

        DefaultTableModel model = new DefaultTableModel(n, m) {
            @Override public Class<?> getColumnClass(int col) { return Integer.class; }
            @Override public boolean isCellEditable(int row, int col) { return true; }
        };
        table.setModel(model);
        table.setDefaultRenderer(Integer.class, renderer);

        for (int i = 0; i < n; i++) for (int j = 0; j < m; j++) {
            model.setValueAt(0, i, j);
            G[i][j] = new Cell(0, i, j);
        }

        for (int j = 0; j < m; j++) table.getColumnModel().getColumn(j).setPreferredWidth(50);

    }

    private void randomFill() {
        TableModel model = table.getModel();
        Random rnd = new Random();
        for (int i = 0; i < n; i++) for (int j = 0; j < m; j++) {
            int val = rnd.nextInt(100);
            model.setValueAt(val, i, j);
            G[i][j].height = val;
        }
    }

    private void runPathFinding() {
        long start = System.currentTimeMillis();
        readInput();

        FindingThePath solver = new FindingThePath(G);
        path = solver.solve();

        renderer.setPath(path);
        table.repaint();

        StringBuilder sb = new StringBuilder();
        for (Cell c : path) sb.append(String.format("(%d, %d)\n", c.i, c.j));
        outputArea.setText(sb.toString());
    }

    private void readInput() {
        path.clear();
        TableModel model = table.getModel();
        for (int i = 0; i < n; i++) for (int j = 0; j < m; j++) {
            Object v = model.getValueAt(i, j);
            int h = (v instanceof Number) ? ((Number) v).intValue() : 0;
            G[i][j].height = h;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PacificAtlanticGUI::new);
    }
}
