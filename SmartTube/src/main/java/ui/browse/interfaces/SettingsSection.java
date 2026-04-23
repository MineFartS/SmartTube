package SmartTubeApp.ui.browse.interfaces;

import SmartTubeApp.app.models.data.SettingsGroup;

public interface SettingsSection extends Section {
    void update(SettingsGroup items);
}
