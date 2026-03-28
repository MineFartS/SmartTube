package smartyoutubetv2.ui.browse.interfaces;

import com.liskovsoft.smartyoutubetv2.common.app.models.data.SettingsGroup;

public interface SettingsSection extends Section {
    void update(SettingsGroup items);
}
