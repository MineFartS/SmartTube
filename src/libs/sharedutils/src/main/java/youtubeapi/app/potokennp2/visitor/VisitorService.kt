package minefarts.sharedutils.app.potokennp2.visitor

import minefarts.sharedutils.app.potokennp2.visitor.data.getVisitorData
import minefarts.googlecommon.common.helpers.RetrofitHelper

internal object VisitorService {
    private val mApi = RetrofitHelper.create(VisitorApi::class.java)
    fun getVisitorData(): String? {
        val visitorResult = RetrofitHelper.get(mApi.getVisitorId(), false)

        return visitorResult?.getVisitorData()
    }
}