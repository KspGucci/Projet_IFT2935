CREATE TABLE utilisateur (
    id_utilisateur INTEGER PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    courriel VARCHAR(100) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(100) NOT NULL,
    telephone VARCHAR(20),
    adresse VARCHAR(150),
    ville VARCHAR(60),
    province VARCHAR(60),
    code_postal VARCHAR(10),
    date_inscription DATE NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE annonceur (
    id_annonceur INTEGER PRIMARY KEY,
    note_moyenne NUMERIC(3,2) DEFAULT 0,
    nb_ventes INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (id_annonceur) REFERENCES utilisateur(id_utilisateur)
);

CREATE TABLE acheteur (
    id_acheteur INTEGER PRIMARY KEY,
    note_moyenne NUMERIC(3,2) DEFAULT 0,
    nb_achats INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (id_acheteur) REFERENCES utilisateur(id_utilisateur)
);

CREATE TABLE expert (
    id_expert INTEGER PRIMARY KEY,
    domaine_expertise VARCHAR(100) NOT NULL,
    nb_estimations INTEGER NOT NULL DEFAULT 0,
    date_certification DATE,
    FOREIGN KEY (id_expert) REFERENCES utilisateur(id_utilisateur)
);

CREATE TABLE categorie (
    id_categorie INTEGER PRIMARY KEY,
    nom VARCHAR(80) NOT NULL UNIQUE,
    description VARCHAR(255),
    categorie_parent INTEGER,
    FOREIGN KEY (categorie_parent) REFERENCES categorie(id_categorie)
);

CREATE TABLE produit (
    id_produit INTEGER PRIMARY KEY,
    titre VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    prix_souhaite NUMERIC(10,2) NOT NULL,
    id_annonceur INTEGER NOT NULL,
    id_categorie INTEGER NOT NULL,
    condition_produit VARCHAR(30) NOT NULL,
    statut VARCHAR(30) NOT NULL,
    date_soumission DATE NOT NULL DEFAULT CURRENT_DATE,
    date_publication DATE,
    FOREIGN KEY (id_annonceur) REFERENCES annonceur(id_annonceur),
    FOREIGN KEY (id_categorie) REFERENCES categorie(id_categorie)
);

CREATE TABLE estimation (
    id_estimation INTEGER PRIMARY KEY,
    id_produit INTEGER NOT NULL,
    id_expert INTEGER NOT NULL,
    prix_estime NUMERIC(10,2) NOT NULL,
    commentaire VARCHAR(255),
    acceptee BOOLEAN,
    date_decision DATE,
    FOREIGN KEY (id_produit) REFERENCES produit(id_produit),
    FOREIGN KEY (id_expert) REFERENCES expert(id_expert)
);

CREATE TABLE proposition (
    id_proposition INTEGER PRIMARY KEY,
    id_produit INTEGER NOT NULL,
    id_acheteur INTEGER NOT NULL,
    prix_propose NUMERIC(10,2) NOT NULL,
    message VARCHAR(255),
    date_proposition TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    statut VARCHAR(30) NOT NULL DEFAULT 'en_attente',
    FOREIGN KEY (id_produit) REFERENCES produit(id_produit),
    FOREIGN KEY (id_acheteur) REFERENCES acheteur(id_acheteur)
);

CREATE TABLE vente (
    id_vente INTEGER PRIMARY KEY,
    id_produit INTEGER NOT NULL,
    id_acheteur INTEGER NOT NULL,
    id_proposition INTEGER NOT NULL UNIQUE,
    prix_final NUMERIC(10,2) NOT NULL,
    date_vente TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    methode_paiement VARCHAR(30) NOT NULL,
    FOREIGN KEY (id_produit) REFERENCES produit(id_produit),
    FOREIGN KEY (id_acheteur) REFERENCES acheteur(id_acheteur),
    FOREIGN KEY (id_proposition) REFERENCES proposition(id_proposition)
);

CREATE TABLE evaluation (
    id_evaluation INTEGER PRIMARY KEY,
    id_vente INTEGER NOT NULL,
    id_evaluateur INTEGER NOT NULL,
    id_evalue INTEGER NOT NULL,
    note NUMERIC(2,1) NOT NULL,
    commentaire VARCHAR(255),
    date_evaluation DATE NOT NULL DEFAULT CURRENT_DATE,
    FOREIGN KEY (id_vente) REFERENCES vente(id_vente),
    FOREIGN KEY (id_evaluateur) REFERENCES utilisateur(id_utilisateur),
    FOREIGN KEY (id_evalue) REFERENCES utilisateur(id_utilisateur)
);

CREATE TABLE photoproduit (
    id_photo INTEGER PRIMARY KEY,
    id_produit INTEGER NOT NULL,
    url_photo VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    principale BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (id_produit) REFERENCES produit(id_produit)
);

CREATE TABLE message (
    id_message INTEGER PRIMARY KEY,
    id_expediteur INTEGER NOT NULL,
    id_destinataire INTEGER NOT NULL,
    id_produit INTEGER NOT NULL,
    contenu VARCHAR(255) NOT NULL,
    date_envoi TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lu BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (id_expediteur) REFERENCES utilisateur(id_utilisateur),
    FOREIGN KEY (id_destinataire) REFERENCES utilisateur(id_utilisateur),
    FOREIGN KEY (id_produit) REFERENCES produit(id_produit)
);

CREATE TABLE favori (
    id_acheteur INTEGER NOT NULL,
    id_produit INTEGER NOT NULL,
    date_ajout TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_acheteur, id_produit),
    FOREIGN KEY (id_acheteur) REFERENCES acheteur(id_acheteur),
    FOREIGN KEY (id_produit) REFERENCES produit(id_produit)
);