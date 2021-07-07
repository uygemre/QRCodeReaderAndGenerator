package com.uygemre.qrcode.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.uygemre.qrcode.R
import kotlinx.android.synthetic.main.layout_toolbar.*

// Created by Emre UYGUN on 7/5/21
// Copyriht © Demiroren Teknoloji. All rights reserved.

class PremiumSubscribeFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_premium_subscribe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_back.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }
}