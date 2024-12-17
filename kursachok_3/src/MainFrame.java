// MainFrame.java
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private InventoryPanel inventoryPanel;
    private CoachesPanel coachesPanel;
    private AthletesPanel athletesPanel;

    public MainFrame() {
        setTitle("Управление инвентарем спортивного клуба");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        inventoryPanel = new InventoryPanel();
        coachesPanel = new CoachesPanel();
        athletesPanel = new AthletesPanel();

        tabbedPane.addTab("Инвентарь", inventoryPanel);
        tabbedPane.addTab("Тренеры", coachesPanel);
        tabbedPane.addTab("Спортсмены", athletesPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
