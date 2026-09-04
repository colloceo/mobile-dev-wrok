package com.example.smartexpensemanager.data.mpesa

import com.example.smartexpensemanager.data.local.entity.TransactionType

/**
 * Best-effort regex parser for Safaricom M-Pesa confirmation messages, e.g.:
 *  "RGH2X7K9L1 Confirmed. Ksh500.00 sent to JOHN DOE 0712345678 on 25/8/26 ..."
 *  "RGH2X7K9L2 Confirmed.You have received Ksh1,000.00 from JANE DOE 0798765432 ..."
 *  "RGH2X7K9L3 Confirmed. Ksh200.00 paid to SUPERMARKET LTD. on 25/8/26 ..."
 *  "RGH2X7K9L4 Confirmed.You have withdrawn Ksh2,000.00 from 123456 - AGENT NAME ..."
 *  "RGH2X7K9L5 Confirmed. Ksh100.00 airtime purchased ..."
 *
 * Message wording varies by transaction type and Safaricom updates its templates
 * occasionally, so this intentionally fails closed (returns null) rather than
 * guessing on formats it doesn't recognise.
 */
object MpesaMessageParser {

    private val codeRegex = Regex("""\b([A-Z0-9]{8,12})\s+[Cc]onfirmed""")
    private val amountRegex = Regex("""Ksh\s?([\d,]+\.\d{2})""")
    private val sentToRegex = Regex("""sent to ([A-Za-z0-9 .&'-]+?)\s+(?:\d{9,}|on )""", RegexOption.IGNORE_CASE)
    private val paidToRegex = Regex("""paid to ([A-Za-z0-9 .&'-]+?)\s+on """, RegexOption.IGNORE_CASE)
    private val receivedFromRegex = Regex("""from ([A-Za-z0-9 .&'-]+?)\s+(?:\d{9,}|on )""", RegexOption.IGNORE_CASE)

    fun parse(rawText: String, receivedAtMillis: Long = System.currentTimeMillis()): MpesaTransaction? {
        val text = rawText.trim()
        if (!text.contains("Ksh", ignoreCase = false)) return null

        val code = codeRegex.find(text)?.groupValues?.get(1) ?: return null
        val amountText = amountRegex.find(text)?.groupValues?.get(1) ?: return null
        val amount = amountText.replace(",", "").toDoubleOrNull() ?: return null

        val lower = text.lowercase()
        val (type, counterparty) = when {
            lower.contains("received") ->
                TransactionType.INCOME to (receivedFromRegex.find(text)?.groupValues?.get(1)?.trim() ?: "M-Pesa")
            lower.contains("sent to") ->
                TransactionType.EXPENSE to (sentToRegex.find(text)?.groupValues?.get(1)?.trim() ?: "M-Pesa transfer")
            lower.contains("paid to") ->
                TransactionType.EXPENSE to (paidToRegex.find(text)?.groupValues?.get(1)?.trim() ?: "M-Pesa payment")
            lower.contains("withdraw") ->
                TransactionType.EXPENSE to "M-Pesa withdrawal"
            lower.contains("airtime") ->
                TransactionType.EXPENSE to "Airtime"
            else -> return null
        }

        return MpesaTransaction(
            code = code,
            type = type,
            amount = amount,
            counterparty = counterparty.trim('.', ' '),
            dateMillis = receivedAtMillis
        )
    }
}
