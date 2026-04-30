package com.example.motsic

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class EpisodeStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("episodes", Context.MODE_PRIVATE)

    fun all(): List<Episode> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        val arr = JSONArray(raw)
        val list = ArrayList<Episode>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                Episode(
                    timestampMs = o.getLong("ts"),
                    trigger = o.getString("trigger"),
                    severity = o.getInt("severity"),
                    helped = o.optString("helped", "")
                )
            )
        }
        return list.sortedByDescending { it.timestampMs }
    }

    fun add(episode: Episode) {
        val current = all().toMutableList()
        current.add(episode)
        save(current)
    }

    private fun save(list: List<Episode>) {
        val arr = JSONArray()
        list.forEach { e ->
            arr.put(JSONObject().apply {
                put("ts", e.timestampMs)
                put("trigger", e.trigger)
                put("severity", e.severity)
                put("helped", e.helped)
            })
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    companion object {
        private const val KEY = "data"
    }
}
