package minefarts.smarttube.ui.browse.interfaces;

import minefarts.smarttube.app.models.data.SettingsGroup;

public interface SettingsSection extends Section {
    void update(SettingsGroup items);
}
