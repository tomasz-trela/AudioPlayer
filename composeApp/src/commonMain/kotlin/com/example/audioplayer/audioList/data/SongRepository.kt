package com.example.audioplayer.audioList.data


class SongRepository {
    private val baseUrl = "https://raw.githubusercontent.com/tomasz-trela/example-music/main/"
    fun getSongs(): List<Song> {
        return listOf(
            Song(
                "${baseUrl}Groovy%20Vibe.mp3",
                "Groovy Vibe",
                "Bransboynd"
            ),
            Song(
                "${baseUrl}Running%20Night%20Music.mp3",
                "Running Night Music",
                "Alex_MakeMusic"
            ),
            Song(
                "${baseUrl}Deep%20Abstract%20Ambient%20Snowcap.mp3",
                "Deep Abstract Ambient Snowcap",
                "ummbrella"
            ),
            Song(
                "${baseUrl}Cascade%20Breathe%20Future%20Garage.mp3",
                "Cascade Breathe",
                "NverAvetyanMusic"
            )
        )
    }
}