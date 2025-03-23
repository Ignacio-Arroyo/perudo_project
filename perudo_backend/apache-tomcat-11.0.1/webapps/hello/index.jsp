
<%@ page language="java" import="pack.*, java.util.*" contentType="text/html; charset=UTF-8"
 pageEncoding="UTF-8"%>
<html> 
    <head><title>Serv</title></head>
    <body>
    <h1>Enter a person</h1>
    <form action="Serv" method="post">
    Number 1<input type="number" name="nb1"><br/>
    Number 2<input type="number" name="nb2"><br/>
    <input type="submit" name="op" value="Compute">
    </form>
    <%
    
    Integer res = (Integer) request.getAttribute("sum");
    out.println("Result: " + res);
    %>
    
   </body>
</html>
   
<!-- Ajout pour la calculatrice avec formulaire -->
<script>
    document.querySelector('form').addEventListener('submit', function(event) {
        event.preventDefault();
        const nb1 = document.querySelector('input[name="nb1"]').value;
        const nb2 = document.querySelector('input[name="nb2"]').value;
        window.location.href = `Serv?nb1=${nb1}&nb2=${nb2}`;
    });
</script>