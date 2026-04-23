package SmartTubeApp.ui.browse.interfaces;

import smartyoutubetv1.app.models.data.SettingsGroup;

public interface SettingsSection extends Section {
    void update(SettingsGroup items);
}
