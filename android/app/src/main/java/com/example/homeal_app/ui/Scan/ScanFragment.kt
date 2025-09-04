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
import com.example.homeal_app.data.remote.NetworkModule
import com.example.homeal_app.ui.components.IngredientSearchBar
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.homeal_app.model.Ingredient

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
        is DialogState.Success -> {
            SuccessDialog(
                message = currentDialogState.message,
                onDismiss = { viewModel.closeDialog() }
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
    var selectedIngredient by remember { mutableStateOf<Ingredient?>(null) }
    var quantity by remember { mutableStateOf("1") }
    
    // Search state for IngredientSearchBar
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Ingredient>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Search function using server
    val searchProducts = { query: String ->
        if (query.isNotBlank()) {
            isLoading = true
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                try {
                    val results = NetworkModule.apiService.searchIngredients(search = query, limit = 10)
                    searchResults = results
                } catch (e: Exception) {
                    searchResults = emptyList()
                } finally {
                    isLoading = false
                }
            }
        } else {
            searchResults = emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Column {
                Text("Product Not Found")
                Text(
                    text = "Barcode: $barcode",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 400.dp)
            ) {
                Text(
                    text = "Search manually or add custom product:",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Use the reusable IngredientSearchBar
                IngredientSearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query ->
                        searchQuery = query
                        searchProducts(query)
                    },
                    suggestions = searchResults,
                    onSuggestionClick = { ingredient ->
                        selectedIngredient = ingredient
                        searchQuery = ingredient.name
                        searchResults = emptyList()
                    },
                    onAddIngredient = { name ->
                        // Create custom ingredient
                        val customIngredient = Ingredient(
                            id = 0, // Custom ingredient ID
                            name = name
                        )
                        selectedIngredient = customIngredient
                        searchQuery = name
                    },
                    placeholder = "Search or type product name...",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Show loading indicator if searching
                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Searching...", fontSize = 12.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Show selected ingredient
                selectedIngredient?.let { ingredient ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Selected: ${ingredient.name}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Quantity input
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { newQuantity ->
                                    if (newQuantity.toIntOrNull() != null || newQuantity.isEmpty()) {
                                        quantity = newQuantity
                                    }
                                },
                                label = { Text("Quantity") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedIngredient?.let { ingredient ->
                        val quantityInt = quantity.toIntOrNull() ?: 1
                        val product = Product.fromIngredient(ingredient)
                        onProductSelected(product, quantityInt)
                    }
                },
                enabled = selectedIngredient != null && quantity.toIntOrNull() != null
            ) {
                Text("Add to Fridge")
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

@Composable
fun SuccessDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Success!")
        },
        text = {
            Text(
                text = message,
                fontSize = 16.sp
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Continue Scanning")
            }
        }
    )
}
