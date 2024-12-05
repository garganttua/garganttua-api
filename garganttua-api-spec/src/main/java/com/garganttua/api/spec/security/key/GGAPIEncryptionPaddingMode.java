package com.garganttua.api.spec.security.key;

public enum GGAPIEncryptionPaddingMode {

	NO_PADDING("NoPadding"),
    PKCS5_PADDING("PKCS5Padding"),
    ISO10126_PADDING("ISO10126Padding"),
    PKCS7_PADDING("PKCS7Padding"),
	PKCS1_PADDING("PKCS1Padding"),
	NONE("None");

    private final String padding;

    GGAPIEncryptionPaddingMode(String padding) {
        this.padding = padding;
    }

    public String getPadding() {
        return padding;
    }
	
}
