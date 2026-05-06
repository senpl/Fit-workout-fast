package com.easyfitness.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.IOException
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException

internal object ExifUtil {
    fun rotateBitmap(src: String?, bitmap: Bitmap): Bitmap {
        try {
            val orientation = getExifOrientation(src)

            if (orientation == 1) {
                return bitmap
            }

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                    matrix.setRotate(180f)
                    matrix.postScale(-1f, 1f)
                }

                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.setRotate(90f)
                    matrix.postScale(-1f, 1f)
                }

                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.setRotate(-90f)
                    matrix.postScale(-1f, 1f)
                }

                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
                else -> return bitmap
            }

            try {
                val oriented = Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    matrix,
                    true
                )
                bitmap.recycle()
                return oriented
            } catch (e: OutOfMemoryError) {
                e.printStackTrace()
                return bitmap
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bitmap
    }

    @Throws(IOException::class)
    private fun getExifOrientation(src: String?): Int {
        var orientation = 1

        try {
            /**
             * if your are targeting only api level >= 5
             * ExifInterface exif = new ExifInterface(src);
             * orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
             */
            val exifClass = Class.forName("android.media.ExifInterface")
            val exifConstructor: Constructor<*> = exifClass.getConstructor(String::class.java)
            val exifInstance: Any = exifConstructor.newInstance(src)
            val getAttributeInt = exifClass.getMethod(
                "getAttributeInt",
                String::class.java,
                Int::class.javaPrimitiveType
            )
            val tagOrientationField = exifClass.getField("TAG_ORIENTATION")
            val tagOrientation = tagOrientationField.get(null) as String?
            orientation = (getAttributeInt.invoke(
                exifInstance,
                *kotlin.arrayOf<Any?>(tagOrientation, 1)
            ) as Int?)!!
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        }

        return orientation
    }
}

