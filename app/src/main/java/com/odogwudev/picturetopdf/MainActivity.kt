package com.odogwudev.picturetopdf

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.odogwudev.picturetopdf.activity.BaseActivity
import com.odogwudev.picturetopdf.activity.SettingActivity
import com.odogwudev.picturetopdf.utils.BaseObject.PICK_IMAGE_MULTIPLE
import com.odogwudev.picturetopdf.utils.BaseObject.getDocumentName
import com.odogwudev.picturetopdf.utils.BaseObject.getFilePath
import com.odogwudev.picturetopdf.utils.BaseObject.getMarginPosition
import com.odogwudev.picturetopdf.utils.BaseObject.getPageMarginHeight
import com.odogwudev.picturetopdf.utils.BaseObject.getPageMarginWidth
import com.odogwudev.picturetopdf.utils.BaseObject.getPageSizeList
import com.odogwudev.picturetopdf.utils.BaseObject.getPageSizePosition
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import java.io.File
import java.io.IOException

class MainActivity : BaseActivity() {

    private var isSuccess: Boolean = false

    private lateinit var imageView: ImageView
    private var myImagePath: String = ""
    private lateinit var mDocumentPath:File
    private var pathList: ArrayList<String> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)

        val covertImagesToPdf = findViewById<Button>(R.id.btn_covertImagesToPdf)
        covertImagesToPdf.setOnClickListener {
            if (pathList.isNullOrEmpty()){
                showMessage("First Select Images")
            }else{
                PdfGenerateAsyncTask(this).execute()
            }
        }

        val imagesSelectButton = findViewById<Button>(R.id.btn_selectImages)
        imagesSelectButton.setOnClickListener {
            if (checkRequestStorage()) {
                photoIntentMethod()
            } else {
                requestStoragePermission()
            }

        }

        val settingButton = findViewById<Button>(R.id.btn_setting)
        settingButton.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_MULTIPLE) {

                if (intentData!!.clipData != null) {
                    val mClipData = intentData.clipData
                    for (i in 0 until mClipData!!.itemCount) {
                        val item = mClipData.getItemAt(i)
                        val imageUri = item.uri
                        imageView.setImageURI(imageUri)
                        myImagePath = imageUri.toString()
                        Log.i("information", imageUri.toString())
                        pathList.add(getImagePath(imageUri))
                    }
                } else if (intentData.data != null) {
                    val imageUri = intentData.data
                    imageView.setImageURI(imageUri)
                    myImagePath = imageUri.toString()
                    pathList.add(getImagePath(imageUri!!))
                    Log.i("information", imageUri.toString())
                }
            }
        }

    }


    private fun getImagePath(uri: Uri): String {
        var cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        var documentId: String = cursor!!.getString(0)
        documentId = documentId.substring(documentId.lastIndexOf(":") + 1)
        cursor.close()
        cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null, MediaStore.Images.Media._ID + " = ? ", arrayOf(documentId), null
        )
        cursor?.moveToFirst()
        val path: String = cursor!!.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        cursor.close()
        return path
    }

    private fun photoIntentMethod() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            PICK_IMAGE_MULTIPLE
        )
    }

    override fun storageRequestAccepted() {
        photoIntentMethod()
    }

    override fun storageRequestDenied() {
        showMessage("Permission Denied")
    }


    fun createPdfBox() {
        val document = PDDocument()
        var contentStream: PDPageContentStream
        try {
            for (i in 0 until pathList.size) {
                // Define a content stream for adding to the PDF
                val page = PDPage(getPageSizeList(getPageSizePosition(this)))
                document.addPage(page)
                contentStream = PDPageContentStream(document, page, true, true)

                val exifInterface = ExifInterface(pathList[i])
                val orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )

                val bitmap: Bitmap = BitmapFactory.decodeFile(
                    pathList.get(i)
                )

                val rBitmap = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> bitmapRotation(bitmap, 90F)
                    ExifInterface.ORIENTATION_ROTATE_180 -> bitmapRotation(bitmap, 180F)
                    ExifInterface.ORIENTATION_ROTATE_270 -> bitmapRotation(bitmap, 270F)
                    else -> bitmapRotation(bitmap, 0F)
                }

                val mBitmap = Bitmap.createScaledBitmap(
                    rBitmap,
                    getImageWidth(rBitmap),
                    getImageHeight(rBitmap),
                    true
                )

                val resizedBitmap= bitmapMargin(mBitmap)

                val xImage = JPEGFactory.createFromImage(document, resizedBitmap, 0.75F, 72)

                contentStream.drawImage(
                    xImage,
                    (getPageSizeList(getPageSizePosition(this)).width / 2 - resizedBitmap.width / 2),
                    (getPageSizeList(getPageSizePosition(this)).height / 2 - resizedBitmap.height / 2)
                )

                contentStream.close()
            }
            Log.i("Information", "Pages: ${document.numberOfPages}")
            // Make sure that the content stream is closed:

            // Save the final pdf document to a file
            document.save(getOutputFile())
            document.close()
            isSuccess = true
        } catch (e: IOException) {
            isSuccess = false
        }
    }

    private fun getImageHeight(mBitmap: Bitmap): Int {
        val pageHeight = getPageSizeList(getPageSizePosition(this)).height
        val pageWidth = getPageSizeList(getPageSizePosition(this)).width
        val bitmapHeight = mBitmap.height
        val bitmapWidth = mBitmap.width

        val diffH: Float
        val diffW: Float

        val returnHeight: Int

        if (bitmapHeight >= pageHeight && bitmapWidth >= pageWidth) {
            diffH = bitmapHeight - pageHeight
            diffW = bitmapWidth - pageWidth

            returnHeight = if (diffH > diffW) {
                pageHeight.toInt()
            } else {
                (bitmapHeight/(bitmapWidth/pageWidth)).toInt()
            }

        } else if (bitmapHeight <= pageHeight && bitmapWidth <= pageWidth) {
            diffH = pageHeight - bitmapHeight
            diffW = pageWidth - bitmapWidth

            returnHeight = if (diffH > diffW) {
                (bitmapHeight + diffW).toInt()
            } else {
                pageHeight.toInt()
            }

        } else {
            returnHeight = pageHeight.toInt()
        }

        return returnHeight
    }

    private fun getImageWidth(mBitmap: Bitmap): Int {

        val dummyPageHeight = getPageSizeList(getPageSizePosition(this)).height
        val dummyPageWidth = getPageSizeList(getPageSizePosition(this)).width
        val dummyBitmapHeight = mBitmap.height
        val dummyBitmapWidth = mBitmap.width
        val diffH: Float
        val diffW: Float

        val returnWidth: Int

        if (dummyBitmapHeight >= dummyPageHeight && dummyBitmapWidth >= dummyPageWidth) {
            diffH = dummyBitmapHeight - dummyPageHeight
            diffW = dummyBitmapWidth - dummyPageWidth

            returnWidth = if (diffW > diffH) {
                dummyPageWidth.toInt()
            } else {
                (dummyBitmapWidth/(dummyBitmapHeight/dummyPageHeight)).toInt()
            }


        } else if (dummyBitmapHeight <= dummyPageHeight && dummyBitmapWidth <= dummyPageWidth) {
            diffH = dummyPageHeight - dummyBitmapHeight
            diffW = dummyPageWidth - dummyBitmapWidth

            returnWidth = if (diffW > diffH) {
                (dummyBitmapWidth + diffH).toInt()
            } else {
                dummyPageWidth.toInt()
            }


        } else {
            returnWidth = dummyPageWidth.toInt()
        }

        return returnWidth

    }

    private fun sharePDFFile() {
        val contentUri: Uri = FileProvider.getUriForFile(
            applicationContext,
            "com.orbital.sonic.imagestopdfconverterkotlin.provider",
            mDocumentPath
        )

        if (mDocumentPath.exists()) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.setDataAndType(contentUri, contentResolver.getType(contentUri))
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(getOutputFile()))
            startActivity(Intent.createChooser(shareIntent, "Choose an app"))
        }
    }

    private fun openPDFFile() {
        val contentUri: Uri = FileProvider.getUriForFile(
            applicationContext,
            "com.orbital.sonic.imagestopdfconverterkotlin.provider",
            mDocumentPath
        )

        if (mDocumentPath.exists()) {
            val openIntent = Intent()
            openIntent.action = Intent.ACTION_VIEW
            openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            openIntent.setDataAndType(contentUri, "application/pdf")
            openIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            startActivity(Intent.createChooser(openIntent, "Choose an app"))
        }

    }

    private fun getOutputFile(): File? {

        val root: File = getFilePath(this)

        var isFolderCreated = true
        if (!root.exists()) {
            isFolderCreated = root.mkdir()
        }
        return if (isFolderCreated) {
            val imageFileName = getDocumentName()
            mDocumentPath=  File(root, "$imageFileName.pdf")
            mDocumentPath
        } else {
            Log.i("information", "error: $root")
//            Toast.makeText(this, "Folder is not created", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun showErrorDialog() {
        val mDialog = Dialog(this)
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.setCanceledOnTouchOutside(true)
        mDialog.setContentView(R.layout.error_dialogs)

        val okBtn = mDialog.findViewById<Button>(R.id._gotIt)
        okBtn.setOnClickListener {

            mDialog.dismiss()
        }
        mDialog.show()
    }

    private fun showSuccessDialog() {
        val mDialog = Dialog(this)
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mDialog.setCanceledOnTouchOutside(true)
        mDialog.setContentView(R.layout.sucsess_dialogs)

        val shareBtn = mDialog.findViewById<Button>(R.id._sharePdf)
        shareBtn.setOnClickListener {
            mDialog.dismiss()
            sharePDFFile()
        }
        val openBtn = mDialog.findViewById<Button>(R.id._openPdf)
        openBtn.setOnClickListener {
            mDialog.dismiss()
            openPDFFile()
        }
        mDialog.show()
    }

    inner class PdfGenerateAsyncTask(context: Context) :
        AsyncTask<Void?, Void?, Void?>() {
        var context: Context? = null
        var mDialog: Dialog? = null

        init {
            this.context = context
            mDialog = Dialog(context)
            mDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            mDialog?.setCanceledOnTouchOutside(false)
            mDialog?.setContentView(R.layout.progress_bar)

        }

        override fun onPreExecute() {
            mDialog?.show()
            super.onPreExecute()
        }

        override fun doInBackground(vararg p0: Void?): Void? {
//            createPDFWithMultipleImage()
            createPdfBox()
            return null

        }

        override fun onPostExecute(result: Void?) {
            mDialog?.dismiss()
            if (isSuccess) {
                showSuccessDialog()
            } else {
                showErrorDialog()
            }
            isSuccess = false

            super.onPostExecute(result)

        }

    }

    private fun bitmapRotation(inputBitmap: Bitmap, mAngle: Float): Bitmap {

        Log.i("Information", "Rotation: $mAngle")

        val matrix = Matrix()
        matrix.postRotate(mAngle)
        return Bitmap.createBitmap(
            inputBitmap,
            0,
            0,
            inputBitmap.width,
            inputBitmap.height,
            matrix,
            true
        )

    }

    private fun bitmapMargin(inputBitmap: Bitmap): Bitmap {
        val mWidth: Int
        val mHeight: Int

        Log.i("Information", "RefineBitmapHeight: ${inputBitmap.height}")
        Log.i("Information", "RefineBitmapWidth: ${inputBitmap.width}")

        when (getMarginPosition(this)) {
            1, 2 -> {
                mWidth = (inputBitmap.width - getPageMarginWidth(getMarginPosition(this)))
                mHeight = (inputBitmap.height - getPageMarginHeight(getMarginPosition(this)))
            }
            else -> {
                mWidth = inputBitmap.width
                mHeight = inputBitmap.height
            }
        }

        Log.i("Information", "MarginBitmapHeight: $mHeight")
        Log.i("Information", "MarginBitmapWidth: $mWidth")

        return Bitmap.createScaledBitmap(
            inputBitmap,
            mWidth,
            mHeight,
            true
        )
    }

}