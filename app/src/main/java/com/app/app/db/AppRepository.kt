package com.app.app.db

import android.util.Log
import com.app.app.config.AppConfig
import com.app.app.model.App
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AppRepository {

    private val TAG = "App Repository manu"
    private val db = Firebase.firestore

    fun findApp() {
        db.collection(AppConfig.COLLECTION).document(AppConfig.APP_ID).get()
            .addOnSuccessListener { appDoc ->
                //var a: App? = app.toObject(App)
                Log.e(TAG, "appDoc data ${appDoc.data}")
                val app = appDoc.toObject(App::class.java)

                if (app != null) {
                    app.uid = appDoc.id
                    Log.e(TAG, "app $app")
                    Log.e(TAG, "app.alarms ${app.alarms}")
                } else {
                    Log.e(TAG, "app model is null")
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "error while loading app")
                println("found app $it")
            }
    }

    fun findAlarms(): Task<QuerySnapshot> {
        return db.collection(AppConfig.COLLECTION)
            .document(AppConfig.APP_ID)
            .collection(AppConfig.ALARM_COLLECTION)
            .get()
                /*
            .addOnSuccessListener { app ->
                app.documents.map { alarmDoc -> Log.e(TAG, "app alarm doc ${alarmDoc.data}") }
            }
            .addOnFailureListener {
                Log.e(TAG, "error while loading app alarms $it")
                println("found app $it")
            } */
    }

    fun findEmergencyContact(): Task<DocumentSnapshot> {
        return db.collection(AppConfig.COLLECTION)
            .document(AppConfig.APP_ID)
            .collection(AppConfig.CLIENT_COLLECTION)
            .document(AppConfig.EMERGENCY_CONTACT_DOC)
            .get()
                /*
            .addOnSuccessListener { contactDoc ->
                val contact = contactDoc.data
                Log.e(TAG, "app emerg contact doc $contact")
            }
            .addOnFailureListener {
                Log.e(TAG, "error while loading app clients $it")
                println("found app $it")
            } */
    }

    fun findClients(): Task<QuerySnapshot> {
        return db.collection(AppConfig.COLLECTION)
            .document(AppConfig.APP_ID)
            .collection(AppConfig.CLIENT_COLLECTION)
            .get()
                /*
            .addOnSuccessListener { app ->
                app.documents.map { clientDoc -> Log.e(TAG, "app client doc ${clientDoc.data}") }
            }
            .addOnFailureListener {
                Log.e(TAG, "error while loading app clients $it")
                println("found app $it")
            } */
    }

    fun saveApp(app: App) {

    }

}