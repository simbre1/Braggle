package com.github.simbre1.braggle

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

class AllWordsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_words)

        val allWords : Array<Pair<String, Boolean>> = intent?.extras?.get(ALL_WORDS) as Array<Pair<String, Boolean>>

        viewManager = LinearLayoutManager(this)
        viewAdapter = AllWordsAdapter(allWords.toList())

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
