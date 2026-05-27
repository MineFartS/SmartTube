package minefarts.sharedutils.app.potokennp2.visitor

import minefarts.sharedutils.common.helpers.AppClient
import minefarts.sharedutils.common.helpers.QueryBuilder

internal object VisitorApiHelper {
    fun getVisitorQuery(): String {
        return QueryBuilder(AppClient.WEB)
            .build()
    }
}