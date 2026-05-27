package minefarts.smarttube.utils.app.potokennp2.visitor

import minefarts.smarttube.utils.common.helpers.AppClient
import minefarts.smarttube.utils.common.helpers.QueryBuilder

internal object VisitorApiHelper {
    fun getVisitorQuery(): String {
        return QueryBuilder(AppClient.WEB)
            .build()
    }
}