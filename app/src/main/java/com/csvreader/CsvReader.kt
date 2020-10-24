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
import java.text.NumberFormat
import java.util.*
//import kotlin.Throws

/**
 * A stream based parser for parsing delimited text data from a file or a
 * stream.
 */
class CsvReader {
    private var inputStream: Reader? = null
    private var fileName: String? = null

    // this holds all the values for switches that the user is allowed to set
    private val userSettings: UserSettings = UserSettings()
    private var charset: Charset? = null
    private var useCustomRecordDelimiter = false

    // this will be our working buffer to hold data chunks
    // read in from the data file
    private val dataBuffer: DataBuffer = DataBuffer()
    private val columnBuffer = ColumnBuffer()
    private val rawBuffer = RawRecordBuffer()
    private var isQualified: BooleanArray? = null
    var rawRecord = ""
        private set
    private val headersHolder = HeadersHolder()

    // these are all more or less global loop variables
    // to keep from needing to pass them all into various
    // methods during parsing
    private var startedColumn = false
    private var startedWithQualifier = false
    private var hasMoreData = true
    private var lastLetter = '\u0000'
    private var hasReadNextLine = false

    /**
     * Gets the count of columns found in this record.
     *
     * @return The count of columns found in this record.
     */
    var columnCount = 0
        private set
    private var currentRecord: Long = 0
    private var values = arrayOfNulls<String>(StaticSettings.INITIAL_COLUMN_COUNT)
    private var initialized = false
    private var closed = false
    /**
     * Creates a [CsvReader][com.csvreader.CsvReader] object using a file
     * as the data source.
     *
     * @param fileName
     * The path to the file to use as the data source.
     * @param delimiter
     * The character to use as the column delimiter.
     * @param charset
     * The [Charset][java.nio.charset.Charset] to use while
     * parsing the data.
     */
    /**
     * Creates a [CsvReader][com.csvreader.CsvReader] object using a file
     * as the data source.&nbsp;Uses ISO-8859-1 as the
     * [Charset][java.nio.charset.Charset].
     *
     * @param fileName
     * The path to the file to use as the data source.
     * @param delimiter
     * The character to use as the column delimiter.
     */
    /**
     * Creates a [CsvReader][com.csvreader.CsvReader] object using a file
     * as the data source.&nbsp;Uses a comma as the column delimiter and
     * ISO-8859-1 as the [Charset][java.nio.charset.Charset].
     *
     * @param fileName
     * The path to the file to use as the data source.
     */
    @JvmOverloads
    constructor(fileName: String?, delimiter: Char = Letters.COMMA, charset: Charset? = Charset.forName("ISO-8859-1")) {
        requireNotNull(fileName) { "Parameter fileName can not be null." }
        requireNotNull(charset) { "Parameter charset can not be null." }
        if (!File(fileName).exists()) {
            throw FileNotFoundException("File " + fileName
                + " does not exist.")
        }
        this.fileName = fileName
        userSettings.delimiter = delimiter
        this.charset = charset
        isQualified = BooleanArray(values.size)
    }
    /**
     * Constructs a [CsvReader][com.csvreader.CsvReader] object using a
     * [Reader][java.io.Reader] object as the data source.
     *
     * @param inputStream
     * The stream to use as the data source.
     * @param delimiter
     * The character to use as the column delimiter.
     */
    /**
     * Constructs a [CsvReader][com.csvreader.CsvReader] object using a
     * [Reader][java.io.Reader] object as the data source.&nbsp;Uses a
     * comma as the column delimiter.
     *
     * @param inputStream
     * The stream to use as the data source.
     */
    @JvmOverloads
    constructor(inputStream: Reader?, delimiter: Char = Letters.COMMA) {
        requireNotNull(inputStream) { "Parameter inputStream can not be null." }
        this.inputStream = inputStream
        userSettings.delimiter = delimiter
        initialized = true
        isQualified = BooleanArray(values.size)
    }

    /**
     * Constructs a [CsvReader][com.csvreader.CsvReader] object using an
     * [InputStream][java.io.InputStream] object as the data source.
     *
     * @param inputStream
     * The stream to use as the data source.
     * @param delimiter
     * The character to use as the column delimiter.
     * @param charset
     * The [Charset][java.nio.charset.Charset] to use while
     * parsing the data.
     */
    constructor(inputStream: InputStream?, delimiter: Char, charset: Charset?) : this(InputStreamReader(inputStream, charset), delimiter) {}

    /**
     * Constructs a [CsvReader][com.csvreader.CsvReader] object using an
     * [InputStream][java.io.InputStream] object as the data
     * source.&nbsp;Uses a comma as the column delimiter.
     *
     * @param inputStream
     * The stream to use as the data source.
     * @param charset
     * The [Charset][java.nio.charset.Charset] to use while
     * parsing the data.
     */
    constructor(inputStream: InputStream?, charset: Charset?) : this(InputStreamReader(inputStream, charset)) {}

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
     * Gets the current way to escape an occurance of the text qualifier inside
     * qualified data.
     *
     * @return The current way to escape an occurance of the text qualifier
     * inside qualified data.
     */
    /**
     * Sets the current way to escape an occurance of the text qualifier inside
     * qualified data.
     *
     * @param escapeMode
     * The way to escape an occurance of the text qualifier inside
     * qualified data.
     * @exception IllegalArgumentException
     * When an illegal value is specified for escapeMode.
     */
    @set:Throws(IllegalArgumentException::class)
    var escapeMode: Int
        get() = userSettings.EscapeMode
        set(escapeMode) {
            require(!(escapeMode != ESCAPE_MODE_DOUBLED
                && escapeMode != ESCAPE_MODE_BACKSLASH)) { "Parameter escapeMode must be a valid value." }
            userSettings.EscapeMode = escapeMode
        }

