import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {

    // Informations de connexion
    static final String URL = "jdbc:postgresql://localhost:5432/ift2935";
    static final String USER = "postgres";
    static final String PASSWORD = "P0stDr1p";

    public static void main(String[] args) {
        try {
            // Connexion à la base de données
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion réussie !");

            // Test
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM utilisateur");

            if (rs.next()) {
                System.out.println("Nombre d'utilisateurs : " + rs.getInt(1));
            }

            conn.close();

        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}