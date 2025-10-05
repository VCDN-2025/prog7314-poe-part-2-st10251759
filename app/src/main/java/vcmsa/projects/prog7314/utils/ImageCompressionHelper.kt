package vcmsa.projects.prog7314.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageCompressionHelper {
    private const val TAG = "ImageCompression"
    private const val MAX_WIDTH = 400
    private const val MAX_HEIGHT = 400
    private const val COMPRESSION_QUALITY = 80
    private const val MAX_SIZE_BYTES = 900_000 // Keep under 900KB to be safe (Firestore limit is 1MB)

    /**
     * Compress image URI to Base64 string
     */
    fun compressImageToBase64(context: Context, imageUri: Uri): Result<String> {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                return Result.failure(Exception("Cannot open image"))
            }

            // Decode bitmap
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) {
                return Result.failure(Exception("Cannot decode image"))
            }

            // Get orientation
            val orientation = getOrientation(context, imageUri)

            // Rotate if needed
            val rotatedBitmap = rotateBitmap(originalBitmap, orientation)

            // Resize bitmap
            val resizedBitmap = resizeBitmap(rotatedBitmap, MAX_WIDTH, MAX_HEIGHT)

            // Compress to Base64
            val base64String = bitmapToBase64(resizedBitmap, COMPRESSION_QUALITY)

            // Check size
            val estimatedSize = (base64String.length * 3) / 4 // Rough Base64 size estimation
            if (estimatedSize > MAX_SIZE_BYTES) {
                // Try with lower quality
                val lowerQualityBase64 = bitmapToBase64(resizedBitmap, 60)
                val lowerSize = (lowerQualityBase64.length * 3) / 4

                if (lowerSize > MAX_SIZE_BYTES) {
                    return Result.failure(Exception("Image too large even after compression"))
                }

                Log.d(TAG, "Compressed to lower quality. Size: ${lowerSize / 1024}KB")
                Result.success(lowerQualityBase64)
            } else {
                Log.d(TAG, "Compressed successfully. Size: ${estimatedSize / 1024}KB")
                Result.success(base64String)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image", e)
            Result.failure(e)
        }
    }

    /**
     * Convert Base64 string to Bitmap
     */
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding Base64 to Bitmap", e)
            null
        }
    }

    /**
     * Convert Bitmap to Base64 string
     */
    private fun bitmapToBase64(bitmap: Bitmap, quality: Int): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /**
     * Resize bitmap maintaining aspect ratio
     */
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val aspectRatio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxWidth
            newHeight = (maxWidth / aspectRatio).toInt()
        } else {
            newHeight = maxHeight
            newWidth = (maxHeight * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Get image orientation from EXIF data
     */
    private fun getOrientation(context: Context, imageUri: Uri): Int {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val exif = ExifInterface(inputStream!!)
            inputStream.close()

            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting orientation", e)
            0
        }
    }

    /**
     * Rotate bitmap by degrees
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap

        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}