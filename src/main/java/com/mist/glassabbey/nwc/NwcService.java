package com.mist.glassabbey.nwc;

import com.mist.glassabbey.nwc.dtos.InvoiceStatus;
import com.mist.glassabbey.nwc.dtos.MakeInvoiceResult;

import java.util.Map;

public interface NwcService {
    MakeInvoiceResult makeInvoice(NwcConn nwcConn, long amountSats, String memo);

    InvoiceStatus lookupInvoice(NwcConn nwcConn, String paymentHash);
}
