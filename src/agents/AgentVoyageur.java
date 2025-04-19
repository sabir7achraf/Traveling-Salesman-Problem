package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import util.Position;
import util.Permutateur;

import java.util.*;

public class AgentVoyageur extends Agent {
    private Map<String, Position> villes = new HashMap<>();

    protected void setup() {
        System.out.println("🚶 [Voyageur] Je suis prêt à résoudre le TSP !");

        // Comportement cyclique pour recevoir les messages
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                // Attente du message contenant la liste des villes
                MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = receive(template);
                if (msg != null) {
                    // Réception du message avec la liste des villes
                    String contenu = msg.getContent();
                    System.out.println("📩 [Voyageur] Reçu : " + contenu);

                    // Convertir les noms des villes reçues en positions
                    List<String> nomsVilles = Arrays.asList(contenu.split(","));
                    for (String ville : nomsVilles) {
                        villes.put(ville, new Position(Math.random() * 100, Math.random() * 100)); // Générer des positions aléatoires
                    }

                    // Résoudre le TSP
                    resoudreTSP();
                } else {
                    block(); // Attente d'un message
                }
            }
        });
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
