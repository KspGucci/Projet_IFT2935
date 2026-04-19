import java.sql.*;

public class Expert {

    // Soumettre une estimation pour un produit
    public static void soumettreEstimation(int idProduit, int idExpert,
                                           double prixEstime, String commentaire) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "INSERT INTO estimation (id_estimation, id_produit, id_expert, prix_estime, commentaire, acceptee) " +
                "VALUES (?, ?, ?, ?, ?, NULL)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, getNextId(conn));
        stmt.setInt(2, idProduit);
        stmt.setInt(3, idExpert);
        stmt.setDouble(4, prixEstime);
        stmt.setString(5, commentaire);
        stmt.executeUpdate();
        conn.close();
    }

    // Récupérer l'estimation d'un produit
    public static ResultSet getEstimation(int idProduit) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT e.id_estimation, e.prix_estime, e.commentaire, " +
                "u.prenom, u.nom, ex.domaine_expertise " +
                "FROM estimation e " +
                "JOIN expert ex ON e.id_expert = ex.id_expert " +
                "JOIN utilisateur u ON ex.id_expert = u.id_utilisateur " +
                "WHERE e.id_produit = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idProduit);
        return stmt.executeQuery();
    }

    // Annonceur accepte l'estimation
    public static void accepterEstimation(int idEstimation) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "UPDATE estimation SET acceptee = TRUE, date_decision = CURRENT_DATE " +
                "WHERE id_estimation = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idEstimation);
        stmt.executeUpdate();
        conn.close();
    }

    // Annonceur refuse l'estimation
    public static void refuserEstimation(int idEstimation) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "UPDATE estimation SET acceptee = FALSE, date_decision = CURRENT_DATE " +
                "WHERE id_estimation = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idEstimation);
        stmt.executeUpdate();
        conn.close();
    }

    // Helper pour générer un nouvel ID
    private static int getNextId(Connection conn) throws Exception {
        String sql = "SELECT MAX(id_estimation) FROM estimation";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) return rs.getInt(1) + 1;
        return 1;
    }
}
