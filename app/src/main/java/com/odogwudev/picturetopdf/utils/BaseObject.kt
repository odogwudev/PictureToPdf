package com.odogwudev.picturetopdf.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object BaseObject {

    private const val DIRECTORY_NAME="OrbitalSonic"
    const val STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_MULTIPLE = 2

    fun getPageSizePosition(context: Context): Int {
        val sharedPreferences =
            context.getSharedPreferences("pageSizesPositionPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("pageSizesPositionValue", 4)
    }

    fun setPageSizePosition(context: Context, mData: Int) {
        val sharedPreferences =
            context.getSharedPreferences("pageSizesPositionPrefs", Context.MODE_PRIVATE)
        val sharedPreferencesEditor = sharedPreferences.edit()
        sharedPreferencesEditor.putInt("pageSizesPositionValue", mData)
        sharedPreferencesEditor.apply()
    }

    fun getMarginPosition(context: Context): Int {
        val sharedPreferences =
            context.getSharedPreferences("marginsPositionPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("marginsPositionValue", 0)
    }

    fun setMarginPosition(context: Context, mData: Int) {
        val sharedPreferences =
            context.getSharedPreferences("marginsPositionPrefs", Context.MODE_PRIVATE)
        val sharedPreferencesEditor = sharedPreferences.edit()
        sharedPreferencesEditor.putInt("marginsPositionValue", mData)
        sharedPreferencesEditor.apply()
    }


    /**
    1" = 25.4mm = 160.0 dp
    1mm = 6.299 dp
    A3: 297mm x 420mm (11.69" x 16.54")
    A4: 210mm x 297mm (8.27" x 11.69")
    A5: 148mm x 210mm (5.83" x 8.27")
    B5: 176mm x 250mm (6.93" x 9.84")
    Letter: 8.5" x 11" (279mm x 216mm)
    Legal: 8.5" x 14" (216mm x 356mm)
    Executive:  216mm x 330mm (8.504" x 12.992")
    No Margin: 0mm
    Normal: Top & Bottom 25.4 mm, Left & Right 31.8 mm
    Narrow: Top & Bottom 12.7 mm, Left & Right 12.7 mm
     */


    fun getPageSizeNameList(): Array<String> {
        return arrayOf("A0", "A1", "A2", "A3", "A4", "A5", "A6", "Letter", "Legal")
    }

    fun getPageSizeList(position: Int): PDRectangle {

        val mList= arrayOf(
            PDRectangle.A0,
            PDRectangle.A1,
            PDRectangle.A2,
            PDRectangle.A3,
            PDRectangle.A4,
            PDRectangle.A5,
            PDRectangle.A6,
            PDRectangle.LEGAL,
            PDRectangle.LETTER
        )

        return mList[position]
    }

    fun getPageMarginNameList(): Array<String> {
        return arrayOf("No Margin", "Normal", "Narrow")
    }

    fun getPageMarginHeight(position: Int): Int {
        val mList= arrayOf(0, 100, 50)

        return mList[position]
    }

    fun getPageMarginWidth(position: Int): Int {
        val mList= arrayOf(0, 100, 50)

        return mList[position]
    }


    fun getFilePath(context: Context): File {

        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            File(context.getExternalFilesDir(null), DIRECTORY_NAME)
        } else {
            File(Environment.getExternalStorageDirectory(), DIRECTORY_NAME)
        }
    }

    fun getDocumentName():String{
        val c = Calendar.getInstance()
        Log.i("information","Doc_${c.timeInMillis}")
        return "Doc_${c.timeInMillis}"
    }

}