package com.app.app.dialog.call

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.app.app.R
import com.app.app.service.call.CallService
import com.app.app.service.call.OngoingCall
import java.lang.Exception

private const val TEXT_PARAM = "text"
private const val NAME_PARAM = "name"
private const val OPTIONS_PARAM = "options"

class CallDialog : DialogFragment() {
    var text: String = ""
    var name: String = ""
    var options: Int = 1
    val TAG = "CallDialog manu"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            name = it.getString(NAME_PARAM).toString()
            text = it.getString(TEXT_PARAM).toString()
            options = it.getInt(OPTIONS_PARAM)
            //Log.e(TAG, "call dialog setting name $name")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_call_dialog, container, false)

        updateUI(view, name, text, options)

        return view
    }

    public fun updateUI(name: String, text: String, options: Int) {
        view?.let { updateUI(it, name, text, options) }
    }

    fun updateUI(view: View, name: String, text: String, options: Int) {
        //Log.e(TAG, "updating with $name $text")
        view.findViewById<TextView>(R.id.callDialogText)?.text = text
        view.findViewById<TextView>(R.id.callDialogName)?.text = name

        val hangup = view.findViewById<Button>(R.id.hangupButton)
        hangup.setOnClickListener{
            //Log.e(TAG, "incomingCall : hangup")
            try {
                CallService.hangup()
            } catch(e: Exception) {
                Log.e(TAG, "$e")
            }
            dismiss()
        }

        val reply = view.findViewById<Button>(R.id.replyButton)

        // can only refuse
        if (options == 1) {
            (hangup.layoutParams as LinearLayout.LayoutParams).weight = 19.0F
            hangup.textSize = 48F

            val separateDiv = view.findViewById<View>(R.id.separationDiv)
            separateDiv.visibility = View.GONE

            reply.visibility = View.GONE
        } else {
            reply.setOnClickListener{
                //Log.e(TAG, "incomingCall : reply")
                try {
                    CallService.answer()
                } catch(e: Exception) {
                    Log.e(TAG, "$e")
                }
            }
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(name: String, text: String, options: Int) =
            CallDialog().apply {
                arguments = Bundle().apply {
                    putString(TEXT_PARAM, text)
                    putString(NAME_PARAM, name)
                    putInt(OPTIONS_PARAM, options)
                }
            }
    }
}