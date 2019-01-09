package com.github.simbre1.braggle

import android.app.SearchManager
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class AllWordsAdapter(private val myDataset: List<Pair<String, Boolean>>,
                      private val dictionaryLookupIntentPackage: String?) :
        RecyclerView.Adapter<AllWordsAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val textView : TextView = view.findViewById(R.id.textView) as TextView
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): AllWordsAdapter.MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
                .inflate(R.layout.word_in_list_view, parent, false)
        // set the view's size, margins, paddings and layout parameters
        //...
        return MyViewHolder(textView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        val word = myDataset[position].first
        holder.textView.text = word
        if (myDataset[position].second) {
            holder.textView.setTextColor(BoardView.getColor(holder.view.context, R.attr.colorTextFound) ?: Color.CYAN)
        } else {
            holder.textView.setTextColor(BoardView.getColor(holder.view.context, R.attr.colorTextNotFound) ?: Color.RED)
        }

        if (dictionaryLookupIntentPackage != null) {
            holder.textView.setOnClickListener{
                val intent = Intent(Intent.ACTION_SEARCH)
                intent.setPackage(dictionaryLookupIntentPackage)
                intent.putExtra(SearchManager.QUERY, word)
                try {
                    holder.view.context.startActivity(intent)
                } catch (ignored: Exception) {
                }
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}