    /**
     * Gets the index of the current record.
     *
     * @return The index of the current record.
     */
    fun getCurrentRecord(): Long {
        return currentRecord - 1
    }// use clone here to prevent the outside code from
    // setting values on the array directly, which would
    // throw off the index lookup based on header name
    // use headersHolder.Length here in case headers is null
    /**
     * Returns the header values as a string array.
     *
     * @return The header values as a String array.
     * @exception IOException
     * Thrown if this object has already been closed.
     */
    @get:Throws(IOException::class)
    var headers: Array<String?>?
        get() {
            checkClosed()
            return if (headersHolder.Headers == null) {
                null
            } else {
                // use clone here to prevent the outside code from
                // setting values on the array directly, which would
                // throw off the index lookup based on header name
                val clone = arrayOfNulls<String>(headersHolder.headerCount)
                System.arraycopy(headersHolder.Headers!!, 0, clone, 0,
                    headersHolder.headerCount)
                clone
            }
        }
        set(headers) {
            headersHolder.Headers = headers
            headersHolder.IndexByName!!.clear()
            if (headers != null) {
                headersHolder.headerCount = headers.size
            } else {
                headersHolder.headerCount = 0
            }

            // use headersHolder.Length here in case headers is null
//            for (i in 0 until headersHolder.headerCount) {//TODO  Commented not worth in kotlin to rewrite
//                headersHolder.IndexByName!![headers!![i]] = i
//            }
        }

    @Throws(IOException::class)
    fun getValues(): Array<String?> {
        checkClosed()

        // need to return a clone, and can't use clone because values.Length
        // might be greater than columnsCount
        val clone = arrayOfNulls<String>(columnCount)
        System.arraycopy(values, 0, clone, 0, columnCount)
        return clone
    }

    /**
     * Returns the current column value for a given column index.
     *
     * @param columnIndex
     * The index of the column.
     * @return The current column value.
     * @exception IOException
     * Thrown if this object has already been closed.
     */
    @Throws(IOException::class)
    operator fun get(columnIndex: Int): String? {
        checkClosed()
        return if (columnIndex > -1 && columnIndex < columnCount) {
            values[columnIndex]
        } else {
            ""
        }
    }

    /**
     * Returns the current column value for a given column header name.
     *
     * @param headerName
     * The header name of the column.
     * @return The current column value.
     * @exception IOException
     * Thrown if this object has already been closed.
     */
    @Throws(IOException::class)
    operator fun get(headerName: String?): String? {
        checkClosed()
        return get(getIndex(headerName))
    }

