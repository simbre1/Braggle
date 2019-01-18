package com.github.simbre1.braggle

import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class AllWordsAdapter(private val myDataset: List<Pair<String, Boolean>>,
                      private val dictionaryLookupIntentPackage: String?,
                      private val dictionaryUrl: String?) :
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
            holder.textView.setTextColor(holder.view.context.getColorFromAttr(R.attr.colorTextFound))
        } else {
            holder.textView.setTextColor(holder.view.context.getColorFromAttr(R.attr.colorTextNotFound))
        }

        var intent: Intent? = null
        if (dictionaryLookupIntentPackage != null) {
            val packageIntent = Intent(Intent.ACTION_SEARCH)
            packageIntent.setPackage(dictionaryLookupIntentPackage)
            packageIntent.putExtra(SearchManager.QUERY, word)
            if (packageIntent.resolveActivity(holder.view.context.packageManager) != null) {
                intent = packageIntent
            }
        }
        if(intent == null && dictionaryUrl != null){
            val webpage: Uri = Uri.parse(dictionaryUrl.replace("%s", word.toLowerCase()))
            val urlIntent = Intent(Intent.ACTION_VIEW, webpage)
            if (urlIntent.resolveActivity(holder.view.context.packageManager) != null) {
                intent = urlIntent
            }
        }

        intent?.run {
            holder.textView.setOnClickListener {
                try {
                    holder.view.context.startActivity(this)
                } catch (ignored: Exception) {
                }
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}