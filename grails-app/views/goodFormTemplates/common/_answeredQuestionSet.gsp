<div class='qset panel panel-success' title='${g.message(code: "goodform.click.edit")}' id='${id}'
     data-backurl='${g.createLink(action: 'back')}/${id}'>
  <div class="panel-heading">
    <div class='clickToEdit center'><i class='fa fa-rotate-right'></i> ${g.message(code: "goodform.click.edit")}</div>

    <div class='qsetDisplay'>${qSet.toString()}</div>
  </div>

  <div class="panel-body">
    <gf:renderQuestionSet qset="${qSet}" questions="${questions}" data="${data}"/>
  </div>
</div>