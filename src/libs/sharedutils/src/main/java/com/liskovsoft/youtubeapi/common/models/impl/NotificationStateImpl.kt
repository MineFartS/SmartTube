package com.liskovsoft.sharedutils.common.models.impl

import com.liskovsoft.sharedutils.data.NotificationState
import com.liskovsoft.sharedutils.common.models.gen.NotificationStateItem
import com.liskovsoft.sharedutils.common.models.gen.getStateId
import com.liskovsoft.sharedutils.common.models.gen.getStateParams
import com.liskovsoft.sharedutils.common.models.gen.getTitle

internal open class NotificationStateImpl(
    val notificationStateItem: NotificationStateItem,
    val selectedSateId: Int?,
    val channelId: String?,
    val params: String?,
    val isSubscribed: Boolean
): NotificationState {
    private var _selected: Boolean? = null
    lateinit var allStates: List<NotificationStateImpl>

    override fun isSelected(): Boolean {
        return _selected ?: (notificationStateItem.getStateId() == selectedSateId)
    }

    override fun getTitle(): String? {
        return notificationStateItem.getTitle()
    }

    val stateParams = notificationStateItem.getStateParams()

    val index by lazy { allStates.indexOf(this) }

    fun setSelected() {
        allStates.forEach { it._selected = it == this }
    }
}
