package minefarts.smarttube.utils.next.v2.gen

import minefarts.smarttube.utils.browse.gen.GridRenderer
import minefarts.smarttube.utils.browse.gen.SectionWrapper
import minefarts.smarttube.utils.browse.gen.Shelf
import minefarts.smarttube.utils.common.models.gen.*

public data class NextVideoItem(
    val item: Item?,
    val endpoint: Endpoint?
) {

    data class Item(val previewButtonRenderer: PreviewButtonRenderer?) {
        data class PreviewButtonRenderer(val thumbnail: ThumbnailItem?, val title: TextItem?, val byline: TextItem?)
    }

    data class Endpoint(val watchEndpoint: WatchEndpointItem?)

}

public data class ShelfRenderer(
    val title: TextItem?,
    val content: Content?,
    val headerRenderer: HeaderRenderer?,
    val endpoint: NavigationEndpointItem?,
    val tvhtml5ShelfRendererType: String?
) {

    data class Content(
        val gridRenderer: GridRenderer?,
        val expandedShelfContentsRenderer: ExpandedShelfContentsRenderer?,
        val horizontalListRenderer: HorizontalListRenderer?
    ) {
    
        data class ExpandedShelfContentsRenderer(
            val items: List<ItemWrapper?>?
        )

        data class HorizontalListRenderer(
                val items: List<ItemWrapper?>?,
                val continuations: List<ContinuationItem?>?
        )
    
    }

    data class HeaderRenderer(
        val shelfHeaderRenderer: ShelfHeaderRenderer?,
        val chipCloudRenderer: ChipCloudRenderer?
    ) {

        data class ShelfHeaderRenderer(
                val title: TextItem?,
                val avatarLockup: AvatarLockup?,
                val buttons: List<ButtonContentWrapper?>?
        ) {
        
            data class AvatarLockup(
                val avatarLockupRenderer: AvatarLockupRenderer?
            ) {
                data class AvatarLockupRenderer(
                    val title: TextItem?
                )
            }

        }

        data class ChipCloudRenderer(
                val chips: List<ChipItem?>?
        )

    }

}

public data class ChipItem(
        val chipCloudChipRenderer: ChipCloudChipRenderer?
) {

    data class ChipCloudChipRenderer(
            val text: TextItem?,
            val content: Content?
    ) {
        
        data class Content(
            val horizontalListRenderer: HorizontalListRenderer?,
            val sectionListRenderer: SectionListRenderer?
        ) {

            data class HorizontalListRenderer(
                    val items: List<ItemWrapper?>?,
                    val continuations: List<ContinuationItem?>?
            )

            data class SectionListRenderer(
                    val contents: List<Shelf?>?
            ) {
                
                data class Shelf(
                        val shelfRenderer: ShelfRenderer?
                )

            }

        }

    }
    
}

public data class GridContinuationWrapper(
    val items: List<ItemWrapper?>?,
    val contents: List<ItemWrapper?>?, // TV
    val continuations: List<ContinuationItem?>?
)

public data class TvSurfaceContentContinuation(
    val content: Content?
) {
    data class Content(
        val gridRenderer: GridContinuationWrapper?,
        val sectionListRenderer: SectionListContinuation?
    )
}

public data class SectionListContinuation(
    val contents: List<Shelf?>?,
    val continuations: List<ContinuationItem?>?
)

public data class ContinuationItem(
    val reloadContinuationData: ReloadContinuationData?,
    val nextContinuationData: NextContinuationData?,
    val nextRadioContinuationData: NextContinuationData?,
    val invalidationContinuationData: LiveChatContinuationData?, // live chats
    val timedContinuationData: LiveChatContinuationData? // live chats
) {
    data class ReloadContinuationData(
            val continuation: String?
    )

    data class NextContinuationData(
            val continuation: String?,
            val label: TextItem?
    )

    data class LiveChatContinuationData(
            val timeoutMs: Int?,
            val continuation: String?
    )
}

public data class VideoOwnerItem(
    val thumbnail: ThumbnailItem?,
    val title: TextItem?,
    val subscribed: Boolean?,
    val subscriberCountText: TextItem?,
    val subscriptionButton: SubscriptionButton?,
    val subscribeButton: SubscribeButton?,
    val navigationEndpoint: NavigationEndpointItem?
) {
    data class SubscriptionButton(
            val subscribed: Boolean?
    )

    data class SubscribeButton(
            val subscribeButtonRenderer: SubscribeButtonRenderer?
    )
}

public data class VideoMetadataRenderer(
    val owner: Owner?,
    val title: TextItem?,
    val byline: TextItem?,
    val albumName: TextItem?,
    val videoId: String?,
    val description: TextItem?,
    val publishedTimeText: TextItem?,
    val publishedTime: TextItem?,
    val dateText: TextItem?,
    val viewCountText: TextItem?,
    val shortViewCountText: TextItem?,
    val viewCount: ViewCount?,
    val likeStatus: String?,
    val likeButton: LikeButton?,
    val badges: List<Badge?>?,
    val thumbnailOverlays: List<ThumbnailOverlayItem?>?
) {
    data class Owner(
            val videoOwnerRenderer: VideoOwnerItem?
    )

    data class ViewCount(
            val videoViewCountRenderer: VideoViewCountRenderer?
    ) {
        data class VideoViewCountRenderer(
                val viewCount: TextItem?,
                val shortViewCount: TextItem?,
                val isLive: Boolean?
        )
    }

    data class LikeButton(
            val likeButtonRenderer: LikeButtonRenderer?
    )

    data class Badge(
            val upcomingEventBadge: UpcomingEventBadge?
    ) {
        data class UpcomingEventBadge(
                val label: TextItem?
        )
    }
}

