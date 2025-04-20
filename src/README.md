# Projet de Résolution du Problème du Voyageur de Commerce (PVC) via une Approche Multi-Agents

## Description du Projet
Ce projet implémente une solution au problème classique du voyageur de commerce (Traveling Salesman Problem - TSP) en utilisant une approche multi-agents basée sur la plateforme JADE (Java Agent DEvelopment Framework). Le problème consiste à trouver le chemin le plus court permettant de visiter un ensemble de villes et de revenir au point de départ.

Le système utilise deux approches différentes pour résoudre le problème :
1. Une approche gloutonne (greedy) avec l'AgentVoyageur
2. Une approche par algorithme génétique avec l'AgentGenetique

## Architecture du Système

Le système est composé de plusieurs agents qui interagissent pour résoudre le problème :

### Classes Principales

#### 1. Launcher (main/Launcher.java)
**Rôle** : Point d'entrée du système multi-agents.
- Initialise la plateforme JADE
- Crée le conteneur principal pour les agents
- Démarre l'agent contrôleur qui orchestrera le reste du système
- Configure l'interface graphique de JADE pour le monitoring

**Fonctions principales** :
- `main()` : Initialise l'environnement JADE et lance l'AgentControleur

#### 2. AgentControleur (agents/AgentControleur.java)
**Rôle** : Agent central qui coordonne l'ensemble du système.
- Charge les villes depuis un fichier JSON
- Crée les agents Ville avec leurs positions
- Crée l'agent Génétique pour résoudre le TSP
- Envoie la liste des villes à l'agent Génétique
- Reçoit et affiche la solution finale du problème

**Fonctions principales** :
- `setup()` : Initialise l'agent et lance les comportements
- `chargerVillesDepuisJson()` : Charge les données des villes depuis un fichier JSON
- `creerAgentVille()` : Crée un agent ville avec ses coordonnées
- `creerAgentGenetique()` : Crée l'agent qui résoudra le problème avec un algorithme génétique

#### 3. AgentVille (agents/AgentVille.java)
**Rôle** : Représente une ville avec une position géographique.
- Stocke ses coordonnées (x, y)
- Répond aux demandes de position des autres agents
- Utilise un comportement cyclique pour traiter les messages entrants

**Fonctions principales** :
- `setup()` : Initialise l'agent avec ses coordonnées et ajoute le comportement de réponse
- Comportement cyclique pour répondre aux requêtes GET_POSITION

#### 4. AgentVoyageur (agents/AgentVoyageur.java)
**Rôle** : Résout le problème du voyageur de commerce avec une approche gloutonne.
- Demande les positions à chaque ville
- Collecte les réponses des villes
- Calcule un itinéraire en utilisant l'algorithme du plus proche voisin (greedy)
- Envoie la solution au contrôleur

**Fonctions principales** :
- `setup()` : Initialise l'agent et lance la collecte des positions
- `CollectePositionBehaviour` : Comportement pour collecter les positions des villes
- `CalculTSPBehaviour` : Comportement pour calculer l'itinéraire optimal avec l'algorithme glouton

#### 5. AgentGenetique (agents/AgentGenetique.java)
**Rôle** : Résout le problème du voyageur de commerce avec un algorithme génétique.
- Reçoit la liste des villes du contrôleur
- Demande les positions à chaque ville
- Exécute un algorithme génétique pour trouver un itinéraire optimal
- Envoie la solution au contrôleur

**Fonctions principales** :
- `setup()` : Initialise l'agent et ajoute le comportement de réception de la liste des villes
- `ReceiveCityListBehaviour` : Comportement pour recevoir la liste des villes
- `CollectPositionsBehaviour` : Comportement pour collecter les positions des villes
- `RunGeneticAlgorithmBehaviour` : Comportement pour exécuter l'algorithme génétique
- `SendSolutionBehaviour` : Comportement pour envoyer la solution au contrôleur
- `initialiserPopulation()` : Initialise la population pour l'algorithme génétique
- `evaluerPopulation()` : Évalue la fitness de chaque individu dans la population
- `calculerDistanceTotale()` : Calcule la distance totale d'un chemin
- `selectionTournoi()` : Sélectionne des individus par tournoi
- `croisementOrdonne()` : Effectue un croisement ordonné entre deux parents
- `muter()` : Applique une mutation à un individu

#### 6. Ville (model/Ville.java)
**Rôle** : Classe modèle pour représenter une ville.
- Stocke le nom et les coordonnées (x, y) d'une ville
- Calcule la distance entre deux villes

**Fonctions principales** :
- `distanceTo()` : Calcule la distance euclidienne entre deux villes
- Getters pour accéder aux propriétés
- `equals()` et `hashCode()` pour les comparaisons

## Flux d'Exécution

1. Le Launcher démarre et initialise la plateforme JADE
2. L'AgentControleur est créé et exécuté
3. L'AgentControleur charge les villes depuis le fichier JSON
4. L'AgentControleur crée plusieurs AgentVille avec leurs positions
5. L'AgentControleur crée l'AgentGenetique
6. L'AgentControleur envoie la liste des noms des villes à l'AgentGenetique
7. L'AgentGenetique demande sa position à chaque AgentVille
8. Chaque AgentVille répond avec sa position
9. Une fois toutes les positions reçues, l'AgentGenetique calcule l'itinéraire optimal avec l'algorithme génétique
10. L'AgentGenetique envoie la solution à l'AgentControleur
11. L'AgentControleur affiche la solution finale

## Méthodologie GAIA

Le projet implémente la méthodologie GAIA pour la conception de systèmes multi-agents :
- Les agents sont organisés en rôles (Controleur, Ville, Voyageur, Génétique)
- Les interactions sont définies par des protocoles (GET_POSITION, LISTE_VILLES, SOLUTION_TSP)
- Les responsabilités et permissions sont clairement définies pour chaque rôle

## Sortie Attendue de l'Exécution

Lors de l'exécution du programme :
1. Le contrôleur affiche les positions initiales des villes créées
2. L'agent génétique demande les positions aux villes
3. Les villes répondent avec leurs positions
4. L'agent génétique calcule et affiche la meilleure tournée
5. L'agent génétique envoie la solution au contrôleur
6. Le contrôleur affiche la solution finale avec le chemin optimal et la distance totale