    /**
     * Reads another record.
     *
     * @return Whether another record was successfully read or not.
     * @exception IOException
     * Thrown if an error occurs while reading data from the
     * source stream.
     */
    @Throws(IOException::class)
    fun readRecord(): Boolean {
        checkClosed()
        columnCount = 0
        rawBuffer.Position = 0
        dataBuffer.LineStart = dataBuffer.Position
        hasReadNextLine = false

        // check to see if we've already found the end of data
        if (hasMoreData) {
            // loop over the data stream until the end of data is found
            // or the end of the record is found
            do {
                if (dataBuffer.Position == dataBuffer.Count) {
                    checkDataLength()
                } else {
                    startedWithQualifier = false

                    // grab the current letter as a char
                    var currentLetter = dataBuffer.Buffer!![dataBuffer.Position]
                    if (userSettings.useTextQualifier
                        && currentLetter == userSettings.textQualifier) {
                        // this will be a text qualified column, so
                        // we need to set startedWithQualifier to make it
                        // enter the seperate branch to handle text
                        // qualified columns
                        lastLetter = currentLetter

                        // read qualified
                        startedColumn = true
                        dataBuffer.ColumnStart = dataBuffer.Position + 1
                        startedWithQualifier = true
                        var lastLetterWasQualifier = false
                        var escapeChar = userSettings.textQualifier
                        if (userSettings.EscapeMode == ESCAPE_MODE_BACKSLASH) {
                            escapeChar = Letters.BACKSLASH
                        }
                        var eatingTrailingJunk = false
                        var lastLetterWasEscape = false
                        var readingComplexEscape = false
                        var escape = ComplexEscape.UNICODE
                        var escapeLength = 0
                        var escapeValue:String = 0.toString()
                        dataBuffer.Position++
                        do {
                            if (dataBuffer.Position == dataBuffer.Count) {
                                checkDataLength()
                            } else {
                                // grab the current letter as a char
                                currentLetter = dataBuffer.Buffer!![dataBuffer.Position]
                                if (eatingTrailingJunk) {
                                    dataBuffer.ColumnStart = dataBuffer.Position + 1
                                    if (currentLetter == userSettings.delimiter) {
                                        endColumn()
                                    } else if (!useCustomRecordDelimiter && (currentLetter == Letters.CR || currentLetter == Letters.LF)
                                        || useCustomRecordDelimiter && currentLetter == userSettings.RecordDelimiter) {
                                        endColumn()
                                        endRecord()
                                    }
                                } else if (readingComplexEscape) {
                                    escapeLength++
                                    when (escape) {
                                        ComplexEscape.UNICODE -> {
                                            escapeValue += 16.toChar()
                                            escapeValue += hexToDec(currentLetter).toInt()
                                            if (escapeLength == 4) {
                                                readingComplexEscape = false
                                            }
                                        }
                                        ComplexEscape.OCTAL -> {
                                            escapeValue = 8.toString()
                                            escapeValue += (currentLetter - '0').toChar()
                                            if (escapeLength == 3) {
                                                readingComplexEscape = false
                                            }
                                        }
                                        ComplexEscape.DECIMAL -> {
                                            escapeValue += 10.toString()
                                            escapeValue += (currentLetter - '0').toChar()
                                            if (escapeLength == 3) {
                                                readingComplexEscape = false
                                            }
                                        }
                                        ComplexEscape.HEX -> {
                                            escapeValue += 16.toString()
                                            escapeValue += hexToDec(currentLetter).toInt()
                                            if (escapeLength == 2) {
                                                readingComplexEscape = false
                                            }
                                        }
                                    }
                                    if (!readingComplexEscape) {
                                        appendLetter(escapeValue)
                                    } else {
                                        dataBuffer.ColumnStart = dataBuffer.Position + 1
                                    }
                                } else if (currentLetter == userSettings.textQualifier) {
                                    if (lastLetterWasEscape) {
                                        lastLetterWasEscape = false
                                        lastLetterWasQualifier = false
                                    } else {
                                        updateCurrentValue()
                                        if (userSettings.EscapeMode == ESCAPE_MODE_DOUBLED) {
                                            lastLetterWasEscape = true
                                        }
                                        lastLetterWasQualifier = true
                                    }
                                } else if (userSettings.EscapeMode == ESCAPE_MODE_BACKSLASH
                                    && lastLetterWasEscape) {
                                    when (currentLetter) {
                                        'n' -> appendLetter(Letters.LF.toString())
                                        'r' -> appendLetter(Letters.CR.toString())
                                        't' -> appendLetter(Letters.TAB.toString())
                                        'b' -> appendLetter(Letters.BACKSPACE.toString())
                                        'f' -> appendLetter(Letters.FORM_FEED)
                                        'e' -> appendLetter(Letters.ESCAPE.toString())
                                        'v' -> appendLetter(Letters.VERTICAL_TAB.toString())
                                        'a' -> appendLetter(Letters.ALERT.toString())
                                        '0', '1', '2', '3', '4', '5', '6', '7' -> {
                                            escape = ComplexEscape.OCTAL
                                            readingComplexEscape = true
                                            escapeLength = 1
                                            escapeValue = (currentLetter - '0').toString()
                                            dataBuffer.ColumnStart = dataBuffer.Position + 1
                                        }
                                        'u', 'x', 'o', 'd', 'U', 'X', 'O', 'D' -> {
                                            when (currentLetter) {
                                                'u', 'U' -> escape = ComplexEscape.UNICODE
                                                'x', 'X' -> escape = ComplexEscape.HEX
                                                'o', 'O' -> escape = ComplexEscape.OCTAL
                                                'd', 'D' -> escape = ComplexEscape.DECIMAL
                                            }
                                            readingComplexEscape = true
                                            escapeLength = 0
                                            escapeValue = 0.toString()
                                            dataBuffer.ColumnStart = dataBuffer.Position + 1
                                        }
                                        else -> {
                                        }
                                    }
                                    lastLetterWasEscape = false

                                    // can only happen for ESCAPE_MODE_BACKSLASH
                                } else if (currentLetter == escapeChar) {
                                    updateCurrentValue()
                                    lastLetterWasEscape = true
                                } else {
                                    if (lastLetterWasQualifier) {
                                        if (currentLetter == userSettings.delimiter) {
                                            endColumn()
                                        } else if (!useCustomRecordDelimiter && (currentLetter == Letters.CR || currentLetter == Letters.LF)
                                            || useCustomRecordDelimiter && currentLetter == userSettings.RecordDelimiter) {
                                            endColumn()
                                            endRecord()
                                        } else {
                                            dataBuffer.ColumnStart = dataBuffer.Position + 1
                                            eatingTrailingJunk = true
                                        }

                                        // make sure to clear the flag for next
                                        // run of the loop
                                        lastLetterWasQualifier = false
                                    }
                                }

                                // keep track of the last letter because we need
                                // it for several key decisions
                                lastLetter = currentLetter
                                if (startedColumn) {
                                    dataBuffer.Position++
                                    if (userSettings.safetySwitch
                                        && dataBuffer.Position
                                        - dataBuffer.ColumnStart
                                        + columnBuffer.Position > 100000) {
                                        close()
                                        throw IOException(
                                            "Maximum column length of 100,000 exceeded in column "
                                                + NumberFormat
                                                .getIntegerInstance()
                                                .format(
                                                    columnCount.toLong())
                                                + " in record "
                                                + NumberFormat
                                                .getIntegerInstance()
                                                .format(
                                                    currentRecord)
                                                + ". Set the SafetySwitch property to false"
                                                + " if you're expecting column lengths greater than 100,000 characters to"
                                                + " avoid this error.")
                                    }
                                }
                            } // end else
                        } while (hasMoreData && startedColumn)
                    } else if (currentLetter == userSettings.delimiter) {
                        // we encountered a column with no data, so
                        // just send the end column
                        lastLetter = currentLetter
                        endColumn()
                    } else if (useCustomRecordDelimiter
                        && currentLetter == userSettings.RecordDelimiter) {
                        // this will skip blank lines
                        if (startedColumn || columnCount > 0 || !userSettings.skipEmptyRecords) {
                            endColumn()
                            endRecord()
                        } else {
                            dataBuffer.LineStart = dataBuffer.Position + 1
                        }
                        lastLetter = currentLetter
                    } else if (!useCustomRecordDelimiter
                        && (currentLetter == Letters.CR || currentLetter == Letters.LF)) {
                        // this will skip blank lines
                        if (startedColumn
                            || columnCount > 0 || !userSettings.skipEmptyRecords && (currentLetter == Letters.CR || lastLetter != Letters.CR)) {
                            endColumn()
                            endRecord()
                        } else {
                            dataBuffer.LineStart = dataBuffer.Position + 1
                        }
                        lastLetter = currentLetter
                    } else if (userSettings.useComments && columnCount == 0 && currentLetter == userSettings.comment) {
                        // encountered a comment character at the beginning of
                        // the line so just ignore the rest of the line
                        lastLetter = currentLetter
                        skipLine()
                    } else if (userSettings.trimWhitespace
                        && (currentLetter == Letters.SPACE || currentLetter == Letters.TAB)) {
                        // do nothing, this will trim leading whitespace
                        // for both text qualified columns and non
                        startedColumn = true
                        dataBuffer.ColumnStart = dataBuffer.Position + 1
                    } else {
                        // since the letter wasn't a special letter, this
                        // will be the first letter of our current column
                        startedColumn = true
                        dataBuffer.ColumnStart = dataBuffer.Position
                        var lastLetterWasBackslash = false
                        var readingComplexEscape = false
                        var escape = ComplexEscape.UNICODE
                        var escapeLength = 0
                        var escapeValue = 0.toString()
                        var firstLoop = true
                        do {
                            if (!firstLoop
                                && dataBuffer.Position == dataBuffer.Count) {
                                checkDataLength()
                            } else {
                                if (!firstLoop) {
                                    // grab the current letter as a char
                                    currentLetter = dataBuffer.Buffer!![dataBuffer.Position]
                                }
                                if (!userSettings.useTextQualifier
                                    && userSettings.EscapeMode == ESCAPE_MODE_BACKSLASH && currentLetter == Letters.BACKSLASH) {
                                    lastLetterWasBackslash = if (lastLetterWasBackslash) {
                                        false
                                    } else {
                                        updateCurrentValue()
                                        true
                                    }
                                } else if (readingComplexEscape) {
                                    escapeLength++
                                    when (escape) {
                                        ComplexEscape.UNICODE -> {
                                            escapeValue += 16.toString()
                                            escapeValue += hexToDec(currentLetter).toInt()
                                            if (escapeLength == 4) {
                                                readingComplexEscape = false
                                            }
                                        }
                                        ComplexEscape.OCTAL -> {
                                            escapeValue += 8.toString()
                                            escapeValue += (currentLetter - '0').toChar()
                                            if (escapeLength == 3) {
                                                readingComplexEscape = false
                                            }
                                        }
                                        ComplexEscape.DECIMAL -> {
                                            escapeValue += 10.toString()
                                            escapeValue += (currentLetter - '0').toChar()
                                            if (escapeLength == 3) {
                                                readingComplexEscape = false
                                            }
                                        }
                                        ComplexEscape.HEX -> {
                                            escapeValue += 16.toChar()
                                            escapeValue += hexToDec(currentLetter).toInt()
                                            if (escapeLength == 2) {
                                                readingComplexEscape = false
                                            }
                                        }
                                    }
                                    if (!readingComplexEscape) {
                                        appendLetter(escapeValue)
                                    } else {
                                        dataBuffer.ColumnStart = dataBuffer.Position + 1
                                    }
                                } else if (userSettings.EscapeMode == ESCAPE_MODE_BACKSLASH
                                    && lastLetterWasBackslash) {
                                    when (currentLetter) {
                                        'n' -> appendLetter(Letters.LF.toString())
                                        'r' -> appendLetter(Letters.CR.toString())
                                        't' -> appendLetter(Letters.TAB.toString())
                                        'b' -> appendLetter(Letters.BACKSPACE.toString())
                                        'f' -> appendLetter(Letters.FORM_FEED)
                                        'e' -> appendLetter(Letters.ESCAPE.toString())
                                        'v' -> appendLetter(Letters.VERTICAL_TAB.toString())
                                        'a' -> appendLetter(Letters.ALERT.toString())
                                        '0', '1', '2', '3', '4', '5', '6', '7' -> {
                                            escape = ComplexEscape.OCTAL
                                            readingComplexEscape = true
                                            escapeLength = 1
                                            escapeValue = (currentLetter - '0').toChar().toString()
                                            dataBuffer.ColumnStart = dataBuffer.Position + 1
                                        }
                                        'u', 'x', 'o', 'd', 'U', 'X', 'O', 'D' -> {
                                            when (currentLetter) {
                                                'u', 'U' -> escape = ComplexEscape.UNICODE
                                                'x', 'X' -> escape = ComplexEscape.HEX
                                                'o', 'O' -> escape = ComplexEscape.OCTAL
                                                'd', 'D' -> escape = ComplexEscape.DECIMAL
                                            }
                                            readingComplexEscape = true
                                            escapeLength = 0
                                            escapeValue = 0.toString()
                                            dataBuffer.ColumnStart = dataBuffer.Position + 1
                                        }
                                        else -> {
                                        }
                                    }
                                    lastLetterWasBackslash = false
                                } else {
                                    if (currentLetter == userSettings.delimiter) {
                                        endColumn()
                                    } else if (!useCustomRecordDelimiter && (currentLetter == Letters.CR || currentLetter == Letters.LF)
                                        || useCustomRecordDelimiter && currentLetter == userSettings.RecordDelimiter) {
                                        endColumn()
                                        endRecord()
                                    }
                                }

                                // keep track of the last letter because we need
                                // it for several key decisions
                                lastLetter = currentLetter
                                firstLoop = false
                                if (startedColumn) {
                                    dataBuffer.Position++
                                    if (userSettings.safetySwitch
                                        && dataBuffer.Position
                                        - dataBuffer.ColumnStart
                                        + columnBuffer.Position > 100000) {
                                        close()
                                        throw IOException(
                                            "Maximum column length of 100,000 exceeded in column "
                                                + NumberFormat
                                                .getIntegerInstance()
                                                .format(
                                                    columnCount.toLong())
                                                + " in record "
                                                + NumberFormat
                                                .getIntegerInstance()
                                                .format(
                                                    currentRecord)
                                                + ". Set the SafetySwitch property to false"
                                                + " if you're expecting column lengths greater than 100,000 characters to"
                                                + " avoid this error.")
                                    }
                                }
                            } // end else
                        } while (hasMoreData && startedColumn)
                    }
                    if (hasMoreData) {
                        dataBuffer.Position++
                    }
                } // end else
            } while (hasMoreData && !hasReadNextLine)

            // check to see if we hit the end of the file
            // without processing the current record
            if (startedColumn || lastLetter == userSettings.delimiter) {
                endColumn()
                endRecord()
            }
        }
        rawRecord = if (userSettings.captureRawRecord) {
            if (hasMoreData) {
                if (rawBuffer.Position == 0) {
                    String(dataBuffer.Buffer!!,
                        dataBuffer.LineStart, dataBuffer.Position
                        - dataBuffer.LineStart - 1)
                } else {
                    (String(rawBuffer.Buffer!!, 0,
                        rawBuffer.Position)
                        + String(dataBuffer.Buffer!!,
                        dataBuffer.LineStart, dataBuffer.Position
                        - dataBuffer.LineStart - 1))
                }
            } else {
                // for hasMoreData to ever be false, all data would have had to
                // have been
                // copied to the raw buffer
                String(rawBuffer.Buffer!!, 0, rawBuffer.Position)
            }
        } else {
            ""
        }
        return hasReadNextLine
    }

