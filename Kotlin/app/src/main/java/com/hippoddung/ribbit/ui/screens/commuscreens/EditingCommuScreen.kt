package com.hippoddung.ribbit.ui.screens.commuscreens

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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hippoddung.ribbit.R
import com.hippoddung.ribbit.network.bodys.RibbitCommuItem
import com.hippoddung.ribbit.ui.RibbitScreen
import com.hippoddung.ribbit.ui.screens.statescreens.ErrorScreen
import com.hippoddung.ribbit.ui.screens.statescreens.LoadingScreen
import com.hippoddung.ribbit.ui.viewmodel.CommuUiState
import com.hippoddung.ribbit.ui.viewmodel.CommuViewModel
import com.hippoddung.ribbit.ui.viewmodel.ManageCommuUiState
import kotlinx.coroutines.runBlocking

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun EditingCommuScreen(
    commuViewModel: CommuViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    when (commuViewModel.manageCommuUiState) {
        is ManageCommuUiState.ReadyManage -> {
            Log.d("HippoLog, EditingCommuScreen", "ReadyManage")
            commuViewModel.manageCommuUiState = ManageCommuUiState.ReadyEdit(   // 혹시 UiState가 변경되지 않고 넘어온 경우 UiState를 업데이트
                (commuViewModel.manageCommuUiState as ManageCommuUiState.ReadyManage).commuItem
            )
        }
        is ManageCommuUiState.ReadyEdit -> {
            Log.d("HippoLog, EditingCommuScreen", "ReadyEdit")
            EditCommuScreen(
                navController = navController,
                commuViewModel = commuViewModel,
                modifier = modifier
            )
        }

        is ManageCommuUiState.Success -> {
            Log.d("HippoLog, EditingCommuScreen", "Success")
            commuViewModel.getCommus()
            navController.navigate(RibbitScreen.CommuScreen.name)
            commuViewModel.manageCommuUiState = ManageCommuUiState.ReadyEdit(RibbitCommuItem())
        }

        is ManageCommuUiState.Loading -> {
            Log.d("HippoLog, EditingCommuScreen", "Loading")
            LoadingScreen(modifier = modifier)
        }

        is ManageCommuUiState.Error -> {
            Log.d("HippoLog, EditingCommuScreen", "Error")
            ErrorScreen(modifier = modifier)
        }
    }
}

@Composable
fun EditCommuScreen(
    navController: NavHostController,
    commuViewModel: CommuViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var ribbitCommuItem by remember { mutableStateOf(RibbitCommuItem()) }// state check 실행을 위해 initializing
    if (commuViewModel.manageCommuUiState is ManageCommuUiState.ReadyEdit) {   // navigation 으로 이동시 현재 스크린 을 backStack 으로 보내면서 재실행, state casting 오류가 발생, state check 삽입
        ribbitCommuItem = (commuViewModel.manageCommuUiState as ManageCommuUiState.ReadyEdit).commuItem
    }
    var inputCommuName by remember { mutableStateOf(ribbitCommuItem.comName ?: "") }
    var inputDescription by remember { mutableStateOf(ribbitCommuItem.description ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val bitmap = remember { mutableStateOf<Bitmap?>(null) }
    var privateMode by remember { mutableStateOf(ribbitCommuItem.privateMode ?: false) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract =
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    Column(
        modifier = modifier
            .padding(40.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.edit_commu),
                color = Color.Black,
                modifier = modifier
                    .padding(bottom = 16.dp)
            )
            Button(
                onClick = {
                    commuViewModel.deleteCommuIdState = ribbitCommuItem.id
                    commuViewModel.deleteCommuClickedUiState = true
                },
                modifier = modifier
            ) {
                Text(
                    text = stringResource(R.string.delete_commu),
                    modifier = modifier
                )
            }
        }
        TextField(
            value = inputCommuName,
            onValueChange = { inputCommuName = it },
            label = { Text(text = "Commu Name", modifier = modifier) },
            modifier = modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth()
        )
        TextField(
            value = inputDescription,
            onValueChange = { inputDescription = it },
            label = { Text(text = "Commu Description", modifier = modifier) },
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
        } else {
            if (ribbitCommuItem.backgroundImage != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context = LocalContext.current).data(
                        ribbitCommuItem.backgroundImage
                            ?: "https://res.heraldm.com/content/image/2015/06/15/20150615000967_0.jpg"
                    )
                        .crossfade(true).build(),
                    contentDescription = stringResource(R.string.commu_background_image),
                    modifier = modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clickable { imageLauncher.launch("image/*") },
                    placeholder = painterResource(R.drawable.loading_img),
                    error = painterResource(R.drawable.ic_broken_image),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Crop,
                )
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

            val image = if (privateMode) Icons.Filled.Lock else Icons.Filled.LockOpen
            val description = if (privateMode) "Private" else "Public"

            Button(
                onClick = { privateMode = !privateMode },
                modifier = modifier.padding(14.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = modifier
                        .width(100.dp)
                ) {
                    Icon(
                        imageVector = image,
                        contentDescription = description,
                        modifier = modifier
                    )
                    Text(
                        text = description,
                        textAlign = TextAlign.Center,
                        modifier = modifier.fillMaxWidth()
                    )
                }
            }

        }
        Row(modifier = modifier) {
            Button(
                onClick = { navController.navigate(RibbitScreen.CommuScreen.name) },
                modifier = modifier.padding(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    modifier = modifier
                )
            }
            Button(
                onClick = {
                    commuViewModel.editCommu(
                        backgroundImage = bitmap.value,
                        description = inputDescription,
                        commuName = inputCommuName,
                        privateMode = privateMode
                    )
                },
                modifier = modifier.padding(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.save),
                    modifier = modifier
                )
            }
        }
        Spacer(modifier = modifier.height(150.dp))
    }
    if (commuViewModel.deleteCommuClickedUiState) {
        AlertDialog(
            onDismissRequest = { commuViewModel.deleteCommuClickedUiState = false },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        commuViewModel.deleteCommuClickedUiState = false
                        commuViewModel.commuUiState = CommuUiState.Loading
                        runBlocking {   // 정확한 삭제정보 표시를 위해 동기로 실행
                            Log.d("HippoLog, CommuScreen", "Delete Commu")
                            commuViewModel.deleteCommuIdState?.let { commuViewModel.deleteCommuIdCommu(it) }
                        }
                        commuViewModel.getCommus()
                        navController.navigate(RibbitScreen.CommuScreen.name)
                    },
                    content = {
                        Text(
                            text = "Delete Commu",
                            color = Color(0xFF006400),
                            fontSize = 14.sp,
                            modifier = modifier
                        )
                    },
                    modifier = modifier,
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { commuViewModel.deleteCommuClickedUiState = false },
                    content = {
                        Text(
                            text = "Cancel",
                            color = Color(0xFF006400),
                            fontSize = 14.sp,
                            modifier = modifier
                        )
                    },
                    modifier = modifier
                )
            },
            text = {
                Text(
                    text = "Are you really going to delete this commu?",
                    modifier = modifier
                )
            },
            modifier = modifier
        )
    }
}