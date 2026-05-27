package minefarts.sharedutils.service.internal

import minefarts.sharedutils.SignInService.OnAccountChange
import minefarts.sharedutils.oauth.Account
import minefarts.sharedutils.misc.WeakHashSet
import minefarts.sharedutils.prefs.SharedPreferencesBase
import minefarts.sharedutils.app.AppService
import minefarts.sharedutils.SignInService

private const val PREF_NAME = "yt_service_prefs"

internal object MediaServicePrefs: SharedPreferencesBase(AppService.instance().context, PREF_NAME), OnAccountChange {
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