package io.github.mortuusars.exposure.world.level.storage;

public enum RequestedExposureStatus {
    NOT_REQUESTED,
    AWAITED,
    TIMED_OUT,
    INVALID_ID,
    NOT_FOUND,
    CANNOT_LOAD,
    SUCCESS,
    NEEDS_REFRESH;
}
