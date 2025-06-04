
# 🎲 Perudo Project - Jeu de Bluff Multijoueur en Ligne

Bienvenue sur le dépôt GitHub de notre projet de cours d'applications web : la **recréation en ligne du jeu de société Perudo**, un jeu de dés stratégique mêlant bluff et déduction.

> 🧠 *"Bluff, stratégie, et fun dans un jeu multijoueur en temps réel !"*

---

## 📌 Objectif du Projet

Développer une application web permettant de jouer à **Perudo** en ligne, de manière fluide, en temps réel, avec gestion de compte, amis, boutique et classement. Ce projet met en œuvre les concepts vus en cours (Spring Boot, JPA...) et des technologies avancées (React, WebSockets...).

---

## 📷 Aperçu du Jeu

![image](https://github.com/user-attachments/assets/7e853c21-dacb-475f-9c8a-6704c6bce8ea)

---

## 📚 Règles du Jeu

* Chaque joueur dispose de **5 dés** cachés.
* À tour de rôle, les joueurs font des **enchères** sur le nombre de dés affichant une certaine valeur.
* Un joueur peut **douter (Dudo/Perudo)** de l’enchère précédente.
* En cas de doute, tous les dés sont révélés :

  * Si l’enchère est correcte, le joueur qui a douté perd un dé.
  * Sinon, l’enchérisseur perd un dé.
* Le dernier joueur à posséder un dé remporte la partie.

---

## ✅ Fonctionnalités Principales

### 🔐 Gestion des Utilisateurs

* Inscription, connexion sécurisée
* Profils utilisateur avec stats, inventaire, cosmétiques

### 👥 Système Social

* Ajout d'amis via un **code ami**
* Liste d'amis et activité

### 🛍️ Boutique

* Achats de cosmétiques pour les dés
* Équipement des objets achetés

### 🎮 Gestion des Parties

* Création et participation à des parties
* Jeu en temps réel via WebSocket

### 🏆 Récompenses & Classement

* Gagnez des **pièces** et **trophées**
* Classement mondial en fonction des performances

---

## ⚙️ Architecture

### 🧩 Frontend

* **React SPA**
* API REST avec Axios
* WebSocket (STOMP) pour le temps réel
* Synchronisation via `localStorage`

### 🔧 Backend

* **Spring Boot**
* API REST + WebSocket (SockJS, STOMP)
* JPA / Hibernate pour la persistance
* PostgreSQL comme base de données

---

## 🗃️ Structure des Entités

### Principales Entités :

* `Player` : Utilisateur
* `Game` : Partie
* `Bid` : Enchère
* `Dice` : Dés
* `Product` : Cosmétiques
* `GameRecord`, `GameMove`, `FriendRequest`

### Relations clés :

* `Player` ↔ `Game` : Plusieurs joueurs par partie
* `Player` ↔ `Dice` : Un joueur possède plusieurs dés
* `Game` ↔ `Bid` : Une enchère active par partie
* `Player` ↔ `Product` : Inventaire + cosmétique équipé

---

## 🛠️ Technologies

| Domaine          | Technologie                     |
| ---------------- | ------------------------------- |
| Frontend         | React, Axios, WebSocket (STOMP) |
| Backend          | Spring Boot, JPA, Hibernate     |
| BDD              | PostgreSQL                      |
| Communication    | WebSocket + REST API            |
| Authentification | JWT (ou Spring Security)        |

---

## 🚧 Défis Techniques

* 🌐 **Boucles de sérialisation JSON** : Résolues avec `@JsonIdentityInfo`, `@JsonIgnore`
* 🔄 **Synchronisation frontend/backend** : via `localStorage` et API périodique
* 🧑‍💻 **Gestion du Player ID après login** : via `PlayerDTO` centralisé
* 📶 **Résilience réseau** : gestion d’erreurs, messages utilisateurs, reconnections

---

## 🧪 Tests

* **Backend** : Tests unitaires avec JUnit
* **Frontend** : Tests manuels et de parcours utilisateur
* **Intégration** : Communication React ↔ Spring vérifiée (API & WebSocket)
* **Débogage** : Outils Dev + logs Spring Boot

---

## 🚀 Déploiement

* En local :

  * Backend : Spring Boot (`localhost:8080`)
  * Frontend : React (`npm start`, proxy configuré)
* Prêt pour un futur déploiement distant (Heroku, AWS, etc.)

---

## 🖼️ Interface Utilisateur

* **Accueil** : Profil, pièces, trophées
* **Connexion/Inscription**
* **Lobby** : Liste des parties, création/rejoindre
* **Partie** : Affichage des dés, enchères, actions (challenge, etc.)
* **Boutique** : Cosmétiques
* **Leaderboard** : Classement global
* **Amis** : Ajouter / voir activité

---

## 🗓️ Développement Étape par Étape

1. ⚙️ Initialisation (React, Spring Boot, DB)
2. 🧱 Modélisation (entités, relations)
3. 🌐 Création des APIs REST
4. 🔁 Intégration WebSocket
5. 💅 Développement de l’UI React
6. 🧪 Tests & débogage

---

## 📎 Lien du projet

🔗 [https://github.com/Ignacio-Arroyo/perudo\_project](https://github.com/Ignacio-Arroyo/perudo_project)

---

## 🙌 Remerciements

Merci à notre équipe pédagogique pour les fondations techniques, et à nos camarades de groupe pour leur implication dans chaque aspect du projet : backend, frontend, design et tests.

---

## 🧾 Licence

Ce projet est libre d’utilisation dans un cadre pédagogique. Toute contribution est la bienvenue via des Pull Requests ou Issues.

---

