package agents;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import model.Ville;

import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AgentControleur extends Agent {

    private List<String> nomsVilles = new ArrayList<>();
    private String geneticAgentName = "AgentTSP_Solver"; // Nom de l'agent génétique

    @Override
    protected void setup() {
        System.out.println("🟡 AgentControleur '" + getLocalName() + "' démarré.");

        List<Ville> villes = chargerVillesDepuisJson("/home/sabir7achraf/IdeaProjects/MultiAgent/src/resources/villes.json"); // Chemin vers votre fichier

        if (villes == null || villes.isEmpty()) {
            System.err.println("❌ Aucune ville chargée depuis le JSON. Arrêt.");
            doDelete();
            return;
        }

        try {
            AgentContainer container = getContainerController();

            // Création dynamique des agents Ville
            for (Ville v : villes) {
                creerAgentVille(container, v);
                nomsVilles.add(v.getNom()); // Garder trace des noms
            }
            System.out.println("✅ Agents Ville créés dynamiquement : " + nomsVilles);

            // Création de l'Agent Génétique qui résoudra le TSP
            creerAgentGenetique(container, geneticAgentName);

            // Envoyer la liste des noms de villes à l'Agent Génétique
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID(geneticAgentName, AID.ISLOCALNAME));
            msg.setContent("LISTE_VILLES:" + String.join(",", nomsVilles)); // Format simple : LISTE_VILLES:nom1,nom2,...
            send(msg);
            System.out.println("✉️ AgentControleur a envoyé la liste des villes à " + geneticAgentName);


        } catch (StaleProxyException e) {
            System.err.println("❌ Erreur JADE (StaleProxyException) lors de la création des agents.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors du setup du Controleur : " + e.getMessage());
            e.printStackTrace();
        }


        // Comportement pour recevoir la solution finale de l'Agent Génétique
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                MessageTemplate mt = MessageTemplate.and(
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                        MessageTemplate.MatchSender(new AID(geneticAgentName, AID.ISLOCALNAME)) // Écouter seulement l'agent solveur
                );
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    System.out.println("📬 [Contrôleur] Message reçu de " + msg.getSender().getLocalName() + " contenu : " + msg.getContent());
                    // Attendre un message spécifique, par exemple "SOLUTION_TSP:chemin|distance"
                    if (msg.getContent().startsWith("SOLUTION_TSP:")) {
                        String data = msg.getContent().substring("SOLUTION_TSP:".length());
                        String[] parts = data.split("\\|");
                        if(parts.length == 2) {
                            String chemin = parts[0];
                            String distance = parts[1];
                            System.out.println("\n\n================ SOLUTION FINALE TSP ================");
                            System.out.println("🧬 Reçue de : " + msg.getSender().getLocalName());
                            System.out.println("🏆 Chemin optimal trouvé : " + chemin.replace(";", " -> "));
                            System.out.println("📏 Distance totale : " + distance);
                            System.out.println("==================================================\n");
                            // Ici, on pourrait arrêter le système ou lancer une autre tâche
                            // doDelete(); // Optionnel : arrêter le contrôleur une fois la solution reçue
                        } else {
                            System.err.println("⚠️ [Contrôleur] Format de message SOLUTION_TSP invalide reçu: " + msg.getContent());
                        }

                    }
                } else {
                    block();
                }
            }
        });
    }

    private List<Ville> chargerVillesDepuisJson(String cheminFichier) {
        Gson gson = new Gson();
        Type typeListeDeVilles = new TypeToken<ArrayList<Ville>>() {}.getType();
        try (Reader reader = new FileReader(cheminFichier)) {
            List<Ville> villes = gson.fromJson(reader, typeListeDeVilles);
            System.out.println("📄 Villes chargées depuis '" + cheminFichier + "' : " + villes.size() + " villes.");
            return villes;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la lecture du fichier JSON '" + cheminFichier + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void creerAgentVille(AgentContainer container, Ville ville) throws StaleProxyException {
        // Passer les coordonnées comme Double, pas String
        Object[] args = new Object[]{ville.getNom(), ville.getX(), ville.getY()};
        AgentController agentVilleCtrl = container.createNewAgent(
                ville.getNom(),         // Nom de l'agent = nom de la ville
                "agents.AgentVille",    // Classe de l'agent
                args                    // Arguments (nom, x, y)
        );
        agentVilleCtrl.start();
    }

    private void creerAgentGenetique(AgentContainer container, String agentName) throws StaleProxyException {
        // Passer l'AID du contrôleur à l'agent génétique pour qu'il puisse répondre
        Object[] args = new Object[]{ getAID() };
        AgentController agentGenetiqueCtrl = container.createNewAgent(
                agentName,
                "agents.AgentGenetique", // Classe de l'agent génétique
                args
        );
        agentGenetiqueCtrl.start();
    }

    // Optionnel : Arrêter proprement les agents ville à la fin
    @Override
    protected void takeDown() {
        System.out.println("🟡 AgentControleur '" + getLocalName() + "' en cours d'arrêt.");
        // Si on veut arrêter les agents ville:
        // AgentContainer container = getContainerController();
        // for (String nom : nomsVilles) {
        //     try {
        //         AgentController ac = container.getAgent(nom);
        //         if (ac != null) ac.kill();
        //     } catch (Exception e) {
        //         // Ignorer si l'agent n'existe plus ou autre erreur
        //     }
        // }
        // try {
        //     AgentController ag = container.getAgent(geneticAgentName);
        //      if (ag != null) ag.kill();
        // } catch (Exception e) {}
    }
}