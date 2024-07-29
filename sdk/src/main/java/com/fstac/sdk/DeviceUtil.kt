package com.fstac.sdk

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections

class DeviceUtil(private val context: Context) {

    private val requestQueue: RequestQueue = Volley.newRequestQueue(context)

    fun getDeviceModel(): String = Build.MODEL

    fun getDeviceBrand(): String = Build.BRAND

    fun getDeviceManufacturer(): String = Build.MANUFACTURER

    fun getDeviceIMEI(): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } else {
            telephonyManager.deviceId
        }
    }

    fun getAndroidVersion(): String = Build.VERSION.RELEASE

//    fun getIPAddress(useIPv4: Boolean): String {
//        try {
//            for (networkInterface in Collections.list(NetworkInterface.getNetworkInterfaces())) {
//                for (inetAddress in Collections.list(networkInterface.inetAddresses)) {
//                    if (!inetAddress.isLoopbackAddress) {
//                        val sAddr = inetAddress.hostAddress
//                        val isIPv4 = sAddr.indexOf(':') < 0
//                        if (useIPv4) {
//                            if (isIPv4) return sAddr
//                        } else {
//                            if (!isIPv4) {
//                                val delim = sAddr.indexOf('%')
//                                return if (delim < 0) sAddr else sAddr.substring(0, delim)
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (ex: Exception) {
//            ex.printStackTrace()
//        }
//        return ""
//    }

    fun getVPNStatus(listener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener) {
        val url = "https://api.ipify.org?format=json"
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    val ipAddress = jsonObject.getString("ip")
                    val result = JSONObject().apply {
                        put("ip", ipAddress)
                    }
                    listener.onResponse(result)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            errorListener
        )
        requestQueue.add(stringRequest)
    }

    fun isVPNActive(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            activeNetwork != null && activeNetwork.type == ConnectivityManager.TYPE_VPN
        } else {
            cm.allNetworkInfo.any { it.type == ConnectivityManager.TYPE_VPN && it.isConnectedOrConnecting }
        }
    }
    fun getIPAddress(context: Context): String? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) ?: return null

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> getWiFiIPAddress(context)
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> getMobileIPAddress()
            else -> null
        }
    }

    private fun getWiFiIPAddress(context: Context): String? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress

        return String.format(
            "%d.%d.%d.%d",
            (ipAddress and 0xff),
            (ipAddress shr 8 and 0xff),
            (ipAddress shr 16 and 0xff),
            (ipAddress shr 24 and 0xff)
        )
    }

    private fun getMobileIPAddress(): String? {
        try {
            val interfaces: List<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (networkInterface in interfaces) {
                val addresses: List<InetAddress> = Collections.list(networkInterface.inetAddresses)
                for (address in addresses) {
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}