package com.example.editor.engine

import java.util.Locale

/**
 * Real-time spelling analysis and typo detection dictionary.
 */
object SpellCheckEngine {

    val COMMON_TYPOS: Map<String, String> = mapOf(
        "teh" to "the",
        "recieve" to "receive",
        "seperate" to "separate",
        "definately" to "definitely",
        "untill" to "until",
        "occured" to "occurred",
        "accomodate" to "accommodate",
        "wierd" to "weird",
        "goverment" to "government",
        "enviroment" to "environment",
        "tommorrow" to "tomorrow",
        "beleive" to "believe",
        "truely" to "truly",
        "writting" to "writing",
        "sucessful" to "successful",
        "begining" to "beginning",
        "neccessary" to "necessary",
        "embarass" to "embarrass",
        "existance" to "existence",
        "guarentee" to "guarantee",
        "happend" to "happened",
        "independant" to "independent",
        "relevent" to "relevant",
        "tendancy" to "tendency",
        "usefull" to "useful",
        "alot" to "a lot"
    )

    fun findTypos(text: String): List<Triple<Int, Int, String>> {
        if (text.isBlank()) return emptyList()
        val typos = mutableListOf<Triple<Int, Int, String>>()
        val wordRegex = Regex("\\b[a-zA-Z]+\\b")
        for (match in wordRegex.findAll(text)) {
            val word = match.value.lowercase(Locale.ROOT)
            val suggestion = COMMON_TYPOS[word]
            if (suggestion != null) {
                typos.add(Triple(match.range.first, match.range.last + 1, suggestion))
            }
        }
        return typos
    }
}
