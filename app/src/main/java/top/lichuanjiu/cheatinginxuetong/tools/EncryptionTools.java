package top.lichuanjiu.cheatinginxuetong.tools;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptionTools {
    public static String md5WithSalt(String input, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String salted = input + salt;
            byte[] hashBytes = md.digest(salted.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            StringBuilder hashtext = new StringBuilder(no.toString(16));
            while (hashtext.length() < 32) {
                hashtext.insert(0, "0");
            }
            return hashtext.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }


    public static String createDataPacket(String data, long timestamp, long sequence, String secretKey) throws NoSuchAlgorithmException {
        // 创建数据包
        String packet = data + "|" + timestamp + "|" + sequence;

        // 计算 HMAC
        String hmac = calculateHMAC(packet, secretKey);

        // 返回包含 HMAC 的数据包
        return packet + "|" + hmac;
    }

    private static String calculateHMAC(String data, String key) throws NoSuchAlgorithmException {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(key.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes());
            return bytesToHex(hmacBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }


}
