package com.github.simbre1.braggle

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.simbre1.braggle.MainActivity.Companion.ALL_WORDS
import com.github.simbre1.braggle.MainActivity.Companion.DICTIONARY_LOOKUP_INTENT_PACKAGE
import com.github.simbre1.braggle.MainActivity.Companion.DICTIONARY_LOOKUP_URL

class AllWordsActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_words)

        @Suppress("UNCHECKED_CAST")
        val allWords : Array<Pair<String, Boolean>> = intent?.extras?.get(ALL_WORDS) as Array<Pair<String, Boolean>>
        val dictionaryLookupIntentPackage = intent?.extras?.getString(DICTIONARY_LOOKUP_INTENT_PACKAGE)
        val dictionaryUrl = intent?.extras?.getString(DICTIONARY_LOOKUP_URL)

        viewManager = LinearLayoutManager(this)
        viewAdapter = AllWordsAdapter(
            allWords.toList(),
            dictionaryLookupIntentPackage,
            dictionaryUrl)

        recyclerView = findViewById<RecyclerView>(R.id.allWordsView).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }
    }
}
