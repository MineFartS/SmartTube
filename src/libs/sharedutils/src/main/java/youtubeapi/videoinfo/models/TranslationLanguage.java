package minefarts.sharedutils.videoinfo.models;

import minefarts.googlecommon.common.converters.jsonpath.JsonPath;
import minefarts.googlecommon.common.models.V2.TextItem;

public class TranslationLanguage {
    @JsonPath("$.languageCode")
    private String mLanguageCode;

    @JsonPath("$.languageName")
    private TextItem mLanguageName;

    public String getLanguageCode() {
        return mLanguageCode;
    }

    public String getLanguageName() {
        return mLanguageName != null ? mLanguageName.toString() : null;
    }
}
