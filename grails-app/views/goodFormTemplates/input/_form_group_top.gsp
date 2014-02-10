<div class="col-12-xs col-8-md col-6-lg formField form-group ${error ? 'has-error' : ''}" title="${error}">

<g:if test="${preamble}"><div class='preamble text-info'>${preamble.encodeAsHTML()}</div></g:if>

<g:if test="${required}"><span class='required'>${required ? '*' : ''}</span></g:if>
<fieldset name="${name}" title="${label.encodeAsHTML()}"
<g:each in="${fieldAttributes}" var="attribute">
  <g:if test="${attribute.key != 'value'}">
    ${attribute.key}="${attribute.value.encodeAsHTML()}"
  </g:if>
</g:each>
>
<legend class="${labelClass}">
  ${label.encodeAsHTML()}

  <g:if test="${hint}"><span class='hint help-block'>${hint}</span></g:if>
</legend>