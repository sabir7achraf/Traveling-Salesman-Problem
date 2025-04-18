package main;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.core.Runtime;

public class Launcher {

    public static void main(String[] args) {
        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.GUI, "true"); // pour démarrer avec l’interface graphique

        AgentContainer container = rt.createMainContainer(p);
        try {
            // Ajout de l’agent contrôleur
            AgentController ac = container.createNewAgent("Controleur", "agents.AgentControleur", null);
            ac.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
