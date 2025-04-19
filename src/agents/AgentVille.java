package agents;

import jade.core.Agent;
import util.Position;

public class AgentVille extends Agent {
    private Position position;

    protected void setup() {
        // Générer une position aléatoire
        this.position = new Position(Math.random() * 100, Math.random() * 100);

        System.out.println("🏙️ [" + getLocalName() + "] Position : (" + position.x + ", " + position.y + ")");
    }
}
