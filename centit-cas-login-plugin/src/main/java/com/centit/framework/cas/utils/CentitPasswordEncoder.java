package com.centit.framework.cas.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Created by codefan on 17-1-20.
 */
public class CentitPasswordEncoder implements PasswordEncoder {

    private BCryptPasswordEncoder passwordEncoder ;

    public CentitPasswordEncoder() {
        this(11);
    }

    public CentitPasswordEncoder(int strength) {
        if(strength<5 ||strength >31){
            passwordEncoder = new BCryptPasswordEncoder(11);
        }else {
            passwordEncoder = new BCryptPasswordEncoder(strength);
        }
    }

    /**
     * Encode the raw password. Generally, a good encoding algorithm applies a SHA-1 or
     * greater hash combined with an 8-byte or greater randomly generated salt.
     *
     * @param rawPassword  rawPassword
     * @return encode
     */
    @Override
    public String encode(CharSequence rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Verify the encoded password obtained from storage matches the submitted raw
     * password after it too is encoded. Returns true if the passwords match, false if
     * they do not. The stored password itself is never decoded.
     *
     * @param rawPassword     the raw password to encode and match
     * @param encodedPassword the encoded password from storage to compare with
     * @return true if the raw password, after encoding, matches the encoded password from
     * storage
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword,encodedPassword);
    }
}
