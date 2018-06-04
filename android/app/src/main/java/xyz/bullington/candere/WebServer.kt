package xyz.bullington.candere

import android.content.Context
import android.database.sqlite.SQLiteException
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.burgstaller.okhttp.AuthenticationCacheInterceptor
import com.burgstaller.okhttp.CachingAuthenticatorDecorator
import com.burgstaller.okhttp.digest.CachingAuthenticator
import com.burgstaller.okhttp.digest.Credentials
import com.burgstaller.okhttp.digest.DigestAuthenticator

import com.rvirin.onvif.onvifcamera.OnvifDevice
import com.rvirin.onvif.onvifcamera.currentDevice

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.SimpleWebServer

import okhttp3.OkHttpClient
import okhttp3.Request

import org.json.JSONArray
import org.json.JSONObject

import java.io.File
import java.io.InputStream

import java.util.*
import java.util.concurrent.ConcurrentHashMap

const val TAG = "WebServer"

class HTTPSessionProxy(
        private val session: NanoHTTPD.IHTTPSession,
        private val newUri: String
) : NanoHTTPD.IHTTPSession {

    init {
        Log.i(TAG, "HTTPSessionProxy: $newUri")
    }

    override fun getRemoteIpAddress(): String {
        return session.remoteIpAddress
    }

    override fun getQueryParameterString(): String {
        return session.queryParameterString
    }

    override fun getCookies(): NanoHTTPD.CookieHandler {
        return session.cookies
    }

    override fun getMethod(): NanoHTTPD.Method {
        return session.method
    }

    override fun getUri(): String {
        return newUri
    }

    override fun getParms(): MutableMap<String, String> {
        return session.parms
    }

    override fun getRemoteHostName(): String {
        return session.remoteHostName
    }

    override fun execute() {
        session.execute()
    }

    override fun getHeaders(): MutableMap<String, String> {
        return session.headers
    }

    override fun getParameters(): MutableMap<String, MutableList<String>> {
        return session.parameters
    }

    override fun parseBody(files: MutableMap<String, String>?) {
        session.parseBody(files)
    }

    override fun getInputStream(): InputStream {
        return session.inputStream
    }
}

fun serializeDevice(device: Device): JSONObject {
    val obj = JSONObject()

    obj.put("id", device.getId())
    obj.put("nickname", device.getNickname())
    obj.put("address", device.getAddress())
    obj.put("username", device.getUsername())
    obj.put("password", device.getPassword())
    obj.put("manufacturer", device.getManufacturer())
    obj.put("snapshotUrl", "http://127.0.0.1:$PORT/devices/snapshot/${device.getId()}")
    obj.put("rtspUrl", device.getRtspUrl())

    return obj
}

fun deserializeDevice(obj: JSONObject): Device {
    // generate id if it doesn't exist
    var id: Int = obj.getInt("id")
    if (id == 0) {
        obj.put("timestamp", Calendar.getInstance()?.timeInMillis)
        id = obj.toString().hashCode()
        obj.remove("timestamp")
    }

    val nickname: String = obj.getString("nickname")
    val address: String = obj.getString("address")
    val username: String = obj.getString("username")
    val password: String = obj.getString("password")
    val manufacturer: String = obj.getString("manufacturer")
    val snapshotUrl: String = obj.getString("snapshotUrl")
    val rtspUrl: String = obj.getString("rtspUrl")

    return Device(id, nickname, address, username, password, manufacturer, snapshotUrl, rtspUrl)
}

fun serializeResponse(response: JSONObject?, data: Array<Pair<String, JSONObject>>): JSONObject {
    val res = JSONObject()

    response?.let { response -> res.put("response", response) }

    val dataObj = JSONObject()

    data.forEach { (key, value) ->
        dataObj.put(key, value)
    }

    res.put("data", dataObj)

    return res
}

fun onvifLogin(
        server: WebServer,
        obj: JSONObject,
        address: String,
        username: String,
        password: String): Boolean {
    Log.i(TAG, "$address $username $password")
    val onvif = OnvifDevice(address, username, password)
    currentDevice = onvif

    // get services
    var onvifRes = onvif.getServices()

    if (!onvifRes.success) {
        Log.i(TAG, "failed on get services ${onvifRes.error}")
        return false
    }

    // get device information
    onvifRes = onvif.getDeviceInformation()

    if (!onvifRes.success) {
        Log.i(TAG, "failed on get device information ${onvifRes.error}")
        return false
    }

    obj.put("manufacturer", onvifRes.parsingUIMessage)

    server.uiHandler.post {
        val toast = Toast.makeText(server.context, "Device information retrieved 👍", Toast.LENGTH_SHORT)
        toast?.show()
    }

    // get device profiles
    onvifRes = onvif.getProfiles()

    if (!onvifRes.success) {
        return false
    }

    val profilesCount = onvif.mediaProfiles.count()
    server.uiHandler.post {
        val toast = Toast.makeText(server.context, "$profilesCount profiles retrieved 😎", Toast.LENGTH_SHORT)
        toast?.show()
    }

    // get snapshot uri
    onvifRes = onvif.getSnapshotURI()

    if (!onvifRes.success) {
        return false
    }

    obj.put("snapshotUrl", onvif.snapshotURI!!)

    server.uiHandler.post {
        val toast = Toast.makeText(server.context, "Snapshot URI retrieved", Toast.LENGTH_SHORT)
        toast?.show()
    }

    // get stream uri
    onvifRes = onvif.getStreamURI()

    if (!onvifRes.success) {
        return false
    }

    obj.put("rtspUrl", onvif.rtspURI!!)

    server.uiHandler.post {
        val toast = Toast.makeText(server.context, "Stream URI retrieved", Toast.LENGTH_SHORT)
        toast?.show()
    }

    return true
}

