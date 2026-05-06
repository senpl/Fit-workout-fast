package com.easyfitness.utils

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.easyfitness.R
import com.mikhaellopez.circularimageview.BuildConfig
import com.mikhaellopez.circularimageview.CircularImageView
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

//import com.easyfitness.BuildConfig;
//import com.theartofdev.edmodo.cropper.CropImage;
//import com.theartofdev.edmodo.cropper.CropImageView;
class ImageUtil {
    private var mF: Fragment? = null
    var filePath: String? = null
        private set
    var view: ImageView? = null
    private var mDeleteImageListener: OnDeleteImageListener? = null


    constructor()

    constructor(view: ImageView?) {
        this.view = view
    }

    /**
     * Return path to the thumb of a picture if it is not already a thumb.
     * If necessary, it creates the thumb file.
     *
     * @param pPath Path to the full size picture
     * @return path to the thumb file
     */
    fun getThumbPath(pPath: String?): String? {
        if (pPath == null || pPath.isEmpty()) return null
        // extract path without the .jpg
        var nameOfOutputImage = ""
        nameOfOutputImage = pPath.substring(pPath.lastIndexOf('/') + 1, pPath.lastIndexOf('.'))
        val pathOfOutputFolder = pPath.substring(0, pPath.lastIndexOf('/'))

        // If it is already a thumb do nothing
        if (nameOfOutputImage.substring(nameOfOutputImage.length - 3) == "_TH") {
            return pPath
            // else check if it already exists
        } else {
            // extract path without the .jpg
            var pathOfThumbImage = ""
            pathOfThumbImage = pathOfOutputFolder + "/.thumb/" + nameOfOutputImage + "_TH.jpg"
            val f = File(pathOfThumbImage)
            if (!f.exists()) return saveThumb(pPath) // create thumb file
            else {
                return pathOfThumbImage
            }
        }
    }

    fun setOnDeleteImageListener(listener: OnDeleteImageListener?) {
        mDeleteImageListener = listener
    }

    fun CreatePhotoSourceDialog(pF: Fragment?): Boolean {
        mF = pF

        val optionListArray = arrayOfNulls<String>(3)
        optionListArray[0] = mF!!.getResources().getString(R.string.camera)
        optionListArray[1] = mF!!.getResources().getString(R.string.gallery)
        optionListArray[2] = "Remove Image"

        requestPermissionForWriting(pF!!)

        val itemActionBuilder = AlertDialog.Builder(mF!!.getActivity())
        itemActionBuilder.setTitle("").setItems(
            optionListArray,
            DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                val lv = (dialog as AlertDialog).getListView()
                when (which) {
                    1 -> getGaleryPict(mF!!)
                    0 -> {}
                    2 -> if (mDeleteImageListener != null) mDeleteImageListener!!.onDeleteImage(this@ImageUtil)
                    else -> {}
                }
            })
        itemActionBuilder.show()

