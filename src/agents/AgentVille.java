package agents;

import jade.core.Agent;

public class AgentVille extends Agent {

    private double x;
    private double y;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length == 2) {
            this.x = (double) args[0];
            this.y = (double) args[1];
            System.out.printf("🏙️ [%s] Position initiale : (%.2f, %.2f)%n", getLocalName(), x, y);
        } else {
            System.out.println("⚠️ [Ville] Paramètres manquants !");
        }
    }
}
