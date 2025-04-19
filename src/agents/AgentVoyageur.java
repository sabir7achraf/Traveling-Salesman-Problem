package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import model.Ville;

import java.util.*;

public class AgentVoyageur extends Agent {

    private List<String> nomsVilles = Arrays.asList("Ville1", "Ville2", "Ville3", "Ville4", "Ville5");
    private Map<String, Ville> villesRecues = new HashMap<>();
    private String controleur;

    @Override
    protected void setup() {
        System.out.println("🔵 AgentVoyageur '" + getLocalName() + "' prêt.");

        Object[] args = getArguments();
        if (args != null && args.length >= 1) {
            controleur = (String) args[0];
        }

        // Démarrer la collecte des positions
        addBehaviour(new CollectePositionBehaviour());
    }

    private class CollectePositionBehaviour extends Behaviour {
        private int index = 0;
        private int responses = 0;

        @Override
        public void action() {
            // Étape 1 : Envoyer la requête à la ville courante
            if (index < nomsVilles.size()) {
                String nomVille = nomsVilles.get(index);
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.setContent("GET_POSITION");
                msg.addReceiver(new AID(nomVille, AID.ISLOCALNAME));
                send(msg);
                index++;
                block(500); // Pause pour laisser le temps aux réponses
            }

            // Étape 2 : Réception des réponses
            ACLMessage reply = receive();
            if (reply != null && reply.getPerformative() == ACLMessage.INFORM) {
                String nom = reply.getSender().getLocalName();
                String[] coords = reply.getContent().split(";");
                double x = Double.parseDouble(coords[0]);
                double y = Double.parseDouble(coords[1]);
                villesRecues.put(nom, new Ville(nom, x, y));
                responses++;
            } else {
                block();
            }
        }

        @Override
        public boolean done() {
            return responses == nomsVilles.size();
        }

        @Override
        public int onEnd() {
            System.out.println("✅ Positions collectées :");
            for (Ville v : villesRecues.values()) {
                System.out.println(" - " + v);
            }

            addBehaviour(new CalculTSPBehaviour());
            return super.onEnd();
        }
    }

    private class CalculTSPBehaviour extends Behaviour {
        private boolean done = false;

        @Override
        public void action() {
            List<Ville> villes = new ArrayList<>(villesRecues.values());
            if (villes.isEmpty()) {
                done = true;
                return;
            }

            Set<Ville> nonVisitees = new HashSet<>(villes);
            List<Ville> chemin = new ArrayList<>();

            // On commence par une ville aléatoire
            Ville depart = villes.get(new Random().nextInt(villes.size()));
            Ville current = depart;
            chemin.add(current);
            nonVisitees.remove(current);

            double distanceTotale = 0;

            while (!nonVisitees.isEmpty()) {
                Ville plusProche = null;
                double distanceMin = Double.MAX_VALUE;

                for (Ville v : nonVisitees) {
                    double d = current.distanceTo(v);
                    if (d < distanceMin) {
                        distanceMin = d;
                        plusProche = v;
                    }
                }

                distanceTotale += distanceMin;
                chemin.add(plusProche);
                nonVisitees.remove(plusProche);
                current = plusProche;
            }

            // Retour au point de départ
            distanceTotale += current.distanceTo(depart);
            chemin.add(depart); // pour boucler la tournée

            // 🖨️ Affichage
            System.out.println("\n🔁 TSP (Greedy) :");
            chemin.forEach(v -> System.out.print(v.getNom() + " → "));
            System.out.println("\n📏 Distance totale : " + distanceTotale);


            // Construction du chemin en string
            StringBuilder cheminStr = new StringBuilder();
            for (Ville v : chemin) {
                cheminStr.append(v.getNom()).append(";");
            }
            cheminStr.deleteCharAt(cheminStr.length() - 1); // enlever le dernier ";"

// Envoi au contrôleur : format = CHEMIN:ville1;ville2;...|DISTANCE:123.45
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID(controleur, AID.ISLOCALNAME));
            msg.setContent("CHEMIN:" + cheminStr + "|DISTANCE:" + distanceTotale);
            send(msg);




            done = true;
        }

        @Override
        public boolean done() {
            return done;
        }
    }

}
