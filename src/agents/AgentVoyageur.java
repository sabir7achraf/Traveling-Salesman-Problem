package agents;

import jade.core.Agent;
import util.Position;
import util.Permutateur;
import java.util.*;

public class AgentVoyageur extends Agent {
    private Map<String, Position> villes = new HashMap<>();

    protected void setup() {
        System.out.println("🚶 [Voyageur] Je suis prêt à résoudre le TSP !");

        // Remplis les villes ici ou depuis les messages reçus
        villes.put("Ville0", new Position(62.5, 84.85));
        villes.put("Ville1", new Position(80.25, 33.56));
        villes.put("Ville2", new Position(51.09, 20.14));
        villes.put("Ville3", new Position(0.70, 27.89));
        villes.put("Ville4", new Position(33.04, 96.13));

        resoudreTSP();
    }

    private void resoudreTSP() {
        List<String> nomsVilles = new ArrayList<>(villes.keySet());
        List<List<String>> permutations = Permutateur.permutations(nomsVilles);

        double meilleureDistance = Double.MAX_VALUE;
        List<String> meilleureTournee = null;

        for (List<String> tournee : permutations) {
            double distance = calculerDistanceTotale(tournee);
            if (distance < meilleureDistance) {
                meilleureDistance = distance;
                meilleureTournee = new ArrayList<>(tournee);
            }
        }

        System.out.println("✅ [TSP] Meilleure tournée trouvée :");
        for (String ville : meilleureTournee) {
            System.out.println(" ➡️ " + ville);
        }
        System.out.printf("📏 Distance totale : %.2f\n", meilleureDistance);
    }

    private double calculerDistanceTotale(List<String> tournee) {
        double total = 0.0;

        for (int i = 0; i < tournee.size() - 1; i++) {
            Position p1 = villes.get(tournee.get(i));
            Position p2 = villes.get(tournee.get(i + 1));
            total += p1.distance(p2);
        }

        // Retour au point de départ
        Position debut = villes.get(tournee.get(0));
        Position fin = villes.get(tournee.get(tournee.size() - 1));
        total += fin.distance(debut);

        return total;
    }
}
