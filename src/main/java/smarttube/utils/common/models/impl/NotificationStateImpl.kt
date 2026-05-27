package minefarts.smarttube.utils.common.models.impl

import minefarts.smarttube.utils.data.NotificationState
import minefarts.smarttube.utils.common.models.gen.NotificationStateItem
import minefarts.smarttube.utils.common.models.gen.getStateId
import minefarts.smarttube.utils.common.models.gen.getStateParams
import minefarts.smarttube.utils.common.models.gen.getTitle

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
