package midgard.drive;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyRep;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Date;
import java.util.Enumeration;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.cert.Certificate;

public class MidgardRequestManager {

	
	
	public Certificate importCertificate(File file) {
			try {
				FileInputStream is = new FileInputStream(file);
				CertificateFactory cf = CertificateFactory.getInstance("RSA");
				Certificate cert = cf.generateCertificate(is);
				return cert;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
	}
	
	public PrivateKey getPrivateKey() {
		KeyStore keyStore;
		Key key = null;
		try {
			String home = System.getProperty("user.home");
			FileInputStream file_inputstream = new FileInputStream(home + "/Desktop/Certificates.p12");
			keyStore = KeyStore.getInstance("RSA");
			keyStore.load(file_inputstream, null);
			System.out.println("Keystore size " + keyStore.size());
			Enumeration<String> aliases = keyStore.aliases();
			while(aliases.hasMoreElements()) {
				System.out.println(aliases.nextElement());
			}
			// Certificate cert= keyStore.getCertificate("brian");
			key=keyStore.getKey("google",null);
			System.out.println("Key information " + key.getAlgorithm() + " " + key.getFormat());
			Certificate[] certChain = keyStore.getCertificateChain("google");
			// System.out.println(certChain[0]);
			System.out.println(certChain.length);
		} catch (Exception e) {
			System.err.println("Exception:- " + e);
		}
		PrivateKey pkey = null;
		return pkey;
	}
	
	public String createSignature(String headerAndClaim) {
		Cipher rsa256 = null;
		
			try {
				byte[] bytes = headerAndClaim.getBytes("UTF-8");
				PrivateKey pkey = getPrivateKey();
				Signature sig = Signature.getInstance("RSA");
				// sig.initSign(getPrivateKey());
				
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
	}
	
	public static void main(String[] args) {
		MidgardRequestManager mrm = new MidgardRequestManager();
		String resourceID = "LOLTEST";
		
		JSONWriter headWriter = new JSONWriter(new StringWriter());
		String header = null;
		JSONWriter claimWriter = new JSONWriter(new StringWriter());
		String claim = null;
		JSONWriter signatureWriter = new JSONWriter(new StringWriter());
		String signature = null;
		
		long base = System.currentTimeMillis();
		long exp = base + 3600;
		
		try {
			header = headWriter.object()
					.key("alg").value("RS256")
					.key("typ").value("JWT")
					.endObject().toString();
			claim = claimWriter.object()
					.key("iss").value("1078037152741@developer.gserviceaccount.com")
					.key("scope").value("https://www.googleapis.com/auth/drive.file")
					.key("aud").value("https://accounts.google.com/o/oauth2/token")
					.key("exp").value(exp)
					.key("iat").value(base)
					.endObject().toString();
			signature = signatureWriter.object().endObject().toString();
			String test = mrm.createSignature(header+"."+claim);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
