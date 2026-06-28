package minefarts.smarttube.utils.innertube.helpers

import com.google.gson.GsonBuilder
import java.util.regex.Pattern

/**
 * Finds a string between two delimiters.
 * @param data the data
 * @param startString start string
 * @param endString end string
 * @return the string between start and end, or null if not found
 */
public fun getStringBetweenStrings(data: String, startString: String, endString: String): String? {
    val regex = escapeStringRegexp(startString) + "(.*?)" + escapeStringRegexp(endString)
    val pattern = Pattern.compile(regex, Pattern.DOTALL)
    val matcher = pattern.matcher(data)
    if (matcher.find()) {
        return matcher.group(1)
    } else {
        return null
    }
}

/**
 * Escapes a string to be used in a regex.
 * @param input input string
 * @return escaped string
 */
public fun escapeStringRegexp(input: String): String {
    // Escape special regex characters
    val escaped = input.replace("([|\\\\{}()\\[\\]^$+*?.])".toRegex(), "\\\\$1")
    // Replace dash
    return escaped.replace("-", "\\x2d")
}

public enum class DeviceCategory {
    MOBILE,
    DESKTOP
}

public fun UserAgents.byCategory(category: DeviceCategory): List<String> =
    when (category) {
        DeviceCategory.DESKTOP -> desktop
        DeviceCategory.MOBILE -> mobile
    }

public fun getRandomUserAgent(type: DeviceCategory): String {
    return UserAgents
        .byCategory(type)
        .random()
}

public fun toJsonString(obj: Any): String {
    val gson = GsonBuilder().create() // nulls are ignored by default
    return gson.toJson(obj)
}