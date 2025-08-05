package com.example.talent_link.ui.Mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.talent_link.R

class MyPageFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       var view = inflater.inflate(R.layout.fragment_my_page, container, false)


        return view
    }

}