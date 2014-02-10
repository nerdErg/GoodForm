<g:if test="${formInstance.readOnly}">
  <div class="panel panel-success">
    <div class="panel-heading">
      <h3>This form has been submitted</h3>
    </div>
  <div class="panel-body">

</g:if>
<g:else>
  <g:if test="${!formData.require && formData.next.size() == 1 && formData.next[0] == 'End'}">
    <div class="panel panel-success">
          <div class="panel-heading">
            <h3>The form is complete. You can submit it now.</h3>

            <div id="submit-application-button" class="btn btn-default">
              <a href="${g.createLink(action: 'submit', id: formInstance.id)}" class="submit">Submit Form</a>
        </div>
    <span class="text text-muted">If you wish to change anything, click to edit on the panels below.</span>
    </div>
    <div class="panel-body">

  </g:if>
  <g:else>
    <div class="panel panel-warning">
          <div class="panel-heading">
    <h3>This form is <em>not</em> complete and has not been submitted.</h3>
    </div>
    <div class="panel-body">
    <g:if test="${formData.require?.size() > 0}">
      <h4>Before it can be submitted:</h4>
      <ul class="required">
        <g:each in="${formData.require}" var="req">
          <li title="${req.Q}">
            <gf:linkToQset formInstance="${formInstance}" questionRef="${req.Q}">${req.message}</gf:linkToQset>
          </li>
        </g:each>
      </ul>
    </g:if>
  </g:else>
</g:else>
<div class="text text-muted">
  <g:if test="${formInstance.userId != 'unknown'}">
    <b>Entered by:</b> ${formInstance.userId.encodeAsHTML()}
  </g:if>
  <b>Started:</b> <g:formatDate date="${formInstance.started}" format="hh:mm a dd/MM/yyyy"/>
  <g:if test="${formInstance.lastUpdated}">,
    <b>Updated:</b> <g:formatDate date="${formInstance.lastUpdated}" format="hh:mm a dd/MM/yyyy"/>
  </g:if>
</div>
</div>
</div>