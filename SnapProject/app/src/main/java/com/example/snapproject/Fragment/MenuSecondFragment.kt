package com.example.snapproject.Fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.snapproject.OnChildButtonClickListener
import com.example.snapproject.R
import com.example.snapproject.databinding.FragmentMenuSecondBinding

class MenuSecondFragment : Fragment() {
    private var _binding: FragmentMenuSecondBinding? = null
    private val binding get() = _binding!!

    private var listener: OnChildButtonClickListener? = null // 버튼 클릭 리스너

    companion object {
        fun newInstance() = MenuSecondFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // 뷰 바인딩
        _binding = FragmentMenuSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnChildButtonClickListener) { // MainActivity의 listener 연결
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
            // MainActivity의 listner 호출 (소비기한 버튼 클릭 -> ListFragment로 화면 전환)
            btnDate.setOnClickListener {
                listener?.onChildButtonClicked(R.id.action_homeFragment_to_listFragment)
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
