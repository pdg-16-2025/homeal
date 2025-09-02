package com.example.homeal_app.ui.Scan

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanFragment : Fragment() {

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val scanViewModel = ViewModelProvider(this).get(ScanViewModel::class.java)

        return ComposeView(requireContext()).apply {
            setContent {
                BarcodeScannerScreen(scanViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BarcodeScannerScreen(viewModel: ScanViewModel) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val scannedText by viewModel.scannedText.observeAsState("")
    val isScanning by viewModel.isScanning.observeAsState(true)
    val dialogState by viewModel.dialogState.observeAsState(DialogState.None)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (cameraPermissionState.status.isGranted) {
            // Camera preview takes most of the screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                CameraPreview(
                    onBarcodeScanned = { barcode ->
                        viewModel.onBarcodeScanned(barcode)
                    },
                    isScanning = isScanning
                )

                // Scanner overlay
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .align(Alignment.Center)
                        .border(
                            width = 2.dp,
                            color = if (isScanning) Color.Green else Color.Red,
                            shape = RoundedCornerShape(12.dp)
                        )
                )

                // Instructions
                Text(
                    text = "Point your camera at a barcode",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                )
            }
        } else {
            // Permission request UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Camera permission is required to scan barcodes",
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() }
                ) {
                    Text("Grant Camera Permission")
                }
            }
        }
    }

    // Handle dialogs
    when (val currentDialogState = dialogState) {
        is DialogState.Loading -> {
            LoadingDialog()
        }
        is DialogState.ProductFound -> {
            ProductFoundDialog(
                product = currentDialogState.product,
                barcode = currentDialogState.barcode,
                onConfirm = { quantity -> viewModel.onProductConfirmed(currentDialogState.product, quantity) },
                onCancel = { viewModel.closeDialog() }
            )
        }
        is DialogState.ProductNotFound -> {
            ProductNotFoundDialog(
                barcode = currentDialogState.barcode,
                onProductSelected = { product, quantity -> viewModel.onManualProductSelected(product, quantity) },
                onCancel = { viewModel.closeDialog() }
            )
        }
        DialogState.None -> {
            // No dialog
        }
    }
}

@Composable
fun LoadingDialog() {
    AlertDialog(
        onDismissRequest = { /* Can't dismiss loading */ },
        title = {
            Text("Searching...")
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("Looking up product...")
            }
        },
        confirmButton = {}
    )
}

@Composable
fun ProductFoundDialog(
    product: Product,
    barcode: String,
    onConfirm: (Int) -> Unit,
    onCancel: () -> Unit
) {
    var quantity by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text("Product Found!")
        },
        text = {
            Column {
                Text(
                    text = product.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                product.category?.let {
                    Text(
                        text = "Category: $it",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Barcode: $barcode", fontSize = 12.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity selector
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quantity:", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(16.dp))

                    // Decrease button
                    Button(
                        onClick = { if (quantity > 1) quantity-- },
                        enabled = quantity > 1
                    ) {
                        Text("-")
                    }

                    Text(
                        text = quantity.toString(),
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    // Increase button
                    Button(
                        onClick = { quantity++ }
                    ) {
                        Text("+")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(quantity) }
            ) {
                Text("Add to Owned")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProductNotFoundDialog(
    barcode: String,
    onProductSelected: (Product, Int) -> Unit,
    onCancel: () -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showProductSearch by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text("Product Not Found")
        },
        text = {
            Column {
                Text("No product found for barcode: $barcode")
                Spacer(modifier = Modifier.height(16.dp))

                if (selectedProduct == null) {
                    Text("Please select a product manually:")
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { showProductSearch = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Search Products")
                    }
                } else {
                    Text("Selected product:")
                    Text(
                        text = selectedProduct!!.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    selectedProduct!!.category?.let {
                        Text(
                            text = "Category: $it",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quantity selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quantity:", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(16.dp))

                        // Decrease button
                        Button(
                            onClick = { if (quantity > 1) quantity-- },
                            enabled = quantity > 1
                        ) {
                            Text("-")
                        }

                        Text(
                            text = quantity.toString(),
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            textAlign = TextAlign.Center
                        )

                        // Increase button
                        Button(
                            onClick = { quantity++ }
                        ) {
                            Text("+")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { selectedProduct = null }
                    ) {
                        Text("Change Product")
                    }
                }
            }
        },
        confirmButton = {
            if (selectedProduct != null) {
                Button(
                    onClick = { onProductSelected(selectedProduct!!, quantity) }
                ) {
                    Text("Add to Owned")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )

    // Product search dialog
    if (showProductSearch) {
        ProductSearchDialog(
            onProductSelected = { product ->
                selectedProduct = product
                showProductSearch = false
            },
            onCancel = { showProductSearch = false }
        )
    }
}

@Composable
fun ProductSearchDialog(
    onProductSelected: (Product) -> Unit,
    onCancel: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // TODO: Replace with actual product search from database/API
    val sampleProducts = listOf(
        Product("1", "Apples", "Fruits"),
        Product("2", "Bread", "Bakery"),
        Product("3", "Milk", "Dairy"),
        Product("4", "Chicken Breast", "Meat"),
        Product("5", "Rice", "Grains")
    )

    val filteredProducts = sampleProducts.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text("Select Product")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search products...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Product list
                filteredProducts.forEach { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = { onProductSelected(product) }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = product.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            product.category?.let {
                                Text(
                                    text = it,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                if (filteredProducts.isEmpty() && searchQuery.isNotEmpty()) {
                    Text(
                        text = "No products found",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CameraPreview(
    onBarcodeScanned: (String) -> Unit,
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = androidx.compose.ui.platform.LocalContext.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
                ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                            if (isScanning) {
                                processImageProxy(imageProxy, onBarcodeScanned)
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )
                } catch (exc: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier.fillMaxSize()
    )
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    onBarcodeScanned: (String) -> Unit
) {
    val inputImage = InputImage.fromMediaImage(
        imageProxy.image!!,
        imageProxy.imageInfo.rotationDegrees
    )

    val scanner = BarcodeScanning.getClient()
    scanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            // Only process the first barcode found
            barcodes.firstOrNull()?.rawValue?.let { value ->
                onBarcodeScanned(value)
            }
        }
        .addOnFailureListener {
            Log.e("BarcodeScanner", "Barcode scanning failed", it)
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}
