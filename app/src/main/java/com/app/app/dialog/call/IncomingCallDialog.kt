package com.app.app.dialog.call

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
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
private const val NAME_PARAM = "name"
private const val TEXT_PARAM = "text"

class IncomingCallDialog : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var name: String? = null
    private var text: String? = null
    val TAG = "manu"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            name = it.getString(NAME_PARAM)
            text = it.getString(TEXT_PARAM)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_incoming_call_dialog, container, false)

        val textView = view.findViewById<TextView>(R.id.incomingCallText)
        textView.text = "Appel entrant de Emmanuel"

        val hangup = view.findViewById<Button>(R.id.hangupButton)
        hangup.setOnClickListener{
            Log.e(TAG, "incomingCall : hangup")
            try {
                OngoingCall.hangup()
            } catch(e: Exception) {
                Log.e(TAG, "$e")
            }
            dismiss()
        }

        val reply = view.findViewById<Button>(R.id.replyButton)
        reply.setOnClickListener{
            Log.e(TAG, "incomingCall : reply")
            try {
                OngoingCall.answer()
            } catch(e: Exception) {
                Log.e(TAG, "$e")
            }
            dismiss()
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment IncomingCallDialog.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(name: String, text: String) =
            IncomingCallDialog().apply {
                arguments = Bundle().apply {
                    putString(NAME_PARAM, name)
                    putString(TEXT_PARAM, text)
                }
            }
    }
}