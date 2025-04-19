package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AgentControleur extends Agent {

    private static final int NB_VILLES = 5;
    private final Random random = new Random();

    @Override
    protected void setup() {
        System.out.println("🟢 [Controleur] Agent démarré : " + getLocalName());

        ContainerController container = getContainerController();
        List<String> nomsVilles = new ArrayList<>();

        // Génération et création des villes
        for (int i = 0; i < NB_VILLES; i++) {
            double x = Math.round(random.nextDouble() * 10000.0) / 100.0;
            double y = Math.round(random.nextDouble() * 10000.0) / 100.0;
            String nom = "Ville" + i;

            try {
                AgentController ville = container.createNewAgent(nom, "agents.AgentVille", new Object[]{x, y});
                ville.start();
                nomsVilles.add(nom);
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la création de " + nom + " : " + e.getMessage());
            }
        }

        // Création de l'agent voyageur
        try {
            AgentController voyageur = container.createNewAgent("Voyageur", "agents.AgentVoyageur", null);
            voyageur.start();
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création du Voyageur : " + e.getMessage());
        }

        // Création de l'agent génétique
        try {
            AgentController genetique = container.createNewAgent("Genetique", "agents.AgentGenetique", null);
            genetique.start();
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création de l'agent génétique : " + e.getMessage());
        }

        // Envoi des noms des villes au voyageur
        ACLMessage msgVoyageur = new ACLMessage(ACLMessage.INFORM);
        msgVoyageur.addReceiver(new AID("Voyageur", AID.ISLOCALNAME));
        msgVoyageur.setContent(String.join(",", nomsVilles));
        send(msgVoyageur);

        // Envoi des noms des villes à l'agent génétique
        ACLMessage msgGenetique = new ACLMessage(ACLMessage.INFORM);
        msgGenetique.addReceiver(new AID("Genetique", AID.ISLOCALNAME));
        msgGenetique.setContent(String.join(",", nomsVilles));
        send(msgGenetique);

        System.out.println("📨 [Controleur] Liste des villes envoyée au Voyageur et à l'agent Génétique.");
    }
}
