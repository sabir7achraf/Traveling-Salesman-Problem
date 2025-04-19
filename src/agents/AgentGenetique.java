package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import model.Ville;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AgentGenetique extends Agent {

    // Paramètres de l'algorithme génétique (ajustables)
    private static final int TAILLE_POPULATION = 50;
    private static final int NOMBRE_GENERATIONS = 100;
    private static final double TAUX_MUTATION = 0.05; // 5% de chance de mutation par gène (ville)
    private static final double TAUX_CROISEMENT = 0.8; // 80% de chance de croisement
    private static final int TAILLE_TOURNOI = 5; // Pour la sélection par tournoi
    private static final boolean ELITISME = true; // Conserver le meilleur individu ?

    private List<String> nomsVilles;
    private Map<String, Ville> donneesVilles = new HashMap<>(); // Stocker les objets Ville récupérés
    private AID controleurAID; // Pour savoir à qui envoyer la solution

    private List<List<String>> population; // La population de chemins (individus)
    private List<String> meilleurCheminGlobal = null;
    private double meilleureDistanceGlobale = Double.POSITIVE_INFINITY;


    @Override
    protected void setup() {
        System.out.println("🧬 AgentGenetique '" + getLocalName() + "' démarré.");

        Object[] args = getArguments();
        if(args != null && args.length > 0) {
            controleurAID = (AID) args[0]; // Récupère l'AID du contrôleur
        } else {
            System.err.println("❌ AgentGenetique n'a pas reçu l'AID du contrôleur ! Arrêt.");
            doDelete();
            return;
        }

        // Comportement principal séquentiel
        SequentialBehaviour mainSequence = new SequentialBehaviour();

        // Étape 1: Recevoir la liste des villes du contrôleur
        mainSequence.addSubBehaviour(new ReceiveCityListBehaviour());

        // Étape 2: Obtenir les positions de toutes les villes
        mainSequence.addSubBehaviour(new CollectPositionsBehaviour());

        // Étape 3: Exécuter l'algorithme génétique
        mainSequence.addSubBehaviour(new RunGeneticAlgorithmBehaviour());

        // Étape 4: Envoyer la meilleure solution au contrôleur
        mainSequence.addSubBehaviour(new SendSolutionBehaviour());

        addBehaviour(mainSequence);
    }

    // --- Comportement 1: Recevoir la liste des noms de villes ---
    private class ReceiveCityListBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println("🧬 AgentGenetique : Attente de la liste des villes...");
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchSender(controleurAID) // S'assurer que ça vient du bon contrôleur
            );
            ACLMessage msg = blockingReceive(mt); // Attend le message
            if (msg != null && msg.getContent().startsWith("LISTE_VILLES:")) {
                String content = msg.getContent().substring("LISTE_VILLES:".length());
                nomsVilles = Arrays.asList(content.split(","));
                System.out.println("📥 AgentGenetique : Liste des villes reçue (" + nomsVilles.size() + ") : " + nomsVilles);
            } else {
                System.err.println("❌ AgentGenetique : N'a pas reçu la liste des villes correctement.");
                // Gérer l'erreur, peut-être arrêter l'agent
                doDelete();
            }
        }
    }

    // --- Comportement 2: Collecter les positions des villes ---
    private class CollectPositionsBehaviour extends Behaviour {
        private int step = 0;
        private int repliesCount = 0;
        private MessageTemplate template; // Pour filtrer les réponses attendues
        private AID[] cityAgents; // Les AID des agents Ville

        @Override
        public void onStart() {
            if (nomsVilles == null || nomsVilles.isEmpty()) {
                System.err.println("❌ AgentGenetique: Impossible de collecter les positions, liste de villes vide.");
                step = 2; // Marquer comme terminé (en erreur)
                return;
            }
            System.out.println("🧬 AgentGenetique : Début de la collecte des positions...");
            cityAgents = nomsVilles.stream().map(name -> new AID(name, AID.ISLOCALNAME)).toArray(AID[]::new);
        }

        @Override
        public void action() {
            switch (step) {
                case 0: // Envoyer toutes les requêtes
                    System.out.println("✉️ AgentGenetique : Envoi des requêtes GET_POSITION à " + cityAgents.length + " villes.");
                    ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                    request.setContent("GET_POSITION");
                    for (AID cityAgent : cityAgents) {
                        request.addReceiver(cityAgent);
                    }
                    // Utiliser un ID de conversation unique pour filtrer les réponses
                    request.setConversationId("get-pos-" + System.currentTimeMillis());
                    myAgent.send(request);
                    // Préparer le template pour recevoir les réponses
                    template = MessageTemplate.and(
                            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                            MessageTemplate.MatchConversationId(request.getConversationId())
                    );
                    step = 1;
                    break;

                case 1: // Recevoir les réponses
                    ACLMessage reply = myAgent.receive(template);
                    if (reply != null) {
                        repliesCount++;
                        String senderName = reply.getSender().getLocalName();
                        String content = reply.getContent();
                        try {
                            String[] coords = content.split(";");
                            double x = Double.parseDouble(coords[0]);
                            double y = Double.parseDouble(coords[1]);
                            donneesVilles.put(senderName, new Ville(senderName, x, y));
                            System.out.println("📍 AgentGenetique : Position reçue de " + senderName + " (" + x + ", " + y + ") - " + repliesCount + "/" + cityAgents.length);
                        } catch (Exception e) {
                            System.err.println("❌ AgentGenetique : Erreur parsing réponse de " + senderName + ": " + content);
                        }
                    } else {
                        block(); // Attendre un peu si pas de message
                    }
                    break;
                case 2: // État d'erreur ou terminé
                    // Ne rien faire ici, done() gère la fin
                    break;
            }
        }

        @Override
        public boolean done() {
            // Terminé si toutes les réponses sont arrivées ou si on est en état d'erreur
            boolean finished = (step == 1 && repliesCount == cityAgents.length) || step == 2;
            if(finished && step != 2){
                System.out.println("✅ AgentGenetique : Toutes les positions ("+ donneesVilles.size() +") ont été collectées.");
            } else if (finished && step == 2) {
                System.err.println("❌ AgentGenetique : La collecte des positions a échoué.");
            }
            return finished;
        }
    }

    // --- Comportement 3: Exécuter l'Algorithme Génétique ---
    private class RunGeneticAlgorithmBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            if (donneesVilles.size() != nomsVilles.size()) {
                System.err.println("❌ AgentGenetique : Données de villes incomplètes ("+ donneesVilles.size() +"/"+nomsVilles.size()+"). Annulation de l'AG.");
                meilleurCheminGlobal = null; // Indiquer l'échec
                return;
            }
            System.out.println("\n========= 🧬 LANCEMENT ALGORITHME GÉNÉTIQUE 🧬 =========");
            System.out.println("Paramètres : Population=" + TAILLE_POPULATION + ", Générations=" + NOMBRE_GENERATIONS +
                    ", Mutation=" + TAUX_MUTATION + ", Croisement=" + TAUX_CROISEMENT +
                    ", Tournoi=" + TAILLE_TOURNOI + ", Elitisme=" + ELITISME);

            // 1. Initialisation de la population
            initialiserPopulation();
            evaluerPopulation(); // Évaluer la population initiale

            // 2. Boucle des générations
            for (int generation = 0; generation < NOMBRE_GENERATIONS; generation++) {
                List<List<String>> nouvellePopulation = new ArrayList<>();

                // (Optionnel) Elitisme : copier le meilleur individu
                int eliteOffset = 0;
                if (ELITISME) {
                    nouvellePopulation.add(getMeilleurIndividu(population));
                    eliteOffset = 1;
                }

                // Remplir le reste de la nouvelle population
                while (nouvellePopulation.size() < TAILLE_POPULATION) {
                    // 3. Sélection des parents
                    List<String> parent1 = selectionTournoi(population);
                    List<String> parent2 = selectionTournoi(population);

                    List<String> enfant1 = parent1; // Par défaut
                    List<String> enfant2 = parent2; // Par défaut

                    // 4. Croisement (si condition remplie)
                    if (Math.random() < TAUX_CROISEMENT) {
                        List<List<String>> enfants = croisementOrdonne(parent1, parent2);
                        enfant1 = enfants.get(0);
                        enfant2 = enfants.get(1);
                    }


                    // 5. Mutation (sur les enfants potentiels)
                    muter(enfant1);
                    if(nouvellePopulation.size() + 1 < TAILLE_POPULATION) { // Vérifier si on a la place pour le 2e enfant
                        muter(enfant2);
                    }


                    // Ajouter le(s) enfant(s) à la nouvelle population
                    nouvellePopulation.add(enfant1);
                    if (nouvellePopulation.size() < TAILLE_POPULATION) {
                        nouvellePopulation.add(enfant2);
                    }
                }

                population = nouvellePopulation;

                // 6. Évaluation de la nouvelle population
                evaluerPopulation(); // Met à jour meilleurCheminGlobal si un meilleur est trouvé

                // Affichage (par exemple, toutes les 10 générations)
                if ((generation + 1) % 10 == 0 || generation == NOMBRE_GENERATIONS - 1) {
                    System.out.printf("🧬 Génération %d/%d - Meilleure distance : %.2f\n",
                            generation + 1, NOMBRE_GENERATIONS, meilleureDistanceGlobale);
                }
            }

            System.out.println("========= ✅ FIN ALGORITHME GÉNÉTIQUE ✅ =========");
            if (meilleurCheminGlobal != null) {
                System.out.println("🏆 Meilleur chemin trouvé : " + formatPath(meilleurCheminGlobal));
                System.out.printf("📏 Distance : %.2f\n", meilleureDistanceGlobale);
            } else {
                System.out.println("⚠️ Aucun chemin valide trouvé.");
            }
        }
    }

    // --- Comportement 4: Envoyer la solution au Contrôleur ---
    private class SendSolutionBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println("🧬 AgentGenetique : Préparation de l'envoi de la solution...");
            if (meilleurCheminGlobal != null && controleurAID != null) {
                ACLMessage resultMsg = new ACLMessage(ACLMessage.INFORM);
                resultMsg.addReceiver(controleurAID);

                // Format: SOLUTION_TSP:ville1;ville2;...;ville1|distance
                String pathString = String.join(";", meilleurCheminGlobal);
                // Assurer que le chemin boucle en ajoutant le point de départ à la fin pour l'affichage/compréhension
                if (!meilleurCheminGlobal.isEmpty()) {
                    pathString += ";" + meilleurCheminGlobal.get(0);
                }

                resultMsg.setContent(String.format("SOLUTION_TSP:%s|%.2f", pathString, meilleureDistanceGlobale));
                resultMsg.setConversationId("tsp-solution-" + System.currentTimeMillis());
                myAgent.send(resultMsg);
                System.out.println("✅ AgentGenetique : Solution envoyée au contrôleur.");
            } else {
                System.err.println("❌ AgentGenetique : Impossible d'envoyer la solution (chemin non trouvé ou contrôleur inconnu).");
                // Envoyer un message d'échec ?
                ACLMessage failMsg = new ACLMessage(ACLMessage.FAILURE);
                failMsg.addReceiver(controleurAID);
                failMsg.setContent("ECHEC_TSP:Impossible de calculer un chemin.");
                myAgent.send(failMsg);
            }
            // L'agent a terminé sa tâche principale
            // doDelete(); // Optionnel : arrêter l'agent après avoir envoyé la solution
        }
    }


    // --- Méthodes de l'Algorithme Génétique ---

    private void initialiserPopulation() {
        population = new ArrayList<>(TAILLE_POPULATION);
        // Créer une liste de base des villes (excluant potentiellement le point de départ si on le fixe)
        // Ici, on inclut toutes les villes et on les mélange
        List<String> base = new ArrayList<>(nomsVilles);
        for (int i = 0; i < TAILLE_POPULATION; i++) {
            Collections.shuffle(base);
            population.add(new ArrayList<>(base)); // Ajouter une copie mélangée
        }
        System.out.println("👶 Population initiale (" + population.size() + ") générée.");
    }

    private void evaluerPopulation() {
        // On cherche la *plus petite* distance
        for (List<String> individu : population) {
            double distance = calculerDistanceTotale(individu);
            if (distance < meilleureDistanceGlobale) {
                meilleureDistanceGlobale = distance;
                meilleurCheminGlobal = new ArrayList<>(individu); // Copier le meilleur chemin
                // Affichage optionnel de l'amélioration
                // System.out.printf("✨ Nouvelle meilleure distance : %.2f\n", meilleureDistanceGlobale);
            }
        }
    }

    private double calculerDistanceTotale(List<String> chemin) {
        double distance = 0.0;
        if (chemin == null || chemin.size() < 2) return Double.POSITIVE_INFINITY;

        for (int i = 0; i < chemin.size() - 1; i++) {
            Ville v1 = donneesVilles.get(chemin.get(i));
            Ville v2 = donneesVilles.get(chemin.get(i + 1));
            if (v1 != null && v2 != null) {
                distance += v1.distanceTo(v2);
            } else {
                return Double.POSITIVE_INFINITY; // Chemin invalide si une ville manque
            }
        }
        // Ajouter la distance pour revenir au point de départ
        Ville derniere = donneesVilles.get(chemin.get(chemin.size() - 1));
        Ville premiere = donneesVilles.get(chemin.get(0));
        if (derniere != null && premiere != null) {
            distance += derniere.distanceTo(premiere);
        } else {
            return Double.POSITIVE_INFINITY;
        }

        return distance;
    }

    private List<String> getMeilleurIndividu(List<List<String>> pop) {
        return Collections.min(pop, Comparator.comparingDouble(this::calculerDistanceTotale));
    }


    // Sélection par Tournoi
    private List<String> selectionTournoi(List<List<String>> pop) {
        List<List<String>> tournoi = new ArrayList<>(TAILLE_TOURNOI);
        Random rand = new Random();
        for (int i = 0; i < TAILLE_TOURNOI; i++) {
            int indexAleatoire = rand.nextInt(pop.size());
            tournoi.add(pop.get(indexAleatoire));
        }
        // Retourner le meilleur du tournoi (celui avec la plus petite distance)
        return getMeilleurIndividu(tournoi);
    }

    // Croisement Ordonné (Ordered Crossover - OX1)
    private List<List<String>> croisementOrdonne(List<String> parent1, List<String> parent2) {
        int taille = parent1.size();
        Random rand = new Random();
        int start = rand.nextInt(taille);
        int end = rand.nextInt(taille);

        // Assurer start < end (ou gérer le cas circulaire)
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }
        if (start == end) { // Si points identiques, pas de croisement efficace, retourner les parents
            return Arrays.asList(new ArrayList<>(parent1), new ArrayList<>(parent2));
        }

        // Créer les enfants avec des placeholders (null)
        List<String> enfant1 = new ArrayList<>(Collections.nCopies(taille, null));
        List<String> enfant2 = new ArrayList<>(Collections.nCopies(taille, null));

        // Copier la section du croisement directement
        List<String> section1 = new ArrayList<>();
        List<String> section2 = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            String gene1 = parent1.get(i);
            String gene2 = parent2.get(i);
            enfant1.set(i, gene1);
            enfant2.set(i, gene2);
            section1.add(gene1);
            section2.add(gene2);
        }


        // Remplir le reste de l'enfant 1 avec les gènes de parent 2 (dans l'ordre)
        int currentP2 = (end + 1) % taille;
        int currentE1 = (end + 1) % taille;
        while (enfant1.contains(null)) {
            String geneP2 = parent2.get(currentP2);
            if (!section1.contains(geneP2)) { // Si le gène n'est pas déjà dans la section copiée
                enfant1.set(currentE1, geneP2);
                currentE1 = (currentE1 + 1) % taille;
            }
            currentP2 = (currentP2 + 1) % taille; // Passer au gène suivant du parent 2
        }


        // Remplir le reste de l'enfant 2 avec les gènes de parent 1 (dans l'ordre)
        int currentP1 = (end + 1) % taille;
        int currentE2 = (end + 1) % taille;
        while (enfant2.contains(null)) {
            String geneP1 = parent1.get(currentP1);
            if (!section2.contains(geneP1)) {
                enfant2.set(currentE2, geneP1);
                currentE2 = (currentE2 + 1) % taille;
            }
            currentP1 = (currentP1 + 1) % taille;
        }


        return Arrays.asList(enfant1, enfant2);
    }


    // Mutation par Échange (Swap Mutation)
    private void muter(List<String> individu) {
        Random rand = new Random();
        for (int i = 0; i < individu.size(); i++) {
            if (Math.random() < TAUX_MUTATION) {
                // Choisir une autre position aléatoire pour échanger
                int j = rand.nextInt(individu.size());
                // Échanger les villes aux positions i et j
                Collections.swap(individu, i, j);
            }
        }
    }

    private String formatPath(List<String> path) {
        if (path == null || path.isEmpty()) return "[]";
        // Ajoute le point de départ à la fin pour montrer la boucle
        return path.stream().collect(Collectors.joining(" -> ")) + " -> " + path.get(0);
    }

}