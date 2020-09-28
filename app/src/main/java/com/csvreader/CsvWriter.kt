/*
 * Java CSV is a stream based library for reading and writing
 * CSV and other delimited data.
 *
 * Copyright (C) Bruce Dunwiddie bruce@csvreader.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package com.csvreader

import java.io.*
import java.nio.charset.Charset

/**
 * A stream based writer for writing delimited text data to a file or a stream.
 */
class CsvWriter {
    private var outputStream: Writer? = null
    private var fileName: String? = null
    private var firstColumn = true
    private var useCustomRecordDelimiter = false
    private var charset: Charset? = null

    // this holds all the values for switches that the user is allowed to set
    private val userSettings: UserSettings = UserSettings()
    private var initialized = false
    private var closed = false
    private val systemRecordDelimiter = System.getProperty("line.separator")
    /**
     * Creates a [CsvWriter][com.csvreader.CsvWriter] object using a file
     * as the data destination.
     *
     * @param fileName
     * The path to the file to output the data.
     * @param delimiter
     * The character to use as the column delimiter.
     * @param charset
     * The [Charset][java.nio.charset.Charset] to use while
     * writing the data.
     */
    /**
     * Creates a [CsvWriter][com.csvreader.CsvWriter] object using a file
     * as the data destination.&nbsp;Uses a comma as the column delimiter and
     * ISO-8859-1 as the [Charset][java.nio.charset.Charset].
     *
     * @param fileName
     * The path to the file to output the data.
     */
    @JvmOverloads
    constructor(fileName: String?, delimiter: Char = Letters.COMMA, charset: Charset? = Charset.forName("ISO-8859-1")) {
        requireNotNull(fileName) { "Parameter fileName can not be null." }
        requireNotNull(charset) { "Parameter charset can not be null." }
        this.fileName = fileName
        userSettings.delimiter = delimiter
        this.charset = charset
    }

    /**
     * Creates a [CsvWriter][com.csvreader.CsvWriter] object using a Writer
     * to write data to.
     *
     * @param outputStream
     * The stream to write the column delimited data to.
     * @param delimiter
     * The character to use as the column delimiter.
     */
    constructor(outputStream: Writer?, delimiter: Char) {
        requireNotNull(outputStream) { "Parameter outputStream can not be null." }
        this.outputStream = outputStream
        userSettings.delimiter = delimiter
        initialized = true
    }

    /**
     * Creates a [CsvWriter][com.csvreader.CsvWriter] object using an
     * OutputStream to write data to.
     *
     * @param outputStream
     * The stream to write the column delimited data to.
     * @param delimiter
     * The character to use as the column delimiter.
     * @param charset
     * The [Charset][java.nio.charset.Charset] to use while
     * writing the data.
     */
    constructor(outputStream: OutputStream?, delimiter: Char, charset: Charset?) : this(OutputStreamWriter(outputStream, charset), delimiter) {}

