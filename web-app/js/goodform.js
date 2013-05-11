(goodform = {
    baseContextPath: '/',
    active: 0,
    x: 0,
    y: 0,
    postData: function (uri, data, func, opts) {
        $.ajax({
            type: "POST",
            url: uri,
            data: data,
            success: function (resp) {
                func(resp, opts);
            }
        });
    },
    postForm: function (form, func) {
        return this.postData(form.action, $(form).serialize(), func)
    },
    addBehaviour: function (parent) {
        parent.find('span.hiddenDetails').each(function () {
            var offset = $(this).parent().position();
            var x = offset.left + 20;
            var y = offset.top + 20;
            $(this).css('left', x + 'px').css('top', y + 'px');
        });

        parent.find('span.quantity').hover(function (event) {
            $(this).children('span.hiddenDetails').show();
        }, function (event) {
            $(this).find('span').hide();
        });

        parent.find('.revealNext').click(function () {
            $(this).next('div.hiddenDiv').slideToggle("medium");
        });

        parent.find('.revealNexthi').click(function () {
            $(this).next('div.hiddenDiv').toggle('highlight', 500);
        });

        parent.find('.hiddenFormCheckbox').change(function (event) {
            var parentDiv = $(this).parents('div.inlineCheck')
            if ($(this).attr('checked')) {
                $(parentDiv).next('div').show();
            } else {
                $(parentDiv).next('div').hide();
            }
        });

        parent.find('.hiddenFormRadio').change(function (event) {
            var parentDiv = $(this).parents('div.prop')
            if ($(this).attr('checked')) {
                $(parentDiv).next('div').show();
            } else {
                $(parentDiv).next('div').hide();
            }
        });

        parent.find(".suggest").each(function () {
            var action = $(this).attr("class").split(" ")[1];
            var actionurl = goodform.baseContextPath + '/suggest/' + action;
            $(this).autocomplete({
                minLength: 1,
                source: actionurl,
                select: function (event, ui) {
                    $(this).val(ui.item.value);
                }
            });
        });

        parent.find("input[type='file']").change(function () {
            var name = $(this).val().replace(/^.*(\\|\/)/, '');
            $(this).next('span').html(name);
        });

        parent.find('.addAnotherForm').click(function (event) {
            var container = $(this).prev('div.listContainer');
            var formDiv = $(container).children('div.questionListOfItem').first();
            //need to remove the datepicker before we copy then add it back
            formDiv.find('input.date').datepicker("destroy");
            var cloneForm = $(formDiv.clone());
            cloneForm.find("input[type='text'],input[type='number']").val('');
            cloneForm.appendTo(container);
            goodform.addBehaviour(cloneForm);
            goodform.addNerdergFormTagsBehaviour(cloneForm);
            goodform.addNerdergFormTagsBehaviour(formDiv);
            cloneForm.children('.removeForm').click(function (event) {
                var container = $(this).parent('div.questionListOfItem').parent('div.listContainer');
                if (container.children('div.questionListOfItem').length > 1) {
                    $(this).parent('div.questionListOfItem').remove();
                } else {
                    $(this).parent('div.questionListOfItem').find("input[type='text'],input[type='number']").val('');
                }
            });
        });

        parent.find('.removeForm').click(function (event) {
            var container = $(this).parent('div.questionListOfItem').parent('div.listContainer');
            if (container.children('div.questionListOfItem').length > 1) {
                $(this).parent('div.questionListOfItem').remove();
            } else {
                $(this).parent('div.questionListOfItem').find("input[type='text'],input[type='number']").val('');
            }
        });

        parent.find('div.qset').click(function (event) {
            var url = $(this).data('backurl');
            window.location = url;
        });

        parent.find('div.inlineCheck').filter(':even').css('background', '#E5E4E8');

    },
    addNerdergFormTagsBehaviour: function(parent) {
        var dateInp = parent.find('input.date');
        dateInp.removeAttr('id'); //datepicker adds it's own id
        dateInp.datepicker({
            dateFormat: nerdergFormTags.dateFormat,
            showOn: 'button',
            buttonImage: nerdergFormTags.dateImg,
            buttonImageOnly: true,
            changeMonth: true,
            changeYear: true
        });
    },
    fixNerdergFormTagsDatepickers: function() {
        $('input.date').datepicker("destroy").removeAttr('id').datepicker({
            dateFormat: nerdergFormTags.dateFormat,
            showOn: 'button',
            buttonImage: nerdergFormTags.dateImg,
            buttonImageOnly: true,
            changeMonth: true,
            changeYear: true
        });
    }

});

$(function () {
    $('body').mousemove(function (e) {
        goodform.x = e.pageX;
        goodform.y = e.pageY;
    });

    $('#spinner').ajaxStart(
        function (e) {
            $(this).css('left', goodform.x);
            $(this).css('top', goodform.y).show();
            $('body').css('cursor', 'wait');
        }).ajaxComplete(function () {
            $(this).hide();
            $('body').css('cursor', 'auto');
        });

    $('.lengthyAction, a[target!="attachment"], button, input[type="submit"]').click(function (e) {
        if (!$(this).hasClass('nospin')) {
            $('#spinner').css('left', goodform.x);
            $('#spinner').css('top', goodform.y).show();
            $(this).css('cursor', 'wait');
            $('body').css('cursor', 'wait');
        }
    });

    var container = $('div.goodFormContainer');
    //datepicker doesn't like id's that are the same...which is generally bad
    goodform.fixNerdergFormTagsDatepickers();
    goodform.addBehaviour(container);

});
