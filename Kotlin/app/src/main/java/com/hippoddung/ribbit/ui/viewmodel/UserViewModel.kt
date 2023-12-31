package com.hippoddung.ribbit.ui.viewmodel

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hippoddung.ribbit.data.network.StringRepository
import com.hippoddung.ribbit.data.network.UploadCloudinaryRepository
import com.hippoddung.ribbit.data.network.UserRepository
import com.hippoddung.ribbit.network.bodys.User
import com.hippoddung.ribbit.network.bodys.requestbody.KarloUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.HttpException
import java.io.IOException
import java.net.URLEncoder
import javax.inject.Inject

sealed interface MyProfileUiState {
    data class Exist(val myProfile: User) : MyProfileUiState
    object Lack : MyProfileUiState
    object Loading : MyProfileUiState
    data class Error(val errorCode: String) : MyProfileUiState
}

sealed interface ProfileUiState {
    data class Exist(val user: User) : ProfileUiState
    object Loading : ProfileUiState
    data class Error(val errorCode: String) : ProfileUiState
}

sealed interface EditingProfileUiState {
    object Ready : EditingProfileUiState
    object Loading : EditingProfileUiState
    object Success : EditingProfileUiState
    object Error : EditingProfileUiState
}

sealed interface WithdrawingUserUiState {
    object Ready : WithdrawingUserUiState
    object Loading : WithdrawingUserUiState
    object Success : WithdrawingUserUiState
    object Error : WithdrawingUserUiState
}

sealed interface SearchingUserUiState {
    object Ready : SearchingUserUiState
    object Loading : SearchingUserUiState
    data class Success(val users: List<User>) : SearchingUserUiState
    object Error : SearchingUserUiState
}

sealed interface UploadProfileImageCloudinaryUiState {
    object Loading : UploadProfileImageCloudinaryUiState
    data class Success(val profileImageUrl: String) : UploadProfileImageCloudinaryUiState
    data class Error(val error: Exception) : UploadProfileImageCloudinaryUiState
    object None : UploadProfileImageCloudinaryUiState
}

sealed interface UploadProfileBackgroundImageCloudinaryUiState {
    object Loading : UploadProfileBackgroundImageCloudinaryUiState
    data class Success(val profileBackgroundImageUrl: String) :
        UploadProfileBackgroundImageCloudinaryUiState

    data class Error(val error: Exception) : UploadProfileBackgroundImageCloudinaryUiState
    object None : UploadProfileBackgroundImageCloudinaryUiState
}

