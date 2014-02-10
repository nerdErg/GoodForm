<div class="formField ${error ? 'error' : ''}" title="${error}">

  <g:if test="${preamble}"><div class='preamble'>${preamble.encodeAsHTML()}</div></g:if>

  <fieldset>
    <legend>${label.encodeAsHTML()}</legend>
    <div class="datetime">
    <label for="${name}.date">Date
      <input type="text" name="${name}.date" id="${name}.date" value="${fieldAttributes.value?.date}"
        ${gf.addAttributes(fieldAttr: fieldAttributes, class: "form-control date", skip: ['value'])}/>
    </label>
    <label for="${name}.time">Time
      <input type="text" name="${name}.time" id="${name}.time" value="${fieldAttributes.value?.time}"
        ${gf.addAttributes(fieldAttr: fieldAttributes, class: "form-control time", skip: ['value'])}/>
    </label>
    </div>
    <div class="datetime">
    <g:if test="${units}"><span class='units'>${units}</span></g:if>
    <g:if test="${required}"><span class='required'>${required ? '*' : ''}</span></g:if>
    <g:if test="${hint}"><p class='hint help-block'>${hint}</p></g:if>
      </div>

  </fieldset>
</div>
