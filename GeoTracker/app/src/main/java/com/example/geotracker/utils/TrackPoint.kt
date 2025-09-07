package com.example.geotracker.utils

import android.content.Context
import android.location.Location
import android.net.Uri
import android.util.Xml
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class TrackPoint(
    val lat: Double,
    val lon: Double,
    val ele: Double? = null,
    val time: Instant? = null
)

fun writeGpxToUri(
    context: Context,
    uri: Uri,
    trackName: String,
    points: List<Location> // or List<TrackPoint>
) {
    context.contentResolver.openOutputStream(uri)?.use { os ->
        val serializer = Xml.newSerializer()
        serializer.setOutput(os, "UTF-8")
        serializer.startDocument("UTF-8", true)

        serializer.setFeature(
            "http://xmlpull.org/v1/doc/features.html#indent-output",
            true
        )

        serializer.startTag(null, "gpx")
        serializer.attribute(null, "version", "1.1")
        serializer.attribute(null, "creator", "GeoTracker")
        serializer.attribute(null, "xmlns", "http://www.topografix.com/GPX/1/1")

        // <metadata>
        serializer.startTag(null, "metadata")
        serializer.startTag(null, "name")
        serializer.text(trackName)
        serializer.endTag(null, "name")
        serializer.endTag(null, "metadata")

        // <trk>
        serializer.startTag(null, "trk")
        serializer.startTag(null, "name")
        serializer.text(trackName)
        serializer.endTag(null, "name")

        serializer.startTag(null, "trkseg")

        val timeFmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC)

        for (loc in points) {
            serializer.startTag(null, "trkpt")
            serializer.attribute(null, "lat", loc.latitude.toString())
            serializer.attribute(null, "lon", loc.longitude.toString())

            if (loc.hasAltitude()) {
                serializer.startTag(null, "ele")
                serializer.text(loc.altitude.toString())
                serializer.endTag(null, "ele")
            }

            // Use Location.time (ms since epoch) if available
            val instant = if (loc.time != 0L) Instant.ofEpochMilli(loc.time) else Instant.now()
            serializer.startTag(null, "time")
            serializer.text(timeFmt.format(instant))
            serializer.endTag(null, "time")

            serializer.endTag(null, "trkpt")
        }

        serializer.endTag(null, "trkseg")
        serializer.endTag(null, "trk")
        serializer.endTag(null, "gpx")
        serializer.endDocument()
        os.flush()
    }
}
