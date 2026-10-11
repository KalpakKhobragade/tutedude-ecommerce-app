package com.example.ecommerceapp.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.ecommerceapp.data.model.FAKE_STORE_PREFIX
import com.example.ecommerceapp.data.model.Product
import com.example.ecommerceapp.data.model.toProduct
import com.example.ecommerceapp.data.remote.FakeStoreApi
import com.example.ecommerceapp.util.AppConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class ProductRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val api: FakeStoreApi,
    @ApplicationContext private val context: Context
) {

    private companion object {
        const val PRODUCTS = "products"
        const val USERS = "users"
        const val MAX_SIDE_PX = 600
        const val JPEG_QUALITY = 55
    }

    /** Live list of all products uploaded by users (newest first). */
    fun observeProducts(): Flow<List<Product>> = callbackFlow {
        val registration = firestore.collection(PRODUCTS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.also { it.id = doc.id }
                }.orEmpty()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    /** Loads one product. Ids starting with "fs_" come from the FakeStore API. */
    suspend fun getProduct(id: String): Result<Product> = runCatching {
        if (id.startsWith(FAKE_STORE_PREFIX)) {
            api.getProduct(id.removePrefix(FAKE_STORE_PREFIX).toInt()).toProduct()
        } else {
            val doc = firestore.collection(PRODUCTS).document(id).get().await()
            val product = doc.toObject(Product::class.java)
                ?: throw IllegalStateException("Product not found.")
            product.also { it.id = doc.id }
        }
    }

    /** Recommended products from https://fakestoreapi.com */
    suspend fun getRecommended(): Result<List<Product>> = runCatching {
        api.getProducts().map { it.toProduct() }
    }

    /** Uploads the images and then saves the product in Firestore. */
    suspend fun uploadProduct(
        title: String,
        description: String,
        price: Double,
        category: String,
        imageUris: List<Uri>
    ): Result<Unit> = runCatching {
        val user = auth.currentUser
            ?: throw IllegalStateException("You must be logged in to upload a product.")

        val profile = runCatching {
            firestore.collection(USERS).document(user.uid).get().await()
        }.getOrNull()
        val sellerName = profile?.getString("name") ?: user.displayName ?: "Unknown seller"
        val phone = profile?.getString("phone").orEmpty()
        val contact = listOf(user.email.orEmpty(), phone)
            .filter { it.isNotBlank() }
            .joinToString("  |  ")

        val docRef = firestore.collection(PRODUCTS).document()

        val imageUrls: List<String> = if (AppConfig.USE_FIREBASE_STORAGE) {
            imageUris.mapIndexed { index, uri ->
                val ref = storage.reference.child("products/${docRef.id}/image_$index.jpg")
                ref.putFile(uri).await()
                ref.downloadUrl.await().toString()
            }
        } else {
            withContext(Dispatchers.IO) {
                imageUris.map { uri -> compressToDataUri(uri) }
            }
        }

        val product = Product(
            id = docRef.id,
            title = title,
            description = description,
            price = price,
            category = category,
            imageUrls = imageUrls,
            uploaderId = user.uid,
            uploaderName = sellerName,
            uploaderContact = contact,
            timestamp = System.currentTimeMillis()
        )
        docRef.set(product).await()
        Unit
    }

    /** Shrinks an image and returns it as a "data:image/jpeg;base64,..." string. */
    private fun compressToDataUri(uri: Uri): String {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

        var sampleSize = 1
        while (bounds.outWidth / sampleSize > MAX_SIDE_PX * 2 ||
            bounds.outHeight / sampleSize > MAX_SIDE_PX * 2
        ) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: throw IllegalStateException("Could not read the selected image.")

        val longestSide = max(decoded.width, decoded.height)
        val scaled = if (longestSide > MAX_SIDE_PX) {
            val ratio = MAX_SIDE_PX.toFloat() / longestSide
            Bitmap.createScaledBitmap(
                decoded,
                (decoded.width * ratio).toInt().coerceAtLeast(1),
                (decoded.height * ratio).toInt().coerceAtLeast(1),
                true
            )
        } else {
            decoded
        }

        val output = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        val base64 = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
        return "data:image/jpeg;base64,$base64"
    }
}
