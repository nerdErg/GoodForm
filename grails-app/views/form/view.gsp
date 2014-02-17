<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta name="layout" content="main"/>
  <link href="//netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome.css" rel="stylesheet">

  <title>Form - ${formInstance.formVersion.formDefinition.name}</title>
</head>

<body>
<g:render template="/goodFormTemplates/common/viewForm" model="[formInstance: formInstance, formData: formData]"/>
</body>
</html>