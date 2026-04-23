package com.liskovsoft.sharedutils.app.potokennp2.visitor

import com.liskovsoft.sharedutils.common.helpers.AppClient
import com.liskovsoft.sharedutils.common.helpers.QueryBuilder

internal object VisitorApiHelper {
    fun getVisitorQuery(): String {
        return QueryBuilder(AppClient.WEB)
            .build()
    }
}