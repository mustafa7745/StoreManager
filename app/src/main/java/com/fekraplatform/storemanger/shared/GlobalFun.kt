package com.fekraplatform.storemanger.shared


import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Message
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.fekraplatform.storemanger.R
import com.fekraplatform.storemanger.application.MyApplication
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.serialization.encodeToString
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


@Composable
fun MainCompose1(
    padding: Dp,
    stateController: StateController,
    activity: Activity,
    read: () -> Unit,
    onSuccess: @Composable() (() -> Unit)
) {
    var verticalArrangement: Arrangement.Vertical by remember { mutableStateOf(Arrangement.Center) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = padding),
//        verticalArrangement = verticalArrangement,
//        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (stateController.isLoadingAUD.value) {
            Dialog(onDismissRequest = { }) {
                Box (Modifier.fillMaxSize()){
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
        if (stateController.isErrorAUD.value) {
            Toast.makeText(activity, stateController.errorAUD.value, Toast.LENGTH_SHORT).show()
            stateController.isErrorAUD.value = false
            stateController.errorAUD.value = ""
        }
        if (stateController.isSuccessRead.value) {
            verticalArrangement = Arrangement.Top
            if (stateController.isHaveSuccessAudMessage()){
                Toast.makeText(activity, stateController.getMessage(), Toast.LENGTH_SHORT).show()
            }

            Column(Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
                ) {
                onSuccess()
            }

        }
        if (stateController.isShowMessage.value) {
            Toast.makeText(activity, stateController.message.value, Toast.LENGTH_SHORT).show()
        }
        if (stateController.isLoadingRead.value) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))


//            LoadingCompose()
        }
        if (stateController.isErrorRead.value) {
            Column(Modifier.align(Alignment.Center)) {
                Text(text = stateController.errorRead.value)
                Button(onClick = {
                    stateController.errorRead.value = ""
                    stateController.isErrorRead.value = false
                    stateController.isLoadingRead.value = true
                    read()
                }) {
                    Text(text = "جرب مرة اخرى")
                }
            }


        }
    }
}

@Composable
fun MainCompose2(
    padding: Dp,
    stateController: StateController,
    activity: Activity,
    content: @Composable() (() -> Unit)
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = padding)
            .safeDrawingPadding(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (stateController.isLoadingAUD.value) {
            Dialog(onDismissRequest = { }) {
                CircularProgressIndicator()
            }
        }
//        if (stateController.i())
        if (stateController.isErrorAUD.value) {
            Toast.makeText(activity, stateController.errorAUD.value, Toast.LENGTH_SHORT).show()
            stateController.isErrorAUD.value = false
            stateController.errorAUD.value = ""
        }
        if (stateController.isShowMessage.value) {
            Toast.makeText(activity, stateController.message.value, Toast.LENGTH_SHORT).show()
        }

        content()
    }
}

@Composable
fun CustomImageView(
    context: Context,
    imageUrl: String,
    okHttpClient: OkHttpClient,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    // Create ImageLoader with OkHttpClient
//    val imageLoader = ImageLoader.Builder(context)
//        .okHttpClient(okHttpClient)
//        .build()

    // Display the image using AsyncImage
    SubcomposeAsyncImage(
        error = {
            Column(
                Modifier,
//                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = R.drawable.logo,
                    contentDescription = null,
                    contentScale = ContentScale.Fit

                )
            }

        },
        loading = {
            CircularProgressIndicator()
        },
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .build(),
//        imageLoader = imageLoader,
        contentDescription = contentDescription,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}
@Composable
fun CustomImageViewUri(
    imageUrl: Any,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale:ContentScale = ContentScale.Fit
) {
    // Create ImageLoader with OkHttpClient
//    val imageLoader = ImageLoader.Builder(context)
//        .okHttpClient(okHttpClient)
//        .build()

    // Display the image using AsyncImage
    SubcomposeAsyncImage(
        error = {
            Column(
                Modifier,
//                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = R.drawable.logo,
                    contentDescription = null,
                    contentScale = contentScale

                )
            }

        },
        loading = {
            CircularProgressIndicator()
        },
        model = imageUrl,
//        imageLoader = imageLoader,
        contentDescription = contentDescription,
        modifier = modifier.fillMaxSize(),
        contentScale = contentScale
    )
}
@Composable
fun CustomImageView1(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    isLoading:Boolean = true
) {
    // Create ImageLoader with OkHttpClient
//    val imageLoader = ImageLoader.Builder(context)
//        .okHttpClient(okHttpClient)
//        .build()

    // Display the image using AsyncImage
    SubcomposeAsyncImage(
        error = {
            Column(
                Modifier,
//                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = R.drawable.logo,
                    contentDescription = null,
                    contentScale = contentScale

                )
            }

        },
        loading = {
            if (isLoading)
                CircularProgressIndicator()
        },
        model = imageUrl,
//        imageLoader = imageLoader,
        contentDescription = contentDescription,
        modifier = modifier.fillMaxSize(),
        contentScale = contentScale
    )
}

