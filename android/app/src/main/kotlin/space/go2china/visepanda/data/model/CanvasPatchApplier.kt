package space.go2china.visepanda.data.model

object CanvasPatchApplier {

    fun apply(current: TripState, patch: CanvasPatch): TripState {
        val alertMap = LinkedHashMap<String, ButlerAlert>()
        current.alerts.forEach { alert ->
            alertMap[alert.dedupeKey()] = alert
        }
        patch.butlerAlerts.orEmpty().forEach { alert ->
            alertMap[alert.dedupeKey()] = alert
        }

        return current.copy(
            summary = current.summary.merge(patch.tripSummary),
            days = patch.days ?: current.days,
            alerts = alertMap.values.toList(),
            lastUpdatedReason = patch.reason,
        )
    }

    private fun TripSummary.merge(patch: TripSummaryPatch?): TripSummary {
        if (patch == null) return this
        return copy(
            title = patch.title ?: title,
            durationDays = patch.durationDays ?: durationDays,
            pace = patch.pace ?: pace,
            travelerStyle = patch.travelerStyle ?: travelerStyle,
            destinations = patch.destinations ?: destinations,
            confidence = patch.confidence ?: confidence,
        )
    }

    private fun ButlerAlert.dedupeKey(): String = "${type.name}:$title"
}
