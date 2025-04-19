# Explication détaillée des classes du projet

Ce document explique le rôle de chaque classe dans le projet de résolution du Problème du Voyageur de Commerce (PVC) via une approche multi-agents.

## 1. Classes principales

### 1.1 Launcher (main/Launcher.java)
**Rôle** : Point d'entrée du système multi-agents.
- Initialise la plateforme JADE
- Crée le conteneur principal pour les agents
- Démarre l'agent contrôleur qui orchestrera le reste du système
- Configure l'interface graphique de JADE pour le monitoring

### 1.2 AgentControleur (agents/AgentControleur.java)
**Rôle** : Agent central qui coordonne l'ensemble du système.
- Crée les agents Ville avec des positions aléatoires
- Crée l'agent Voyageur
- Envoie la liste des villes au Voyageur
- Reçoit et affiche la solution finale du problème
- Implémente le rôle "Controleur" dans le cadre de la méthodologie GAIA

### 1.3 AgentVille (agents/AgentVille.java)
**Rôle** : Représente une ville avec une position géographique.
- Stocke ses coordonnées (x, y)
- Répond aux demandes de position de l'agent Voyageur
- Implémente le rôle "Ville" dans le cadre de la méthodologie GAIA
- Utilise un comportement cyclique pour traiter les messages entrants

### 1.4 AgentVoyageur (agents/AgentVoyageur.java)
**Rôle** : Résout le problème du voyageur de commerce.
- Demande les positions à chaque ville
- Collecte les réponses des villes
- Calcule toutes les permutations possibles des itinéraires
- Détermine l'itinéraire optimal (distance minimale)
- Envoie la solution au contrôleur
- Implémente le rôle "Voyageur" dans le cadre de la méthodologie GAIA

## 2. Classes du framework GAIA

### 2.1 GaiaAgent (gaia/GaiaAgent.java)
**Rôle** : Classe abstraite qui étend l'agent JADE standard.
- Fournit des fonctionnalités communes à tous les agents GAIA
- Implémente une méthode de journalisation (log)
- Définit une méthode abstraite execute() que tous les agents doivent implémenter

### 2.2 Role (gaia/Role.java)
**Rôle** : Interface définissant les comportements d'un rôle dans la méthodologie GAIA.
- Définit la méthode getRoleName() pour identifier le rôle
- Définit la méthode performRole() pour exécuter les comportements associés au rôle

### 2.3 Protocol (gaia/Protocol.java)
**Rôle** : Classe utilitaire définissant les protocoles de communication.
- REQUEST_POSITIONS : protocole pour demander les positions des villes
- SEND_POSITION : protocole pour envoyer une position
- REPORT_SOLUTION : protocole pour rapporter la solution du TSP

### 2.4 Ontology (gaia/Ontology.java)
**Rôle** : Classe utilitaire définissant le vocabulaire commun du système.
- POSITION : terme utilisé pour les messages concernant les positions
- SOLUTION : terme utilisé pour les messages concernant la solution du TSP

## 3. Classes utilitaires

### 3.1 Position (util/Position.java)
**Rôle** : Représente une position géographique en 2D.
- Stocke les coordonnées x et y
- Calcule la distance euclidienne entre deux positions

### 3.2 Permutateur (util/Permutateur.java)
**Rôle** : Génère toutes les permutations possibles d'une liste d'éléments.
- Utilisé par l'AgentVoyageur pour générer tous les itinéraires possibles
- Implémente un algorithme récursif de génération de permutations

### 3.3 Ville (model/Ville.java)
**Rôle** : Classe modèle pour représenter une ville.
- Cette classe est vide dans l'implémentation actuelle, car les fonctionnalités sont gérées par AgentVille

## 4. Flux d'exécution

1. Le Launcher démarre et initialise la plateforme JADE
2. L'AgentControleur est créé et exécuté
3. L'AgentControleur crée plusieurs AgentVille avec des positions aléatoires
4. L'AgentControleur crée l'AgentVoyageur
5. L'AgentControleur envoie la liste des noms des villes à l'AgentVoyageur
6. L'AgentVoyageur demande sa position à chaque AgentVille
7. Chaque AgentVille répond avec sa position
8. Une fois toutes les positions reçues, l'AgentVoyageur calcule l'itinéraire optimal
9. L'AgentVoyageur envoie la solution à l'AgentControleur
10. L'AgentControleur affiche la solution

## 5. Méthodologie GAIA

Le projet implémente la méthodologie GAIA pour la conception de systèmes multi-agents :
- Les agents sont organisés en rôles (Controleur, Ville, Voyageur)
- Les interactions sont définies par des protocoles (REQUEST_POSITIONS, SEND_POSITION, REPORT_SOLUTION)
- Le vocabulaire commun est défini par une ontologie (POSITION, SOLUTION)
- Les responsabilités et permissions sont clairement définies pour chaque rôle

## 6. Sortie attendue de l'exécution

Lors de l'exécution du programme :
1. Le contrôleur affiche les positions initiales des villes créées
2. Le voyageur demande les positions aux villes
3. Les villes répondent avec leurs positions
4. Le voyageur calcule et affiche la meilleure tournée
5. Le voyageur envoie la solution au contrôleur
6. Le contrôleur affiche la solution finale

Cette sortie permet de visualiser le processus de résolution du problème du voyageur de commerce par le système multi-agents.