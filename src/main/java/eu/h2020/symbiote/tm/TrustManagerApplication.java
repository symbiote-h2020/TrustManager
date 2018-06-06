package eu.h2020.symbiote.tm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author RuggenthalerC
 *
 *         Main entry point to start the trust manager.
 */
@EnableDiscoveryClient
@EnableAutoConfiguration
@SpringBootApplication
public class TrustManagerApplication {

	public static void main(String[] args) {
		WaitForPort.waitForServices(WaitForPort.findProperty("SPRING_BOOT_WAIT_FOR_SERVICES"));
		SpringApplication.run(TrustManagerApplication.class, args);
	}
}
