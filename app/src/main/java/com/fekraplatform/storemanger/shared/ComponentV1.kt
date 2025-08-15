package com.fekraplatform.storemanger.shared

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.fekraplatform.storemanger.R

@Composable
fun IconBackWithTitle(onBack:()->Unit ,title:String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            modifier = Modifier
                .padding(14.dp)
                .size(25.dp),
            onClick =  onBack ) {
            Box {
                Icon(
                    modifier = Modifier,
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "",
                )
            }
        }
        Text(
            title, modifier = Modifier.padding(8.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black
        )
    }
}
@Composable
fun CustomImageView(
    imageUrl: Any,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit
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
        modifier = modifier,
        contentDescription = contentDescription,
        contentScale = contentScale
    )
}

@Composable
fun MainCompose(
    stateController: StateController,
    read: () -> Unit,
    onSuccess: @Composable() (() -> Unit)
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize().background(Color.White),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (stateController.isLoadingAUD.value) {
            Dialog(onDismissRequest = { }) {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
        if (stateController.isErrorAUD.value) {
            Toast.makeText(context, stateController.errorAUD.value, Toast.LENGTH_SHORT).show()
            stateController.isErrorAUD.value = false
            stateController.errorAUD.value = ""
        }
        if (stateController.isShowMessage.value) {
            Toast.makeText(context, stateController.message.value, Toast.LENGTH_SHORT).show()
        }
        if (stateController.isSuccessRead.value) {
            if (stateController.isHaveSuccessAudMessage()) {
                Toast.makeText(context, stateController.getMessage(), Toast.LENGTH_SHORT).show()
            }

            onSuccess()
        }

        if (stateController.isLoadingRead.value || stateController.isErrorRead.value )
            Box(Modifier.fillMaxSize()) {
                if (stateController.isLoadingRead.value)
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                if (stateController.isErrorRead.value) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text(text = stateController.errorRead.value)
                        Button(onClick = {
                            stateController.errorRead.value = ""
                            stateController.isErrorRead.value = false
                            read()
                        }) {
                            Text(text = "جرب مرة اخرى")
                        }
                    }
                }
            }



    }
}

@Composable
fun MainComposeAUD(
    name:String,
    stateController: StateController,
    back:()-> Unit,
    content: @Composable() (() -> Unit)
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize().background(Color.White),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                CustomIcon3(
                    Icons.AutoMirrored.Default.ArrowBack, border = false,
                    modifierButton = Modifier
                        .padding(14.dp).size(25.dp),
                ) {
                    back()
                }
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = (name),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }
        }
        HorizontalDivider(Modifier.fillMaxWidth())
        if (stateController.isLoadingAUD.value) {
            Dialog(onDismissRequest = { }) {
                CircularProgressIndicator()
            }
        }

        if (stateController.isShowMessage.value) {
            Toast.makeText(context, stateController.message.value, Toast.LENGTH_SHORT).show()
        }

        if (stateController.isErrorAUD.value) {
            Toast.makeText(context, stateController.errorAUD.value, Toast.LENGTH_SHORT)
                .show()
            stateController.isErrorAUD.value = false
            stateController.errorAUD.value = ""
        }
        content()
    }
}

@Composable
fun MainComposeRead(
    name:String,
    stateController: StateController,
    back:()-> Unit,
    read: () -> Unit,
    onSuccess: @Composable() (() -> Unit)
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize().background(Color.White),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                CustomIcon3(
                    Icons.AutoMirrored.Default.ArrowBack, border = false,
                    modifierButton = Modifier
                        .padding(14.dp).size(25.dp),
                ) {
                    back()
                }
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = (name),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }
        }
        HorizontalDivider(Modifier.fillMaxWidth())



        if (stateController.isLoadingAUD.value) {
            Dialog(onDismissRequest = { }) {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
        if (stateController.isErrorAUD.value) {
            Toast.makeText(context, stateController.errorAUD.value, Toast.LENGTH_SHORT).show()
            stateController.isErrorAUD.value = false
            stateController.errorAUD.value = ""
        }
        if (stateController.isShowMessage.value) {
            Toast.makeText(context, stateController.message.value, Toast.LENGTH_SHORT).show()
        }
        if (stateController.isSuccessRead.value) {
            if (stateController.isHaveSuccessAudMessage()) {
                Toast.makeText(context, stateController.getMessage(), Toast.LENGTH_SHORT).show()
            }

            onSuccess()
        }

        if (stateController.isLoadingRead.value || stateController.isErrorRead.value )

            Box(Modifier.fillMaxSize()) {
                if (stateController.isLoadingRead.value)
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                if (stateController.isErrorRead.value) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text(text = stateController.errorRead.value)
                        Button(onClick = {
                            stateController.errorRead.value = ""
                            stateController.isErrorRead.value = false
                            read()
                        }) {
                            Text(text = "جرب مرة اخرى")
                        }
                    }
                }
            }

    }
}


@Composable
fun CustomIcon3(imageVector: ImageVector, modifierIcon: Modifier = Modifier, modifierButton: Modifier = Modifier, borderColor: Color = MaterialTheme.colorScheme.primary, tint: Color = LocalContentColor.current, border:Boolean=false, onClick: () -> Unit) {
    val modifier = if (border) modifierButton
        .border(
            1.dp,
            borderColor,
            CircleShape
        )
        .clip(
            CircleShape
        )
    else modifierButton
    IconButton(
        modifier = modifier,
        onClick = onClick) {
        Box {
            Icon(
                modifier = modifierIcon,
                imageVector = imageVector,
                contentDescription = "",
                tint = tint
            )
//            Text(modifier =  Modifier.align(Alignment.TopEnd) .background(MaterialTheme.colorScheme.primary, CircleShape) // خلفية دائرية للـ Badge
//            , color = Color.White, fontSize = 10.sp, text = "3")
        }

    }
}
