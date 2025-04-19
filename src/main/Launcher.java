package main;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class Launcher {
    public static void main(String[] args) {
        // Création de l'environnement JADE
        Runtime rt = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN, "true"); // Conteneur principal
        profile.setParameter(Profile.GUI, "true");  // Activer l'interface graphique JADE

        // Création du conteneur principal
        ContainerController mainContainer = rt.createMainContainer(profile);

        try {
            // Lancement de l'agent Controleur (les autres agents seront créés par lui)
            AgentController controleur = mainContainer.createNewAgent("Controleur", "agents.AgentControleur", null);
            controleur.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