        return true
    }

    private fun dispatchTakePictureIntent(pF: Fragment?) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(mF!!.requireActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile(pF)
                this.filePath = photoFile!!.getAbsolutePath()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                return
            }
            // Continue only if the File was successfully created
            val photoURI = FileProvider.getUriForFile(
                mF!!.requireActivity(),
                BuildConfig.LIBRARY_PACKAGE_NAME + ".provider",
                photoFile
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            mF!!.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
        }
    }

    private fun getGaleryPict(pF: Fragment) {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.setType("image/*")
        pF.startActivityForResult(photoPickerIntent, REQUEST_PICK_GALERY_PHOTO)
    }

    fun galleryAddPic(pF: Fragment, file: String) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(file)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.setData(contentUri)
        pF.requireActivity().sendBroadcast(mediaScanIntent)
    }

    @Throws(IOException::class)
    private fun createImageFile(pF: Fragment?): File? {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        var storageDir: File? = null

        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED != state) {
            return null
        } else {
            //We use the FastNFitness directory for saving our .csv file.
            storageDir = Environment.getExternalStoragePublicDirectory("/FastnFitness/DCIM/")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
        }
        //File storageDir = pF.getActivity().getExternalFilesDir(Environment.DIRECTORY_DCIM);
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = image.getAbsolutePath();
        return image
    }

    private fun requestPermissionForWriting(pF: Fragment) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                pF.requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // No explanation needed, we can request the permission.

            val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102
            ActivityCompat.requestPermissions(
                pF.requireActivity(),
                arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
            )

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    @Throws(IOException::class)
    fun moveFile(file: File, dir: File?): File {
        return copyFile(file, dir, "", true)
    }

    @Throws(IOException::class)
    fun moveFile(file: File, dir: File?, newFileName: String): File {
        return copyFile(file, dir, newFileName, true)
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun copyFile(
        file: File,
        dir: File?,
        newFileName: String = "",
        moveFile: Boolean = false
    ): File {
        var newFile: File? = null
        if (newFileName == "") newFile = File(dir, file.getName())
        else newFile = File(dir, newFileName)

        FileOutputStream(newFile).getChannel().use { outputChannel ->
            FileInputStream(file).getChannel().use { inputChannel ->
                inputChannel.transferTo(0, inputChannel.size(), outputChannel)
                inputChannel.close()
                if (moveFile) file.delete()
            }
        }
        return newFile
    }

    fun interface OnDeleteImageListener {
        fun onDeleteImage(imgUtil: ImageUtil?)
    }

    companion object {
        const val REQUEST_TAKE_PHOTO: Int = 1
        const val REQUEST_PICK_GALERY_PHOTO: Int = 2
        const val REQUEST_DELETE_IMAGE: Int = 3
        fun setThumb(mImageView: ImageView, pPath: String?) {
            try {
                if (pPath == null || pPath.isEmpty()) return
                val f = File(pPath)
                if (!f.exists() || f.isDirectory()) return

                // Get the dimensions of the View
                val targetW = 128f //mImageView.getWidth();

                //float targetH = mImageView.getHeight();

                // Get the dimensions of the bitmap
                val bmOptions = BitmapFactory.Options()
                bmOptions.inJustDecodeBounds = true
                BitmapFactory.decodeFile(pPath, bmOptions)
                val photoW = bmOptions.outWidth.toFloat()
                val photoH = bmOptions.outHeight.toFloat()

                // Determine how much to scale down the image
                val scaleFactor =
                    (photoW / targetW).toInt() //Math.min(photoW/targetW, photoH/targetH);

                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false
                bmOptions.inSampleSize = scaleFactor
                bmOptions.inPurgeable = true

                val bitmap = BitmapFactory.decodeFile(pPath, bmOptions)
                val orientedBitmap = ExifUtil.rotateBitmap(pPath, bitmap)
                mImageView.setImageBitmap(orientedBitmap)
                mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun saveThumb(pPath: String?): String? {
            if (pPath == null || pPath.isEmpty()) return null
            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true
            BitmapFactory.decodeFile(pPath, bmOptions)
            val photoW = bmOptions.outWidth.toFloat()
            val photoH = bmOptions.outHeight.toFloat()

            // Determine how much to scale down the image
            val scaleFactor = photoW / photoH //Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor.toInt()
            bmOptions.inPurgeable = true

            var ThumbImage: Bitmap? = null
            //if (photoW < photoH)
            val bitmap = BitmapFactory.decodeFile(pPath, bmOptions)
            val orientedBitmap = ExifUtil.rotateBitmap(pPath, bitmap)
            ThumbImage =
                ThumbnailUtils.extractThumbnail(orientedBitmap, 128, (128 / scaleFactor).toInt())

            //else
            //ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(pPath), (int) (96 / scaleFactor), 96);

            // extract path without the .jpg
            val nameOfOutputImage =
                pPath.substring(pPath.lastIndexOf('/') + 1, pPath.lastIndexOf('.'))
            val pathOfOutputFolder = pPath.substring(0, pPath.lastIndexOf('/'))
            val pathThumbFolder = File(pathOfOutputFolder + "/.thumb/")
            if (!pathThumbFolder.exists()) {
                pathThumbFolder.mkdirs()
            }
            val pathOfThumbImage = pathOfOutputFolder + "/.thumb/" + nameOfOutputImage + "_TH.jpg"

            try {
                val out = FileOutputStream(pathOfThumbImage)
                ThumbImage.compress(Bitmap.CompressFormat.JPEG, 80, out)
            } catch (e: Exception) {
                Log.e("Image", e.message, e)
            }

            return pathOfThumbImage
        }

        fun setPic(mImageView: CircularImageView?, pPath: String?) {
            try {
                if (pPath == null) return
                val f = File(pPath)
                if (!f.exists() || f.isDirectory()) return

                // Get the dimensions of the View
                var targetW = mImageView!!.getWidth()
                if (targetW == 0) targetW = mImageView.getMeasuredWidth()
                var targetH = mImageView!!.getHeight()
                if (targetH == 0) targetH = mImageView.getMeasuredHeight()

                // Get the dimensions of the bitmap
                val bmOptions = BitmapFactory.Options()
                bmOptions.inJustDecodeBounds = true
                BitmapFactory.decodeFile(pPath, bmOptions)
                val photoW = bmOptions.outWidth
                val photoH = bmOptions.outHeight

                // Determine how much to scale down the image
                val scaleFactor = photoW / targetW //Math.min(photoW/targetW, photoH/targetH);

                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false
                bmOptions.inSampleSize = scaleFactor
                bmOptions.inPurgeable = true

                val bitmap = BitmapFactory.decodeFile(pPath, bmOptions)
                val orientedBitmap = ExifUtil.rotateBitmap(pPath, bitmap)
                mImageView.setImageBitmap(orientedBitmap)

                //mImageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                mImageView.setAdjustViewBounds(true)
                mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
