package eu.h2020.symbiote.tm.interfaces.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import eu.h2020.symbiote.security.ComponentSecurityHandlerFactory;
import eu.h2020.symbiote.security.commons.SecurityConstants;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import eu.h2020.symbiote.security.handler.IComponentSecurityHandler;

/**
 * @author RuggenthalerC
 *
 *         Handling Security Requests.
 */
@Component
public class AuthManager {

	private static final Logger logger = LoggerFactory.getLogger(AuthManager.class);
	private IComponentSecurityHandler securityHandler = null;

	private final boolean isSecurityEnabled;

	@Autowired
	public AuthManager(@Value("${symbIoTe.component.username}") String componentOwnerName,
			@Value("${symbIoTe.component.password}") String componentOwnerPassword, @Value("${symbIoTe.localaam.url}") String aamAddress,
			@Value("${symbIoTe.component.clientId}") String clientId, @Value("${symbIoTe.component.keystore.path}") String keystoreName,
			@Value("${symbIoTe.component.keystore.password}") String keystorePass, @Value("${symbIoTe.aam.integration}") boolean isSecurityEnabled)
			throws SecurityHandlerException {
		this.isSecurityEnabled = isSecurityEnabled;

		if (this.isSecurityEnabled) {
			securityHandler = ComponentSecurityHandlerFactory.getComponentSecurityHandler(keystoreName, keystorePass, clientId, aamAddress, componentOwnerName,
					componentOwnerPassword);
		} else {
			logger.info("Security Request validation is disabled");
		}
	}

	/**
	 * Generates Security headers for response.
	 * 
	 * @return {@see HttpHeaders}
	 */
	public HttpHeaders generateRequestHeaders() {
		HttpHeaders httpHeaders = new HttpHeaders();

		if (isSecurityEnabled) {
			try {
				SecurityRequest securityRequest = securityHandler.generateSecurityRequestUsingLocalCredentials();
				securityRequest.getSecurityRequestHeaderParams().entrySet().forEach(entry -> {
					httpHeaders.add(entry.getKey(), entry.getValue());
				});
			} catch (Exception e) {
				logger.warn("Security request generation failed", e);
			}
		}

		return httpHeaders;
	}

	/**
	 * Verify security headers in response.
	 * 
	 * @param componentId
	 *            component sender
	 * @param platformId
	 *            platform ID or SecurityConstants.CORE_AAM_INSTANCE_ID for core.
	 * @param httpHeaders
	 *            received {@see HttpHeaders}
	 * @return true if verified else false
	 */
	public boolean verifyResponseHeaders(String componentId, String platformId, HttpHeaders httpHeaders) {
		boolean isResponseVerified = false;

		try {
			String resp = httpHeaders.get(SecurityConstants.SECURITY_RESPONSE_HEADER).get(0);
			isResponseVerified = securityHandler.isReceivedServiceResponseVerified(resp, componentId, platformId);
		} catch (Exception e) {
			logger.warn("Exception during verifying service response", e);
		}

		return isResponseVerified;
	}
}