package com.donik1998.cubeclash.core.model

import java.util.Locale

/**
 * ISO 3166-1 alpha-2 code → display name.
 *
 * The wire carries only the code (`IR`, `UZ`) because localisation is not the server's job;
 * turning it into "Iran"/"Uzbekistan" is a client display concern and lives here so both the
 * leaderboard row and any future profile screen resolve names the same way.
 *
 * An unknown or malformed code **falls back to the raw code** rather than a blank or a guess —
 * a two-letter badge is still information, and a lie ("Unknown") is not.
 */
object CountryNames {

    // The set of codes the JVM actually knows. Anything outside it (an unassigned code like `ZZ`,
    // or garbage) is echoed back rather than resolved, because the JVM substitutes placeholder
    // strings such as "Unknown Region" for some unassigned-but-valid codes and we never want that.
    private val isoCountries: Set<String> = Locale.getISOCountries().toSet()

    /**
     * @param code an ISO 3166-1 alpha-2 code, any case. `null` or blank returns `null` so the
     *   UI can omit the country line entirely.
     * @return the localised country name, or the upper-cased raw code if the code is not a known
     *   ISO region.
     */
    fun displayName(code: String?): String? {
        val trimmed = code?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        val normalised = trimmed.uppercase(Locale.ROOT)
        if (normalised !in isoCountries) return normalised
        val resolved = Locale("", normalised).getDisplayCountry(Locale.ENGLISH)
        return resolved.ifEmpty { normalised }
    }
}
