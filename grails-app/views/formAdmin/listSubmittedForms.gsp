<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="layout" content="main"/>
</head>

<body>
<div class="goodFormContainer">
    <g:if test="${forms}">
        <table>
            <tr>
                <th>Id</th>
                <th>
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
                        <a href="${g.createLink(action: 'viewSubmittedForm', id: form.id)}"></a>
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