fun getRemoteConfig(): FirebaseRemoteConfig {
    val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
    val configSettings = remoteConfigSettings {
        minimumFetchIntervalInSeconds = 3600
    }
    remoteConfig.setConfigSettingsAsync(configSettings)
    return remoteConfig;
}
fun initVarConfig(serverConfig: ServerConfig,onFail:()->Unit,onSuccess: () -> Unit) {
    val remoteConfig = getRemoteConfig()
    remoteConfig.fetchAndActivate()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val BASE_URL = remoteConfig.getString("BASE_URL")
                val BASE_IMAGE_URL = remoteConfig.getString("BASE_IMAGE_URL")
                val SUB_FOLDER_PRODUCT = remoteConfig.getString("SUB_FOLDER_PRODUCT")
                val TYPE = remoteConfig.getString("TYPE_STORE_MANAGER")
                val SUB_FOLDER_STORE_LOGOS = remoteConfig.getString("SUB_FOLDER_STORE_LOGOS")
                    val SUB_FOLDER_STORE_COVERS = remoteConfig.getString("SUB_FOLDER_STORE_COVERS")
                val varRemoteConfig = VarRemoteConfig(
                    BASE_URL = BASE_URL,
                    BASE_IMAGE_URL = BASE_IMAGE_URL,
                    SUB_FOLDER_PRODUCT = SUB_FOLDER_PRODUCT, TYPE = TYPE,
                    SUB_FOLDER_STORE_LOGOS = SUB_FOLDER_STORE_LOGOS,
                    SUB_FOLDER_STORE_COVERS = SUB_FOLDER_STORE_COVERS
                )
                serverConfig.setRemoteConfig(MyJson.IgnoreUnknownKeys.encodeToString(varRemoteConfig))
                onSuccess()
//                stateController.successStateAUD()
            } else {
//                stateController.errorStateAUD("frc")
                onFail()
                Log.e("RemoteConfig", "Failed to fetch remote config", task.exception)
            }
        }
}

private fun sharedBuilderForm(): MultipartBody.Builder {
    val appInfoMethod = AppInfoMethod()
    return MultipartBody.Builder()
        .setType(MultipartBody.FORM)
//        .addFormDataPart("sha", appInfoMethod.getAppSha())
        .addFormDataPart("sha", appInfoMethod.getAppSha())
        .addFormDataPart("packageName", appInfoMethod.getAppPackageName())
        .addFormDataPart("deviceId", appInfoMethod.getDeviceId().toString())
}

fun builderForm(token:String): MultipartBody.Builder {
    return sharedBuilderForm()
        .addFormDataPart("appToken", token)
        .addFormDataPart("model", Build.MODEL)
        .addFormDataPart("version", Build.VERSION.RELEASE)
}

fun builderForm2(): MultipartBody.Builder {
    val appInfoMethod = AppInfoMethod()
    return MultipartBody.Builder()
        .setType(MultipartBody.FORM)
//        .addFormDataPart("sha","11:AA:07:80:6F:35:8B:F1:03:44:F9:5F:4F:89:02:5E:F2:9B:4C:65:AE:9F:88:B6:42:AE:64:84:C8:A6:3C:0C")
        .addFormDataPart("sha", appInfoMethod.getAppSha())
        .addFormDataPart("packageName", appInfoMethod.getAppPackageName())
        .addFormDataPart("deviceId", appInfoMethod.getDeviceId().toString())
        .addFormDataPart("model", Build.MODEL)
        .addFormDataPart("version", Build.VERSION.RELEASE)
}

fun builderForm3(): MultipartBody.Builder {
    return sharedBuilderForm()
        .addFormDataPart("accessToken",AToken().getAccessToken().token)
}
fun builderForm4(): MultipartBody.Builder {
    return sharedBuilderForm()
        .addFormDataPart("storeId",CustomSingleton.selectedStore!!.id.toString())
        .addFormDataPart("accessToken",AToken().getAccessToken().token)
}

@Composable
fun IconDelete(ids: List<Int> , onClick: () -> Unit) {

    if (ids.isNotEmpty()) {
        IconButton(onClick = onClick) {
            Icon(
                modifier =
                Modifier
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(
                            16.dp
                        )
                    )
                    .clip(
                        RoundedCornerShape(
                            16.dp
                        )
                    ),
                imageVector = Icons.Outlined.Delete,
                contentDescription = ""
            )
        }
    }
}


@Composable
fun CustomIcon(imageVector: ImageVector, border:Boolean=false , tint :Color = Color.Black , onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        val modifier = if (border) Modifier
            .padding(8.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary,
                RoundedCornerShape(
                    16.dp
                )
            )
            .clip(
                RoundedCornerShape(
                    16.dp
                )
            )else Modifier
        Icon(
            modifier = modifier,
            imageVector = imageVector,
            contentDescription = "",
            tint = tint
        )
    }
}
@Composable
fun CustomIcon2(imageVector: ImageVector,
               modifierIcon: Modifier = Modifier,
               border:Boolean=false,
               onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        val modifier = if (border) Modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary,
                RoundedCornerShape(
                    16.dp
                )
            )
            .clip(
                RoundedCornerShape(
                    16.dp
                )
            )
        else Modifier
        Icon(
            modifier = modifierIcon,
            imageVector = imageVector,
            contentDescription = ""
        )
    }
}

