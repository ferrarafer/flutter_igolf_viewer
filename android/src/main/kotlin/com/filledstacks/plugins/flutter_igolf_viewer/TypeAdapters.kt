package com.filledstacks.plugins.flutter_igolf_viewer

import com.google.gson.*
import java.lang.reflect.Type

class MapDeserializer : JsonDeserializer<Map<String?, Array<Int>?>> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Map<String?, Array<Int>?> {
        val jsonObject = json.asJsonObject
        val resultMap = mutableMapOf<String?, Array<Int>?>()

        jsonObject.entrySet().forEach { entry ->
            val key = entry.key
            val jsonArray = entry.value.asJsonArray
            val intArray = jsonArray.map { it.asInt }.toTypedArray()
            resultMap[key] = intArray
        }

        return resultMap
    }
}