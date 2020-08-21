package ca.corefacility.bioinformatics.irida.ria.web.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import ca.corefacility.bioinformatics.irida.model.IridaClientDetails;
import ca.corefacility.bioinformatics.irida.repositories.specification.IridaClientDetailsSpecification;
import ca.corefacility.bioinformatics.irida.ria.web.ajax.dto.ClientTableModel;
import ca.corefacility.bioinformatics.irida.ria.web.ajax.dto.ClientTableRequest;
import ca.corefacility.bioinformatics.irida.ria.web.ajax.dto.CreateClientRequest;
import ca.corefacility.bioinformatics.irida.ria.web.models.tables.TableResponse;
import ca.corefacility.bioinformatics.irida.service.IridaClientDetailsService;

import com.google.common.collect.Sets;

/**
 * UI Service to handle IRIDA Clients
 */
@Component
public class UIClientService {
	private final IridaClientDetailsService clientDetailsService;

	public UIClientService(IridaClientDetailsService clientDetailsService) {
		this.clientDetailsService = clientDetailsService;
	}

	/**
	 * Get a listing of clients based on the table request.
	 *
	 * @param tableRequest Information about the sort and page of the table.
	 * @return Current status of the table
	 */
	public TableResponse<ClientTableModel> getClientList(ClientTableRequest tableRequest) {
		Specification<IridaClientDetails> specification = IridaClientDetailsSpecification.searchClient(
				tableRequest.getSearch());

		Page<IridaClientDetails> page = clientDetailsService.search(specification,
				PageRequest.of(tableRequest.getCurrent(), tableRequest.getPageSize(), tableRequest.getSort()));
		List<ClientTableModel> models = page.getContent()
				.stream()
				.map(client -> new ClientTableModel(client, clientDetailsService.countActiveTokensForClient(client)))
				.collect(Collectors.toList());

		return new TableResponse<>(models, page.getTotalElements());
	}

	public Long createClient(CreateClientRequest request) {
		final String AUTO_APPROVE = "auto";
		final String SCOPE_READ = "read";
		final String SCOPE_WRITE = "write";
		final String GRANT_TYPE_AUTH_CODE = "authorization_code";

		IridaClientDetails client = new IridaClientDetails();
		client.setClientSecret(RandomStringUtils.randomAlphanumeric(42));
		client.setClientId(request.getClientId());
		client.setAccessTokenValiditySeconds(request.getTokenValidity());

		// Let's set up the scopes for this client
		Set<String> scopes = new HashSet<>();
		Set<String> autoScopes = new HashSet<>();
		// 1. Read scope
		if (request.getRead()
				.equals(SCOPE_READ)) {
			scopes.add(SCOPE_READ);
		} else if (request.getRead()
				.equals(AUTO_APPROVE)) {
			scopes.add(SCOPE_READ);
			autoScopes.add(SCOPE_READ);
		}
		// 2. Write scope
		if (request.getWrite()
				.equals(SCOPE_WRITE)) {
			scopes.add(SCOPE_WRITE);
		} else if (request.getWrite()
				.equals(AUTO_APPROVE)) {
			scopes.add(SCOPE_WRITE);
			autoScopes.add(SCOPE_WRITE);
		}
		client.setScope(scopes);
		client.setAutoApprovableScopes(autoScopes);

		// Set the grant type
		client.setAuthorizedGrantTypes(Sets.newHashSet(request.getGrantType()));
		if (request.getGrantType()
				.equals(GRANT_TYPE_AUTH_CODE)) {
			client.setRegisteredRedirectUri(request.getRedirectURI());
		}

		// See if allowed refresh tokens
		if (request.getRefreshToken() > 0) {
			client.getAuthorizedGrantTypes()
					.add("refresh_token");
			client.setRefreshTokenValiditySeconds(request.getRefreshToken());
		}

		client = clientDetailsService.create(client);

		return client.getId();
	}
}
