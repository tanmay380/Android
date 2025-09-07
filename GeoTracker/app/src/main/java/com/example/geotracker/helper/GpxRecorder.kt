package com.example.geotracker.helper

import android.content.Context
import android.location.Location
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Xml
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class GpxRecorder(
    private val context: Context,
    private val uri: Uri
) {
    private var serializer = Xml.newSerializer()
    private var pfd: ParcelFileDescriptor? = null
    private var fos: FileOutputStream? = null
    private val timeFmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC)
    private var started = false

    fun start(trackName: String) {
        // Open for write & truncate if exists (or use "wa" to append to an existing file)
        pfd = context.contentResolver.openFileDescriptor(uri, "w")
        fos = FileOutputStream(pfd!!.fileDescriptor)
        serializer.setOutput(fos, "UTF-8")
        serializer.startDocument("UTF-8", true)
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)

        // GPX header
        serializer.startTag(null, "gpx")
        serializer.attribute(null, "version", "1.1")
        serializer.attribute(null, "creator", "GeoTracker")
        serializer.attribute(null, "xmlns", "http://www.topografix.com/GPX/1/1")

        // metadata
        serializer.startTag(null, "metadata")
        serializer.startTag(null, "name"); serializer.text(trackName); serializer.endTag(null, "name")
        serializer.endTag(null, "metadata")

        // trk + trkseg
        serializer.startTag(null, "trk")
        serializer.startTag(null, "name"); serializer.text(trackName); serializer.endTag(null, "name")
        serializer.startTag(null, "trkseg")

        serializer.flush()
        started = true
    }

    fun addLocation(loc: Location) {
        if (!started) return
        serializer.startTag(null, "trkpt")
        serializer.attribute(null, "lat", loc.latitude.toString())
        serializer.attribute(null, "lon", loc.longitude.toString())

        if (loc.hasAltitude()) {
            serializer.startTag(null, "ele")
            serializer.text(loc.altitude.toString())
            serializer.endTag(null, "ele")
        }

        val instant = if (loc.time != 0L) Instant.ofEpochMilli(loc.time) else Instant.now()
        serializer.startTag(null, "time")
        serializer.text(timeFmt.format(instant))
        serializer.endTag(null, "time")

        serializer.endTag(null, "trkpt")
        serializer.flush()
        fos?.fd?.sync()  // ensure data hits disk on long runs
    }

    fun stop() {
        if (!started) return
        // close tags
        serializer.endTag(null, "trkseg")
        serializer.endTag(null, "trk")
        serializer.endTag(null, "gpx")
        serializer.endDocument()
        serializer.flush()
        try { fos?.fd?.sync() } catch (_: Throwable) {}
        fos?.close()
        pfd?.close()
        started = false
    }
}
