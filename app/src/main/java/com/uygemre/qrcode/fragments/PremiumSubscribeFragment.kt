package com.uygemre.qrcode.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.uygemre.qrcode.R
import com.uygemre.qrcode.constants.PrefConstants
import com.uygemre.qrcode.helpers.LocalPrefManager
import com.uygemre.qrcode.helpers.PurchaseHelper
import kotlinx.android.synthetic.main.fragment_premium_subscribe.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class PremiumSubscribeFragment : Fragment() {

    lateinit var localPrefManager: LocalPrefManager

    private var selectMonthly = false
    private var selectAnnual = false

    private var purchaseHelper: PurchaseHelper? = null
    private var productsSkuDetails: List<SkuDetails>? = null
    private var skuDetails: SkuDetails? = null
    private var isAlreadyPurchased = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_premium_subscribe, container, false)
    }

    override fun onResume() {
        super.onResume()
        purchaseHelper = PurchaseHelper(requireActivity(), purchaseHelperListener)
        purchaseHelper?.setupPurchase()
        setupView()
    }

    override fun onDestroy() {
        super.onDestroy()
        purchaseHelper?.destroyBilling()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        localPrefManager = LocalPrefManager(requireContext())
        //setupView()
        firstSelect()
        selectButtons()
    }

    private val purchaseHelperListener = object : PurchaseHelper.PurchaseHelperListener {
        override fun getProductList(products: List<SkuDetails>) {
            if (products.isNotEmpty()) {
                tv_monthly_price.text = products.getOrNull(1)?.price
                tv_annual_price.text = products.getOrNull(0)?.price

                productsSkuDetails = products
                skuDetails = productsSkuDetails?.getOrNull(1)
            }
        }

        override fun getAlreadyExistProductList(alreadyProducts: List<Purchase>?) {
            isAlreadyPurchased = !alreadyProducts.isNullOrEmpty()
        }

        override fun purchaseSuccess() {
            localPrefManager.push(PrefConstants.PREF_IS_PREMIUM, true)
            requireActivity().onBackPressed()
        }

        override fun purchaseFailed(responseCode: Int?) {
            if (responseCode == 7 || responseCode == 0) {
                localPrefManager.push(PrefConstants.PREF_IS_PREMIUM, true)
            } else
                localPrefManager.push(PrefConstants.PREF_IS_PREMIUM, false)
        }
    }

    private fun firstSelect() {
        selectMonthly = true
        selectAnnual = false
        btn_monthly_select.setBackgroundResource(R.drawable.shape_subscribe_selected)
        btn_monthly_select.setTextColor(resources.getColor(R.color.white))
        btn_monthly_select.text = resources.getString(R.string.unselect)
        skuDetails = productsSkuDetails?.getOrNull(1)
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

                skuDetails = productsSkuDetails?.getOrNull(1)
            } else {
                selectMonthly = false
                btn_monthly_select.setBackgroundResource(R.drawable.shape_subscribe_unselected)
                btn_monthly_select.setTextColor(resources.getColor(R.color.caribbean_green))
                btn_monthly_select.text = resources.getString(R.string.select)

                skuDetails = null
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

                skuDetails = productsSkuDetails?.getOrNull(0)
            } else {
                selectAnnual = false
                btn_annual_select.setBackgroundResource(R.drawable.shape_subscribe_unselected)
                btn_annual_select.setTextColor(resources.getColor(R.color.caribbean_green))
                btn_annual_select.text = resources.getString(R.string.select)

                skuDetails = null
            }
        }
    }

    private fun setupView() {
        tv_title.text = resources.getString(R.string.premium)
        tv_premium_description.text = getString(R.string.premium_description)
        tv_premium_subtext.text = getString(R.string.premium_subtext)
        img_toolbar.setImageResource(R.drawable.ic_premium)
        btn_back.setOnClickListener { requireActivity().onBackPressed() }

        btn_subscribe.setOnClickListener {
            if (skuDetails == null) {
                Toast.makeText(
                    context,
                    getString(R.string.premium_select_package),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                skuDetails?.let { _skuDetails ->
                    purchaseHelper?.purchase(_skuDetails)
                }
            }
        }

        btn_restore.setOnClickListener {
            if (isAlreadyPurchased) {
                Toast.makeText(
                    context,
                    if (localPrefManager.pull(
                            PrefConstants.PREF_IS_PREMIUM,
                            false
                        )
                    ) getString(R.string.restore_is_already_have) else getString(R.string.restore_is_successful),
                    Toast.LENGTH_LONG
                ).show()
                localPrefManager.push(PrefConstants.PREF_IS_PREMIUM, true)
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.restore_is_failed),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}