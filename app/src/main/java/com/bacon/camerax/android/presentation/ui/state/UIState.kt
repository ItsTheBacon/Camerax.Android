package com.bacon.camerax.android.presentation.ui.state

import com.bacon.domain.utils.NetworkError

sealed class UIState<T> {
    class Idle<T> : UIState<T>()
    class Loading<T> : UIState<T>()
    class Error<T>(val error: NetworkError) : UIState<T>()
    class Success<T>(val data: T) : UIState<T>()
}