package com.hippoddung.ribbit.ui.viewmodel

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.hippoddung.ribbit.data.network.RibbitRepository
import com.hippoddung.ribbit.network.bodys.RibbitPost
import com.hippoddung.ribbit.network.bodys.requestbody.ReplyRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.inject.Inject

sealed interface DeletePostUiState {
    object Ready : DeletePostUiState
    object Success : DeletePostUiState
    data class Error(val errorCode: String) : DeletePostUiState
    object Loading : DeletePostUiState
}

sealed interface HomeUiState {
    data class Success(val posts: List<RibbitPost>) : HomeUiState
    data class Error(val errorCode: String) : HomeUiState
    object Loading : HomeUiState
}

sealed interface PostIdUiState {
    data class Success(val post: RibbitPost) : PostIdUiState
    data class Error(val errorCode: String) : PostIdUiState
    object Loading : PostIdUiState
}

sealed interface ReplyUiState {
    object Ready : ReplyUiState
    object Loading : ReplyUiState
    object Success : ReplyUiState
    object Error : ReplyUiState
}

sealed interface ReplyClickedUiState {
    object Clicked : ReplyClickedUiState
    object NotClicked : ReplyClickedUiState
}

sealed interface WhereReplyClickedUiState {
    object HomeScreen : WhereReplyClickedUiState
    object TwitIdScreen : WhereReplyClickedUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val ribbitRepository: RibbitRepository
) : BaseViewModel() {
    var homeUiState: HomeUiState by mutableStateOf(HomeUiState.Loading)
    var deletePostUiState: DeletePostUiState by mutableStateOf(DeletePostUiState.Ready)
    var postIdUiState: PostIdUiState by mutableStateOf(PostIdUiState.Loading)

    var replyUiState: ReplyUiState by mutableStateOf(ReplyUiState.Ready)
    var replyClickedUiState: ReplyClickedUiState by mutableStateOf(ReplyClickedUiState.NotClicked)
    var whereReplyClickedUiState: WhereReplyClickedUiState by mutableStateOf(
        WhereReplyClickedUiState.HomeScreen
    )
//    val homeResponse: MutableLiveData<ApiResponse<List<RibbitPost>>> by lazy {
//        MutableLiveData<ApiResponse<List<RibbitPost>>>()
//    }

//    init {
//        getRibbitPosts(
////            object : CoroutinesErrorHandler {
////                override fun onError(message: String) {
////                    Log.d("HippoLog, HomeViewModel", "Error! $message")
////                }
////            }
//        )
//    }

    //    fun getRibbitPosts(coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(
//        homeResponse, coroutinesErrorHandler
//    ) {
//        Log.d("HippoLog, HomeViewModel", "getRibbitPosts")
//        ribbitRepository.getPosts()
//    }
    fun getRibbitPosts() {
        viewModelScope.launch(Dispatchers.IO) {
            homeUiState = HomeUiState.Loading
            Log.d("HippoLog, HomeViewModel", "getRibbitPosts, $homeUiState")
            homeUiState = try {
                HomeUiState.Success(ribbitRepository.getPosts())
            } catch (e: IOException) {
                Log.d("HippoLog, HomeViewModel", "${e.stackTrace}, ${e.message}")
                HomeUiState.Error(e.message.toString())

            } catch (e: ExceptionInInitializerError) {
                Log.d("HippoLog, HomeViewModel", "${e.stackTrace}, ${e.message}")
                HomeUiState.Error(e.message.toString())
            } catch (e: HttpException) {
                Log.d("HippoLog, HomeViewModel", "${e.stackTrace}, ${e.code()}, $e")
                if (e.code() == 500) {
                    HomeUiState.Error(e.code().toString())
                } else {
                    HomeUiState.Error(e.message.toString())
                }
            }
            Log.d("HippoLog, HomeViewModel", "getRibbitPosts, $homeUiState")
        }
    }

    suspend fun deleteRibbitPost(postId: Int) {
        Log.d("HippoLog, HomeViewModel", "deleteRibbitPost, $postId")
        try {
            deletePostUiState = DeletePostUiState.Loading
            ribbitRepository.deletePost(postId)
        } catch (e: IOException) {
            Log.d("HippoLog, HomeViewModel", "${e.stackTrace}, ${e.message}")
            DeletePostUiState.Error(e.message.toString())

        } catch (e: ExceptionInInitializerError) {
            Log.d("HippoLog, HomeViewModel", "${e.stackTrace}, ${e.message}")
            DeletePostUiState.Error(e.message.toString())

        } catch (e: HttpException) {
            Log.d("HippoLog, HomeViewModel", "${e.stackTrace}, ${e.code()}, $e")
            if (e.code() == 500) {
                DeletePostUiState.Error(e.code().toString())
            } else {
                DeletePostUiState.Error(e.message.toString())
            }
        }
        Log.d("HippoLog, HomeViewModel", "deleteRibbitPost")
    }

    fun getPostIdPost(postId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            postIdUiState = PostIdUiState.Loading
            Log.d("HippoLog, HomeViewModel", "getTwitIdPosts, $postIdUiState")
            postIdUiState = try {
                PostIdUiState.Success(ribbitRepository.getPostIdPost(postId))
            } catch (e: IOException) {
                Log.d("HippoLog, HomeViewModel", "${e.stackTrace}, ${e.message}")
                PostIdUiState.Error(e.message.toString())

            } catch (e: ExceptionInInitializerError) {
                Log.d("HippoLog, HomeViewModel", "${e.stackTrace}, ${e.message}")
                PostIdUiState.Error(e.message.toString())

            } catch (e: HttpException) {
                Log.d("HippoLog, HomeViewModel", "${e.stackTrace}, ${e.code()}, $e")
                if (e.code() == 500) {
                    PostIdUiState.Error(e.code().toString())
                } else {
                    PostIdUiState.Error(e.message.toString())
                }
            }
            Log.d("HippoLog, HomeViewModel", "getTwitIdPosts, $postIdUiState")
        }
    }

    //suspend fun retrieveThumbnailFromVideo(videoUrl: String?): Bitmap? {
    fun retrieveThumbnailFromVideo(videoUrl: String?): Future<Bitmap> {
        val executor = Executors.newFixedThreadPool(2)
        val future: Future<Bitmap> = executor.submit(Callable<Bitmap> {
            var retrievalCount = 0
            val maxRetrievalCount = 2 // Adjust this value as needed
            if (retrievalCount >= maxRetrievalCount) {
                // You've reached the limit, so return null or handle it as needed
                return@Callable null
            }
            var bitmap: Bitmap? = null
            var mediaMetadataRetriever: MediaMetadataRetriever? = null
            mediaMetadataRetriever = MediaMetadataRetriever()
            if (Build.VERSION.SDK_INT >= 14) {
                mediaMetadataRetriever.setDataSource(videoUrl, HashMap())
            } else {
                mediaMetadataRetriever.setDataSource(videoUrl)
            }
            try {
                Log.d("HippoLog, HomeScreen, retrieve", "Thread - ${Thread.currentThread().name}")
                bitmap = mediaMetadataRetriever.getFrameAtTime(100000)
            } catch (e: SocketTimeoutException) {
                Log.d(
                    "HippoLog, HomeScreen, retrieve, Exception",
                    "Exception in retrieveThumbnailFromVideo: ${e.message}"
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(
                    "HippoLog, HomeScreen, retrieve, Exception",
                    "Exception in retrieveThumbnailFromVideo: ${e.message}"
                )
            } finally {
                mediaMetadataRetriever.release()
            }
            retrievalCount++
//    return bitmap
            return@Callable bitmap
        }
        )
        return future
    }

    fun postReply(inputText: String, postId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            replyUiState = ReplyUiState.Loading
            val replyRequest = ReplyRequest(content = inputText, twitId = postId)
            try {
                ribbitRepository.postReply(replyRequest)
                replyUiState = ReplyUiState.Success
            } catch (e: IOException) {
                replyUiState = ReplyUiState.Error
                println(e.stackTrace)
            } catch (e: ExceptionInInitializerError) {
                replyUiState = ReplyUiState.Error
                println(e.stackTrace)
            }
            when (whereReplyClickedUiState) {
                is WhereReplyClickedUiState.HomeScreen -> {
                    getRibbitPosts()
                }

                is WhereReplyClickedUiState.TwitIdScreen -> {
                    getPostIdPost((postIdUiState as PostIdUiState.Success).post.id)
                }
            }
        }
    }
}