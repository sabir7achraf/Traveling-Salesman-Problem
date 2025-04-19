package main;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Launcher {
    public static void main(String[] args) {
        // Configuration du conteneur principal
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.MAIN_PORT, "1099");

        // Lancer la plateforme JADE
        AgentContainer container = runtime.createMainContainer(profile);

        try {
            // Lancer les agents
            AgentController controleur = container.createNewAgent("Controleur", "agents.AgentControleur", new Object[]{});
            AgentController voyageur = container.createNewAgent("Voyageur", "agents.AgentVoyageur", new Object[]{});
            AgentController ville0 = container.createNewAgent("Ville0", "agents.AgentVille", new Object[]{});
            AgentController ville1 = container.createNewAgent("Ville1", "agents.AgentVille", new Object[]{});
            AgentController ville2 = container.createNewAgent("Ville2", "agents.AgentVille", new Object[]{});
            AgentController ville3 = container.createNewAgent("Ville3", "agents.AgentVille", new Object[]{});
            AgentController ville4 = container.createNewAgent("Ville4", "agents.AgentVille", new Object[]{});

            controleur.start();
            voyageur.start();
            ville0.start();
            ville1.start();
            ville2.start();
            ville3.start();
            ville4.start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
