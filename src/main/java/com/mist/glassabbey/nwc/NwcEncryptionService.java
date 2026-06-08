package com.mist.glassabbey.nwc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Slf4j
@Service
public class NwcEncryptionService {

    // TODO: replace with real AES-GCM encryption once NWC flow is working
    public String encrypt(String plaintext) {
        return plaintext;
    }

    // TODO: implement decrypt
    public String decrypt(String encoded) {
        return encoded;
    }
}
