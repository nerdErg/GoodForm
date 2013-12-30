<label for="${name}" class="${labelClass}">
  ${label.encodeAsHTML()} <g:if test="${hint}"><span class='hint'>${hint}</span></g:if>
</label>
<g:if test="${preamble}"><div class='preamble'>${preamble.encodeAsHTML()}</div></g:if>

<div class='listContainer'>