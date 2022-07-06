package com.dicoding.todoapp.setting

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.work.Data
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.dicoding.todoapp.R
import com.dicoding.todoapp.notification.NotificationWorker
import com.dicoding.todoapp.utils.NOTIFICATION_CHANNEL_ID
import com.dicoding.todoapp.utils.NOTIFICATION_CONTENT_ID
import java.util.concurrent.TimeUnit

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private lateinit var workManager: WorkManager
        private lateinit var periodicWorkRequest: PeriodicWorkRequest

        companion object{
            val TAG = "SettingsFragment"
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            workManager = WorkManager.getInstance(requireContext())
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val prefNotification = findPreference<SwitchPreference>(getString(R.string.pref_key_notify))
            prefNotification?.setOnPreferenceChangeListener { _, newValue ->
                //TODO 13 : Schedule and cancel daily reminder using WorkManager with data channelName done
                if(newValue == true){
                    startPeriodicTask()
                }
                else{
                    try {
                        workManager.getWorkInfoByIdLiveData(periodicWorkRequest.id)
                            .observe(viewLifecycleOwner) { workInfo ->
                                val status = workInfo.state.name
                                Log.d(TAG, "WorkManager Status : $status")
                                if (workInfo.state == WorkInfo.State.ENQUEUED) {
                                    cancelPeriodicTask()
                                    Toast.makeText(requireContext(), "Task reminder has been cancelled", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } catch (e: Exception){
                        Log.e(TAG, "Cancelling Reminder Failed : ${e.message}")
                    }
                }
                true
            }
        }

        private fun startPeriodicTask() {
            val data = Data.Builder()
                .putString(NOTIFICATION_CHANNEL_ID, "notification_id_reminder")
                .putString(NOTIFICATION_CONTENT_ID, getString(R.string.notify_content))
                .build()

            periodicWorkRequest = PeriodicWorkRequest.Builder(
                NotificationWorker::class.java,
                1, TimeUnit.DAYS
            ).setInputData(data).build()

            workManager.enqueue(periodicWorkRequest)
            workManager.getWorkInfoByIdLiveData(periodicWorkRequest.id)
                .observe(viewLifecycleOwner) { workInfo ->
                    val status = workInfo.state.name
                    Log.d(TAG, "WorkManager Status : $status")
                    if (workInfo.state == WorkInfo.State.ENQUEUED) {
                        Log.d(TAG, "Reminder has been enqueued")
                    }
                }
        }

        private fun cancelPeriodicTask() {
            try {
                workManager.cancelWorkById(periodicWorkRequest.id)
            }catch (e : Exception){
                Log.e(TAG, "Cancel Periodic Work Failed : ${e.message}")
            }
        }
    }
}