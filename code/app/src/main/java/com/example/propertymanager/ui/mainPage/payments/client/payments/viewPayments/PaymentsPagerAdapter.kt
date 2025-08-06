package com.example.propertymanager.ui.mainPage.payments.client.payments.viewPayments

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class PaymentsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    private val parent = fragment as FragmentPaymentTabs

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PendingPaymentsFragment.newInstance(
                parent.propertyId,
                parent.contractId,
                parent.payableItemId,
                parent.mode
            )
            1 -> ApprovedPaymentsFragment()
            else -> DeniedPaymentsFragment()
        }
    }
}

