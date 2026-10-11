package com.example.data

object SampleBrailleLibrary {

    val sampleBooks: List<BrailleDocument> = listOf(
        BrailleDocument(
            id = "braille_book_1",
            title = "The Little Prince (Braille Edition)",
            brailleUnicode = """
⠠⠕⠝⠉⠑ ⠥⠏⠕⠝ ⠁ ⠞⠊⠍⠑ ⠞⠓⠑⠗⠑ ⠺⠁⠎ ⠁ ⠇⠊⠞⠞⠇⠑ ⠏⠗⠊⠝⠉⠑ ⠺⠓⠕ ⠇⠊⠧⠑⠙ ⠕⠝ ⠁ ⠏⠇⠁⠝⠑⠞ ⠞⠓⠁⠞ ⠺⠁⠎ ⠎⠉⠁⠗⠉⠑⠇⠽ ⠃⠊⠛⠛⠑⠗ ⠞⠓⠁⠝ ⠓⠊⠍⠎⠑⠇⠋⠂ ⠁⠝⠙ ⠺⠓⠕ ⠓⠁⠙ ⠝⠑⠑⠙ ⠕⠋ ⠁ ⠎⠓⠑⠑⠏⠲
⠠⠊⠞ ⠊⠎ ⠕⠝⠇⠽ ⠺⠊⠞⠓ ⠞⠓⠑ ⠓⠑⠁⠗⠞ ⠞⠓⠁⠞ ⠕⠝⠑ ⠉⠁⠝ ⠎⠑⠑ ⠗⠊⠛⠓⠞⠇⠽⠞ ⠺⠓⠁⠞ ⠊⠎ ⠑⠎⠎⠑⠝⠞⠊⠁⠇ ⠊⠎ ⠊⠝⠧⠊⠎⠊⠃⠇⠑ ⠞⠕ ⠞⠓⠑ ⠑⠽⠑⠲
⠠⠁⠇⠇ ⠛⠗⠕⠺⠝ ⠥⠏⠎ ⠺⠑⠗⠑ ⠕⠝⠉⠑ ⠉⠓⠊⠇⠙⠗⠑⠝ ⠁⠇⠞⠓⠕⠥⠛⠓ ⠋⠑⠺ ⠕⠋ ⠞⠓⠑⠍ ⠗⠑⠍⠑⠍⠃⠑⠗ ⠊⠞⠲
            """.trimIndent(),
            translatedText = "Once upon a time there was a little prince who lived on a planet that was scarcely bigger than himself, and who had need of a sheep. It is only with the heart that one can see rightly; what is essential is invisible to the eye. All grown-ups were once children, although few of them remember it.",
            summary = "The Little Prince lives alone on an asteroid and longs for companionship. The core message reveals that true value and emotion are felt through the heart, not seen with physical eyes, reminding adults of their lost childhood wonder.",
            keyPoints = listOf(
                "Origin of the Little Prince on his tiny planet",
                "The essential secret: Invisible truth seen only by the heart",
                "Reflection on the disconnect between adult life and childhood innocence"
            ),
            sampleQuestions = listOf(
                "What is the famous secret told to the prince?",
                "Where did the little prince live?",
                "What do grown-ups forget according to this text?"
            )
        ),
        BrailleDocument(
            id = "braille_book_2",
            title = "Astronomy & Cosmos (Tactile Science)",
            brailleUnicode = """
⠠⠕⠥⠗ ⠎⠥⠝ ⠊⠎ ⠕⠝⠑ ⠎⠞⠁⠗ ⠁⠍⠕⠝⠛ ⠞⠺⠕ ⠓⠥⠝⠙⠗⠑⠙ ⠃⠊⠇⠇⠊⠕⠝ ⠎⠞⠁⠗⠎ ⠊⠝ ⠞⠓⠑ ⠠⠍⠊⠇⠅⠽ ⠠⠺⠁⠽ ⠛⠁⠇⠁⠭⠽⠲
⠠⠇⠊⠛⠓⠞ ⠋⠗⠕⠍ ⠞⠓⠑ ⠎⠥⠝ ⠞⠁⠅⠑⠎ ⠑⠊⠛⠓⠞ ⠍⠊⠝⠥⠞⠑⠎ ⠁⠝⠙ ⠞⠺⠑⠝⠞⠽ ⠎⠑⠉⠕⠝⠙⠎ ⠞⠕ ⠗⠑⠁⠉⠓ ⠠⠑⠁⠗⠞⠓⠲
⠠⠎⠕⠥⠝⠙ ⠉⠁⠝⠝⠕⠞ ⠞⠗⠁⠧⠑⠇ ⠞⠓⠗⠕⠥⠛⠓ ⠞⠓⠑ ⠧⠁⠉⠥⠥⠍ ⠕⠋ ⠎⠏⠁⠉⠑⠂ ⠍⠁⠅⠊⠝⠛ ⠞⠓⠑ ⠉⠕⠎⠍⠕⠎ ⠁ ⠎⠊⠇⠑⠝⠞ ⠗⠑⠁⠇⠍ ⠕⠋ ⠛⠗⠁⠧⠊⠞⠽ ⠁⠝⠙ ⠇⠊⠛⠓⠞⠲
            """.trimIndent(),
            translatedText = "Our Sun is one star among two hundred billion stars in the Milky Way galaxy. Light from the Sun takes eight minutes and twenty seconds to reach Earth. Sound cannot travel through the vacuum of space, making the cosmos a silent realm of gravity and light.",
            summary = "An overview of our solar neighborhood within the Milky Way galaxy, detailing the 8-minute journey of sunlight to Earth and explaining why the vast cosmos remains silent.",
            keyPoints = listOf(
                "The Sun is one of 200 billion stars in our galaxy",
                "Sunlight transit time is 8 minutes and 20 seconds",
                "Space vacuum prevents sound transmission"
            ),
            sampleQuestions = listOf(
                "How long does sunlight take to reach Earth?",
                "How many stars are in the Milky Way galaxy?",
                "Why is space silent?"
            )
        ),
        BrailleDocument(
            id = "braille_book_3",
            title = "Universal Declaration of Human Rights",
            brailleUnicode = """
⠠⠁⠇⠇ ⠓⠥⠍⠁⠝ ⠃⠑⠊⠝⠛⠎ ⠁⠗⠑ ⠃⠕⠗⠝ ⠋⠗⠑⠑ ⠁⠝⠙ ⠑⠟⠥⠁⠇ ⠊⠝ ⠙⠊⠛⠝⠊⠞⠽ ⠁⠝⠙ ⠗⠊⠛⠓⠞⠎⠲
⠠⠞⠓⠑⠽ ⠁⠗⠑ ⠑⠝⠙⠕⠺⠑⠙ ⠺⠊⠞⠓ ⠗⠑⠁⠎⠕⠝ ⠁⠝⠙ ⠉⠕⠝⠎⠉⠊⠑⠝⠉⠑ ⠁⠝⠙ ⠎⠓⠕⠥⠇⠙ ⠁⠉⠞ ⠞⠕⠺⠁⠗⠙⠎ ⠕⠝⠑ ⠁⠝⠕⠞⠓⠑⠗ ⠊⠝ ⠁ ⠎⠏⠊⠗⠊⠞ ⠕⠋ ⠃⠗⠕⠞⠓⠑⠗⠓⠕⠕⠙⠲
            """.trimIndent(),
            translatedText = "All human beings are born free and equal in dignity and rights. They are endowed with reason and conscience and should act towards one another in a spirit of brotherhood.",
            summary = "Article 1 establishes the universal foundation of human freedom, dignity, conscience, and brotherhood among all persons worldwide.",
            keyPoints = listOf(
                "Fundamental human equality and dignity from birth",
                "Endowment of human conscience and reasoning",
                "Call to act in mutual fraternity and respect"
            ),
            sampleQuestions = listOf(
                "What are all human beings born with?",
                "What spirit should guide human relationships?",
                "Which human faculties are explicitly mentioned?"
            )
        ),
        BrailleDocument(
            id = "braille_script_mary_lamb",
            title = "Mary Had a Little Lamb (Braille Script)",
            brailleUnicode = """
⠠⠍⠁⠗⠽ ⠓⠁⠙ ⠁ ⠇⠊⠞⠞⠇⠑ ⠇⠁⠍⠃⠂
⠱⠕⠎⠑ ⠋⠇⠑⠑⠉⠑ ⠺⠁⠎ ⠺⠓⠊⠞⠑ ⠁⠎ ⠎⠝⠕⠺⠂
⠁⠝⠙ ⠑⠧⠑⠗⠽⠺⠓⠑⠗⠑ ⠹⠁⠞ ⠠⠍⠁⠗⠽ ⠺⠑⠝⠞
⠮ ⠇⠁⠍⠃ ⠺⠁⠎ ⠎⠥⠗⠑ ⠞⠕ ⠛⠕⠲
⠠⠓⠑ ⠋⠕⠇⠇⠕⠺⠑⠙ ⠓⠑⠗ ⠞⠕ ⠎⠉⠓⠕⠕⠇ ⠕⠝⠑ ⠙⠁⠽⠂
⠱⠊⠉⠓ ⠺⠁⠎ ⠁⠛⠁⠊⠝⠎⠞ ⠮ ⠗⠥⠇⠑⠆
⠊⠞ ⠍⠁⠙⠑ ⠮ ⠉⠓⠊⠇⠙⠗⠑⠝ ⠇⠁⠥⠛⠓ ⠁⠝⠙ ⠏⠇⠁⠽⠂
⠞⠕ ⠎⠑⠑ ⠁ ⠇⠁⠍⠃ ⠁⠞ ⠎⠉⠓⠕⠕⠇⠲
⠠⠁⠝⠙ ⠎⠕ ⠮ ⠞⠑⠁⠉⠓⠑⠗ ⠞⠥⠗⠝⠑⠙ ⠓⠊⠍ ⠕⠥⠞⠂
⠃⠥⠞ ⠎⠞⠊⠇⠇ ⠓⠑ ⠇⠊⠝⠛⠑⠗⠑⠙ ⠝⠑⠁⠗⠂
            """.trimIndent(),
            translatedText = "Mary had a little lamb, whose fleece was white as snow, and everywhere that Mary went the lamb was sure to go. He followed her to school one day, which was against the rule; it made the children laugh and play, to see a lamb at school. And so the teacher turned him out, but still he lingered near.",
            summary = "This Braille script contains the classic poem 'Mary Had a Little Lamb'. It narrates the story of Mary and her loyal pet lamb following her all the way to school, amusing the children before being sent outside by the teacher.",
            keyPoints = listOf(
                "Mary has a loyal pet lamb with fleece as white as snow",
                "The lamb follows Mary to school against the rules, causing laughter",
                "The teacher turns the lamb out, but it patiently waits nearby"
            ),
            sampleQuestions = listOf(
                "What color was the lamb's fleece?",
                "Why did the teacher turn the lamb out?",
                "What happened when the children saw the lamb at school?"
            )
        )
    )
}
