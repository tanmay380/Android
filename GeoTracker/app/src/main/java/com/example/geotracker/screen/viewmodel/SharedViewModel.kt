package com.example.geotracker.screen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geotracker.repository.SelectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.remove

@HiltViewModel
class SharedViewModel @Inject constructor(private val selectionManager: SelectionManager) : ViewModel() {

    // backing mutable + public read-only selection flow
//    private val _selectedSessionId = MutableStateFlow<Set<Long?>>(emptySet())
    val selectedSessionId: StateFlow<Set<Long?>> = selectionManager.selectedSessionId


    /** Toggle selection: select id if not selected, otherwise clear selection. */
    fun toggleSessionSelection(sessionId: Long) {
        selectionManager.toggleSessionSelection(sessionId)
    }


    /** Optional helper to clear selection */
    fun clearSelection() {
        selectionManager.clearSelection()
    }
}