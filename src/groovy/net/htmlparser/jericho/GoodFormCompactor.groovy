package net.htmlparser.jericho

/**
 * User: pmcneil
 * Date: 17/02/14
 *
 */
class GoodFormCompactor implements CharStreamSource {
    private final Segment segment
    private String newLine=null

    GoodFormCompactor(Segment segment) {
        this.segment = segment
    }

// Documentation inherited from CharStreamSource
    public void writeTo(final Writer writer) throws IOException {
        appendTo(writer)
        writer.flush()
    }

    // Documentation inherited from CharStreamSource
    public void appendTo(final Appendable appendable) throws IOException {
        new SourceFormatter(segment).setTidyTags(true).setNewLine(newLine).setRemoveLineBreaks(false).appendTo(appendable)
    }

    // Documentation inherited from CharStreamSource
    public long getEstimatedMaximumOutputLength() {
        return segment.length()
    }

    // Documentation inherited from CharStreamSource
    public String toString() {
        return CharStreamSourceUtil.toString(this)
    }

    /**
     * Sets the string to be used to represent a <a target="_blank" href="http://en.wikipedia.org/wiki/Newline">newline</a> in the output.
     * <p>
     * The default is to use the same new line string as is used in the source document, which is determined via the {@link Source#getNewLine()} method.
     * If the source document does not contain any new lines, a "best guess" is made by either taking the new line string of a previously parsed document,
     * or using the value from the static {@link Config#NewLine} property.
     * <p>
     * Specifying a <code>null</code> argument resets the property to its default value, which is to use the same new line string as is used in the source document.
     *
     * @param newLine  the string to be used to represent a <a target="_blank" href="http://en.wikipedia.org/wiki/Newline">newline</a> in the output, may be <code>null</code>.
     * @return this <code>SourceFormatter</code> instance, allowing multiple property setting methods to be chained in a single statement. 
     * @see #getNewLine()
     */
    public SourceCompactor setNewLine(final String newLine) {
        this.newLine=newLine
        return this
    }

    /**
     * Returns the string to be used to represent a <a target="_blank" href="http://en.wikipedia.org/wiki/Newline">newline</a> in the output.
     * <p>
     * See the {@link #setNewLine(String)} method for a full description of this property.
     *
     * @return the string to be used to represent a <a target="_blank" href="http://en.wikipedia.org/wiki/Newline">newline</a> in the output.
     */
    public String getNewLine() {
        if (newLine==null) newLine=segment.source.getBestGuessNewLine()
        return newLine
    }
}
