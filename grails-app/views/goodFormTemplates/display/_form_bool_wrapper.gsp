  <dt>${label.encodeAsHTML()}</dt>
  <g:if test="${fieldAttributes.checked}">
    <dd class="text-success"><span class="fa fa-check"><span class="hidden">Yes</span></span></dd>
  </g:if>
  <g:else>
    <dd class="text-muted"><span class="fa fa-close"><span class="hidden">No</span></span></dd>
  </g:else>

