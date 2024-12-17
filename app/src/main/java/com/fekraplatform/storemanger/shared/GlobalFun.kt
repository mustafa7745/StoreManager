package com.fekraplatform.storemanger.shared


import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.fekraplatform.storemanger.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.serialization.encodeToString
import okhttp3.MultipartBody
import okhttp3.OkHttpClient


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

            onSuccess()
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
            .padding(top = padding),
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
    imageUrl: Uri,
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
        model = imageUrl,
//        imageLoader = imageLoader,
        contentDescription = contentDescription,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
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

fun builderForm(token:String): MultipartBody.Builder {
    val appInfoMethod = AppInfoMethod()
    return MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("sha", appInfoMethod.getAppSha())
        .addFormDataPart("appToken", token)
        .addFormDataPart("packageName", appInfoMethod.getAppPackageName())
        .addFormDataPart("deviceId", appInfoMethod.getDeviceId().toString())
        .addFormDataPart("model", Build.MODEL)
        .addFormDataPart("version", Build.VERSION.RELEASE)
}
fun builderForm2(): MultipartBody.Builder {
    val appInfoMethod = AppInfoMethod()
    return MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("sha", appInfoMethod.getAppSha())
        .addFormDataPart("packageName", appInfoMethod.getAppPackageName())
        .addFormDataPart("deviceId", appInfoMethod.getDeviceId().toString())
        .addFormDataPart("model", Build.MODEL)
        .addFormDataPart("version", Build.VERSION.RELEASE)
}

fun builderForm3(): MultipartBody.Builder {
    val appInfoMethod = AppInfoMethod()
    return MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("accessToken",AToken().getAccessToken().token)
        .addFormDataPart("deviceId", appInfoMethod.getDeviceId().toString())
}