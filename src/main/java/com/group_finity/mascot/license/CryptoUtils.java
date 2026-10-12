package com.group_finity.mascot.license;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class CryptoUtils {
   private static final String ALGORITHM = "RSA";
   private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
   private static final int KEY_SIZE = 2048;

   public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
      keyGen.initialize(2048);
      return keyGen.generateKeyPair();
   }

   public static String publicKeyToString(PublicKey publicKey) {
      return Base64.getEncoder().encodeToString(publicKey.getEncoded());
   }

   public static String privateKeyToString(PrivateKey privateKey) {
      return Base64.getEncoder().encodeToString(privateKey.getEncoded());
   }

   public static PublicKey stringToPublicKey(String keyString) throws Exception {
      byte[] keyBytes = Base64.getDecoder().decode(keyString);
      X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      return keyFactory.generatePublic(spec);
   }

   public static PrivateKey stringToPrivateKey(String keyString) throws Exception {
      byte[] keyBytes = Base64.getDecoder().decode(keyString);
      PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      return keyFactory.generatePrivate(spec);
   }

   public static String signData(String data, PrivateKey privateKey) throws Exception {
      Signature signature = Signature.getInstance("SHA256withRSA");
      signature.initSign(privateKey);
      signature.update(data.getBytes(StandardCharsets.UTF_8));
      byte[] signatureBytes = signature.sign();
      return Base64.getEncoder().encodeToString(signatureBytes);
   }

   public static boolean verifySignature(String data, String signatureString, PublicKey publicKey) {
      try {
         Signature signature = Signature.getInstance("SHA256withRSA");
         signature.initVerify(publicKey);
         signature.update(data.getBytes(StandardCharsets.UTF_8));
         byte[] signatureBytes = Base64.getDecoder().decode(signatureString);
         return signature.verify(signatureBytes);
      } catch (Exception var5) {
         return false;
      }
   }

   public static String generateKeyId() {
      SecureRandom random = new SecureRandom();
      byte[] bytes = new byte[8];
      random.nextBytes(bytes);
      return Base64.getEncoder().encodeToString(bytes).substring(0, 11);
   }

   public static String sha256Hash(String input) throws Exception {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
   }
}
