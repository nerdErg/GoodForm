<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="layout" content="main"/>
</head>

<body>
<g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
</g:if>
<div class="goodFormContainer">
    <g:if test="${forms}">
        <table>
            <tr>
                <th>Id</th>
                <th colspan="2">
                    Date Started
                </th>
            </tr>

            <g:each in="${forms}" var="form">
                <tr>
                    <td>
                        ${form.id}
                    </td>
                    <td>
                        ${form.started}
                    </td>
                    <td>
                        <a href="${g.createLink(action: 'viewSubmittedForm', id: form.id)}">View</a>
                    </td>
                </tr>
            </g:each>
        </table>
    </g:if>
    <g:else>
        <div>
            No submitted forms for ${formDefinition.name}
        </div>

        <div>
            <a href="${g.createLink(action: 'listFormDefinitions')}">Back</a>
        </div>
    </g:else>

</div>
</body>
</html>