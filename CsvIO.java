import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes company earnings records in UTF-8 comma-separated-value
 * files, and provides CSV parsing and formatting for clipboard operations.
 * This utility class is not intended to be instantiated.
 */
public final class CsvIO {
    private static final String[] HEADER = {
        "Company Name", "Sector", "Q1 Earnings", "Q2 Earnings", "Q3 Earnings", "Q4 Earnings"
    };

    /**
     * Prevents construction of this static utility class.
     */
    private CsvIO() {
    }

    /**
     * Reads company records from a CSV file. A leading UTF-8 byte-order mark
     * and an optional first-row header are accepted.
     *
     * @param path the CSV file to read
     * @return the company records parsed from the file, in file order
     * @throws IOException if the file cannot be read or contains malformed records
     */
    public static List<CompanyEarnings> read(Path path) throws IOException {
        String content = Files.readString(path, StandardCharsets.UTF_8);
        if (content.startsWith("\uFEFF")) {
            content = content.substring(1);
        }
        List<List<String>> records = parse(content);
        List<CompanyEarnings> companies = new ArrayList<>();
        boolean firstRecord = true;

        for (int i = 0; i < records.size(); i++) {
            List<String> fields = records.get(i);
            if (fields.size() == 1 && fields.get(0).isBlank()) {
                continue;
            }
            if (firstRecord && !fields.isEmpty()
                    && fields.get(0).trim().equalsIgnoreCase(HEADER[0])) {
                firstRecord = false;
                continue;
            }
            firstRecord = false;
            if (fields.size() != 6) {
                throw new IOException("CSV record " + (i + 1) + " must contain 6 fields.");
            }
            try {
                companies.add(new CompanyEarnings(
                        fields.get(0),
                        fields.get(1),
                        Double.parseDouble(fields.get(2).trim()),
                        Double.parseDouble(fields.get(3).trim()),
                        Double.parseDouble(fields.get(4).trim()),
                        Double.parseDouble(fields.get(5).trim())));
            } catch (NumberFormatException e) {
                throw new IOException("CSV record " + (i + 1) + " contains an invalid earnings value.", e);
            }
        }
        return companies;
    }

    /**
     * Writes company records to a UTF-8 CSV file, including the standard header
     * row and one row per company.
     *
     * @param path the destination file
     * @param companies the company records to write
     * @throws IOException if the file cannot be written
     */
    public static void write(Path path, List<CompanyEarnings> companies) throws IOException {
        StringBuilder csv = new StringBuilder();
        appendRecord(csv, List.of(
                HEADER[0], HEADER[1], HEADER[2], HEADER[3], HEADER[4], HEADER[5]));
        for (CompanyEarnings company : companies) {
            appendRecord(csv, List.of(
                    company.getCompanyName(),
                    company.getSector(),
                    Double.toString(company.q1),
                    Double.toString(company.q2),
                    Double.toString(company.q3),
                    Double.toString(company.q4)));
        }
        Files.writeString(path, csv.toString(), StandardCharsets.UTF_8);
    }

    /**
     * Formats one list of field values as a CSV record, quoting fields when
     * required and escaping embedded quotation marks.
     *
     * @param fields the values to encode as CSV fields
     * @return the formatted CSV record without a trailing line separator
     */
    public static String formatRecord(List<String> fields) {
        StringBuilder csv = new StringBuilder();
        appendRecord(csv, fields);
        return csv.substring(0, csv.length() - System.lineSeparator().length());
    }

    /**
     * Parses CSV text into records and fields, supporting quoted fields,
     * escaped quotation marks, and LF or CRLF record separators.
     *
     * @param csv the CSV text to parse
     * @return the parsed records, each represented by its list of field values
     * @throws IOException if the text contains malformed CSV quoting
     */
    public static List<List<String>> parse(String csv) throws IOException {
        List<List<String>> records = new ArrayList<>();
        List<String> record = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean afterQuote = false;
        boolean recordHasContent = false;

        for (int i = 0; i < csv.length(); i++) {
            char ch = csv.charAt(i);
            if (quoted) {
                if (ch == '"') {
                    if (i + 1 < csv.length() && csv.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                        afterQuote = true;
                    }
                } else {
                    field.append(ch);
                }
            } else if (afterQuote) {
                if (ch == ',') {
                    record.add(field.toString());
                    field.setLength(0);
                    afterQuote = false;
                    recordHasContent = true;
                } else if (ch == '\n' || ch == '\r') {
                    record.add(field.toString());
                    records.add(record);
                    record = new ArrayList<>();
                    field.setLength(0);
                    afterQuote = false;
                    recordHasContent = false;
                    if (ch == '\r' && i + 1 < csv.length() && csv.charAt(i + 1) == '\n') {
                        i++;
                    }
                } else {
                    throw new IOException("Unexpected character after a quoted CSV field.");
                }
            } else if (ch == '"') {
                if (field.length() != 0) {
                    throw new IOException("Quote found inside an unquoted CSV field.");
                }
                quoted = true;
                recordHasContent = true;
            } else if (ch == ',') {
                record.add(field.toString());
                field.setLength(0);
                recordHasContent = true;
            } else if (ch == '\n' || ch == '\r') {
                record.add(field.toString());
                records.add(record);
                record = new ArrayList<>();
                field.setLength(0);
                recordHasContent = false;
                if (ch == '\r' && i + 1 < csv.length() && csv.charAt(i + 1) == '\n') {
                    i++;
                }
            } else {
                field.append(ch);
                recordHasContent = true;
            }
        }

        if (quoted) {
            throw new IOException("CSV ended inside a quoted field.");
        }
        if (recordHasContent || afterQuote || field.length() > 0 || !record.isEmpty()) {
            record.add(field.toString());
            records.add(record);
        }
        return records;
    }

    /**
     * Appends one CSV-encoded record and the platform line separator to a
     * string builder.
     *
     * @param csv the builder receiving the encoded record
     * @param fields the values to encode as CSV fields
     */
    private static void appendRecord(StringBuilder csv, List<String> fields) {
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                csv.append(',');
            }
            String value = fields.get(i);
            if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                    || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
                csv.append('"').append(value.replace("\"", "\"\"")).append('"');
            } else {
                csv.append(value);
            }
        }
        csv.append(System.lineSeparator());
    }
}