    /**
     * @exception IOException
     * Thrown if an error occurs while reading data from the
     * source stream.
     */
    @Throws(IOException::class)
    private fun checkDataLength() {
        if (!initialized) {
            if (fileName != null) {
                inputStream = BufferedReader(InputStreamReader(
                    FileInputStream(fileName), charset),
                    StaticSettings.MAX_FILE_BUFFER_SIZE)
            }
            charset = null
            initialized = true
        }
        updateCurrentValue()
        if (userSettings.captureRawRecord && dataBuffer.Count > 0) {
            if (rawBuffer.Buffer!!.size - rawBuffer.Position < dataBuffer.Count
                - dataBuffer.LineStart) {
                val newLength = (rawBuffer.Buffer!!.size
                    + Math.max(dataBuffer.Count - dataBuffer.LineStart,
                    rawBuffer.Buffer!!.size))
                val holder = CharArray(newLength)
                System.arraycopy(rawBuffer.Buffer!!, 0, holder, 0,
                    rawBuffer.Position)
                rawBuffer.Buffer = holder
            }
            System.arraycopy(dataBuffer.Buffer!!, dataBuffer.LineStart,
                rawBuffer.Buffer, rawBuffer.Position, dataBuffer.Count
                - dataBuffer.LineStart)
            rawBuffer.Position += dataBuffer.Count - dataBuffer.LineStart
        }
        try {
            dataBuffer.Count = inputStream!!.read(dataBuffer.Buffer, 0,
                dataBuffer.Buffer!!.size)
        } catch (ex: IOException) {
            close()
            throw ex
        }

        // if no more data could be found, set flag stating that
        // the end of the data was found
        if (dataBuffer.Count == -1) {
            hasMoreData = false
        }
        dataBuffer.Position = 0
        dataBuffer.LineStart = 0
        dataBuffer.ColumnStart = 0
    }