    /**
     * Sets the character to use as the record delimiter.
     *
     * @param recordDelimiter
     * The character to use as the record delimiter. Default is
     * combination of standard end of line characters for Windows,
     * Unix, or Mac.
     */
    var recordDelimiter: Char
        get() = userSettings.RecordDelimiter
        set(recordDelimiter) {
            useCustomRecordDelimiter = true
            userSettings.RecordDelimiter = recordDelimiter
        }
    /**
     * Writes another column of data to this record.
     *
     * @param content
     * The data for the new column.
     * @param preserveSpaces
     * Whether to preserve leading and trailing whitespace in this
     * column of data.
     * @exception IOException
     * Thrown if an error occurs while writing data to the
     * destination stream.
     */
    /**
     * Writes another column of data to this record.&nbsp;Does not preserve
     * leading and trailing whitespace in this column of data.
     *
     * @param content
     * The data for the new column.
     * @exception IOException
     * Thrown if an error occurs while writing data to the
     * destination stream.
     */
    @JvmOverloads
    @Throws(IOException::class)
    fun write(content: String, preserveSpaces: Boolean = false) {
        var content = content
        checkClosed()
        checkInit()
        if (content == null) {
            content = ""
        }
        if (!firstColumn) {
            outputStream!!.write(userSettings.delimiter.toInt())
        }
        var textQualify = userSettings.forceQualifier
        if (!preserveSpaces && content.length > 0) {
            content = content.trim { it <= ' ' }
        }
        if (!textQualify
            && userSettings.useTextQualifier
            && (content.indexOf(userSettings.textQualifier) > -1 || content.indexOf(userSettings.delimiter) > -1 || !useCustomRecordDelimiter && (content
                .indexOf(Letters.LF) > -1 || content
                .indexOf(Letters.CR) > -1)
                || useCustomRecordDelimiter && content
                .indexOf(userSettings.RecordDelimiter) > -1
                || firstColumn && content.length > 0 && content[0] == userSettings.comment ||  // check for empty first column, which if on its own line must
                // be qualified or the line will be skipped
                firstColumn && content.length == 0)) {
            textQualify = true
        }
        if (userSettings.useTextQualifier && !textQualify
            && content.length > 0 && preserveSpaces) {
            val firstLetter = content[0]
            if (firstLetter == Letters.SPACE || firstLetter == Letters.TAB) {
                textQualify = true
            }
            if (!textQualify && content.length > 1) {
                val lastLetter = content[content.length - 1]
                if (lastLetter == Letters.SPACE || lastLetter == Letters.TAB) {
                    textQualify = true
                }
            }
        }
        if (textQualify) {
            outputStream!!.write(userSettings.textQualifier.toInt())
            if (userSettings.escapeMode == ESCAPE_MODE_BACKSLASH) {
                content = replace(content, "" + Letters.BACKSLASH, ""
                    + Letters.BACKSLASH + Letters.BACKSLASH)
                content = replace(content, "" + userSettings.textQualifier, ""
                    + Letters.BACKSLASH + userSettings.textQualifier)
            } else {
                content = replace(content, "" + userSettings.textQualifier, ""
                    + userSettings.textQualifier
                    + userSettings.textQualifier)
            }
        } else if (userSettings.escapeMode == ESCAPE_MODE_BACKSLASH) {
            content = replace(content, "" + Letters.BACKSLASH, ""
                + Letters.BACKSLASH + Letters.BACKSLASH)
            content = replace(content, "" + userSettings.delimiter, ""
                + Letters.BACKSLASH + userSettings.delimiter)
            if (useCustomRecordDelimiter) {
                content = replace(content, "" + userSettings.RecordDelimiter,
                    "" + Letters.BACKSLASH + userSettings.RecordDelimiter)
            } else {
                content = replace(content, "" + Letters.CR, ""
                    + Letters.BACKSLASH + Letters.CR)
                content = replace(content, "" + Letters.LF, ""
                    + Letters.BACKSLASH + Letters.LF)
            }
            if (firstColumn && content.length > 0 && content[0] == userSettings.comment) {
                content = if (content.length > 1) {
                    ("" + Letters.BACKSLASH + userSettings.comment
                        + content.substring(1))
                } else {
                    "" + Letters.BACKSLASH + userSettings.comment
                }
            }
        }
        outputStream!!.write(content)
        if (textQualify) {
            outputStream!!.write(userSettings.textQualifier.toInt())
        }
        firstColumn = false
    }

    @Throws(IOException::class)
    fun writeComment(commentText: String?) {
        checkClosed()
        checkInit()
        outputStream!!.write(userSettings.comment.toInt())
        outputStream!!.write(commentText)
        if (useCustomRecordDelimiter) {
            outputStream!!.write(userSettings.RecordDelimiter.toInt())
        } else {
            outputStream!!.write(systemRecordDelimiter)
        }
        firstColumn = true
    }
    /**
     * Writes a new record using the passed in array of values.
     *
     * @param values
     * Values to be written.
     *
     * @param preserveSpaces
     * Whether to preserver leading and trailing spaces in columns
     * while writing out to the record or not.
     *
     * @throws IOException
     * Thrown if an error occurs while writing data to the
     * destination stream.
     */
    /**
     * Writes a new record using the passed in array of values.
     *
     * @param values
     * Values to be written.
     *
     * @throws IOException
     * Thrown if an error occurs while writing data to the
     * destination stream.
     */
    @JvmOverloads
    @Throws(IOException::class)
    fun writeRecord(values: Array<String>?, preserveSpaces: Boolean = false) {
        if (values != null && values.size > 0) {
            for (i in values.indices) {
                write(values[i], preserveSpaces)
            }
            endRecord()
        }
    }

    /**
     * Ends the current record by sending the record delimiter.
     *
     * @exception IOException
     * Thrown if an error occurs while writing data to the
     * destination stream.
     */
    @Throws(IOException::class)
    fun endRecord() {
        checkClosed()
        checkInit()
        if (useCustomRecordDelimiter) {
            outputStream!!.write(userSettings.RecordDelimiter.toInt())
        } else {
            outputStream!!.write(systemRecordDelimiter)
        }
        firstColumn = true
    }

    /**
     *
     */
    @Throws(IOException::class)
    private fun checkInit() {
        if (!initialized) {
            if (fileName != null) {
                outputStream = BufferedWriter(OutputStreamWriter(
                    FileOutputStream(fileName), charset))
            }
            initialized = true
        }
    }

