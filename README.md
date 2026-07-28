# Question 1

Voici la liste des modules que j’ai identifié en me basant sur l’énoncé et les users storys :

## Module Product Catalog

Il a pour responsabilité de gérer le cycle de vie des produits (création, modification, listing et consultation).

Il a des interactions avec les modules suivants : **API**, **Sales** et **Recommendation**.

Il existe des flux entrants qui correspondent aux demandes de création, modification ou encore consultation de produit.

En sortie, le produit existe en base de données et ses informations sont consultables et manipulables.

Concernant les dépendances, le module a besoin d’un accès à la base de données (`RepositoryProduct`).

---

## Module API

Il a pour responsabilité d’exposer les endpoints et d’assurer les échanges entre les **DTO** et l’applicatif.

Il a des interactions avec les modules suivants : **Product Catalog**, **Sales** et **Recommendation**.

Il existe des flux entrants qui correspondent à des requêtes HTTP (format JSON).

En sortie, les informations sont transmises au client via des réponses HTTP.

Concernant les dépendances, le module a besoin d’accéder aux DTO et aux services applicatifs.

---

## Module Recommandation

Il a pour responsabilité de générer une suggestion de produits à partir du catalogue et de l’historique.

Il a des interactions avec les modules suivants : **Sales** et **Product Catalog**.

Il existe des flux entrants qui correspondent aux demandes de recommandation.

En sortie, une liste de produits est consultables.

Concernant les dépendances, le module a besoin d’une interface `RecommendationStrategy` et des repositorys de **Sales** et **Product Catalog**.

---

## Module Sales

Il a pour responsabilité d’enregistrer les ventes ainsi que les informations associées.

Il a des interactions avec les produits.

Il existe des flux entrants qui correspondent aux commandes réalisées par les utilisateurs ou le souhait de consultation des informations de ventes.

Les flux entrants correspondent aux commandes de création de vente et aux demandes de consultation.

Les flux sortants correspondent à l’enregistrement de la vente en base de données et la possibilité de la consultation de ses informations.

Concernant les dépendances, ce module s’appuie sur `SaleRepository`, `SaleLineRepository` et `ProductRepository`.

---

## Module Security

Il a pour responsabilité d’assurer l’authentification et l’autorisation sur les endpoints sensibles.

Il interagit avec le module **API** en interceptant les requêtes avant l’exécution des contrôleurs.

Les flux entrants correspondent aux requêtes HTTP contenant les informations d’authentification.

Les flux sortants correspondent soit à un contexte utilisateur autorisé, soit à un refus d’accès.

Concernant les dépendances, ce module s’appuie sur les composants de sécurité Spring et les différentes règles de rôles.

---

## Module Error Handling

Il a pour responsabilité de centraliser le traitement des exceptions et de normaliser les réponses d’erreur.

Il interagit avec tous les modules applicatifs via la capture des exceptions.

Les flux entrants correspondent aux exceptions déclenchées pendant le traitement des requêtes.

Les flux sortants correspondent à des réponses JSON standardisées avec le code HTTP cohérent.

Concernant les dépendances, ce module s’appuie sur les classes d’exceptions métier et les mécanismes de gestion d’erreurs Spring.

---

## Module Data

Il a pour responsabilité d’encapsuler l’accès à la base relationnelle et le mapping ORM.

Il interagit avec les modules suivants : **Product Catalog**, **Sales** et **Recommendation**.

Les flux entrants correspondent aux opérations de lecture et d’écriture demandées par les services.

Les flux sortants correspondent aux entités persistées et aux résultats de requêtes.

Concernant les dépendances, ce module s’appuie sur JPA/Hibernate et la base de données relationnelle.


# Diagramme UML (composants)

![Diagramme UML](images/Diagramme_UML_Composants.png)

# Critères de qualité

## Évolutivité

Les modules sont définis avec un découpage permettant de limiter le nombre d’opérations et de modules à modifier lors d’une évolution du code.

J’ai utilisé des interfaces afin que les modules communiquent entre eux via des abstractions. Cela permet de remplacer facilement certaines implémentations sans impacter l’ensemble du système.

Concernant la sécurité et la gestion des erreurs, j’ai choisi de créer deux modules séparés afin d’externaliser ces traitements et de conserver une meilleure séparation des responsabilités.

---

## Robustesse

Le module **Security** permet de protéger les endpoints API critiques.

Le module **Error Handling** permet de retourner des erreurs dans un format JSON standardisé, facilement accessible et compréhensible par les développeurs.

L’utilisation d’interfaces permet de diminuer le couplage entre les modules et de limiter les potentielles régressions.

Enfin, l’attribution d’un rôle unique par module permet de favoriser la cohérence ainsi qu’une meilleure séparation des responsabilités de chaque composant.

---

## Testabilité

L’utilisation d’interfaces permet de réaliser facilement des mocks au niveau des dépendances, ce qui facilite l’écriture et la maintenance des tests.

Le découpage en modules distincts permet également de mieux isoler les composants et de faciliter la détection des problèmes lors de l’exécution des tests.

# Question 2

# Schéma base de données (Module Sales)

![Schéma base de données](bdd/diagramme_base_données.png)

## 1) Creer une vente
- Methode: POST
- URL: /api/sales

## 2) Lister les ventes
- Methode: GET
- URL: /api/sales

## 3) Detail d'une vente
- Methode: GET
- URL: /api/sales/{id}
