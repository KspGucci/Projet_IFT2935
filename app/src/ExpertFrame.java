import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class ExpertFrame extends JFrame {
    private final int idExpert;
    private final JTable produitsTable;
    private final JTable estimationsTable;

    public ExpertFrame(int idExpert) {
        this.idExpert = idExpert;

        setTitle("Mode expert - ID " + idExpert);
        setSize(900, 600);
        setLocationRelativeTo(null);

        produitsTable = new JTable();
        estimationsTable = new JTable();

        JButton estimationButton = new JButton("Soumettre une estimation");
        JButton refreshButton = new JButton("Rafra\u00eechir");

        estimationButton.addActionListener(e -> soumettreEstimation());
        refreshButton.addActionListener(e -> chargerDonnees());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(estimationButton);
        buttons.add(refreshButton);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                creerTablePanel(
                        "Produits \u00e0 estimer",
                        "Produits soumis par les annonceurs et pas encore publi\u00e9s. S\u00e9lectionnez un produit pour soumettre une estimation.",
                        produitsTable
                ),
                creerTablePanel(
                        "Mes estimations",
                        "Estimations d\u00e9j\u00e0 soumises par cet expert et d\u00e9cision de l'annonceur lorsqu'elle existe.",
                        estimationsTable
                )
        );
        splitPane.setDividerLocation(300);

        add(buttons, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        chargerDonnees();
    }

    private void chargerDonnees() {
        chargerProduitsAEstimer();
        chargerMesEstimations();
    }

    private void chargerProduitsAEstimer() {
        String sql = "SELECT p.id_produit AS \"ID produit\", p.titre AS \"Titre\", " +
                "p.description AS \"Description\", p.prix_souhaite AS \"Prix souhait\u00e9\", " +
                "c.nom AS \"Cat\u00e9gorie\", u.prenom || ' ' || u.nom AS \"Annonceur\", " +
                "CASE p.statut " +
                "WHEN 'en_attente' THEN 'En attente' " +
                "WHEN 'en_attente_estimation' THEN 'En attente d''estimation' " +
                "WHEN 'estimation_refusee' THEN 'Estimation refus\u00e9e' " +
                "ELSE p.statut END AS \"Statut\" " +
                "FROM produit p " +
                "JOIN categorie c ON p.id_categorie = c.id_categorie " +
                "JOIN utilisateur u ON p.id_annonceur = u.id_utilisateur " +
                "WHERE p.statut IN ('en_attente', 'en_attente_estimation', 'estimation_refusee') " +
                "ORDER BY p.date_soumission DESC, p.id_produit";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            produitsTable.setModel(creerModele(rs));
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void chargerMesEstimations() {
        String sql = "SELECT e.id_estimation AS \"ID estimation\", p.id_produit AS \"ID produit\", " +
                "p.titre AS \"Titre\", e.prix_estime AS \"Prix estim\u00e9\", e.commentaire AS \"Commentaire\", " +
                "CASE WHEN e.acceptee IS NULL THEN 'En attente' " +
                "WHEN e.acceptee = TRUE THEN 'Accept\u00e9e' ELSE 'Refus\u00e9e' END AS \"D\u00e9cision\", " +
                "e.date_decision AS \"Date de d\u00e9cision\" " +
                "FROM estimation e " +
                "JOIN produit p ON e.id_produit = p.id_produit " +
                "WHERE e.id_expert = ? " +
                "ORDER BY e.id_estimation DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idExpert);
            try (ResultSet rs = stmt.executeQuery()) {
                estimationsTable.setModel(creerModele(rs));
            }
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void soumettreEstimation() {
        Integer idProduit = getProduitSelectionne();
        if (idProduit == null) {
            return;
        }

        JTextField prixField = new JTextField();
        JTextField commentaireField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Prix estim\u00e9 :"));
        panel.add(prixField);
        panel.add(new JLabel("Commentaire :"));
        panel.add(commentaireField);

        int choix = JOptionPane.showConfirmDialog(this, panel, "Nouvelle estimation", JOptionPane.OK_CANCEL_OPTION);
        if (choix != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            BigDecimal prix = new BigDecimal(prixField.getText().trim());
            insererEstimation(idProduit, prix, commentaireField.getText().trim());
            JOptionPane.showMessageDialog(this, "Estimation soumise. L'annonceur peut maintenant l'accepter ou la refuser.");
            chargerDonnees();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Le prix estim\u00e9 doit \u00eatre un nombre.");
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void insererEstimation(int idProduit, BigDecimal prix, String commentaire) throws Exception {
        String insertSql = "INSERT INTO estimation " +
                "(id_estimation, id_produit, id_expert, prix_estime, commentaire, acceptee) " +
                "VALUES (?, ?, ?, ?, ?, NULL)";
        String updateProduitSql = "UPDATE produit SET statut = 'en_attente_estimation' WHERE id_produit = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                    stmt.setInt(1, getNextId(conn, "estimation", "id_estimation"));
                    stmt.setInt(2, idProduit);
                    stmt.setInt(3, idExpert);
                    stmt.setBigDecimal(4, prix);
                    stmt.setString(5, commentaire);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(updateProduitSql)) {
                    stmt.setInt(1, idProduit);
                    stmt.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private Integer getProduitSelectionne() {
        int row = produitsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "S\u00e9lectionnez un produit.");
            return null;
        }

        int modelRow = produitsTable.convertRowIndexToModel(row);
        Object value = produitsTable.getModel().getValueAt(modelRow, 0);
        return Integer.parseInt(value.toString());
    }

    private DefaultTableModel creerModele(ResultSet rs) throws Exception {
        ResultSetMetaData meta = rs.getMetaData();
        int colonnes = meta.getColumnCount();
        DefaultTableModel model = new DefaultTableModel();

        for (int i = 1; i <= colonnes; i++) {
            model.addColumn(meta.getColumnLabel(i));
        }

        while (rs.next()) {
            Object[] ligne = new Object[colonnes];
            for (int i = 1; i <= colonnes; i++) {
                ligne[i - 1] = rs.getObject(i);
            }
            model.addRow(ligne);
        }

        return model;
    }

    private int getNextId(Connection conn, String table, String column) throws Exception {
        String sql = "SELECT COALESCE(MAX(" + column + "), 0) + 1 FROM " + table;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private void afficherErreur(Exception e) {
        JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage());
    }

    private JPanel creerTablePanel(String titre, String description, JTable table) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel header = new JPanel(new BorderLayout());
        header.add(new JLabel(titre), BorderLayout.NORTH);
        header.add(new JLabel(description), BorderLayout.SOUTH);

        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
}
