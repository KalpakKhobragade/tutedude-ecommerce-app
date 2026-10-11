package com.example.ecommerceapp.util

import java.util.Locale

fun formatPrice(price: Double): String = String.format(Locale.US, "\$%.2f", price)
