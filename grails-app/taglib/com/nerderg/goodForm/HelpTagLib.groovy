package com.nerderg.goodForm

/**
 * Custom taglib which can be used to render help for form elements.
 *
 * @author Peter McNeil
 */
class HelpTagLib {
    def help = {attrs, body ->
        def type = attrs.type ? attrs.type : 'help'
        out << """
        <div class="${type}">
        <div class="${type}Popup">
        """
        out << body()
        out << """</div>
        </div>"""
    }

    def html = {attrs, body ->
        out << body().encodeAsHTML()
    }
}
