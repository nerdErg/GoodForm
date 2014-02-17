<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta name="layout" content="main"/>
  <r:script>
  goodform.baseContextPath = "${request.getContextPath()}";
  </r:script>
  <link href="//netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome.css" rel="stylesheet">

</head>

<body>

<g:render template="/goodFormTemplates/common/form" model="[form: form, questions: questions, formData: formData, formInstance: formInstance]"/>

</body>
</html>