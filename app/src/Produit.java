import java.sql.*;

public class Produit {

    // Soumettre un nouveau produit
    public static int soumettreProduit(int idAnnonceur, String titre, String description,
                                       double prixSouhaite, int idCategorie, String condition) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "INSERT INTO produit (id_produit, titre, description, prix_souhaite, id_annonceur, id_categorie, condition_produit, statut, date_soumission) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 'en_attente', CURRENT_DATE) RETURNING id_produit";
        PreparedStatement stmt = conn.prepareStatement(sql);

        // Générer un nouvel ID
        int newId = getNextId(conn);
        stmt.setInt(1, newId);
        stmt.setString(2, titre);
        stmt.setString(3, description);
        stmt.setDouble(4, prixSouhaite);
        stmt.setInt(5, idAnnonceur);
        stmt.setInt(6, idCategorie);
        stmt.setString(7, condition);

        ResultSet rs = stmt.executeQuery();
        rs.next();
        conn.close();
        return rs.getInt(1);
    }

    // Publier un produit après acceptation de l'estimation
    public static void publierProduit(int idProduit) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "UPDATE produit SET statut = 'publie', date_publication = CURRENT_DATE WHERE id_produit = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idProduit);
        stmt.executeUpdate();
        conn.close();
    }

    // Récupérer tous les produits publiés
    public static ResultSet getProduitsPublies() throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT p.id_produit, p.titre, p.description, p.prix_souhaite, " +
                "c.nom AS categorie, u.prenom, u.nom, p.condition_produit " +
                "FROM produit p " +
                "JOIN categorie c ON p.id_categorie = c.id_categorie " +
                "JOIN annonceur a ON p.id_annonceur = a.id_annonceur " +
                "JOIN utilisateur u ON a.id_annonceur = u.id_utilisateur " +
                "WHERE p.statut = 'publie'";
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

    // Vérifier si une proposition déclenche une vente automatique
    public static boolean verifierVenteAutomatique(int idProduit, double prixPropose) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT e.prix_estime FROM estimation e " +
                "WHERE e.id_produit = ? AND e.acceptee = true";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idProduit);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            double prixEstime = rs.getDouble("prix_estime");
            conn.close();
            return prixPropose >= prixEstime;
        }
        conn.close();
        return false;
    }

    // Helper pour générer un nouvel ID
    private static int getNextId(Connection conn) throws Exception {
        String sql = "SELECT MAX(id_produit) FROM produit";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) return rs.getInt(1) + 1;
        return 1;
    }
}
