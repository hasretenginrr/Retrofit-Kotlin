data class WeatherResponse(
    val name: String,  // Şehir adı
    val main: Main,    // Ana veri (sıcaklık, nem, vs.)
    val weather: List<Weather> // Hava durumu verisi (açıklama, ikon, vs.)
)

data class Main(
    val temp: Double,  // Sıcaklık
    val humidity: Int  // Nem
)

data class Weather(
    val description: String  // Hava durumu açıklaması (örneğin, açık hava, yağmur)
)
