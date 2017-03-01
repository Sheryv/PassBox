package com.sheryv.PassBox;

import android.os.Build;
import android.os.Process;
import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Sheryv on 24.09.2015.
 */
public class ApiCrypter {

    private static final boolean ALLOW_BROKEN_PRNG = false;
    private static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String CIPHER = "AES";
    private static final String ENCODING = "UTF-8";
    private static final String RANDOM_ALGORITHM = "SHA1PRNG";
    private static final int IV_LENGTH_BYTES = 16;
    private byte[] iv    ;
    private byte[] secretkey;
    private IvParameterSpec ivspec;
    private SecretKeySpec keyspec;
    public byte hmacLenght;
    private Cipher cipher;
    static final AtomicBoolean prngFixed = new AtomicBoolean(false);

    public ApiCrypter(byte[] secretkey, byte hmacLenght)
    {
        this.hmacLenght = hmacLenght;
        this.secretkey = secretkey;
        try {
            this.iv = generateIv();
            ivspec = new IvParameterSpec(iv);
            keyspec = new SecretKeySpec(secretkey, CIPHER);
            cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        } catch (NoSuchAlgorithmException e) {
            Sh.d(e.getMessage());
        } catch (NoSuchPaddingException e) {
            Sh.d(e.getMessage());
        } catch (GeneralSecurityException e)
        {
            Sh.d(e.getMessage());
        }
    }
    public byte[] getIv()
    {
        return ivspec.getIV();
    }

    public byte[] encrypt(String text) throws Exception
    {
        if(text == null || text.length() == 0) {
            throw new Exception("Empty string");
        }
        byte[] encrypted = null;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            encrypted = cipher.doFinal(text.getBytes(ENCODING));
        }
        catch (Exception e) {
            throw new Exception("[encrypt] " + e.getMessage());
        }

