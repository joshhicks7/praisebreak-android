package com.jagoalt.praisebreak.overlay

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

class OverlayHost :
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val registry = LifecycleRegistry(this)
    private val savedStateController = SavedStateRegistryController.create(this)
    override val viewModelStore = ViewModelStore()
    override val lifecycle: Lifecycle
        get() = registry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    init {
        savedStateController.performAttach()
        savedStateController.performRestore(null)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    fun start() {
        if (registry.currentState == Lifecycle.State.CREATED) {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }
    }

    fun resume() {
        if (registry.currentState == Lifecycle.State.STARTED) {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
    }

    fun destroy() {
        when (registry.currentState) {
            Lifecycle.State.RESUMED -> {
                registry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            }
            Lifecycle.State.STARTED -> {
                registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            }
            Lifecycle.State.CREATED -> registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            else -> Unit
        }
        viewModelStore.clear()
    }
}
