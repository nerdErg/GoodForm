<input
    type="file"
    name="${name}"
    id="${name}"
  ${gf.addAttributes(fieldAttr: fieldAttributes, class: "form-control", skip: ['value'])}/>
&nbsp;${fieldAttributes.fileName.encodeAsHTML()}

