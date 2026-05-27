package minefarts.sharedutils.notifications.gen

import minefarts.sharedutils.common.models.gen.MenuWrapper
import minefarts.sharedutils.common.models.gen.NavigationEndpointItem
import minefarts.sharedutils.common.models.gen.TextItem
import minefarts.sharedutils.common.models.gen.ThumbnailItem

internal data class NotificationAction(
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

internal data class NotificationItem(
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