    /**
     * Read the first record of data as column headers.
     *
     * @return Whether the header record was successfully read or not.
     * @exception IOException
     * Thrown if an error occurs while reading data from the
     * source stream.
     */
    @Throws(IOException::class)
    fun readHeaders(): Boolean {
        val result = readRecord()

        // copy the header data from the column array
        // to the header string array
        headersHolder.headerCount = columnCount
        headersHolder.Headers = arrayOfNulls(columnCount)
        for (i in 0 until headersHolder.headerCount) {
            val columnValue = get(i)
            headersHolder.Headers!![i] = columnValue

            // if there are duplicate header names, we will save the last one
//            headersHolder.IndexByName!![columnValue] = i////TODO  Commented not worth in kotlin to rewrite
        }
        if (result) {
            currentRecord--
        }
        columnCount = 0
        return result
    }

    /**
     * Returns the column header value for a given column index.
     *
     * @param columnIndex
     * The index of the header column being requested.
     * @return The value of the column header at the given column index.
     * @exception IOException
     * Thrown if this object has already been closed.
     */
    @Throws(IOException::class)
    fun getHeader(columnIndex: Int): String? {
        checkClosed()

        // check to see if we have read the header record yet

        // check to see if the column index is within the bounds
        // of our header array
        return if (columnIndex > -1 && columnIndex < headersHolder.headerCount) {
            // return the processed header data for this column
            headersHolder.Headers!![columnIndex]
        } else {
            ""
        }
    }

