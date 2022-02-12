package com.app.app

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.app.app.service.call.OngoingCall
import java.lang.Exception

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM = "name"

/**
 * A simple [Fragment] subclass.
 * Use the [CallDialog.newInstance] factory method to
 * create an instance of this fragment.
 */
class CallDialog : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var name: String? = null
    val TAG = "manu"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            name = it.getString(ARG_PARAM)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_call_dialog, container, false)

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

    fun stopCallDialog(view: View) {
        Log.e(TAG, "dialog stopCAll")
        dismiss()
    }

    companion object {
        @JvmStatic
        fun newInstance(name: String) =
            CallDialog().apply {
                arguments = Bundle().apply {
                    putString(name, name)
                }
            }
    }
}