<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta name="layout" content="main"/>
</head>

<body>
<div class="goodFormContainer">

  <form:showMessages/>

  <h1><g:message code="goodform.submit.form"/></h1>

  <p>You tried to submit form ${params.id}</p>

  <p>
    This is a place holder page. You need to implement the submit action in your controller and do something with the
    form data. So override the submit action in your form controller. See Good Form plugin doco for examples.
  </p>
</div>
</body>
</html>