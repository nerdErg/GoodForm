<dt>${label.encodeAsHTML()}</dt>
<dd>
Date: ${fieldAttributes.value?.date != '' ? fieldAttributes.value.date.encodeAsHTML() :  "<span class='text-muted'>(blank)</span>"}
Time: ${fieldAttributes.value?.time != '' ? fieldAttributes.value.time.encodeAsHTML() :  "<span class='text-muted'>(blank)</span>"}
</dd>