class Encryptor {

    String encrypt(String algorithm, String text) {

        if ("AES".equals(algorithm))
            return encryptWithAES(text);

        if ("BLOWFISH".equals(algorithm))
            return encryptWithBlowfish(text);

        if ("DES".equals(algorithm))
            return encryptWithDES(text);

        throw new IllegalArgumentException();
    }

    private String encryptWithAES(String text) {
        return "AES:" + text;
    }

    private String encryptWithBlowfish(String text) {
        return "BLOWFISH:" + text;
    }

    private String encryptWithDES(String text) {
        return "DES:" + text;
    }
}
