import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

/**
 * Provides the application's entry point and initializes the Swing interface
 * on the event dispatch thread. No instance is needed; start the application
 * by invoking {@link #main(String[])}.
 */
public class Main {
    /**
     * Creates an empty earnings model and opens the application window.
     *
     * @param args command-line arguments; currently unused
     */
    public static void main(String[] args) {
        List<CompanyEarnings> initData = new ArrayList<>();

        SwingUtilities.invokeLater(() -> {
            EarningsTableModel model = new EarningsTableModel(initData);
            QuarterlyEarningsApp app = new QuarterlyEarningsApp(model);
            app.setVisible(true);
        });
    }
}