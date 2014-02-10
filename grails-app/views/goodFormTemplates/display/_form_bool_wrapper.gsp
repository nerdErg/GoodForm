  <dt>${label.encodeAsHTML()}</dt>
  <g:if test="${fieldAttributes.checked}">
    <dd class="text-success"><span class="glyphicon glyphicon-ok"><span class="hidden">Yes</span></span></dd>
  </g:if>
  <g:else>
    <dd class="text-muted"><span class="glyphicon glyphicon-remove"><span class="hidden">No</span></span></dd>
  </g:else>

