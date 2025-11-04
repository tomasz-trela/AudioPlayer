package com.example.audioplayer.audioplayer.data


class SongRepository {
    private val baseUrl = "https://raw.githubusercontent.com/tomasz-trela/example-music/main/"
    fun getSongs(): List<Song> {
        return listOf(
            Song(
                "${baseUrl}Groovy%20Vibe.mp3",
                "Groovy Vibe",
                "Bransboynd",
                "https://cdn.pixabay.com/photo/2023/03/17/08/14/psychedelic-7858138_1280.jpg"
            ),
            Song(
                "${baseUrl}Running%20Night%20Music.mp3",
                "Running Night Music",
                "Alex_MakeMusic",
                "https://cdn.pixabay.com/photo/2024/09/04/00/58/flagbearer-9020475_1280.png"
            ),
            Song(
                "${baseUrl}Deep%20Abstract%20Ambient%20Snowcap.mp3",
                "Deep Abstract Ambient Snowcap",
                "ummbrella",
                "https://cdn.pixabay.com/photo/2016/03/26/05/32/fractal-1280107_1280.jpg"
            ),
            Song(
                "${baseUrl}Cascade%20Breathe%20Future%20Garage.mp3",
                "Cascade Breathe",
                "NverAvetyanMusic",
                "https://cdn.pixabay.com/photo/2022/10/17/18/03/breathing-7528398_1280.jpg"
            )
        )
    }
}