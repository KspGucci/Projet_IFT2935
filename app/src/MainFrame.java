import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MainFrame extends JFrame {
    private final JComboBox<UtilisateurItem> acheteurCombo;
    private final JComboBox<UtilisateurItem> annonceurCombo;
    private final JComboBox<UtilisateurItem> expertCombo;

    public MainFrame() {
        setTitle("Projet BD - Marketplace");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(620, 330);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("Choisir un utilisateur", JLabel.CENTER);

        acheteurCombo = new JComboBox<>();
        annonceurCombo = new JComboBox<>();
        expertCombo = new JComboBox<>();

        JButton acheteurButton = new JButton("Ouvrir le mode acheteur");
        JButton annonceurButton = new JButton("Ouvrir le mode annonceur");
        JButton expertButton = new JButton("Ouvrir le mode expert");

        acheteurButton.addActionListener(e -> ouvrirAcheteur());
        annonceurButton.addActionListener(e -> ouvrirAnnonceur());
        expertButton.addActionListener(e -> ouvrirExpert());

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        formPanel.add(new JLabel("Choisir un acheteur :"));
        formPanel.add(acheteurCombo);
        formPanel.add(new JLabel("Choisir un annonceur :"));
        formPanel.add(annonceurCombo);
        formPanel.add(new JLabel("Choisir un expert :"));
        formPanel.add(expertCombo);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 30));
        buttonPanel.add(acheteurButton);
        buttonPanel.add(annonceurButton);
        buttonPanel.add(expertButton);

        add(titleLabel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        chargerUtilisateurs();
    }

    private void chargerUtilisateurs() {
        chargerAcheteurs();
        chargerAnnonceurs();
        chargerExperts();
    }

    private void chargerAcheteurs() {
        String sql = "SELECT a.id_acheteur AS id, u.prenom, u.nom " +
                "FROM acheteur a " +
                "JOIN utilisateur u ON a.id_acheteur = u.id_utilisateur " +
                "ORDER BY a.id_acheteur";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            acheteurCombo.removeAllItems();
            while (rs.next()) {
                acheteurCombo.addItem(new UtilisateurItem(
                        rs.getInt("id"),
                        rs.getString("prenom"),
                        rs.getString("nom")
                ));
            }
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void chargerAnnonceurs() {
        String sql = "SELECT an.id_annonceur AS id, u.prenom, u.nom " +
                "FROM annonceur an " +
                "JOIN utilisateur u ON an.id_annonceur = u.id_utilisateur " +
                "ORDER BY an.id_annonceur";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            annonceurCombo.removeAllItems();
            while (rs.next()) {
                annonceurCombo.addItem(new UtilisateurItem(
                        rs.getInt("id"),
                        rs.getString("prenom"),
                        rs.getString("nom")
                ));
            }
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void chargerExperts() {
        String sql = "SELECT ex.id_expert AS id, u.prenom, u.nom " +
                "FROM expert ex " +
                "JOIN utilisateur u ON ex.id_expert = u.id_utilisateur " +
                "ORDER BY ex.id_expert";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            expertCombo.removeAllItems();
            while (rs.next()) {
                expertCombo.addItem(new UtilisateurItem(
                        rs.getInt("id"),
                        rs.getString("prenom"),
                        rs.getString("nom")
                ));
            }
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void ouvrirAcheteur() {
        UtilisateurItem acheteur = (UtilisateurItem) acheteurCombo.getSelectedItem();
        if (acheteur == null) {
            JOptionPane.showMessageDialog(this, "Aucun acheteur s\u00e9lectionn\u00e9.");
            return;
        }

        new AcheteurFrame(acheteur.getId()).setVisible(true);
    }

    private void ouvrirAnnonceur() {
        UtilisateurItem annonceur = (UtilisateurItem) annonceurCombo.getSelectedItem();
        if (annonceur == null) {
            JOptionPane.showMessageDialog(this, "Aucun annonceur s\u00e9lectionn\u00e9.");
            return;
        }

        new AnnonceurFrame(annonceur.getId()).setVisible(true);
    }

    private void ouvrirExpert() {
        UtilisateurItem expert = (UtilisateurItem) expertCombo.getSelectedItem();
        if (expert == null) {
            JOptionPane.showMessageDialog(this, "Aucun expert s\u00e9lectionn\u00e9.");
            return;
        }

        new ExpertFrame(expert.getId()).setVisible(true);
    }

    private void afficherErreur(Exception e) {
        JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
    }

    private static class UtilisateurItem {
        private final int id;
        private final String prenom;
        private final String nom;

        public UtilisateurItem(int id, String prenom, String nom) {
            this.id = id;
            this.prenom = prenom;
            this.nom = nom;
        }

        public int getId() {
            return id;
        }

        public String toString() {
            return id + " - " + prenom + " " + nom;
        }
    }
}
