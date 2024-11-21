package com.example.tranquangngoc_cao_myruns2.automatic

object Globals {
    // Buffer sizes
    const val ACCELEROMETER_BUFFER_CAPACITY = 2048
    const val ACCELEROMETER_BLOCK_CAPACITY = 64

    // Activity IDs
    const val ACTIVITY_ID_STANDING = 0
    const val ACTIVITY_ID_WALKING = 1
    const val ACTIVITY_ID_RUNNING = 2
    const val ACTIVITY_ID_OTHER = 2

    // Service states
    const val SERVICE_TASK_TYPE_COLLECT = 0
    const val SERVICE_TASK_TYPE_CLASSIFY = 1

    // Activity labels
    const val CLASS_LABEL_STANDING = "Standing"
    const val CLASS_LABEL_WALKING = "Walking"
    const val CLASS_LABEL_RUNNING = "Running"
    const val CLASS_LABEL_OTHER = "Other"

    // Feature related
    const val FEAT_FFT_COEF_LABEL = "fft_coef_"
    const val FEAT_MAX_LABEL = "max"
}