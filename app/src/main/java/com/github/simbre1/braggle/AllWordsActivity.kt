package com.github.simbre1.braggle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AllWordsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_words)

        @Suppress("UNCHECKED_CAST")
        val allWords : Array<Pair<String, Boolean>> = intent?.extras?.get(ALL_WORDS) as Array<Pair<String, Boolean>>
        val dictionaryLookupIntentPackage = intent?.extras?.getString(DICTIONARY_LOOKUP_INTENT_PACKAGE)

        viewManager = LinearLayoutManager(this)
        viewAdapter = AllWordsAdapter(allWords.toList(), dictionaryLookupIntentPackage)

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
