package com.christidischristidis.passkeys

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(

) : ViewModel() {

    private var _uiState: MutableState<Boolean> = mutableStateOf(false)
    val uiState: State<Boolean> = _uiState
}
