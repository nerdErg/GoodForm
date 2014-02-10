<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta name="layout" content="main"/>
  <r:script>
  goodform.baseContextPath = "${request.getContextPath()}";
  </r:script>
</head>

<body>

<g:render template="/goodFormTemplates/common/form" model="[form: form, questions: questions, formData: formData, formInstance: formInstance]"/>

</body>
</html>