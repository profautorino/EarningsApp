import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        List<CompanyEarnings> initData = new ArrayList<>();
        initData.add(new CompanyEarnings("Acme Corp", 150000.0, 175000.0, 200000.0, 225000.0));
        initData.add(new CompanyEarnings("Globex Inc", 120000.0, 130000.0, 140000.0, 150000.0));
        initData.add(new CompanyEarnings("Initech", 100000.0, 110000.0, 120000.0, 130000.0));
        initData.add(new CompanyEarnings("Stark Industries", 200000.0, 210000.0, 220000.0, 230000.0));

        SwingUtilities.invokeLater(() -> {
            EarningsTableModel model = new EarningsTableModel(initData);
            QuarterlyEarningsApp app = new QuarterlyEarningsApp(model);
            app.setVisible(true);
        });
    }
}