    @Throws(IOException::class)
    fun isQualified(columnIndex: Int): Boolean {
        checkClosed()
        return if (columnIndex < columnCount && columnIndex > -1) {
            isQualified!![columnIndex]
        } else {
            false
        }
    }

    /**
     * @exception IOException
     * Thrown if a very rare extreme exception occurs during
     * parsing, normally resulting from improper data format.
     */
    @Throws(IOException::class)
    private fun endColumn() {
        var currentValue: String? = ""

        // must be called before setting startedColumn = false
        if (startedColumn) {
            if (columnBuffer.Position == 0) {
                if (dataBuffer.ColumnStart < dataBuffer.Position) {
                    var lastLetter = dataBuffer.Position - 1
                    if (userSettings.trimWhitespace && !startedWithQualifier) {
                        while (lastLetter >= dataBuffer.ColumnStart
                            && (dataBuffer.Buffer!![lastLetter] == Letters.SPACE || dataBuffer.Buffer!![lastLetter] == Letters.TAB)) {
                            lastLetter--
                        }
                    }
                    currentValue = String(dataBuffer.Buffer!!,
                        dataBuffer.ColumnStart, lastLetter
                        - dataBuffer.ColumnStart + 1)
                }
            } else {
                updateCurrentValue()
                var lastLetter = columnBuffer.Position - 1
                if (userSettings.trimWhitespace && !startedWithQualifier) {
                    while (lastLetter >= 0
                        && (columnBuffer.Buffer!![lastLetter] == Letters.SPACE || columnBuffer.Buffer!![lastLetter] == Letters.SPACE)) {
                        lastLetter--
                    }
                }
                currentValue = String(columnBuffer.Buffer!!, 0,
                    lastLetter + 1)
            }
        }
        columnBuffer.Position = 0
        startedColumn = false
        if (columnCount >= 100000 && userSettings.safetySwitch) {
            close()
            throw IOException(
                "Maximum column count of 100,000 exceeded in record "
                    + NumberFormat.getIntegerInstance().format(
                    currentRecord)
                    + ". Set the SafetySwitch property to false"
                    + " if you're expecting more than 100,000 columns per record to"
                    + " avoid this error.")
        }

        // check to see if our current holder array for
        // column chunks is still big enough to handle another
        // column chunk
        if (columnCount == values.size) {
            // holder array needs to grow to be able to hold another column
            val newLength = values.size * 2
            val holder = arrayOfNulls<String>(newLength)
            System.arraycopy(values, 0, holder, 0, values.size)
            values = holder
            val qualifiedHolder = BooleanArray(newLength)
            System.arraycopy(isQualified!!, 0, qualifiedHolder, 0,
                isQualified!!.size)
            isQualified = qualifiedHolder
        }
        values[columnCount] = currentValue
        isQualified!![columnCount] = startedWithQualifier
        columnCount++
    }

    private fun appendLetter(letter: String) {
        if (columnBuffer.Position == columnBuffer.Buffer!!.size) {
            val newLength = columnBuffer.Buffer!!.size * 2
            val holder = CharArray(newLength)
            System.arraycopy(columnBuffer.Buffer!!, 0, holder, 0,
                columnBuffer.Position)
            columnBuffer.Buffer = holder
        }
        columnBuffer.Buffer!![columnBuffer.Position++] = letter[0]
        dataBuffer.ColumnStart = dataBuffer.Position + 1
    }

    private fun updateCurrentValue() {
        if (startedColumn && dataBuffer.ColumnStart < dataBuffer.Position) {
            if (columnBuffer.Buffer!!.size - columnBuffer.Position < dataBuffer.Position
                - dataBuffer.ColumnStart) {
                val newLength = (columnBuffer.Buffer!!.size
                    + Math.max(
                    dataBuffer.Position - dataBuffer.ColumnStart,
                    columnBuffer.Buffer!!.size))
                val holder = CharArray(newLength)
                System.arraycopy(columnBuffer.Buffer!!, 0, holder, 0,
                    columnBuffer.Position)
                columnBuffer.Buffer = holder
            }
            System.arraycopy(dataBuffer.Buffer!!, dataBuffer.ColumnStart,
                columnBuffer.Buffer, columnBuffer.Position,
                dataBuffer.Position - dataBuffer.ColumnStart)
            columnBuffer.Position += (dataBuffer.Position
                - dataBuffer.ColumnStart)
        }
        dataBuffer.ColumnStart = dataBuffer.Position + 1
    }

    /**
     * @exception IOException
     * Thrown if an error occurs while reading data from the
     * source stream.
     */
    @Throws(IOException::class)
    private fun endRecord() {
        // this flag is used as a loop exit condition
        // during parsing
        hasReadNextLine = true
        currentRecord++
    }

