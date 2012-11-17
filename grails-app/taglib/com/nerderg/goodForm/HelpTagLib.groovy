package com.nerderg.goodForm

/**
 *
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
