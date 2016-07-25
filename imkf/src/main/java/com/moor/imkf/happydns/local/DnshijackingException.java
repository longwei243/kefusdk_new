package com.moor.imkf.happydns.local;


import com.moor.imkf.happydns.DnsException;

/**
 * Created by bailong on 15/6/19.
 */
public class DnshijackingException extends DnsException {
    public DnshijackingException(String domain, String server) {
        super(domain, "has hijacked by " + server);
    }

    public DnshijackingException(String domain, String server, int ttl) {
        super(domain, "has hijacked by " + server + " ttl " + ttl);
    }
}
