package com.example.snapproject.Fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.snapproject.OnChildButtonClickListener
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentMenuFirstBinding

class MenuFirstFragment : Fragment() {
    private var _binding: FragmentMenuFirstBinding? = null
    private val binding get() = _binding!!

    private var listener: OnChildButtonClickListener? = null

    companion object {
        fun newInstance() = MenuFirstFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMenuFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnChildButtonClickListener) {
            listener = context
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() =
        with(binding) {
            btnCamera.setOnClickListener {
                listener?.onChildButtonClicked(R.id.action_homeFragment_to_cameraFragment)
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
