package ca.corefacility.bioinformatics.irida.service.impl;

import ca.corefacility.bioinformatics.irida.model.Project;
import ca.corefacility.bioinformatics.irida.model.Role;
import ca.corefacility.bioinformatics.irida.model.User;
import ca.corefacility.bioinformatics.irida.repositories.memory.ProjectMemoryRepository;
import ca.corefacility.bioinformatics.irida.repositories.memory.UserMemoryRepository;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test the business logic for the {@link ProjectServiceImpl}.
 *
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 */
public class ProjectServiceImplTest {

    private ProjectService projectService;
    private UserMemoryRepository userRepository;

    @Before
    public void setUp() {
        userRepository = new UserMemoryRepository();
        projectService = new ProjectServiceImpl(new ProjectMemoryRepository(), userRepository);
    }

    /**
     * When a user is added to a project, they should be added by role. The user
     * should also have the project added to their projects collection.
     */
    @Test
    public void testAddUserToProject() {
        Project p = new Project();
        User u = new User();
        Role r = new Role();

        u.setUsername("super-user");
        r.setName("ROLE_MANAGER");
        
        // create the user and project
        p = projectService.create(p);
        u = userRepository.create(u);

        // add the user to the project
        projectService.addUserToProject(p, u, r);
        
        // get the new versions of the files out of the database
        p = projectService.read(p.getId());
        u = userRepository.read("http://api.irida.ca/User/super-user");

        // assert that the changes were correctly made
        assertEquals(1, p.getUsersByRole(r).size());
        assertTrue(u.getProjects().containsKey(p));
    }
}
