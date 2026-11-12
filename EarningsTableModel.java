import javax.swing.table.AbstractTableModel;
import javax.swing.JOptionPane;
import java.util.List;


/* Represents the table model for displaying earnings data 
*
* @param List<CompanyEarnings> data: The list of company earnings data to be displayed in the table.
* 
* @method getRowCount() Returns the number of rows in the table model.
* @method getColumnCount() Returns the number of columns in the table model.
* @method getColumnName(int col) Returns the name of the column at int col.
* @method getValueAt(int rowIndex, int columnIndex) Returns the value for the cell at columnIndex and rowIndex.
* @method setValueAt(Object value, int rowIndex, int columnIndex) Sets the value of a cell based on object class, 
* rowIndex and columnIndex. Also updates the table to reflect the changes.
*/
public class EarningsTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Company Name", "Q1 Earnings", "Q2 Earnings", "Q3 Earnings", "Q4 Earnings", "Yearly Total"};
    private List<CompanyEarnings> data;

    public EarningsTableModel(List<CompanyEarnings> data) {
        this.data = data;
    }

    public int getRowCount() {
        return data.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    // Returns the value for the cell at columnIndex and rowIndex.
    public Object getValueAt(int rowIndex, int columnIndex) {
        CompanyEarnings earnings = data.get(rowIndex);
        switch (columnIndex) {
            case 0: return earnings.companyName;
            case 1: return earnings.q1;
            case 2: return earnings.q2;
            case 3: return earnings.q3;
            case 4: return earnings.q4;
            case 5: return earnings.getYearlyTotal();
            default: return null;
        }
    }

    // Return the object class for a column at columnIndex.
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return String.class;
        } else {
            return Double.class; // forces numeric formatting for earnings columns
        }
    }

    // Indicates whether the cell at rowIndex and columnIndex is editable.
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return rowIndex >= 0 && columnIndex != 5; // only the yearly total cell is non-editable
    }

    // Indicates whether the column at columnIndex is editable.
    public boolean isColumnEditable(int columnIndex) {
        return columnIndex != 5; // only the yearly total cell is non-editable
    }

    // Sets the value of a cell based on object class, rowIndex and columnIndex.
    // If the value is a string, it updates the company name.
    // Otherwise, we attempt to update a cell within Q1-Q4 earnings.
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        CompanyEarnings row = data.get(rowIndex);
        Object valueObject = getColumnClass(columnIndex);

        if (columnIndex == 0 && valueObject == String.class) {
            row.companyName = (String) value;
        } else {
            try {
                double newValue = Double.parseDouble(value.toString());
                switch (columnIndex) {
                    case 1 -> row.q1 = newValue;
                    case 2 -> row.q2 = newValue;
                    case 3 -> row.q3 = newValue;
                    case 4 -> row.q4 = newValue;
                }
            } catch (NumberFormatException e) {
                // Handle invalid input gracefully
                JOptionPane.showMessageDialog(null, "Please enter a valid number for earnings.", 
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        // Update the table to reflect the changes
        fireTableCellUpdated(rowIndex, columnIndex);
        fireTableCellUpdated(rowIndex, 5); // Update the yearly total cell
    }
}

