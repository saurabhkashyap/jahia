<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@page import="java.io.File"%>
<%@page import="org.apache.commons.io.FileUtils"%>
<%@page import="org.jahia.settings.SettingsBean"%>
<%@page import="org.jahia.services.search.spell.CompositeSpellChecker"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link rel="stylesheet" href="tools.css" type="text/css" />
<title>Search Engine Manager</title>
</head>
<body>
<h1>Search Engine Management</h1>
<c:if test="${not empty param.action}">
	<c:choose>
		<c:when test="${param.action == 'updateSpellCheckerIndex'}">
			<% CompositeSpellChecker.updateSpellCheckerIndex(); %>
			<p style="color: blue">Spell checker index update triggered</p>
		</c:when>
        <c:when test="${param.action == 'reindex'}">
            <% FileUtils.touch(new File(SettingsBean.getInstance().getRepositoryHome(), "reindex")); %>
            <p style="color: blue">Re-indexing of the repository content will be done on next Jahia startup</p>
        </c:when>
	</c:choose>
</c:if>
<p>Available actions:</p>
<ul>
    <li><a href="?action=reindex">Repository re-indexing</a> - Do repository re-indexing on the next Jahia start</li>
	<li><a href="?action=updateSpellCheckerIndex">Spell checker index update</a> - triggers an immediate update (no restart needed) of the spell checker
	dictionary index used by the "Did you mean" search feature</li>
</ul>
<p>
    <img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
    <a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>
</body>
</html>