    /**
     * Gets the corresponding column index for a given column header name.
     *
     * @param headerName
     * The header name of the column.
     * @return The column index for the given column header name.&nbsp;Returns
     * -1 if not found.
     * @exception IOException
     * Thrown if this object has already been closed.
     */
    @Throws(IOException::class)
    fun getIndex(headerName: String?): Int {
        checkClosed()
        val indexValue = headersHolder.IndexByName!![headerName]
        return if (indexValue != null) {
            (indexValue as Int).toInt()
        } else {
            -1
        }
    }

    /**
     * Skips the next record of data by parsing each column.&nbsp;Does not
     * increment
     * [getCurrentRecord()][com.csvreader.CsvReader.getCurrentRecord].
     *
     * @return Whether another record was successfully skipped or not.
     * @exception IOException
     * Thrown if an error occurs while reading data from the
     * source stream.
     */
    @Throws(IOException::class)
    fun skipRecord(): Boolean {
        checkClosed()
        var recordRead = false
        if (hasMoreData) {
            recordRead = readRecord()
            if (recordRead) {
                currentRecord--
            }
        }
        return recordRead
    }

    /**
     * Skips the next line of data using the standard end of line characters and
     * does not do any column delimited parsing.
     *
     * @return Whether a line was successfully skipped or not.
     * @exception IOException
     * Thrown if an error occurs while reading data from the
     * source stream.
     */
    @Throws(IOException::class)
    fun skipLine(): Boolean {
        checkClosed()

        // clear public column values for current line
        columnCount = 0
        var skippedLine = false
        if (hasMoreData) {
            var foundEol = false
            do {
                if (dataBuffer.Position == dataBuffer.Count) {
                    checkDataLength()
                } else {
                    skippedLine = true

                    // grab the current letter as a char
                    val currentLetter = dataBuffer.Buffer!![dataBuffer.Position]
                    if (currentLetter == Letters.CR
                        || currentLetter == Letters.LF) {
                        foundEol = true
                    }

                    // keep track of the last letter because we need
                    // it for several key decisions
                    lastLetter = currentLetter
                    if (!foundEol) {
                        dataBuffer.Position++
                    }
                } // end else
            } while (hasMoreData && !foundEol)
            columnBuffer.Position = 0
            dataBuffer.LineStart = dataBuffer.Position + 1
        }
        rawBuffer.Position = 0
        rawRecord = ""
        return skippedLine
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
                headersHolder.Headers = null
                headersHolder.IndexByName = null
                dataBuffer.Buffer = null
                columnBuffer.Buffer = null
                rawBuffer.Buffer = null
            }
            try {
                if (initialized) {
                    inputStream!!.close()
                }
            } catch (e: Exception) {
                // just eat the exception
            }
            inputStream = null
            closed = true
        }
    }

    /**
     * @exception IOException
     * Thrown if this object has already been closed.
     */
    @Throws(IOException::class)
    private fun checkClosed() {
        if (closed) {
            throw IOException(
                "This instance of the CsvReader class has already been closed.")
        }
    }

    /**
     *
     */
    protected fun finalize() {
        close(false)
    }

    private object ComplexEscape {
        const val UNICODE = 1
        const val OCTAL = 2
        const val DECIMAL = 3
        const val HEX = 4
    }

    private inner class DataBuffer {
        var Buffer: CharArray?
        var Position: Int

        // / <summary>
        // / How much usable data has been read into the stream,
        // / which will not always be as long as Buffer.Length.
        // / </summary>
        var Count: Int

        // / <summary>
        // / The position of the cursor in the buffer when the
        // / current column was started or the last time data
        // / was moved out to the column buffer.
        // / </summary>
        var ColumnStart: Int
        var LineStart: Int

        init {
            Buffer = CharArray(StaticSettings.MAX_BUFFER_SIZE)
            Position = 0
            Count = 0
            ColumnStart = 0
            LineStart = 0
        }
    }

    private inner class ColumnBuffer {
        var Buffer: CharArray?
        var Position: Int

        init {
            Buffer = CharArray(StaticSettings.INITIAL_COLUMN_BUFFER_SIZE)
            Position = 0
        }
    }

    private inner class RawRecordBuffer {
        var Buffer: CharArray?
        var Position: Int

        init {
            Buffer = CharArray(StaticSettings.INITIAL_COLUMN_BUFFER_SIZE
                * StaticSettings.INITIAL_COLUMN_COUNT)
            Position = 0
        }
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
        const val BACKSPACE = '\b'
        val FORM_FEED = "\\f"
        const val ESCAPE = '\u001B' // ASCII/ANSI escape
        const val VERTICAL_TAB = '\u000B'
        const val ALERT = '\u0007'
    }

    private inner class UserSettings {
        // having these as publicly accessible members will prevent
        // the overhead of the method call that exists on properties
        var CaseSensitive = true
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
        var textQualifier: Char
            get() = userSettings.textQualifier
            set(textQualifier) {
                userSettings.textQualifier = textQualifier
            }
        /**
         * Gets whether leading and trailing whitespace characters are being trimmed
         * from non-textqualified column data. Default is true.
         *
         * @return Whether leading and trailing whitespace characters are being
         * trimmed from non-textqualified column data.
         */
        /**
         * Sets whether leading and trailing whitespace characters should be trimmed
         * from non-textqualified column data or not. Default is true.
         *
         * @param trimWhitespace
         * Whether leading and trailing whitespace characters should be
         * trimmed from non-textqualified column data or not.
         */
        var trimWhitespace: Boolean
            get() = userSettings.trimWhitespace
            set(trimWhitespace) {
                userSettings.trimWhitespace = trimWhitespace
            }
        /**
         * Whether text qualifiers will be used while parsing or not.
         *
         * @return Whether text qualifiers will be used while parsing or not.
         */
        /**
         * Sets whether text qualifiers will be used while parsing or not.
         *
         * @param useTextQualifier
         * Whether to use a text qualifier while parsing or not.
         */
        var useTextQualifier: Boolean
            get() = userSettings.useTextQualifier
            set(useTextQualifier) {
                userSettings.useTextQualifier = useTextQualifier
            }
        /**
         * Gets the character being used as the column delimiter. Default is comma,
         * ','.
         *
         * @return The character being used as the column delimiter.
         */
        /**
         * Sets the character to use as the column delimiter. Default is comma, ','.
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
        /**
         * Gets the character being used as a comment signal.
         *
         * @return The character being used as a comment signal.
         */
        /**
         * Sets the character to use as a comment signal.
         *
         * @param comment
         * The character to use as a comment signal.
         */
        var comment: Char
            get() = userSettings.comment
            set(comment) {
                userSettings.comment = comment
            }
        /**
         * Gets whether comments are being looked for while parsing or not.
         *
         * @return Whether comments are being looked for while parsing or not.
         */
        /**
         * Sets whether comments are being looked for while parsing or not.
         *
         * @param useComments
         * Whether comments are being looked for while parsing or not.
         */
        var useComments: Boolean
            get() = userSettings.useComments
            set(useComments) {
                userSettings.useComments = useComments
            }
        var EscapeMode: Int
        /**
         * Safety caution to prevent the parser from using large amounts of memory
         * in the case where parsing settings like file encodings don't end up
         * matching the actual format of a file. This switch can be turned off if
         * the file format is known and tested. With the switch off, the max column
         * lengths and max column count per record supported by the parser will
         * greatly increase. Default is true.
         *
         * @return The current setting of the safety switch.
         */
        /**
         * Safety caution to prevent the parser from using large amounts of memory
         * in the case where parsing settings like file encodings don't end up
         * matching the actual format of a file. This switch can be turned off if
         * the file format is known and tested. With the switch off, the max column
         * lengths and max column count per record supported by the parser will
         * greatly increase. Default is true.
         *
         * @param safetySwitch
         */
        var safetySwitch: Boolean
            get() = userSettings.safetySwitch
            set(safetySwitch) {
                userSettings.safetySwitch = safetySwitch
            }
        var skipEmptyRecords: Boolean
            get() = userSettings.skipEmptyRecords
            set(skipEmptyRecords) {
                userSettings.skipEmptyRecords = skipEmptyRecords
            }
        var captureRawRecord: Boolean
            get() = userSettings.captureRawRecord
            set(captureRawRecord) {
                userSettings.captureRawRecord = captureRawRecord
            }

        init {
            textQualifier = Letters.QUOTE
            trimWhitespace = true
            useTextQualifier = true
            delimiter = Letters.COMMA
            RecordDelimiter = Letters.NULL
            comment = Letters.POUND
            useComments = false
            EscapeMode = ESCAPE_MODE_DOUBLED
            safetySwitch = true
            skipEmptyRecords = true
            captureRawRecord = true
        }
    }

    private inner class HeadersHolder {
        var Headers: Array<String?>? = null

        /**
         * Gets the count of headers read in by a previous call to
         * [readHeaders()][com.csvreader.CsvReader.readHeaders].
         *
         * @return The count of headers read in by a previous call to
         * [readHeaders()][com.csvreader.CsvReader.readHeaders].
         */
        var headerCount = 0
            get() = headersHolder.headerCount
        var IndexByName: HashMap<*, *>?

        init {
            IndexByName = HashMap<Any?, Any?>()
        }
    }

    private object StaticSettings {
        // these are static instead of final so they can be changed in unit test
        // isn't visible outside this class and is only accessed once during
        // CsvReader construction
        const val MAX_BUFFER_SIZE = 1024
        const val MAX_FILE_BUFFER_SIZE = 4 * 1024
        const val INITIAL_COLUMN_COUNT = 10
        const val INITIAL_COLUMN_BUFFER_SIZE = 50
    }

    companion object {
        /**
         * Double up the text qualifier to represent an occurance of the text
         * qualifier.
         */
        const val ESCAPE_MODE_DOUBLED = 1

        /**
         * Use a backslash character before the text qualifier to represent an
         * occurance of the text qualifier.
         */
        const val ESCAPE_MODE_BACKSLASH = 2

        /**
         * Creates a [CsvReader][com.csvreader.CsvReader] object using a string
         * of data as the source.&nbsp;Uses ISO-8859-1 as the
         * [Charset][java.nio.charset.Charset].
         *
         * @param data
         * The String of data to use as the source.
         * @return A [CsvReader][com.csvreader.CsvReader] object using the
         * String of data as the source.
         */
        fun parse(data: String?): CsvReader {
            requireNotNull(data) { "Parameter data can not be null." }
            return CsvReader(StringReader(data))
        }

        private fun hexToDec(hex: Char): Char {
            val result: Char
            result = if (hex >= 'a') {
                (hex - 'a' + 10).toChar()
            } else if (hex >= 'A') {
                (hex - 'A' + 10).toChar()
            } else {
                (hex - '0').toChar()
            }
            return result
        }
    }
}
