package com.hippoddung.ribbit.ui.screens.homescreens

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.hippoddung.ribbit.network.bodys.RibbitPost
import com.hippoddung.ribbit.ui.screens.carditems.RibbitCard
import com.hippoddung.ribbit.ui.viewmodel.ClassificationUiState
import com.hippoddung.ribbit.ui.viewmodel.GetCardViewModel
import com.hippoddung.ribbit.ui.viewmodel.UserViewModel

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnrememberedMutableState")
@Composable
fun PostsGrid(
    posts: List<RibbitPost>,
    getCardViewModel: GetCardViewModel,
    userViewModel: UserViewModel,
    myId: Int,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val comparator =
        when(getCardViewModel.classificationUiState){
            is ClassificationUiState.Recent -> compareByDescending<RibbitPost> { it.id }
            is ClassificationUiState.Following -> compareByDescending { it.id }
            is ClassificationUiState.TopViews -> compareByDescending { it.viewCount }
            is ClassificationUiState.TopLikes -> compareByDescending { it.totalLikes }
        }
    val sortedRibbitPost = remember(posts, comparator) {
        posts.sortedWith(comparator)
    }   // LazyColumn items 에 List 를 바로 주는 것이 아니라 Comparator 로 정렬하여 remember 로 기억시켜서 recomposition 을 방지하여 성능을 올린다.
    LazyColumn(modifier = modifier) {
        item {
            Column(modifier = modifier) {
                Row(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            getCardViewModel.getRibbitPosts()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if(getCardViewModel.classificationUiState is ClassificationUiState.Recent){
                                Color(0xFF006400)
                            } else Color.White,
                            contentColor = if(getCardViewModel.classificationUiState is ClassificationUiState.Recent){
                                Color.White
                            } else Color(0xFF006400),
                        ),
                        modifier = modifier
                    ) {
                        Text(
                            text = "Recent",
                            fontSize = 16.sp,
                            modifier = modifier
                        )
                    }
                    TextButton(
                        onClick = {
                            getCardViewModel.getFollowingPosts()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if(getCardViewModel.classificationUiState is ClassificationUiState.Following){
                                Color(0xFF006400)
                            } else Color.White,
                            contentColor = if(getCardViewModel.classificationUiState is ClassificationUiState.Following){
                                Color.White
                            } else Color(0xFF006400),
                        ),
                        modifier = modifier
                    ) {
                        Text(
                            text = "Following",
                            fontSize = 16.sp,
                            modifier = modifier
                        )
                    }
                    TextButton(
                        onClick = {
                            getCardViewModel.getTopViewsRibbitPosts()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if(getCardViewModel.classificationUiState is ClassificationUiState.TopViews){
                                Color(0xFF006400)
                            } else Color.White,
                            contentColor = if(getCardViewModel.classificationUiState is ClassificationUiState.TopViews){
                                Color.White
                            } else Color(0xFF006400),
                        ),
                        modifier = modifier
                    ) {
                        Text(
                            text = "Top Views",
                            fontSize = 16.sp,
                            modifier = modifier
                        )
                    }
                    TextButton(
                        onClick = {
                            getCardViewModel.getTopLikesRibbitPosts()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if(getCardViewModel.classificationUiState is ClassificationUiState.TopLikes){
                                Color(0xFF006400)
                            } else Color.White,
                            contentColor = if(getCardViewModel.classificationUiState is ClassificationUiState.TopLikes){
                                Color.White
                            } else Color(0xFF006400),
                        ),
                        modifier = modifier
                    ) {
                        Text(
                            text = "Top Likes",
                            fontSize = 16.sp,
                            modifier = modifier
                        )
                    }
                }
                Canvas(
                    modifier = modifier,
                    onDraw = {
                        drawLine(
                            color = Color(0xFF4c6c4a),
                            start = Offset(0.dp.toPx(), 0.dp.toPx()),
                            end = Offset(500.dp.toPx(), 0.dp.toPx()),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                )
            }
        }
        items(items = sortedRibbitPost, key = { post -> post.id }) {
            RibbitCard(
                post = it,
                getCardViewModel = getCardViewModel,
                myId = myId,
                navController = navController,
                userViewModel = userViewModel,
                modifier = modifier
            )
        }
    }
}