package com.example.vid_it;

import android.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SecurityHandler{
	
	public static final int ITERATIONS = 1000;

	public enum HashMethod {
	    PBKDF2(){
	        @Override
	        public String getHashString() { return "PBKDF2WithHmacSHA1"; }
	    }, SHA512(){
	        @Override
	        public String getHashString() { return "SHA-512"; }
	    }, SHA384() {
	        @Override
	        public String getHashString() { return "SHA-384"; }
	    }, SHA256() {
	        @Override
	        public String getHashString() { return "SHA-256"; }
	    }
	    , SHA1() {
	        @Override
	        public String getHashString() { return "SHA-1"; }
	    };

	    public abstract String getHashString();
	}

	public static String getAppropriateHash() {
	    String method = null;
	
	    if(isPBKDFAvailable())
	        method = HashMethod.PBKDF2.getHashString();
	    else if(isDigestAvailable( HashMethod.SHA512.getHashString()))
	        method = HashMethod.SHA512.getHashString();
	    else if(isDigestAvailable( HashMethod.SHA384.getHashString()))
	        method = HashMethod.SHA384.getHashString();
	    else if(isDigestAvailable( HashMethod.SHA256.getHashString()))
	        method = HashMethod.SHA256.getHashString();
	    else if(isDigestAvailable( HashMethod.SHA1.getHashString()))
	        method = HashMethod.SHA1.getHashString();
	
	    return method;
	}
	
	private static boolean isPBKDFAvailable() {
	    try { SecretKeyFactory.getInstance(HashMethod.PBKDF2.getHashString()); }
	    catch ( Exception notAvailable) { return false; }
	    return true;
	}
	
	private static boolean isDigestAvailable( String method ) {
	    try { MessageDigest.getInstance( method ); }
	    catch ( Exception notAvailable ) { return false; }
	    return true;
	}
	
	public static String getHashedPassword( String method, String password ) {
	    String hashed;
	
	    if (HashMethod.PBKDF2.getHashString().equals(method)) {
	        hashed = generatePBKDF( password );
	    }
	    else {
	        hashed = password;
	        for ( int i = 0; i < ITERATIONS; i++ )
	            hashed = generateDigestPassword(password, method);
	    }
	    return hashed;
	}
	
	private static String generatePBKDF( String password ) {
	    // Generate a 512-bit key
	    final int outputKeyLength = 512;
	
	    char[] chars = new char[password.length()];
	    password.getChars( 0, password.length(), chars, 0 );
	    byte[] salt = "salt_on_client_is_funny".getBytes(); 
	    
	    byte[] hashedPassBytes = new byte[0];
	    try {
	        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance( HashMethod.PBKDF2.getHashString() );
	        KeySpec keySpec = new PBEKeySpec( chars, salt, ITERATIONS, outputKeyLength );
	
	        hashedPassBytes = secretKeyFactory.generateSecret(keySpec).getEncoded();
	    }
	    catch ( Exception shouldNotHappen ) { /*nothing should happen*/ }
	
	    return Base64.encodeToString( hashedPassBytes, Base64.DEFAULT );
	}
	
	private static String generateDigestPassword( String password, String algorithm ) {
	    byte[] digest = new byte[0];
	    byte[] buffer = password.getBytes();
	
	    try {
	        MessageDigest messageDigest = MessageDigest.getInstance( algorithm );
	        messageDigest.reset();
	        messageDigest.update( buffer );
	        digest = messageDigest.digest();
	    }
	    catch ( NoSuchAlgorithmException ex ) { /*nothing should happen*/ }
	
	    return Base64.encodeToString(digest, Base64.DEFAULT);
	}
}