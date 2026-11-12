import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/* Represents the main application window for displaying quarterly and yearly earnings data. 
*
* @param EarningsTableModel model: The table model containing the earnings data to be displayed in the JTable.
*/
public class QuarterlyEarningsApp extends JFrame {
    
    public QuarterlyEarningsApp(EarningsTableModel model) {
        // Set up the main app window
        setTitle("Corporate Quarterly Earnings");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // Center the window

        // Set up the menu bar
        EarningsAppMenuBar menuBar = new EarningsAppMenuBar();
        setJMenuBar(menuBar);

        // JTable MUST use a table model to display data. 
        // The table model is responsible for providing the data to the JTable.
        JTable earningsTable = new JTable(model);

        // Customize table appearence for readability
        earningsTable.setFillsViewportHeight(true);
        earningsTable.setRowHeight(25);
        earningsTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        earningsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));

        // Add the table to a scroll frame
        JScrollPane scrollPane = new JScrollPane(earningsTable);

        // Layout placement
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        // Top header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(45, 52, 54));
        JLabel headerLabel = new JLabel("Corporate Quarterly Earnings Table");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);
    }

    private class EarningsAppMenuBar extends JMenuBar {
        public EarningsAppMenuBar() {
            JMenu fileMenu = new JMenu("File");
            JMenuItem exitItem = new JMenuItem("Exit");
            exitItem.addActionListener(e -> System.exit(0));
            fileMenu.add(exitItem);
            add(fileMenu);
        }
    }
}