class WebServer(
        // only use from within ui handler
        internal val context: Context,
        internal val uiHandler: Handler,
        private val db: DeviceDatabase,
        port: Int,
        directory: String
) : SimpleWebServer("127.0.0.1", port, File(directory), true) {
    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        Log.i(TAG, "uri: $uri")

        val dbErrorRes = newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                "database error")

        if (uri == "/devices/fetch") {
            try {
                val devices = db.deviceDao().fetch()
                val data = JSONObject()

                data.put("operation", "=")

                val items = JSONArray()

                devices?.forEach { device -> items.put(serializeDevice(device)) }

                data.put("items", items)

                return newFixedLengthResponse(
                        serializeResponse(null, arrayOf(Pair("devices", data))).toString()
                )
            } catch (e: SQLiteException) {
                return dbErrorRes
            }
        }

        if (uri == "/devices/add") {
            val loginFailedRes = newFixedLengthResponse(serializeResponse(
                    JSONObject(mapOf(Pair("success", false))),
                    arrayOf()
            ).toString())

            try {
                val files = HashMap<String, String>()
                session.parseBody(files)

                files.values?.first()?.let { str ->
                    val obj = JSONObject(str)
                    val result = onvifLogin(
                            this,
                            obj,
                            obj.getString("address"),
                            obj.getString("username"),
                            obj.getString("password")
                    )

                    if (!result) return loginFailedRes

                    val device: Device = deserializeDevice(obj)

                    db.deviceDao().add(device)

                    val data = JSONObject()

                    data.put("operation", "+")
                    data.put("item", obj)

                    return newFixedLengthResponse(serializeResponse(
                            JSONObject(mapOf(Pair("success", true))),
                            arrayOf(Pair("devices", data))
                    ).toString())
                }
            } catch (e: SQLiteException) {
                return dbErrorRes
            }
        }

        if (uri == "/devices/refresh") {
            val loginFailedRes = newFixedLengthResponse(serializeResponse(
                    JSONObject(mapOf(Pair("success", false))),
                    arrayOf()
            ).toString())

            try {
                val files = HashMap<String, String>()
                session.parseBody(files)

                files.values?.first()?.let { str ->
                    val obj = JSONObject(str)
                    val result = onvifLogin(
                            this,
                            obj,
                            obj.getString("address"),
                            obj.getString("username"),
                            obj.getString("password")
                    )

                    if (!result) return loginFailedRes

                    val device: Device = deserializeDevice(obj)

                    db.deviceDao().update(device)

                    val devices = db.deviceDao().fetch()

                    val data = JSONObject()
                    val items = JSONArray()

                    devices?.forEach { device -> items.put(serializeDevice(device)) }

                    data.put("operation", "=")
                    data.put("items", items)

                    return newFixedLengthResponse(serializeResponse(
                            JSONObject(mapOf(Pair("success", true))),
                            arrayOf(Pair("devices", data))
                    ).toString())
                }
            } catch (e: SQLiteException) {
                return dbErrorRes
            }
        }

        if (uri == "/devices/remove") {
            try {
                val files = HashMap<String, String>()
                session.parseBody(files)

                files.values?.first().let { str ->
                    val obj = JSONObject(str)
                    val device: Device = deserializeDevice(obj)

                    db.deviceDao().remove(device)

                    val data = JSONObject()

                    data.put("operation", "-")
                    data.put("item", obj)

                    return newFixedLengthResponse(serializeResponse(
                            JSONObject(mapOf(Pair("success", true))),
                            arrayOf(Pair("devices", data))
                    ).toString())
                }
            } catch (e: SQLiteException) {
                return dbErrorRes
            }
        }

        if (uri.startsWith("/devices/snapshot")) {
            try {
                val id = uri.substring("/devices/snapshot".length).trim().trim('/').toInt()

                // not enough time in contest to actually use SQL correctly here
                var device: Device? = null
                db.deviceDao().fetch().forEach { potentialDevice ->
                    if (potentialDevice.getId() == id) {
                        device = potentialDevice
                    }
                }

                device?.let { device ->
                    val authenticator = DigestAuthenticator(Credentials(device.getUsername(), device.getPassword()))
                    val authCache = ConcurrentHashMap<String, CachingAuthenticator>()

                    val okClient: OkHttpClient = OkHttpClient.Builder()
                            .authenticator(CachingAuthenticatorDecorator(authenticator, authCache))
                            .addInterceptor(AuthenticationCacheInterceptor(authCache))
                            .build()

                    val okReq = Request.Builder()
                            .url(device.getSnapshotUrl())
                            .get()
                            .build()

                    val okRes = okClient.newCall(okReq).execute()

                    if (okRes.body() == null) {
                        return dbErrorRes
                    }

                    val res = newFixedLengthResponse(
                            Response.Status.lookup(okRes.code()),
                            okRes.header("Content-Type"),
                            okRes.body()!!.byteStream(),
                            okRes.body()!!.contentLength())

                    okRes.headers().toMultimap().forEach { (key, value) ->
                        value?.forEach { header ->
                            res.addHeader(key, header)
                        }
                    }

                    return res
                }
            } catch (e: Exception) {
                return dbErrorRes
            }
        }

        if (uri.startsWith("/static")) {
            val index = "/static".length
            val suffix = if (index == uri.length) "/" else ""
            return super.serve(HTTPSessionProxy(session, uri.substring(index) + suffix))
        }

        return super.serve(session)
    }
}