<g:if test="${formInstance.readOnly}">
  <h2>This form has been submitted</h2>
</g:if>
<g:else>
  <g:if test="${!formData.require && formData.next.size() == 1 && formData.next[0] == 'End'}">
    <h2>All required documents attached.</h2>

    <p>You can submit this form.</p>

    <div id="submit-application-button" class="button">
      <a href="${g.createLink(action: 'submit', id: formInstance.id)}" class="submit">Submit Form</a>
    </div>
  </g:if>
  <g:else>
    <h2>This form is <u>not</u> complete and has <u>not</u> been submitted.</h2>
    <g:if test="${formData.require?.size() > 0}">
      <h2>Before it can be submitted you must supply:</h2>
      <ul class="required">
        <g:each in="${formData.require}" var="req">
          <li title="${req.Q}">
            <form:linkToQset application="${formInstance}" questionRef="${req.Q}">${req.message}</form:linkToQset>
          </li>
        </g:each>
      </ul>
    </g:if>
  </g:else>
</g:else>
<b>Assisted by:</b> ${formInstance.userId.encodeAsHTML()}
<b>Started:</b> <g:formatDate date="${formInstance.started}" format="hh:mm a dd/MM/yyyy"/>
<g:if test="${formInstance.lastUpdated}">,
  <b>Updated:</b> <g:formatDate date="${formInstance.lastUpdated}" format="hh:mm a dd/MM/yyyy"/>
</g:if>
