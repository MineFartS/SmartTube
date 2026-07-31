package minefarts.smarttube.commons;

import android.widget.ImageView;

/**
 * Stub for chatkit ImageLoader.
 */
public abstract class ImageLoader {
    public abstract void loadImage(ImageView imageView, String url);

    public void loadImage(ImageView imageView, String url, Object payload) {
        loadImage(imageView, url);
    }

    public static ImageLoader create(com.bumptech.glide.RequestManager glide) {
        return new ImageLoader() {
            @Override public void loadImage(ImageView imageView, String url) {
                // stub
            }
        };
    }
}
