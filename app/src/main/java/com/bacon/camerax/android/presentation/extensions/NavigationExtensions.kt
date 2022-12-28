package com.bacon.camerax.android.presentation.extensions

import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment

fun Fragment.flowNavController(@IdRes navHostId: Int) = requireActivity().findNavController(
    navHostId
)

fun NavController.navigateSafely(@IdRes actionId: Int) {
    currentDestination?.getAction(actionId)?.let { navigate(actionId) }
}

fun NavController.navigateSafely(directions: NavDirections) {
    currentDestination?.getAction(directions.actionId)?.let { navigate(directions) }
}

inline fun <reified T : Fragment> Fragment.parentFragmentInNavHost(): T {
    return ((parentFragment as NavHostFragment).parentFragment as T)
}

fun Fragment.overrideOnBackPressed(onBackPressed: OnBackPressedCallback. () -> Unit) {
    requireActivity().onBackPressedDispatcher.addCallback(this) {
        onBackPressed()
    }
}

fun <T> NavController.getNavigationResult(key: String = "result") =
    currentBackStackEntry?.savedStateHandle?.get<T>(key)

fun <T> NavController.getNavigationResultLiveData(key: String = "result") =
    currentBackStackEntry?.savedStateHandle?.getLiveData<T>(key)

fun <T> NavController.setNavigationResult(key: String = "result", result: T) {
    previousBackStackEntry?.savedStateHandle?.set(key, result)
}

//Only Dialogs
fun <T> NavBackStackEntry.getAccurateResultLiveData(
    lifecycleEvent: Lifecycle.Event = Lifecycle.Event.ON_RESUME,
    key: String,
    observer: (data: T?) -> Unit,
) {
    lifecycle.addObserver(LifecycleEventObserver { _, event ->
        if (event == lifecycleEvent && savedStateHandle.contains(key)) {
            observer(savedStateHandle.get<T>(key))
        }
    })
}