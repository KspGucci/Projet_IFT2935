import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    // Le style par defaut de Swing fonctionne aussi.
                }

                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            }
        });
    }
}
