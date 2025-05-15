package com.lulakssoft.mygroceries.database.product

import android.util.Log
import com.lulakssoft.mygroceries.dataservice.FirestoreManager
import java.time.LocalDate

class ProductRepository(
    val productDao: ProductDao,
) {
    private val firestoreManager = FirestoreManager()

    suspend fun insertProduct(product: Product) {
        Log.d("ProductRepository", "Called insertProduct with product: ${product.productName}")
        productDao.insertProduct(product)

        val syncedProduct = firestoreManager.addProductToHousehold(product.firestoreId, product)

        Log.d("ProductRepository", "Sync result: $syncedProduct")
        if (syncedProduct) {
            productDao.updateProduct(
                product.productUuid,
                product.productName,
                product.productBrand,
                product.productBarcode,
                product.productQuantity,
                product.productBestBeforeDate,
                product.productEntryDate,
                product.productImageUrl,
                true,
            )
            Log.d("ProductRepository", "Product synced with Firestore: ${product.productName}")
        } else {
            Log.d("ProductRepository", "Failed to sync product with Firestore: ${product.productName}")
        }
    }

    suspend fun insertOrUpdateProduct(product: Product) {
        val existingProduct = productDao.selectProductByUuid(product.productUuid)
        Log.d("ProductRepository", "Recieved local product: ${existingProduct?.productUuid}")
        if (existingProduct == null) {
            productDao.insertProduct(product)
            Log.d("ProductRepository", "Inserted new product to local database: ${product.productName}")
        } else {
            var productChanged = false
            for (field in product.javaClass.declaredFields) {
                if (field.name == "id") continue
                if (field.name == "productImage") continue
                field.isAccessible = true
                val newValue = field.get(product)
                val existingValue = field.get(existingProduct)

                if (newValue != existingValue) {
                    productChanged = true
                    Log.d(
                        "ProductRepository",
                        "Field: ${field.name}, New Value: $newValue, Existing Value: $existingValue",
                    )
                }
            }
            if (!productChanged) {
                Log.d("ProductRepository", "No changes detected for product: ${product.productName}")
                return
            }
            productDao.updateProduct(
                existingProduct.productUuid,
                product.productName,
                product.productBrand,
                product.productBarcode,
                product.productQuantity,
                product.productBestBeforeDate,
                product.productEntryDate,
                product.productImageUrl,
                product.isSynced,
            )

            Log.d("ProductRepository", "Updated existing product in local database: ${product.productName}")
        }
    }

    suspend fun getProductsByHouseholdId(householdId: Int) = productDao.selectProductsByHouseholdId(householdId)

    suspend fun getProductsByFirestoreId(firestoreId: String) = productDao.selectProductsByFirestoreId(firestoreId)

    suspend fun deleteProduct(product: Product) {
        productDao.delete(product)
        firestoreManager.deleteProductFromHousehold(product)
    }

    suspend fun deleteAll() {
        productDao.deleteAll()
    }

    suspend fun getExpiringProducts(daysThreshold: Int): List<Product> {
        val today = LocalDate.now()
        val thresholdDate = today.plusDays(daysThreshold.toLong())
        return productDao.getProductsExpiringBefore(thresholdDate)
    }
}
