package com.clementcorporation.levosonusii.screens.login

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clementcorporation.levosonusii.main.Constants.EMPLOYEE_ID
import com.clementcorporation.levosonusii.model.LSUserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val sessionDataStore: DataStore<LSUserInfo>): ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun signInWithEmailAndPassword(userId: String, password: String, home: () -> Unit = {}) {
        var email: String? = ""
        viewModelScope.launch {
            _loading.value = true
            try {
                FirebaseFirestore.getInstance().collection("users")
                    .document(userId).get().addOnCompleteListener { document ->
                        email = document.result?.getString("emailAddress")
                        email?.let {
                            auth.signInWithEmailAndPassword(it.trim(), password.trim()).addOnCompleteListener{ task ->
                                Log.d("Sign In: ", "SUCCESS")
                                viewModelScope.launch {
                                    sessionDataStore.updateData { userInfo ->
                                        userInfo.copy(employeeId = userId)
                                    }
                                }
                                home()
                            }
                        }
                    }
            } catch (e: Exception) {
                e.localizedMessage?.let {
                    Log.d("Sign In: ", it)
                }
            }
        }
        _loading.value = false
    }
}