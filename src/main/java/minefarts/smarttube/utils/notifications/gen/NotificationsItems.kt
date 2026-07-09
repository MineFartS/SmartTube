package minefarts.smarttube.utils.notifications.gen

import minefarts.smarttube.utils.common.models.gen.MenuWrapper
import minefarts.smarttube.utils.common.models.gen.NavigationEndpointItem
import minefarts.smarttube.utils.common.models.gen.TextItem
import minefarts.smarttube.utils.common.models.gen.ThumbnailItem

public data class NotificationAction(
    val openPopupAction: OpenPopupAction?
) {
    data class OpenPopupAction(
        val popup: Popup?
    ) {
        data class Popup(
            val multiPageMenuRenderer: MultiPageMenuRenderer?
        ) {
            data class MultiPageMenuRenderer(
                val sections: List<MenuSection?>?
            ) {
                data class MenuSection(
                    val multiPageMenuNotificationSectionRenderer: MultiPageMenuNotificationSectionRenderer?
                ) {
                    data class MultiPageMenuNotificationSectionRenderer(
                        val items: List<NotificationItem?>?
                    )
                }
            }
        }
    }
}

public data class NotificationItem(
    val notificationRenderer: NotificationRenderer?
) {
    data class NotificationRenderer(
        val thumbnail: ThumbnailItem?,
        val videoThumbnail: ThumbnailItem?,
        val shortMessage: TextItem?,
        val sentTimeText: TextItem?,
        val contextualMenu: MenuWrapper?,
        val navigationEndpoint: NavigationEndpointItem?
    )
}