<g:if test="${fieldAttributes.value != ''}">
  ${fieldAttributes.value.encodeAsHTML()}
</g:if>
<g:else>
  <span class='text-muted'>(blank)</span>
</g:else>