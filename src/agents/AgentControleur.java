package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class AgentControleur extends Agent {
    protected void setup() {
        System.out.println("🟢 [Controleur] Agent démarré : " + getLocalName());

        // Création du message contenant la liste des villes
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(new AID("Voyageur", AID.ISLOCALNAME));
        message.setContent("Ville0,Ville1,Ville2,Ville3,Ville4");
        send(message);

        System.out.println("📨 [Controleur] Liste des villes envoyée au voyageur.");
    }
}
