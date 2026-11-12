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
        setJMenuBar(createMenuBar());

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

    private static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem saveAsItem = new JMenuItem("Save As");
        JMenuItem copyItem = new JMenuItem("Copy");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(copyItem);
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        JMenuItem addItem = new JMenuItem("Add company");
        JMenuItem editItem = new JMenuItem("Edit company");
        JMenuItem deleteItem = new JMenuItem("Delete company");
        JMenuItem undoItem = new JMenuItem("Undo");
        JMenuItem redoItem = new JMenuItem("Redo");
        
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        menuBar.add(editMenu);
        editMenu.add(addItem);
        editMenu.add(editItem);
        editMenu.add(deleteItem);

        // View menu
        JMenu viewMenu = new JMenu("View");
        JMenu displayMenu = new JMenu("Currency display type");
        JMenuItem zerosItem = new JMenuItem("000");
        JMenuItem thousandsItem = new JMenuItem("Thousands");
        JMenuItem millionsItem = new JMenuItem("Millions");
        displayMenu.add(millionsItem);
        displayMenu.add(zerosItem);
        displayMenu.add(thousandsItem);
        viewMenu.add(displayMenu);
        JMenu sortMenu = new JMenu("Sort by");
        JMenuItem sortByNameItem = new JMenuItem("Company Name");
        JMenuItem sortByQ1Item = new JMenuItem("Quarter 1");
        JMenuItem sortByQ2Item = new JMenuItem("Quarter 2");
        JMenuItem sortByQ3Item = new JMenuItem("Quarter 3");
        JMenuItem sortByQ4Item = new JMenuItem("Quarter 4");
        JMenuItem sortByYearlyItem = new JMenuItem("Yearly Earnings");
        sortMenu.add(sortByNameItem);
        sortMenu.add(sortByQ1Item);
        sortMenu.add(sortByQ2Item);
        sortMenu.add(sortByQ3Item);
        sortMenu.add(sortByQ4Item);
        sortMenu.add(sortByYearlyItem);
        viewMenu.add(sortMenu);
        menuBar.add(viewMenu);

        return menuBar;
    }
}
