package com.fhtrier.voiceDiary;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

public class CipherStreamGen
{

    //http://openbook.galileocomputing.de/java7/1507_22_006.html#dodtp44355202-8b70-416d-a198-9553468eed73 10.01.2013
    
    private final static byte[] pass = "3w8]BMqj".getBytes();

    public static OutputStream getEncryptOutputStream(OutputStream outputStream)
    {
        try
        {
            Cipher c = Cipher.getInstance("ARCFOUR");
            Key k = new SecretKeySpec(pass, "ARCFOUR");
            c.init(Cipher.ENCRYPT_MODE, k);
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, c);
            return cipherOutputStream;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static InputStream getDecryptInputStream(InputStream inputStream)
    {
        try
        {
            Cipher c = Cipher.getInstance("ARCFOUR");
            Key k = new SecretKeySpec(pass, "ARCFOUR");
            c.init(Cipher.DECRYPT_MODE, k);
            CipherInputStream cipherInputStream = new CipherInputStream(inputStream, c);
            return cipherInputStream;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}