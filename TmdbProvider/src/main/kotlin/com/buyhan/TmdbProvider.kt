package com.buyhan

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import org.json.JSONObject

class TmdbProvider : MainAPI() {
    override var name = "TMDB Katalog"
    override var mainUrl = "https://api.themoviedb.org/3"
    override var lang = "en"
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    private val key = BuildConfig.TMDB_KEY
    private val img = "https://image.tmdb.org/t/p/w500"

    override val mainPage = mainPageOf(
        "movie/popular" to "Popüler Filmler",
        "movie/top_rated" to "En Yüksek Puanlı Filmler",
        "tv/popular" to "Popüler Diziler",
        "tv/top_rated" to "En Yüksek Puanlı Diziler",
    )

    private fun poster(o: JSONObject): String? =
        o.optString("poster_path").takeIf { it.isNotBlank() && it != "null" }?.let { img + it }

    private fun toResult(o: JSONObject, isTv: Boolean): SearchResponse {
        val title = o.optString(if (isTv) "name" else "title")
        val url = "${if (isTv) "tv" else "movie"}/${o.getInt("id")}"
        val p = poster(o)
        return if (isTv) newTvSeriesSearchResponse(title, url, TvType.TvSeries) { posterUrl = p }
        else newMovieSearchResponse(title, url, TvType.Movie) { posterUrl = p }
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val isTv = request.data.startsWith("tv")
        val json = JSONObject(
            app.get("$mainUrl/${request.data}?api_key=$key&language=tr-TR&page=$page").text
        )
        val arr = json.getJSONArray("results")
        val items = (0 until arr.length()).map { toResult(arr.getJSONObject(it), isTv) }
        return newHomePageResponse(request.name, items)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val json = JSONObject(
            app.get("$mainUrl/search/multi?api_key=$key&language=tr-TR&query=$query").text
        )
        val arr = json.getJSONArray("results")
        return (0 until arr.length()).mapNotNull {
            val o = arr.getJSONObject(it)
            when (o.optString("media_type")) {
                "movie" -> toResult(o, false)
                "tv" -> toResult(o, true)
                else -> null
            }
        }
    }

    private suspend fun watchProviders(url: String): String = try {
        val j = JSONObject(app.get("$mainUrl/$url/watch/providers?api_key=$key").text)
        val arr = j.getJSONObject("results").optJSONObject("TR")?.optJSONArray("flatrate")
        if (arr == null) "" else (0 until arr.length()).joinToString(", ") {
            arr.getJSONObject(it).getString("provider_name")
        }
    } catch (e: Exception) {
        ""
    }

    override suspend fun load(url: String): LoadResponse {
        val isTv = url.startsWith("tv")
        val o = JSONObject(app.get("$mainUrl/$url?api_key=$key&language=tr-TR").text)
        val title = o.optString(if (isTv) "name" else "title")
        val where = watchProviders(url)
        val desc = o.optString("overview") +
            if (where.isNotBlank()) "\n\nİzleyebileceğin yer: $where" else ""
        val p = poster(o)
        val y = o.optString(if (isTv) "first_air_date" else "release_date").take(4).toIntOrNull()

        return if (isTv) newTvSeriesLoadResponse(title, url, TvType.TvSeries, emptyList()) {
            posterUrl = p; plot = desc; year = y
        } else newMovieLoadResponse(title, url, TvType.Movie, url) {
            posterUrl = p; plot = desc; year = y
        }
    }

    // Katalog eklentisi: video kaynagi yok
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean = false
}
