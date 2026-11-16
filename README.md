# Quarterly Earnings App

A Java Swing desktop application for organizing company earnings by sector and
quarter. The table supports editing, sorting, undo and redo, CSV file
operations, and copying, cutting, or pasting company rows. A tree alongside
the table groups companies by sector.

## Run

From the project directory:

```bash
javac *.java
java Main
```

The application starts with an empty list. Use the **File** menu to create,
open, save, or save as a CSV file. The **Edit** menu provides undo/redo,
clipboard actions, and company add/edit/delete commands. The **View** menu
provides earnings display scales and sorting options.

## CSV format

Each record contains six fields in this order: company name, sector, Q1
earnings, Q2 earnings, Q3 earnings, and Q4 earnings. Files written by the app
include a header row; files opened by the app may omit it. Fields containing
commas, quotation marks, or line breaks are quoted and escaped using CSV
conventions.

## Java components

### `CompanyEarnings.java` — CompanyEarnings

The data object for one company's name, sector, and four quarterly earnings.
`getYearlyTotal()` sums the four quarters; `getCompanyName()` and
`getSector()` return the corresponding identifying information. The
constructor takes the name, sector, then Q1 through Q4 earnings.

### `EarningsTableModel.java` — EarningsTableModel

The Swing table model that owns the displayed company records and provides
editable company, sector, and quarterly earnings columns, plus a calculated
yearly-total column. It also maintains undo and redo history.

- The constructor accepts the initial company list and copies its records.
- `getRowCount()`, `getColumnCount()`, `getColumnName()`, `getColumnClass()`,
  `isCellEditable()`, and `getValueAt()` provide table structure and cell
  values. `setValueAt()` applies edits and updates the yearly total when a
  quarterly value changes.
- `getCompanyAt()` returns a record at a model row; `getCompanies()` returns
  an immutable list snapshot.
- `addCompany()`, `setCompanyAt()`, `removeCompanies()`, and
  `replaceCompanies()` add, replace, remove, or replace the model's records.
- `sortByColumn()` sorts records in ascending order by a table column.
- `canUndo()` and `canRedo()` report whether history is available;
  `undo()` and `redo()` restore a saved model state.
- `allCompanies()` returns a company-name-to-sector map.
- `saveUndoState()` records a snapshot before a change. `copyCompanies()`
  makes defensive copies, while `getQuarter()` and `setQuarter()` read and
  update the quarter corresponding to a table column.

### `CompanyTreeModel.java` — CompanyTreeModel

The Swing tree model that groups company names under sector nodes. Its
constructor populates the tree from an `EarningsTableModel`.
`updateCompanyName()` updates an existing company label, `refresh()` rebuilds
the tree after model changes, and `populateCompanyTree()` creates sector and
company nodes from the current records.

### `CsvIO.java` — CsvIO

A non-instantiable utility for importing, exporting, parsing, and formatting
CSV data. `read()` loads company records from a path and `write()` saves them
with a header. `parse()` converts CSV text into rows and fields, including
quoted fields and escaped quotation marks; `formatRecord()` encodes one row.
The private `appendRecord()` helper performs the CSV escaping used by writing
and formatting.

### `QuarterlyEarningsApp.java` — QuarterlyEarningsApp

The main Swing window. Its constructor assembles the table, sector tree,
header, menus, and model-change listener.

- `createMenuBar()` builds the File, Edit, and View menus; `addSortItem()`
  adds a column-sorting command.
- `newFile()`, `openFile()`, `saveFile()`, `saveFileAs()`, and `writeFile()`
  manage the current CSV document. `selectedPath()` resolves a dialog
  selection, and `ensureCsvExtension()` adds the CSV suffix when required.
- `addCompany()`, `editSelectedCompany()`, and `deleteSelectedCompanies()`
  manage company records. `showCompanyDialog()` collects a new or edited
  company, and `selectedModelRows()` translates selected view rows for model
  operations.
- `copySelectedCompanies()`, `cutSelectedCompanies()`, and
  `pasteCompanies()` exchange CSV company rows with the system clipboard.
  `selectedViewRows()` provides the selected row indices for clipboard
  handling.
- `setCurrencyScale()` changes the displayed earnings units, and
  `showError()` displays an error message.
- The currency cell renderer's `setValue()` formats each displayed numeric
  value using the active scale.

### `Main.java` — Main

The application entry point. `main()` creates an empty
`EarningsTableModel` and constructs and displays `QuarterlyEarningsApp` on
Swing's event dispatch thread. The `args` parameter is reserved for
command-line arguments and is currently unused.
