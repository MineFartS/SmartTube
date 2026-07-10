package minefarts.smarttube.utils.app.potoken.visitor

import minefarts.smarttube.utils.common.helpers.AppClient
import minefarts.smarttube.utils.common.helpers.QueryBuilder

public object VisitorApiHelper {
    fun getVisitorQuery(): String {
        return QueryBuilder(AppClient.WEB)
            .build()
    }
}