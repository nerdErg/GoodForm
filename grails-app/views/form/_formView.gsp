<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page import="org.codehaus.groovy.grails.commons.ConfigurationHolder" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Letter</title>
    <link rel="stylesheet" href="${ConfigurationHolder.config.grails.serverURL}/css/reset.css"/>
    <link rel="stylesheet" href="${ConfigurationHolder.config.grails.serverURL}/css/base.css"/>
    <link rel="stylesheet" href="${ConfigurationHolder.config.grails.serverURL}/css/fonts.css"/>
    <link rel="stylesheet" href="${ConfigurationHolder.config.grails.serverURL}/css/layout.css"/>
    <link rel="stylesheet" href="${ConfigurationHolder.config.grails.serverURL}/resource/css/main.css"/>
    <style type="text/css">

    @font-face {
      src: url('${ConfigurationHolder.config.grails.serverURL}/fonts/calibri.ttf');
      font-weight: normal;
      font-style: normal;
      -fs-pdf-font-embed: embed;
    }

    @font-face {
      src: url('${ConfigurationHolder.config.grails.serverURL}/fonts/calibrib.ttf');
      font-weight: bold;
      font-style: normal;
      -fs-pdf-font-embed: embed;
    }

    @font-face {
      src: url('${ConfigurationHolder.config.grails.serverURL}/fonts/calibrii.ttf');
      font-weight: normal;
      font-style: italic;
      -fs-pdf-font-embed: embed;
    }

    @page {
        size: 210mm 297mm;
        margin-top: 40mm;
        margin-bottom: 40mm;
        margin-left: 0mm;
        margin-right: 25mm;

    @top-right {
      content:  element(header, first);
    }
    @bottom-left { content:  element(footer, first); }
    @bottom-right { content: "Page " counter(page); }
        }
    </style>

</head>

<body>

<div class="footer">
</div>

<div class="header">
</div>
<div style="margin-left: 25mm">
  <g:render template="formBody" model="[formInstance: formInstance, formData: formData]"/>
</div>
</body>
</html>
