package com.uygemre.qrcode.recyclerview

import android.annotation.SuppressLint
import android.graphics.Outline
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.uygemre.qrcode.R
import com.uygemre.qrcode.database.QRCodeDTO
import com.uygemre.qrcode.extensions.dp2px
import com.uygemre.qrcode.extensions.inflate
import com.uygemre.qrcode.extensions.isLollipop
import kotlinx.android.synthetic.main.item_history.view.*

class RecyclerViewAdapter(
    private var list: List<QRCodeDTO>,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<RecyclerViewAdapter.QRCodeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QRCodeViewHolder {
        val view = parent.inflate(R.layout.item_history)
        val elevation = view.resources.getDimension(R.dimen.cardElevation)
        ViewCompat.setElevation(view, elevation)
        return QRCodeViewHolder(view)
    }

    fun update(list: List<QRCodeDTO>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: QRCodeViewHolder, position: Int) {
        holder.bind(list[position], position, list.size, itemClickListener)
    }

    override fun getItemCount(): Int = list.size

    class QRCodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val contentPadding: Int = dp2px(16).toInt()
        private val defaultOutline: ViewOutlineProvider? by lazy {
            if (isLollipop()) ViewOutlineProvider.BACKGROUND else null
        }
        private val fixedOutline: ViewOutlineProvider? by lazy {
            if (isLollipop()) {
                object : ViewOutlineProvider() {
                    @SuppressLint("NewApi")
                    override fun getOutline(view: View, outline: Outline) {
                        outline.setRect(
                            0,
                            view.resources.getDimensionPixelSize(R.dimen.cardElevation),
                            view.width,
                            view.height
                        )
                    }
                }
            } else null
        }

        fun bind(item: QRCodeDTO, position: Int, size: Int, clickListener: OnItemClickListener) =
            with(itemView) {
                bindBackground(itemView, position, size)
                bindOutlineProvider(itemView, position, size)

                img_qr.setImageResource(item.image ?: 0)
                tv_qr_format.text = item.format
                tv_qr_date.text = item.date
                tv_qr_description.text = Html.fromHtml(item.description)
                img_delete.setOnClickListener {
                    clickListener.deleteItemOnClicked(item, position)
                }
                root_history.setOnClickListener {
                    clickListener.goDetailItemOnClicked(item)
                }
            }

        private fun bindBackground(itemView: View, position: Int, size: Int) {
            val drawableRes: Int = when {
                size == 1 -> R.drawable.item_top_bottom
                position == 0 -> R.drawable.item_top
                position == size - 1 -> R.drawable.item_bottom
                else -> R.drawable.item_middle
            }
            itemView.background = ContextCompat
                .getDrawable(itemView.context, drawableRes)
            itemView.setPadding(
                contentPadding, contentPadding,
                contentPadding, contentPadding
            )
        }

        private fun bindOutlineProvider(itemView: View, position: Int, size: Int) {
            if (!isLollipop()) return
            itemView.outlineProvider =
                if (size == 1 || position == 0) defaultOutline else fixedOutline
        }
    }
}

