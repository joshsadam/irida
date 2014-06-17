package ca.corefacility.bioinformatics.irida.repositories.remote.model;

import ca.corefacility.bioinformatics.irida.model.user.User;
import ca.corefacility.bioinformatics.irida.repositories.remote.model.resource.RemoteResource;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A user read from a remote irida API
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 */
public class RemoteUser extends User implements RemoteResource{

	private static final long serialVersionUID = 9194044903836613576L;
	private List<Map<String,String>> links;
	
	@Override
	public String getIdentifier() {
		return this.getId().toString();
	}

	@Override
	public void setIdentifier(String identifier) {
		this.setId(Long.parseLong(identifier));
	}

	@Override
	public List<Map<String, String>> getLinks() {
		return links;
	}

	@Override
	public void setLinks(List<Map<String, String>> links) {
		this.links = links;
	}

	@Override
	public Date getDateCreated() {
		return this.getTimestamp();
	}
	
}
