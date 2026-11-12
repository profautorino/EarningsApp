/* Represents a company's quarterly earnings data 
*
* @param companyName The name of the company
* @param q1 The earnings for the first quarter
* @param q2 The earnings for the second quarter
* @param q3 The earnings for the third quarter
* @param q4 The earnings for the fourth quarter
* 
* @method getYearlyTotal() Returns the total earnings for the year by summing up the quarterly earnings
*/
public class CompanyEarnings {
    String companyName;
    double q1, q2, q3, q4;

    public CompanyEarnings(String companyName, double q1, double q2, double q3, double q4) {
        this.companyName = companyName;
        this.q1 = q1;
        this.q2 = q2;
        this.q3 = q3;
        this.q4 = q4;
    }

    public double getYearlyTotal() {
        return q1 + q2 + q3 + q4;
    }
}