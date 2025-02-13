package dev.gitlive.firebase.perf.metrics

/** Trace allows you to set beginning and end of a certain action in your app. */
public expect class Trace {
    /** Starts this trace. */
    public fun start()

    /** Stops this trace. */
    public fun stop()

    /**
     * Gets the value of the metric with the given name in the current trace. If a metric with the
     * given name doesn't exist, it is NOT created and a 0 is returned. This method is atomic.
     *
     * @param metricName Name of the metric to get. Requires no leading or trailing whitespace, no
     *     leading underscore '_' character, max length is 100 characters.
     * @return Value of the metric or 0 if it hasn't yet been set.
     */
    public fun getLongMetric(metricName: String): Long

    /**
     * Atomically increments the metric with the given name in this trace by the incrementBy value. If
     * the metric does not exist, a new one will be created. If the trace has not been started or has
     * already been stopped, returns immediately without taking action.
     *
     * @param metricName Name of the metric to be incremented. Requires no leading or trailing
     *     whitespace, no leading underscore [_] character, max length of 100 characters.
     * @param incrementBy Amount by which the metric has to be incremented.
     */
    public fun incrementMetric(metricName: String, incrementBy: Long)

    /**
     * Sets the value of the metric with the given name in this trace to the value provided. If a
     * metric with the given name doesn't exist, a new one will be created. If the trace has not been
     * started or has already been stopped, returns immediately without taking action. This method is
     * atomic.
     *
     * @param metricName Name of the metric to set. Requires no leading or trailing whitespace, no
     *     leading underscore '_' character, max length is 100 characters.
     * @param value The value to which the metric should be set to.
     */
    public fun putMetric(metricName: String, value: Long)
}
