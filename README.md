# COMPILATEUR DECA
Projet Génie Logiciel 2024–2025
Grenoble INP – Ensimag

## DESCRIPTION

Ce projet consiste en la conception et l’implémentation d’un compilateur complet pour le langage Deca.
Le compilateur traduit un code source Deca en code assembleur exécutable par la machine virtuelle IMA.

Le pipeline de compilation comprend :
* Analyse lexicale (tokenisation)
* Analyse syntaxique (construction de l’AST)
* Analyse sémantique (vérifications contextuelles et typage)
* Génération de code assembleur
* Optimisations optionnelles

## ARCHITECTURE

Le compilateur est structuré en plusieurs phases indépendantes :
* Parsing
* Découpage du code en tokens
* Construction de l’arbre syntaxique abstrait (AST)
* Analyse contextuelle
* Vérification des types
* Gestion des environnements (variables, classes, méthodes)
* Détection des erreurs sémantiques
* Génération de code
* Production d’assembleur IMA
* Gestion des registres
* Gestion de la pile
* Optimisation (optionnelle via -o)
* Propagation de constantes
* Élimination de code mort
* Mémoïsation partielle

## UTILISATION

Commande principale :

decac [options] <fichier.deca>

Le fichier source doit obligatoirement avoir l’extension .deca.
La compilation génère un fichier assembleur .ass dans le même répertoire.

OPTIONS PRINCIPALES

-b Affiche la bannière de l’équipe

-p Affiche l’AST et s’arrête après le parsing

-v Arrête après les vérifications contextuelles

-n Désactive les vérifications à l’exécution

-r X Limite les registres disponibles (4 ≤ X ≤ 16)

-d Active les traces de debug (répétable)

-P Compile plusieurs fichiers en parallèle

-o Active les optimisations

Exemple : decac -o -r 8 programme.deca

## GESTION DES ERREURS

Le compilateur détecte :
* Erreurs lexicales et syntaxiques
* Erreurs contextuelles (identifiants non déclarés, incompatibilités de types, redéfinitions…)
* Erreurs à l’exécution (division par zéro, null pointer dereferencing, overflow, absence de return…)
* Les messages d’erreur indiquent le fichier, la ligne et la position.

## LIMITATIONS

Dans certains cas rares, l’utilisation d’appels de méthodes directement dans des conditions peut entraîner une évaluation incorrecte.
Il est recommandé de stocker le résultat de ces appels dans une variable intermédiaire.

## COMPÉTENCES DÉVELOPPÉES

Conception d’architecture logicielle modulaire
Manipulation d’arbres syntaxiques (AST)
Analyse sémantique et gestion d’environnements
Génération de code bas niveau
Implémentation d’optimisations
Travail collaboratif sur projet complexe

## ÉQUIPE

Projet réalisé par :
Maël Bornard
Manuella Fokou Djontu
Mattéo Gautier
Matteo Jeulin
Arthur Mellot
