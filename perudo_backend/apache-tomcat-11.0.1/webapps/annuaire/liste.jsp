<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.Collection" %>
<%@ page import="pack.Personne" %>
<%@ page import="pack.Adresse" %>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Liste des Personnes</title>
</head>
<body>
    <h1>Liste des Personnes</h1>

    <%
        Collection<Personne> personnes = (Collection<Personne>) request.getAttribute("listePersonnes");
        if (personnes != null) {
            for (Personne p : personnes) {
                int id = p.getId();
                String nom = p.getNom();
                String prenom = p.getPrenom();
    %>
                <p><strong><%= nom %> - <%= prenom %></strong></p>
    <%
                if (p.getAdresses() != null) {
                    for (Adresse a : p.getAdresses()) {
    %>
                        <p><%= a.getRue() %> - <%= a.getVille() %></p>
    <%
                    }
                }
            }
        } else {
    %>
        <p>Aucune personne trouvée.</p>
    <%
        }
    %>

    <br>
    <a href="index.html">Retour</a>
</body>
</html>
