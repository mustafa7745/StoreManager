package com.fekraplatform.storemanger.shared


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Build
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.fekraplatform.storemanger.R
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import okhttp3.MultipartBody
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
            .padding(top = padding).safeDrawingPadding(),
//        verticalArrangement = verticalArrangement,
//        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (stateController.isLoadingAUD.value) {
            Dialog(onDismissRequest = { }) {
                Box (Modifier.fillMaxSize()){
                    CustomCard2(modifierBox = Modifier, modifierCard = Modifier.align(Alignment.Center)) {
                        CircularProgressIndicator()
                    }
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
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stateController.errorRead.value)
                Button(onClick = {
                    stateController.errorRead.value = ""
                    stateController.isErrorRead.value = false
                    stateController.isLoadingRead.value = true
                    read()
                }) {
                    Text(text = stringResource(R.string.tryagian))
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
             .statusBarsPadding(),
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

private fun sharedBuilderForm(): MultipartBody.Builder {
    val serverConfig = ServerConfigStorage()

    Log.e("ffff",serverConfig.isSetAppToken().toString())
    val appToken = serverConfig.getAppToken()
    Log.e("tttt",appToken)
    val appInfoMethod = AppInfoMethod()
    return MultipartBody.Builder()
        .setType(MultipartBody.FORM)
//        .addFormDataPart("sha", appInfoMethod.getAppSha())
        .addFormDataPart("sha", appInfoMethod.getAppSha())
        .addFormDataPart("packageName", appInfoMethod.getAppPackageName())
        .addFormDataPart("deviceId", appInfoMethod.getDeviceId().toString())
        .addFormDataPart("appToken", if (serverConfig.isSetAppToken()) appToken else "101")
        .addFormDataPart("remoteConfigVersion",CustomSingleton.remoteConfig.REMOTE_CONFIG_VERSION.toString())
}

fun builderFormWithAccessToken(): MultipartBody.Builder {
    return sharedBuilderForm()
        .addFormDataPart("accessToken", AToken().getAccessToken().token)
}

fun builderForm0(): MultipartBody.Builder {
    return sharedBuilderForm()
        .addFormDataPart("model", Build.MODEL)
        .addFormDataPart("version", Build.VERSION.RELEASE)
}

fun builderForm1(token:String): MultipartBody.Builder {
    return builderForm0()
        .addFormDataPart("appToken", token)
}

fun builderForm2(): MultipartBody.Builder {
    return sharedBuilderForm()
        .addFormDataPart("accessToken",AToken().getAccessToken().token)
}
fun builderForm3(): MultipartBody.Builder {
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
fun CustomIcon2(imageVector: ImageVector, modifierIcon: Modifier = Modifier, border:Boolean=false, onClick: () -> Unit) {
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
fun CustomCard2(modifierCard: Modifier = Modifier
    .fillMaxWidth()
    .padding(8.dp)
    .border(
        1.dp, Color.Gray,
        RoundedCornerShape(12.dp)
    ),

                modifierBox: Modifier,
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
            Column {
                content()
            }

        }
    }
}
//@Composable
//fun CustomCard2(modifierCard: Modifier = Modifier
//    .fillMaxWidth()
//    .padding(8.dp)
//    .border(
//        1.dp, Color.Gray,
//        RoundedCornerShape(12.dp)
//    ),
//
//               modifierBox: Modifier ,
//               content: @Composable() (ColumnScope.() -> Unit)){
//    Card(
//        colors  = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surface
//        ),
//        modifier =  modifierCard
//    ){
//        Box (
//            modifier = modifierBox
//
//        ) {
//            Column(Modifier.fillMaxSize()) {
//                content()
//            }
//
//        }
//    }
//}

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
fun formatPrice(price: String): String {
    val doublePrice = price.toDouble()
    val symbols = DecimalFormatSymbols(Locale.ENGLISH)
    val decimalFormat = DecimalFormat("#.##", symbols) // Format to two decimal places
    return decimalFormat.format(doublePrice)
}
@Composable
fun MyHeader(onBack:()->Unit,otherSide:@Composable ()->Unit = {},content: @Composable ()->Unit){
    CustomCard2(modifierBox = Modifier) {
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

fun confirmDialog(context: Context, message:String = "", withTextField: Boolean = true, onSuccess: (data: String) -> Unit) {
        val s = android.app.AlertDialog.Builder(context)

            .setTitle(context.getString(R.string.prosessconfirm))
            if (message.isNotEmpty()){
                s.setMessage(message)
            }
            s.setNegativeButton(
                context.getString(R.string.cancel),
                { di, i ->

                }
            )

        if (withTextField) {
            val editText = EditText(context)
            editText.hint = "Enter Password App here"  // You can set a hint for the EditText
//            editText.is

            // Create a layout to add the EditText inside it
            val layout = LinearLayout(context)
            layout.orientation = LinearLayout.VERTICAL
            layout.addView(editText)
            s.setView(layout)
            s.setPositiveButton(context.getString(R.string.ok), { di, i ->
                val userInput = editText.text.toString().trim()
                Log.e("ffrrf", "yes")
                onSuccess(userInput)
                Log.e("ffrrfse", userInput)
            })
        }
    s.setPositiveButton(context.getString(R.string.ok), { di, i ->
        onSuccess("")
    })


        s.show()
    }
fun confirmDialog3(
    context: Context,
    message: String = "",
    text: String? = null,
    onSuccess: (data: String) -> Unit
) {
    val builder = android.app.AlertDialog.Builder(context)
        .setTitle(context.getString(R.string.prosessconfirm))

    if (message.isNotEmpty()) {
        builder.setMessage(message)
    }

    builder.setNegativeButton(context.getString(R.string.cancel)) { _, _ -> }

    var editText: EditText? = null

    if (text != null) {
        editText = EditText(context).apply {
            hint = text
        }
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(editText)
        }
        builder.setView(layout)
    }

    builder.setPositiveButton(context.getString(R.string.ok)) { _, _ ->
        val input = editText?.text?.toString()?.trim() ?: ""
        onSuccess(input)
    }

    builder.show()
}



fun confirmDialog2(context: Context, message: String = "", onSuccess: (data: String) -> Unit) {
    val builder = AlertDialog.Builder(context)
        .setTitle(context.getString(R.string.prosessconfirm))

    if (message.isNotEmpty()) {
        builder.setMessage(message)
    }

    val editText = EditText(context).apply {
        hint = "Enter Password App here"
    }

    val layout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(50, 20, 50, 10) // padding اختياري لتحسين الشكل
        addView(editText)
    }

    builder.setView(layout)

    builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
    }

    builder.setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
        val userInput = editText.text.toString().trim()
        onSuccess(userInput)
    }

    builder.show()
}



