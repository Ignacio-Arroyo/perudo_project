<%@ page import="java.util.Collection" %>
<%@ page import="pack.Personne" %>
<%@ page import="pack.Adresse" %>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Choix</title>
</head>
<body>
    <form action="Serv" method="post">
        Choisir une personne:
        <br>
        <% 
            Collection<Personne> personnes = (Collection<Personne>) request.getAttribute("listePersonnes"); 
            if (personnes != null) {
                for (Personne p : personnes) {
                    int id = p.getId();
                    String nomprenom = p.getNom() + " " + p.getPrenom();
        %>
                    <input type="radio" name="idPersonne" value="<%= id %>"> <%= nomprenom %> <br>
        <% 
                }
            }
        %>
        <br>
        Choisir une adresse:
        <br>
        <% 
            Collection<Adresse> adresses = (Collection<Adresse>) request.getAttribute("listeAdresses"); 
            if (adresses != null) {
                for (Adresse a : adresses) {
                    int id = a.getId();
                    String rueville = a.getRue() + " " + a.getVille();
        %>
                    <input type="radio" name="idAdresse" value="<%= id %>"> <%= rueville %> <br>
        <% 
                }
            }
        %>
        <br>
        <input type="submit" value="OK">
        <input type="hidden" name="op" value="choix">
    </form>
</body>
</html>
