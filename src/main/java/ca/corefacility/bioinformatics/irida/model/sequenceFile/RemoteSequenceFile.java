package ca.corefacility.bioinformatics.irida.model.sequenceFile;

import java.nio.file.Path;
import java.util.Date;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import ca.corefacility.bioinformatics.irida.model.IridaThing;
import ca.corefacility.bioinformatics.irida.model.irida.IridaSequenceFile;

@Entity
@Table(name = "remote_sequence_file")
public class RemoteSequenceFile implements IridaSequenceFile, IridaThing {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@NotNull
	@Column(name = "remote_uri")
	private final String remoteURI;

	@Column(name = "file_path", unique = true)
	private Path file;

	@CreatedDate
	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false, name = "created_date")
	private final Date createdDate;

	@LastModifiedDate
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modified_date")
	private Date modifiedDate;

	// Key/value map of additional properties you could set on a sequence file.
	// This may contain optional sequencer specific properties.
	@ElementCollection(fetch = FetchType.EAGER)
	@MapKeyColumn(name = "property_key", nullable = false)
	@Column(name = "property_value", nullable = false)
	@CollectionTable(name = "remote_sequence_file_properties", joinColumns = @JoinColumn(name = "sequence_file_id"), uniqueConstraints = @UniqueConstraint(columnNames = {
			"sequence_file_id", "property_key" }, name = "UK_SEQUENCE_FILE_PROPERTY_KEY"))
	private Map<String, String> optionalProperties;

	public RemoteSequenceFile(SequenceFile base) {
		createdDate = new Date();

		remoteURI = base.getSelfHref();
		optionalProperties = base.getOptionalProperties();
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public Path getFile() {
		return file;
	}

	public void setFile(Path file) {
		this.file = file;
	}

	@Override
	public Map<String, String> getOptionalProperties() {
		return optionalProperties;
	}

	@Override
	public Date getCreatedDate() {
		return createdDate;
	}

	@Override
	public Date getModifiedDate() {
		return modifiedDate;
	}

	@Override
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	@Override
	public String getLabel() {
		return file.getFileName().toString();
	}

}
