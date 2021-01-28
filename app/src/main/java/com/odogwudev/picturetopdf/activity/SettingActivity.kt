package com.odogwudev.picturetopdf.activity


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.odogwudev.picturetopdf.R
import com.odogwudev.picturetopdf.utils.BaseObject.getMarginPosition
import com.odogwudev.picturetopdf.utils.BaseObject.getPageMarginNameList
import com.odogwudev.picturetopdf.utils.BaseObject.getPageSizeNameList
import com.odogwudev.picturetopdf.utils.BaseObject.getPageSizePosition
import com.odogwudev.picturetopdf.utils.BaseObject.setMarginPosition
import com.odogwudev.picturetopdf.utils.BaseObject.setPageSizePosition


class SettingActivity : AppCompatActivity() {

    private lateinit var textPageSize: TextView
    private lateinit var textMargin: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        supportActionBar?.title = "Settings"

        textPageSize = findViewById(R.id.textPageSize)
        textMargin = findViewById(R.id.textMargin)

        textPageSize.text =
            "Page size: ${getPageSizeNameList()[getPageSizePosition(this)]}"
        textMargin.text =
            "PDF Page Margin: ${getPageMarginNameList()[getMarginPosition(this)]}"

    }

    fun listenerMethod(view: View) {
        when (view.id) {
            R.id.pageSize -> {
                showPageSizeDialog()
            }
            R.id.pageMargin -> {
                showMarginDialog()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showPageSizeDialog() {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialog.setTitle("PDF Page Size")

        alertDialog.setSingleChoiceItems(
            getPageSizeNameList(),
            getPageSizePosition(this@SettingActivity)
        ) { dialog, which ->
            dialog.dismiss()
            setPageSizePosition(this@SettingActivity, which)
            textPageSize.text = "Page size: ${getPageSizeNameList()[which]}"
//            Toast.makeText(this, itemsPageSize[which], Toast.LENGTH_LONG).show()
        }
        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(true)
        alert.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showMarginDialog() {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialog.setTitle("PDF Page Size")

        alertDialog.setSingleChoiceItems(
            getPageMarginNameList(),
            getMarginPosition(this@SettingActivity)
        ) { dialog, which ->
            dialog.dismiss()
            setMarginPosition(this@SettingActivity, which)
            textMargin.text = "PDF Page Margin: ${getPageMarginNameList()[which]}"
//            Toast.makeText(this, itemsMargin[which], Toast.LENGTH_LONG).show()
        }
        val alert: AlertDialog = alertDialog.create()
        alert.setCanceledOnTouchOutside(true)
        alert.show()
    }
}