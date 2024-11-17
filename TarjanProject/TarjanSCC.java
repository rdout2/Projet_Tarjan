import java.sql.*;
import java.util.*;

public class TarjanSCC {
    private int V; // Nombre de sommets
    private List<List<Integer>> adj; // Liste d'adjacence
    private int time;
    private int[] disc, low;
    private Stack<Integer> stack;
    private boolean[] onStack;
    private List<List<Integer>> sccs;
    private Connection connection; // Connexion à la base de données

    public TarjanSCC(int V) {
        this.V = V;
        adj = new ArrayList<>(V);
        for (int i = 0; i < V; i++) {
            adj.add(new ArrayList<>());
        }
        time = 0;
        disc = new int[V];
        low = new int[V];
        Arrays.fill(disc, -1);
        Arrays.fill(low, -1);
        stack = new Stack<>();
        onStack = new boolean[V];
        sccs = new ArrayList<>();
        connectDatabase(); // Connexion à la base de données
        createTables();    // Création des tables
    }

    // Méthode pour établir la connexion à la base de données
    private void connectDatabase() {
    try {
        Class.forName("org.sqlite.JDBC"); // Charger explicitement le pilote SQLite
        connection = DriverManager.getConnection("jdbc:sqlite:graph.db");
        System.out.println("Connexion à la base de données réussie !");
    } catch (ClassNotFoundException e) {
        System.out.println("Pilote JDBC non trouvé : " + e.getMessage());
    } catch (SQLException e) {
        System.out.println("Erreur de connexion : " + e.getMessage());
    }
}

    // Méthode pour créer les tables
    private void createTables() {
        String createEdgesTable = "CREATE TABLE IF NOT EXISTS edges (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "source INTEGER NOT NULL, " +
                "destination INTEGER NOT NULL);";

        String createSCCsTable = "CREATE TABLE IF NOT EXISTS sccs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "component TEXT NOT NULL);";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createEdgesTable);
            stmt.execute(createSCCsTable);
            System.out.println("Tables créées avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la création des tables : " + e.getMessage());
        }
    }

    // Méthode pour ajouter une arête et l'enregistrer dans la base
    public void addEdge(int u, int v) {
        adj.get(u).add(v);
        String sql = "INSERT INTO edges (source, destination) VALUES (?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, u);
            pstmt.setInt(2, v);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'insertion de l'arête : " + e.getMessage());
        }
    }

    private void DFSUtil(int u) {
        disc[u] = low[u] = ++time;
        stack.push(u);
        onStack[u] = true;

        for (int v : adj.get(u)) {
            if (disc[v] == -1) {
                DFSUtil(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                low[u] = Math.min(low[u], disc[v]);
            }
        }

        if (low[u] == disc[u]) {
            List<Integer> component = new ArrayList<>();
            int w;
            do {
                w = stack.pop();
                onStack[w] = false;
                component.add(w);
            } while (w != u);
            sccs.add(component);
            saveSCCToDatabase(component); // Sauvegarde dans la base
        }
    }

    public void findSCCs() {
        for (int i = 0; i < V; i++)
            if (disc[i] == -1)
                DFSUtil(i);
    }

    // Méthode pour sauvegarder une SCC dans la base de données
    private void saveSCCToDatabase(List<Integer> component) {
        String sql = "INSERT INTO sccs (component) VALUES (?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, component.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'enregistrement de la SCC : " + e.getMessage());
        }
    }

    public void printSCCs() {
        System.out.println("Les composantes fortement connexes sont :");
        for (List<Integer> scc : sccs) {
            Collections.sort(scc); // Trier chaque composante
            System.out.print("SCC: ");
            for (int v : scc) {
                System.out.print(v + " ");
            }
            System.out.println();
        }
    }

    // Méthode pour fermer la connexion à la base de données
    public void closeDatabase() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Connexion fermée !");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la fermeture : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Entrez le nombre de sommets : ");
        int V = scanner.nextInt();
        scanner.nextLine();
        TarjanSCC graph = new TarjanSCC(V);

        System.out.println("Entrez les arêtes (format : u v) : ");
        while (true) {
            String line = scanner.nextLine();
            if (line.isEmpty()) {
                break;
            }
            String[] parts = line.split(" ");
            try {
                int u = Integer.parseInt(parts[0]);
                int v = Integer.parseInt(parts[1]);
                graph.addEdge(u, v);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                System.out.println("Erreur : Entrez les arêtes sous la forme 'u v'.");
            }
        }

        graph.findSCCs();
        graph.printSCCs();
        graph.closeDatabase();
    }
}
