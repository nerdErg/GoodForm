<input
    type="text"
    name="${name}"
    id="${name}"
  <g:if test="${applicationContext.getBean('pluginManager').hasGrailsPlugin('asset-pipeline')}">
    data-image="${assetPath(src: 'icons/date.png')}"
  </g:if>
  <g:elseif test="${applicationContext.getBean('pluginManager').hasGrailsPlugin('resources')}">
    data-image="${resource(dir: 'images/icons/date.png')}"
  </g:elseif>
  <gf:addAttributes fieldAttr="${fieldAttributes}" class="form-control date"/> />
