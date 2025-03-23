<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Choix</title>
</head>
<body>
    <form action="Serv" method="post">
        Choisir une personne:
        <% 
            Collection<Personne> personnes = request.getAttribute("listepersonnes"); 
            for (Personne p : personnes) {
                int id = p.getId();
                String nomprenom = p.getNom()+" "+p.getPrenom();
                <input type="radio" name="idpersonne" value="<%=id%>"><%=nomprenom%><br>
            }
        %>
        <br>
        Choisir une adresse:
        <% 
            Collection<Adresse> adresses = request.getAttribute("listeadresses"); 
            for (Adresse a : adresses) {
                int id = a.getId();
                String rueville = a.getRue()+" "+a.getVille();
                <input type="radio" name="idadresse" value="<%=id%>"><%=rueville%><br>
            }
        %>
        <br>
        <input type="submit" value="OK">
        <input type="hidden" name="op" value="associer">
    </form>
</body>
</html>