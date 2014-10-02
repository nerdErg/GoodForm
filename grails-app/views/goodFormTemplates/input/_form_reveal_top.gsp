<div class="col-12-xs col-8-md col-6-lg formField form-group ${error ? 'has-error' : ''}" title="${error}">

<g:if test="${preamble}"><div class='preamble text-info'>${preamble.encodeAsHTML()}</div></g:if>
<g:if test="${prefix}"><span class='prefix'>${prefix}</span></g:if>

<g:if test="${fieldAttributes.remove('pick1')}">
  <fieldset class="reset reveal">
  <legend><label class="${labelClass}">
  <input
      type="radio"
      name="${fieldAttributes.parentName}"
      value="${label.encodeAsHTML()}"
      data-hidden-form="${gf.makeId(identity: [name,label])}-hiddenForm"
    <gf:addAttributes fieldAttr="${fieldAttributes}" class="form-control hiddenFormRadio" skip="['value', 'parentName']"/> />&nbsp;${label.encodeAsHTML()}</label></legend>
</g:if>

<g:else>
  <fieldset class="reset reveal">
  <legend><label for="${name}">
  <input
      type="checkbox"
      name="${name}.yes"
      id="${name}"
      value="${label.encodeAsHTML()}"
      data-hidden-form="${gf.makeId(identity: [name,label])}-hiddenForm"
    <gf:addAttributes fieldAttr="${fieldAttributes}" class="form-control hiddenFormCheckbox" skip="['value']"/> />&nbsp;${label.encodeAsHTML()}</label></legend>
</g:else>

<g:if test="${units}"><span class='units'>${units}</span></g:if>
<g:if test="${required}"><span class='required'>${required ? '*' : ''}</span></g:if>
<g:if test="${hint}"><span class='hint help-block'>${hint}</span></g:if>
<g:if test="${fieldAttributes.checked}">
  <div id="${gf.makeId(identity: [name,label])}-hiddenForm">
</g:if>
<g:else>
  <div id="${gf.makeId(identity: [name,label])}-hiddenForm" class='hiddenForm'>
</g:else>