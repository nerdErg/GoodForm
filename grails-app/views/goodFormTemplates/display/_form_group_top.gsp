<div class="formField ${error ? 'error' : ''}" title="${error}">

<fieldset name="${name}" title="${label.encodeAsHTML()}"
<g:each in="${fieldAttributes}" var="attribute">
  <g:if test="${attribute.key != 'value'}">
    ${attribute.key}="${attribute.value.encodeAsHTML()}"
  </g:if>
</g:each>
>
<legend class="${labelClass}">${label.encodeAsHTML()}</legend>