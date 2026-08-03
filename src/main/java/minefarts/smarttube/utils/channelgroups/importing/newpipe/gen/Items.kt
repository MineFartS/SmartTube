package minefarts.smarttube.utils.channelgroups.importing.newpipe.gen

public data class NewPipeSubscriptionsGroup(
    val subscriptions: List<SubscriptionsItem>?
) {
    data class SubscriptionsItem(
        val service_id: Int?,
        val url: String?,
        val name: String?
    )
}