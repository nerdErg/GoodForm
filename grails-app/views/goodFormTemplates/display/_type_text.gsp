<g:if test="${!fieldAttributes.value}">
  <span class='text-muted'>(blank)</span>
</g:if>
<g:else>
  <g:if test="${fieldAttributes.size > 100}">
    <div style="overflow: auto">
    <gf:preFormatToHTML text="${fieldAttributes.value}"/>
    </div>
  </g:if>
  <g:else>
    ${fieldAttributes.value.encodeAsHTML()}
  </g:else>

</g:else>