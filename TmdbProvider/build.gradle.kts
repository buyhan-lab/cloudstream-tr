version = 1

cloudstream {
    description = "TMDB katalog: film/dizi listesi, afis, ozet, Turkiye'de hangi platformda oldugu"
    authors = listOf("Burhan")
    status = 1
    tvTypes = listOf("Movie", "TvSeries")
    language = "tr"
    iconUrl = "https://www.themoviedb.org/assets/2/v4/logos/v2/blue_square_1-5bdc75aaebeb75dc7ae79426ddd9be3b2be1e342510f8202baf6bffa71d7f5c4.svg"
}

android {
    defaultConfig {
        buildConfigField("String", "TMDB_KEY", "\"${System.getenv("TMDB_API_KEY") ?: ""}\"")
    }
    buildFeatures {
        buildConfig = true
    }
}
