import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

/**
 * Supplies company earnings data to the Swing table and manages edits,
 * sorting, and undo/redo history for that data.
 */
public class EarningsTableModel extends AbstractTableModel {
    private final String[] columnNames = {
        "Company Name", "Sector", "Q1 Earnings", "Q2 Earnings",
        "Q3 Earnings", "Q4 Earnings", "Yearly Total"
    };
    private List<CompanyEarnings> data;
    private final Deque<List<CompanyEarnings>> undoHistory = new ArrayDeque<>();
    private final Deque<List<CompanyEarnings>> redoHistory = new ArrayDeque<>();

    /**
     * Creates a table model containing a defensive copy of the supplied
     * company records.
     *
     * @param data the initial company records to display
     */
    public EarningsTableModel(List<CompanyEarnings> data) {
        this.data = copyCompanies(data);
    }

    /**
     * Gets the number of company rows currently in the model.
     *
     * @return the number of company records
     */
    @Override
    public int getRowCount() {
        return data.size();
    }

    /**
     * Gets the number of columns, including the calculated yearly total.
     *
     * @return the number of table columns
     */
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Gets the display label for a table column.
     *
     * @param col the zero-based column index
     * @return the column label
     */
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    /**
     * Gets the value type used by the table for a column.
     *
     * @param columnIndex the zero-based column index
     * @return {@code String.class} for company and sector columns, otherwise
     *         {@code Double.class} for earnings columns
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex < 2 ? String.class : Double.class;
    }

    /**
     * Determines whether a table cell can be edited. Company, sector, and
     * quarterly earnings cells are editable; yearly totals are calculated.
     *
     * @param rowIndex the zero-based row index
     * @param columnIndex the zero-based column index
     * @return {@code true} when the column is editable
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex >= 0 && columnIndex < 6;
    }

    /**
     * Gets the company record at a model row.
     *
     * @param rowIndex the zero-based model row index
     * @return the company record at that row
     */
    public CompanyEarnings getCompanyAt(int rowIndex) {
        return data.get(rowIndex);
    }

    /**
     * Gets the row index of a company record.
     *
     * @param company the company record to locate

     * @return the integer row index of the company, or null if it doesn't exist
     */
    public Integer getRowIndexByCompanyEarnings(CompanyEarnings company) {
        company = (data.contains(company)) ? company : null;
        return data.indexOf(company);
    }

    /**
     * Gets the row index of a company record by company name.
     *
     * @param companyName the company record to locate

     * @return the integer row index of the company, or null if it doesn't exist
     */
    public Integer getRowIndexByCompanyName(String companyName) {
        return data.stream()
                .map(CompanyEarnings::getCompanyName)
                .collect(Collectors.toList())
                .indexOf(companyName);
    }

    /**
     * Gets an immutable list containing the current company records.
     *
     * @return an unmodifiable snapshot of the model's company list
     */
    public List<CompanyEarnings> getCompanies() {
        return List.copyOf(data);
    }

