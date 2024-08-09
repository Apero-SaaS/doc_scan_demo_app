package com.apero.app.poc_ml_docscan.scan.common.util

public typealias AnalyticsParam = Pair<String, String>

public interface AnalyticsReporter {
    public fun reportEvent(name: String, vararg params: AnalyticsParam)
}