@Composable
fun CustomCard(modifierCard: Modifier = Modifier
    .fillMaxWidth()
    .padding(8.dp)
    .border(
        1.dp, Color.Gray,
        RoundedCornerShape(12.dp)
    ),

               modifierBox: Modifier ,
               content: @Composable() (BoxScope.() -> Unit)){
    Card(
        colors  = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier =  modifierCard
    ){
        Box (
            modifier = modifierBox

        ) {
            content()
        }
    }
}
@Composable
fun CustomCard2(modifierCard: Modifier = Modifier
    .fillMaxWidth()
    .padding(8.dp)
    .border(
        1.dp, Color.Gray,
        RoundedCornerShape(12.dp)
    ),

               modifierBox: Modifier ,
               content: @Composable() (ColumnScope.() -> Unit)){
    Card(
        colors  = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier =  modifierCard
    ){
        Box (
            modifier = modifierBox

        ) {
            Column(Modifier.fillMaxSize()) {
                content()
            }

        }
    }
}

@Composable
fun CustomRow(content: @Composable() (RowScope.() -> Unit)){
    Row  (
        Modifier
            .fillMaxWidth()
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
        ){

        content()
    }
}
@Composable
fun CustomRow2(content: @Composable() (RowScope.() -> Unit)){
    Row  (
        Modifier
            .fillMaxWidth()
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ){
        content()
    }
}

fun convertStringToLatLng(coordinates: String): LatLng? {
    val parts = coordinates.split(",")

    // Ensure the string has exactly two parts (latitude and longitude)
    if (parts.size == 2) {
        val latitude = parts[0].toDoubleOrNull()
        val longitude = parts[1].toDoubleOrNull()

        if (latitude != null && longitude != null) {
            return LatLng(latitude, longitude)
        }
    }

    // Return null if the conversion fails
    return null
}

fun MyToast(context: Activity,message: String) {

    context.runOnUiThread {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

fun formatPrice(price: String): String {
    val doublePrice = price.toDouble()
    val symbols = DecimalFormatSymbols(Locale.ENGLISH)
    val decimalFormat = DecimalFormat("#.##", symbols) // Format to two decimal places
    return decimalFormat.format(doublePrice)
}

@Composable
fun MyHeader(onBack:()->Unit,otherSide:@Composable ()->Unit = {},content: @Composable ()->Unit){
    CustomCard(modifierBox = Modifier) {
        CustomRow {

            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomIcon(Icons.AutoMirrored.Default.ArrowBack, border = true) {
                onBack()
            }
               content()
            }
            otherSide()

        }
    }
}

@Composable
fun ConfirmationDialog(onDismiss:()->Unit,onConfirm: () -> Unit) {
    // Step 1: State to show/hide the dialog
    var showDialog by remember { mutableStateOf(false) }

    // Step 2: Handle the actions when buttons are clicked
//    val onConfirm = {
//        showDialog = false
//        // Handle your "Yes" action here
//    }

    val onDismiss = {
        showDialog = false
        // Handle your "No" action here
    }

    // Step 3: Button to show the dialog
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
    ) {
        Button(
            onClick = { showDialog = true },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Show Confirmation Dialog")
        }

        // Step 4: Confirmation Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },  // Handle dismiss by tapping outside
                title = { Text("Confirm Action") },
                text = { Text("Are you sure you want to proceed?") },
                confirmButton = {
                    Button(
                        onClick = onConfirm
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
//                            ConfirmationDialog(onDismiss:()->Unit,onConfirm: () -> Unit)
                        }
                    ) {
                        Text("No")
                    }
                }
            )
        }
    }
}
    fun ConfirmDialog(
        context: Context,
        withTextField: Boolean = true,
        onSuccess: (data: String) -> Unit,
    ) {
        val s = android.app.AlertDialog.Builder(context)

            .setTitle("Hello")
            .setMessage("Message")

            .setNegativeButton("Cencel",
                { di, i ->
                    Log.e("ffrrf", "yes")
                }
            )
        val editText = EditText(context)
        if (withTextField) {

            editText.hint = "Enter text here"  // You can set a hint for the EditText

            // Create a layout to add the EditText inside it
            val layout = LinearLayout(context)
            layout.orientation = LinearLayout.VERTICAL
            layout.addView(editText)
            s.setView(layout)
        }
        s.setPositiveButton("Ok", { di, i ->
            val userInput = editText.text.toString().trim()
            Log.e("ffrrf", "yes")
            onSuccess(userInput)
            Log.e("ffrrfse", userInput)
        })

        s.show()

    }

