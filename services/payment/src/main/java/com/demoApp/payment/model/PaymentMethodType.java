package com.demoApp.payment.model;

import lombok.Getter;

/**
 * Enum for specific payment method types supported by Stripe
 */
@Getter
public enum PaymentMethodType {
    // Card types
    VISA("card", "visa"),
    MASTERCARD("card", "mastercard"),
    AMEX("card", "amex"),
    DISCOVER("card", "discover"),
    JCB("card", "jcb"),
    DINERS("card", "diners"),
    
    // UPI types
    GOOGLE_PAY("upi", "google_pay"),
    PHONE_PE("upi", "phone_pe"),
    PAYTM("upi", "paytm"),
    BHIM("upi", "bhim"),
    AMAZON_PAY("upi", "amazon_pay"),
    
    // Netbanking
    HDFC("netbanking", "hdfc"),
    SBI("netbanking", "sbi"),
    ICICI("netbanking", "icici"),
    AXIS("netbanking", "axis"),
    
    // Wallet 
    PAYTM_WALLET("wallet", "paytm"),
    MOBIKWIK("wallet", "mobikwik"),
    FREECHARGE("wallet", "freecharge"),
    
    // Other methods
    PAYPAL("paypal", "paypal"),
    BANK_TRANSFER("bank_transfer", "bank_transfer");
    
    private final String stripeMethod;
    private final String subtype;
    
    PaymentMethodType(String stripeMethod, String subtype) {
        this.stripeMethod = stripeMethod;
        this.subtype = subtype;
    }
    
    /**
     * Get the Stripe payment method string
     */
    public String getStripeMethod() {
        return stripeMethod;
    }
    
    /**
     * Get the subtype or brand of the payment method
     */
    public String getSubtype() {
        return subtype;
    }
} 