sealed interface GetAiImageUrlUiState {
    object Ready : GetAiImageUrlUiState
    object Loading : GetAiImageUrlUiState
    data class Success(val aiImageBitmap: Bitmap) : GetAiImageUrlUiState
    object Error : GetAiImageUrlUiState
}

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val stringRepository: StringRepository,
    private val uploadCloudinaryRepository: UploadCloudinaryRepository
) : ViewModel() {
    var myProfile = MutableLiveData<User?>()
    var myProfileUiState: MyProfileUiState by mutableStateOf(MyProfileUiState.Lack)
        private set

    var profileUiState: ProfileUiState by mutableStateOf(ProfileUiState.Loading)
        private set


    var editingProfileUiState: EditingProfileUiState by mutableStateOf(EditingProfileUiState.Ready)
    private var editingProfileImageUrl: String? = null
    private var editingProfileBackgroundImageUrl: String? = null

    var withdrawingUserUiState: WithdrawingUserUiState by mutableStateOf(WithdrawingUserUiState.Ready)

    private var uploadProfileImageCloudinaryUiState: UploadProfileImageCloudinaryUiState by mutableStateOf(
        UploadProfileImageCloudinaryUiState.None
    )
    private var uploadProfileBackgroundImageCloudinaryUiState: UploadProfileBackgroundImageCloudinaryUiState by mutableStateOf(
        UploadProfileBackgroundImageCloudinaryUiState.None
    )

    private var _usersSearchData = MutableStateFlow<List<User>>(listOf())
    val usersSearchData: StateFlow<List<User>> = _usersSearchData.asStateFlow()

    var getAiImageUiState: GetAiImageUrlUiState by mutableStateOf(GetAiImageUrlUiState.Ready)
    private var kakaoImageUrlState: String? by mutableStateOf(null)

    fun getMyProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            myProfileUiState = MyProfileUiState.Loading
            Log.d("HippoLog, UserViewModel", "getMyProfile, $myProfileUiState")
            try {
                val responseUser = userRepository.getMyProfile()
                myProfileUiState = MyProfileUiState.Exist(responseUser)
                myProfile.postValue(responseUser)    // user Livedata 에 user 값을 넣는다.
            } catch (e: IOException) {
                Log.d("HippoLog, UserViewModel", "${e.stackTrace}, ${e.message}")
                myProfileUiState = MyProfileUiState.Error(e.message.toString())

            } catch (e: ExceptionInInitializerError) {
                Log.d("HippoLog, UserViewModel", "${e.stackTrace}, ${e.message}")
                myProfileUiState = MyProfileUiState.Error(e.message.toString())

            } catch (e: HttpException) {
                Log.d("HippoLog, UserViewModel", "${e.stackTrace}, ${e.code()}, $e")
                myProfileUiState = if (e.code() == 500) {
                    MyProfileUiState.Error(e.code().toString())
                } else {
                    MyProfileUiState.Error(e.message.toString())
                }
            }
            Log.d("HippoLog, UserViewModel", "getMyProfile, $myProfileUiState")
        }
    }

    suspend fun putUserIdFollow(userId: Int) {   // 얘도 이것 만으로 follow 와 unfollow 를 다 함.
        try {
            userRepository.putUserIdFollow(userId)
        } catch (e: IOException) {
            Log.d("HippoLog, UserViewModel", "putUserIdFollow: ${e.stackTrace}, ${e.message}")
        } catch (e: ExceptionInInitializerError) {
            Log.d("HippoLog, UserViewModel", "putUserIdFollow: ${e.stackTrace}, ${e.message}")
        }
    }

    fun resetMyProfile() {
        myProfile.postValue(null)
        Log.d("HippoLog, UserViewModel", "유저정보 리셋")
    }

    fun getProfile(userId: Int) {   // 원하는 유저의 profile 을 가져오는 메소드
        viewModelScope.launch(Dispatchers.IO) {
            profileUiState = ProfileUiState.Loading
            Log.d("HippoLog, UserViewModel", "getUserProfile, $profileUiState")
            try {
                val responseProfile = userRepository.getProfile(userId)
                profileUiState = ProfileUiState.Exist(responseProfile)
            } catch (e: IOException) {
                Log.d("HippoLog, UserViewModel", "${e.stackTrace}, ${e.message}")
                profileUiState = ProfileUiState.Error(e.message.toString())

            } catch (e: ExceptionInInitializerError) {
                Log.d("HippoLog, UserViewModel", "${e.stackTrace}, ${e.message}")
                profileUiState = ProfileUiState.Error(e.message.toString())

            } catch (e: HttpException) {
                Log.d("HippoLog, UserViewModel", "${e.stackTrace}, ${e.code()}, $e")
                profileUiState = if (e.code() == 500) {
                    ProfileUiState.Error(e.code().toString())
                } else {
                    ProfileUiState.Error(e.message.toString())
                }
            }
            Log.d("HippoLog, UserViewModel", "getProfile, $profileUiState")
        }
    }

    fun editProfile(
        inputFullName: String = "",
        inputBio: String?,
        inputWebsite: String?,
        inputEducation: String?,
        inputBirthDate: String?,
        profileImage: Bitmap?,
        profileBackgroundImage: Bitmap?,
    ) {
        Log.d("HippoLog, UserViewModel", "editProfile 1")

        viewModelScope.launch(Dispatchers.IO) {
            editingProfileUiState = EditingProfileUiState.Loading
            uploadProfileImageCloudinaryUiState = UploadProfileImageCloudinaryUiState.None
            uploadProfileBackgroundImageCloudinaryUiState =
                UploadProfileBackgroundImageCloudinaryUiState.None
            Log.d("HippoLog, UserViewModel", "editProfile 2")
            runBlocking {
                launch(Dispatchers.IO) {
                    Log.d("HippoLog, UserViewModel", "editProfile 2-1")
                    if (profileImage != null) {
                        uploadProfileImageCloudinaryUiState =
                            UploadProfileImageCloudinaryUiState.Loading
                        uploadProfileImageCloudinary(profileImage = profileImage)
                        // 성공하면 uploadImageCloudinary 함수에서 UploadImageCloudinaryUiState.Success 로 업데이트함
                        when (uploadProfileImageCloudinaryUiState) {
                            is UploadProfileImageCloudinaryUiState.Success -> {
                                editingProfileImageUrl =
                                    (uploadProfileImageCloudinaryUiState as UploadProfileImageCloudinaryUiState.Success).profileImageUrl
                            }

                            else -> {}  // 다른 경우 처리를 위함, 아직 미구현
                        }
                    }
                }
                launch(Dispatchers.IO) {
                    Log.d("HippoLog, UserViewModel", "editProfile 2-2")
                    if (profileBackgroundImage != null) {
                        uploadProfileBackgroundImageCloudinaryUiState =
                            UploadProfileBackgroundImageCloudinaryUiState.Loading
                        uploadProfileBackgroundImageCloudinary(profileBackgroundImage = profileBackgroundImage)
                        // 성공하면 uploadVideoCloudinary 함수에서 UploadVideoCloudinaryUiState.Success 로 업데이트함
                        when (uploadProfileBackgroundImageCloudinaryUiState) {
                            is UploadProfileBackgroundImageCloudinaryUiState.Success -> {
                                editingProfileBackgroundImageUrl =
                                    (uploadProfileBackgroundImageCloudinaryUiState as UploadProfileBackgroundImageCloudinaryUiState.Success).profileBackgroundImageUrl
                            }

                            else -> {}  // 다른 경우 처리를 위함, 아직 미구현
                        }
                    }
                }
            }
            Log.d("HippoLog, UserViewModel", "editProfile 3")
            Log.d("HippoLog, UserViewModel", "$uploadProfileImageCloudinaryUiState")
            Log.d("HippoLog, UserViewModel", "$uploadProfileBackgroundImageCloudinaryUiState")
            if (
                ((uploadProfileImageCloudinaryUiState is UploadProfileImageCloudinaryUiState.Success) or (uploadProfileImageCloudinaryUiState is UploadProfileImageCloudinaryUiState.None))
                and ((uploadProfileBackgroundImageCloudinaryUiState is UploadProfileBackgroundImageCloudinaryUiState.Success) or (uploadProfileBackgroundImageCloudinaryUiState is UploadProfileBackgroundImageCloudinaryUiState.None))

            ) {
                Log.d("HippoLog, UserViewModel", "editProfile 4")
                putEditingProfile(
                    User(
                        backgroundImage = editingProfileBackgroundImageUrl,
                        bio = inputBio,
                        birthDate = inputBirthDate,
                        education = inputEducation,
                        email = myProfile.value!!.email,
                        fullName = inputFullName,
                        image = editingProfileImageUrl,
                        website = inputWebsite
                    )
                )
            } else {    // uploadImageCloudinaryUiState 와 uploadVideoCloudinaryUiState 가 다른 상태일 때 처리를 위함 미구현
            }
        }
    }

    private suspend fun putEditingProfile(user: User) {
        Log.d("HippoLog, UserViewModel", "putEditingProfile user: $user")
        try {
            userRepository.putEditingProfile(user)
            Log.d("HippoLog, UserViewModel", "putEditingProfile")
            editingProfileUiState = EditingProfileUiState.Success
        } catch (e: IOException) {
            editingProfileUiState = EditingProfileUiState.Error
            Log.d("HippoLog, UserViewModel", "putEditingProfile error: ${e.message}")
        } catch (e: ExceptionInInitializerError) {
            editingProfileUiState = EditingProfileUiState.Error
            Log.d("HippoLog, UserViewModel", "putEditingProfile error: ${e.message}")
        }
    }

    suspend fun postWithdrawal() {
        Log.d("HippoLog, UserViewModel", "postWithdrawal")
        try {
            userRepository.postWithdrawal()
            Log.d("HippoLog, UserViewModel", "postWithdrawal")
            withdrawingUserUiState = WithdrawingUserUiState.Success
        } catch (e: IOException) {
            withdrawingUserUiState = WithdrawingUserUiState.Error
            Log.d("HippoLog, UserViewModel", "postWithdrawal error: ${e.message}")
        } catch (e: ExceptionInInitializerError) {
            withdrawingUserUiState = WithdrawingUserUiState.Error
            Log.d("HippoLog, UserViewModel", "postWithdrawal error: ${e.message}")
        }
    }

    fun getUsersSearch(searchQuery: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("HippoLog, UserViewModel", "getUserSearch")
                _usersSearchData.value = (userRepository.getUsersSearch(searchQuery))
            } catch (e: IOException) {
                Log.d("HippoLog, UserViewModel", "getUserSearch error: ${e.message}")
                SearchingUserUiState.Error
            } catch (e: ExceptionInInitializerError) {
                Log.d("HippoLog, UserViewModel", "getUserSearch error: ${e.message}")
                SearchingUserUiState.Error
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun getAiImage(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            getAiImageUiState = GetAiImageUrlUiState.Loading
            try {
                Log.d("HippoLog, UserViewModel", "getAiImage 1-1")
                kakaoImageUrlState = ((stringRepository.getSendPrompt(query)).kakaoImageUrl)
            } catch (e: IOException) {
                Log.d("HippoLog, UserViewModel", "getAiImage 1-2 error: ${e.message}")
                getAiImageUiState = GetAiImageUrlUiState.Error
            } catch (e: ExceptionInInitializerError) {
                Log.d("HippoLog, UserViewModel", "getAiImage 1-3 error: ${e.message}")
                getAiImageUiState = GetAiImageUrlUiState.Error
            }
            try {
                Log.d("HippoLog, UserViewModel", "getAiImage 2-1")
                kakaoImageUrlState?.let { Log.d("HippoLog, UserViewModel", it) }
                val imageBytes: ByteArray =
                    stringRepository.postWebpToJpg(KarloUrl(kakaoImageUrlState)).bytes()
                getAiImageUiState =
                    GetAiImageUrlUiState.Success(byteArrayToBitmap(imageBytes))
            } catch (e: IOException) {
                Log.d("HippoLog, UserViewModel", "getAiImage 2-2 error: ${e.message}")
                getAiImageUiState = GetAiImageUrlUiState.Error
            } catch (e: ExceptionInInitializerError) {
                Log.d("HippoLog, UserViewModel", "getAiImage 2-3 error: ${e.message}")
                getAiImageUiState = GetAiImageUrlUiState.Error
            } catch (e: Exception) {
                Log.d(
                    "HippoLog, UserViewModel",
                    "getAiImage 2-4 error: ${e.message}, ${e.stackTrace}"
                )
                getAiImageUiState = GetAiImageUrlUiState.Error
            }
        }
    }

    private fun byteArrayToBitmap(imageBytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private suspend fun uploadProfileImageCloudinary(profileImage: Bitmap?) { // CreatingPostViewModel 에 있던 함수, 어떻게 불러와서 쓸지 결정을 못해서 우선 복붙함.
        if (profileImage != null) {
            try {
                val result = uploadCloudinaryRepository.uploadImageCloudinary(profileImage)
                Log.d("HippoLog, UserViewModel", "result: $result")
                uploadProfileImageCloudinaryUiState = UploadProfileImageCloudinaryUiState.Success(
                    profileImageUrl = result.url
                )
            } catch (e: Exception) {
                uploadProfileImageCloudinaryUiState =
                    UploadProfileImageCloudinaryUiState.Error(e)
                Log.d("HippoLog, UserViewModel", "error: ${e.stackTrace}, ${e.message}")
            }
        }
    }

    private suspend fun uploadProfileBackgroundImageCloudinary(profileBackgroundImage: Bitmap?) { // CreatingPostViewModel 에 있던 함수, 어떻게 불러와서 쓸지 결정을 못해서 우선 복붙함.
        if (profileBackgroundImage != null) {
            try {
                val result =
                    uploadCloudinaryRepository.uploadImageCloudinary(profileBackgroundImage)
                Log.d("HippoLog, UserViewModel", "result: $result")
                uploadProfileBackgroundImageCloudinaryUiState =
                    UploadProfileBackgroundImageCloudinaryUiState.Success(
                        profileBackgroundImageUrl = result.url
                    )
            } catch (e: Exception) {
                uploadProfileBackgroundImageCloudinaryUiState =
                    UploadProfileBackgroundImageCloudinaryUiState.Error(e)
                Log.d("HippoLog, UserViewModel", "error: ${e.stackTrace}, ${e.message}")
            }
        }
    }
}