    /**
     * Gets the value displayed at a table cell.
     *
     * @param rowIndex the zero-based model row index
     * @param columnIndex the zero-based model column index
     * @return the company name, sector, quarterly earnings, or yearly total
     *         for the requested cell
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CompanyEarnings company = data.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> company.companyName;
            case 1 -> company.sector;
            case 2 -> company.q1;
            case 3 -> company.q2;
            case 4 -> company.q3;
            case 5 -> company.q4;
            case 6 -> company.getYearlyTotal();
            default -> null;
        };
    }

    /**
     * Updates an editable table cell and notifies table listeners. Invalid
     * earnings text is rejected with an error message.
     *
     * @param value the new cell value supplied by the table editor
     * @param rowIndex the zero-based model row index
     * @param columnIndex the zero-based model column index
     */
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= data.size() || columnIndex < 0 || columnIndex > 5) {
            return;
        }

        CompanyEarnings company = data.get(rowIndex);
        String text = value == null ? "" : value.toString();
        if (columnIndex < 2) {
            String oldValue = columnIndex == 0 ? company.companyName : company.sector;
            if (Objects.equals(oldValue, text)) {
                return;
            }
            saveUndoState();
            if (columnIndex == 0) {
                company.companyName = text;
            } else {
                company.sector = text;
            }
        } else {
            final double amount;
            try {
                amount = Double.parseDouble(text);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Please enter a valid number for earnings.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (Double.compare(getQuarter(company, columnIndex), amount) == 0) {
                return;
            }
            saveUndoState();
            setQuarter(company, columnIndex, amount);
        }

        fireTableCellUpdated(rowIndex, columnIndex);
        if (columnIndex >= 2) {
            fireTableCellUpdated(rowIndex, 6);
        }
    }

    /**
     * Adds a copy of a company record to the end of the model.
     *
     * @param company the company record to add
     */
    public void addCompany(CompanyEarnings company) {
        saveUndoState();
        int row = data.size();
        data.add(copyCompanies(List.of(company)).get(0));
        fireTableRowsInserted(row, row);
    }

    /**
     * Replaces the company record at a model row with a copy of the supplied
     * record.
     *
     * @param rowIndex the zero-based model row to replace
     * @param company the replacement company record
     * @throws IndexOutOfBoundsException if {@code rowIndex} is not a valid row
     */
    public void setCompanyAt(int rowIndex, CompanyEarnings company) {
        if (rowIndex < 0 || rowIndex >= data.size()) {
            throw new IndexOutOfBoundsException("Invalid company row: " + rowIndex);
        }
        saveUndoState();
        data.set(rowIndex, copyCompanies(List.of(company)).get(0));
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    /**
     * Removes the companies at the supplied model rows. Duplicate and invalid
     * indices are ignored; rows are removed from highest to lowest index so
     * earlier indices remain stable.
     *
     * @param rowIndices the zero-based model row indices to remove
     */
    public void removeCompanies(int[] rowIndices) {
        List<Integer> rows = java.util.Arrays.stream(rowIndices)
                .distinct()
                .boxed()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        if (rows.isEmpty()) {
            return;
        }
        saveUndoState();
        for (int row : rows) {
            if (row >= 0 && row < data.size()) {
                data.remove(row);
            }
        }
        fireTableDataChanged();
    }

    /**
     * Replaces all records with copies of the supplied company list.
     *
     * @param companies the records that will become the model's contents
     */
    public void replaceCompanies(List<CompanyEarnings> companies) {
        saveUndoState();
        data = copyCompanies(companies);
        fireTableDataChanged();
    }

    /**
     * Sorts company records in ascending order by the selected column.
     *
     * @param columnIndex the zero-based model column to sort by, from 0 to 6
     */
    public void sortByColumn(int columnIndex) {
        if (columnIndex < 0 || columnIndex > 6 || data.size() < 2) {
            return;
        }
        saveUndoState();
        Comparator<CompanyEarnings> comparator = switch (columnIndex) {
            case 0 -> Comparator.comparing(c -> c.companyName, String.CASE_INSENSITIVE_ORDER);
            case 1 -> Comparator.comparing(c -> c.sector, String.CASE_INSENSITIVE_ORDER);
            case 2 -> Comparator.comparingDouble(c -> c.q1);
            case 3 -> Comparator.comparingDouble(c -> c.q2);
            case 4 -> Comparator.comparingDouble(c -> c.q3);
            case 5 -> Comparator.comparingDouble(c -> c.q4);
            default -> Comparator.comparingDouble(CompanyEarnings::getYearlyTotal);
        };
        data.sort(comparator);
        fireTableDataChanged();
    }

    /**
     * Checks whether a previous model state is available to restore.
     *
     * @return {@code true} if an undo operation is available
     */
    public boolean canUndo() {
        return !undoHistory.isEmpty();
    }

    /**
     * Checks whether a state can be restored after an undo operation.
     *
     * @return {@code true} if a redo operation is available
     */
    public boolean canRedo() {
        return !redoHistory.isEmpty();
    }

    /**
     * Restores the most recently saved model state, if one is available.
     */
    public void undo() {
        if (canUndo()) {
            redoHistory.push(copyCompanies(data));
            data = undoHistory.pop();
            fireTableDataChanged();
        }
    }

    /**
     * Restores the most recently undone model state, if one is available.
     */
    public void redo() {
        if (canRedo()) {
            undoHistory.push(copyCompanies(data));
            data = redoHistory.pop();
            fireTableDataChanged();
        }
    }

    /**
     * Creates a mapping from each company name to its sector. When names
     * repeat, the sector from the last matching record is retained.
     *
     * @return a map of company names to sectors
     */
    public Map<String, String> allCompanies() {
        return data.stream().collect(Collectors.toMap(
                CompanyEarnings::getCompanyName,
                CompanyEarnings::getSector,
                (existing, replacement) -> replacement));
    }

    /**
     * Saves the current records for undo and clears any redo states.
     */
    private void saveUndoState() {
        undoHistory.push(copyCompanies(data));
        redoHistory.clear();
    }

    /**
     * Makes a new list of company records with each record copied.
     *
     * @param companies the company records to copy
     * @return a mutable list containing copies of the supplied records
     */
    private static List<CompanyEarnings> copyCompanies(List<CompanyEarnings> companies) {
        List<CompanyEarnings> copy = new ArrayList<>(companies.size());
        for (CompanyEarnings company : companies) {
            copy.add(new CompanyEarnings(
                    company.companyName, company.sector,
                    company.q1, company.q2, company.q3, company.q4));
        }
        return copy;
    }

    /**
     * Gets the earnings value represented by a quarterly table column.
     *
     * @param company the company record to read
     * @param columnIndex the quarterly column index, from 2 to 5
     * @return the earnings for the corresponding quarter
     * @throws IllegalArgumentException if the column is not a quarterly column
     */
    private static double getQuarter(CompanyEarnings company, int columnIndex) {
        return switch (columnIndex) {
            case 2 -> company.q1;
            case 3 -> company.q2;
            case 4 -> company.q3;
            case 5 -> company.q4;
            default -> throw new IllegalArgumentException("Not a quarter column: " + columnIndex);
        };
    }

    /**
     * Sets the earnings value represented by a quarterly table column.
     *
     * @param company the company record to update
     * @param columnIndex the quarterly column index, from 2 to 5
     * @param value the earnings amount to store
     * @throws IllegalArgumentException if the column is not a quarterly column
     */
    private static void setQuarter(CompanyEarnings company, int columnIndex, double value) {
        switch (columnIndex) {
            case 2 -> company.q1 = value;
            case 3 -> company.q2 = value;
            case 4 -> company.q3 = value;
            case 5 -> company.q4 = value;
            default -> throw new IllegalArgumentException("Not a quarter column: " + columnIndex);
        }
    }
}
