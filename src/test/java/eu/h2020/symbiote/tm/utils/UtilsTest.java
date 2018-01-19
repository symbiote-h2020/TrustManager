package eu.h2020.symbiote.tm.utils;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.h2020.symbiote.model.mim.Federation;
import eu.h2020.symbiote.model.mim.FederationMember;

public class UtilsTest {

	@Test
	public void testConvertObjToJSON() throws Exception {

		Federation fed = new Federation();
		fed.setId("fedId");
		fed.setName("fedName");
		fed.setPublic(false);
		fed.setSlaDefinition("FedSla");

		List<FederationMember> members = new ArrayList<FederationMember>();
		members.add(new FederationMember("123", "/url/123"));
		members.add(new FederationMember("456", "/url/456"));

		fed.setMembers(members);

		String serialFed = Utils.convertObjectToJson(fed);

		Assert.assertTrue(serialFed.startsWith("{"));
		Assert.assertTrue(serialFed.endsWith("}"));
		Assert.assertTrue(serialFed.contains("\"id\":" + "\"" + fed.getId() + "\""));
		Assert.assertTrue(serialFed.contains("\"name\":" + "\"" + fed.getName() + "\""));
		Assert.assertTrue(serialFed.contains("\"slaDefinition\":" + "\"" + fed.getSlaDefinition() + "\""));
		Assert.assertTrue(serialFed.contains("\"public\":" + fed.isPublic()));

		fed.getMembers().forEach(member -> {
			Assert.assertTrue(serialFed
					.contains("\"platformId\":\"" + member.getPlatformId() + "\",\"interworkingServiceURL\":\"" + member.getInterworkingServiceURL() + "\"}"));
		});
		Assert.assertEquals(2, fed.getMembers().size());
	}
}