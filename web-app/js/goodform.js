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
            var hiddenForm = $('#'+$(this).attr('data-hidden-form'));
            if ($(this).attr('checked')) {
                hiddenForm.show();
            } else {
                hiddenForm.hide();
            }
        });

        parent.find('input:radio').change(function (event) {
            goodform.radioChange(event);
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
            goodform.addDatePickerBehaviour(cloneForm);
            goodform.addDatePickerBehaviour(formDiv);
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

        parent.find('input.time').timeEntry({
            spinnerImage: '',
            ampmPrefix: ' '
        });


        parent.find('div.inlineCheck').filter(':even').css('background', '#E5E4E8');
        this.addDatePickerBehaviour(parent);

    },
    addDatePickerBehaviour: function (parent) {
        parent.find('input.date').each(function(index,el){
            var element = $(el);
            var format = element.attr('format').toLowerCase().replace('yyyy', 'yy'); //java date format uses MM for month
            element.datepicker({
                dateFormat: format,
                showOn: 'button',
                buttonImage: goodform.dateImg,
                buttonImageOnly: false,
                changeMonth: true,
                changeYear: true
            });
            element.prev('label').attr('for', element.attr('id'));
        });
    },
    radioChange: function (event) {
        $('input:radio').each(function (index, el) {
            var element = $(el);
            if ($(element).hasClass('hiddenFormRadio')) {
                var hiddenForm = $('#'+$(this).attr('data-hidden-form'));
                if ($(this).attr('checked')) {
                    hiddenForm.show();
                } else {
                    hiddenForm.hide();
                }
            }
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
    goodform.addBehaviour(container);

});
