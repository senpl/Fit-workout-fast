package com.easyfitness.utils

import java.io.BufferedWriter
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.Date

class CustomExceptionHandler(private val localPath: String?) : Thread.UncaughtExceptionHandler {
    private val defaultUEH: Thread.UncaughtExceptionHandler?

    //private String url;
    /*
     * if any of the parameters is null, the respective functionality
     * will not be used
     */
    init { //, String url) {
        //this.url = url;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler()
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        val dateFormat = SimpleDateFormat("dd_MM_yyyy_H_m_s")
        val date = Date()
        val timestamp = dateFormat.format(date)
        val result: Writer = StringWriter()
        val printWriter = PrintWriter(result)
        e.printStackTrace(printWriter)
        val stacktrace = result.toString()
        printWriter.close()
        val filename = timestamp + ".txt"

        if (localPath != null) {
            writeToFile(stacktrace, filename)
        }

        /*if (url != null) {
            sendToServer(stacktrace, filename);
        }*/
        defaultUEH!!.uncaughtException(t, e)
    }

    private fun writeToFile(stacktrace: String?, filename: String?) {
        try {
            val bos = BufferedWriter(
                FileWriter(
                    localPath + "/" + filename
                )
            )
            bos.write(stacktrace)
            bos.flush()
            bos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } /*
    private void sendToServer(String stacktrace, String filename) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("filename", filename));
        nvps.add(new BasicNameValuePair("stacktrace", stacktrace));
        try {
            httpPost.setEntity(
                new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            httpClient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
}
