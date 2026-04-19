import java.sql.*;

public class Annonceur {

    // Récupérer les produits d'un annonceur
    public static ResultSet getMesProduits(int idAnnonceur) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT p.id_produit, p.titre, p.prix_souhaite, p.statut, " +
                "p.date_soumission, p.date_publication, c.nom AS categorie " +
                "FROM produit p " +
                "JOIN categorie c ON p.id_categorie = c.id_categorie " +
                "WHERE p.id_annonceur = ? " +
                "ORDER BY p.date_soumission DESC";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idAnnonceur);
        return stmt.executeQuery();
    }

    // Récupérer les propositions reçues pour un produit
    public static ResultSet getPropositionsRecues(int idProduit) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT pr.id_proposition, pr.prix_propose, pr.message, " +
                "pr.date_proposition, pr.statut, " +
                "u.prenom, u.nom " +
                "FROM proposition pr " +
                "JOIN acheteur a ON pr.id_acheteur = a.id_acheteur " +
                "JOIN utilisateur u ON a.id_acheteur = u.id_utilisateur " +
                "WHERE pr.id_produit = ? " +
                "ORDER BY pr.prix_propose DESC";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idProduit);
        return stmt.executeQuery();
    }

    // Récupérer les catégories pour le formulaire de soumission
    public static ResultSet getCategories() throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT id_categorie, nom FROM categorie ORDER BY nom";
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

    // Récupérer les statistiques d'un annonceur
    public static ResultSet getStats(int idAnnonceur) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT a.nb_ventes, a.note_moyenne, " +
                "COUNT(p.id_produit) AS nb_produits " +
                "FROM annonceur a " +
                "LEFT JOIN produit p ON a.id_annonceur = p.id_annonceur " +
                "WHERE a.id_annonceur = ? " +
                "GROUP BY a.nb_ventes, a.note_moyenne";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idAnnonceur);
        return stmt.executeQuery();
    }
}