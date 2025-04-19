package agents;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;

public class AgentGenetique extends Agent {

    private List<String> villes;

    @Override
    protected void setup() {
        System.out.println("🧬 [AgentGenetique] Démarré");

        // Comportement pour recevoir la liste des villes
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = blockingReceive(mt);
                if (msg != null) {
                    String contenu = msg.getContent();
                    villes = Arrays.asList(contenu.split(","));
                    System.out.println("📥 [AgentGenetique] Reçu les villes : " + villes);

                    // Génération de la population initiale
                    List<List<String>> population = genererPopulation(villes, 10); // population de 10 chemins
                    System.out.println("👶 [AgentGenetique] Population initiale générée :");
                    for (List<String> individu : population) {
                        System.out.println("   ➤ " + individu);
                    }
                }
            }
        });
    }

    private List<List<String>> genererPopulation(List<String> villes, int taillePopulation) {
        List<List<String>> population = new ArrayList<>();
        for (int i = 0; i < taillePopulation; i++) {
            List<String> individu = new ArrayList<>(villes);
            Collections.shuffle(individu);
            population.add(individu);
        }
        return population;
    }
}
