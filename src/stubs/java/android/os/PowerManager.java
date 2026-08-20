package android.os;

/**
 * Stub interface implemented directly in the application to prevent 
 * ClassNotFoundException failures on Android 9 and older devices.
 */
public class PowerManager {
    
    public interface OnThermalStatusChangedListener {
        void onThermalStatusChanged(int status);
    }
    
}
