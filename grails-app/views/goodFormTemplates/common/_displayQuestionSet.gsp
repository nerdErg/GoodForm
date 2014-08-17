<g:if test="${readOnly}">
  <div class='qsetReadOnly panel panel-success' style='page-break-inside: avoid;'>
</g:if>
<g:else>
<div class='qset panel panel-success' title='${g.message(code: "goodform.click.edit")}' id='${id}'
     data-backurl='${g.createLink(action: 'back')}/${id}'>
  <div class="panel-heading">
    <div class='clickToEdit center'>${i} <i class='fa fa-edit'></i> <i class='fa fa-arrow-left'></i> ${g.message(code: "goodform.click.edit")}</div>

    <div class='qsetDisplay'>${qSet.toString()}</div>
  </div>
</g:else>
  <div class="panel-body">
    <dl class="dl-horizontal">
      <gf:renderQuestionSet qset="${qSet}" questions="${questions}" data="${data}" display="${true}"/>
    </dl>
  </div>
</div>