public data class LikeButtonRenderer(
    val likeStatus: String?,
    val likeCount: Int?,
    val likeCountText: TextItem?
)

public data class ButtonStateItem(
    val subscribeButton: SubscribeButton?,
    val likeButton: LikeButton?,
    val dislikeButton: DislikeButton?,
    val channelButton: ChannelButton?,
    val buttons: List<GenericButton?>?
) {
    data class SubscribeButton(
            val toggleButtonRenderer: ToggleButtonRenderer?
    )

    data class LikeButton(
            val toggleButtonRenderer: ToggleButtonRenderer?
    )

    data class DislikeButton(
            val toggleButtonRenderer: ToggleButtonRenderer?
    )

    data class ChannelButton(
            val videoOwnerRenderer: VideoOwnerItem?
    )

    data class GenericButton(
            val type: String?,
            val button: ButtonContentWrapper?
    )
}

public data class PlaylistInfo(
    val title: String?,
    val currentIndex: Int?,
    val playlistId: String?,
    val totalVideos: Int?,
    val ownerName: TextItem?,
    val isEditable: Boolean?
)

//////////

public data class EngagementPanel(
    val engagementPanelSectionListRenderer: EngagementPanelSectionListRenderer?
) {
    data class EngagementPanelSectionListRenderer(
        val panelIdentifier: String?,
        val header: Header?,
        val content: Content?,
        val identifier: Identifier?
    ) {
        data class Header(
            val engagementPanelTitleHeaderRenderer: EngagementPanelTitleHeaderRenderer?,
            val overlayPanelHeaderRenderer: OverlayPanelHeaderRenderer?
        )
        data class Content(
            val structuredDescriptionContentRenderer: StructuredDescriptionContentRenderer?,
            val macroMarkersListRenderer: MacroMarkersListRenderer?,
            val sectionListRenderer: SectionListRenderer?
        ) {
            data class StructuredDescriptionContentRenderer(
                 val items: List<Item?>?
            ) {
                data class Item(
                    val videoDescriptionHeaderRenderer: VideoDescriptionHeaderRenderer?,
                    val expandableVideoDescriptionBodyRenderer: ExpandableVideoDescriptionBodyRenderer?
                )
            }
            data class MacroMarkersListRenderer(
                val contents: List<ChapterItemWrapper?>
            )
            data class SectionListRenderer(
                val contents: List<SectionWrapper?>?
            )
        }
        data class Identifier(
            val tag: String?
        )
    }
}

public data class VideoDescriptionHeaderRenderer(
    val title: TextItem?,
    val channel: TextItem?,
    val views: TextItem?,
    val publishDate: TextItem,
    val channelNavigationEndpoint: NavigationEndpointItem?,
    val factoid: List<Factoid?>?
)

public data class ExpandableVideoDescriptionBodyRenderer(
    val descriptionBodyText: TextItem?,
    val label: TextItem?
)

public data class Factoid(
    val factoidRenderer: FactoidRenderer?
) {
    data class FactoidRenderer(
        val value: TextItem?,
        val label: TextItem?,
        val accessibilityText: String?
    )
}

public data class Menu(
    val sortFilterSubMenuRenderer: SortFilterSubMenuRenderer?
) {
    data class SortFilterSubMenuRenderer(
        val subMenuItems: List<SubMenuItem>
    )
}

public data class SubMenuItem(
    val continuation: ContinuationItem?
)

///////// Chapters V1

public data class ChapterItem(
    val chapterRenderer: ChapterRenderer?
)

public data class ChapterRenderer(
    val title: TextItem?,
    val timeRangeStartMillis: Long?,
    val thumbnail: ThumbnailItem?
)

///////// Chapters V2

public data class ChapterItemWrapper(
    val chapterRenderer: ChapterRenderer?,
    val macroMarkersListItemRenderer: MacroMarkersListItemRenderer?,

    // Chapters V3
    val title: TextItem?,
    val startMillis: String?,
    val durationMillis: String?,
    val thumbnailDetails: ThumbnailItem?
)

public data class MacroMarkersListItemRenderer(
    val title: TextItem?,
    val timeDescription: TextItem?,
    val thumbnail: ThumbnailItem?,
    val onTap: TapItem?
)

public data class TapItem(
    val watchEndpoint: WatchEndpointItem?
)

///////// Chapters V3

public data class Marker(
    val title: TextItem?,
    val startMillis: String?,
    val durationMillis: String?,
    val thumbnailDetails: ThumbnailItem?
)

//////////

public data class NextVideoRenderer(
    val maybeHistoryEndpointRenderer: NextVideoItem?,
    val autoplayEndpointRenderer: NextVideoItem?,
    val autoplayVideoWrapperRenderer: AutoplayVideoWrapperRenderer?
) {
    data class AutoplayVideoWrapperRenderer(
        val primaryEndpointRenderer: PrimaryEndpointRenderer?
    ) {
        data class PrimaryEndpointRenderer(
            val autoplayEndpointRenderer: NextVideoItem?
        )
    }
}