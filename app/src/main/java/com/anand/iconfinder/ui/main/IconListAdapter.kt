package com.anand.iconfinder.ui.main

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.anand.iconfinder.BuildConfig
import com.anand.iconfinder.R
import com.anand.iconfinder.model.Icon
import com.anand.iconfinder.utils.*
import kotlinx.android.synthetic.main.grid_item.view.*


class IconListAdapter(var list: List<Icon>)
    : RecyclerView.Adapter<IconListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.grid_item, parent, false)
        )

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    fun submitList(list: List<Icon>) {
        this.list = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(position: Int) {
            val item = list[position]

            with(itemView) {
                //  icon_name.text = item.type Name isn't coming from the API
                if (item.rasterSizes.size > 6)
                    image.load(item.rasterSizes[6].formats[0].previewUrl) {
                        crossfade(true)
                        placeholder(R.drawable.ic_loading)
                    }   // For testing purposes
                // taking 128 px image only otherwise using index directly like 6 is a bad practice
                else
                    image.load(item.rasterSizes[0].formats[0].previewUrl) {
                        crossfade(true)
                        placeholder(R.drawable.ic_loading)
                    }

                if (item.isPremium) { // Show Price
                    image_paid.visibility = View.VISIBLE
                    download_btn.visibility = View.GONE
                    price.visibility = View.VISIBLE

                    if (item.prices.isNotEmpty())
                        price.text = "${item.prices[0].currency} ${item.prices[0].price}"

                } else { // Show Download Button

                    image_paid.visibility = View.INVISIBLE
                    download_btn.visibility = View.VISIBLE
                    price.visibility = View.INVISIBLE

                    setButtonClick(item)
                }
            }
        }

        private fun View.setButtonClick(item: Icon) {
            lateinit var mainActivity: MainActivity

            download_btn.setOnClickListener {
//                if (!isPermissionGranted(context)){
//
//                    askForPermission(mainActivity.parent)
//                } else {
                    val filePath = item.rasterSizes[0].formats[0].downloadUrl
                    val downloadUrl = getDownloadUrl("$BASE_URL$filePath")
                    downloadImage(context, downloadUrl)
//                }
            }
        }

        private fun getDownloadUrl(baseUrl: String) =
            "$baseUrl?$CLIENT_ID=${BuildConfig.CLIENT_ID}&$CLIENT_SECRET=${BuildConfig.CLIENT_SECRET}"
    }
}
