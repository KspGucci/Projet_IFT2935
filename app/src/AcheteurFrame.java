import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class AcheteurFrame extends JFrame {
    private final int idAcheteur;
    private final JTable produitsTable;
    private final JTable favorisTable;

    public AcheteurFrame(int idAcheteur) {
        this.idAcheteur = idAcheteur;

        setTitle("Mode acheteur - ID " + idAcheteur);
        setSize(900, 600);
        setLocationRelativeTo(null);

        produitsTable = new JTable();
        favorisTable = new JTable();

        JButton favoriButton = new JButton("Ajouter aux favoris");
        JButton propositionButton = new JButton("Faire une proposition");
        JButton refreshButton = new JButton("Rafra\u00eechir");

        favoriButton.addActionListener(e -> ajouterFavori());
        propositionButton.addActionListener(e -> faireProposition());
        refreshButton.addActionListener(e -> chargerDonnees());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(favoriButton);
        buttons.add(propositionButton);
        buttons.add(refreshButton);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                creerTablePanel(
                        "Produits publi\u00e9s",
                        "Produits disponibles \u00e0 l'achat. S\u00e9lectionnez une ligne pour ajouter le produit aux favoris ou faire une proposition.",
                        produitsTable
                ),
                creerTablePanel(
                        "Mes favoris",
                        "Produits que vous avez ajout\u00e9s aux favoris. Ce tableau se met \u00e0 jour apr\u00e8s un ajout aux favoris ou un rafra\u00eechissement.",
                        favorisTable
                )
        );
        splitPane.setDividerLocation(330);

        add(buttons, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        chargerDonnees();
    }

    private void chargerDonnees() {
        chargerProduits();
        chargerFavoris();
    }

    private void chargerProduits() {
        String sql = "SELECT p.id_produit AS \"ID produit\", p.titre AS \"Titre\", " +
                "p.prix_souhaite AS \"Prix souhait\u00e9\", c.nom AS \"Cat\u00e9gorie\" " +
                "FROM produit p " +
                "JOIN categorie c ON p.id_categorie = c.id_categorie " +
                "WHERE p.statut = 'publie' " +
                "ORDER BY p.id_produit";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            produitsTable.setModel(creerModele(rs));
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void chargerFavoris() {
        String sql = "SELECT p.id_produit AS \"ID produit\", p.titre AS \"Titre\", " +
                "p.prix_souhaite AS \"Prix souhait\u00e9\", c.nom AS \"Cat\u00e9gorie\", " +
                "f.date_ajout AS \"Date d'ajout\" " +
                "FROM favori f " +
                "JOIN produit p ON f.id_produit = p.id_produit " +
                "JOIN categorie c ON p.id_categorie = c.id_categorie " +
                "WHERE f.id_acheteur = ? " +
                "ORDER BY f.date_ajout DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAcheteur);
            try (ResultSet rs = stmt.executeQuery()) {
                favorisTable.setModel(creerModele(rs));
            }
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void ajouterFavori() {
        Integer idProduit = getProduitSelectionne();
        if (idProduit == null) {
            return;
        }

        String sql = "INSERT INTO favori (id_acheteur, id_produit, date_ajout) " +
                "VALUES (?, ?, CURRENT_TIMESTAMP) " +
                "ON CONFLICT (id_acheteur, id_produit) DO NOTHING";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAcheteur);
            stmt.setInt(2, idProduit);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Produit ajout\u00e9 aux favoris.");
            chargerFavoris();
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void faireProposition() {
        Integer idProduit = getProduitSelectionne();
        if (idProduit == null) {
            return;
        }

        String prixTexte = JOptionPane.showInputDialog(this, "Prix propos\u00e9");
        if (prixTexte == null || prixTexte.trim().isEmpty()) {
            return;
        }

        String message = JOptionPane.showInputDialog(this, "Message");
        if (message == null) {
            message = "";
        }

        try {
            BigDecimal prix = new BigDecimal(prixTexte.trim());
            boolean venteConclue = insererProposition(idProduit, prix, message);
            if (venteConclue) {
                JOptionPane.showMessageDialog(this, "Proposition envoy\u00e9e. Le prix atteint l'estimation accept\u00e9e : la vente est conclue automatiquement.");
                chargerDonnees();
            } else {
                JOptionPane.showMessageDialog(this, "Proposition envoy\u00e9e.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Le prix doit \u00eatre un nombre.");
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private boolean insererProposition(int idProduit, BigDecimal prix, String message) throws Exception {
        String nextIdSql = "SELECT COALESCE(MAX(id_proposition), 0) + 1 FROM proposition";
        String insertSql = "INSERT INTO proposition " +
                "(id_proposition, id_produit, id_acheteur, prix_propose, message, date_proposition, statut) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, 'en_attente')";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            int idProposition;
            try {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(nextIdSql)) {
                    rs.next();
                    idProposition = rs.getInt(1);
                }

                try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                    stmt.setInt(1, idProposition);
                    stmt.setInt(2, idProduit);
                    stmt.setInt(3, idAcheteur);
                    stmt.setBigDecimal(4, prix);
                    stmt.setString(5, message);
                    stmt.executeUpdate();
                }

                boolean venteConclue = conclureVenteAutomatiqueSiPossible(conn, idProduit, idAcheteur, idProposition, prix);
                conn.commit();
                return venteConclue;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private boolean conclureVenteAutomatiqueSiPossible(Connection conn, int idProduit, int idAcheteur,
                                                       int idProposition, BigDecimal prix) throws Exception {
        BigDecimal prixEstime = getPrixEstimeAccepte(conn, idProduit);
        if (prixEstime == null || prix.compareTo(prixEstime) < 0) {
            return false;
        }

        creerVente(conn, idProduit, idAcheteur, idProposition, prix);
        marquerProduitVendu(conn, idProduit);
        accepterProposition(conn, idProposition);
        refuserAutresPropositions(conn, idProduit, idProposition);
        return true;
    }

    private BigDecimal getPrixEstimeAccepte(Connection conn, int idProduit) throws Exception {
        String sql = "SELECT prix_estime FROM estimation " +
                "WHERE id_produit = ? AND acceptee = TRUE " +
                "ORDER BY date_decision DESC NULLS LAST, id_estimation DESC LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("prix_estime");
                }
                return null;
            }
        }
    }

    private void creerVente(Connection conn, int idProduit, int idAcheteur, int idProposition,
                            BigDecimal prixFinal) throws Exception {
        String existsSql = "SELECT COUNT(*) FROM vente WHERE id_proposition = ?";
        try (PreparedStatement stmt = conn.prepareStatement(existsSql)) {
            stmt.setInt(1, idProposition);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    return;
                }
            }
        }

        String insertSql = "INSERT INTO vente " +
                "(id_vente, id_produit, id_acheteur, id_proposition, prix_final, date_vente, methode_paiement) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, 'a_confirmer')";

        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setInt(1, getNextId(conn, "vente", "id_vente"));
            stmt.setInt(2, idProduit);
            stmt.setInt(3, idAcheteur);
            stmt.setInt(4, idProposition);
            stmt.setBigDecimal(5, prixFinal);
            stmt.executeUpdate();
        }
    }

    private void marquerProduitVendu(Connection conn, int idProduit) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE produit SET statut = 'vendu' WHERE id_produit = ?")) {
            stmt.setInt(1, idProduit);
            stmt.executeUpdate();
        }
    }

    private void accepterProposition(Connection conn, int idProposition) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE proposition SET statut = 'acceptee' WHERE id_proposition = ?")) {
            stmt.setInt(1, idProposition);
            stmt.executeUpdate();
        }
    }

    private void refuserAutresPropositions(Connection conn, int idProduit, int idProposition) throws Exception {
        String sql = "UPDATE proposition SET statut = 'refusee' " +
                "WHERE id_produit = ? AND id_proposition <> ? AND statut = 'en_attente'";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProduit);
            stmt.setInt(2, idProposition);
            stmt.executeUpdate();
        }
    }

    private int getNextId(Connection conn, String table, String column) throws Exception {
        String sql = "SELECT COALESCE(MAX(" + column + "), 0) + 1 FROM " + table;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
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
