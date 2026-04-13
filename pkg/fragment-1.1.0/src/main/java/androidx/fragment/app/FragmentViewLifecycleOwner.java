

package androidx.fragment.app;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

class FragmentViewLifecycleOwner implements LifecycleOwner {
    private LifecycleRegistry mLifecycleRegistry = null;

    /**
     * Initializes the underlying Lifecycle if it hasn't already been created.
     */
    void initialize() {
        if (mLifecycleRegistry == null) {
            mLifecycleRegistry = new LifecycleRegistry(this);
        }
    }

    /**
     * @return True if the Lifecycle has been initialized.
     */
    boolean isInitialized() {
        return mLifecycleRegistry != null;
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        initialize();
        return mLifecycleRegistry;
    }

    void handleLifecycleEvent(@NonNull Lifecycle.Event event) {
        mLifecycleRegistry.handleLifecycleEvent(event);
    }
}
