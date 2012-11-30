<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta name="layout" content="main"/>
  <title>Form</title>
</head>

<body>
<h1>Form submitted</h1>
<form:displayText formInstance="${formInstance}" store="${formData}" readOnly="true"/>
</body>
</html>