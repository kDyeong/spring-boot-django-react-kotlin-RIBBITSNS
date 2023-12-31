package com.hippoddung.ribbit.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.hippoddung.ribbit.R
import com.hippoddung.ribbit.ui.RibbitScreen
import com.hippoddung.ribbit.ui.screens.textfielditems.InputTextField
import com.hippoddung.ribbit.ui.screens.statescreens.ErrorScreen
import com.hippoddung.ribbit.ui.screens.statescreens.LoadingScreen
import com.hippoddung.ribbit.ui.viewmodel.CommuIdUiState
import com.hippoddung.ribbit.ui.viewmodel.CommuViewModel
import com.hippoddung.ribbit.ui.viewmodel.CreatingPostUiState
import com.hippoddung.ribbit.ui.viewmodel.GetCardViewModel
import com.hippoddung.ribbit.ui.viewmodel.PostingViewModel
import java.io.File

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun CreatingPostScreen(
    getCardViewModel: GetCardViewModel,
    postingViewModel: PostingViewModel,
    commuViewModel: CommuViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    when (postingViewModel.creatingPostUiState) {
        is CreatingPostUiState.Ready -> {
            Log.d("HippoLog, CreatingPostScreen", "Ready")
            InputPostScreen(
                navController = navController,
                postingViewModel = postingViewModel,
                commuViewModel = commuViewModel,
//                cardViewModel = cardViewModel,
                modifier = modifier
            )
        }

        is CreatingPostUiState.Success -> {
            if(postingViewModel.currentScreenState.value != RibbitScreen.CommuIdScreen) {   // CommuIdScreen 이 아닐 경우
                Log.d("HippoLog, CreatingPostScreen", "Success to HomeScreen")
                getCardViewModel.getRibbitPosts()
                navController.navigate(RibbitScreen.HomeScreen.name)
                postingViewModel.creatingPostUiState = CreatingPostUiState.Ready
            }else{  // CommuIdScreen 인 경우
                Log.d("HippoLog, CreatingPostScreen", "Success to CommuIdScreen")
                getCardViewModel.getCommuIdPosts((commuViewModel.commuIdUiState as CommuIdUiState.Success).commuItem.id!!)
                navController.navigate(RibbitScreen.CommuIdScreen.name)
                postingViewModel.creatingPostUiState = CreatingPostUiState.Ready
            }
        }

        is CreatingPostUiState.Loading -> {
            Log.d("HippoLog, CreatingPostScreen", "Loading")
            LoadingScreen(modifier = modifier)
        }

        is CreatingPostUiState.Error -> {
            Log.d("HippoLog, CreatingPostScreen", "Error")
            ErrorScreen(modifier = modifier)
        }
    }
}

@Composable
fun InputPostScreen(
    navController: NavHostController,
    postingViewModel: PostingViewModel,
    commuViewModel: CommuViewModel,
//    cardViewModel: CardViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }
    var videoAbsolutePath by remember { mutableStateOf<String?>(null) }
    var videoFile by remember { mutableStateOf<File?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract =
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }
    val videoLauncher = rememberLauncherForActivityResult(
        contract =
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> videoUri = uri }
    Column(
        modifier = modifier
            .padding(40.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.create_ribbit),
            color = Color.Black,
            modifier = modifier
                .padding(bottom = 16.dp)
                .align(alignment = Alignment.Start)
        )
        InputTextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier = modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = modifier.height(4.dp))
        if (imageUri != null) {
            Box(modifier = modifier.size(200.dp)) {
                imageUri?.let {
                    if (Build.VERSION.SDK_INT < 28) {
                        bitmap.value = MediaStore.Images
                            .Media.getBitmap(context.contentResolver, it)   // getBitmap: deprecated
                    } else {
                        val source = ImageDecoder
                            .createSource(context.contentResolver, it)
                        bitmap.value = ImageDecoder.decodeBitmap(source)
                    }

                    bitmap.value?.let { btm ->
                        Image(
                            bitmap = btm.asImageBitmap(),
                            contentScale = ContentScale.Fit,
                            contentDescription = null,
                            modifier = modifier.size(200.dp)
                        )
                    }
                }
            }
        }
        if (videoUri != null) {
            videoAbsolutePath = postingViewModel.getFilePathFromUri(context, videoUri!!)
            videoFile = videoAbsolutePath?.let { File(it) }
            Row(modifier = modifier) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = "Video Uri",
                    modifier = modifier.padding(8.dp)
                )
                videoFile?.let {
                    Text(
                        text = it.name,
                        modifier = modifier.padding(8.dp)
                    )
                }
            }
        }
        Row(modifier = modifier) {
            Button(
                onClick = { imageLauncher.launch("image/*") },
                modifier.padding(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Image,
                    contentDescription = "Pick Image button.",
                    modifier = modifier
                )
            }
            Button(
                onClick = { videoLauncher.launch("video/*") },
                modifier = modifier.padding(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.OndemandVideo,
                    contentDescription = "Pick Video button.",
                    modifier = modifier
                )
            }
        }
        Row(modifier = modifier) {
            Button(
                onClick = { navController.navigate(RibbitScreen.HomeScreen.name) },
                modifier = modifier.padding(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    modifier = modifier
                )
            }
            Button(
                onClick = {
                    if(postingViewModel.currentScreenState.value != RibbitScreen.CommuIdScreen) {
                        postingViewModel.createPost(
                            image = bitmap.value,
                            videoFile = videoFile,
                            inputText = inputText,
                            commuId = null
                        )
                    }else{
                        postingViewModel.createPost(
                            image = bitmap.value,
                            videoFile = videoFile,
                            inputText = inputText,
                            commuId = (commuViewModel.commuIdUiState as CommuIdUiState.Success).commuItem.id
                        )
                    }
                },
                modifier = modifier.padding(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.create_ribbit),
                    modifier = modifier
                )
            }
        }
        Spacer(modifier = modifier.height(150.dp))
    }
}