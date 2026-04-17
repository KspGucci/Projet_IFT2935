-- 1. Produits publiés avec le nom complet de l’annonceur et la catégorie
SELECT p.id_produit, p.titre, p.prix_souhaite, c.nom AS categorie,
       u.prenom, u.nom
FROM produit p
JOIN categorie c ON p.id_categorie = c.id_categorie
JOIN annonceur a ON p.id_annonceur = a.id_annonceur
JOIN utilisateur u ON a.id_annonceur = u.id_utilisateur
WHERE p.statut = 'publie'
ORDER BY p.id_produit;

-- 2. Nombre de produits par catégorie
SELECT c.nom AS categorie, COUNT(*) AS nb_produits
FROM produit p
JOIN categorie c ON p.id_categorie = c.id_categorie
GROUP BY c.id_categorie, c.nom
ORDER BY nb_produits DESC, c.nom;

-- 3. Acheteurs ayant fait le plus de propositions
SELECT u.prenom, u.nom, COUNT(*) AS nb_propositions
FROM proposition pr
JOIN acheteur a ON pr.id_acheteur = a.id_acheteur
JOIN utilisateur u ON a.id_acheteur = u.id_utilisateur
GROUP BY u.id_utilisateur, u.prenom, u.nom
ORDER BY nb_propositions DESC, u.nom, u.prenom;

-- 4. Produits vendus avec prix souhaité, prix final et écart
SELECT p.titre, p.prix_souhaite, v.prix_final,
       (v.prix_final - p.prix_souhaite) AS ecart_prix
FROM vente v
JOIN produit p ON v.id_produit = p.id_produit
ORDER BY ecart_prix DESC;

-- 5. Produits avec annonceur, catégorie, estimation et expert (4 relations)
SELECT p.id_produit, p.titre, c.nom AS categorie,
       e.prix_estime, e.acceptee,
       u.prenom AS prenom_expert, u.nom AS nom_expert
FROM produit p
JOIN categorie c ON p.id_categorie = c.id_categorie
JOIN estimation e ON p.id_produit = e.id_produit
JOIN expert ex ON e.id_expert = ex.id_expert
JOIN utilisateur u ON ex.id_expert = u.id_utilisateur
ORDER BY p.id_produit;

-- 6. Propositions avec produit, acheteur et annonceur (5 relations)
SELECT pr.id_proposition, p.titre, pr.prix_propose, pr.statut,
       ua.prenom AS prenom_acheteur, ua.nom AS nom_acheteur,
       uv.prenom AS prenom_annonceur, uv.nom AS nom_annonceur
FROM proposition pr
JOIN produit p ON pr.id_produit = p.id_produit
JOIN acheteur a ON pr.id_acheteur = a.id_acheteur
JOIN utilisateur ua ON a.id_acheteur = ua.id_utilisateur
JOIN annonceur an ON p.id_annonceur = an.id_annonceur
JOIN utilisateur uv ON an.id_annonceur = uv.id_utilisateur
ORDER BY pr.id_proposition;

-- 7. Ventes avec produit, acheteur, annonceur et proposition originale (6 relations)
SELECT v.id_vente, p.titre, v.prix_final, v.date_vente,
       ub.prenom AS prenom_acheteur, ub.nom AS nom_acheteur,
       us.prenom AS prenom_annonceur, us.nom AS nom_annonceur,
       pr.prix_propose
FROM vente v
JOIN produit p ON v.id_produit = p.id_produit
JOIN proposition pr ON v.id_proposition = pr.id_proposition
JOIN acheteur a ON v.id_acheteur = a.id_acheteur
JOIN utilisateur ub ON a.id_acheteur = ub.id_utilisateur
JOIN annonceur an ON p.id_annonceur = an.id_annonceur
JOIN utilisateur us ON an.id_annonceur = us.id_utilisateur
ORDER BY v.id_vente;

-- 8. Évaluations avec vente, produit, évaluateur et évalué (5 relations)
SELECT ev.id_evaluation, p.titre, ev.note, ev.commentaire,
       ue.prenom AS prenom_evaluateur, ue.nom AS nom_evaluateur,
       uv.prenom AS prenom_evalue, uv.nom AS nom_evalue
FROM evaluation ev
JOIN vente v ON ev.id_vente = v.id_vente
JOIN produit p ON v.id_produit = p.id_produit
JOIN utilisateur ue ON ev.id_evaluateur = ue.id_utilisateur
JOIN utilisateur uv ON ev.id_evalue = uv.id_utilisateur
ORDER BY ev.id_evaluation;

-- 9. Produits favoris avec acheteur, catégorie et annonceur (5 relations)
SELECT uf.prenom AS prenom_acheteur, uf.nom AS nom_acheteur,
       p.titre, c.nom AS categorie,
       ua.prenom AS prenom_annonceur, ua.nom AS nom_annonceur
FROM favori f
JOIN produit p ON f.id_produit = p.id_produit
JOIN categorie c ON p.id_categorie = c.id_categorie
JOIN acheteur ac ON f.id_acheteur = ac.id_acheteur
JOIN utilisateur uf ON ac.id_acheteur = uf.id_utilisateur
JOIN annonceur an ON p.id_annonceur = an.id_annonceur
JOIN utilisateur ua ON an.id_annonceur = ua.id_utilisateur
ORDER BY uf.nom, uf.prenom;

-- 10. Nombre de messages par produit avec expéditeur et destinataire (4 relations + agrégation)
SELECT p.titre,
       ue.prenom AS prenom_expediteur, ue.nom AS nom_expediteur,
       ud.prenom AS prenom_destinataire, ud.nom AS nom_destinataire,
       COUNT(*) AS nb_messages
FROM message m
JOIN produit p ON m.id_produit = p.id_produit
JOIN utilisateur ue ON m.id_expediteur = ue.id_utilisateur
JOIN utilisateur ud ON m.id_destinataire = ud.id_utilisateur
GROUP BY p.titre, ue.prenom, ue.nom, ud.prenom, ud.nom
ORDER BY nb_messages DESC, p.titre;