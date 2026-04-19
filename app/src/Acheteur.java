import java.sql.*;

public class Acheteur {

    // Faire une proposition sur un produit
    public static int faireProposition(int idProduit, int idAcheteur,
                                       double prixPropose, String message) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "INSERT INTO proposition (id_proposition, id_produit, id_acheteur, " +
                "prix_propose, message, date_proposition, statut) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, 'en_attente')";
        PreparedStatement stmt = conn.prepareStatement(sql);
        int newId = getNextId(conn);
        stmt.setInt(1, newId);
        stmt.setInt(2, idProduit);
        stmt.setInt(3, idAcheteur);
        stmt.setDouble(4, prixPropose);
        stmt.setString(5, message);
        stmt.executeUpdate();
        conn.close();
        return newId;
    }

    // Récupérer les propositions d'un acheteur
    public static ResultSet getPropositions(int idAcheteur) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT pr.id_proposition, p.titre, pr.prix_propose, pr.statut, " +
                "pr.date_proposition " +
                "FROM proposition pr " +
                "JOIN produit p ON pr.id_produit = p.id_produit " +
                "WHERE pr.id_acheteur = ? " +
                "ORDER BY pr.date_proposition DESC";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idAcheteur);
        return stmt.executeQuery();
    }

    // Conclure une vente automatiquement
    public static void conclureVente(int idProduit, int idAcheteur,
                                     int idProposition, double prixFinal) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            // Créer la vente
            String sqlVente = "INSERT INTO vente (id_vente, id_produit, id_acheteur, " +
                    "id_proposition, prix_final, date_vente, methode_paiement) " +
                    "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, 'en_ligne')";
            PreparedStatement stmtVente = conn.prepareStatement(sqlVente);
            stmtVente.setInt(1, getNextIdVente(conn));
            stmtVente.setInt(2, idProduit);
            stmtVente.setInt(3, idAcheteur);
            stmtVente.setInt(4, idProposition);
            stmtVente.setDouble(5, prixFinal); // CORRECTION : mauvais ordre avant !
            stmtVente.executeUpdate();

            // Mettre à jour le statut de la proposition
            String sqlProp = "UPDATE proposition SET statut = 'acceptee' WHERE id_proposition = ?";
            PreparedStatement stmtProp = conn.prepareStatement(sqlProp);
            stmtProp.setInt(1, idProposition);
            stmtProp.executeUpdate();

            // Mettre à jour le statut du produit
            String sqlProduit = "UPDATE produit SET statut = 'vendu' WHERE id_produit = ?";
            PreparedStatement stmtProduit = conn.prepareStatement(sqlProduit);
            stmtProduit.setInt(1, idProduit);
            stmtProduit.executeUpdate();

            conn.commit();

        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    // Ajouter un produit aux favoris
    public static void ajouterFavori(int idAcheteur, int idProduit) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "INSERT INTO favori (id_acheteur, id_produit, date_ajout) " +
                "VALUES (?, ?, CURRENT_TIMESTAMP)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idAcheteur);
        stmt.setInt(2, idProduit);
        stmt.executeUpdate();
        conn.close();
    }

    // Helper ID proposition
    private static int getNextId(Connection conn) throws Exception {
        String sql = "SELECT MAX(id_proposition) FROM proposition";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) return rs.getInt(1) + 1;
        return 1;
    }

    // Helper ID vente
    private static int getNextIdVente(Connection conn) throws Exception {
        String sql = "SELECT MAX(id_vente) FROM vente";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        if (rs.next()) return rs.getInt(1) + 1;
        return 1;
    }
}
