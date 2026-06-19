package minefarts.smarttube.ui;

class AspectRatioUpdateDispatcher implements Runnable {

    private float targetAspectRatio;
    private float naturalAspectRatio;
    private boolean aspectRatioMismatch;
    private boolean isScheduled;

    final AspectRatioFrameLayout arfl;

    AspectRatioUpdateDispatcher(AspectRatioFrameLayout arfl) {
        this.arfl = arfl;
    }

    public void scheduleUpdate(
        float targetAspectRatio, 
        float naturalAspectRatio
    ) {
        this.targetAspectRatio = targetAspectRatio;
        this.naturalAspectRatio = naturalAspectRatio;

        if (!isScheduled) {
            isScheduled = true;
            arfl.post(this);
        }
    }

    @Override
    public void run() {

        isScheduled = false;

        if (arfl.aspectRatioListener == null) return;
        
        arfl.aspectRatioListener.onAspectRatioUpdated(
            targetAspectRatio, 
            naturalAspectRatio, 
            true
        );

    }

}