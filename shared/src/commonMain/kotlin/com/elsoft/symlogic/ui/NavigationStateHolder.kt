package com.elsoft.symlogic.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * A simple state holder for managing a navigation back stack.
 * @param initialScreen The screen to start with.
 */
//@Composable
class NavigationStateHolder(val initialScreen: Screen) {
    private val _backStack = mutableStateListOf<Screen>()

    val backStack: SnapshotStateList<Screen> = _backStack // Expose as SnapshotStateList for observation

    init {
        _backStack.add(initialScreen)
    }

    /**
     * Navigates to a new screen by pushing it onto the back stack.
     */
    fun navigateTo(screen: Screen) {
        _backStack.add(screen)
    }

    /**
     * Goes back to the previous screen by popping the current screen from the stack.
     * @return true if navigation occurred, false if the stack is at its base (cannot go back further).
     */
    fun goBack(): Boolean {
        if (_backStack.size > 1) {
            _backStack.removeLast()
            return true
        }
        return false
    }

    /**
     * Returns the currently active screen at the top of the stack.
     */
    fun currentScreen(): Screen {
        return _backStack.lastOrNull() ?: initialScreen
    }
}
