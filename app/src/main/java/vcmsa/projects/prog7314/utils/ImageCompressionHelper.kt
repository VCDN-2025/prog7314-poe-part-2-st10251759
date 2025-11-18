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

/**
 * Utility object for compressing and converting images.
 * Handles image compression, resizing, rotation, and Base64 encoding.
 * Ensures images stay under Firestore's 1MB size limit for storage.
 */
object ImageCompressionHelper {
    private const val TAG = "ImageCompression"

    // Maximum dimensions for resized images (maintains aspect ratio)
    private const val MAX_WIDTH = 400
    private const val MAX_HEIGHT = 400

    // JPEG compression quality (0-100, where 100 is best quality)
    private const val COMPRESSION_QUALITY = 80

    // Maximum allowed size in bytes (set below Firestore's 1MB limit for safety)
    private const val MAX_SIZE_BYTES = 900_000

    /**
     * Compresses an image from a URI to a Base64 encoded string.
     * Performs the following steps:
     * 1. Loads the image from the URI
     * 2. Corrects orientation based on EXIF data
     * 3. Resizes to fit within MAX_WIDTH and MAX_HEIGHT
     * 4. Compresses to JPEG format
     * 5. Converts to Base64 string
     * 6. Verifies size is under limit, reduces quality if needed
     *
     * Returns a Result containing the Base64 string on success, or an error on failure.
     */
    fun compressImageToBase64(context: Context, imageUri: Uri): Result<String> {
        return try {
            // Open the image file from the URI
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                return Result.failure(Exception("Cannot open image"))
            }

            // Convert the input stream to a Bitmap object
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) {
                return Result.failure(Exception("Cannot decode image"))
            }

            // Check EXIF data to see if image needs rotation
            val orientation = getOrientation(context, imageUri)

            // Apply rotation if the image was taken in portrait or upside down
            val rotatedBitmap = rotateBitmap(originalBitmap, orientation)

            // Scale down the image to fit within maximum dimensions
            val resizedBitmap = resizeBitmap(rotatedBitmap, MAX_WIDTH, MAX_HEIGHT)

            // Convert bitmap to Base64 string with standard quality
            val base64String = bitmapToBase64(resizedBitmap, COMPRESSION_QUALITY)

            // Calculate approximate size of the encoded image
            val estimatedSize = (base64String.length * 3) / 4

            if (estimatedSize > MAX_SIZE_BYTES) {
                // Image is still too large, try with reduced quality
                val lowerQualityBase64 = bitmapToBase64(resizedBitmap, 60)
                val lowerSize = (lowerQualityBase64.length * 3) / 4

                if (lowerSize > MAX_SIZE_BYTES) {
                    // Even with lower quality, image is too large
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
     * Converts a Base64 encoded string back to a Bitmap image.
     * Used for displaying images that were previously compressed and stored.
     * Returns null if the string cannot be decoded.
     */
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            // Decode the Base64 string to a byte array
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)

            // Convert byte array back to Bitmap
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding Base64 to Bitmap", e)
            null
        }
    }

    /**
     * Converts a Bitmap to a Base64 encoded string.
     * Compresses as JPEG with the specified quality level.
     * Quality ranges from 0 (smallest file, worst quality) to 100 (largest file, best quality).
     */
    private fun bitmapToBase64(bitmap: Bitmap, quality: Int): String {
        val byteArrayOutputStream = ByteArrayOutputStream()

        // Compress bitmap to JPEG format with specified quality
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        // Encode the byte array to Base64 string
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /**
     * Resizes a bitmap to fit within the specified maximum dimensions.
     * Maintains the original aspect ratio to prevent distortion.
     * If the image is already smaller than the max dimensions, returns it unchanged.
     */
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // If image is already small enough, no need to resize
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        // Calculate aspect ratio to maintain proportions
        val aspectRatio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        // Determine new dimensions based on orientation
        if (width > height) {
            // Landscape orientation - constrain by width
            newWidth = maxWidth
            newHeight = (maxWidth / aspectRatio).toInt()
        } else {
            // Portrait orientation - constrain by height
            newHeight = maxHeight
            newWidth = (maxHeight * aspectRatio).toInt()
        }

        // Create and return the resized bitmap
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Reads the EXIF orientation data from an image.
     * Many cameras and phones store rotation information in EXIF metadata.
     * Returns the rotation angle in degrees (0, 90, 180, or 270).
     */
    private fun getOrientation(context: Context, imageUri: Uri): Int {
        return try {
            // Open the image to read its EXIF data
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val exif = ExifInterface(inputStream!!)
            inputStream.close()

            // Convert EXIF orientation flag to rotation degrees
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0  // No rotation needed
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting orientation", e)
            0  // Default to no rotation on error
        }
    }

    /**
     * Rotates a bitmap by the specified number of degrees.
     * Used to correct images that were taken in portrait or upside down.
     * If degrees is 0, returns the original bitmap unchanged.
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap

        // Create a transformation matrix for rotation
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())

        // Apply the rotation and return the new bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}