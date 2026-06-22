package cz.bezcisobe.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cz.bezcisobe.R
import cz.bezcisobe.data.repository.RaceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Locale

@HiltWorker
class UpcomingRaceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: RaceRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            repository.refresh()
            val next = repository.observeRaces().first().firstOrNull { !it.isPast }
            if (next != null) notify(next.name, next.date)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun notify(name: String, date: String) {
        // Resolve notification strings against the user's selected per-app locale.
        // On API < 33 a background worker doesn't automatically pick up the app locale,
        // so we build a localized context explicitly.
        val ctx = localizedContext()
        val channelId = "upcoming_races"
        val mgr = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mgr.createNotificationChannel(
                NotificationChannel(channelId, ctx.getString(R.string.notif_channel_races), NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(ctx.getString(R.string.notif_upcoming_title))
            .setContentText("$name • $date")
            .setAutoCancel(true)
            .build()
        if (NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            NotificationManagerCompat.from(applicationContext).notify(1001, notification)
        }
    }

    private fun localizedContext(): Context {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return applicationContext
        val locale = locales[0] ?: return applicationContext
        Locale.setDefault(locale)
        val config = Configuration(applicationContext.resources.configuration).apply { setLocale(locale) }
        return applicationContext.createConfigurationContext(config)
    }
}
