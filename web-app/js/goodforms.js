(goodform = {
    active:0,
    x:0,
    y:0,
    dataChanged:0,
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
    },
    decorateNotes:function (noteDivId) {
        var noteDel = noteDivId + ' a.delete';
        $(noteDel).click(function (event) {
            event.preventDefault();
            var item = $(this).attr('title');
            var ok = confirm("Are you sure you want to " + item + "?");
            if (ok) {
                var url = $(this).attr('href');
                goodform.postData(url, null, function (resp) {
                    $(noteDivId).html(resp);
                    goodform.decorateNotes(noteDivId);
                });
            }
        });
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


    $('.checkedAmount').each(function (index) {
        var limit = $(this).val();
        $.data(this, "limit", limit);
    });
    $('.checkedAmount').change(
        function (event) {
            var validNumberPat = /^[0-9]*\.?[0-9]?[0-9]?$/
            var value = $(this).val().replace(/[\$,\,]/, '');
            if (value.match(validNumberPat)) {
                var limit = Number($(this).data("limit").replace(/[\$,\,]/, ''));
                value = Number(value);
                if (value > limit) {
                    alert("Value exceeds limit of $" + limit);
                    $(this).val(limit);
                }
            } else {
                alert("The value can only contain numbers and commas. Please re-enter the number.");
            }
        }).keypress(function (event) {
            if (event.which && event.which == 13) {
                return false;
            }
            return true;
        });

    $('tr.file').click(function (event) {
        $('#spinner').css('left', goodform.x);
        $('#spinner').css('top', goodform.y).show();
        $('body').css('cursor', 'wait');
        $(this).css('cursor', 'wait');

        var who = $(event.currentTarget);
        var link = who.find('a').attr('href');
        window.location = link;
    });

    $("#work").autocomplete({
        minLength:1,
        source:function (request, response) {
            var results = new Array();
            var terms = request.term.split(' ');
            var regs = new Array();
            for (var i in terms) {
                var term = terms[i].replace('+', ' ');
                term = $.trim(term);
                var rex = new RegExp(term, 'i');
                regs.push(rex);
            }
            for (var i in workTypes) {
                var wt = workTypes[i]
                var found = true;
                for (var i in regs) {
                    var regex = regs[i];
                    found = found && (wt.court.search(regex) != -1 || wt.csuCode.search(regex) != -1 || wt.description.search(regex) != -1 || wt.engagement.search(regex) != -1)
                }
                if (found) {
                    results.push(wt)
                }
            }
            response(results);
        },

        select:function (event, ui) {
            var ownerCodeExists = false;
            $("input[name='ownerCode']").each(function () {
                if ($(this).val() == ui.item.ownerCode) {
                    ownerCodeExists = true;
                }
            });
            if (ownerCodeExists) {
                alert("You have already added one of these. Please select a different item.");
                return false;
            }
            $("#work").val("adding " + ui.item.description);
            $('body').css('cursor', 'wait');

            $.ajax({
                url:'${g.createLink(controller: "account", action: "addExtension")}',
                dataType:"html",
                data:{
                    id:ui.item.ownerCode,
                    file:$('#fileId').val()
                },
                success:function (resp) {
                    $('body').css('cursor', 'auto');

                    $('#items').append(resp).find("textarea").unbind('focus').focus(function (event) {
                        if ($(this).val() == "type your answer here") {
                            $(this).val("");
                        }
                    });
                    $("input[type='file']").unbind('change').change(function () {
                        var name = $(this).val().replace(/^.*(\\|\/)/, '');
                        $(this).next('span').html(name);
                    });

                    $(".removeExt").unbind('click').click(function (event) {
                        $(this).closest("div.roundboxForm").remove();
                    });
                    $("#work").val('');

                    $("#submitExtensions").attr('disabled', false);

                    $('input.date').datepicker({
                        dateFormat:nerdergFormTags.dateFormat,
                        showOn:'button',
                        buttonImage:nerdergFormTags.dateImg,
                        buttonImageOnly:true,
                        changeMonth:true,
                        changeYear:true
                    });
                }
            });
            return false;
        }
    });

    $("#work").each(function () {
        var ac = $(this).data("autocomplete")
        if (ac) {
            ac._renderItem = function (ul, item) {
                var snip = "<a>";
                if (item.court) {
                    snip += "<span class='hint'>(" + item.court + ")</span> ";
                }
                snip += item.csuCode + " " + item.description + " " + item.engagement + "</a>";
                if (item.note1) {
                    snip += "<div class='help'><div class='helpPopup'><p>" + item.note1.replace("\n", "<br/>") + "</p><p>" + item.note2.replace("\n", "<br/>") + "</p></div></div>"
                }
                return $("<li></li>").data("item.autocomplete", item).append(snip).appendTo(ul);
            }
        }
    });

    $(".suggest").each(function () {
        var url = '${g.createLink(controller: "suggest", action: "index")}'
        var action = $(this).attr("class").split(" ")[1];
        var actionurl = url.replace('index', action);
        $(this).autocomplete({
            minLength:1,
            source:actionurl,
            select:function (event, ui) {
                $(this).val(ui.item.value);
            }
        });
    });

    $(".removeExt").click(function (event) {
        $(this).closest("div.roundboxForm").remove();
    });

    $(".inlineBar.red").sparkline('html', {type:'bar', barColor:'red'});

    $(".inlineBar.green").sparkline('html', {type:'bar', barColor:'green'});

    $("textarea").focus(function (event) {
        if ($(this).val() == "type your answer here") {
            $(this).val("");
        }
    });

    $(".sessionDoc").each(
        function () {
            var url = $(this).attr('href');
            window.open(url);
        }).hide();

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

//    $('div.qset').click(function (event) {
//        var url = "${g.createLink(controller: "grant", action: "goback")}/" + $(this).attr('id');
//        window.location = url;
//    });

    $('div.inlineCheck').filter(':even').css('background', '#E5E4E8');

    $('.appList a.delete').click(function (event) {
        var item = $(this).attr('title');
        var ok = confirm("Are you sure you want to " + item + "?");
        if (!ok) {
            event.preventDefault();
        }
        return ok;
    });

    $('div.editForm').dialog({
        autoOpen:false
    });

    goodform.decorateNotes('#meansNotes');
    goodform.decorateNotes('#meritNotes');

});
