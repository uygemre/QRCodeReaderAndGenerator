package com.uygemre.qrcode.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.uygemre.qrcode.R
import kotlinx.android.synthetic.main.fragment_premium_subscribe.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class PremiumSubscribeFragment : Fragment() {

    private var selectMonthly = false
    private var selectAnnual = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_premium_subscribe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        firstSelect()
        selectButtons()
    }

    private fun firstSelect() {
        selectMonthly = true
        selectAnnual = false
        btn_monthly_select.setBackgroundResource(R.drawable.shape_subscribe_selected)
        btn_monthly_select.setTextColor(resources.getColor(R.color.white))
        btn_monthly_select.text = resources.getString(R.string.unselect)
    }

    private fun selectButtons() {
        btn_monthly_select.setOnClickListener {
            if (!selectMonthly) {
                selectMonthly = true
                selectAnnual = false
                btn_monthly_select.setBackgroundResource(R.drawable.shape_subscribe_selected)
                btn_annual_select.setBackgroundResource(R.drawable.shape_subscribe_unselected)
                btn_monthly_select.setTextColor(resources.getColor(R.color.white))
                btn_annual_select.setTextColor(resources.getColor(R.color.caribbean_green))
                btn_monthly_select.text = resources.getString(R.string.unselect)
                btn_annual_select.text = resources.getString(R.string.select)
            } else {
                selectMonthly = false
                btn_monthly_select.setBackgroundResource(R.drawable.shape_subscribe_unselected)
                btn_monthly_select.setTextColor(resources.getColor(R.color.caribbean_green))
                btn_monthly_select.text = resources.getString(R.string.select)
            }
        }

        btn_annual_select.setOnClickListener {
            if (!selectAnnual) {
                selectAnnual = true
                selectMonthly = false
                btn_annual_select.setBackgroundResource(R.drawable.shape_subscribe_selected)
                btn_monthly_select.setBackgroundResource(R.drawable.shape_subscribe_unselected)
                btn_annual_select.setTextColor(resources.getColor(R.color.white))
                btn_monthly_select.setTextColor(resources.getColor(R.color.caribbean_green))
                btn_annual_select.text = resources.getString(R.string.unselect)
                btn_monthly_select.text = resources.getString(R.string.select)
            } else {
                selectAnnual = false
                btn_annual_select.setBackgroundResource(R.drawable.shape_subscribe_unselected)
                btn_annual_select.setTextColor(resources.getColor(R.color.caribbean_green))
                btn_annual_select.text = resources.getString(R.string.select)
            }
        }
    }

    private fun setupView() {
        tv_title.text = resources.getString(R.string.premium)
        img_toolbar.setImageResource(R.drawable.ic_premium)
        btn_back.setOnClickListener { requireActivity().onBackPressed() }
    }
}