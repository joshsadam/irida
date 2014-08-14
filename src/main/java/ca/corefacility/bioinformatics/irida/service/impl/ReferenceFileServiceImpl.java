package ca.corefacility.bioinformatics.irida.service.impl;

import java.util.List;
import java.util.Map;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.corefacility.bioinformatics.irida.exceptions.EntityExistsException;
import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.exceptions.InvalidPropertyException;
import ca.corefacility.bioinformatics.irida.model.joins.Join;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.project.ReferenceFile;
import ca.corefacility.bioinformatics.irida.repositories.joins.project.ProjectReferenceFileJoinRepository;
import ca.corefacility.bioinformatics.irida.repositories.referencefile.ReferenceFileRepository;
import ca.corefacility.bioinformatics.irida.service.ReferenceFileService;

/**
 * Service for storing and reading {@link ReferenceFile} objects
 * 
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 *
 */
@Service
public class ReferenceFileServiceImpl extends CRUDServiceImpl<Long, ReferenceFile> implements ReferenceFileService {
	private final ProjectReferenceFileJoinRepository prfjRepository;

	@Autowired
	public ReferenceFileServiceImpl(ReferenceFileRepository repository,
			ProjectReferenceFileJoinRepository prfjRepository, Validator validator) {
		super(repository, validator, ReferenceFile.class);
		this.prfjRepository = prfjRepository;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReferenceFile read(Long id) throws EntityNotFoundException {
		return super.read(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ReferenceFile update(Long id, Map<String, Object> updatedFields) throws ConstraintViolationException,
			EntityExistsException, InvalidPropertyException {
		return super.update(id, updatedFields);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Join<Project, ReferenceFile>> getReferenceFilesForProject(Project project) {
		return prfjRepository.findReferenceFilesForProject(project);
	}

	/**
	 * @throws UnsupportedOperationException
	 *             Reference files cannot be created by themselves. They must be
	 *             created in a project.
	 */
	@Override
	public ReferenceFile create(ReferenceFile object) throws ConstraintViolationException, EntityExistsException {
		throw new UnsupportedOperationException("Reference file must be created in a project");
	}

	@Override
	public void delete(Long id) throws EntityNotFoundException {
		throw new UnsupportedOperationException("Reference file must be deleted from a project");
	}

}
