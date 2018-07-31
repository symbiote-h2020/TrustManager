package eu.h2020.symbiote.tm.interfaces.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.model.internal.FederatedResource;
import eu.h2020.symbiote.cloud.model.internal.FederationInfoBean;
import eu.h2020.symbiote.cloud.model.internal.ResourceSharingInformation;
import eu.h2020.symbiote.cloud.model.internal.ResourcesAddedOrUpdatedMessage;
import eu.h2020.symbiote.cloud.model.internal.ResourcesDeletedMessage;
import eu.h2020.symbiote.cloud.trust.model.TrustEntry;
import eu.h2020.symbiote.model.cim.Resource;
import eu.h2020.symbiote.model.mim.Federation;
import eu.h2020.symbiote.model.mim.FederationMember;
import eu.h2020.symbiote.tm.repositories.SLAViolationRepository;
import eu.h2020.symbiote.tm.repositories.TrustRepository;

@RunWith(SpringRunner.class)
public class EventUpdateListenerTest {
	@Mock
	private TrustRepository trustRepository;

	@Mock
	private SLAViolationRepository violationRepository;

	@InjectMocks
	private final EventUpdateListener service = new EventUpdateListener();

	@Before
	public void setup() throws Exception {
		ReflectionTestUtils.setField(service, "ownPlatformId", "p-123");
	}

	@Test
	public void testReceiveOwnSharedResources0() throws Exception {
		service.receiveOwnSharedResources(new ArrayList<>());
		service.receiveOwnSharedResources(null);
		Mockito.verify(trustRepository, Mockito.never()).exists(Mockito.any(String.class));
		Mockito.verify(trustRepository, Mockito.never()).save(Mockito.any(TrustEntry.class));
	}

	@Test
	public void testReceiveOwnSharedResources1() throws Exception {
		List<CloudResource> resList = new ArrayList<>();
		CloudResource cr1 = new CloudResource();
		cr1.setInternalId("r-123");
		resList.add(cr1);

		Mockito.when(trustRepository.exists(Mockito.anyString())).thenReturn(false);
		service.receiveOwnSharedResources(resList);
		Mockito.verify(trustRepository, Mockito.times(1)).save(Mockito.any(TrustEntry.class));
	}

	@Test
	public void testReceiveOwnSharedResources2() throws Exception {
		List<CloudResource> resList = new ArrayList<>();
		CloudResource cr1 = new CloudResource();
		cr1.setInternalId("r-123");
		resList.add(cr1);

		CloudResource cr2 = new CloudResource();
		cr1.setInternalId("r-456");
		resList.add(cr2);

		Mockito.when(trustRepository.exists(Mockito.anyString())).thenReturn(false).thenReturn(true);
		service.receiveOwnSharedResources(resList);
		Mockito.verify(trustRepository, Mockito.times(1)).save(Mockito.any(TrustEntry.class));
	}

	@Test
	public void testReceiveOwnUnsharedResources0() throws Exception {
		service.receiveOwnUnsharedResources(new ArrayList<>());
		service.receiveOwnUnsharedResources(null);
		Mockito.verify(trustRepository, Mockito.never()).delete(Mockito.anyString());
	}

	@Test
	public void testReceiveOwnUnsharedResources1() throws Exception {
		List<String> resList = new ArrayList<>();
		resList.add("r-123");

		service.receiveOwnUnsharedResources(resList);
		Mockito.verify(trustRepository, Mockito.times(1)).delete(Mockito.anyString());
	}

	@Test
	public void testReceiveForeignSharedResources0() throws Exception {
		ResourcesAddedOrUpdatedMessage sr = new ResourcesAddedOrUpdatedMessage(null);

		service.receiveForeignSharedResources(sr);
		service.receiveForeignSharedResources(null);
		Mockito.verify(trustRepository, Mockito.never()).save(Mockito.any(TrustEntry.class));
	}

	@Test
	public void testReceiveForeignSharedResources1() throws Exception {
		FederationInfoBean fib = new FederationInfoBean();
		fib.setResourceTrust(1.2);

		Map<String, ResourceSharingInformation> sharingInformation = new HashMap<>();
		ResourceSharingInformation rsi = new ResourceSharingInformation();
		rsi.setSymbioteId("sr@345");
		sharingInformation.put("123", rsi);
		fib.setSharingInformation(sharingInformation);

		fib.setAggregationId("sr@345");
		CloudResource cr = new CloudResource();
		cr.setFederationInfo(fib);
		Resource r = new Resource();
		r.setInterworkingServiceURL("https://");
		cr.setResource(r);
		FederatedResource fr = new FederatedResource("sr@123", cr, 4.0);
		List<FederatedResource> frList = new ArrayList<>();
		frList.add(fr);

		ResourcesAddedOrUpdatedMessage sr = new ResourcesAddedOrUpdatedMessage(frList);

		service.receiveForeignSharedResources(sr);
		Mockito.verify(trustRepository, Mockito.times(2)).save(Mockito.any(TrustEntry.class));
	}

	@Test
	public void testReceiveForeignUnsharedResources0() throws Exception {
		ResourcesDeletedMessage sr = new ResourcesDeletedMessage(null);

		service.receiveForeignUnsharedResources(sr);
		service.receiveForeignUnsharedResources(null);
		Mockito.verify(trustRepository, Mockito.never()).delete(Mockito.anyString());
	}

	@Test
	public void testReceiveForeignUnsharedResources1() throws Exception {
		Set<String> map = new HashSet<String>();
		map.add("r-123");
		ResourcesDeletedMessage sr = new ResourcesDeletedMessage(map);

		service.receiveForeignUnsharedResources(sr);
		Mockito.verify(trustRepository, Mockito.times(2)).delete(Mockito.anyString());
	}

	@Test
	public void testReceiveFederationUpdate0() throws Exception {
		service.receiveFederationCreated(new Federation());
		service.receiveFederationCreated(null);
		Mockito.verify(trustRepository, Mockito.never()).exists(Mockito.anyString());
		Mockito.verify(trustRepository, Mockito.never()).save(Mockito.any(TrustEntry.class));
	}

	@Test
	public void testReceiveFederationUpdate1() throws Exception {
		Federation fed = new Federation();
		List<FederationMember> members = new ArrayList<>();
		members.add(new FederationMember("p-1", "https://..."));
		members.add(new FederationMember("p-2", "https://..."));
		fed.setMembers(members);

		Mockito.when(trustRepository.exists(Mockito.anyString())).thenReturn(false).thenReturn(true);
		service.receiveFederationUpdated(fed);
		Mockito.verify(trustRepository, Mockito.times(1)).save(Mockito.any(TrustEntry.class));
	}
}
