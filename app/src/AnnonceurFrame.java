import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class AnnonceurFrame extends JFrame {
    private final int idAnnonceur;
    private final JTable produitsTable;
    private final JTable propositionsTable;
    private final JTable estimationsTable;

    public AnnonceurFrame(int idAnnonceur) {
        this.idAnnonceur = idAnnonceur;

        setTitle("Mode annonceur - ID " + idAnnonceur);
        setSize(950, 600);
        setLocationRelativeTo(null);

        produitsTable = new JTable();
        propositionsTable = new JTable();
        estimationsTable = new JTable();

        JButton creerProduitButton = new JButton("Cr\u00e9er un produit");
        JButton accepterEstimationButton = new JButton("Accepter estimation");
        JButton refuserEstimationButton = new JButton("Refuser estimation");
        JButton accepterButton = new JButton("Accepter proposition");
        JButton refuserButton = new JButton("Refuser proposition");
        JButton refreshButton = new JButton("Rafra\u00eechir");

        creerProduitButton.addActionListener(e -> creerProduit());
        accepterEstimationButton.addActionListener(e -> changerDecisionEstimation(true));
        refuserEstimationButton.addActionListener(e -> changerDecisionEstimation(false));
        accepterButton.addActionListener(e -> accepterProposition());
        refuserButton.addActionListener(e -> refuserProposition());
        refreshButton.addActionListener(e -> chargerDonnees());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(creerProduitButton);
        buttons.add(accepterEstimationButton);
        buttons.add(refuserEstimationButton);
        buttons.add(accepterButton);
        buttons.add(refuserButton);
        buttons.add(refreshButton);

        JSplitPane bottomSplitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                creerTablePanel(
                        "Estimations re\u00e7ues",
                        "Estimations faites par les experts pour vos produits. S\u00e9lectionnez une estimation pour l'accepter ou la refuser.",
                        estimationsTable
                ),
                creerTablePanel(
                        "Propositions des acheteurs",
                        "Offres re\u00e7ues pour vos produits. Accepter une proposition cr\u00e9e une vente et marque le produit comme vendu.",
                        propositionsTable
                )
        );
        bottomSplitPane.setDividerLocation(170);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                creerTablePanel(
                        "Mes produits",
                        "Produits qui vous appartiennent, avec leur prix souhait\u00e9, leur cat\u00e9gorie et leur statut actuel.",
                        produitsTable
                ),
                bottomSplitPane
        );
        splitPane.setDividerLocation(220);

        add(buttons, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        chargerDonnees();
    }

    private void chargerDonnees() {
        chargerProduits();
        chargerEstimations();
        chargerPropositions();
    }

    private void chargerProduits() {
        String sql = "SELECT p.id_produit AS \"ID produit\", p.titre AS \"Titre\", " +
                "p.prix_souhaite AS \"Prix souhait\u00e9\", c.nom AS \"Cat\u00e9gorie\", " +
                "CASE p.statut " +
                "WHEN 'publie' THEN 'Publi\u00e9' " +
                "WHEN 'vendu' THEN 'Vendu' " +
                "WHEN 'retire' THEN 'Retir\u00e9' " +
                "WHEN 'en_attente' THEN 'En attente' " +
                "WHEN 'en_attente_estimation' THEN 'En attente d''estimation' " +
                "WHEN 'estimation_refusee' THEN 'Estimation refus\u00e9e' " +
                "ELSE p.statut END AS \"Statut\" " +
                "FROM produit p " +
                "JOIN categorie c ON p.id_categorie = c.id_categorie " +
                "WHERE p.id_annonceur = ? " +
                "ORDER BY p.id_produit";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAnnonceur);
            try (ResultSet rs = stmt.executeQuery()) {
                produitsTable.setModel(creerModele(rs));
            }
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void chargerPropositions() {
        String sql = "SELECT pr.id_proposition AS \"ID proposition\", pr.id_produit AS \"ID produit\", " +
                "p.titre AS \"Titre\", pr.id_acheteur AS \"ID acheteur\", " +
                "u.prenom || ' ' || u.nom AS \"Acheteur\", pr.prix_propose AS \"Prix propos\u00e9\", " +
                "pr.message AS \"Message\", " +
                "CASE pr.statut " +
                "WHEN 'en_attente' THEN 'En attente' " +
                "WHEN 'acceptee' THEN 'Accept\u00e9e' " +
                "WHEN 'refusee' THEN 'Refus\u00e9e' " +
                "ELSE pr.statut END AS \"Statut\" " +
                "FROM proposition pr " +
                "JOIN produit p ON pr.id_produit = p.id_produit " +
                "JOIN utilisateur u ON pr.id_acheteur = u.id_utilisateur " +
                "WHERE p.id_annonceur = ? " +
                "ORDER BY pr.date_proposition DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAnnonceur);
            try (ResultSet rs = stmt.executeQuery()) {
                propositionsTable.setModel(creerModele(rs));
            }
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void chargerEstimations() {
        String sql = "SELECT e.id_estimation AS \"ID estimation\", p.id_produit AS \"ID produit\", " +
                "p.titre AS \"Titre\", u.prenom || ' ' || u.nom AS \"Expert\", " +
                "e.prix_estime AS \"Prix estim\u00e9\", e.commentaire AS \"Commentaire\", " +
                "CASE WHEN e.acceptee IS NULL THEN 'En attente' " +
                "WHEN e.acceptee = TRUE THEN 'Accept\u00e9e' ELSE 'Refus\u00e9e' END AS \"D\u00e9cision\" " +
                "FROM estimation e " +
                "JOIN produit p ON e.id_produit = p.id_produit " +
                "JOIN utilisateur u ON e.id_expert = u.id_utilisateur " +
                "WHERE p.id_annonceur = ? " +
                "ORDER BY e.id_estimation DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAnnonceur);
            try (ResultSet rs = stmt.executeQuery()) {
                estimationsTable.setModel(creerModele(rs));
            }
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void creerProduit() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            JComboBox<CategorieItem> categorieCombo = chargerCategories(conn);
            JComboBox<UtilisateurItem> expertCombo = chargerExperts(conn);
            JTextField nouvelleCategorieField = new JTextField();
            JTextField titreField = new JTextField();
            JTextField descriptionField = new JTextField();
            JTextField prixField = new JTextField();
            JTextField conditionField = new JTextField("bon");

            JPanel panel = new JPanel(new GridLayout(7, 2, 5, 5));
            panel.add(new JLabel("Titre :"));
            panel.add(titreField);
            panel.add(new JLabel("Description :"));
            panel.add(descriptionField);
            panel.add(new JLabel("Prix souhait\u00e9 :"));
            panel.add(prixField);
            panel.add(new JLabel("Condition :"));
            panel.add(conditionField);
            panel.add(new JLabel("Cat\u00e9gorie existante :"));
            panel.add(categorieCombo);
            panel.add(new JLabel("Nouvelle cat\u00e9gorie (facultatif) :"));
            panel.add(nouvelleCategorieField);
            panel.add(new JLabel("Expert pour l'estimation :"));
            panel.add(expertCombo);

            int choix = JOptionPane.showConfirmDialog(this, panel, "Nouveau produit", JOptionPane.OK_CANCEL_OPTION);
            if (choix != JOptionPane.OK_OPTION) {
                return;
            }

            String titre = titreField.getText().trim();
            String prixTexte = prixField.getText().trim();
            String condition = conditionField.getText().trim();
            if (titre.isEmpty() || prixTexte.isEmpty() || condition.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Le titre, le prix et la condition sont obligatoires.");
                return;
            }

            BigDecimal prix = new BigDecimal(prixTexte);
            int idCategorie = getCategorieChoisie(conn, categorieCombo, nouvelleCategorieField.getText().trim());
            insererProduit(conn, titre, descriptionField.getText().trim(), prix, idCategorie, condition);
            JOptionPane.showMessageDialog(this, "Produit cr\u00e9\u00e9. La fen\u00eatre expert va s'ouvrir pour simuler l'estimation.");
            ouvrirExpertSelectionne(expertCombo);
            chargerDonnees();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Le prix doit \u00eatre un nombre.");
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private JComboBox<CategorieItem> chargerCategories(Connection conn) throws Exception {
        JComboBox<CategorieItem> combo = new JComboBox<>();
        String sql = "SELECT id_categorie, nom FROM categorie ORDER BY nom";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                combo.addItem(new CategorieItem(rs.getInt("id_categorie"), rs.getString("nom")));
            }
        }
        return combo;
    }

    private JComboBox<UtilisateurItem> chargerExperts(Connection conn) throws Exception {
        JComboBox<UtilisateurItem> combo = new JComboBox<>();
        String sql = "SELECT ex.id_expert, u.prenom, u.nom " +
                "FROM expert ex " +
                "JOIN utilisateur u ON ex.id_expert = u.id_utilisateur " +
                "ORDER BY ex.id_expert";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                combo.addItem(new UtilisateurItem(
                        rs.getInt("id_expert"),
                        rs.getString("prenom"),
                        rs.getString("nom")
                ));
            }
        }
        return combo;
    }

    private void ouvrirExpertSelectionne(JComboBox<UtilisateurItem> expertCombo) {
        UtilisateurItem expert = (UtilisateurItem) expertCombo.getSelectedItem();
        if (expert != null) {
            new ExpertFrame(expert.getId()).setVisible(true);
        }
    }

    private int getCategorieChoisie(Connection conn, JComboBox<CategorieItem> categorieCombo,
                                    String nouvelleCategorie) throws Exception {
        if (!nouvelleCategorie.isEmpty()) {
            return creerCategorie(conn, nouvelleCategorie);
        }

        CategorieItem categorie = (CategorieItem) categorieCombo.getSelectedItem();
        if (categorie == null) {
            throw new Exception("Aucune categorie disponible.");
        }
        return categorie.getId();
    }

    private int creerCategorie(Connection conn, String nom) throws Exception {
        String selectSql = "SELECT id_categorie FROM categorie WHERE LOWER(nom) = LOWER(?)";
        try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
            stmt.setString(1, nom);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_categorie");
                }
            }
        }

        int idCategorie = getNextId(conn, "categorie", "id_categorie");
        String insertSql = "INSERT INTO categorie (id_categorie, nom, description, categorie_parent) VALUES (?, ?, ?, NULL)";
        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setInt(1, idCategorie);
            stmt.setString(2, nom);
            stmt.setString(3, "Cat\u00e9gorie ajout\u00e9e depuis l'application");
            stmt.executeUpdate();
        }
        return idCategorie;
    }

    private void insererProduit(Connection conn, String titre, String description, BigDecimal prix,
                                int idCategorie, String condition) throws Exception {
        String sql = "INSERT INTO produit " +
                "(id_produit, titre, description, prix_souhaite, id_annonceur, id_categorie, condition_produit, statut, date_soumission) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 'en_attente_estimation', CURRENT_DATE)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, getNextId(conn, "produit", "id_produit"));
            stmt.setString(2, titre);
            stmt.setString(3, description);
            stmt.setBigDecimal(4, prix);
            stmt.setInt(5, idAnnonceur);
            stmt.setInt(6, idCategorie);
            stmt.setString(7, condition);
            stmt.executeUpdate();
        }
    }

    private void changerDecisionEstimation(boolean accepter) {
        Integer idEstimation = getEstimationSelectionnee();
        if (idEstimation == null) {
            return;
        }

        String estimationSql = "UPDATE estimation SET acceptee = ?, date_decision = CURRENT_DATE WHERE id_estimation = ?";
        String produitSql = "UPDATE produit SET statut = ?, date_publication = CASE WHEN ? THEN CURRENT_DATE ELSE date_publication END " +
                "WHERE id_produit = (SELECT id_produit FROM estimation WHERE id_estimation = ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(estimationSql)) {
                    stmt.setBoolean(1, accepter);
                    stmt.setInt(2, idEstimation);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(produitSql)) {
                    stmt.setString(1, accepter ? "publie" : "estimation_refusee");
                    stmt.setBoolean(2, accepter);
                    stmt.setInt(3, idEstimation);
                    stmt.executeUpdate();
                }

                conn.commit();
                JOptionPane.showMessageDialog(this, accepter ? "Estimation accept\u00e9e. Le produit est publi\u00e9." : "Estimation refus\u00e9e. Le produit n'est pas publi\u00e9.");
                chargerDonnees();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void accepterProposition() {
        Integer idProposition = getPropositionSelectionnee();
        if (idProposition == null) {
            return;
        }

        String[] methodesP = {"carte", "comptant", "paypal", "virement"};
        String methodeP = (String) JOptionPane.showInputDialog(this,
                "M\u00e9thode de paiement :", "Paiement",
                JOptionPane.QUESTION_MESSAGE, null, methodesP, methodesP[0]);
        if (methodeP == null) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                PropositionInfo proposition = getPropositionInfo(conn, idProposition);
                creerVente(conn, proposition, methodeP);
                marquerProduitVendu(conn, proposition.idProduit);
                changerStatutProposition(conn, idProposition, "acceptee");
                refuserAutresPropositions(conn, proposition.idProduit, idProposition);

                conn.commit();
                JOptionPane.showMessageDialog(this, "Proposition accept\u00e9e. La vente est cr\u00e9\u00e9e et le produit est marqu\u00e9 comme vendu.");
                chargerDonnees();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private void refuserProposition() {
        Integer idProposition = getPropositionSelectionnee();
        if (idProposition == null) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            changerStatutProposition(conn, idProposition, "refusee");
            JOptionPane.showMessageDialog(this, "Proposition refus\u00e9e.");
            chargerPropositions();
        } catch (Exception e) {
            afficherErreur(e);
        }
    }

    private PropositionInfo getPropositionInfo(Connection conn, int idProposition) throws Exception {
        String sql = "SELECT pr.id_proposition, pr.id_produit, pr.id_acheteur, pr.prix_propose " +
                "FROM proposition pr " +
                "JOIN produit p ON pr.id_produit = p.id_produit " +
                "WHERE pr.id_proposition = ? AND p.id_annonceur = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProposition);
            stmt.setInt(2, idAnnonceur);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("Proposition introuvable pour cet annonceur.");
                }
                return new PropositionInfo(
                        rs.getInt("id_proposition"),
                        rs.getInt("id_produit"),
                        rs.getInt("id_acheteur"),
                        rs.getBigDecimal("prix_propose")
                );
            }
        }
    }

    private void creerVente(Connection conn, PropositionInfo proposition, String methodeP) throws Exception {
        String existsSql = "SELECT COUNT(*) FROM vente WHERE id_proposition = ?";
        try (PreparedStatement stmt = conn.prepareStatement(existsSql)) {
            stmt.setInt(1, proposition.idProposition);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    return;
                }
            }
        }

        String insertSql = "INSERT INTO vente " +
                "(id_vente, id_produit, id_acheteur, id_proposition, prix_final, date_vente, methode_paiement) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setInt(1, getNextId(conn, "vente", "id_vente"));
            stmt.setInt(2, proposition.idProduit);
            stmt.setInt(3, proposition.idAcheteur);
            stmt.setInt(4, proposition.idProposition);
            stmt.setBigDecimal(5, proposition.prixFinal);
            stmt.setString(6, methodeP);
            stmt.executeUpdate();
        }
    }

    private void marquerProduitVendu(Connection conn, int idProduit) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE produit SET statut = 'vendu' WHERE id_produit = ?")) {
            stmt.setInt(1, idProduit);
            stmt.executeUpdate();
        }
    }

    private void changerStatutProposition(Connection conn, int idProposition, String statut) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE proposition SET statut = ? WHERE id_proposition = ?")) {
            stmt.setString(1, statut);
            stmt.setInt(2, idProposition);
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

    private Integer getPropositionSelectionnee() {
        int row = propositionsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "S\u00e9lectionnez une proposition.");
            return null;
        }

        int modelRow = propositionsTable.convertRowIndexToModel(row);
        Object value = propositionsTable.getModel().getValueAt(modelRow, 0);
        return Integer.parseInt(value.toString());
    }

    private Integer getEstimationSelectionnee() {
        int row = estimationsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "S\u00e9lectionnez une estimation.");
            return null;
        }

        int modelRow = estimationsTable.convertRowIndexToModel(row);
        Object value = estimationsTable.getModel().getValueAt(modelRow, 0);
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

    private int getNextId(Connection conn, String table, String column) throws Exception {
        String sql = "SELECT COALESCE(MAX(" + column + "), 0) + 1 FROM " + table;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
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

    private static class CategorieItem {
        private final int id;
        private final String nom;

        public CategorieItem(int id, String nom) {
            this.id = id;
            this.nom = nom;
        }

        public int getId() {
            return id;
        }

        public String toString() {
            return id + " - " + nom;
        }
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

    private static class PropositionInfo {
        private final int idProposition;
        private final int idProduit;
        private final int idAcheteur;
        private final BigDecimal prixFinal;

        public PropositionInfo(int idProposition, int idProduit, int idAcheteur, BigDecimal prixFinal) {
            this.idProposition = idProposition;
            this.idProduit = idProduit;
            this.idAcheteur = idAcheteur;
            this.prixFinal = prixFinal;
        }
    }
}
