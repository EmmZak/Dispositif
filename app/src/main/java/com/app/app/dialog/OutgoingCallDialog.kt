package com.app.app.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.app.app.R
import com.app.app.service.call.OngoingCall
import java.lang.Exception

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val TEXT_PARAM = "text"
private const val NAME_PARAM = "name"

class OutgoingCallDialog : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var text: String? = null
    private var name: String? = null
    val TAG = "manu"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            name = it.getString(NAME_PARAM)
            text = it.getString(TEXT_PARAM)
            Log.e(TAG, "call dialog setting name $name")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_outgoing_call_dialog, container, false)

        val nameView = view.findViewById<TextView>(R.id.callDialogName)
        nameView.text = "$name"

        val textView = view.findViewById<TextView>(R.id.callDialogText)
        textView.text = "$text"

        val hangupButton = view.findViewById<Button>(R.id.hangupButton)
        hangupButton.setOnClickListener {
            Log.e(TAG, "hangupButton clicked")
            try {
                OngoingCall.hangup()
            } catch(e: Exception) {
                Log.e(TAG, "$e")
            }
            dismiss()
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(name: String, text: String) =
            OutgoingCallDialog().apply {
                arguments = Bundle().apply {
                    putString("name", name)
                    putString("text", text)
                }
            }
    }
}