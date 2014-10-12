<div class="formField ${error ? 'error' : ''}" title="${error}">

  <g:if test="${preamble}"><div class='preamble'>${preamble.encodeAsHTML()}</div></g:if>

  <fieldset>
    <legend>${label.encodeAsHTML()}</legend>

    <div class="datetime">
      <label for="${name}.date">Date</label>
      <input type="text" name="${name}.date" id="${name}.date" value="${fieldAttributes.value?.date}"
             data-image="${assetPath(src: 'icons/date.png')}"
        <gf:addAttributes fieldAttr="${fieldAttributes}" class="form-control date" skip="['value']"/>/>
      <g:if test="${required}"><span class='required'>${required ? '*' : ''}</span></g:if>

      <label for="${name}.time">Time</label>
      <input type="text" name="${name}.time" id="${name}.time" value="${fieldAttributes.value?.time}"
        <gf:addAttributes fieldAttr="${fieldAttributes}" class="form-control time" skip="['value']"/>/>

    </div>

    <div class="datetime">
      <g:if test="${hint}"><p class='hint help-block'>${hint}</p></g:if>
    </div>

  </fieldset>
</div>
