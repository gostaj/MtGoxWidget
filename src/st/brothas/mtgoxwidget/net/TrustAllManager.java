package st.brothas.mtgoxwidget.net;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/*
 * Taken from: http://meneameandroid.googlecode.com/svn/trunk/src/com/dcg/auth/TrustAllManager.java
 * Big thanks to B.Thax.DCG and pakore!
 *
 */
public class TrustAllManager implements X509TrustManager {
	public void checkClientTrusted(X509Certificate[] cert, String authType)
			throws CertificateException {
	}

	public void checkServerTrusted(X509Certificate[] cert, String authType)
			throws CertificateException {
	}

	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}
}

