package androidx.leanback.widget;

/**
 * Interface for receiving notification when a row or item becomes selected. The concept of
 * current selection is different than focus.  A row or item can be selected without having focus;
 * for example, when a row header view gains focus then the corresponding row view becomes selected.
 * This interface expects row object to be sub class of {@link Row}.
 */
public interface OnItemViewSelectedListener extends BaseOnItemViewSelectedListener<Row> {
}
