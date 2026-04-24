
package com.eclipsesource.v8.debug;

/**
 * An enumeration of possible Step Actions. A step action indicates to the
 * debugger how to proceed with the next step.
 */
public enum StepAction {
    STEP_OUT(0), STEP_NEXT(1), STEP_IN(2), STEP_FRAME(3);
    int index;

    StepAction(final int index) {
        this.index = index;
    }

}