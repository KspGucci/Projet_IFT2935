# Projet_IFT2935

Application de type Kijiji ou les vendeurs doivent faire estimer chacun de leurs objets avant de les mettre a l'enchere.

## Prerequis

- Java JDK 8 ou plus recent
- PostgreSQL
- PowerShell ou bash, pour utiliser les commandes ci-dessous

Le pilote PostgreSQL est deja inclus dans `app/lib/postgresql.jar`.

## Configuration de la base de donnees

L'application se connecte a PostgreSQL avec les parametres suivants, definis dans `app/src/DatabaseConnection.java`:

- URL: `jdbc:postgresql://localhost:5432/projet-ift2935`
- utilisateur: `postgres`
- mot de passe: `root`

Avant de lancer l'application, creer la base de donnees `projet-ift2935`, puis executer les scripts SQL du dossier `SQL`.

## Compilation

Depuis le dossier `app`:

```powershell
mkdir out
javac -encoding UTF-8 -d out src/Main.java src/DatabaseConnection.java src/MainFrame.java src/AcheteurFrame.java src/AnnonceurFrame.java src/ExpertFrame.java
```

## Création du JAR 

```powershell
jar cfm marketplace.jar MANIFEST.MF -C out .
```

## Exécution

```powershell
java -jar marketplace.jar
```

Le fichier `MANIFEST.MF` indique la classe principale `Main`.