        String encc = Base64.encodeToString(encrypted, Base64.DEFAULT);
        return encrypted;
    }

    public byte[] decrypt(String code) throws Exception
    {
        if(code == null || code.length() == 0) {
            throw new Exception("Empty string");
        }
        byte[] decrypted = null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
            decrypted = cipher.doFinal(Base64.decode(code, Base64.DEFAULT));               /// changed   hexToBytes(code)
        }
        catch (Exception e) {
            throw new Exception("[decrypt] " + e.getMessage());
        }
        return decrypted;
    }

    public String encryptNoIv(String text) throws Exception
    {
        if(text == null || text.length() == 0) {
            throw new Exception("Empty string");
        }
        byte[] encrypted = null;
        Sh.d("pass enc: "+toBase64(secretkey));
        try {
            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            encrypted = cipher.doFinal(text.getBytes(ENCODING));
        }
        catch (Exception e) {
            throw new Exception("[encrypt] " + e.getMessage());
        }
        Sh.d("encrypting: -- done");

       // byte[] f = concat(iv, encrypted);
        String len = ivmacToString(iv);
        Sh.d("iv,concat: "+Sh.ins.printd(iv)+" iv leng arr: "+iv.length+" | encode: "+len+" "+len.length());
        String hma = ivmacToString(hmacSha1(encrypted));
        Sh.d("encrypting mac: "+hma+" | hmac len: "+hma.length());
        String r = toBase64(encrypted);             // changed toBase64(f)
        Sh.d("cipher:"+r.length()+" | "+r+" --");
        return new StringBuilder().append(hma).append(len).append(r).toString();
    }

    public String decryptNoIv(String encryptedCipher) throws Exception
    {
        if(encryptedCipher == null || encryptedCipher.length() == 0) {
            throw new Exception("Empty string");
        }
        String mac = encryptedCipher.substring(0,hmacLenght-1);
        String ivEnc = encryptedCipher.substring(hmacLenght-1);
        String ivCoded = ivEnc.substring(0, 22);
        ivEnc = ivEnc.substring(22);
        Sh.d("pass dec: "+toBase64(secretkey));
        Sh.d("encry: mac, ivCoded: " + mac + " | " + ivCoded + " " + ivCoded.length()+" | ciph enc: "+ivEnc.length());
        byte[] cipherl = fromBase64(ivEnc);
        Sh.d("getbytes| ivenc "+ivEnc);
       // byte[] ivEn = ivEnc.getBytes(ENCODING);                /// changed fromBase64(ivEnc)
       // Sh.d("dec: from64  | iv,enc: "+Sh.ins.printd(ivEn));
        ivEnc = null;
        byte[] hmh = hmacSha1(cipherl);
        Sh.d("mac array ");
        String hm = ivmacToString(hmh);
        Sh.d("new mac done: "+hm+" | "+hm.length());
        if (!mac.equals(hm))
        {
            throw new Exception("Hashes not equal. File modified");
            //Sh.msgl(Sh.ins.getString(R.string.data_altered));
        }
        hm = null;
        mac = null;
/*        byte[] ivv  = new byte[16];
        byte[] text  = new byte[cipher.length-16];
        for (int i = 0; i < cipher.length; i++)
        {
            if (i<16)
                ivv[i] = cipher[i];
            else
                text[i-16] = cipher[i];
        }*/
        byte[] ivv = ivmacFromString(ivCoded);
        Sh.d("de: iv: "+Sh.ins.printd(ivv)+" txt.len: "+cipherl.length);
        byte[] decrypted = null;
        try {
            this.cipher.init(Cipher.DECRYPT_MODE, keyspec, new IvParameterSpec(ivv));
            decrypted = this.cipher.doFinal(cipherl);               /// changed   hexToBytes(code)
        }
        catch (Exception e) {
            throw new Exception("[decrypt no iv] " + e.getMessage());
        }
        cipherl = null;
        Sh.d("decrypt complete");

        return new String(decrypted, ENCODING);
    }

    public static byte[] concat(byte[] a, byte[] b) {

       byte[] c = ByteBuffer.allocate(a.length + b.length).put(a).put(b).array();
/*        byte c[] = new byte[a.length+b.length];
        Sh.d("concat after allocating");
        for (int i = 0; i < c.length; i++)
        {
            if (i<a.length)
                c[i] = a[i];
            else
                c[i] = b[i-16];
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(a.length+b.length);
        try
        {
            outputStream.write( a );
            outputStream.write( b );
        }
        catch (IOException e)
        {
            Sh.de(e);
            e.printStackTrace();
        }

        byte c[] = outputStream.toByteArray( );*/

/*        int aLen = a.length;
        int bLen = b.length;
        byte[] c= new byte[aLen+bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);*/
        return c;
    }

    public byte[] decryptFromBytes(byte[] code) throws Exception
    {
        if(code == null || code.length == 0) {
            throw new Exception("Empty string");
        }
        byte[] decrypted = null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
            decrypted = cipher.doFinal(code);
        }
        catch (Exception e) {
            throw new Exception("[decrypt]3 " + e.getMessage());
        }
        return decrypted;
    }

    private static String bytesToHex(byte[] data)
    {
        if (data==null) {
            return null;
        }
        int len = data.length;
        String str = "";
        for (int i=0; i<len; i++) {
            if ((data[i]&0xFF)<16) {
                str = str + "0" + java.lang.Integer.toHexString(data[i]&0xFF);
            }
            else {
                str = str + java.lang.Integer.toHexString(data[i]&0xFF);
            }
        }
        return str;
    }

    private static byte[] hexToBytes(String str) {
        if (str==null) {
            return null;
        }
        else if (str.length() < 2) {
            return null;
        }
        else {
            int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i=0; i<len; i++) {
                buffer[i] = (byte) Integer.parseInt(str.substring(i*2,i*2+2),16);
            }
            return buffer;
        }
    }


    /**
     * Create Hmac-sha1 and return as base64-encoded string
     * ===============================
     * PHP:  hash_hmac("sha1", "helloworld", "mykey")
     *  <=^ mkey - key used as secret key
     *
     * @param value data
     * @return base64-encoded string
     */
    public String hmacSha1(String value) {
        try {
            // Get an hmac_sha1 key from the raw key bytes
            byte[] keyBytes = secretkey;
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(fromBase64(value));
            hmacLenght = (byte) rawHmac.length;
         //   Sh.d("h: "+rawHmac.length);
            // Convert raw bytes to Hex
/*            byte[] hexBytes = new Hex().encode(rawHmac);
            //  Covert array of Hex bytes to a String
            return new String(hexBytes, ENCODING);*/
            return ivmacToString(rawHmac);
        } catch (Exception e) {
            Sh.d("hmac exc :"+e.getMessage());
            throw new RuntimeException(e);
        }
    }
    /**
     * Create Hmac-sha1 and return as base64-encoded string
     * ===============================
     * PHP:  hash_hmac("sha1", "helloworld", "mykey")
     *  <=^ mkey - key used as secret key
     *
     * @param value data
     * @return base64-encoded string
     */
    public byte[] hmacSha1(byte[] value) {
        try {
            // Get an hmac_sha1 key from the raw key bytes
            byte[] keyBytes = secretkey;
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");
            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(value);
         //   hmacLenght = (byte) rawHmac.length;
            //   Sh.d("h: "+rawHmac.length);
            // Convert raw bytes to Hex
/*            byte[] hexBytes = new Hex().encode(rawHmac);
            //  Covert array of Hex bytes to a String
            return new String(hexBytes, ENCODING);*/
            return rawHmac;
            //todo changed ^
        } catch (Exception e) {
            Sh.d("hmac exc :"+e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Change encrypted array to readable string
     * @param data array
     * @return string
     */
    public static String toBase64(byte[] data)
    {
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }
    /**
     * Hash with sha-256
     * @param data password
     * @return hashed
     */
    public static String hashSha256(String data)
    {
        MessageDigest messageDigest = null;
        try
        {
            messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hh = messageDigest.digest(data.getBytes("UTF-8"));
            return Base64.encodeToString(hh, Base64.NO_PADDING);

        } catch (NoSuchAlgorithmException e)
        {
            Sh.d(e.getMessage());
        } catch (UnsupportedEncodingException e)
        {
            Sh.d(e.getMessage());
        }
        return "";
    }
    /**
     * Hash with sha-256
     * @param data password
     * @return hashed
     */
    public static byte[] hashSha256(byte[] data)
    {
        MessageDigest messageDigest = null;
        try
        {
            messageDigest = MessageDigest.getInstance("SHA-256");
            return messageDigest.digest(data);

        } catch (NoSuchAlgorithmException e)
        {
           Sh.d(e.getMessage());
        }
        return null;
    }
     /**
     * Change encrypted base64 string for decrypting to array
     * @param data string
     * @return array
     */
    public static byte[] fromBase64(String data)
    {
        return Base64.decode(data, Base64.NO_WRAP);
    }

    public static String ivmacToString(byte[] iv)
    {
        String s =Base64.encodeToString(iv, Base64.NO_PADDING);
        s = s.replace("\n", "").replace("\r", "");
        return s;
    }
    public static byte[] ivmacFromString(String iv)
    {
        return Base64.decode(iv, Base64.NO_PADDING);
    }
    /**
     * Creates a random Initialization Vector (IV) of IV_LENGTH_BYTES.
     *
     * @return The byte array of this IV
     * @throws GeneralSecurityException if a suitable RNG is not available
     */
    public static byte[] generateIv() throws GeneralSecurityException {
        return randomBytes(IV_LENGTH_BYTES);
    }

    private static byte[] randomBytes(int length) throws GeneralSecurityException {
        fixPrng();
        SecureRandom random = SecureRandom.getInstance(RANDOM_ALGORITHM);
        byte[] b = new byte[length];
        random.nextBytes(b);
        return b;
    }

    /**
     * Simple constant-time equality of two byte arrays. Used for security to avoid timing attacks.
     * @param a
     * @param b
     * @return true iff the arrays are exactly equal.
     */
    public static boolean constantTimeEq(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
    /**
     * Ensures that the PRNG is fixed. Should be used before generating any keys.
     * Will only run once, and every subsequent call should return immediately.
     */
    private static void fixPrng() {
        try
        {
            if (!prngFixed.get()) {
                synchronized (PrngFixes.class) {
                    if (!prngFixed.get()) {
                        PrngFixes.apply();
                        prngFixed.set(true);
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.e("PassCrypt Exception","This exception is not important, its occurrence depends on device model");
        }
    }
    /**
     * Fixes for the RNG as per
     * http://android-developers.blogspot.com/2013/08/some-securerandom-thoughts.html
     *
     * This software is provided 'as-is', without any express or implied
     * warranty. In no event will Google be held liable for any damages arising
     * from the use of this software.
     *
     * Permission is granted to anyone to use this software for any purpose,
     * including commercial applications, and to alter it and redistribute it
     * freely, as long as the origin is not misrepresented.
     *
     * Fixes for the output of the default PRNG having low entropy.
     *
     * The fixes need to be applied via {@link #apply()} before any use of Java
     * Cryptography Architecture primitives. A good place to invoke them is in
     * the application's {@code onCreate}.
     */
    public static final class PrngFixes {

        private static final int VERSION_CODE_JELLY_BEAN = 16;
        private static final int VERSION_CODE_JELLY_BEAN_MR2 = 18;
        private static final byte[] BUILD_FINGERPRINT_AND_DEVICE_SERIAL = getBuildFingerprintAndDeviceSerial();

        /** Hidden constructor to prevent instantiation. */
        private PrngFixes() {
        }

        /**
         * Applies all fixes.
         *
         * @throws SecurityException if a fix is needed but could not be
         *             applied.
         */
        public static void apply() {
            applyOpenSSLFix();
            installLinuxPRNGSecureRandom();
        }

        /**
         * Applies the fix for OpenSSL PRNG having low entropy. Does nothing if
         * the fix is not needed.
         *
         * @throws SecurityException if the fix is needed but could not be
         *             applied.
         */
        private static void applyOpenSSLFix() throws SecurityException {
            if ((Build.VERSION.SDK_INT < VERSION_CODE_JELLY_BEAN)
                    || (Build.VERSION.SDK_INT > VERSION_CODE_JELLY_BEAN_MR2)) {
                // No need to apply the fix
                return;
            }

            try {
                // Mix in the device- and invocation-specific seed.
                Class.forName("org.apache.harmony.xnet.provider.jsse.NativeCrypto")
                        .getMethod("RAND_seed", byte[].class).invoke(null, generateSeed());

                // Mix output of Linux PRNG into OpenSSL's PRNG
                int bytesRead = (Integer) Class
                        .forName("org.apache.harmony.xnet.provider.jsse.NativeCrypto")
                        .getMethod("RAND_load_file", String.class, long.class)
                        .invoke(null, "/dev/urandom", 1024);
                if (bytesRead != 1024) {
                    throw new IOException("Unexpected number of bytes read from Linux PRNG: "
                            + bytesRead);
                }
            } catch (Exception e) {
                if (ALLOW_BROKEN_PRNG) {
                    Log.w(PrngFixes.class.getSimpleName(), "Failed to seed OpenSSL PRNG", e);
                } else {
                    throw new SecurityException("Failed to seed OpenSSL PRNG", e);
                }
            }
        }

        /**
         * Installs a Linux PRNG-backed {@code SecureRandom} implementation as
         * the default. Does nothing if the implementation is already the
         * default or if there is not need to install the implementation.
         *
         * @throws SecurityException if the fix is needed but could not be
         *             applied.
         */
        private static void installLinuxPRNGSecureRandom() throws SecurityException {
            if (Build.VERSION.SDK_INT > VERSION_CODE_JELLY_BEAN_MR2) {
                // No need to apply the fix
                return;
            }

            // Install a Linux PRNG-based SecureRandom implementation as the
            // default, if not yet installed.
            Provider[] secureRandomProviders = Security.getProviders("SecureRandom.SHA1PRNG");

            // Insert and check the provider atomically.
            // The official Android Java libraries use synchronized methods for
            // insertProviderAt, etc., so synchronizing on the class should
            // make things more stable, and prevent race conditions with other
            // versions of this code.
            synchronized (java.security.Security.class) {
                if ((secureRandomProviders == null)
                        || (secureRandomProviders.length < 1)
                        || (!secureRandomProviders[0].getClass().getSimpleName().equals("LinuxPRNGSecureRandomProvider"))) {
                    Security.insertProviderAt(new LinuxPRNGSecureRandomProvider(), 1);
                }

                // Assert that new SecureRandom() and
                // SecureRandom.getInstance("SHA1PRNG") return a SecureRandom backed
                // by the Linux PRNG-based SecureRandom implementation.
                SecureRandom rng1 = new SecureRandom();
                if (!rng1.getProvider().getClass().getSimpleName().equals("LinuxPRNGSecureRandomProvider")) {
                    if (ALLOW_BROKEN_PRNG) {
                        Log.w(PrngFixes.class.getSimpleName(),
                                "new SecureRandom() backed by wrong Provider: " + rng1.getProvider().getClass());
                        return;
                    } else {
                        throw new SecurityException("new SecureRandom() backed by wrong Provider: "
                                + rng1.getProvider().getClass());
                    }
                }

                SecureRandom rng2 = null;
                try {
                    rng2 = SecureRandom.getInstance("SHA1PRNG");
                } catch (NoSuchAlgorithmException e) {
                    if (ALLOW_BROKEN_PRNG) {
                        Log.w(PrngFixes.class.getSimpleName(), "SHA1PRNG not available", e);
                        return;
                    } else {
                        new SecurityException("SHA1PRNG not available", e);
                    }
                }
                if (!rng2.getProvider().getClass().getSimpleName().equals("LinuxPRNGSecureRandomProvider")) {
                    if (ALLOW_BROKEN_PRNG) {
                        Log.w(PrngFixes.class.getSimpleName(),
                                "SecureRandom.getInstance(\"SHA1PRNG\") backed by wrong" + " Provider: "
                                        + rng2.getProvider().getClass());
                        return;
                    } else {
                        throw new SecurityException(
                                "SecureRandom.getInstance(\"SHA1PRNG\") backed by wrong" + " Provider: "
                                        + rng2.getProvider().getClass());
                    }
                }
            }
        }

        /**
         * {@code Provider} of {@code SecureRandom} engines which passSetup through
         * all requests to the Linux PRNG.
         */
        private static class LinuxPRNGSecureRandomProvider extends Provider {

            public LinuxPRNGSecureRandomProvider() {
                super("LinuxPRNG", 1.0, "A Linux-specific random number provider that uses"
                        + " /dev/urandom");
                // Although /dev/urandom is not a SHA-1 PRNG, some apps
                // explicitly request a SHA1PRNG SecureRandom and we thus need
                // to prevent them from getting the default implementation whose
                // output may have low entropy.
                put("SecureRandom.SHA1PRNG", LinuxPRNGSecureRandom.class.getName());
                put("SecureRandom.SHA1PRNG ImplementedIn", "Software");
            }
        }

        /**
         * {@link SecureRandomSpi} which passes all requests to the Linux PRNG (
         * {@code /dev/urandom}).
         */
        public static class LinuxPRNGSecureRandom extends SecureRandomSpi {

            /*
             * IMPLEMENTATION NOTE: Requests to generate bytes and to mix in a
             * seed are passed through to the Linux PRNG (/dev/urandom).
             * Instances of this class seed themselves by mixing in the current
             * time, PID, UID, build fingerprint, and hardware serial number
             * (where available) into Linux PRNG.
             *
             * Concurrency: Read requests to the underlying Linux PRNG are
             * serialized (on sLock) to ensure that multiple threads do not get
             * duplicated PRNG output.
             */

            private static final File URANDOM_FILE = new File("/dev/urandom");

            private static final Object sLock = new Object();

            /**
             * Input stream for reading from Linux PRNG or {@code null} if not
             * yet opened.
             *
             * @GuardedBy("sLock")
             */
            private static DataInputStream sUrandomIn;

            /**
             * Output stream for writing to Linux PRNG or {@code null} if not
             * yet opened.
             *
             * @GuardedBy("sLock")
             */
            private static OutputStream sUrandomOut;

            /**
             * Whether this engine instance has been seeded. This is needed
             * because each instance needs to seed itself if the client does not
             * explicitly seed it.
             */
            private boolean mSeeded;

            @Override
            protected void engineSetSeed(byte[] bytes) {
                try {
                    OutputStream out;
                    synchronized (sLock) {
                        out = getUrandomOutputStream();
                    }
                    out.write(bytes);
                    out.flush();
                } catch (IOException e) {
                    // On a small fraction of devices /dev/urandom is not
                    // writable Log and ignore.
                    Log.w(PrngFixes.class.getSimpleName(), "Failed to mix seed into "
                            + URANDOM_FILE);
                } finally {
                    mSeeded = true;
                }
            }

            @Override
            protected void engineNextBytes(byte[] bytes) {
                if (!mSeeded) {
                    // Mix in the device- and invocation-specific seed.
                    engineSetSeed(generateSeed());
                }

                try {
                    DataInputStream in;
                    synchronized (sLock) {
                        in = getUrandomInputStream();
                    }
                    synchronized (in) {
                        in.readFully(bytes);
                    }
                } catch (IOException e) {
                    throw new SecurityException("Failed to read from " + URANDOM_FILE, e);
                }
            }

            @Override
            protected byte[] engineGenerateSeed(int size) {
                byte[] seed = new byte[size];
                engineNextBytes(seed);
                return seed;
            }

            private DataInputStream getUrandomInputStream() {
                synchronized (sLock) {
                    if (sUrandomIn == null) {
                        // NOTE: Consider inserting a BufferedInputStream
                        // between DataInputStream and FileInputStream if you need
                        // higher PRNG output performance and can live with future PRNG
                        // output being pulled into this process prematurely.
                        try {
                            sUrandomIn = new DataInputStream(new FileInputStream(URANDOM_FILE));
                        } catch (IOException e) {
                            throw new SecurityException("Failed to open " + URANDOM_FILE
                                    + " for reading", e);
                        }
                    }
                    return sUrandomIn;
                }
            }

            private OutputStream getUrandomOutputStream() throws IOException {
                synchronized (sLock) {
                    if (sUrandomOut == null) {
                        sUrandomOut = new FileOutputStream(URANDOM_FILE);
                    }
                    return sUrandomOut;
                }
            }
        }

        /**
         * Generates a device- and invocation-specific seed to be mixed into the
         * Linux PRNG.
         */
        private static byte[] generateSeed() {
            try {
                ByteArrayOutputStream seedBuffer = new ByteArrayOutputStream();
                DataOutputStream seedBufferOut = new DataOutputStream(seedBuffer);
                seedBufferOut.writeLong(System.currentTimeMillis());
                seedBufferOut.writeLong(System.nanoTime());
                seedBufferOut.writeInt(android.os.Process.myPid());
                seedBufferOut.writeInt(Process.myUid());
                seedBufferOut.write(BUILD_FINGERPRINT_AND_DEVICE_SERIAL);
                seedBufferOut.close();
                return seedBuffer.toByteArray();
            } catch (IOException e) {
                throw new SecurityException("Failed to generate seed", e);
            }
        }

        /**
         * Gets the hardware serial number of this device.
         *
         * @return serial number or {@code null} if not available.
         */
        private static String getDeviceSerialNumber() {
            // We're using the Reflection API because Build.SERIAL is only
            // available since API Level 9 (Gingerbread, Android 2.3).
            try {
                return (String) Build.class.getField("SERIAL").get(null);
            } catch (Exception ignored) {
                return null;
            }
        }

        private static byte[] getBuildFingerprintAndDeviceSerial() {
            StringBuilder result = new StringBuilder();
            String fingerprint = Build.FINGERPRINT;
            if (fingerprint != null) {
                result.append(fingerprint);
            }
            String serial = getDeviceSerialNumber();
            if (serial != null) {
                result.append(serial);
            }
            try {
                return result.toString().getBytes(ENCODING);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 encoding not supported");
            }
        }
    }

}
