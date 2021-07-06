package com.mahi.videolyticsframework.api

import android.content.Context
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import com.mahi.videolyticsframework.VideoLytics
import com.mahi.videolyticsframework.model.AnalyticsData
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class Repository {

    fun pushAnalyticsData(context: Context, analyticsData: AnalyticsData) {

        val analyticsDataJSON = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(analyticsData)
        Log.d("myHttpResponse", analyticsDataJSON.toString())

        try {
            val requestQueue = Volley.newRequestQueue(context)
            val url = VideoLytics.BASE_URL + "posts"
            val jsonBody = JSONObject(analyticsDataJSON)
            val requestBody = jsonBody.toString()
            val stringRequest: StringRequest = object : StringRequest(
                Request.Method.POST, url,
                Response.Listener { response -> Log.i("VOLLEY Httpcode -->>", response!!) },
                Response.ErrorListener { error -> Log.e("VOLLEY error", error.toString()) }) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                @Throws(AuthFailureError::class)
                override fun getBody(): ByteArray? {
                    return try {
                        return requestBody.encodeToByteArray()
                    } catch (uee: UnsupportedEncodingException) {
                        VolleyLog.wtf(
                            "Unsupported Encoding while trying to get the bytes of %s using %s",
                            requestBody,
                            "utf-8"
                        )
                        null
                    }
                }

                override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                    val responseString: String = response.statusCode.toString()
                    return Response.success(
                        responseString,
                        HttpHeaderParser.parseCacheHeaders(response)
                    )
                }
            }
            requestQueue.add(stringRequest)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}