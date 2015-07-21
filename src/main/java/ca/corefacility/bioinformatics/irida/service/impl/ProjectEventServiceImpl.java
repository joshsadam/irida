package ca.corefacility.bioinformatics.irida.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;
import javax.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import ca.corefacility.bioinformatics.irida.model.event.ProjectEvent;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.user.User;
import ca.corefacility.bioinformatics.irida.repositories.ProjectEventRepository;
import ca.corefacility.bioinformatics.irida.service.ProjectEventService;

/**
 * Implementation of {@link ProjectEventService} using a
 * {@link ProjectEventRepository}
 * 
 *
 */
@Service
public class ProjectEventServiceImpl extends CRUDServiceImpl<Long, ProjectEvent>implements ProjectEventService {

	private ProjectEventRepository repository;

	@Autowired
	public ProjectEventServiceImpl(ProjectEventRepository repository, Validator validator) {
		super(repository, validator, ProjectEvent.class);
		this.repository = repository;
	}

	/**
	 * {@inheritDoc}
	 */
	@PreAuthorize("hasAnyRole('ROLE_ADMIN') or hasPermission(#project, 'canReadProject')")
	public Page<ProjectEvent> getEventsForProject(Project project, Pageable pageable) {
		return repository.getEventsForProject(project, pageable);
	}

	/**
	 * {@inheritDoc}
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN') or principal.username == #user.username")
	public Page<ProjectEvent> getEventsForUser(User user, Pageable pageable) {
		return repository.getEventsForUser(user, pageable);
	}

	/**
	 * {@inheritDoc}
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Override
	public Page<ProjectEvent> list(int page, int size, Direction order, String... sortProperties)
			throws IllegalArgumentException {
		return super.list(page, size, order, sortProperties);
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Transactional
	public List<ProjectEvent> getEventsToEmailToUser(User user, long cooldown) {

		// get the current time minus the cool down. We'll use this to see if
		// there's any events currently happening
		Date cooldownDate = new Date(new Date().getTime() - cooldown);

		Date lastSubscriptionEmail = user.getLastSubscriptionEmail();

		List<ProjectEvent> eventsForUserAfterDate = repository.getEventsForUserAfterDate(user,
				lastSubscriptionEmail);

		// if there's any events within the cool down, do nothing
		Optional<ProjectEvent> datesWithinCooldown = eventsForUserAfterDate.stream()
				.filter((e) -> e.getCreatedDate().after(cooldownDate)).findAny();

		if (datesWithinCooldown.isPresent()) {
			eventsForUserAfterDate = Lists.newArrayList();
		}

		return eventsForUserAfterDate;
	}
}
