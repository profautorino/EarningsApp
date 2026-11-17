import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Main Swing window for viewing and editing company quarterly earnings.
 * The window coordinates the table and sector tree, file operations,
 * clipboard actions, sorting, undo/redo, and currency display settings.
 */
public class QuarterlyEarningsApp extends JFrame {
    private final EarningsTableModel earningsModel;
    private final CompanyTreeModel treeModel;
    private final JTable earningsTable;
    private Path currentFile;

    /**
     * Builds and configures the earnings window using the supplied table
     * model as its data source.
     *
     * @param model the table model whose company records are displayed
     */
    public QuarterlyEarningsApp(EarningsTableModel model) {
        this.earningsModel = model;
        this.treeModel = new CompanyTreeModel(model, "All Companies");

        setTitle("Corporate Quarterly Earnings");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 600);
        setLocationRelativeTo(null);

        JTree companyTree = new JTree(treeModel);
        model.addTableModelListener(event -> {
            int row = event.getFirstRow();
            int column = event.getColumn();
            if (row < 0 || column == -1) {
                treeModel.refresh(model);
            } else if (column == 0) {
                treeModel.updateCompanyName(model.getCompanyAt(row));
            } else if (column == 1) {
                treeModel.refresh(model);
            }
        });
        companyTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) companyTree.getLastSelectedPathComponent();
            if (selectedNode != null) {
                selectRowInTable(selectedNode);
            }
        });

        earningsTable = new JTable(model);
        earningsTable.setAutoCreateRowSorter(true);
        earningsTable.setFillsViewportHeight(true);
        earningsTable.setRowHeight(25);
        earningsTable.getTableHeader().setReorderingAllowed(false);
        earningsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        earningsTable.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JScrollPane treeScrollPane = new JScrollPane(companyTree);
        treeScrollPane.setPreferredSize(new Dimension(200, 600));
        JScrollPane tableScrollPane = new JScrollPane(earningsTable);
        tableScrollPane.setPreferredSize(new Dimension(1000, 600));

        setLayout(new BorderLayout());
        add(tableScrollPane, BorderLayout.CENTER);
        add(treeScrollPane, BorderLayout.WEST);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(45, 52, 54));
        JLabel headerLabel = new JLabel("Corporate Quarterly Earnings Table");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);
        setJMenuBar(createMenuBar());
    }

    /**
     * Creates the File, Edit, and View menus and connects their actions to the
     * corresponding application operations.
     *
     * @return the configured menu bar for this window
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem saveAsItem = new JMenuItem("Save As");
        JMenuItem exitItem = new JMenuItem("Exit");
        newItem.addActionListener(event -> newFile());
        openItem.addActionListener(event -> openFile());
        saveItem.addActionListener(event -> saveFile());
        saveAsItem.addActionListener(event -> saveFileAs());
        exitItem.addActionListener(event -> dispose());
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        JMenuItem undoItem = new JMenuItem("Undo");
        JMenuItem redoItem = new JMenuItem("Redo");
        JMenuItem cutItem = new JMenuItem("Cut");
        JMenuItem copyItem = new JMenuItem("Copy");
        JMenuItem pasteItem = new JMenuItem("Paste");
        JMenuItem addItem = new JMenuItem("Add company");
        JMenuItem editItem = new JMenuItem("Edit company");
        JMenuItem deleteItem = new JMenuItem("Delete company");
        undoItem.addActionListener(event -> earningsModel.undo());
        redoItem.addActionListener(event -> earningsModel.redo());
        cutItem.addActionListener(event -> cutSelectedCompanies());
        copyItem.addActionListener(event -> copySelectedCompanies());
        pasteItem.addActionListener(event -> pasteCompanies());
        addItem.addActionListener(event -> addCompany());
        editItem.addActionListener(event -> editSelectedCompany());
        deleteItem.addActionListener(event -> deleteSelectedCompanies());
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.addSeparator();
        editMenu.add(addItem);
        editMenu.add(editItem);
        editMenu.add(deleteItem);
        menuBar.add(editMenu);

        JMenu viewMenu = new JMenu("View");
        JMenu displayMenu = new JMenu("Currency display type");
        JMenuItem decimalsItem = new JMenuItem("Decimals");
        JMenuItem thousandsItem = new JMenuItem("Thousands");
        JMenuItem millionsItem = new JMenuItem("Millions");
        decimalsItem.addActionListener(event -> setCurrencyScale(1, ""));
        thousandsItem.addActionListener(event -> setCurrencyScale(1_000, " (thousands)"));
        millionsItem.addActionListener(event -> setCurrencyScale(1_000_000, " (millions)"));
        displayMenu.add(decimalsItem);
        displayMenu.add(thousandsItem);
        displayMenu.add(millionsItem);
        viewMenu.add(displayMenu);

        JMenu sortMenu = new JMenu("Sort by");
        addSortItem(sortMenu, "Company Name", 0);
        addSortItem(sortMenu, "Yearly Earnings", 6);
        addSortItem(sortMenu, "Quarter 1", 2);
        addSortItem(sortMenu, "Quarter 2", 3);
        addSortItem(sortMenu, "Quarter 3", 4);
        addSortItem(sortMenu, "Quarter 4", 5);
        viewMenu.add(sortMenu);
        menuBar.add(viewMenu);
        return menuBar;
    }

    /**
     * Adds an ascending-sort command to a menu for the given table column.
     *
     * @param menu the menu that will contain the command
     * @param title the command's display label
     * @param modelColumn the zero-based model column to sort
     */
    private void addSortItem(JMenu menu, String title, int modelColumn) {
        JMenuItem item = new JMenuItem(title);
        item.addActionListener(event -> {
            earningsModel.sortByColumn(modelColumn);
            if (earningsTable.getRowSorter() instanceof TableRowSorter<?>) {
                TableRowSorter<?> sorter = (TableRowSorter<?>) earningsTable.getRowSorter();
                sorter.setSortKeys(List.of(new RowSorter.SortKey(modelColumn, SortOrder.ASCENDING)));
            }
        });
        menu.add(item);
    }

    /**
     * Starts a new empty earnings list after asking before discarding existing records.
     */
    private void newFile() {
        if (earningsModel.getRowCount() > 0) {
            int answer = JOptionPane.showConfirmDialog(
                    this, "Clear the current earnings list?", "New",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
        }
        earningsModel.replaceCompanies(List.of());
        currentFile = null;
    }

    /**
     * Prompts for a CSV file, loads its records into the model, and reports
     * file or data errors to the user.
     */
    private void openFile() {
        FileDialog dialog = new FileDialog(this, "Open CSV", FileDialog.LOAD);
        dialog.setFilenameFilter((directory, name) ->
                name.toLowerCase(Locale.ROOT).endsWith(".csv"));
        if (currentFile != null && currentFile.getParent() != null) {
            dialog.setDirectory(currentFile.getParent().toString());
        }
        dialog.setVisible(true);
        if (dialog.getFile() == null) {
            return;
        }
        Path selected = selectedPath(dialog);
        try {
            earningsModel.replaceCompanies(CsvIO.read(selected));
            currentFile = selected;
        } catch (IOException | RuntimeException e) {
            showError("Could not open CSV file: " + e.getMessage());
        }
    }

    /**
     * Saves the current records to the current file, or opens Save As when no
     * file has been selected yet.
     */
    private void saveFile() {
        if (currentFile == null) {
            saveFileAs();
            return;
        }
        writeFile(currentFile);
    }

    /**
     * Prompts for a destination and saves the current records there, adding a
     * CSV extension when needed and confirming before overwriting a file.
     */
    private void saveFileAs() {
        FileDialog dialog = new FileDialog(this, "Save CSV", FileDialog.SAVE);
        dialog.setFile(currentFile == null ? "earnings.csv" : currentFile.getFileName().toString());
        if (currentFile != null && currentFile.getParent() != null) {
            dialog.setDirectory(currentFile.getParent().toString());
        }
        dialog.setVisible(true);
        if (dialog.getFile() == null) {
            return;
        }
        Path selected = ensureCsvExtension(selectedPath(dialog));
        if (java.nio.file.Files.exists(selected)) {
            int answer = JOptionPane.showConfirmDialog(
                    this, "Overwrite " + selected.getFileName() + "?", "Confirm Save",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
        }
        if (writeFile(selected)) {
            currentFile = selected;
        }
    }

    /**
     * Writes the current model records to a CSV file and displays an error if
     * writing fails.
     *
     * @param path the destination path
     * @return {@code true} if the records were written successfully;
     *         {@code false} otherwise
     */
    private boolean writeFile(Path path) {
        try {
            CsvIO.write(path, earningsModel.getCompanies());
            return true;
        } catch (IOException | RuntimeException e) {
            showError("Could not save CSV file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Resolves the selected file dialog filename against its selected
     * directory when one is provided.
     *
     * @param dialog the file dialog containing the selected file and directory
     * @return the selected file path
     */
    private Path selectedPath(FileDialog dialog) {
        Path file = Path.of(dialog.getFile());
        String directory = dialog.getDirectory();
        return directory == null ? file : Path.of(directory).resolve(file);
    }

    /**
     * Ensures that a selected file path ends with the {@code .csv} extension.
     *
     * @param path the original destination path
     * @return the original path if it already has a CSV extension, otherwise
     *         a sibling path with {@code .csv} appended
     */
    private Path ensureCsvExtension(Path path) {
        return path.getFileName().toString().toLowerCase().endsWith(".csv")
                ? path
                : path.resolveSibling(path.getFileName() + ".csv");
    }

    /**
     * Prompts for a new company record and adds it to the table model if the
     * dialog is accepted.
     */
    private void addCompany() {
        CompanyEarnings company = showCompanyDialog(null, "Add Company");
        if (company != null) {
            earningsModel.addCompany(company);
        }
    }

    /**
     * Prompts to edit the selected company and replaces its model record if
     * the dialog is accepted.
     */
    private void editSelectedCompany() {
        int viewRow = earningsTable.getSelectedRow();
        if (viewRow < 0) {
            showError("Select a company to edit.");
            return;
        }
        int modelRow = earningsTable.convertRowIndexToModel(viewRow);
        CompanyEarnings original = earningsModel.getCompanyAt(modelRow);
        CompanyEarnings edited = showCompanyDialog(original, "Edit Company");
        if (edited != null) {
            earningsModel.setCompanyAt(modelRow, edited);
        }
    }

    /**
     * Shows a dialog for creating or editing a company and validates the
     * quarterly earnings fields.
     *
     * @param company the record to edit, or {@code null} to create a new record
     * @param title the dialog title
     * @return the entered company record if accepted, or {@code null} if
     *         cancelled
     */
    private CompanyEarnings showCompanyDialog(CompanyEarnings company, String title) {
        JTextField nameField = new JTextField(company == null ? "" : company.getCompanyName());
        JTextField sectorField = new JTextField(company == null ? "" : company.getSector());
        JTextField[] earningsFields = new JTextField[4];
        double[] values = company == null
                ? new double[] {0, 0, 0, 0}
                : new double[] {company.q1, company.q2, company.q3, company.q4};
        String[] labels = {"Company Name", "Sector", "Q1 Earnings", "Q2 Earnings", "Q3 Earnings", "Q4 Earnings"};

        JPanel panel = new JPanel(new java.awt.GridLayout(0, 2, 6, 6));
        panel.add(new JLabel(labels[0]));
        panel.add(nameField);
        panel.add(new JLabel(labels[1]));
        panel.add(sectorField);
        for (int i = 0; i < earningsFields.length; i++) {
            earningsFields[i] = new JTextField(Double.toString(values[i]));
            panel.add(new JLabel(labels[i + 2]));
            panel.add(earningsFields[i]);
        }

        while (JOptionPane.showConfirmDialog(
                this, panel, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                return new CompanyEarnings(
                        nameField.getText().trim(),
                        sectorField.getText().trim(),
                        Double.parseDouble(earningsFields[0].getText().trim()),
                        Double.parseDouble(earningsFields[1].getText().trim()),
                        Double.parseDouble(earningsFields[2].getText().trim()),
                        Double.parseDouble(earningsFields[3].getText().trim()));
            } catch (NumberFormatException e) {
                showError("Enter a valid number for each quarter's earnings.");
            }
        }
        return null;
    }

    /**
     * Confirms and removes all currently selected companies.
     */
    private void deleteSelectedCompanies() {
        int[] selected = selectedModelRows();
        if (selected.length == 0) {
            showError("Select at least one company to delete.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(
                this, "Delete " + selected.length + " selected company row(s)?",
                "Delete Companies", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            earningsModel.removeCompanies(selected);
        }
    }

    /**
     * Converts the selected table view row indices to model row indices.
     *
     * @return the selected zero-based model row indices
     */
    private int[] selectedModelRows() {
        int[] rows = earningsTable.getSelectedRows();
        for (int i = 0; i < rows.length; i++) {
            rows[i] = earningsTable.convertRowIndexToModel(rows[i]);
        }
        return rows;
    }

    /**
     * Encodes the selected companies as CSV and copies them to the system
     * clipboard.
     *
     * @return {@code true} if the selected data was copied successfully;
     *         {@code false} if no companies were selected or clipboard access
     *         failed
     */
    private boolean copySelectedCompanies() {
        List<Integer> rows = selectedViewRows();
        if (rows.isEmpty()) {
            showError("Select at least one company to copy.");
            return false;
        }
        StringBuilder text = new StringBuilder();
        for (int viewRow : rows) {
            CompanyEarnings company = earningsModel.getCompanyAt(earningsTable.convertRowIndexToModel(viewRow));
            text.append(CsvIO.formatRecord(List.of(
                    company.getCompanyName(),
                    company.getSector(),
                    Double.toString(company.q1),
                    Double.toString(company.q2),
                    Double.toString(company.q3),
                    Double.toString(company.q4))));
            text.append(System.lineSeparator());
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(text.toString()), null);
            return true;
        } catch (HeadlessException | IllegalStateException e) {
            showError("Could not access the system clipboard: " + e.getMessage());
            return false;
        }
    }

    /**
     * Copies the selected companies to the clipboard and removes them from the
     * model if copying succeeds.
     */
    private void cutSelectedCompanies() {
        int[] rows = selectedModelRows();
        if (rows.length == 0) {
            showError("Select at least one company to cut.");
            return;
        }
        if (copySelectedCompanies()) {
            earningsModel.removeCompanies(rows);
        }
    }

    /**
     * Gets the selected table row indices in view order.
     *
     * @return the selected zero-based view row indices
     */
    private List<Integer> selectedViewRows() {
        List<Integer> rows = new ArrayList<>();
        for (int row : earningsTable.getSelectedRows()) {
            rows.add(row);
        }
        return rows;
    }

    /**
     * Parses CSV company records from the clipboard and appends them to the
     * current model contents.
     */
    private void pasteCompanies() {
        try {
            String text = (String) Toolkit.getDefaultToolkit().getSystemClipboard()
                    .getData(DataFlavor.stringFlavor);
            List<CompanyEarnings> companies = new ArrayList<>();
            List<List<String>> records = CsvIO.parse(text);
            boolean firstRecord = true;
            for (int i = 0; i < records.size(); i++) {
                List<String> fields = records.get(i);
                if (fields.size() == 1 && fields.get(0).isBlank()) {
                    continue;
                }
                if (firstRecord && !fields.isEmpty()
                        && fields.get(0).trim().equalsIgnoreCase("Company Name")) {
                    firstRecord = false;
                    continue;
                }
                firstRecord = false;
                if (fields.size() != 6) {
                    throw new IOException("Clipboard rows must contain 6 CSV fields.");
                }
                companies.add(new CompanyEarnings(
                        fields.get(0), fields.get(1),
                        Double.parseDouble(fields.get(2).trim()),
                        Double.parseDouble(fields.get(3).trim()),
                        Double.parseDouble(fields.get(4).trim()),
                        Double.parseDouble(fields.get(5).trim())));
            }
            if (companies.isEmpty()) {
                return;
            }
            List<CompanyEarnings> combined = new ArrayList<>(earningsModel.getCompanies());
            combined.addAll(companies);
            earningsModel.replaceCompanies(combined);
        } catch (HeadlessException | IllegalStateException | java.awt.datatransfer.UnsupportedFlavorException
                | IOException | NumberFormatException e) {
            showError("Could not paste company data: " + e.getMessage());
        }
    }

    /**
     * Configures numeric renderers for earnings columns and scales displayed
     * amounts into the selected units.
     *
     * @param scale the divisor applied to each earnings value
     * @param unit the unit label appended to earnings column headers
     */
    private void setCurrencyScale(double scale, String unit) {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            /**
             * Displays numeric earnings using the selected scale and number
             * format.
             *
             * @param value the cell value to render
             */
            @Override
            protected void setValue(Object value) {
                if (value instanceof Number) {
                    setText(format.format(((Number) value).doubleValue() / scale));
                } else {
                    super.setValue(value);
                }
            }
        };
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int column = 2; column < earningsModel.getColumnCount(); column++) {
            earningsTable.getColumnModel().getColumn(column).setCellRenderer(renderer);
            earningsTable.getColumnModel().getColumn(column).setHeaderValue(
                    earningsModel.getColumnName(column) + unit);
        }
        earningsTable.revalidate();
        earningsTable.repaint();
        earningsTable.getTableHeader().repaint();
    }

    /**
     * Displays an error dialog associated with this application window.
     *
     * @param message the error message to show
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Earnings App", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Selects the row in the JTable based on the node selected in CompanyTreeModel.
     *
     * @param node the node selected in the CompanyTreeModel
     */
    private void selectRowInTable(DefaultMutableTreeNode node) {
        String companyName =  (String) node.getUserObject();
        int modelRow = earningsModel.getRowIndexByCompanyName(companyName);
        int viewRow = earningsTable.convertRowIndexToView(modelRow);
        if (viewRow != -1) {
            earningsTable.setRowSelectionInterval(viewRow, viewRow);
        }
    }
}
