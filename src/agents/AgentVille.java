package agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import model.Ville;

public class AgentVille extends Agent {
    private Ville ville;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length == 3) {
            try {
                String nom = (String) args[0];
                double x = Double.parseDouble(args[1].toString());
                double y = Double.parseDouble(args[2].toString());

                ville = new Ville(nom, x, y);

                // ✅ Affichage au démarrage
                System.out.println("🟢 AgentVille '" + getLocalName() + "' créé : " +
                        ville.getNom() + " (" + ville.getX() + ", " + ville.getY() + ")");

                // 🔁 Comportement de réponse à GET_POSITION
                addBehaviour(new CyclicBehaviour() {
                    @Override
                    public void action() {
                        ACLMessage msg = receive();
                        if (msg != null) {
                            if (msg.getPerformative() == ACLMessage.REQUEST &&
                                    msg.getContent().equals("GET_POSITION")) {

                                ACLMessage reply = msg.createReply();
                                reply.setPerformative(ACLMessage.INFORM);
                                reply.setContent(ville.getX() + ";" + ville.getY());
                                send(reply);
                            }
                        } else {
                            block();
                        }
                    }
                });

            } catch (Exception e) {
                System.err.println("❌ Erreur de parsing des arguments pour AgentVille.");
                doDelete();
            }
        } else {
            System.err.println("⚠️ Paramètres manquants pour AgentVille.");
            doDelete();
        }
    }
}
