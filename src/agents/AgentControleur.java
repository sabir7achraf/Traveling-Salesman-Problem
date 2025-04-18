package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import java.util.Random;

public class AgentControleur extends Agent {

    private static final int NB_VILLES = 5;

    @Override
    protected void setup() {
        System.out.println("🟢 [Controleur] Agent démarré : " + getLocalName());

        ContainerController container = getContainerController();
        Random rand = new Random();

        // Création des villes
        for (int i = 0; i < NB_VILLES; i++) {
            double x = rand.nextDouble() * 100;
            double y = rand.nextDouble() * 100;
            try {
                AgentController ville = container.createNewAgent(
                        "Ville" + i,
                        "agents.AgentVille",
                        new Object[]{x, y}
                );
                ville.start();
                System.out.printf("🏙️ [Ville %d] Position : (%.2f, %.2f)%n", i, x, y);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Création de l'agent Voyageur
        try {
            AgentController voyageur = container.createNewAgent("Voyageur", "agents.AgentVoyageur", null);
            voyageur.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Attente courte pour s'assurer que le Voyageur est prêt
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Envoi de la liste des villes au voyageur
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("Voyageur", AID.ISLOCALNAME));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < NB_VILLES; i++) {
            sb.append("Ville").append(i).append(",");
        }
        // Supprimer la virgule finale
        msg.setContent(sb.substring(0, sb.length() - 1));
        send(msg);
        System.out.println("📨 [Controleur] Liste des villes envoyée au voyageur.");
    }
}