    /**
     * Clears all buffers for the current writer and causes any buffered data to
     * be written to the underlying device.
     * @exception IOException
     * Thrown if an error occurs while writing data to the
     * destination stream.
     */
    @Throws(IOException::class)
    fun flush() {
        outputStream!!.flush()
    }

    /**
     * Closes and releases all related resources.
     */
    fun close() {
        if (!closed) {
            close(true)
            closed = true
        }
    }

    /**
     *
     */
    private fun close(closing: Boolean) {
        if (!closed) {
            if (closing) {
                charset = null
            }
            try {
                if (initialized) {
                    outputStream!!.close()
                }
            } catch (e: Exception) {
                // just eat the exception
            }
            outputStream = null
            closed = true
        }
    }

    /**
     *
     */
    @Throws(IOException::class)
    private fun checkClosed() {
        if (closed) {
            throw IOException(
                "This instance of the CsvWriter class has already been closed.")
        }
    }

    /**
     *
     */
    protected fun finalize() {
        close(false)
    }

    private object Letters {
        const val LF = '\n'
        const val CR = '\r'
        const val QUOTE = '"'
        const val COMMA = ','
        const val SPACE = ' '
        const val TAB = '\t'
        const val POUND = '#'
        const val BACKSLASH = '\\'
        const val NULL = '\u0000'
    }

    private inner class UserSettings {
        /**
         * Gets the character to use as a text qualifier in the data.
         *
         * @return The character to use as a text qualifier in the data.
         */
        /**
         * Sets the character to use as a text qualifier in the data.
         *
         * @param textQualifier
         * The character to use as a text qualifier in the data.
         */
        // having these as publicly accessible members will prevent
        // the overhead of the method call that exists on properties
        var textQualifier: Char
            get() = userSettings.textQualifier
            set(textQualifier) {
                userSettings.textQualifier = textQualifier
            }
        /**
         * Whether text qualifiers will be used while writing data or not.
         *
         * @return Whether text qualifiers will be used while writing data or not.
         */
        /**
         * Sets whether text qualifiers will be used while writing data or not.
         *
         * @param useTextQualifier
         * Whether to use a text qualifier while writing data or not.
         */
        var useTextQualifier: Boolean
            get() = userSettings.useTextQualifier
            set(useTextQualifier) {
                userSettings.useTextQualifier = useTextQualifier
            }
        /**
         * Gets the character being used as the column delimiter.
         *
         * @return The character being used as the column delimiter.
         */
        /**
         * Sets the character to use as the column delimiter.
         *
         * @param delimiter
         * The character to use as the column delimiter.
         */
        var delimiter: Char
            get() = userSettings.delimiter
            set(delimiter) {
                userSettings.delimiter = delimiter
            }
        var RecordDelimiter: Char
        var comment: Char
            get() = userSettings.comment
            set(comment) {
                userSettings.comment = comment
            }
        var escapeMode: Int
            get() = userSettings.escapeMode
            set(escapeMode) {
                userSettings.escapeMode = escapeMode
            }
        /**
         * Whether fields will be surrounded by the text qualifier even if the
         * qualifier is not necessarily needed to escape this field.
         *
         * @return Whether fields will be forced to be qualified or not.
         */
        /**
         * Use this to force all fields to be surrounded by the text qualifier even
         * if the qualifier is not necessarily needed to escape this field. Default
         * is false.
         *
         * @param forceQualifier
         * Whether to force the fields to be qualified or not.
         */
        var forceQualifier: Boolean
            get() = userSettings.forceQualifier
            set(forceQualifier) {
                userSettings.forceQualifier = forceQualifier
            }

        init {
            textQualifier = Letters.QUOTE
            useTextQualifier = true
            delimiter = Letters.COMMA
            RecordDelimiter = Letters.NULL
            comment = Letters.POUND
            escapeMode = ESCAPE_MODE_DOUBLED
            forceQualifier = false
        }
    }

    companion object {
        /**
         * Double up the text qualifier to represent an occurrence of the text
         * qualifier.
         */
        const val ESCAPE_MODE_DOUBLED = 1

        /**
         * Use a backslash character before the text qualifier to represent an
         * occurrence of the text qualifier.
         */
        const val ESCAPE_MODE_BACKSLASH = 2
        fun replace(original: String, pattern: String, replace: String?): String {
            val len = pattern.length
            var found = original.indexOf(pattern)
            return if (found > -1) {
                val sb = StringBuffer()
                var start = 0
                while (found != -1) {
                    sb.append(original.substring(start, found))
                    sb.append(replace)
                    start = found + len
                    found = original.indexOf(pattern, start)
                }
                sb.append(original.substring(start))
                sb.toString()
            } else {
                original
            }
        }
    }
}
