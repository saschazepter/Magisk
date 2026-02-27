package com.topjohnwu.magisk.view

import android.app.Activity
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.shape.MaterialShapeDrawable
import com.topjohnwu.magisk.R

typealias DialogButtonClickListener = (DialogInterface) -> Unit

class MagiskDialog(
    context: Activity, theme: Int = 0
) : AppCompatDialog(context, theme) {

    private val root: View = LayoutInflater.from(context).inflate(R.layout.dialog_magisk_base, null)
    private val iconView: ImageView = root.findViewById(R.id.dialog_base_icon)
    private val titleView: TextView = root.findViewById(R.id.dialog_base_title)
    private val messageView: TextView = root.findViewById(R.id.dialog_base_message)
    private val containerView: ViewGroup = root.findViewById(R.id.dialog_base_container)
    private val buttonPositiveView: MaterialButton = root.findViewById(R.id.dialog_base_button_1)
    private val buttonNeutralView: MaterialButton = root.findViewById(R.id.dialog_base_button_2)
    private val buttonNegativeView: MaterialButton = root.findViewById(R.id.dialog_base_button_3)

    val activity: AppCompatActivity get() = ownerActivity as AppCompatActivity

    init {
        setCancelable(true)
        setOwnerActivity(context)
    }

    enum class ButtonType {
        POSITIVE, NEUTRAL, NEGATIVE
    }

    interface Button {
        var icon: Int
        var text: Any
        var isEnabled: Boolean
        var doNotDismiss: Boolean

        fun onClick(listener: DialogButtonClickListener)
    }

    inner class ButtonViewModel(private val view: MaterialButton) : Button {
        override var icon = 0
            set(value) {
                field = value
                if (value != 0) view.setIconResource(value) else view.icon = null
                updateVisibility()
            }

        private var message: String = ""
            set(value) {
                field = value
                view.text = value.ifEmpty { null }
                updateVisibility()
            }

        override var text: Any
            get() = message
            set(value) {
                message = when (value) {
                    is Int -> context.getText(value)
                    else -> value
                }.toString()
            }

        override var isEnabled = true
            set(value) {
                field = value
                view.isEnabled = value
                view.isClickable = value
                view.isFocusable = value
            }

        override var doNotDismiss = false

        private var onClickAction: DialogButtonClickListener = {}

        override fun onClick(listener: DialogButtonClickListener) {
            onClickAction = listener
        }

        private fun updateVisibility() {
            val show = icon != 0 || message.isNotEmpty()
            view.isVisible = show
        }

        fun attach() {
            view.setOnClickListener {
                onClickAction(this@MagiskDialog)
                if (!doNotDismiss) dismiss()
            }
        }
    }

    private val btnPositive = ButtonViewModel(buttonPositiveView)
    private val btnNeutral = ButtonViewModel(buttonNeutralView)
    private val btnNegative = ButtonViewModel(buttonNegativeView)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(root)

        val default = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, javaClass.canonicalName)
        val surfaceColor = MaterialColors.getColor(context, R.attr.colorSurfaceSurfaceVariant, default)
        val materialShapeDrawable = MaterialShapeDrawable(context, null, androidx.appcompat.R.attr.alertDialogStyle, com.google.android.material.R.style.MaterialAlertDialog_MaterialComponents)
        materialShapeDrawable.initializeElevationOverlay(context)
        materialShapeDrawable.fillColor = ColorStateList.valueOf(surfaceColor)
        materialShapeDrawable.elevation = context.resources.getDimension(R.dimen.margin_generic)
        materialShapeDrawable.setCornerSize(context.resources.getDimension(R.dimen.l_50))

        val inset = context.resources.getDimensionPixelSize(com.google.android.material.R.dimen.appcompat_dialog_background_inset)
        window?.apply {
            setBackgroundDrawable(InsetDrawable(materialShapeDrawable, inset, inset, inset, inset))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        btnPositive.attach()
        btnNeutral.attach()
        btnNegative.attach()
    }

    override fun setTitle(@StringRes titleId: Int) {
        val str = context.getString(titleId)
        titleView.text = str
        titleView.isVisible = str.isNotEmpty()
    }

    override fun setTitle(title: CharSequence?) {
        val str = title ?: ""
        titleView.text = str
        titleView.isVisible = str.isNotEmpty()
    }

    fun setMessage(@StringRes msgId: Int, vararg args: Any) {
        setMessage(context.getString(msgId, *args))
    }

    fun setMessage(message: CharSequence) {
        messageView.text = message
        val show = message.isNotEmpty()
        messageView.isVisible = show
        // container is visible when message is NOT shown
        if (show) containerView.isVisible = false
    }

    fun setIcon(@DrawableRes drawableRes: Int) {
        setIcon(AppCompatResources.getDrawable(context, drawableRes)!!)
    }

    fun setIcon(drawable: Drawable) {
        iconView.setImageDrawable(drawable)
        iconView.isVisible = true
    }

    fun setButton(buttonType: ButtonType, builder: Button.() -> Unit) {
        val button = when (buttonType) {
            ButtonType.POSITIVE -> btnPositive
            ButtonType.NEUTRAL -> btnNeutral
            ButtonType.NEGATIVE -> btnNegative
        }
        button.apply(builder)
    }

    class DialogItem(val item: CharSequence, val position: Int)

    fun interface DialogClickListener {
        fun onClick(position: Int)
    }

    fun setListItems(
        list: Array<out CharSequence>,
        listener: DialogClickListener
    ) = setView(
        RecyclerView(context).also { rv ->
            rv.isNestedScrollingEnabled = false
            rv.layoutManager = LinearLayoutManager(context)
            rv.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                val items = list.mapIndexed { i, cs -> DialogItem(cs, i) }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                    val tv = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_list_single_line, parent, false) as TextView
                    return object : RecyclerView.ViewHolder(tv) {}
                }

                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    val item = items[position]
                    (holder.itemView as TextView).text = item.item
                    holder.itemView.setOnClickListener {
                        listener.onClick(item.position)
                        dismiss()
                    }
                }

                override fun getItemCount() = items.size
            }
        }
    )

    fun setView(view: View) {
        containerView.removeAllViews()
        containerView.addView(
            view,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        containerView.isVisible = messageView.text.isEmpty()
        messageView.isVisible = false
    }

    fun resetButtons() {
        ButtonType.values().forEach {
            setButton(it) {
                text = ""
                icon = 0
                isEnabled = true
                doNotDismiss = false
                onClick {}
            }
        }
    }

    // Prevent calling setContentView
    @Deprecated("Please use setView(view)", level = DeprecationLevel.ERROR)
    override fun setContentView(layoutResID: Int) {}
    @Deprecated("Please use setView(view)", level = DeprecationLevel.ERROR)
    override fun setContentView(view: View) {}
    @Deprecated("Please use setView(view)", level = DeprecationLevel.ERROR)
    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {}
}
