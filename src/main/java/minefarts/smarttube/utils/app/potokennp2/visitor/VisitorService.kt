package minefarts.smarttube.utils.app.potokennp2.visitor

import minefarts.smarttube.utils.app.potokennp2.visitor.data.getVisitorData
import minefarts.smarttube.google.common.helpers.RetrofitHelper

public object VisitorService {
    private val mApi = RetrofitHelper.create(VisitorApi::class.java)
    fun getVisitorData(): String? {
        val visitorResult = RetrofitHelper.get(mApi.getVisitorId(), false)

        return visitorResult?.getVisitorData()
    }
}