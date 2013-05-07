(goodform = {
    baseContextPath: '/',
    active:0,
    x:0,
    y:0,
    postData:function (uri, data, func, opts) {
        $.ajax({
            type:"POST",
            url:uri,
            data:data,
            success:function (resp) {
                func(resp, opts);
            }
        });
    },
    postForm:function (form, func) {
        return this.postData(form.action, $(form).serialize(), func)
    }

});

var workTypes = [];
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

    $('span.hiddenDetails').each(function () {
        var offset = $(this).parent().position();
        var x = offset.left + 20;
        var y = offset.top + 20;
        $(this).css('left', x + 'px').css('top', y + 'px');
    });

    $('span.quantity').hover(function (event) {
        $(this).children('span.hiddenDetails').show();
    }, function (event) {
        $(this).find('span').hide();
    });

    $('.revealNext').click(function () {
        $(this).next('div.hiddenDiv').slideToggle("medium");
    });
    $('.revealNexthi').click(function () {
        $(this).next('div.hiddenDiv').toggle('highlight', 500);
    });

    $('.paycb').click(function () {
        $(this).hide();
        $(this).next('div.hiddenForm').show();
        $(this).next('div.hiddenForm').find('input').removeAttr('disabled');
        $('#claim').removeAttr('disabled');
        goodform.active++;
    });
    $('.paycbx').click(function () {
        $(this).parents('div.hiddenForm').prev('img.paycb').show();
        $(this).parents('div.hiddenForm').hide();
        $(this).prev('input').attr('disabled', 'disabled');
        if (--goodform.active == 0) {
            $('#claim').attr('disabled', 'disabled');
        }
    });

    $('.hiddenFormCheckbox').change(function (event) {
        var parentDiv = $(this).parents('div.inlineCheck')
        if ($(this).attr('checked')) {
            $(parentDiv).next('div').show();
        } else {
            $(parentDiv).next('div').hide();
        }
    });

    $('.invertedHiddenFormCheckbox').change(
        function (event) {
            var parentDiv = $(this).parents('div.inlineCheck')
            if ($(this).attr('checked')) {
                $(parentDiv).next('div').hide();
            } else {
                $(parentDiv).next('div').show();
            }
        }).parents('div.prop').next('div').hide();

    $('.hiddenFormRadio').change(function (event) {
        var parentDiv = $(this).parents('div.prop')
        if ($(this).attr('checked')) {
            $(parentDiv).next('div').show();
        } else {
            $(parentDiv).next('div').hide();
        }
    });

    $('.invertedHiddenRadio').change(
        function (event) {
            var parentDiv = $(this).parents('div.prop')
            if ($(this).attr('checked')) {
                $(parentDiv).next('div').hide();
            } else {
                $(parentDiv).next('div').show();
            }
        }).parents('div.prop').next('div').hide();

    $(".suggest").each(function () {
        var action = $(this).attr("class").split(" ")[1];
        var actionurl = goodform.baseContextPath + '/suggest/' + action;
        $(this).autocomplete({
            minLength:1,
            source:actionurl,
            select:function (event, ui) {
                $(this).val(ui.item.value);
            }
        });
    });

    $("input[type='file']").change(function () {
        var name = $(this).val().replace(/^.*(\\|\/)/, '');
        $(this).next('span').html(name);
    });

    $('.addAnotherForm').click(function (event) {
        var container = $(this).prev('div.listContainer');
        var formDiv = $(container).children('div.questionListOfItem').first();

        var cloneForm = $(formDiv.clone());
        cloneForm.find("input[type='text'],input[type='number']").val('');
        cloneForm.appendTo(container);
        cloneForm.children('.removeForm').click(function (event) {
            var container = $(this).parent('div.questionListOfItem').parent('div.listContainer');
            if (container.children('div.questionListOfItem').length > 1) {
                $(this).parent('div.questionListOfItem').remove();
            } else {
                $(this).parent('div.questionListOfItem').find("input[type='text'],input[type='number']").val('');
            }
        });
    });

    $('.removeForm').click(function (event) {
        var container = $(this).parent('div.questionListOfItem').parent('div.listContainer');
        if (container.children('div.questionListOfItem').length > 1) {
            $(this).parent('div.questionListOfItem').remove();
        } else {
            $(this).parent('div.questionListOfItem').find("input[type='text'],input[type='number']").val('');
        }
    });

    $('div.qset').click(function (event) {
        var url = $(this).data('backurl');
        window.location = url;
    });

    $('div.inlineCheck').filter(':even').css('background', '#E5E4E8');

    $('.appList a.delete').click(function (event) {
        var item = $(this).attr('title');
        var ok = confirm("Are you sure you want to " + item + "?");
        if (!ok) {
            event.preventDefault();
        }
        return ok;
    });

});
