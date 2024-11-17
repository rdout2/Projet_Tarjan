import java.sql.*;

public class DatabaseManager {
    private Connection connection;

    // Méthode pour établir la connexion
    public void connect() {
        try {
            // Connexion à SQLite
            connection = DriverManager.getConnection("jdbc:sqlite:graph.db");
            System.out.println("Connexion réussie !");
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
    }

    // Méthode pour créer une table
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS edges (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "source INTEGER NOT NULL, " +
                     "destination INTEGER NOT NULL" +
                     ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'edges' créée !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la création de la table : " + e.getMessage());
        }
    }

    // Méthode pour insérer une arête dans la base
    public void insertEdge(int source, int destination) {
        String sql = "INSERT INTO edges (source, destination) VALUES (?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, source);
            pstmt.setInt(2, destination);
            pstmt.executeUpdate();
            System.out.println("Arête insérée : (" + source + ", " + destination + ")");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'insertion : " + e.getMessage());
        }
    }

    // Méthode pour fermer la connexion
    public void close() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Connexion fermée !");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la fermeture : " + e.getMessage());
        }
    }
}
