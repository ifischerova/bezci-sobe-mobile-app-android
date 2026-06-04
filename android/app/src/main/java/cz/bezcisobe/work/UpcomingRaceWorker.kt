package cz.bezcisobe.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
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
        val ctx = applicationContext
        val channelId = "upcoming_races"
        val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mgr.createNotificationChannel(
                NotificationChannel(channelId, "Závody", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val notification = NotificationCompat.Builder(ctx, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Blížící se závod")
            .setContentText("$name • $date")
            .setAutoCancel(true)
            .build()
        if (NotificationManagerCompat.from(ctx).areNotificationsEnabled()) {
            NotificationManagerCompat.from(ctx).notify(1001, notification)
        }
    }
}
