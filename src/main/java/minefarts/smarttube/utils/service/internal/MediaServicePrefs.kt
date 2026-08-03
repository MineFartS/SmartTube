package minefarts.smarttube.utils.service.internal

import minefarts.smarttube.utils.SignInService.OnAccountChange
import minefarts.smarttube.utils.oauth.Account
import minefarts.smarttube.utils.misc.WeakHashSet
import minefarts.smarttube.utils.prefs.SharedPreferencesBase
import minefarts.smarttube.utils.app.AppService
import minefarts.smarttube.utils.SignInService

private const val PREF_NAME = "yt_service_prefs"

object MediaServicePrefs: SharedPreferencesBase(AppService.instance().context, PREF_NAME), OnAccountChange {

    private const val ANONYMOUS_PROFILE_NAME = "anonymous"
    private val mListeners = WeakHashSet<ProfileChangeListener>()
    private lateinit var mProfileName: String

    interface ProfileChangeListener {
        fun onProfileChanged()
    }

    init {
        val signInService = SignInService.instance()
        setProfileName(signInService.selectedAccount)
        signInService.addOnAccountChange(this)
    }

    override fun onAccountChanged(account: Account?) {
        setProfileName(account)
        notifyListeners()
    }

    private fun notifyListeners() {
        mListeners.forEach { it.onProfileChanged() }
    }

    private fun setProfileName(account: Account?) {
        mProfileName = account?.name?.replace(" ", "_") ?: ANONYMOUS_PROFILE_NAME
    }

    fun addListener(listener: ProfileChangeListener) {
        mListeners.add(listener)
    }

    fun getData(key: String): String? {
        return getString(getProfileDataKey(key), null)
    }

    fun setData(key: String, data: String?) {
        putString(getProfileDataKey(key), data)
    }

    private fun getProfileDataKey(dataKey: String) = "${mProfileName}_$dataKey"
}