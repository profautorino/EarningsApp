/**
 * Stores a company's name, sector, and earnings for each quarter of one year.
 * Instances are used as the records displayed by the earnings table and tree.
 */
public class CompanyEarnings {
    String companyName, sector;
    double q1, q2, q3, q4;

    /**
     * Creates a company earnings record.
     *
     * @param companyName the company's display name
     * @param sector the company's industry sector
     * @param q1 earnings for the first quarter
     * @param q2 earnings for the second quarter
     * @param q3 earnings for the third quarter
     * @param q4 earnings for the fourth quarter
     */
    public CompanyEarnings(String companyName, String sector, double q1, double q2, double q3, double q4) {
        this.companyName = companyName;
        this.sector = sector;
        this.q1 = q1;
        this.q2 = q2;
        this.q3 = q3;
        this.q4 = q4;
    }

    /**
     * Calculates the combined earnings across all four quarters.
     *
     * @return the sum of Q1, Q2, Q3, and Q4 earnings
     */
    public double getYearlyTotal() {
        return q1 + q2 + q3 + q4;
    }

    /**
     * Gets this company's display name.
     *
     * @return the company name
     */
    public String getCompanyName() {
        return this.companyName;
    }

    /**
     * Gets this company's sector.
     *
     * @return the company sector
     */
    public String getSector() {
        return this.sector;
    }
}